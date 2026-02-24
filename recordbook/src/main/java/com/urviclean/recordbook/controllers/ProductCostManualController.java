package com.urviclean.recordbook.controllers;

import com.urviclean.recordbook.models.ProductCostManualRequest;
import com.urviclean.recordbook.models.ProductCostManualResponse;
import com.urviclean.recordbook.services.ProductCostManualService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-cost")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductCostManualController {

    @Autowired
    private ProductCostManualService productCostManualService;

    /**
     * Create a new product cost entry
     * POST /api/product-cost/add
     */
    @PostMapping("/add")
    public ResponseEntity<ProductCostManualResponse> createProductCost(
            @RequestBody ProductCostManualRequest request) {
        try {
            ProductCostManualResponse response = productCostManualService.createProductCost(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get product cost by product code
     * GET /api/product-cost/by-code/{productCode}
     */
    @GetMapping("/by-code/{productCode}")
    public ResponseEntity<ProductCostManualResponse> getProductCostByCode(
            @PathVariable String productCode) {
        ProductCostManualResponse response = productCostManualService.getProductCostByCode(productCode);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all product costs
     * GET /api/product-cost/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<ProductCostManualResponse>> getAllProductCosts() {
        List<ProductCostManualResponse> responses = productCostManualService.getAllProductCosts();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get product costs by product name
     * GET /api/product-cost/by-name/{productName}
     */
    @GetMapping("/by-name/{productName}")
    public ResponseEntity<List<ProductCostManualResponse>> getProductCostsByName(
            @PathVariable String productName) {
        List<ProductCostManualResponse> responses = productCostManualService.getProductCostsByName(productName);
        return ResponseEntity.ok(responses);
    }

    /**
     * Update product cost
     * PUT /api/product-cost/update/{pid}
     */
    @PutMapping("/update/{pid}")
    public ResponseEntity<ProductCostManualResponse> updateProductCost(
            @PathVariable Long pid,
            @RequestBody ProductCostManualRequest request) {
        try {
            ProductCostManualResponse response = productCostManualService.updateProductCost(pid, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Delete product cost
     * DELETE /api/product-cost/delete/{pid}
     */
    @DeleteMapping("/delete/{pid}")
    public ResponseEntity<Void> deleteProductCost(@PathVariable Long pid) {
        try {
            productCostManualService.deleteProductCost(pid);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if product code exists
     * GET /api/product-cost/exists/{productCode}
     */
    @GetMapping("/exists/{productCode}")
    public ResponseEntity<Boolean> existsByProductCode(@PathVariable String productCode) {
        boolean exists = productCostManualService.existsByProductCode(productCode);
        return ResponseEntity.ok(exists);
    }
}

