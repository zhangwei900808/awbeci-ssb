package com.awbeci.ssb.core.authentication.mobile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.concurrent.TimeUnit;

//用户认证所在类
public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

   private RedisTemplate<Object, Object> redisTemplate;

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
        redisTemplate.opsForValue().set(mobile, smsCode, 10, TimeUnit.MINUTES);
        String smsCodeFromRedis = (String) redisTemplate.opsForValue().get(mobile);
        if(!smsCode.equals(smsCodeFromRedis)){
            throw new InternalAuthenticationServiceException("手机验证码不正确");
        }

        UserDetails user = userDetailsService.loadUserByUsername(mobile);
        if (user == null) {
            throw new InternalAuthenticationServiceException("无法获取用户信息");
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

    public RedisTemplate<Object, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
