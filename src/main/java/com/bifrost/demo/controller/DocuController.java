package com.bifrost.demo.controller;

import com.bifrost.demo.annotation.DailyLimit;
import com.bifrost.demo.dto.model.ResumeRoastEntry;
import com.bifrost.demo.dto.response.BaseResponse;
import com.bifrost.demo.dto.response.ResumeRoastResponse;
import com.bifrost.demo.dto.response.ServiceResponse;
import com.bifrost.demo.service.media.ResumeAgentService;
import com.bifrost.demo.service.media.ResumePDFAgentService;
import com.bifrost.demo.util.ResponseUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1")
public class DocuController {
    private final ResumeAgentService resumeAgent;

    public DocuController(ResumePDFAgentService resumePDF) {
        this.resumeAgent = resumePDF;
    }

    @DailyLimit(id = "roastIt", max = 5)
    @PostMapping(value = "/roast-it", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse> roastIt(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            ServiceResponse<ResumeRoastEntry> inf = resumeAgent.roastIt(file.getInputStream());

            if (inf.isSuccess()) {
                ResumeRoastEntry entry = inf.getData();

                return ResponseEntity
                        .ok(BaseResponse.success("Inference successful.", new ResumeRoastResponse(
                                entry.identifier(), entry.status().name(), null, entry.updatedAt())));
            } else {
                return ResponseUtil.processError(inf);
            }
        } catch (Exception ex) {
            return ResponseUtil.processError(ServiceResponse.failure(ServiceResponse.ServiceError.SERVICE_ERROR, "Unknown error."));
        }
    }

    @PostMapping(value = "/roast-it/{id}")
    public ResponseEntity<BaseResponse> roastItStatus(@PathVariable String id) {
        try {
            ServiceResponse<ResumeRoastResponse> res = resumeAgent.getRoast(id);

            if (res.isSuccess()) {
                return ResponseEntity
                        .ok(BaseResponse.success(
                                "Data retrieval successful.", res.getData()));
            } else {
                return ResponseUtil.processError(res);
            }
        } catch (Exception ex) {
            return ResponseUtil.processError(ServiceResponse.failure(ServiceResponse.ServiceError.SERVICE_ERROR, "Unknown error."));
        }
    }
}
