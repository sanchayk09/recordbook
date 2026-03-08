package com.urviclean.recordbook.controllers;

import com.urviclean.recordbook.models.Route;
import com.urviclean.recordbook.models.RouteVillage;
import com.urviclean.recordbook.services.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Routes & Villages", description = "APIs for managing sales routes and villages")
public class RouteController {

    @Autowired
    private RouteService routeService;

    // Route endpoints
    @GetMapping
    @Operation(summary = "List all routes", description = "Get all sales routes")
    public List<Route> listRoutes() {
        return routeService.getAllRoutes();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get route by ID", description = "Get a specific route")
    public ResponseEntity<Route> getRoute(
            @Parameter(description = "Route ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @PostMapping("/")
    public Route createRoute(@RequestBody Route route) {
        return routeService.saveRoute(route);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable Long id, @RequestBody Route newDetails) {
        newDetails.setRouteId(id);
        return ResponseEntity.ok(routeService.saveRoute(newDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    // Route Villages endpoints
    @GetMapping("/villages")
    public List<RouteVillage> listVillages() {
        return routeService.getAllRouteVillages();
    }

    @GetMapping("/villages/{id}")
    public ResponseEntity<RouteVillage> getVillage(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteVillageById(id));
    }

    @PostMapping("/villages")
    public RouteVillage createVillage(@RequestBody RouteVillage village) {
        return routeService.saveRouteVillage(village);
    }

    @PutMapping("/villages/{id}")
    public ResponseEntity<RouteVillage> updateVillage(@PathVariable Long id, @RequestBody RouteVillage newDetails) {
        newDetails.setVillageId(id);
        return ResponseEntity.ok(routeService.saveRouteVillage(newDetails));
    }

    @DeleteMapping("/villages/{id}")
    public ResponseEntity<Void> deleteVillage(@PathVariable Long id) {
        routeService.deleteRouteVillage(id);
        return ResponseEntity.noContent().build();
    }
}

