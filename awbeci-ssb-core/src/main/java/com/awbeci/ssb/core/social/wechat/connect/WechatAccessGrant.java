package com.awbeci.ssb.core.social.wechat.connect;

import org.springframework.social.oauth2.AccessGrant;

public class WechatAccessGrant extends AccessGrant {

    private String openId;

    public WechatAccessGrant() {
        super("");
    }

    public WechatAccessGrant(String accessToken, String scope, String refreshToken, Long expiresIn) {
        super(accessToken, scope, refreshToken, expiresIn);
    }

    /**
     * @return the openId
     */
    public String getOpenId() {
        return openId;
    }

    /**
     * @param openId the openId to set
     */
    public void setOpenId(String openId) {
        this.openId = openId;
    }

}
