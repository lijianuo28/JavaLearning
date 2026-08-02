package com.sky.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.service.AiService;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.model}")
    private String model;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private OrderMapper orderMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用ai进行对话
     * @param userInput
     * @return
     */
    public String chat(String userInput) {
        try{
            //1.第一次调用AI
            String firstReply = callAI(userInput);
            log.info("第一次AI回复：{}", firstReply);

            //2.检查是否包含动作指令（手动 function calling）
            String actionJson = extractActionJson(firstReply);
            if(actionJson != null){
                //解析动作
                JsonNode actionNode = objectMapper.readTree(actionJson);
                String action = actionNode.path("action").asText();

                //处理查询最近订单
                if("query_latest_order".equals(action)){
                    //获取当前用户id
                    Long userId = BaseContext.getCurrentId();
                    if(userId == null){
                        return "请先登录后再查询";
                    }
                    //查询订单，仅限当前用户
                    List<Orders> activeOrders = orderMapper.getActiveOrder(userId);
                    String orderInfo;
                    if(activeOrders == null){
                        orderInfo = "您当前没有进行中的订单。";
                    } else if(activeOrders.size() == 1){
                        Orders order = activeOrders.get(0);
                        String statusDesc = getOrderStatusDesc(order);
                        orderInfo = String.format(
                                "您的订单 %s 当前状态：%s，下单时间：%s，总金额：%.2f元。",
                                order.getNumber(),
                                statusDesc,
                                order.getOrderTime(),
                                order.getAmount().doubleValue()
                        );
                    } else{
                        //多个订单，列出所有
                        StringBuilder sb = new StringBuilder("您当前有 " + activeOrders.size() + " 个进行中的订单：\n");
                        int index = 1;
                        for (Orders order : activeOrders) {
                            String statusDesc = getOrderStatusDesc(order);
                            sb.append(index++).append(". 订单号 ").append(order.getNumber())
                                    .append("，状态：").append(statusDesc)
                                    .append("，下单时间：").append(order.getOrderTime())
                                    .append("，金额：").append(order.getAmount().doubleValue()).append("元\n");
                        }
                        orderInfo = sb.toString();
                    }

                    //第二次调用ai，让ai组织友好回复
                    String finalReply = callAI("用户询问订单状态，查询结果如下：" + orderInfo + "，请用简洁、友好的语气告诉用户。");
                    log.info("订单问题调用ai：{}", finalReply);
                    return finalReply;
                }

            }

            //无动作指令，直接返回第一次回复
            return firstReply;
        }
        catch(Exception e){
            log.error("调用 qianwen 接口失败", e);
            return "抱歉，AI 服务暂时不可用，请稍后再试。";
        }
    }

    /**
     * 调用ai接口
     * @param userMessage
     * @return
     */
    private String callAI(String userMessage) throws Exception{
        //1.创建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        //2.创建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        //3.构造消息队列
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt()));
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> input = new HashMap<>();
        input.put("messages", messages);
        requestBody.put("input", input);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("result_format", "message");
        requestBody.put("parameters", parameters);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        //发送请求
        String responseJson = restTemplate.postForObject(apiUrl, entity, String.class);

        //解析响应
        JsonNode root = objectMapper.readTree(responseJson);
        String reply = root.path("output")
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
        return reply;
    }

    /**
     * 构建系统提示词
     * @return
     */
    private String buildSystemPrompt(){
        //1.查询所有在售菜品
        List<Dish> dishList = dishMapper.getByStatus(StatusConstant.ENABLE);

        //2.构建菜单描述字符串
        StringBuilder menuDesc = new StringBuilder();
        menuDesc.append("我们餐厅提供一下菜品：\n");

        for (Dish dish : dishList) {
            menuDesc.append("- ").append(dish.getName())
                    .append(" （价格：").append(dish.getPrice()).append("元）\n");
        }

        String orderRule = "【重要】如果用户询问订单状态（例如：我的订单到哪了？订单号xxx怎么样了？），\n" +
                            "你不需要直接回答，而是输出一个 JSON 对象，格式如下：\n" +
                            "{\"action\": \"query_latest_order\"}\n" +
                            "注意：你不需要询问订单号，直接触发查询动作即可。\n" +
                            "后端会自动查询当前用户最近一条进行中的订单。";

        //组合系统提示词
        return "你是苍穹外卖的智能助手，请用友好专业的语气回答用户关于菜品、菜单、订单的问题。" +
                "如果用户询问推荐，可以根据菜单给出建议。" +
                "如果用户询问的菜品不在菜单中，请礼貌地告知暂无此菜品，并推荐其他菜品。\n" +
                menuDesc.toString() + orderRule;
    }

    /**
     * 从文本中提取动作JSON
     * @param text
     * @return
     */
    private String extractActionJson(String text){
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if(start != -1 && end != -1 && end > start){
            String possibleJson = text.substring(start, end + 1);
            try {
                JsonNode node = objectMapper.readTree(possibleJson);
                if (node.has("action") && "query_latest_order".equals(node.path("action").asText()))
                    return possibleJson;
            }catch (Exception e){
                //不是有效json，忽略
            }
        }
        return null;
    }

    /**
     * 将订单状态码转为文字描述
     */
    private String getOrderStatusDesc(Orders order) {
        Integer status = order.getStatus();
        // 假设状态常量如下（根据你的项目调整）
        // 1=待支付 2=待接单 3=已接单 4=派送中 5=已完成 6=已取消
        switch (status) {
            case 1: return "待支付";
            case 2: return "待接单";
            case 3: return "已接单";
            case 4: return "派送中";
            case 5: return "已完成";
            case 6: return "已取消";
            default: return "未知状态";
        }
    }

}
