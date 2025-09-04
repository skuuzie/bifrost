package com.bifrost.demo.service.media;

import com.bifrost.demo.dto.model.ResumeRoastEntry;
import com.bifrost.demo.dto.response.ResumeRoastResponse;
import com.bifrost.demo.dto.response.ServiceResponse;

import java.io.InputStream;

public interface ResumeAgentService {
    ServiceResponse<ResumeRoastEntry> roastIt(InputStream document);

    ServiceResponse<ResumeRoastResponse> getRoast(String id);
}
