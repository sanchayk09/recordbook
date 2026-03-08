package com.urviclean.recordbook.services;

import com.urviclean.recordbook.exception.ResourceNotFoundException;
import com.urviclean.recordbook.models.*;
import com.urviclean.recordbook.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing chemicals, chemical inventory, production batches,
 * batch consumptions, product recipes, and warehouses.
 */
@Service
@Transactional
public class ProductionService {

    @Autowired
    private ChemicalRepository chemicalRepository;

    @Autowired
    private ChemicalInventoryRepository chemicalInventoryRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ProductRecipeRepository productRecipeRepository;

    @Autowired
    private ProductionBatchRepository productionBatchRepository;

    @Autowired
    private BatchConsumptionRepository batchConsumptionRepository;

    // ---- Chemicals ----

    public List<Chemical> getAllChemicals() {
        return chemicalRepository.findAll();
    }

    public Chemical getChemicalById(Long id) {
        return chemicalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chemical", "id", id));
    }

    public Chemical saveChemical(Chemical chemical) {
        return chemicalRepository.save(chemical);
    }

    public void deleteChemical(Long id) {
        if (!chemicalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Chemical", "id", id);
        }
        chemicalRepository.deleteById(id);
    }

    // ---- Chemical Inventory ----

    public List<ChemicalInventory> getAllChemicalInventory() {
        return chemicalInventoryRepository.findAll();
    }

    public ChemicalInventory getChemicalInventoryById(Long id) {
        return chemicalInventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChemicalInventory", "id", id));
    }

    public ChemicalInventory saveChemicalInventory(ChemicalInventory inventory) {
        return chemicalInventoryRepository.save(inventory);
    }

    public void deleteChemicalInventory(Long id) {
        if (!chemicalInventoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("ChemicalInventory", "id", id);
        }
        chemicalInventoryRepository.deleteById(id);
    }

    // ---- Warehouses ----

    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    public Warehouse getWarehouseById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
    }

    public Warehouse saveWarehouse(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    public void deleteWarehouse(Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Warehouse", "id", id);
        }
        warehouseRepository.deleteById(id);
    }

    // ---- Product Recipes ----

    public List<ProductRecipe> getAllProductRecipes() {
        return productRecipeRepository.findAll();
    }

    public ProductRecipe getProductRecipeById(Long id) {
        return productRecipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductRecipe", "id", id));
    }

    public ProductRecipe saveProductRecipe(ProductRecipe recipe) {
        return productRecipeRepository.save(recipe);
    }

    public void deleteProductRecipe(Long id) {
        if (!productRecipeRepository.existsById(id)) {
            throw new ResourceNotFoundException("ProductRecipe", "id", id);
        }
        productRecipeRepository.deleteById(id);
    }

    // ---- Production Batches ----

    public List<ProductionBatch> getAllProductionBatches() {
        return productionBatchRepository.findAll();
    }

    public ProductionBatch getProductionBatchById(Long id) {
        return productionBatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductionBatch", "id", id));
    }

    public ProductionBatch saveProductionBatch(ProductionBatch batch) {
        return productionBatchRepository.save(batch);
    }

    public void deleteProductionBatch(Long id) {
        if (!productionBatchRepository.existsById(id)) {
            throw new ResourceNotFoundException("ProductionBatch", "id", id);
        }
        productionBatchRepository.deleteById(id);
    }

    // ---- Batch Consumption ----

    public List<BatchConsumption> getAllBatchConsumptions() {
        return batchConsumptionRepository.findAll();
    }

    public BatchConsumption getBatchConsumptionById(Long id) {
        return batchConsumptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BatchConsumption", "id", id));
    }

    public BatchConsumption saveBatchConsumption(BatchConsumption consumption) {
        return batchConsumptionRepository.save(consumption);
    }

    public void deleteBatchConsumption(Long id) {
        if (!batchConsumptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("BatchConsumption", "id", id);
        }
        batchConsumptionRepository.deleteById(id);
    }
}
