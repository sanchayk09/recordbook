import axios from 'axios';
import { notifyError } from './utils/toast';

const api = axios.create({
  baseURL:
    process.env.REACT_APP_API_URL ||
    (process.env.NODE_ENV === 'development' ? '' : 'http://localhost:8080'),
  timeout: 10000,
});

// Request interceptor (could add auth headers here)
api.interceptors.request.use((config) => config, (error) => Promise.reject(error));

// Response interceptor for centralized error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message || error.message || 'Network error';
    try {
      notifyError(message);
    } catch (e) {
      // ignore if toast not available
    }
    return Promise.reject(error);
  }
);

// ==================== SALES API ====================
export const salesAPI = {
  // Get all sales records
  getAllSales: () => api.get('/api/sales'),
  
  // Get sales by ID for editing
  getSalesById: (id) => api.get(`/api/sales/${id}`),
  
  // Create new sales record
  createSales: (data) => api.post('/api/admin/sales', data),
  
  // Update existing sales record
  updateSales: (id, data) => api.put(`/api/sales/${id}`, data),
  
  // Delete sales record
  deleteSales: (id) => api.delete(`/api/sales/${id}`),
};

// ==================== PRODUCT SALES SUMMARY API ====================
export const productSalesSummaryAPI = {
  // Get product sales for today
  getTodayProductSales: () => api.get('/api/sales/summary/product-sales/today'),
  
  // Get product sales by specific date
  getProductSalesByDate: (date) => api.get(`/api/sales/summary/product-sales/date?date=${date}`),
  
  // Get product sales by month
  getProductSalesByMonth: (year, month) => 
    api.get(`/api/sales/summary/product-sales/month?year=${year}&month=${month}`),
  
  // Get product sales by date range
  getProductSalesByRange: (startDate, endDate) => 
    api.get(`/api/sales/summary/product-sales/range?startDate=${startDate}&endDate=${endDate}`),
  
  // Get all product sales (fallback)
  getAllProductSales: () => api.get('/api/sales/summary/product-sales'),
};

// ==================== PRODUCT COST API ====================
export const productCostAPI = {
  // Get all product costs
  getAll: () => api.get('/api/product-cost/all'),
  
  // Check if product code exists
  checkCodeExists: (code) => api.get(`/api/product-cost/exists/${code}`),
  
  // Get product by code
  getByCode: (code) => api.get(`/api/product-cost/by-code/${code}`),
  
  // Get product by name
  getByName: (name) => api.get(`/api/product-cost/by-name/${name}`),
  
  // Add new product cost
  add: (data) => api.post('/api/product-cost/add', {
    productName: data.productName,
    productCode: data.productCode,
    cost: data.cost,
  }),
  
  // Update product cost
  update: (id, data) => api.put(`/api/product-cost/update/${id}`, data),
  
  // Delete product cost
  delete: (id) => api.delete(`/api/product-cost/delete/${id}`),
};

// ==================== SALESMAN/ADMIN API ====================
export const salesmanAPI = {
  // Get all salesmen
  getAll: () => api.get('/api/salesmen'),
  
  // Get salesmen aliases
  getAliases: () => api.get('/api/v1/admin/salesmen/aliases'),
  
  // Create new salesman
  create: (data) => api.post('/api/v1/admin/salesmen', data),
};

// ==================== CUSTOMER API ====================
export const customerAPI = {
  // Get all customers
  getAll: () => api.get('/api/customers'),
  
  // Create new customer
  create: (data) => api.post('/api/admin/customers', data),
};

// ==================== ROUTE API ====================
export const routeAPI = {
  // Get all routes
  getAll: () => api.get('/api/routes'),
  
  // Get villages by route
  getVillages: (routeId) => api.get(`/api/routes/${routeId}/villages`),
  
  // Create new route
  create: (data) => api.post('/api/routes', data),
  
  // Add village to route
  addVillage: (data) => api.post('/api/routes/villages', data),
};

// ==================== PRODUCT API ====================
export const productAPI = {
  // Get all products
  getAll: () => api.get('/api/products'),
};

// ==================== EXPENSE API ====================
export const expenseAPI = {
  // Get expense by salesman and date
  getByDate: (alias, date) => 
    api.get(`/api/daily-expenses/salesman-date?alias=${alias}&date=${date}`),
  
  // Submit sales with expenses
  submitSalesWithExpense: (data) => api.post('/api/sales/sales-expense', data),
};

