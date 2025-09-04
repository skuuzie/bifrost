package com.bifrost.demo.repository.parameter;

import com.bifrost.demo.constants.DBConstants;
import com.bifrost.demo.data.AuroraDB;
import com.bifrost.demo.dto.model.DataEntry;
import com.bifrost.demo.util.DateUtil;
import com.bifrost.demo.util.JSONUtil;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class DefaultParameterRepository implements ParameterRepository {
    private final AuroraDB _db;

    public DefaultParameterRepository(AuroraDB db) {
        this._db = db;
    }

    @Override
    public DataEntry getEntryById(String id) throws SQLException {
        String selectSql = "SELECT * FROM %s WHERE %s = ?".formatted(
                DBConstants.ParameterRegistryTable.NAME,
                DBConstants.ParameterRegistryTable.COL_ID
        );

        PreparedStatement stmt = this._db.getConnection().prepareStatement(selectSql);
        stmt.setString(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new DataEntry(
                    rs.getString(DBConstants.ParameterRegistryTable.COL_ID),
                    rs.getString(DBConstants.ParameterRegistryTable.COL_DESCRIPTION),
                    rs.getString(DBConstants.ParameterRegistryTable.COL_KEY),
                    JSONUtil.parseIfJson(rs.getString(DBConstants.ParameterRegistryTable.COL_VALUE)),
                    DateUtil.toFormattedDate(rs.getTimestamp(DBConstants.ParameterRegistryTable.COL_CREATED).toLocalDateTime()),
                    DateUtil.toFormattedDate(rs.getTimestamp(DBConstants.ParameterRegistryTable.COL_CREATED).toLocalDateTime()));
        }

        return null;
    }

    @Override
    public DataEntry createNewEntry(DataEntry entry) throws SQLException {
        String query = """
                    INSERT INTO %s (%s, %s, %s, %s, %s, %s)
                    VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
                """.formatted(
                        DBConstants.ParameterRegistryTable.NAME,
                        DBConstants.ParameterRegistryTable.COL_ID,
                        DBConstants.ParameterRegistryTable.COL_KEY,
                        DBConstants.ParameterRegistryTable.COL_VALUE,
                        DBConstants.ParameterRegistryTable.COL_DESCRIPTION,
                        DBConstants.ParameterRegistryTable.COL_CREATED,
                        DBConstants.ParameterRegistryTable.COL_UPDATED
        );

        PreparedStatement stmt = this._db.getConnection().prepareStatement(query);

        String newId = UUID.randomUUID().toString();
        stmt.setString(1, newId);
        stmt.setString(2, entry.key());
        stmt.setString(3, JSONUtil.toJsonString(entry.value()));
        stmt.setString(4, entry.description());

        if (stmt.executeUpdate() == 0) {
            return null;
        }

        return getEntryById(newId);
    }

    @Override
    public boolean updateEntry(DataEntry entry) throws SQLException {
        String query = """
                    UPDATE %s
                    SET %s = ?, %s = ?, %s = ?, %s = CURRENT_TIMESTAMP
                    WHERE %s = ?;
                """.formatted(
                DBConstants.ParameterRegistryTable.NAME,
                DBConstants.ParameterRegistryTable.COL_KEY,
                DBConstants.ParameterRegistryTable.COL_VALUE,
                DBConstants.ParameterRegistryTable.COL_DESCRIPTION,
                DBConstants.ParameterRegistryTable.COL_UPDATED,
                DBConstants.ParameterRegistryTable.COL_ID
        );

        PreparedStatement stmt = this._db.getConnection().prepareStatement(query);
        stmt.setString(1, entry.key());
        stmt.setString(2, JSONUtil.toJsonString(entry.value()));
        stmt.setString(3, entry.description());
        stmt.setString(4, entry.id());

        return stmt.executeUpdate() > 0;
    }

    @Override
    public boolean deleteEntryById(String id) throws SQLException {
        String sql = "DELETE FROM %s WHERE %s = ?".formatted(
                DBConstants.ParameterRegistryTable.NAME,
                DBConstants.ParameterRegistryTable.COL_ID
        );

        PreparedStatement stmt = this._db.getConnection().prepareStatement(sql);
        stmt.setString(1, id);

        return stmt.executeUpdate() > 0;
    }

    @Override
    public List<DataEntry> getEntries(int limit) throws SQLException {
        String selectSql = "SELECT * FROM %s ORDER BY %s DESC LIMIT ?".formatted(
                DBConstants.ParameterRegistryTable.NAME,
                DBConstants.ParameterRegistryTable.COL_CREATED
        );
        List<DataEntry> entries = new ArrayList<>();

        PreparedStatement stmt = this._db.getConnection().prepareStatement(selectSql);
        stmt.setInt(1, limit);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            entries.add(new DataEntry(
                    rs.getString(DBConstants.ParameterRegistryTable.COL_ID),
                    rs.getString(DBConstants.ParameterRegistryTable.COL_DESCRIPTION),
                    rs.getString(DBConstants.ParameterRegistryTable.COL_KEY),
                    JSONUtil.parseIfJson(rs.getString(DBConstants.ParameterRegistryTable.COL_VALUE)),
                    DateUtil.toFormattedDate(rs.getTimestamp(DBConstants.ParameterRegistryTable.COL_CREATED).toLocalDateTime()),
                    DateUtil.toFormattedDate(rs.getTimestamp(DBConstants.ParameterRegistryTable.COL_UPDATED).toLocalDateTime())
            ));
        }

        return entries;
    }
}
