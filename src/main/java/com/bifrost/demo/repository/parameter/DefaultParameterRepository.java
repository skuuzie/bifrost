package com.bifrost.demo.repository.parameter;

import com.bifrost.demo.dto.model.DataEntry;
import com.bifrost.demo.service.util.CoreDB;
import com.bifrost.demo.service.util.DateUtil;
import com.bifrost.demo.service.util.JSONUtil;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class DefaultParameterRepository implements ParameterRepository {
    private final CoreDB _db;

    public DefaultParameterRepository(CoreDB db) {
        this._db = db;
    }

    @Override
    public DataEntry getEntryById(String id) throws SQLException {
        String selectSql = String.format("SELECT * FROM %s WHERE id = ?", _db.PARAMETERS_TABLE_NAME);

        PreparedStatement stmt = this._db.getConnection().prepareStatement(selectSql);
        stmt.setString(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new DataEntry(
                    rs.getString("id"),
                    rs.getString("description"),
                    rs.getString("key"),
                    JSONUtil.parseIfJson(rs.getString("value")),
                    DateUtil.toFormattedDate(rs.getTimestamp("created_at").toLocalDateTime()),
                    DateUtil.toFormattedDate(rs.getTimestamp("updated_at").toLocalDateTime()));
        }

        return null;
    }

    @Override
    public DataEntry createNewEntry(DataEntry entry) throws SQLException {
        String query = String.format("""
                    INSERT INTO %s (id, key, value, description, created_at, updated_at)
                    VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
                """, _db.PARAMETERS_TABLE_NAME);

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
        String query = String.format("""
                    UPDATE %s
                    SET key = ?, value = ?, description = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?;
                """, _db.PARAMETERS_TABLE_NAME);

        PreparedStatement stmt = this._db.getConnection().prepareStatement(query);
        stmt.setString(1, entry.key());
        stmt.setString(2, JSONUtil.toJsonString(entry.value()));
        stmt.setString(3, entry.description());
        stmt.setString(4, entry.id());

        return stmt.executeUpdate() > 0;
    }

    @Override
    public boolean deleteEntryById(String id) throws SQLException {
        String sql = "DELETE FROM parameters WHERE id = ?";

        PreparedStatement stmt = this._db.getConnection().prepareStatement(sql);
        stmt.setString(1, id);

        return stmt.executeUpdate() > 0;
    }

    @Override
    public List<DataEntry> getEntries(int limit) throws SQLException {
        String selectSql = String.format("SELECT * FROM %s ORDER BY created_at DESC LIMIT ?", _db.PARAMETERS_TABLE_NAME);
        List<DataEntry> entries = new ArrayList<>();

        PreparedStatement stmt = this._db.getConnection().prepareStatement(selectSql);
        stmt.setInt(1, limit);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            entries.add(new DataEntry(
                    rs.getString("id"),
                    rs.getString("description"),
                    rs.getString("key"),
                    JSONUtil.parseIfJson(rs.getString("value")),
                    DateUtil.toFormattedDate(rs.getTimestamp("created_at").toLocalDateTime()),
                    DateUtil.toFormattedDate(rs.getTimestamp("updated_at").toLocalDateTime())
            ));
        }

        return entries;
    }
}
