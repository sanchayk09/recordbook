package com.urviclean.recordbook.controllers;

import com.urviclean.recordbook.models.*;
import com.urviclean.recordbook.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(
        origins = "http://localhost:3000",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
@Tag(name = "Admin Operations", description = "Admin APIs for managing sales, vendors, products, customers, salesmen, production batches, and routes")
public class AdminController {

    @Autowired
    private SalesService salesService;

    @Autowired
    private VendorService vendorService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SalesmanService salesmanService;

    @Autowired
    private ProductionService productionService;

    @Autowired
    private RouteService routeService;

    // ---- Sales endpoints (delegate to SalesService for stock-aware operations) ----

    @GetMapping("/sales")
    @Operation(summary = "List all daily sales", description = "Get all daily sale records")
    public List<DailySaleRecord> listSales() {
        return salesService.getAllSales();
    }

    @GetMapping("/sales/{id}")
    @Operation(summary = "Get daily sale by ID", description = "Get a specific daily sale record")
    public ResponseEntity<DailySaleRecord> getSale(
            @Parameter(description = "Sale ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(salesService.getById(id));
    }

    @PostMapping("/sales")
    @Operation(summary = "Create a daily sale", description = "Creates a sale and decrements salesman stock atomically")
    public DailySaleRecord createSale(@RequestBody DailySaleRecord sale) {
        return salesService.createSale(sale);
    }

    @PutMapping("/sales/{id}")
    @Operation(summary = "Update a daily sale", description = "Updates a sale and reconciles salesman stock")
    public DailySaleRecord updateSale(@PathVariable Long id, @RequestBody DailySaleRecord newDetails) {
        return salesService.updateSale(id, newDetails);
    }

    @DeleteMapping("/sales/{id}")
    @Operation(summary = "Delete (void) a daily sale", description = "Voids a sale and restores salesman stock")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        salesService.voidSale(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Vendor endpoints ----

    @GetMapping("/vendors")
    public List<Vendor> listVendors() { return vendorService.getAll(); }

    @GetMapping("/vendors/{id}")
    public ResponseEntity<Vendor> getVendor(@PathVariable Long id) {
        return ResponseEntity.ok(vendorService.getById(id));
    }

    @PostMapping("/vendors")
    public Vendor createVendor(@RequestBody Vendor vendor) { return vendorService.save(vendor); }

    @PutMapping("/vendors/{id}")
    public ResponseEntity<Vendor> updateVendor(@PathVariable Long id, @RequestBody Vendor newDetails) {
        newDetails.setVendorId(id);
        return ResponseEntity.ok(vendorService.save(newDetails));
    }

    @DeleteMapping("/vendors/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        vendorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Product endpoints ----

    @GetMapping("/products")
    public List<Product> listProducts() { return productService.getAll(); }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) { return productService.save(product); }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product newDetails) {
        newDetails.setProductId(id);
        return ResponseEntity.ok(productService.save(newDetails));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Customer endpoints ----

    @GetMapping("/customers")
    public List<Customer> listCustomers() { return customerService.getAll(); }

    @GetMapping("/customers/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @PostMapping("/customers")
    public Customer createCustomer(@RequestBody Customer customer) { return customerService.save(customer); }

    @PutMapping("/customers/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer newDetails) {
        newDetails.setCustomerId(id);
        return ResponseEntity.ok(customerService.save(newDetails));
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Salesman endpoints ----

    @GetMapping("/salesmen")
    public List<Salesman> listSalesmen() { return salesmanService.getAll(); }

    @GetMapping("/salesmen/aliases")
    public List<String> getSalesmanAliases() {
        return salesmanService.getAll().stream()
                .map(Salesman::getAlias)
                .toList();
    }

    @GetMapping("/salesmen/lookup")
    public ResponseEntity<List<Long>> getSalesmanIdsByName(@RequestParam String firstName,
                                                           @RequestParam String lastName) {
        List<Long> ids = salesmanService.getIdsByName(firstName, lastName);
        if (ids.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ids);
    }

    @GetMapping("/salesmen/{id}")
    public ResponseEntity<Salesman> getSalesman(@PathVariable Long id) {
        return ResponseEntity.ok(salesmanService.getById(id));
    }

    @PostMapping("/salesmen")
    public Salesman createSalesman(@RequestBody Salesman salesman) { return salesmanService.save(salesman); }

    @PutMapping("/salesmen/{id}")
    public ResponseEntity<Salesman> updateSalesman(@PathVariable Long id, @RequestBody Salesman newDetails) {
        newDetails.setSalesmanId(id);
        return ResponseEntity.ok(salesmanService.save(newDetails));
    }

    @DeleteMapping("/salesmen/{id}")
    public ResponseEntity<Void> deleteSalesman(@PathVariable Long id) {
        salesmanService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Chemical endpoints ----

    @GetMapping("/chemicals")
    public List<Chemical> listChemicals() { return productionService.getAllChemicals(); }

    @GetMapping("/chemicals/{id}")
    public ResponseEntity<Chemical> getChemical(@PathVariable Long id) {
        return ResponseEntity.ok(productionService.getChemicalById(id));
    }

    @PostMapping("/chemicals")
    public Chemical createChemical(@RequestBody Chemical chemical) { return productionService.saveChemical(chemical); }

    @PutMapping("/chemicals/{id}")
    public ResponseEntity<Chemical> updateChemical(@PathVariable Long id, @RequestBody Chemical newDetails) {
        newDetails.setChemicalId(id);
        return ResponseEntity.ok(productionService.saveChemical(newDetails));
    }

    @DeleteMapping("/chemicals/{id}")
    public ResponseEntity<Void> deleteChemical(@PathVariable Long id) {
        productionService.deleteChemical(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Warehouse endpoints ----

    @GetMapping("/warehouses")
    public List<Warehouse> listWarehouses() { return productionService.getAllWarehouses(); }

    @GetMapping("/warehouses/{id}")
    public ResponseEntity<Warehouse> getWarehouse(@PathVariable Long id) {
        return ResponseEntity.ok(productionService.getWarehouseById(id));
    }

    @PostMapping("/warehouses")
    public Warehouse createWarehouse(@RequestBody Warehouse warehouse) { return productionService.saveWarehouse(warehouse); }

    @PutMapping("/warehouses/{id}")
    public ResponseEntity<Warehouse> updateWarehouse(@PathVariable Long id, @RequestBody Warehouse newDetails) {
        newDetails.setWarehouseId(id);
        return ResponseEntity.ok(productionService.saveWarehouse(newDetails));
    }

    @DeleteMapping("/warehouses/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        productionService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Chemical Inventory endpoints ----

    @GetMapping("/chemical-inventory")
    public List<ChemicalInventory> listChemicalInventory() { return productionService.getAllChemicalInventory(); }

    @GetMapping("/chemical-inventory/{id}")
    public ResponseEntity<ChemicalInventory> getChemicalInventory(@PathVariable Long id) {
        return ResponseEntity.ok(productionService.getChemicalInventoryById(id));
    }

    @PostMapping("/chemical-inventory")
    public ChemicalInventory createChemicalInventory(@RequestBody ChemicalInventory inventory) { return productionService.saveChemicalInventory(inventory); }

    @PutMapping("/chemical-inventory/{id}")
    public ResponseEntity<ChemicalInventory> updateChemicalInventory(@PathVariable Long id, @RequestBody ChemicalInventory newDetails) {
        newDetails.setInventoryId(id);
        return ResponseEntity.ok(productionService.saveChemicalInventory(newDetails));
    }

    @DeleteMapping("/chemical-inventory/{id}")
    public ResponseEntity<Void> deleteChemicalInventory(@PathVariable Long id) {
        productionService.deleteChemicalInventory(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Product Recipes endpoints ----

    @GetMapping("/product-recipes")
    public List<ProductRecipe> listProductRecipes() { return productionService.getAllProductRecipes(); }

    @GetMapping("/product-recipes/{id}")
    public ResponseEntity<ProductRecipe> getProductRecipe(@PathVariable Long id) {
        return ResponseEntity.ok(productionService.getProductRecipeById(id));
    }

    @PostMapping("/product-recipes")
    public ProductRecipe createProductRecipe(@RequestBody ProductRecipe recipe) { return productionService.saveProductRecipe(recipe); }

    @PutMapping("/product-recipes/{id}")
    public ResponseEntity<ProductRecipe> updateProductRecipe(@PathVariable Long id, @RequestBody ProductRecipe newDetails) {
        newDetails.setRecipeId(id);
        return ResponseEntity.ok(productionService.saveProductRecipe(newDetails));
    }

    @DeleteMapping("/product-recipes/{id}")
    public ResponseEntity<Void> deleteProductRecipe(@PathVariable Long id) {
        productionService.deleteProductRecipe(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Production Batches endpoints ----

    @GetMapping("/production-batches")
    public List<ProductionBatch> listProductionBatches() { return productionService.getAllProductionBatches(); }

    @GetMapping("/production-batches/{id}")
    public ResponseEntity<ProductionBatch> getProductionBatch(@PathVariable Long id) {
        return ResponseEntity.ok(productionService.getProductionBatchById(id));
    }

    @PostMapping("/production-batches")
    public ProductionBatch createProductionBatch(@RequestBody ProductionBatch batch) { return productionService.saveProductionBatch(batch); }

    @PutMapping("/production-batches/{id}")
    public ResponseEntity<ProductionBatch> updateProductionBatch(@PathVariable Long id, @RequestBody ProductionBatch newDetails) {
        newDetails.setBatchId(id);
        return ResponseEntity.ok(productionService.saveProductionBatch(newDetails));
    }

    @DeleteMapping("/production-batches/{id}")
    public ResponseEntity<Void> deleteProductionBatch(@PathVariable Long id) {
        productionService.deleteProductionBatch(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Batch Consumption endpoints ----

    @GetMapping("/batch-consumption")
    public List<BatchConsumption> listBatchConsumptions() { return productionService.getAllBatchConsumptions(); }

    @GetMapping("/batch-consumption/{id}")
    public ResponseEntity<BatchConsumption> getBatchConsumption(@PathVariable Long id) {
        return ResponseEntity.ok(productionService.getBatchConsumptionById(id));
    }

    @PostMapping("/batch-consumption")
    public BatchConsumption createBatchConsumption(@RequestBody BatchConsumption consumption) { return productionService.saveBatchConsumption(consumption); }

    @PutMapping("/batch-consumption/{id}")
    public ResponseEntity<BatchConsumption> updateBatchConsumption(@PathVariable Long id, @RequestBody BatchConsumption newDetails) {
        newDetails.setConsumptionId(id);
        return ResponseEntity.ok(productionService.saveBatchConsumption(newDetails));
    }

    @DeleteMapping("/batch-consumption/{id}")
    public ResponseEntity<Void> deleteBatchConsumption(@PathVariable Long id) {
        productionService.deleteBatchConsumption(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Route endpoints ----

    @GetMapping("/routes")
    public List<Route> listRoutes() { return routeService.getAllRoutes(); }

    @GetMapping("/routes/{id}")
    public ResponseEntity<Route> getRoute(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @PostMapping("/routes")
    public Route createRoute(@RequestBody Route route) { return routeService.saveRoute(route); }

    @PutMapping("/routes/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable Long id, @RequestBody Route newDetails) {
        newDetails.setRouteId(id);
        return ResponseEntity.ok(routeService.saveRoute(newDetails));
    }

    @DeleteMapping("/routes/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Route Villages endpoints ----

    @GetMapping("/route-villages")
    public List<RouteVillage> listRouteVillages() { return routeService.getAllRouteVillages(); }

    @GetMapping("/route-villages/{id}")
    public ResponseEntity<RouteVillage> getRouteVillage(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteVillageById(id));
    }

    @PostMapping("/route-villages")
    public RouteVillage createRouteVillage(@RequestBody RouteVillage village) { return routeService.saveRouteVillage(village); }

    @PutMapping("/route-villages/{id}")
    public ResponseEntity<RouteVillage> updateRouteVillage(@PathVariable Long id, @RequestBody RouteVillage newDetails) {
        newDetails.setVillageId(id);
        return ResponseEntity.ok(routeService.saveRouteVillage(newDetails));
    }

    @DeleteMapping("/route-villages/{id}")
    public ResponseEntity<Void> deleteRouteVillage(@PathVariable Long id) {
        routeService.deleteRouteVillage(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Salesman Expenses endpoints ----

    @GetMapping("/expenses")
    public List<SalesmanExpense> listExpenses() { return salesmanService.getAllExpenses(); }

    @GetMapping("/expenses/{id}")
    public ResponseEntity<SalesmanExpense> getExpense(@PathVariable Long id) {
        return ResponseEntity.ok(salesmanService.getExpenseById(id));
    }

    @PostMapping("/expenses")
    public SalesmanExpense createExpense(@RequestBody SalesmanExpense expense) { return salesmanService.saveExpense(expense); }

    @PutMapping("/expenses/{id}")
    public ResponseEntity<SalesmanExpense> updateExpense(@PathVariable Long id, @RequestBody SalesmanExpense newDetails) {
        newDetails.setExpenseId(id);
        return ResponseEntity.ok(salesmanService.saveExpense(newDetails));
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        salesmanService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
