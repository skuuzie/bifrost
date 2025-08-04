package com.bifrost.demo.middleware;

import com.bifrost.demo.annotation.RequireRole;
import com.bifrost.demo.dto.model.BifrostUser;
import com.bifrost.demo.dto.response.BaseResponse;
import com.bifrost.demo.dto.response.ServiceResponse;
import com.bifrost.demo.service.authentication.JWTService;
import com.bifrost.demo.service.authentication.TokenService;
import com.bifrost.demo.service.util.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;

@Component
public class AdminEndpointInterceptor implements HandlerInterceptor {
    @Value("${admin.token}")
    private String adminToken;

    private final TokenService tokenService;

    public AdminEndpointInterceptor(JWTService jwtService) {
        this.tokenService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("X-Token");

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

        if (token == null || token.isBlank()) {
            return reject(response, "Missing token.");
        }

        if (token.equals(adminToken)) {
            return true;
        }

        try {
            ServiceResponse<BifrostUser> user = tokenService.validateToken(token);

            if (!user.isSuccess()) {
                return reject(response, user.getMessage());
            }

            if (requireRole == null) {
                return true;
            }

            if (requireRole.value()[0].equals(BifrostUser.Role.SUPERADMIN)) {
                return reject(response, "Nuh uh.");
            }

            if (!Arrays.asList(requireRole.value()).contains(user.getData().role())) {
                return reject(response, "Unauthorized action.");
            }

            return true;
        } catch (Exception e) {
            return reject(response, "Invalid token.");
        }
    }

    private boolean reject(HttpServletResponse response, String message) throws IOException, IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(JSONUtil.toJsonString(BaseResponse.error(message)));
        return false;
    }
}
