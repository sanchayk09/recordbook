package com.urviclean.recordbook.services;

import com.urviclean.recordbook.models.ProductCostManual;
import com.urviclean.recordbook.models.ProductCostManualRequest;
import com.urviclean.recordbook.models.ProductCostManualResponse;
import com.urviclean.recordbook.repositories.ProductCostManualRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductCostManualService {

    @Autowired
    private ProductCostManualRepository productCostManualRepository;

    /**
     * Create a new product cost entry
     */
    public ProductCostManualResponse createProductCost(ProductCostManualRequest request) {
        ProductCostManual productCost = new ProductCostManual(
                request.getProductName(),
                request.getProductCode(),
                request.getCost()
        );
        ProductCostManual saved = productCostManualRepository.save(productCost);
        return ProductCostManualResponse.fromEntity(saved);
    }

    /**
     * Get product cost by product code
     */
    public ProductCostManualResponse getProductCostByCode(String productCode) {
        return productCostManualRepository.findByProductCode(productCode)
                .map(ProductCostManualResponse::fromEntity)
                .orElse(null);
    }

    /**
     * Get all product costs
     */
    public List<ProductCostManualResponse> getAllProductCosts() {
        return productCostManualRepository.findAll()
                .stream()
                .map(ProductCostManualResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get product costs by product name
     */
    public List<ProductCostManualResponse> getProductCostsByName(String productName) {
        return productCostManualRepository.findByProductName(productName)
                .stream()
                .map(ProductCostManualResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update product cost
     */
    public ProductCostManualResponse updateProductCost(Long pid, ProductCostManualRequest request) {
        ProductCostManual existing = productCostManualRepository.findById(pid)
                .orElseThrow(() -> new RuntimeException("Product cost not found with id: " + pid));

        existing.setProductName(request.getProductName());
        existing.setProductCode(request.getProductCode());
        existing.setCost(request.getCost());

        ProductCostManual updated = productCostManualRepository.save(existing);
        return ProductCostManualResponse.fromEntity(updated);
    }

    /**
     * Delete product cost
     */
    public void deleteProductCost(Long pid) {
        productCostManualRepository.deleteById(pid);
    }

    /**
     * Check if product code exists
     */
    public boolean existsByProductCode(String productCode) {
        return productCostManualRepository.existsByProductCode(productCode);
    }
}

