package com.bifrost.demo.service.encryption;

import com.bifrost.demo.dto.response.ServiceResponse;

public interface EncryptionService {
    ServiceResponse<String> encrypt(String data);

    ServiceResponse<String> decrypt(String data);

    void tokenize(String token);
}