// ==================== SUMMARY API ====================
export const summaryAPI = {
  // Submit summary
  submit: (data) => api.post('/api/summary/submit', data),
};

const isRetryableWarehouseRouteError = (error) => {
  const status = error?.response?.status;
  return status === 404 || status === 405 || status === 501;
};

const callWarehouseWithFallback = async (requestFns) => {
  let lastError;

  for (const requestFn of requestFns) {
    try {
      return await requestFn();
    } catch (error) {
      lastError = error;
      if (!isRetryableWarehouseRouteError(error)) {
        throw error;
      }
    }
  }

  throw lastError;
};

// ==================== WAREHOUSE API ====================
export const warehouseAPI = {
  // Get all inventory
  getAllInventory: () =>
    callWarehouseWithFallback([
      () => api.get('/api/warehouse/inventory'),
      () => api.get('/api/v1/warehouse/inventory'),
      () => api.get('/api/v1/admin/warehouse/inventory'),
    ]),

  // Get inventory by product code
  getInventoryByProductCode: (productCode) =>
    callWarehouseWithFallback([
      () => api.get(`/api/warehouse/inventory/${productCode}`),
      () => api.get(`/api/v1/warehouse/inventory/${productCode}`),
      () => api.get(`/api/v1/admin/warehouse/inventory/${productCode}`),
    ]),

  // Issue stock to salesman
  issueStock: (data) =>
    callWarehouseWithFallback([
      () => api.post('/api/warehouse/issue', data),
      () => api.post('/api/v1/warehouse/issue', data),
      () => api.post('/api/v1/admin/warehouse/issue', data),
    ]),

  // Return stock from salesman
  returnStock: (data) =>
    callWarehouseWithFallback([
      () => api.post('/api/warehouse/return', data),
      () => api.post('/api/v1/warehouse/return', data),
      () => api.post('/api/v1/admin/warehouse/return', data),
    ]),

  // Adjust stock (TRANSFER_IN, DAMAGE, MANUAL_ADJUST)
  adjustStock: (data) =>
    callWarehouseWithFallback([
      () => api.post('/api/warehouse/adjust', data),
      () => api.post('/api/v1/warehouse/adjust', data),
      () => api.post('/api/v1/admin/warehouse/adjust', data),
    ]),

  // Get all salesmen with their stock breakdown
  getSalesmenStockSummary: () =>
    callWarehouseWithFallback([
      () => api.get('/api/warehouse/salesmen-stock-summary'),
      () => api.get('/api/v1/warehouse/salesmen-stock-summary'),
      () => api.get('/api/v1/admin/warehouse/salesmen-stock-summary'),
    ]),

  // Get all ledger entries
  getAllLedger: () =>
    callWarehouseWithFallback([
      () => api.get('/api/warehouse/ledger'),
      () => api.get('/api/v1/warehouse/ledger'),
      () => api.get('/api/v1/admin/warehouse/ledger'),
    ]),

  // Get ledger by product
  getLedgerByProduct: (productCode) =>
    callWarehouseWithFallback([
      () => api.get(`/api/warehouse/ledger/product/${productCode}`),
      () => api.get(`/api/v1/warehouse/ledger/product/${productCode}`),
      () => api.get(`/api/v1/admin/warehouse/ledger/product/${productCode}`),
    ]),

  // Get ledger by salesman
  getLedgerBySalesman: (salesmanAlias) =>
    callWarehouseWithFallback([
      () => api.get(`/api/warehouse/ledger/salesman/${salesmanAlias}`),
      () => api.get(`/api/v1/warehouse/ledger/salesman/${salesmanAlias}`),
      () => api.get(`/api/v1/admin/warehouse/ledger/salesman/${salesmanAlias}`),
    ]),

  // Get stock with salesman
  getStockWithSalesman: (salesmanAlias, productCode = null) => {
    const query = productCode ? `?productCode=${encodeURIComponent(productCode)}` : '';

    return callWarehouseWithFallback([
      () => api.get(`/api/warehouse/salesman/${salesmanAlias}/stock${query}`),
      () => api.get(`/api/v1/warehouse/salesman/${salesmanAlias}/stock${query}`),
      () => api.get(`/api/v1/admin/warehouse/salesman/${salesmanAlias}/stock${query}`),
    ]);
  },
};

export default api;
