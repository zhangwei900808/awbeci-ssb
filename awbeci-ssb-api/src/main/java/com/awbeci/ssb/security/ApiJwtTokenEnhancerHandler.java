package com.awbeci.ssb.security;

import com.awbeci.ssb.core.jwt.JwtTokenEnhancerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 拓展jwt token里面的信息
 */
@Service
public class ApiJwtTokenEnhancerHandler implements JwtTokenEnhancerHandler {

    public HashMap<String, Object> getInfoToToken() {
        HashMap<String, Object> info = new HashMap<String, Object>();
        info.put("author", "张威");
        info.put("company", "awbeci-copy");
        return info;
    }
}
