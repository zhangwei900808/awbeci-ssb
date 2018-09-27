package com.awbeci.ssb.core.social.view;

import com.awbeci.ssb.core.social.utils.SocialRedisHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.GenericTypeResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.social.connect.*;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.connect.web.*;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UrlPathHelper;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping({"/socialConnect"})
public class SocialConnect implements InitializingBean {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final ConnectionFactoryLocator connectionFactoryLocator;
    private final ConnectionRepository connectionRepository;
    @Autowired
    private UsersConnectionRepository jdbcConnectionRepository;

    private final MultiValueMap<Class<?>, ConnectInterceptor<?>> connectInterceptors = new LinkedMultiValueMap();
    private final MultiValueMap<Class<?>, DisconnectInterceptor<?>> disconnectInterceptors = new LinkedMultiValueMap();
    private ConnectSupport connectSupport;
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();
    private String viewPath = "connect/";
    private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
    private String applicationUrl = null;
    private String callbackUrl;

    @Value("${ssb.security.social.connect-url}")
    private String connectUrl;

    @Autowired
    private ProviderSignInUtils providerSignInUtils;

    @Autowired
    private SocialRedisHelper socialRedisHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Inject
    public SocialConnect(ConnectionFactoryLocator connectionFactoryLocator, ConnectionRepository connectionRepository) {
        this.connectionFactoryLocator = connectionFactoryLocator;
        this.connectionRepository = connectionRepository;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setInterceptors(List<ConnectInterceptor<?>> interceptors) {
        this.setConnectInterceptors(interceptors);
    }

    public void setConnectInterceptors(List<ConnectInterceptor<?>> interceptors) {
        Iterator i$ = interceptors.iterator();

        while (i$.hasNext()) {
            ConnectInterceptor<?> interceptor = (ConnectInterceptor) i$.next();
            this.addInterceptor(interceptor);
        }

    }

    public void addInterceptor(ConnectInterceptor<?> interceptor) {
        Class<?> serviceApiType = GenericTypeResolver.resolveTypeArgument(interceptor.getClass(), ConnectInterceptor.class);
        this.connectInterceptors.add(serviceApiType, interceptor);
    }

    @RequestMapping(
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> connectionStatus(NativeWebRequest request, Model model) throws JsonProcessingException {
        this.setNoCache(request);
        this.processFlash(request, model);
        Map<String, List<Connection<?>>> connections = this.connectionRepository.findAllConnections();
        model.addAttribute("providerIds", this.connectionFactoryLocator.registeredProviderIds());
        model.addAttribute("connectionMap", connections);
        Map<String,Boolean> result = new HashMap<String, Boolean>();
        for (String key : connections.keySet()){
            result.put(key, org.apache.commons.collections.CollectionUtils.isNotEmpty(connections.get(key)));
        }
        return ResponseEntity.ok(objectMapper.writeValueAsString(result));
    }

    /**
     * 重写connect，返回json给前台，而不是跳转页面
     *
     * @param providerId
     * @param request
     * @return
     */
    @RequestMapping(
            value = {"/{providerId}"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> connect(@PathVariable String providerId,
                                     NativeWebRequest request) {
        HttpServletRequest nativeRequest = request.getNativeRequest(HttpServletRequest.class);
        Principal user = nativeRequest.getUserPrincipal();
        ConnectionFactory<?> connectionFactory = this.connectionFactoryLocator.getConnectionFactory(providerId);
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap();
        this.preConnect(connectionFactory, parameters, request);
        try {
            String social_connect_url = this.connectSupport.buildOAuthUrl(connectionFactory, request, parameters);
            String state = (String) this.sessionStrategy.getAttribute(request, "oauth2State");
            this.sessionStrategy.removeAttribute(request, "oauth2State");
            socialRedisHelper.saveStateUserId(state, user.getName());
            return ResponseEntity.ok(social_connect_url);
        } catch (Exception var6) {
            this.sessionStrategy.setAttribute(request, "social_provider_error", var6);
            logger.info(var6.getMessage());
            return null;
        }
    }

    protected String callbackUrl(NativeWebRequest request) {
        if (this.callbackUrl != null) {
            return this.callbackUrl;
        } else {
            HttpServletRequest nativeRequest = request.getNativeRequest(HttpServletRequest.class);
            return this.applicationUrl != null ? this.applicationUrl + this.connectPath(nativeRequest) : nativeRequest.getRequestURL().toString();
        }
    }

    private String connectPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return request.getServletPath() + (pathInfo != null ? pathInfo : "");
    }

    @RequestMapping(
            value = {"/{providerId}"},
            method = {RequestMethod.GET},
            params = {"code"}
    )
    public void oauth2Callback(@PathVariable String providerId,
                               NativeWebRequest request,
                               HttpServletResponse response) {
        try {
            //ConnectController是先保存在session里面，然后回调从session里面取出来校验
            //我现在是通过redis保存state 的 userId，这样就相当于校验了state
            String state = request.getParameter("state");
            String code = request.getParameter("code");
            OAuth2ConnectionFactory<?> connectionFactory = (OAuth2ConnectionFactory) this.connectionFactoryLocator.getConnectionFactory(providerId);
            AccessGrant accessGrant = connectionFactory.getOAuthOperations().exchangeForAccess(code, this.callbackUrl(request), null);
            Connection<?> connection = connectionFactory.createConnection(accessGrant);
            String userId = socialRedisHelper.getStateUserId(state);
            jdbcConnectionRepository.createConnectionRepository(userId).addConnection(connection);
            response.sendRedirect(connectUrl);
        } catch (Exception ex) {
            logger.info(ex.getMessage());
        }
    }


    @RequestMapping(
            value = {"/{providerId}"},
            method = {RequestMethod.DELETE}
    )
    public ResponseEntity<?> removeConnections(@PathVariable String providerId, NativeWebRequest request) {
        ConnectionFactory<?> connectionFactory = this.connectionFactoryLocator.getConnectionFactory(providerId);
        this.preDisconnect(connectionFactory, request);
        this.connectionRepository.removeConnections(providerId);
        this.postDisconnect(connectionFactory, request);
        return ResponseEntity.ok("success");
    }

    @RequestMapping(
            value = {"/{providerId}/{providerUserId}"},
            method = {RequestMethod.DELETE}
    )
    public ResponseEntity<?> removeConnection(@PathVariable String providerId,
                                              @PathVariable String providerUserId,
                                              NativeWebRequest request) throws IOException {
        try {
            ConnectionFactory<?> connectionFactory = this.connectionFactoryLocator.getConnectionFactory(providerId);
            this.preDisconnect(connectionFactory, request);
            this.connectionRepository.removeConnection(new ConnectionKey(providerId, providerUserId));
            this.postDisconnect(connectionFactory, request);
        } catch (Exception ex) {
            logger.info(ex.getMessage());
        }
        return ResponseEntity.ok("success");
    }

    protected String connectView() {
        return this.getViewPath() + "status";
    }


    public void afterPropertiesSet() throws Exception {
        this.connectSupport = new ConnectSupport(this.sessionStrategy);
        if (this.applicationUrl != null) {
            this.connectSupport.setApplicationUrl(this.applicationUrl);
        }

    }

    private boolean prependServletPath(HttpServletRequest request) {
        return !this.urlPathHelper.getPathWithinServletMapping(request).equals("");
    }

    private String getViewPath() {
        return this.viewPath;
    }

    private void addConnection(Connection<?> connection, ConnectionFactory<?> connectionFactory, WebRequest request) {
        try {
            this.connectionRepository.addConnection(connection);
            this.postConnect(connectionFactory, connection, request);
        } catch (DuplicateConnectionException var5) {
            this.sessionStrategy.setAttribute(request, "social_addConnection_duplicate", var5);
        }

    }

    private void preConnect(ConnectionFactory<?> connectionFactory, MultiValueMap<String, String> parameters, WebRequest request) {
        Iterator i$ = this.interceptingConnectionsTo(connectionFactory).iterator();

        while (i$.hasNext()) {
            ConnectInterceptor interceptor = (ConnectInterceptor) i$.next();
            interceptor.preConnect(connectionFactory, parameters, request);
        }

    }

    private void postConnect(ConnectionFactory<?> connectionFactory, Connection<?> connection, WebRequest request) {
        Iterator i$ = this.interceptingConnectionsTo(connectionFactory).iterator();

        while (i$.hasNext()) {
            ConnectInterceptor interceptor = (ConnectInterceptor) i$.next();
            interceptor.postConnect(connection, request);
        }

    }

    private void preDisconnect(ConnectionFactory<?> connectionFactory, WebRequest request) {
        Iterator i$ = this.interceptingDisconnectionsTo(connectionFactory).iterator();

        while (i$.hasNext()) {
            DisconnectInterceptor interceptor = (DisconnectInterceptor) i$.next();
            interceptor.preDisconnect(connectionFactory, request);
        }

    }

    private void postDisconnect(ConnectionFactory<?> connectionFactory, WebRequest request) {
        Iterator i$ = this.interceptingDisconnectionsTo(connectionFactory).iterator();

        while (i$.hasNext()) {
            DisconnectInterceptor interceptor = (DisconnectInterceptor) i$.next();
            interceptor.postDisconnect(connectionFactory, request);
        }

    }

    private List<ConnectInterceptor<?>> interceptingConnectionsTo(ConnectionFactory<?> connectionFactory) {
        Class<?> serviceType = GenericTypeResolver.resolveTypeArgument(connectionFactory.getClass(), ConnectionFactory.class);
        List<ConnectInterceptor<?>> typedInterceptors = (List) this.connectInterceptors.get(serviceType);
        if (typedInterceptors == null) {
            typedInterceptors = Collections.emptyList();
        }

        return typedInterceptors;
    }

    private List<DisconnectInterceptor<?>> interceptingDisconnectionsTo(ConnectionFactory<?> connectionFactory) {
        Class<?> serviceType = GenericTypeResolver.resolveTypeArgument(connectionFactory.getClass(), ConnectionFactory.class);
        List<DisconnectInterceptor<?>> typedInterceptors = (List) this.disconnectInterceptors.get(serviceType);
        if (typedInterceptors == null) {
            typedInterceptors = Collections.emptyList();
        }

        return typedInterceptors;
    }

    private void processFlash(WebRequest request, Model model) {
        this.convertSessionAttributeToModelAttribute("social_addConnection_duplicate", request, model);
        this.convertSessionAttributeToModelAttribute("social_provider_error", request, model);
        model.addAttribute("social_authorization_error", this.sessionStrategy.getAttribute(request, "social_authorization_error"));
        this.sessionStrategy.removeAttribute(request, "social_authorization_error");
    }

    private void convertSessionAttributeToModelAttribute(String attributeName, WebRequest request, Model model) {
        if (this.sessionStrategy.getAttribute(request, attributeName) != null) {
            model.addAttribute(attributeName, Boolean.TRUE);
            this.sessionStrategy.removeAttribute(request, attributeName);
        }

    }

    private void setNoCache(NativeWebRequest request) {
        HttpServletResponse response = (HttpServletResponse) request.getNativeResponse(HttpServletResponse.class);
        if (response != null) {
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 1L);
            response.setHeader("Cache-Control", "no-cache");
            response.addHeader("Cache-Control", "no-store");
        }

    }
}
