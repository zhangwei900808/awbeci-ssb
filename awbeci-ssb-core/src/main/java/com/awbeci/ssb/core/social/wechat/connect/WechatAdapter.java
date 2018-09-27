package com.awbeci.ssb.core.social.wechat.connect;

import com.awbeci.ssb.core.social.wechat.api.Wechat;
import com.awbeci.ssb.core.social.wechat.api.WechatUserInfo;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;

public class WechatAdapter implements ApiAdapter<Wechat> {

    private String openId;

    public WechatAdapter() {}

    public WechatAdapter(String openId){
        this.openId = openId;
    }

    /**
     * @param api
     * @return
     */
    public boolean test(Wechat api) {
        return true;
    }

    /**
     * @param api
     * @param values
     */
    public void setConnectionValues(Wechat api, ConnectionValues values) {
        WechatUserInfo profile = api.getUserInfo(openId);
        values.setProviderUserId(profile.getOpenid());
        values.setDisplayName(profile.getNickname());
        values.setImageUrl(profile.getHeadimgurl());
    }

    /**
     * @param api
     * @return
     */
    public UserProfile fetchUserProfile(Wechat api) {
        return null;
    }

    /**
     * @param api
     * @param message
     */
    public void updateStatus(Wechat api, String message) {
        //do nothing
    }

}
