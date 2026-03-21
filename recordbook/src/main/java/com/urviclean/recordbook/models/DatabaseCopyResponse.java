package com.urviclean.recordbook.models;

import java.util.ArrayList;
import java.util.List;

public class DatabaseCopyResponse {

    private String sourceDbName;
    private String backupDbName;
    private int tablesCopied;
    private long rowsCopied;
    private List<String> copiedTables = new ArrayList<>();
    private String message;

    public String getSourceDbName() {
        return sourceDbName;
    }

    public void setSourceDbName(String sourceDbName) {
        this.sourceDbName = sourceDbName;
    }

    public String getBackupDbName() {
        return backupDbName;
    }

    public void setBackupDbName(String backupDbName) {
        this.backupDbName = backupDbName;
    }

    public int getTablesCopied() {
        return tablesCopied;
    }

    public void setTablesCopied(int tablesCopied) {
        this.tablesCopied = tablesCopied;
    }

    public long getRowsCopied() {
        return rowsCopied;
    }

    public void setRowsCopied(long rowsCopied) {
        this.rowsCopied = rowsCopied;
    }

    public List<String> getCopiedTables() {
        return copiedTables;
    }

    public void setCopiedTables(List<String> copiedTables) {
        this.copiedTables = copiedTables;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

