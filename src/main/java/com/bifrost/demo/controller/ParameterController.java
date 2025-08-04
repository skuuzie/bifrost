package com.bifrost.demo.controller;

import com.bifrost.demo.annotation.DailyLimit;
import com.bifrost.demo.dto.model.DataEntry;
import com.bifrost.demo.dto.request.NewParameterRequest;
import com.bifrost.demo.dto.request.ParameterRequest;
import com.bifrost.demo.dto.response.BaseResponse;
import com.bifrost.demo.dto.response.ServiceResponse;
import com.bifrost.demo.service.parameter.DefaultParameterService;
import com.bifrost.demo.service.parameter.ParameterService;
import com.bifrost.demo.service.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class ParameterController {
    private final ParameterService parameterService;

    public ParameterController(DefaultParameterService parameterService) {
        this.parameterService = parameterService;
    }

    @DailyLimit(id = "getById", max = 10)
    @GetMapping("/parameter/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable String id) {
        ServiceResponse<DataEntry> res = parameterService.getEntryById(id);

        if (res.isSuccess()) {
            return ResponseEntity
                    .ok(BaseResponse.success("Parameter retrieval successful.", res.getData()));
        } else {
            return ResponseUtil.processError(res);
        }
    }

    @DailyLimit(id = "createNewParameter", max = 10)
    @PostMapping("/parameter")
    public ResponseEntity<BaseResponse> createNewParameter(@RequestBody NewParameterRequest req) {
        DataEntry newEntry = new DataEntry(null, req.description(), req.key(), req.value(), null, null);
        ServiceResponse<DataEntry> res = parameterService.createNewEntry(newEntry);

        if (res.isSuccess()) {
            return ResponseEntity
                    .ok(BaseResponse.success("Parameter creation successful.", res.getData()));
        } else {
            return ResponseUtil.processError(res);
        }
    }

    @DailyLimit(id = "updateParameter", max = 10)
    @PutMapping("/parameter")
    public ResponseEntity<BaseResponse> updateParameter(
            @RequestBody ParameterRequest req
    ) {
        DataEntry newEntry = new DataEntry(req.id(), req.description(), req.key(), req.value(), null, null);
        ServiceResponse<Boolean> res = parameterService.updateEntry(newEntry);

        if (res.isSuccess()) {
            return ResponseEntity
                    .ok(BaseResponse.success("Parameter update successful.", res.getData()));
        } else {
            return ResponseUtil.processError(res);
        }
    }

    @DailyLimit(id = "deleteById", max = 10)
    @DeleteMapping("/parameter/{id}")
    public ResponseEntity<BaseResponse> deleteById(@PathVariable String id) {
        ServiceResponse<Boolean> res = parameterService.deleteEntryById(id);

        if (res.isSuccess()) {
            return ResponseEntity
                    .ok(BaseResponse.success("Parameter deletion successful.", res.getData()));
        } else {
            return ResponseUtil.processError(res);
        }
    }

    @DailyLimit(id = "getEntries", max = 10)
    @GetMapping("/parameter")
    public ResponseEntity<BaseResponse> getEntries(
            @RequestHeader(value = "X-Limit", required = true) int limit
    ) {
        ServiceResponse<List<DataEntry>> res = parameterService.getEntries(limit);

        if (res.isSuccess()) {
            return ResponseEntity
                    .ok(BaseResponse.success("Parameter retrieval successful.", res.getData()));
        } else {
            return ResponseUtil.processError(res);
        }
    }
}
