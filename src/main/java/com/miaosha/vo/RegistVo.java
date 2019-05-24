package com.miaosha.vo;

import com.miaosha.validator.IsMobile;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * 注册请求表单封装
 *
 * @author yx
 * @create 2019-05-24  14:21
 **/
public class RegistVo {
    @NotNull
    @IsMobile
    private String mobile;

    @NotNull
    @Length(min=2)
    private String nickname;

    @NotNull
    @Length(min=32)
    private String password;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
