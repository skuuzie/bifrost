package com.bifrost.demo.service.ai;

import com.bifrost.demo.dto.response.ServiceResponse;

public interface LLMService {
    ServiceResponse<String> inference(String prompt);
}
