package com.bifrost.demo.service.encryption;

import com.bifrost.demo.dto.model.JSONEntry;
import com.bifrost.demo.dto.response.ServiceResponse;
import com.bifrost.demo.service.monitoring.CloudWatchService;
import com.bifrost.demo.service.monitoring.LogService;
import com.bifrost.demo.service.parameter.ParameterRegistry;
import com.bifrost.demo.util.CryptoUtil;
import com.bifrost.demo.util.EncodingUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class DefaultEncryptionService implements EncryptionService {
    @Value("${crypto.common-key}")
    private String commonKey;

    @Value("${parameter.encryption-rules}")
    private String encryptionRulesKey;
    private int maxSizeInBytes = 0;
    private String token;
    private final ParameterRegistry parameterRegistry;
    private final LogService log;

    public DefaultEncryptionService(
            ParameterRegistry parameterRegistry,
            CloudWatchService cloudWatchService
    ) {
        this.parameterRegistry = parameterRegistry;
        this.log = cloudWatchService;
    }

    @PostConstruct
    public void init() {
        refreshParameter();
    }

    public void refreshParameter() {
        JSONEntry param = parameterRegistry.getJson(encryptionRulesKey);

        if (param == null) {
            return;
        }

        maxSizeInBytes = param.get("maxDataSizeInBytes").asInt(0);
    }

    public ServiceResponse<Void> allowRequest(byte[] data) {
        refreshParameter();

        if (data.length > maxSizeInBytes) {
            return ServiceResponse.failure(ServiceResponse.ServiceError.SERVER_LIMIT, "Data size exceeds limit.");
        }

        return ServiceResponse.success(null);
    }

    @Override
    public ServiceResponse<String> encrypt(String data) {
        byte[] _data = data.getBytes(StandardCharsets.UTF_8);
        ServiceResponse<Void> precheck = allowRequest(_data);

        if (!precheck.isSuccess()) {
            return ServiceResponse.failure(precheck.getError(), precheck.getMessage());
        }

        try {
            byte[] enc = CryptoUtil.AESGCMEncrypt(this.commonKey, token, _data);
            return ServiceResponse.success(EncodingUtil.b64EncodeUrlSafe(enc));
        } catch (Exception e) {
            log.error(e);
            return ServiceResponse.failure(ServiceResponse.ServiceError.SERVICE_ERROR, e.getMessage());
        }
    }

    @Override
    public ServiceResponse<String> decrypt(String data) {
        byte[] _data = EncodingUtil.b64DecodeUrlSafe(data);
        ServiceResponse<Void> precheck = allowRequest(new byte[CryptoUtil.getGCMActualSize(data.getBytes(StandardCharsets.UTF_8))]);

        if (!precheck.isSuccess()) {
            return ServiceResponse.failure(precheck.getError(), precheck.getMessage());
        }

        try {
            byte[] dec = CryptoUtil.AESGCMDecrypt(this.commonKey, token, _data);
            return ServiceResponse.success(new String(dec, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error(e);
            return ServiceResponse.failure(ServiceResponse.ServiceError.SERVICE_ERROR, e.getMessage());
        }
    }

    @Override
    public void tokenize(String token) {
        this.token = token;
    }
}
