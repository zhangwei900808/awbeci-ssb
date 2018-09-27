package com.awbeci.ssb.core.social.wechat.config;

import com.awbeci.ssb.core.social.qq.config.SocialAutoConfigurerAdapter;
import com.awbeci.ssb.core.social.wechat.connect.WechatConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.social.connect.ConnectionFactory;

@Configuration
@ConditionalOnProperty(prefix = "ssb.security.social.wechat", name = "app-id")
@Order(2)
public class WechatAutoConfiguration extends SocialAutoConfigurerAdapter {

    @Value("${ssb.security.social.wechat.app-id}")
    private String appId;

    @Value("${ssb.security.social.wechat.app-secret}")
    private String appSecret;

    @Value("${ssb.security.social.wechat.provider-id}")
    private String providerId;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.boot.autoconfigure.social.SocialAutoConfigurerAdapter
     * #createConnectionFactory()
     */
    @Override
    protected ConnectionFactory<?> createConnectionFactory() {
        return new WechatConnectionFactory(providerId, appId, appSecret);
    }
}
