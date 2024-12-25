package com.blize.conf;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.io.IOException;
import java.util.Arrays;

@Component
public class HeaderInterceptor implements HandlerInterceptor {

    @Value("${ANGULAR_HASH}")
    private String ANGULAR_HASH;

    @Value("${SOCKET_CONNECTION_URL}")
    private String SOCKET_CONNECTION_URL;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             @NotNull Object handler) throws IOException, ServletException {

        var isParentRequest = request.getAttribute("subRequest") == null;
        var isWebservice = request.getHeader("Webservice") != null;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var isAuthenticated = authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String);

        if (!isParentRequest) {
            return true;
        }

        if (request.getRequestURI().startsWith("/error")) {
            return true;
        }

        if (isWebservice) {
            response.addHeader("angular-hash", this.ANGULAR_HASH);

        }else if (Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("socket-connection-url")).findAny().isEmpty()) {
            Cookie cookie = new Cookie("socket-connection-url", this.SOCKET_CONNECTION_URL);
            cookie.setHttpOnly(false);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(60*60*24);
            response.addCookie(cookie);
        }

        if (!isAuthenticated) {
            return true;
        }

        if (!isWebservice) {
            var redirectToDashboard = false;
            if (handler instanceof HandlerMethod) {
                Class<?> controllerClass = ((HandlerMethod) handler).getBeanType();
                if (controllerClass.isAnnotationPresent(RestController.class)) {
                    redirectToDashboard = true;
                }
            } else if (handler instanceof ResourceHttpRequestHandler) {
                redirectToDashboard = true;
            }
            if (redirectToDashboard) {
                request.getRequestDispatcher("/dashboard").forward(request, response);
                return false;
            }
        }

        request.setAttribute("subRequest", "ok");
        return true;
    }


}
