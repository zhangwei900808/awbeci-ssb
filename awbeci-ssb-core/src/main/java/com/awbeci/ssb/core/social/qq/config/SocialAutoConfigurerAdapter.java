package com.awbeci.ssb.core.social.qq.config;

import org.springframework.core.env.Environment;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.ConnectionFactory;

/**
 * spring social 1.1.6已经移除了该类，所以自己新建一下
 */
public abstract class SocialAutoConfigurerAdapter extends SocialConfigurerAdapter {

    @Override
    public void addConnectionFactories(ConnectionFactoryConfigurer configurer, Environment environment) {
        configurer.addConnectionFactory(createConnectionFactory());
    }

    protected abstract ConnectionFactory<?> createConnectionFactory();

}