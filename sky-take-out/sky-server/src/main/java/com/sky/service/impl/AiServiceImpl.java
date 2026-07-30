package com.sky.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.service.AiService;
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

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用ai进行对话
     * @param userInput
     * @return
     */
    public String chat(String userInput) {
        log.info("实际请求的 URL: {}", apiUrl);
        try{
            //1.构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            //2.构造请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            // 添加系统提示词
            messages.add(Map.of("role", "system", "content", buildSystemPrompt()));
            // 添加用户输入
            messages.add(Map.of("role", "user", "content", userInput));

            Map<String, Object> input = new HashMap<>();
            input.put("messages", messages);
            requestBody.put("input", input);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("result_format", "message");
            requestBody.put("parameters", parameters);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            //3.发送请求
            log.info("调用 qianwen API，输入：{}", userInput);
            String responseJson = restTemplate.postForObject(apiUrl, entity, String.class);

            //4.解析响应
            JsonNode root = objectMapper.readTree(responseJson);
            String reply = root.path("output")
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            log.info("AI 回复：{}", reply);
            return reply;
        }
        catch(Exception e){
            log.error("调用 qianwen 接口失败", e);
            return "抱歉，AI 服务暂时不可用，请稍后再试。";
        }
    }

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

        //组合系统提示词
        return "你是苍穹外卖的智能助手，请用友好专业的语气回答用户关于菜品、菜单、订单的问题。" +
                "如果用户询问推荐，可以根据菜单给出建议。" +
                "如果用户询问的菜品不在菜单中，请礼貌地告知暂无此菜品，并推荐其他菜品。\n" +
                menuDesc.toString();
    }
}
