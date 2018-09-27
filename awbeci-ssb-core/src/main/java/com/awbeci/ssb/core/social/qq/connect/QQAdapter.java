package com.awbeci.ssb.core.social.qq.connect;

import com.awbeci.ssb.core.social.qq.api.QQ;
import com.awbeci.ssb.core.social.qq.api.QQUserInfo;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;

public class QQAdapter implements ApiAdapter<QQ> {
    public boolean test(QQ qq) {
        return true;
    }

    public void setConnectionValues(QQ qq, ConnectionValues connectionValues) {
        QQUserInfo userInfo = qq.getUserInfo();

        //openId 唯一标识
        connectionValues.setProviderUserId(userInfo.getOpenId());
        connectionValues.setDisplayName(userInfo.getNickname());
        connectionValues.setImageUrl(userInfo.getFigureurl_qq_1());
        connectionValues.setProfileUrl(null);
    }

    public UserProfile fetchUserProfile(QQ qq) {
        return null;
    }

    public void updateStatus(QQ qq, String s) {

    }
}
