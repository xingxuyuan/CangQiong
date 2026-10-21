package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    @Select("select * from orders where \"number\" = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    void update(Orders orders);

    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrdertimeLT(Integer status, LocalDateTime orderTime);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select sum(amount) from orders where order_time >= #{begin} and order_time <= #{end} and status = #{status}")
    Double sumByMap(Map map);

    @Select("<script>select count(*) from orders where order_time &gt;= #{begin} and order_time &lt;= #{end} " +
            "<if test='status != null'> and status = #{status} </if></script>")
    Integer countByMap(Map map);

    List<GoodsSalesDTO> getSalesTop10(Map map);
}