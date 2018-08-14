package com.awbeci.ssb.app.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

@Configuration
@EnableAuthorizationServer
public class AwbeciAuthorizationServerConfig {
//        extends AuthorizationServerConfigurerAdapter {
//
//    @Override
//    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        clients.inMemory() // 使用in-memory存储
//                .withClient("awbeci") // client_id
//                .secret("awbeci-secret") // client_secret
//                .authorizedGrantTypes("authorization_code") // 该client允许的授权类型
//                .scopes("all"); // 允许的授权范围
//    }
}
