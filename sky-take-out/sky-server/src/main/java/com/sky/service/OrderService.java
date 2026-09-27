package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    // 用户端
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
    void payment(OrdersPaymentDTO ordersPaymentDTO);
    void reminder(Long id);

    // 管理端
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);
    OrderStatisticsVO statistics();
    OrderVO details(Long id);
    void confirm(OrdersConfirmDTO ordersConfirmDTO);
    void rejection(OrdersRejectionDTO ordersRejectionDTO);
    void cancel(OrdersCancelDTO ordersCancelDTO);
    void delivery(Long id);
    void complete(Long id);
}