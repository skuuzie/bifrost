package com.bifrost.demo.controller;

import com.bifrost.demo.annotation.DailyLimit;
import com.bifrost.demo.dto.request.EncryptionRequest;
import com.bifrost.demo.dto.response.BaseResponse;
import com.bifrost.demo.dto.response.ServiceResponse;
import com.bifrost.demo.service.encryption.DefaultEncryptionService;
import com.bifrost.demo.service.encryption.EncryptionService;
import com.bifrost.demo.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class EncryptionController {
    private final EncryptionService encryptionService;

    public EncryptionController(DefaultEncryptionService defaultEncryptionService) {
        this.encryptionService = defaultEncryptionService;
    }

    @DailyLimit(id = "encryptData", max = 10)
    @PostMapping("/encrypt")
    public ResponseEntity<BaseResponse> encryptData(
            @RequestBody EncryptionRequest req,
            @RequestHeader(value = "X-Password", required = false) String password
    ) {
        if (password != null) {
            encryptionService.tokenize(password);
        }
        ServiceResponse<String> enc = encryptionService.encrypt(req.data());
        encryptionService.tokenize(null);

        if (enc.isSuccess()) {
            return ResponseEntity
                    .ok(BaseResponse.success("Encryption successful.", enc.getData()));
        } else {
            return ResponseUtil.processError(enc);
        }
    }

    @DailyLimit(id = "decryptData", max = 10)
    @PostMapping("/decrypt")
    public ResponseEntity<BaseResponse> decryptData(
            @RequestBody EncryptionRequest req,
            @RequestHeader(value = "X-Password", required = false) String password
    ) {
        if (password != null) {
            encryptionService.tokenize(password);
        }
        ServiceResponse<String> dec = encryptionService.decrypt(req.data());
        encryptionService.tokenize(null);

        if (dec.isSuccess()) {
            return ResponseEntity
                    .ok(BaseResponse.success("Decryption successful.", dec.getData()));
        } else {
            return ResponseUtil.processError(dec);
        }
    }
}
