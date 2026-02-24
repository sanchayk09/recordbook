package com.urviclean.recordbook.services;

import com.urviclean.recordbook.models.ProductSalesDTO;
import com.urviclean.recordbook.repositories.ProductCostManualRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductCostService {

    @Autowired
    private ProductCostManualRepository productCostManualRepository;

    /**
     * Enriches ProductSalesDTO with cost information from product_cost_manual table
     * Calculates: totalCost = productCost * totalQuantity
     */
    public ProductSalesDTO enrichWithCost(ProductSalesDTO dto) {
        if (dto == null || dto.getProductCode() == null) {
            return dto;
        }

        // Look up cost from product_cost_manual table
        var productCostOptional = productCostManualRepository.findByProductCode(dto.getProductCode());

        if (productCostOptional.isPresent()) {
            BigDecimal cost = productCostOptional.get().getCost();
            dto.setProductCost(cost);

            // Calculate total cost: quantity * cost per unit
            BigDecimal totalCost = BigDecimal.valueOf(dto.getTotalQuantity())
                    .multiply(cost);
            dto.setTotalCost(totalCost);
        } else {
            // If cost not found, set to 0
            dto.setProductCost(BigDecimal.ZERO);
            dto.setTotalCost(BigDecimal.ZERO);
        }

        return dto;
    }

    /**
     * Enriches a list of ProductSalesDTO with cost information
     */
    public List<ProductSalesDTO> enrichWithCosts(List<ProductSalesDTO> dtos) {
        if (dtos == null) {
            return dtos;
        }

        return dtos.stream()
                .map(this::enrichWithCost)
                .collect(Collectors.toList());
    }
}

