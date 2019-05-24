package com.miaosha.dao;

import com.miaosha.domain.MiaoshaUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface MiaoshaUserDao {
	
	@Select("select * from miaosha_user where id = #{id}")
	public MiaoshaUser getById(@Param("id")long id);

	@Update("update miaosha_user set password = #{password} where id = #{id}")
    public void update(MiaoshaUser toBeUpdate);

	@Insert("insert into miaosha_user (id,nickname,password,salt,register_date,merchant) values(#{id}, #{nickname}, #{password},#{salt},#{registerDate},#{merchant})")
    public void addUser(MiaoshaUser registUser);
}
