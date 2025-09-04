package com.bifrost.demo.service.ai;

import com.bifrost.demo.dto.response.ServiceResponse;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogleGeminiService implements LLMService {
    @Value("${network.api.key.google-gemini}")
    private String APIKey;

    private Client gClient;

    @PostConstruct
    void init() {
        this.gClient = Client.builder().apiKey(APIKey).build();
    }

    @Override
    public ServiceResponse<String> inference(String prompt) {
        GenerateContentResponse response =
                gClient.models.generateContent(
                        "gemini-2.5-flash",
                        prompt,
                        null);

        return ServiceResponse.success(response.text());
    }
}
