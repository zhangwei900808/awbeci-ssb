package com.awbeci.ssb.core.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 交给前端使用的认证成功处理器
 */
@Component("jsAuthenticationSuccessHandler")
public class JsAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${ssb.security.social.callback-url}")
    private String callbackUrl;

    private String openIdParameter = "openId";

    private String providerIdParameter = "providerId";

    @Autowired
    private ObjectMapper objectMapper;

    //    @Autowired
//    TokenProperties tokenProperties;
//
//    @Autowired
//    AuthenticationManager authenticationManager;
//
//    @Autowired
//    UserDetailsService userDetailsService;
//
//    @Autowired
//    JwtTokenUtil jwtTokenUtil;

    /**
     * 通过验证生成token
     *
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
//        logger.info("------------正在生成token--------- {}", JSON.toJSON(authentication));
        try {
//            JwtSocialUserDetails user = (JwtSocialUserDetails) authentication.getPrincipal();
//            logger.info("Authentication User = {}", JSON.toJSON(user));
//            JwtUser jwtUser = new JwtUser(
//                    authentication.getName(),
//                    user.getPhone(),
//                    user.getEmail());
            // 生成 token
//            String token = jwtTokenUtil.generateToken(jwtUser);
            // 社交登录成功跳转到成功页面
            response.sendRedirect(callbackUrl + "?token=" + "test");
        } catch (Exception ex) {
            logger.info(ex.getMessage());
//            throw new DomainException(ResultCode.USER_AUTH_FAILD);
        }
    }

    /**
     * 获取openId
     */
    protected String obtainOpenId(HttpServletRequest request) {
        return request.getParameter(openIdParameter);
    }

    /**
     * 获取提供商id
     */
    protected String obtainProviderId(HttpServletRequest request) {
        return request.getParameter(providerIdParameter);
    }
}
