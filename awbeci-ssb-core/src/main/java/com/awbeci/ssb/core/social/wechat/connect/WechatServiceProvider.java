package com.awbeci.ssb.core.social.wechat.connect;

import com.awbeci.ssb.core.social.wechat.api.Wechat;
import com.awbeci.ssb.core.social.wechat.api.WechatImpl;
import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;

public class WechatServiceProvider extends AbstractOAuth2ServiceProvider<Wechat> {

    /**
     * 微信获取授权码的url
     */
    private static final String URL_AUTHORIZE = "https://open.weixin.qq.com/connect/qrconnect";
    /**
     * 微信获取accessToken的url
     */
    private static final String URL_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token";

    /**
     * @param appId
     * @param appSecret
     */
    public WechatServiceProvider(String appId, String appSecret) {
        super(new WechatOAuth2Template(appId, appSecret, URL_AUTHORIZE, URL_ACCESS_TOKEN));
    }


    /* (non-Javadoc)
     * @see org.springframework.social.oauth2.AbstractOAuth2ServiceProvider#getApi(java.lang.String)
     */
    @Override
    public Wechat getApi(String accessToken) {
        return new WechatImpl(accessToken);
    }
}
