package com.urviclean.recordbook.controllers;

import com.urviclean.recordbook.models.Route;
import com.urviclean.recordbook.models.RouteVillage;
import com.urviclean.recordbook.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "http://localhost:3000")
public class RouteController {

    @Autowired
    private AdminService adminService;

    // Route endpoints
    @GetMapping
    public List<Route> listRoutes() {
        return adminService.getAllRoutes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Route> getRoute(@PathVariable Long id) {
        Route route = adminService.getRouteById(id);
        if (route == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(route);
    }

    @PostMapping
    public Route createRoute(@RequestBody Route route) {
        return adminService.saveRoute(route);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable Long id, @RequestBody Route newDetails) {
        Route existing = adminService.getRouteById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setRouteId(id);
        return ResponseEntity.ok(adminService.saveRoute(newDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        adminService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    // Route Villages endpoints
    @GetMapping("/villages")
    public List<RouteVillage> listVillages() {
        return adminService.getAllRouteVillages();
    }

    @GetMapping("/villages/{id}")
    public ResponseEntity<RouteVillage> getVillage(@PathVariable Long id) {
        RouteVillage village = adminService.getRouteVillageById(id);
        if (village == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(village);
    }

    @PostMapping("/villages")
    public RouteVillage createVillage(@RequestBody RouteVillage village) {
        return adminService.saveRouteVillage(village);
    }

    @PutMapping("/villages/{id}")
    public ResponseEntity<RouteVillage> updateVillage(@PathVariable Long id, @RequestBody RouteVillage newDetails) {
        RouteVillage existing = adminService.getRouteVillageById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setVillageId(id);
        return ResponseEntity.ok(adminService.saveRouteVillage(newDetails));
    }

    @DeleteMapping("/villages/{id}")
    public ResponseEntity<Void> deleteVillage(@PathVariable Long id) {
        adminService.deleteRouteVillage(id);
        return ResponseEntity.noContent().build();
    }
}

