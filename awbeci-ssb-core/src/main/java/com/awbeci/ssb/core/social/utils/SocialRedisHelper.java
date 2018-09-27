package com.awbeci.ssb.core.social.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.WebRequest;

import java.util.concurrent.TimeUnit;


/**
 * 将第三方用户信息保存到redis里面
 */
@Component
public class SocialRedisHelper {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private UsersConnectionRepository usersConnectionRepository;

    @Autowired
    private ConnectionFactoryLocator connectionFactoryLocator;

    public void saveConnectionData(String mkey, ConnectionData connectionData) {
        redisTemplate.opsForValue().set(getKey(mkey), connectionData, 10, TimeUnit.MINUTES);
    }

    public void saveStateUserId(String mkey, String userId) {
        redisTemplate.opsForValue().set(getKey(mkey), userId, 10, TimeUnit.MINUTES);
    }

    public String getStateUserId(String mkey) {
        String key = getKey(mkey);
        if (!redisTemplate.hasKey(key)) {
            throw new RuntimeException("无法找到缓存的第三方社交账号信息");
        }
        return (String) redisTemplate.opsForValue().get(key);
    }


    public void doPostSignUp(String mkey,String userId){
        String key = getKey(mkey);
        if (!redisTemplate.hasKey(key)){
            throw new RuntimeException("无法找到缓存的第三方社交账号信息");
        }
        ConnectionData connectionData = (ConnectionData) redisTemplate.opsForValue().get(key);
        Connection<?> connection = connectionFactoryLocator.getConnectionFactory(connectionData.getProviderId())
                .createConnection(connectionData);
        usersConnectionRepository.createConnectionRepository(userId).addConnection(connection);
        redisTemplate.delete(key);
    }

    private String getKey(String mkey) {
        if (StringUtils.isEmpty(mkey)) {
            throw new RuntimeException("设置ID：mkey 不为空");
        }
        return "awbeci:security:social.connect." + mkey;
    }
}
