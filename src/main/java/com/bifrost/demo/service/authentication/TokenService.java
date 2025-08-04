package com.bifrost.demo.service.authentication;

import com.bifrost.demo.dto.model.BifrostUser;
import com.bifrost.demo.dto.response.ServiceResponse;

public interface TokenService {
    ServiceResponse<BifrostUser> createToken(String username, BifrostUser.Role role);

    ServiceResponse<BifrostUser> validateToken(String token);
}
