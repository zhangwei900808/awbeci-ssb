package com.awbeci.ssb.core.authentication.mobile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

//用户认证所在类
public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

    // 注意这里的userdetailservice ，因为SmsCodeAuthenticationProvider类没有@Component
    // 所以这里不能加@Autowire，只能通过外面设置才行
    private UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 在这里认证用户信息
     * @param authentication
     * @return
     * @throws AuthenticationException
     */
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsCodeAuthenticationToken authenticationToken = (SmsCodeAuthenticationToken) authentication;
//        String mobile = (String) authenticationToken.getPrincipal();
        String mobile = authentication.getName();
        String smsCode = (String) authenticationToken.getCredentials();

        UserDetails user = userDetailsService.loadUserByUsername(mobile);
//        String password = (String) authentication.getCredentials();
        if (user == null) {
            throw new InternalAuthenticationServiceException("无法获取用户信息");
        }
        //todo:在这里验证短信验证码,用redis缓存的验证码在这里进行验证
        if (!passwordEncoder().matches(smsCode, user.getPassword())) {
            throw new RuntimeException("密码不正确");
        }

        SmsCodeAuthenticationToken authenticationResult = new SmsCodeAuthenticationToken(user,null, user.getAuthorities());
        authenticationResult.setDetails(authenticationToken.getDetails());
        return authenticationResult;
    }

    public boolean supports(Class<?> authentication) {
        return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}
