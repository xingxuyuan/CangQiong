package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user_tab where openid = #{openid}")
    User getByOpenid(String openid);

    void insert(User user);

    @Select("<script>select count(*) from user_tab where create_time &gt;= #{begin} and create_time &lt;= #{end}</script>")
    Integer countByMap(Map map);
}