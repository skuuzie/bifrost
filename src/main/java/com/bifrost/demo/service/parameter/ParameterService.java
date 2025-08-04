package com.bifrost.demo.service.parameter;

import com.bifrost.demo.dto.model.DataEntry;
import com.bifrost.demo.dto.response.ServiceResponse;

import java.util.List;

public interface ParameterService {
    ServiceResponse<DataEntry> getEntryById(String id);

    ServiceResponse<DataEntry> createNewEntry(DataEntry entry);

    ServiceResponse<Boolean> updateEntry(DataEntry data);

    ServiceResponse<Boolean> deleteEntryById(String id);

    ServiceResponse<List<DataEntry>> getEntries(int limit);
}
