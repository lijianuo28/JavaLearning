package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //处理业务异常（地址簿为空，购物车数据为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        ShoppingCart shoppingCart = new ShoppingCart();
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if(shoppingCartList == null || shoppingCartList.size() == 0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
        orders.setUserId(userId);
        orders.setUserName(userMapper.getById(userId).getName());

        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        //向订单明细表插入n条数据
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());//设置当前订单明细关联的订单id
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);

        //清空当前用户的购物车
        shoppingCartMapper.deleteByUserId(userId);

        //封装VO，返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付（模拟支付）
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // ========== 1. 获取当前登录用户（模拟支付不需要，但保留以备后用） ==========
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);  // 其实模拟支付用不到，但保留无妨

        // ========== 2. 根据订单号查询订单 ==========
        String orderNumber = ordersPaymentDTO.getOrderNumber();
        Orders ordersDB = orderMapper.getByNumber(orderNumber);
        if (ordersDB == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // ========== 3. 检查订单是否已支付（防止重复支付） ==========
        if (ordersDB.getPayStatus() == Orders.PAID) {
            throw new OrderBusinessException("该订单已支付");
        }

        // ========== 4. 模拟支付成功：直接更新订单状态 ==========
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)      // 订单状态：待接单
                .payStatus(Orders.PAID)              // 支付状态：已支付
                .checkoutTime(LocalDateTime.now())   // 结账时间
                .build();
        orderMapper.update(orders);

        // ========== 5. 构造模拟的微信支付返回参数（给前端用的） ==========
        OrderPaymentVO vo = new OrderPaymentVO();
        vo.setNonceStr("mockNonceStr");            // 随机字符串（模拟值）
        vo.setPackageStr("mockPackage");           // 统一下单接口返回的 prepay_id 参数值（模拟）
        vo.setSignType("MD5");                     // 签名类型（模拟）
        vo.setTimeStamp(String.valueOf(System.currentTimeMillis())); // 时间戳
        vo.setPaySign("mockPaySign");              // 签名（模拟）

        // ========== 6. 返回模拟数据给前端 ==========
        return vo;
    }

    /**
     * 支付成功回调（模拟支付时留空，真实支付时再实现）
     */
    @Override
    public void paySuccess(String outTradeNo) {
        log.info("模拟支付回调接收，订单号：{}，模拟支付已直接更新状态，此处不做处理", outTradeNo);
        // 真实支付时，可在此处更新订单状态、来单提醒等
    }

    /**
     * 查询历史订单
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    public PageResult pageQuery(int page, int pageSize, Integer status) {
        //开始分页
        PageHelper.startPage(page, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        //分页条件查询
        Page<Orders> page1 = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();

        //查询订单明细，并封装进VO进行响应
        if(page1 != null && page1.size() > 0){
            for (Orders orders : page1) {
                Long orderId = orders.getId();

                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
        }
        return new PageResult(page1.getTotal(), list);
    }

    /**
     * 查询订单明细
     * @param id
     * @return
     */
    public OrderVO details(Long id) {
        Orders orders = orderMapper.getById(id);

        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 取消订单
     * @param id
     */
    public void cancel(Long id) {
        //根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        //校验订单是否存在
        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //判断当前订单状态，1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if(ordersDB.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //若当前订单处于接单状态，需退款，本项目为模拟支付，无需退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
//            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额
//
            //支付状态修改为 退款
            ordersDB.setPayStatus(Orders.REFUND);
        }

        //更新订单状态、取消原因、取消时间
        ordersDB.setStatus(Orders.CANCELLED);
        ordersDB.setCancelTime(LocalDateTime.now());
        ordersDB.setCancelReason("用户取消");
        orderMapper.update(ordersDB);
    }

    /**
     * 再来一单
     * @param id
     */
    public void repetiton(Long id) {
        //查询当前用户id
        Long userId = BaseContext.getCurrentId();

        //查询当前订单内容
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        //将当前订单内容加入购物车
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 条件搜索订单
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult conditionService(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 各个状态的订单数量查询
     * @return
     */
    public OrderStatisticsVO statistics() {
        //获取待派送数量，派送中数量，待接单数量
        Integer toBeConfirmed = orderMapper.getNumByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.getNumByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.getNumByStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        //根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        //判断订单状态是否为2（待接单）
        if(ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //判断支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == Orders.PAID) {
            //用户已支付，需要退款，本次为模拟付款无需退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//            log.info("申请退款：{}", refund);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    public void delivery(Long id) {
        Orders orders = orderMapper.getById(id);

        if(orders == null || !orders.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders ordersDB = new Orders();
        ordersDB.setId(id);
        ordersDB.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(ordersDB);
    }

    /**
     * 完成订单
     * @param id
     */
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);

        if(orders == null || !orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders ordersDB = new Orders();
        ordersDB.setId(id);
        ordersDB.setStatus(Orders.COMPLETED);
        ordersDB.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(ordersDB);
    }

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     */
    public void Cancel(OrdersCancelDTO ordersCancelDTO) {
        //在已支付之后的状态，取消订单后都需要退款，目前暂不实现
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);

        orderMapper.update(orders);
    }

    /**
     * 将Orders转化为OrderVO
     * @param page
     * @return
     */
    private List<OrderVO> getOrderVOList(Page<Orders> page){
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if(!CollectionUtils.isEmpty(ordersList)){
            for (Orders orders : ordersList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishStr(orders);

                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 获取菜品字符串信息
     * @param orders
     * @return
     */
    private String getOrderDishStr(Orders orders){
        //查询订单中菜品详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        //将每条菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x ->{
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        return String.join("", orderDishList);
    }

//    /**
//     * 订单支付
//     *
//     * @param ordersPaymentDTO
//     * @return
//     */
//    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
//        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getById(userId);
//
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//
//        return vo;
//    }
//
//    /**
//     * 支付成功，修改订单状态
//     *
//     * @param outTradeNo
//     */
//    public void paySuccess(String outTradeNo) {
//
//        // 根据订单号查询订单
//        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
//
//        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
//        Orders orders = Orders.builder()
//                .id(ordersDB.getId())
//                .status(Orders.TO_BE_CONFIRMED)
//                .payStatus(Orders.PAID)
//                .checkoutTime(LocalDateTime.now())
//                .build();
//
//        orderMapper.update(orders);
//    }
}
