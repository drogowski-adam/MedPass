package com.example.medpassbackend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MedPassRepo {

    private final JdbcTemplate jdbc;

    public MedPassRepo(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existsPersonnelCode(String code) {
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dbo.SecurityPersonnel WHERE personnel_code = ?",
                Integer.class,
                code
        );
        return cnt != null && cnt > 0;
    }

    public String findDiseaseByIdentifierHash(String identifierHash) {
        List<String> res = jdbc.query(
                "SELECT disease_name FROM dbo.Participants WHERE identifier_hash = ?",
                (rs, rowNum) -> rs.getString("disease_name"),
                identifierHash
        );
        return res.isEmpty() ? null : res.get(0);
    }

    public List<String> findAllowedItems(String diseaseName) {
        return jdbc.query(
                """
                SELECT DISTINCT I.item
                FROM dbo.DiseaseRules DR
                JOIN dbo.Items I ON I.id = DR.item_id
                WHERE DR.disease_name = ?
                ORDER BY I.item
                """,
                (rs, rowNum) -> rs.getString("item"),
                diseaseName
        );
    }
    public String findRoleByLogin(String username, String passwordHash) {
        return jdbc.query(
                "SELECT role FROM dbo.SecurityPersonnel WHERE username = ? AND password_hash = ?",
                (rs, rowNum) -> rs.getString("role"),
                username,
                passwordHash
        ).stream().findFirst().orElse(null);
    }

    public void logAccess(String guardHash, String action, String participantHash) {
        jdbc.update(
                "EXEC LogAccess ?, ?, ?",
                guardHash,
                action,
                participantHash
        );
    }

    public List<AccessLogDto> getLogsForGuard(String guardHash) {
        return jdbc.query(
                """
                SELECT log_date, action_type, participant_hash
                FROM AppLogs
                WHERE guard_hash = ?
                ORDER BY log_date DESC
                """,
                (rs, rowNum) -> new AccessLogDto(
                        rs.getTimestamp("log_date").toLocalDateTime(),
                        rs.getString("action_type"),
                        rs.getString("participant_hash")
                ),
                guardHash
        );
    }
}
