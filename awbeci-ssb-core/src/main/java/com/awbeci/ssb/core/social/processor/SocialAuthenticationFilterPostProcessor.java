package com.awbeci.ssb.core.social.processor;

import org.springframework.social.security.SocialAuthenticationFilter;

public interface SocialAuthenticationFilterPostProcessor {
    void process(SocialAuthenticationFilter socialAuthenticationFilter);
}
