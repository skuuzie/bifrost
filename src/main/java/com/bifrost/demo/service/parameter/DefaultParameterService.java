package com.bifrost.demo.service.parameter;

import com.bifrost.demo.dto.model.DataEntry;
import com.bifrost.demo.dto.response.ServiceResponse;
import com.bifrost.demo.repository.parameter.DefaultParameterRepository;
import com.bifrost.demo.repository.parameter.ParameterRepository;
import com.bifrost.demo.service.monitoring.CloudWatchService;
import com.bifrost.demo.service.monitoring.LogService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class DefaultParameterService implements ParameterService {
    private final ParameterRepository parameterRepository;
    private final LogService log;

    public DefaultParameterService(DefaultParameterRepository parameterRepository, CloudWatchService cloudWatchService) {
        this.parameterRepository = parameterRepository;
        this.log = cloudWatchService;
    }

    @Override
    public ServiceResponse<DataEntry> getEntryById(String id) {
        try {
            DataEntry entry = parameterRepository.getEntryById(id);

            if (entry == null) {
                return ServiceResponse
                        .failure(ServiceResponse.ServiceError.BAD_INPUT, "Parameter not found.");
            }

            return ServiceResponse.success(entry);
        } catch (SQLException e) {
            e.printStackTrace(System.out);
            return ServiceResponse
                    .failure(ServiceResponse.ServiceError.GENERAL_ERROR, e.getMessage());
        }
    }

    @Override
    public ServiceResponse<List<DataEntry>> getEntries(int limit) {
        try {
            return ServiceResponse.success(parameterRepository.getEntries(limit));
        } catch (SQLException e) {
            e.printStackTrace(System.out);
            return ServiceResponse.failure(ServiceResponse.ServiceError.GENERAL_ERROR, e.getMessage());
        }
    }


    @Override
    public ServiceResponse<DataEntry> createNewEntry(DataEntry entry) {
        try {
            DataEntry newEntry = parameterRepository.createNewEntry(entry);

            if (newEntry == null) {
                return ServiceResponse
                        .failure(ServiceResponse.ServiceError.GENERAL_ERROR, "Unknown error.");
            }

            log.info(
                    String.format("New parameter: id: %s | key: %s | value: %s", entry.id(), entry.key(), entry.value())
            );

            return ServiceResponse
                    .success(newEntry);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return ServiceResponse
                    .failure(ServiceResponse.ServiceError.GENERAL_ERROR, e.getMessage());
        }
    }

    @Override
    public ServiceResponse<Boolean> updateEntry(DataEntry entry) {
        try {
            if (!parameterRepository.updateEntry(entry)) {
                return ServiceResponse
                        .failure(ServiceResponse.ServiceError.BAD_INPUT, "Invalid input.");
            }

            log.info(
                    String.format("Parameter %s updated | new value: %s", entry.id(), entry.value())
            );

            return ServiceResponse.success(null);
        } catch (SQLException e) {
            e.printStackTrace(System.out);
            return ServiceResponse
                    .failure(ServiceResponse.ServiceError.GENERAL_ERROR, e.getMessage());
        }
    }

    @Override
    public ServiceResponse<Boolean> deleteEntryById(String id) {
        try {
            if (!parameterRepository.deleteEntryById(id)) {
                return ServiceResponse
                        .failure(ServiceResponse.ServiceError.BAD_INPUT, "Invalid input.");
            }

            log.info(
                    String.format("Parameter %s deleted", id)
            );

            return ServiceResponse.success(null);
        } catch (SQLException e) {
            e.printStackTrace(System.out);
            return ServiceResponse
                    .failure(ServiceResponse.ServiceError.GENERAL_ERROR, e.getMessage());
        }
    }
}
