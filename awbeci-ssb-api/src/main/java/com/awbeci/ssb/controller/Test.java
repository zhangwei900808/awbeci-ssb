package com.awbeci.ssb.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@RestController
public class Test {

    @Value("${ssb.security.jwt.signingKey}")
    private String signingkey;

    @GetMapping("/test")
    public String test() {
        return "test";
    }

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
}
