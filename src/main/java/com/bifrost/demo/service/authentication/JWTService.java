package com.bifrost.demo.service.authentication;

import com.bifrost.demo.dto.model.BifrostUser;
import com.bifrost.demo.dto.model.JSONEntry;
import com.bifrost.demo.dto.response.ServiceResponse;
import com.bifrost.demo.registry.ParameterRegistry;
import com.bifrost.demo.service.monitoring.CloudWatchService;
import com.bifrost.demo.service.monitoring.LogService;
import com.bifrost.demo.service.util.EncodingUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JWTService implements TokenService {
    @Value("${crypto.secret-token-key}")
    private String SECRET_KEY;
    private Key signingKey;
    @Value("${parameter.user-token-rules}")
    private String userTokenRules;
    private static final long EXPIRATION_MS = 5 * 60 * 1000;
    private final ParameterRegistry parameterRegistry;
    private final LogService log;

    public JWTService(
            ParameterRegistry parameterRegistry,
            CloudWatchService cloudWatchService
    ) {
        this.parameterRegistry = parameterRegistry;
        this.log = cloudWatchService;
    }

    public ServiceResponse<Void> precheck() {
        JSONEntry param = parameterRegistry.getJson(userTokenRules);

        if (param == null) {
            return null;
        }

        if (!param.get("allowUserTokenAuthentication").asBoolean()) {
            log.info("allowUserTokenAuthentication is false | rejecting user token authentication");
            return ServiceResponse.failure(ServiceResponse.ServiceError.SERVER_LIMIT, "Currently user token authentication is not allowed.");
        }

        return ServiceResponse.success(null);
    }

    @PostConstruct
    void init() {
        this.signingKey = Keys.hmacShaKeyFor(EncodingUtil.b64DecodeUrlSafe(SECRET_KEY));
    }

    @Override
    public ServiceResponse<BifrostUser> createToken(String username, BifrostUser.Role role) {
        ServiceResponse<Void> precheck = precheck();

        if (!precheck.isSuccess()) {
            return ServiceResponse.failure(precheck.getError(), precheck.getMessage());
        }

        try {
            String token = Jwts.builder()
                    .setSubject(username)
                    .claim("role", role.name())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                    .signWith(signingKey)
                    .compact();

            BifrostUser user = new BifrostUser(username, role, token);
            return ServiceResponse.success(user);
        } catch (Exception e) {
            log.error(
                    String.format("JWTService::createToken: %s", e.getMessage())
            );

            return ServiceResponse
                    .failure(ServiceResponse.ServiceError.GENERAL_ERROR, "Failed to create token.");
        }
    }

    @Override
    public ServiceResponse<BifrostUser> validateToken(String token) {
        ServiceResponse<Void> precheck = precheck();

        if (!precheck.isSuccess()) {
            return ServiceResponse.failure(precheck.getError(), precheck.getMessage());
        }

        try {
            Claims claims = Jwts
                    .parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token).getBody();

            String username = claims.getSubject();
            BifrostUser.Role role = BifrostUser.Role.valueOf(claims.get("role").toString());

            BifrostUser user = new BifrostUser(username, role, token);

            return ServiceResponse.success(user);
        } catch (JwtException e) {
            return ServiceResponse
                    .failure(ServiceResponse.ServiceError.BAD_INPUT, "Invalid or expired token");
        }
    }
}
