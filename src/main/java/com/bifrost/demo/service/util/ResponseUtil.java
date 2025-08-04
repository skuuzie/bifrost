package com.bifrost.demo.service.util;

import com.bifrost.demo.dto.response.BaseResponse;
import com.bifrost.demo.dto.response.ServiceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ResponseUtil {
    public static ResponseEntity<BaseResponse> processError(ServiceResponse<?> res) {
        return switch (res.getError()) {
            case GENERAL_ERROR -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.error(res.getMessage()));
            case SERVER_LIMIT -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(BaseResponse.error(res.getMessage()));
            case BAD_INPUT -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.error(res.getMessage()));
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Unknown error."));
        };
    }
}
