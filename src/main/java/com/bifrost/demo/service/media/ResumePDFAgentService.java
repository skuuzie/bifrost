package com.bifrost.demo.service.media;

import com.bifrost.demo.dto.model.DocuPDF;
import com.bifrost.demo.dto.model.ResumeRoastCache;
import com.bifrost.demo.dto.model.ResumeRoastEntry;
import com.bifrost.demo.dto.response.ResumeRoastResponse;
import com.bifrost.demo.dto.response.ServiceResponse;
import com.bifrost.demo.repository.docu.DefaultResumeRoastery;
import com.bifrost.demo.repository.docu.ResumeRoasteryRepository;
import com.bifrost.demo.service.monitoring.CloudWatchService;
import com.bifrost.demo.service.monitoring.LogService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

@Service
public class ResumePDFAgentService implements ResumeAgentService {

    private final ResumeProcessor processor;
    private final LogService log;
    private final ResumeRoasteryRepository resumeRoasteryRepository;
    private final RedisTemplate<String, ResumeRoastCache> resumeCache;

    public ResumePDFAgentService(
            ResumeProcessor processor,
            CloudWatchService cloudWatchService,
            DefaultResumeRoastery defaultResumeRoastery,
            RedisTemplate<String, ResumeRoastCache> template
    ) {
        this.processor = processor;
        this.log = cloudWatchService;
        this.resumeRoasteryRepository = defaultResumeRoastery;
        this.resumeCache = template;
    }

    public ResumeRoastEntry getNewOrCache(String identifier) throws SQLException {
        ResumeRoastCache cache = resumeCache.opsForValue().get(identifier);

        if (cache != null) {
            log.debug("[CACHE HIT] %s".formatted(identifier));

            return new ResumeRoastEntry(
                    identifier,
                    ResumeRoastEntry.Status.valueOf(cache.status()),
                    cache.result(),
                    cache.createdAt(),
                    cache.updatedAt()
            );
        }

        log.debug("[CACHE MISS] %s".formatted(identifier));
        log.debug("[DB Live Call] %s".formatted(identifier));

        return resumeRoasteryRepository.getEntry(identifier);
    }

    @Override
    public ServiceResponse<ResumeRoastEntry> roastIt(InputStream document) {
        try {
            DocuPDF docuPDF = DocuPDF.load(document);
            ResumeRoastEntry entry = getNewOrCache(docuPDF.getIdentifier());

            if (entry == null) {
                entry = resumeRoasteryRepository.addOrUpdateJob(docuPDF, ResumeRoastEntry.Status.NEW, null);
            } else {
                switch (entry.status()) {
                    case NEW, PROCESSING -> {
                        return ServiceResponse.failure(
                                ServiceResponse.ServiceError.BAD_INPUT,
                                "Document is already in queue for processing.");
                    }
                    case FAILED, DONE -> {
                        entry = resumeRoasteryRepository.addOrUpdateJob(docuPDF, ResumeRoastEntry.Status.NEW, null);
                    }
                }
            }

            processor.processRoasting(docuPDF);

            return ServiceResponse.success(entry);

        } catch (IllegalArgumentException ex) {
            return ServiceResponse.failure(ServiceResponse.ServiceError.BAD_INPUT, ex.getMessage());
        } catch (IOException ex) {
            return ServiceResponse.failure(ServiceResponse.ServiceError.SERVICE_ERROR, "Unknown PDF parsing error.");
        } catch (Exception ex) {
            return ServiceResponse.failure(ServiceResponse.ServiceError.SERVICE_ERROR, ex.toString());
        }
    }

    @Override
    public ServiceResponse<ResumeRoastResponse> getRoast(String id) {
        try {
            ResumeRoastEntry entry = getNewOrCache(id);

            if (entry == null) {
                return ServiceResponse.failure(
                        ServiceResponse.ServiceError.BAD_INPUT,
                        "Document ID not found.");
            }

            ResumeRoastResponse res = new ResumeRoastResponse(entry.identifier(), entry.status().name(), entry.rawResult(), entry.updatedAt());

            return ServiceResponse.success(res);
        } catch (Exception ex) {
            log.error(ex);

            return ServiceResponse.failure(ServiceResponse.ServiceError.SERVICE_ERROR, ex.toString());
        }
    }
}
