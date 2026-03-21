package com.urviclean.recordbook.models;

import jakarta.validation.constraints.NotBlank;

public class DatabaseCopyRequest {

    @NotBlank(message = "sourceDbName is required")
    private String sourceDbName;

    @NotBlank(message = "backupDbName is required")
    private String backupDbName;

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
}

