package com.bifrost.demo.repository.docu;

import com.bifrost.demo.dto.model.DocuPDF;
import com.bifrost.demo.dto.model.ResumeRoastEntry;

import java.sql.SQLException;

public interface ResumeRoasteryRepository {
    ResumeRoastEntry addOrUpdateJob(DocuPDF document, ResumeRoastEntry.Status status, String result) throws SQLException;

    ResumeRoastEntry getEntry(String identifier) throws SQLException;
}
