package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderDetailMapper orderDetailMapper;
    @Autowired private AddressBookMapper addressBookMapper;
    @Autowired private ShoppingCartMapper shoppingCartMapper;
    @Autowired private WebSocketServer webSocketServer;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) throw new RuntimeException("地址簿为空");

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.size() == 0) throw new RuntimeException("购物车为空");

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
        orderMapper.insert(orders);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail od = new OrderDetail();
            BeanUtils.copyProperties(cart, od);
            od.setOrderId(orders.getId());
            orderDetails.add(od);
        }
        orderDetailMapper.insertBatch(orderDetails);
        shoppingCartMapper.deleteByUserId(userId);

        return OrderSubmitVO.builder().id(orders.getId()).orderNumber(orders.getNumber()).orderAmount(orders.getAmount()).orderTime(orders.getOrderTime()).build();
    }

    @Override
    public void payment(OrdersPaymentDTO ordersPaymentDTO) {
        Orders ordersDB = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        if (ordersDB == null) throw new RuntimeException("订单不存在");

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.TO_BE_CONFIRMED);
        orders.setPayStatus(Orders.PAID);
        orders.setCheckoutTime(LocalDateTime.now());
        orderMapper.update(orders);

        // 来单提醒
        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + ordersPaymentDTO.getOrderNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) throw new RuntimeException("订单不存在");
        Map map = new HashMap();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号：" + orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        
        Map map1 = new HashMap();
        map1.put("status", Orders.TO_BE_CONFIRMED);
        orderStatisticsVO.setToBeConfirmed(orderMapper.countByMap(map1));

        Map map2 = new HashMap();
        map2.put("status", Orders.CONFIRMED);
        orderStatisticsVO.setConfirmed(orderMapper.countByMap(map2));

        Map map3 = new HashMap();
        map3.put("status", Orders.DELIVERY_IN_PROGRESS);
        orderStatisticsVO.setDeliveryInProgress(orderMapper.countByMap(map3));

        return orderStatisticsVO;
    }

    @Override
    public OrderVO details(Long id) {
        Orders orders = orderMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder().id(ordersConfirmDTO.getId()).status(Orders.CONFIRMED).build();
        orderMapper.update(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = Orders.builder().id(ordersRejectionDTO.getId()).status(Orders.CANCELLED).rejectionReason(ordersRejectionDTO.getRejectionReason()).build();
        orderMapper.update(orders);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = Orders.builder().id(ordersCancelDTO.getId()).status(Orders.CANCELLED).cancelReason(ordersCancelDTO.getCancelReason()).build();
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders orders = Orders.builder().id(id).status(Orders.DELIVERY_IN_PROGRESS).build();
        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        Orders orders = Orders.builder().id(id).status(Orders.COMPLETED).deliveryTime(LocalDateTime.now()).build();
        orderMapper.update(orders);
    }
}