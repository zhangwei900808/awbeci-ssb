package com.awbeci.ssb.controller;

import com.awbeci.ssb.core.social.entity.SocialUserInfo;
import com.awbeci.ssb.core.social.utils.SocialRedisHelper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

@RestController
public class Test {

    @Value("${ssb.security.social.qq.register-url}")
    private String registerUrl;

    @Value("${ssb.security.social.bind-url}")
    private String bindUrl;

    @Value("${ssb.security.social.qq.provider-id}")
    private String providerId;

    @Value("${ssb.security.jwt.signingKey}")
    private String signingkey;


    @Autowired
    private ProviderSignInUtils providerSignInUtils;

    @Autowired
    private SocialRedisHelper socialRedisHelper;

    /**
     * 获取用户信息
     *
     * @param userDetails
     * @return
     */
    @GetMapping("/me")
    public Object me(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails;
    }

    @GetMapping("/me2")
    public Object me2(Authentication user, HttpServletRequest request) throws UnsupportedEncodingException {
        String token = StringUtils.substringAfter(request.getHeader("Authorization"), "bearer ");

        //解析外源信息到token中
        Claims claims = Jwts.parser()
                // 不加getBytes会报错
                .setSigningKey(signingkey.getBytes("UTF-8"))
                .parseClaimsJws(token)
                .getBody();
        String company = (String) claims.get("company");
        System.out.println(company);
        return user;
    }

    @GetMapping("/social/user")
    public SocialUserInfo socialUser(HttpServletRequest request) {
        SocialUserInfo userInfo = new SocialUserInfo();
        Connection<?> connectionFromSession = providerSignInUtils.getConnectionFromSession(new ServletWebRequest(request));
        userInfo.setHeadImg(connectionFromSession.getImageUrl());
        userInfo.setNickname(connectionFromSession.getDisplayName());
        userInfo.setProviderId(connectionFromSession.getKey().getProviderId());
        userInfo.setProviderUserId(connectionFromSession.getKey().getProviderUserId());
        return userInfo;
    }

    /**
     * 获取社交账号数据并保存到redis里面待前端绑定时使用
     * @param request
     * @param response
     * @throws IOException
     */
    @GetMapping("/social/signUp")
    public void socialSignUp(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uuid = UUID.randomUUID().toString();
        SocialUserInfo userInfo = new SocialUserInfo();
        Connection<?> connectionFromSession = providerSignInUtils.getConnectionFromSession(new ServletWebRequest(request));
        userInfo.setHeadImg(connectionFromSession.getImageUrl());
        userInfo.setNickname(connectionFromSession.getDisplayName());
        userInfo.setProviderId(connectionFromSession.getKey().getProviderId());
        userInfo.setProviderUserId(connectionFromSession.getKey().getProviderUserId());

        socialRedisHelper.saveConnectionData(uuid, connectionFromSession.createData());
        response.sendRedirect(bindUrl + "?mkey=" + uuid);
    }

    /**
     * 社交账号绑定
     * 这里的参数是举例，后面可以创建一个User类代替并使用@ResponseBody注解
     * @param username
     * @param password
     * @param phone
     * @param email
     * @param mkey
     * @return
     * @throws AuthenticationException
     */
    @PostMapping("/social/bind")
    public ResponseEntity<?> bind(String username, String password, String phone, String email, String mkey) throws AuthenticationException {
        String userId = UUID.randomUUID().toString();

        // 如果是社交注册，既进行社交保存数据操作
        if (!org.springframework.util.StringUtils.isEmpty(mkey)) {
            socialRedisHelper.doPostSignUp(mkey, userId);
        }
        //todo:保存自有平台系统用户数据库，并生成token返回给前端
        return null;
    }
}
