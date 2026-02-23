package com.urviclean.recordbook.services;

import com.urviclean.recordbook.interfaces.AdminInterface;
import com.urviclean.recordbook.models.*;
import com.urviclean.recordbook.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminService implements AdminInterface {

    @Autowired
    private SalesRecordRepository salesRecordRepository;
    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private SalesmanRepository salesmanRepository;
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
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private RouteVillageRepository routeVillageRepository;
    @Autowired
    private SalesmanExpenseRepository salesmanExpenseRepository;

    // SalesRecord
    @Override
    public List<SalesRecord> getAllSales() { return salesRecordRepository.findAll(); }
    @Override
    public SalesRecord getSaleById(Long id) { return salesRecordRepository.findById(id).orElse(null); }
    @Override
    public SalesRecord saveSale(SalesRecord sale) { return salesRecordRepository.save(sale); }
    @Override
    public void deleteSale(Long id) { salesRecordRepository.deleteById(id); }

    // Vendor
    @Override
    public List<Vendor> getAllVendors() { return vendorRepository.findAll(); }
    @Override
    public Vendor getVendorById(Long id) { return vendorRepository.findById(id).orElse(null); }
    @Override
    public Vendor saveVendor(Vendor vendor) { return vendorRepository.save(vendor); }
    @Override
    public void deleteVendor(Long id) { vendorRepository.deleteById(id); }

    // Product
    @Override
    public List<Product> getAllProducts() { return productRepository.findAll(); }
    @Override
    public Product getProductById(Long id) { return productRepository.findById(id).orElse(null); }
    @Override
    public Product saveProduct(Product product) { return productRepository.save(product); }
    @Override
    public void deleteProduct(Long id) { productRepository.deleteById(id); }

    // Customer
    @Override
    public List<Customer> getAllCustomers() { return customerRepository.findAll(); }
    @Override
    public Customer getCustomerById(Long id) { return customerRepository.findById(id).orElse(null); }
    @Override
    public Customer saveCustomer(Customer customer) { return customerRepository.save(customer); }
    @Override
    public void deleteCustomer(Long id) { customerRepository.deleteById(id); }

    // Salesman
    @Override
    public List<Salesman> getAllSalesmen() { return salesmanRepository.findAll(); }
    @Override
    public Salesman getSalesmanById(Long id) { return salesmanRepository.findById(id).orElse(null); }
    @Override
    public Salesman saveSalesman(Salesman salesman) { return salesmanRepository.save(salesman); }
    @Override
    public void deleteSalesman(Long id) { salesmanRepository.deleteById(id); }
    @Override
    public List<Long> getSalesmanIdsByName(String firstName, String lastName) {
        return salesmanRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName)
                .stream()
                .map(Salesman::getSalesmanId)
                .toList();
    }

    // Chemicals
    @Override
    public List<Chemical> getAllChemicals() { return chemicalRepository.findAll(); }
    @Override
    public Chemical getChemicalById(Long id) { return chemicalRepository.findById(id).orElse(null); }
    @Override
    public Chemical saveChemical(Chemical chemical) { return chemicalRepository.save(chemical); }
    @Override
    public void deleteChemical(Long id) { chemicalRepository.deleteById(id); }

    // Chemical Inventory
    @Override
    public List<ChemicalInventory> getAllChemicalInventory() { return chemicalInventoryRepository.findAll(); }
    @Override
    public ChemicalInventory getChemicalInventoryById(Long id) { return chemicalInventoryRepository.findById(id).orElse(null); }
    @Override
    public ChemicalInventory saveChemicalInventory(ChemicalInventory inventory) { return chemicalInventoryRepository.save(inventory); }
    @Override
    public void deleteChemicalInventory(Long id) { chemicalInventoryRepository.deleteById(id); }

    // Warehouse
    @Override
    public List<Warehouse> getAllWarehouses() { return warehouseRepository.findAll(); }
    @Override
    public Warehouse getWarehouseById(Long id) { return warehouseRepository.findById(id).orElse(null); }
    @Override
    public Warehouse saveWarehouse(Warehouse warehouse) { return warehouseRepository.save(warehouse); }
    @Override
    public void deleteWarehouse(Long id) { warehouseRepository.deleteById(id); }

    // Product Recipes
    @Override
    public List<ProductRecipe> getAllProductRecipes() { return productRecipeRepository.findAll(); }
    @Override
    public ProductRecipe getProductRecipeById(Long id) { return productRecipeRepository.findById(id).orElse(null); }
    @Override
    public ProductRecipe saveProductRecipe(ProductRecipe recipe) { return productRecipeRepository.save(recipe); }
    @Override
    public void deleteProductRecipe(Long id) { productRecipeRepository.deleteById(id); }

    // Production Batches
    @Override
    public List<ProductionBatch> getAllProductionBatches() { return productionBatchRepository.findAll(); }
    @Override
    public ProductionBatch getProductionBatchById(Long id) { return productionBatchRepository.findById(id).orElse(null); }
    @Override
    public ProductionBatch saveProductionBatch(ProductionBatch batch) { return productionBatchRepository.save(batch); }
    @Override
    public void deleteProductionBatch(Long id) { productionBatchRepository.deleteById(id); }

    // Batch Consumption
    @Override
    public List<BatchConsumption> getAllBatchConsumptions() { return batchConsumptionRepository.findAll(); }
    @Override
    public BatchConsumption getBatchConsumptionById(Long id) { return batchConsumptionRepository.findById(id).orElse(null); }
    @Override
    public BatchConsumption saveBatchConsumption(BatchConsumption consumption) { return batchConsumptionRepository.save(consumption); }
    @Override
    public void deleteBatchConsumption(Long id) { batchConsumptionRepository.deleteById(id); }

    // Routes
    @Override
    public List<Route> getAllRoutes() { return routeRepository.findAll(); }
    @Override
    public Route getRouteById(Long id) { return routeRepository.findById(id).orElse(null); }
    @Override
    public Route saveRoute(Route route) { return routeRepository.save(route); }
    @Override
    public void deleteRoute(Long id) { routeRepository.deleteById(id); }

    // Route Villages
    @Override
    public List<RouteVillage> getAllRouteVillages() { return routeVillageRepository.findAll(); }
    @Override
    public RouteVillage getRouteVillageById(Long id) { return routeVillageRepository.findById(id).orElse(null); }
    @Override
    public RouteVillage saveRouteVillage(RouteVillage village) { return routeVillageRepository.save(village); }
    @Override
    public void deleteRouteVillage(Long id) { routeVillageRepository.deleteById(id); }

    // Salesman Expenses
    @Override
    public List<SalesmanExpense> getAllSalesmanExpenses() { return salesmanExpenseRepository.findAll(); }
    @Override
    public SalesmanExpense getSalesmanExpenseById(Long id) { return salesmanExpenseRepository.findById(id).orElse(null); }
    @Override
    public SalesmanExpense saveSalesmanExpense(SalesmanExpense expense) { return salesmanExpenseRepository.save(expense); }
    @Override
    public void deleteSalesmanExpense(Long id) { salesmanExpenseRepository.deleteById(id); }
}
