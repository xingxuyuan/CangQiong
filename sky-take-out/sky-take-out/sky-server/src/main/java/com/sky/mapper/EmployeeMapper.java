package com.sky.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工数据
     */
    void insert(Employee employee);

    /**
     * 分页查询员工
     */
    List<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    

    /**
     * 根据 ID 查询员工
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);

    /**
     * 动态更新员工信息
     */
    void update(Employee employee);
}