package com.urviclean.recordbook.controllers;

import com.urviclean.recordbook.models.*;
import com.urviclean.recordbook.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(
        origins = "http://localhost:3000",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Sales endpoints
    @GetMapping("/sales")
    public List<SalesRecord> listSales() {
        return adminService.getAllSales();
    }

    @GetMapping("/sales/{id}")
    public ResponseEntity<SalesRecord> getSale(@PathVariable Long id) {
        SalesRecord sale = adminService.getSaleById(id);
        if (sale == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(sale);
    }

    @PostMapping("/sales")
    public SalesRecord createSale(@RequestBody SalesRecord sale) {
        return adminService.saveSale(sale);
    }

    @PutMapping("/sales/{id}")
    public ResponseEntity<SalesRecord> updateSale(@PathVariable Long id, @RequestBody SalesRecord newDetails) {
        SalesRecord existing = adminService.getSaleById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setSaleId(id);
        return ResponseEntity.ok(adminService.saveSale(newDetails));
    }

    @DeleteMapping("/sales/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        adminService.deleteSale(id);
        return ResponseEntity.noContent().build();
    }

    // Vendor endpoints
    @GetMapping("/vendors")
    public List<Vendor> listVendors() { return adminService.getAllVendors(); }

    @GetMapping("/vendors/{id}")
    public ResponseEntity<Vendor> getVendor(@PathVariable Long id) {
        Vendor v = adminService.getVendorById(id);
        if (v == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(v);
    }

    @PostMapping("/vendors")
    public Vendor createVendor(@RequestBody Vendor vendor) { return adminService.saveVendor(vendor); }

    @PutMapping("/vendors/{id}")
    public ResponseEntity<Vendor> updateVendor(@PathVariable Long id, @RequestBody Vendor newDetails) {
        Vendor existing = adminService.getVendorById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setVendorId(id);
        return ResponseEntity.ok(adminService.saveVendor(newDetails));
    }

    @DeleteMapping("/vendors/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        adminService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }

    // Product endpoints
    @GetMapping("/products")
    public List<Product> listProducts() { return adminService.getAllProducts(); }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Product p = adminService.getProductById(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) { return adminService.saveProduct(product); }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product newDetails) {
        Product existing = adminService.getProductById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setProductId(id);
        return ResponseEntity.ok(adminService.saveProduct(newDetails));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Customer endpoints
    @GetMapping("/customers")
    public List<Customer> listCustomers() { return adminService.getAllCustomers(); }

    @GetMapping("/customers/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        Customer c = adminService.getCustomerById(id);
        if (c == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(c);
    }

    @PostMapping("/customers")
    public Customer createCustomer(@RequestBody Customer customer) { return adminService.saveCustomer(customer); }

    @PutMapping("/customers/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer newDetails) {
        Customer existing = adminService.getCustomerById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setCustomerId(id);
        return ResponseEntity.ok(adminService.saveCustomer(newDetails));
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        adminService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    // Salesman endpoints
    @GetMapping("/salesmen")
    public List<Salesman> listSalesmen() { return adminService.getAllSalesmen(); }

    @GetMapping("/salesmen/aliases")
    public List<String> getSalesmanAliases() {
        return adminService.getAllSalesmen().stream()
                .map(Salesman::getAlias)
                .toList();
    }

    @GetMapping("/salesmen/lookup")
    public ResponseEntity<List<Long>> getSalesmanIdsByName(@RequestParam String firstName,
                                                          @RequestParam String lastName) {
        List<Long> ids = adminService.getSalesmanIdsByName(firstName, lastName);
        if (ids.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ids);
    }

    @GetMapping("/salesmen/{id}")
    public ResponseEntity<Salesman> getSalesman(@PathVariable Long id) {
        Salesman s = adminService.getSalesmanById(id);
        if (s == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(s);
    }

    @PostMapping("/salesmen")
    public Salesman createSalesman(@RequestBody Salesman salesman) { return adminService.saveSalesman(salesman); }

    @PutMapping("/salesmen/{id}")
    public ResponseEntity<Salesman> updateSalesman(@PathVariable Long id, @RequestBody Salesman newDetails) {
        Salesman existing = adminService.getSalesmanById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setSalesmanId(id);
        return ResponseEntity.ok(adminService.saveSalesman(newDetails));
    }

    @DeleteMapping("/salesmen/{id}")
    public ResponseEntity<Void> deleteSalesman(@PathVariable Long id) {
        adminService.deleteSalesman(id);
        return ResponseEntity.noContent().build();
    }

    // Chemicals endpoints
    @GetMapping("/chemicals")
    public List<Chemical> listChemicals() { return adminService.getAllChemicals(); }

    @GetMapping("/chemicals/{id}")
    public ResponseEntity<Chemical> getChemical(@PathVariable Long id) {
        Chemical c = adminService.getChemicalById(id);
        if (c == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(c);
    }

    @PostMapping("/chemicals")
    public Chemical createChemical(@RequestBody Chemical chemical) { return adminService.saveChemical(chemical); }

    @PutMapping("/chemicals/{id}")
    public ResponseEntity<Chemical> updateChemical(@PathVariable Long id, @RequestBody Chemical newDetails) {
        Chemical existing = adminService.getChemicalById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setChemicalId(id);
        return ResponseEntity.ok(adminService.saveChemical(newDetails));
    }

    @DeleteMapping("/chemicals/{id}")
    public ResponseEntity<Void> deleteChemical(@PathVariable Long id) {
        adminService.deleteChemical(id);
        return ResponseEntity.noContent().build();
    }

    // Warehouse endpoints
    @GetMapping("/warehouses")
    public List<Warehouse> listWarehouses() { return adminService.getAllWarehouses(); }

    @GetMapping("/warehouses/{id}")
    public ResponseEntity<Warehouse> getWarehouse(@PathVariable Long id) {
        Warehouse w = adminService.getWarehouseById(id);
        if (w == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(w);
    }

    @PostMapping("/warehouses")
    public Warehouse createWarehouse(@RequestBody Warehouse warehouse) { return adminService.saveWarehouse(warehouse); }

    @PutMapping("/warehouses/{id}")
    public ResponseEntity<Warehouse> updateWarehouse(@PathVariable Long id, @RequestBody Warehouse newDetails) {
        Warehouse existing = adminService.getWarehouseById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setWarehouseId(id);
        return ResponseEntity.ok(adminService.saveWarehouse(newDetails));
    }

    @DeleteMapping("/warehouses/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        adminService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }

    // Chemical Inventory endpoints
    @GetMapping("/chemical-inventory")
    public List<ChemicalInventory> listChemicalInventory() { return adminService.getAllChemicalInventory(); }

    @GetMapping("/chemical-inventory/{id}")
    public ResponseEntity<ChemicalInventory> getChemicalInventory(@PathVariable Long id) {
        ChemicalInventory ci = adminService.getChemicalInventoryById(id);
        if (ci == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ci);
    }

    @PostMapping("/chemical-inventory")
    public ChemicalInventory createChemicalInventory(@RequestBody ChemicalInventory inventory) { return adminService.saveChemicalInventory(inventory); }

    @PutMapping("/chemical-inventory/{id}")
    public ResponseEntity<ChemicalInventory> updateChemicalInventory(@PathVariable Long id, @RequestBody ChemicalInventory newDetails) {
        ChemicalInventory existing = adminService.getChemicalInventoryById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setInventoryId(id);
        return ResponseEntity.ok(adminService.saveChemicalInventory(newDetails));
    }

    @DeleteMapping("/chemical-inventory/{id}")
    public ResponseEntity<Void> deleteChemicalInventory(@PathVariable Long id) {
        adminService.deleteChemicalInventory(id);
        return ResponseEntity.noContent().build();
    }

    // Product Recipes endpoints
    @GetMapping("/product-recipes")
    public List<ProductRecipe> listProductRecipes() { return adminService.getAllProductRecipes(); }

    @GetMapping("/product-recipes/{id}")
    public ResponseEntity<ProductRecipe> getProductRecipe(@PathVariable Long id) {
        ProductRecipe pr = adminService.getProductRecipeById(id);
        if (pr == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(pr);
    }

    @PostMapping("/product-recipes")
    public ProductRecipe createProductRecipe(@RequestBody ProductRecipe recipe) { return adminService.saveProductRecipe(recipe); }

    @PutMapping("/product-recipes/{id}")
    public ResponseEntity<ProductRecipe> updateProductRecipe(@PathVariable Long id, @RequestBody ProductRecipe newDetails) {
        ProductRecipe existing = adminService.getProductRecipeById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setRecipeId(id);
        return ResponseEntity.ok(adminService.saveProductRecipe(newDetails));
    }

    @DeleteMapping("/product-recipes/{id}")
    public ResponseEntity<Void> deleteProductRecipe(@PathVariable Long id) {
        adminService.deleteProductRecipe(id);
        return ResponseEntity.noContent().build();
    }

    // Production Batches endpoints
    @GetMapping("/production-batches")
    public List<ProductionBatch> listProductionBatches() { return adminService.getAllProductionBatches(); }

    @GetMapping("/production-batches/{id}")
    public ResponseEntity<ProductionBatch> getProductionBatch(@PathVariable Long id) {
        ProductionBatch pb = adminService.getProductionBatchById(id);
        if (pb == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(pb);
    }

    @PostMapping("/production-batches")
    public ProductionBatch createProductionBatch(@RequestBody ProductionBatch batch) { return adminService.saveProductionBatch(batch); }

    @PutMapping("/production-batches/{id}")
    public ResponseEntity<ProductionBatch> updateProductionBatch(@PathVariable Long id, @RequestBody ProductionBatch newDetails) {
        ProductionBatch existing = adminService.getProductionBatchById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setBatchId(id);
        return ResponseEntity.ok(adminService.saveProductionBatch(newDetails));
    }

    @DeleteMapping("/production-batches/{id}")
    public ResponseEntity<Void> deleteProductionBatch(@PathVariable Long id) {
        adminService.deleteProductionBatch(id);
        return ResponseEntity.noContent().build();
    }

    // Batch Consumption endpoints
    @GetMapping("/batch-consumption")
    public List<BatchConsumption> listBatchConsumptions() { return adminService.getAllBatchConsumptions(); }

    @GetMapping("/batch-consumption/{id}")
    public ResponseEntity<BatchConsumption> getBatchConsumption(@PathVariable Long id) {
        BatchConsumption bc = adminService.getBatchConsumptionById(id);
        if (bc == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(bc);
    }

    @PostMapping("/batch-consumption")
    public BatchConsumption createBatchConsumption(@RequestBody BatchConsumption consumption) { return adminService.saveBatchConsumption(consumption); }

    @PutMapping("/batch-consumption/{id}")
    public ResponseEntity<BatchConsumption> updateBatchConsumption(@PathVariable Long id, @RequestBody BatchConsumption newDetails) {
        BatchConsumption existing = adminService.getBatchConsumptionById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setConsumptionId(id);
        return ResponseEntity.ok(adminService.saveBatchConsumption(newDetails));
    }

    @DeleteMapping("/batch-consumption/{id}")
    public ResponseEntity<Void> deleteBatchConsumption(@PathVariable Long id) {
        adminService.deleteBatchConsumption(id);
        return ResponseEntity.noContent().build();
    }

    // Routes endpoints
    @GetMapping("/routes")
    public List<Route> listRoutes() { return adminService.getAllRoutes(); }

    @GetMapping("/routes/{id}")
    public ResponseEntity<Route> getRoute(@PathVariable Long id) {
        Route r = adminService.getRouteById(id);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(r);
    }

    @PostMapping("/routes")
    public Route createRoute(@RequestBody Route route) { return adminService.saveRoute(route); }

    @PutMapping("/routes/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable Long id, @RequestBody Route newDetails) {
        Route existing = adminService.getRouteById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setRouteId(id);
        return ResponseEntity.ok(adminService.saveRoute(newDetails));
    }

    @DeleteMapping("/routes/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        adminService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    // Route Villages endpoints
    @GetMapping("/route-villages")
    public List<RouteVillage> listRouteVillages() { return adminService.getAllRouteVillages(); }

    @GetMapping("/route-villages/{id}")
    public ResponseEntity<RouteVillage> getRouteVillage(@PathVariable Long id) {
        RouteVillage rv = adminService.getRouteVillageById(id);
        if (rv == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(rv);
    }

    @PostMapping("/route-villages")
    public RouteVillage createRouteVillage(@RequestBody RouteVillage village) { return adminService.saveRouteVillage(village); }

    @PutMapping("/route-villages/{id}")
    public ResponseEntity<RouteVillage> updateRouteVillage(@PathVariable Long id, @RequestBody RouteVillage newDetails) {
        RouteVillage existing = adminService.getRouteVillageById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setVillageId(id);
        return ResponseEntity.ok(adminService.saveRouteVillage(newDetails));
    }

    @DeleteMapping("/route-villages/{id}")
    public ResponseEntity<Void> deleteRouteVillage(@PathVariable Long id) {
        adminService.deleteRouteVillage(id);
        return ResponseEntity.noContent().build();
    }

    // Salesman Expenses endpoints
    @GetMapping("/expenses")
    public List<SalesmanExpense> listExpenses() { return adminService.getAllSalesmanExpenses(); }

    @GetMapping("/expenses/{id}")
    public ResponseEntity<SalesmanExpense> getExpense(@PathVariable Long id) {
        SalesmanExpense e = adminService.getSalesmanExpenseById(id);
        if (e == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(e);
    }

    @PostMapping("/expenses")
    public SalesmanExpense createExpense(@RequestBody SalesmanExpense expense) { return adminService.saveSalesmanExpense(expense); }

    @PutMapping("/expenses/{id}")
    public ResponseEntity<SalesmanExpense> updateExpense(@PathVariable Long id, @RequestBody SalesmanExpense newDetails) {
        SalesmanExpense existing = adminService.getSalesmanExpenseById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        newDetails.setExpenseId(id);
        return ResponseEntity.ok(adminService.saveSalesmanExpense(newDetails));
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        adminService.deleteSalesmanExpense(id);
        return ResponseEntity.noContent().build();
    }
}
