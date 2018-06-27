package com.heiku.panicbuy.dao;


import com.heiku.panicbuy.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserDao {


    @Select("select * from user where id = #{id}")
    User getById(@Param("id") long id);


    @Update("update user set password = #{password} where id = #{id}")
    void updateUser(User user);
}
