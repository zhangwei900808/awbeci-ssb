package com.awbeci.ssb.core.social.wechat.connect;

import com.awbeci.ssb.core.social.wechat.api.Wechat;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.support.OAuth2Connection;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2ServiceProvider;

public class WechatConnectionFactory extends OAuth2ConnectionFactory<Wechat> {

    /**
     * @param appId
     * @param appSecret
     */
    public WechatConnectionFactory(String providerId, String appId, String appSecret) {
        super(providerId, new WechatServiceProvider(appId, appSecret), new WechatAdapter());
    }

    /**
     * 由于微信的openId是和accessToken一起返回的，所以在这里直接根据accessToken设置providerUserId即可，不用像QQ那样通过QQAdapter来获取
     */
    @Override
    protected String extractProviderUserId(AccessGrant accessGrant) {
        if(accessGrant instanceof WechatAccessGrant) {
            return ((WechatAccessGrant)accessGrant).getOpenId();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.social.connect.support.OAuth2ConnectionFactory#createConnection(org.springframework.social.oauth2.AccessGrant)
     */
    public Connection<Wechat> createConnection(AccessGrant accessGrant) {
        return new OAuth2Connection<Wechat>(getProviderId(), extractProviderUserId(accessGrant), accessGrant.getAccessToken(),
                accessGrant.getRefreshToken(), accessGrant.getExpireTime(), getOAuth2ServiceProvider(), getApiAdapter(extractProviderUserId(accessGrant)));
    }

    /* (non-Javadoc)
     * @see org.springframework.social.connect.support.OAuth2ConnectionFactory#createConnection(org.springframework.social.connect.ConnectionData)
     */
    public Connection<Wechat> createConnection(ConnectionData data) {
        return new OAuth2Connection<Wechat>(data, getOAuth2ServiceProvider(), getApiAdapter(data.getProviderUserId()));
    }

    private ApiAdapter<Wechat> getApiAdapter(String providerUserId) {
        return new WechatAdapter(providerUserId);
    }

    private OAuth2ServiceProvider<Wechat> getOAuth2ServiceProvider() {
        return (OAuth2ServiceProvider<Wechat>) getServiceProvider();
    }


}