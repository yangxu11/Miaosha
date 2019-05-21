package com.miaosha.config;

import com.miaosha.domain.MiaoshaUser;

/**
 * 线程内存储user对象
 *
 * @author yx
 * @create 2019-04-07  16:42
 **/
public class UserContext {

    public static ThreadLocal<MiaoshaUser> userContext = new ThreadLocal<MiaoshaUser>();

    public static void setUser(MiaoshaUser user){
        userContext.set(user);
    }

    public static MiaoshaUser getUser(){
        return userContext.get();
    }
}
