package com.urviclean.recordbook.controllers;

import com.urviclean.recordbook.models.DatabaseCopyRequest;
import com.urviclean.recordbook.models.DatabaseCopyResponse;
import com.urviclean.recordbook.services.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(
        origins = "http://localhost:3000",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
@Tag(name = "Maintenance Operations", description = "Maintenance APIs for backup and database operations")
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    @PostMapping("/database/copy")
    @Operation(summary = "Copy one database into another", description = "Creates or refreshes a backup database by cloning all tables and data from the source database")
    public ResponseEntity<DatabaseCopyResponse> copyDatabase(@Valid @RequestBody DatabaseCopyRequest request) {
        return ResponseEntity.ok(maintenanceService.copyDatabase(request));
    }
}

