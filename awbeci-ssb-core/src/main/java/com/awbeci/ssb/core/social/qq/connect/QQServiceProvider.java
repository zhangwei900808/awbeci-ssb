package com.awbeci.ssb.core.social.qq.connect;

import com.awbeci.ssb.core.social.qq.api.QQ;
import com.awbeci.ssb.core.social.qq.api.impl.QQImpl;
import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;

public class QQServiceProvider extends AbstractOAuth2ServiceProvider<QQ> {
    //获取code
    private static final String QQ_URL_AUTHORIZE = "https://graph.qq.com/oauth2.0/authorize";

    //获取access_token 也就是令牌
    private static final String QQ_URL_ACCESS_TOKEN = "https://graph.qq.com/oauth2.0/token";

    private String appId;

    public QQServiceProvider(String appId, String appSecret) {
        super(new QQOAuth2Template(appId, appSecret, QQ_URL_AUTHORIZE, QQ_URL_ACCESS_TOKEN));
        this.appId = appId;
    }

    public QQ getApi(String accessToken) {
        return new QQImpl(accessToken, appId);
    }
}
