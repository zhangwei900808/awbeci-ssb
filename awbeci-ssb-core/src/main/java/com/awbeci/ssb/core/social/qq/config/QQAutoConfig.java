package com.awbeci.ssb.core.social.qq.config;

import com.awbeci.ssb.core.social.qq.connect.QQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.connect.ConnectionFactory;

@Configuration
public class QQAutoConfig extends SocialAutoConfigurerAdapter{

    @Value("${ssb.security.social.qq.app-id}")
    private String qqAppId;

    @Value("${ssb.security.social.qq.app-secret}")
    private String qqAppSecret;

    @Value("${ssb.security.social.qq.provider-id}")
    private String qqProviderId;

    @Override
    protected ConnectionFactory<?> createConnectionFactory() {
        return new QQConnectionFactory(qqProviderId, qqAppId, qqAppSecret);
    }
}
