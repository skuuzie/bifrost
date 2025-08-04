package com.bifrost.demo.repository.parameter;

import com.bifrost.demo.dto.model.DataEntry;

import java.sql.SQLException;
import java.util.List;

public interface ParameterRepository {
    DataEntry getEntryById(String id) throws SQLException;

    DataEntry createNewEntry(DataEntry entry) throws SQLException;

    boolean updateEntry(DataEntry entry) throws SQLException;

    boolean deleteEntryById(String id) throws SQLException;

    List<DataEntry> getEntries(int limit) throws SQLException;
}
