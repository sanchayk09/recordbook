package com.urviclean.recordbook.services;

import com.urviclean.recordbook.models.DatabaseCopyRequest;
import com.urviclean.recordbook.models.DatabaseCopyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MaintenanceService {

    @Autowired
    private DatabaseCopyService databaseCopyService;

    public DatabaseCopyResponse copyDatabase(DatabaseCopyRequest request) {
        return databaseCopyService.copyDatabase(request.getSourceDbName(), request.getBackupDbName());
    }
}

