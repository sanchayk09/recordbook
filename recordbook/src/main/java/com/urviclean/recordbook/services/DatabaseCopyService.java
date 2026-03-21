package com.urviclean.recordbook.services;

import com.urviclean.recordbook.models.DatabaseCopyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class DatabaseCopyService {

    private static final Pattern SAFE_DB_NAME = Pattern.compile("^[A-Za-z0-9_]+$");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public DatabaseCopyResponse copyDatabase(String sourceDbName, String backupDbName) {
        String sourceDb = normalizeAndValidate(sourceDbName, "sourceDbName");
        String backupDb = normalizeAndValidate(backupDbName, "backupDbName");

        if (sourceDb.equalsIgnoreCase(backupDb)) {
            throw new IllegalArgumentException("Source and backup database names must be different");
        }

        if (!databaseExists(sourceDb)) {
            throw new IllegalArgumentException("Source database does not exist: " + sourceDb);
        }

        createDatabaseIfMissing(backupDb);

        List<String> tables = getBaseTables(sourceDb);
        if (tables.isEmpty()) {
            throw new IllegalStateException("No tables found in source database: " + sourceDb);
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        try {
            List<String> copiedTables = new ArrayList<>();
            long totalRowsCopied = 0L;

            for (String table : tables) {
                recreateTable(sourceDb, backupDb, table);
                totalRowsCopied += copyTableData(sourceDb, backupDb, table);
                copiedTables.add(table);
            }

            DatabaseCopyResponse response = new DatabaseCopyResponse();
            response.setSourceDbName(sourceDb);
            response.setBackupDbName(backupDb);
            response.setTablesCopied(copiedTables.size());
            response.setRowsCopied(totalRowsCopied);
            response.setCopiedTables(copiedTables);
            response.setMessage("Database copy completed successfully");
            return response;
        } finally {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    private String normalizeAndValidate(String dbName, String fieldName) {
        String normalized = Objects.requireNonNull(dbName, fieldName + " is required").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (!SAFE_DB_NAME.matcher(normalized).matches()) {
            throw new IllegalArgumentException(fieldName + " contains invalid characters. Only letters, numbers, and underscore are allowed");
        }
        return normalized;
    }

    private boolean databaseExists(String dbName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?",
                Integer.class,
                dbName
        );
        return count != null && count > 0;
    }

    private void createDatabaseIfMissing(String dbName) {
        jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS `" + dbName + "`");
    }

    private List<String> getBaseTables(String dbName) {
        return jdbcTemplate.query(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = ? AND table_type = 'BASE TABLE' ORDER BY table_name",
                (rs, rowNum) -> rs.getString("table_name"),
                dbName
        );
    }

    private void recreateTable(String sourceDbName, String backupDbName, String tableName) {
        String backupTable = qualifiedName(backupDbName, tableName);
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + backupTable);
        jdbcTemplate.execute("CREATE TABLE " + backupTable + " LIKE " + qualifiedName(sourceDbName, tableName));
    }

    private long copyTableData(String sourceDbName, String backupDbName, String tableName) {
        String sourceTable = qualifiedName(sourceDbName, tableName);
        String backupTable = qualifiedName(backupDbName, tableName);

        jdbcTemplate.execute("INSERT INTO " + backupTable + " SELECT * FROM " + sourceTable);

        Long rowCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + backupTable,
                Long.class
        );
        return rowCount == null ? 0L : rowCount;
    }

    private String qualifiedName(String dbName, String tableName) {
        return "`" + dbName + "`.`" + tableName + "`";
    }
}

