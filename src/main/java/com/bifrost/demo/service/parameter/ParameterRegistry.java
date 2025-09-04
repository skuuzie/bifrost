package com.bifrost.demo.service.parameter;

import com.bifrost.demo.dto.model.DataEntry;
import com.bifrost.demo.dto.model.JSONEntry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ParameterRegistry {
    private final ParameterService parameterService;

    private final Map<String, DataEntry> parameterMap = new ConcurrentHashMap<>();

    public ParameterRegistry(DefaultParameterService parameterService) {
        this.parameterService = parameterService;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        List<DataEntry> all = parameterService.getEntries(Integer.MAX_VALUE).getData();
        parameterMap.clear();
        for (DataEntry entry : all) {
            parameterMap.put(entry.key(), entry);
        }
    }

    public DataEntry get(String key) {
        return parameterMap.get(key);
    }

    public JSONEntry getJson(String key) {
        DataEntry entry = get(key);
        return entry != null ? JSONEntry.parse(entry.value().toString()) : null;
    }
}
