package com.urviclean.recordbook.interfaces;

import com.urviclean.recordbook.models.*;

import java.util.List;

public interface AdminInterface {
    // SalesRecord
    List<SalesRecord> getAllSales();
    SalesRecord getSaleById(Long id);
    SalesRecord saveSale(SalesRecord sale);
    void deleteSale(Long id);

    // Vendor
    List<Vendor> getAllVendors();
    Vendor getVendorById(Long id);
    Vendor saveVendor(Vendor vendor);
    void deleteVendor(Long id);

    // Product
    List<Product> getAllProducts();
    Product getProductById(Long id);
    Product saveProduct(Product product);
    void deleteProduct(Long id);

    // Customer
    List<Customer> getAllCustomers();
    Customer getCustomerById(Long id);
    Customer saveCustomer(Customer customer);
    void deleteCustomer(Long id);

    // Salesman
    List<Salesman> getAllSalesmen();
    Salesman getSalesmanById(Long id);
    Salesman saveSalesman(Salesman salesman);
    void deleteSalesman(Long id);
    List<Long> getSalesmanIdsByName(String firstName, String lastName);

    // Chemicals
    List<Chemical> getAllChemicals();
    Chemical getChemicalById(Long id);
    Chemical saveChemical(Chemical chemical);
    void deleteChemical(Long id);

    // Chemical Inventory
    List<ChemicalInventory> getAllChemicalInventory();
    ChemicalInventory getChemicalInventoryById(Long id);
    ChemicalInventory saveChemicalInventory(ChemicalInventory inventory);
    void deleteChemicalInventory(Long id);

    // Warehouse
    List<Warehouse> getAllWarehouses();
    Warehouse getWarehouseById(Long id);
    Warehouse saveWarehouse(Warehouse warehouse);
    void deleteWarehouse(Long id);

    // Product Recipes
    List<ProductRecipe> getAllProductRecipes();
    ProductRecipe getProductRecipeById(Long id);
    ProductRecipe saveProductRecipe(ProductRecipe recipe);
    void deleteProductRecipe(Long id);

    // Production Batches
    List<ProductionBatch> getAllProductionBatches();
    ProductionBatch getProductionBatchById(Long id);
    ProductionBatch saveProductionBatch(ProductionBatch batch);
    void deleteProductionBatch(Long id);

    // Batch Consumption
    List<BatchConsumption> getAllBatchConsumptions();
    BatchConsumption getBatchConsumptionById(Long id);
    BatchConsumption saveBatchConsumption(BatchConsumption consumption);
    void deleteBatchConsumption(Long id);

    // Routes
    List<Route> getAllRoutes();
    Route getRouteById(Long id);
    Route saveRoute(Route route);
    void deleteRoute(Long id);

    // Route Villages
    List<RouteVillage> getAllRouteVillages();
    RouteVillage getRouteVillageById(Long id);
    RouteVillage saveRouteVillage(RouteVillage village);
    void deleteRouteVillage(Long id);

    // Salesman Expenses
    List<SalesmanExpense> getAllSalesmanExpenses();
    SalesmanExpense getSalesmanExpenseById(Long id);
    SalesmanExpense saveSalesmanExpense(SalesmanExpense expense);
    void deleteSalesmanExpense(Long id);
}
