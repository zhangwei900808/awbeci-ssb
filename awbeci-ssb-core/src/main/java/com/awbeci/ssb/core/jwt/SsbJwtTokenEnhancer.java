package com.awbeci.ssb.core.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;

/**
 * 拓展jwt 认证信息
 */
public class SsbJwtTokenEnhancer implements TokenEnhancer {

    /**
     * 注入外源信息到jwt token store里面
     */
    @Autowired
    private JwtTokenEnhancerHandler jwtTokenEnhancerHandler;

    public OAuth2AccessToken enhance(OAuth2AccessToken oAuth2AccessToken,
                                     OAuth2Authentication oAuth2Authentication) {
        Map<String, Object> info = jwtTokenEnhancerHandler.getInfoToToken();
        //todo:判断info是否为空
        ((DefaultOAuth2AccessToken) oAuth2AccessToken).setAdditionalInformation(info);
        return oAuth2AccessToken;
    }
}
