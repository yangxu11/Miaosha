package com.miaosha.access;

import com.alibaba.fastjson.JSON;
import com.miaosha.config.UserContext;
import com.miaosha.domain.MiaoshaUser;
import com.miaosha.redis.AccessKey;
import com.miaosha.redis.MiaoshaKey;
import com.miaosha.redis.RedisService;
import com.miaosha.result.CodeMsg;
import com.miaosha.result.Result;
import com.miaosha.service.MiaoshaUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.lang.annotation.Annotation;

/**
 * 请求次数拦截器
 *
 * @author yx
 * @create 2019-04-07  16:03
 **/
@Component
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    RedisService redisService;

    @Autowired
    MiaoshaUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        MiaoshaUser user = getUser(request,response);
        UserContext.setUser(user);
        if(handler instanceof HandlerMethod){
            HandlerMethod hm = (HandlerMethod)handler;
            Annotation annotation = hm.getMethodAnnotation(Access.class);
            if(annotation==null){
                return true;
            } else{
                int maxCount = ((Access) annotation).maxCount();
                int seconds = ((Access) annotation).seconds();

                String key = request.getRequestURI();

                if(redisService.exists(AccessKey.withExpireTime(seconds),""+key+user.getId())){
                    int count = redisService.get(AccessKey.withExpireTime(seconds),""+key+user.getId(),Integer.class);
                    if(count<=maxCount){
                        redisService.set(AccessKey.withExpireTime(seconds),""+key+user.getId(),count+1);
                    } else{
                        render(response, CodeMsg.ACCESS_OVERTIME);
                        return false;
                    }
                } else{
                    redisService.set(AccessKey.withExpireTime(seconds),""+key+user.getId(),1);
                }


            }
        }
        return true;
    }

    private void render(HttpServletResponse response, CodeMsg accessOvertime) throws Exception{
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(accessOvertime));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response){

        String paramToken = request.getParameter(MiaoshaUserService.COOKI_NAME_TOKEN);
        String cookieToken = getCookieValue(request, MiaoshaUserService.COOKI_NAME_TOKEN);
        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        //取缓存

        return userService.getByToken(response, token);
    }

    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[]  cookies = request.getCookies();
        if(cookies == null || cookies.length <= 0){
            return null;
        }
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
