package com.urviclean.recordbook.services;

import com.urviclean.recordbook.exception.ResourceNotFoundException;
import com.urviclean.recordbook.models.Route;
import com.urviclean.recordbook.models.RouteVillage;
import com.urviclean.recordbook.repositories.RouteRepository;
import com.urviclean.recordbook.repositories.RouteVillageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing sales routes and their villages.
 */
@Service
@Transactional
public class RouteService {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RouteVillageRepository routeVillageRepository;

    // ---- Routes ----

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", id));
    }

    public Route saveRoute(Route route) {
        return routeRepository.save(route);
    }

    public void deleteRoute(Long id) {
        if (!routeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Route", "id", id);
        }
        routeRepository.deleteById(id);
    }

    // ---- Route Villages ----

    public List<RouteVillage> getAllRouteVillages() {
        return routeVillageRepository.findAll();
    }

    public RouteVillage getRouteVillageById(Long id) {
        return routeVillageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RouteVillage", "id", id));
    }

    public RouteVillage saveRouteVillage(RouteVillage village) {
        return routeVillageRepository.save(village);
    }

    public void deleteRouteVillage(Long id) {
        if (!routeVillageRepository.existsById(id)) {
            throw new ResourceNotFoundException("RouteVillage", "id", id);
        }
        routeVillageRepository.deleteById(id);
    }
}
