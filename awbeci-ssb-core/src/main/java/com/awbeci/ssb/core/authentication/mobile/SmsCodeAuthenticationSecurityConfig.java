package com.awbeci.ssb.core.authentication.mobile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

/**
 * * 自定义验证密码或者验证码
 * https://blog.csdn.net/tzdwsy/article/details/50738043
 * https://blog.csdn.net/xiejx618/article/details/42609497
 * https://blog.csdn.net/jiangshanwe/article/details/73842143
 * https://longfeizheng.github.io/2018/01/14/Spring-Security%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E4%BA%94-Spring-Security%E7%9F%AD%E4%BF%A1%E7%99%BB%E5%BD%95/
 * https://cloud.tencent.com/developer/article/1040105
 */
@Component
public class SmsCodeAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Autowired
    private AuthenticationSuccessHandler ssbAuthenticationSuccessHandler;

    @Autowired
    private AuthenticationFailureHandler ssbAuthenticationFailureHandler;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void configure(HttpSecurity http) throws Exception {

        SmsCodeAuthenticationFilter smsCodeAuthenticationFilter = new SmsCodeAuthenticationFilter();
        smsCodeAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        smsCodeAuthenticationFilter.setAuthenticationSuccessHandler(ssbAuthenticationSuccessHandler);
        smsCodeAuthenticationFilter.setAuthenticationFailureHandler(ssbAuthenticationFailureHandler);

        //todo:可以往这个类里面注入验证短信验证码的service
        SmsCodeAuthenticationProvider smsCodeDaoAuthenticationProvider = new SmsCodeAuthenticationProvider();
        smsCodeDaoAuthenticationProvider.setUserDetailsService(userDetailsService);
        http.authenticationProvider(smsCodeDaoAuthenticationProvider)
                .addFilterAfter(smsCodeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
