package com.heiku.panicbuy.dao;


import com.heiku.panicbuy.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDao {


    @Select("select * from user where id = #{id}")
    User getById(@Param("id") long id);
}
