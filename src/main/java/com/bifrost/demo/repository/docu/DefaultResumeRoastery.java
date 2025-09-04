package com.bifrost.demo.repository.docu;

import com.bifrost.demo.constants.DBConstants;
import com.bifrost.demo.data.AuroraDB;
import com.bifrost.demo.dto.model.DocuPDF;
import com.bifrost.demo.dto.model.ResumeRoastEntry;
import com.bifrost.demo.util.DateUtil;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class DefaultResumeRoastery implements ResumeRoasteryRepository {
    private final AuroraDB _db;

    public DefaultResumeRoastery(AuroraDB db) {
        this._db = db;
    }

    @Override
    public ResumeRoastEntry addOrUpdateJob(DocuPDF document, ResumeRoastEntry.Status status, String result) throws SQLException {
        String query = """
                    INSERT INTO %s (%s, %s, %s, %s, %s)
                    VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    ON CONFLICT (%s) DO UPDATE
                    SET
                        %s = EXCLUDED.%s,
                        %s = EXCLUDED.%s,
                        %s = CURRENT_TIMESTAMP;
                """.formatted(
                DBConstants.ResumeRoasteryTable.NAME,
                DBConstants.ResumeRoasteryTable.COL_ID,
                DBConstants.ResumeRoasteryTable.COL_STATUS,
                DBConstants.ResumeRoasteryTable.COL_RESULT,
                DBConstants.ResumeRoasteryTable.COL_CREATED,
                DBConstants.ResumeRoasteryTable.COL_UPDATED,

                // ON CONFLICT
                DBConstants.ResumeRoasteryTable.COL_ID,

                // SET
                DBConstants.ResumeRoasteryTable.COL_STATUS, DBConstants.ResumeRoasteryTable.COL_STATUS,
                DBConstants.ResumeRoasteryTable.COL_RESULT, DBConstants.ResumeRoasteryTable.COL_RESULT,
                DBConstants.ResumeRoasteryTable.COL_UPDATED
        );

        try (PreparedStatement stmt = _db.getConnection().prepareStatement(query)) {
            stmt.setString(1, document.getIdentifier());
            stmt.setString(2, status.name());
            stmt.setString(3, result);

            if (stmt.executeUpdate() == 0) {
                return null;
            }

            return getEntry(document.getIdentifier());
        }
    }

    @Override
    public ResumeRoastEntry getEntry(String identifier) throws SQLException {
        String selectSql = String.format("SELECT * FROM %s WHERE id = ?", DBConstants.ResumeRoasteryTable.NAME);

        PreparedStatement stmt = this._db.getConnection().prepareStatement(selectSql);
        stmt.setString(1, identifier);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new ResumeRoastEntry(
                    rs.getString(DBConstants.ResumeRoasteryTable.COL_ID),
                    ResumeRoastEntry.Status.valueOf(rs.getString(DBConstants.ResumeRoasteryTable.COL_STATUS)),
                    rs.getString(DBConstants.ResumeRoasteryTable.COL_RESULT),
                    DateUtil.toFormattedDate(rs.getTimestamp(DBConstants.ResumeRoasteryTable.COL_CREATED).toLocalDateTime()),
                    DateUtil.toFormattedDate(rs.getTimestamp(DBConstants.ResumeRoasteryTable.COL_UPDATED).toLocalDateTime()));
        }

        return null;
    }
}
