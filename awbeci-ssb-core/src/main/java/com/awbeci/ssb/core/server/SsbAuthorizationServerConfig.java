package com.awbeci.ssb.core.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableAuthorizationServer
public class SsbAuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

//    @Autowired
//    AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TokenStore redisTokenStore;

    @Autowired(required = false)
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    @Autowired(required = false)
    private TokenEnhancer jwtTokenEnhancer;

    @Autowired
    public SsbAuthorizationServerConfig(
//            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            TokenStore redisTokenStore) {
//        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.redisTokenStore = redisTokenStore;
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

        //使用Redis作为Token的存储
        endpoints.tokenStore(redisTokenStore)
//                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService);

        //1、设置token为jwt形式
        //2、设置jwt 拓展认证信息
        if (jwtAccessTokenConverter != null && jwtTokenEnhancer != null) {
            TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
            List<TokenEnhancer> enhancers = new ArrayList<TokenEnhancer>();
            enhancers.add(jwtTokenEnhancer);
            enhancers.add(jwtAccessTokenConverter);

            enhancerChain.setTokenEnhancers(enhancers);
            endpoints.tokenEnhancer(enhancerChain)
                    .accessTokenConverter(jwtAccessTokenConverter);
        }
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()//配置内存中，也可以是数据库
                .withClient("awbeci")//clientid
                .secret("awbeci-secret")
                .accessTokenValiditySeconds(3600)//token有效时间  秒
                .authorizedGrantTypes("refresh_token", "password", "authorization_code")//token模式
                .scopes("all")//限制允许的权限配置

                .and()//下面配置第二个应用   （不知道动态的是怎么配置的，那就不能使用内存模式，应该使用数据库模式来吧）
                .withClient("test")
                .scopes("testSc")
                .accessTokenValiditySeconds(7200)
                .scopes("all");
    }
}
