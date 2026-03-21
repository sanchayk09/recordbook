import axios from 'axios';
import { notifyError } from './utils/toast';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor (could add auth headers here)
api.interceptors.request.use(
  (config) => {
    console.log('API Request:', config.method?.toUpperCase(), config.url);
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for centralized error handling
api.interceptors.response.use(
  (response) => {
    console.log('API Response:', response.config.url, '- Status:', response.status);
    return response;
  },
  (error) => {
    console.error('API Error:', error);
    
    let message = 'Network error';
    
    if (error.response) {
      // Server responded with error status
      message = error.response.data?.message || error.response.statusText || `Error ${error.response.status}`;
      console.error('Response Error:', error.response.status, message);
      
      // Don't show toast for 500 errors - let components handle them with specific messages
      if (error.response.status !== 500) {
        try {
          notifyError(message);
        } catch (e) {
          console.error('Toast notification failed:', e);
        }
      }
    } else if (error.request) {
      // Request made but no response received
      message = 'No response from server. Please check if the backend is running on port 8080.';
      console.error('Request Error:', 'No response received');
      try {
        notifyError(message);
      } catch (e) {
        console.error('Toast notification failed:', e);
      }
    } else {
      // Something else happened
      message = error.message || 'Request failed';
      console.error('Error:', message);
      try {
        notifyError(message);
      } catch (e) {
        console.error('Toast notification failed:', e);
      }
    }
    
    return Promise.reject(error);
  }
);

export default api;

// Route API
export const routeAPI = {
  getAll: () => api.get('/api/routes'),
  getById: (id) => api.get(`/api/routes/${id}`),
  create: (data) => api.post('/api/routes', data),
  update: (id, data) => api.put(`/api/routes/${id}`, data),
  delete: (id) => api.delete(`/api/routes/${id}`),
  getAllVillages: () => api.get('/api/routes/villages'),
  getVillageById: (id) => api.get(`/api/routes/villages/${id}`),
  getVillages: (routeId) => api.get('/api/routes/villages').then(res => ({ data: Array.isArray(res.data) ? res.data.filter(v => v.routeId === routeId) : [] })), // Client-side filter
  createVillage: (data) => api.post('/api/routes/villages', data),
  addVillage: (data) => api.post('/api/routes/villages', data), // alias for createVillage
  updateVillage: (id, data) => api.put(`/api/routes/villages/${id}`, data),
  deleteVillage: (id) => api.delete(`/api/routes/villages/${id}`),
};

// Salesman API
export const salesmanAPI = {
  getAll: () => api.get('/api/admin/salesmen'),
  getAliases: () => api.get('/api/admin/salesmen/aliases'),
  create: (data) => api.post('/api/admin/salesmen', data),
};

// Customer API
export const customerAPI = {
  getAll: () => api.get('/api/admin/customers'),
  create: (data) => api.post('/api/admin/customers', data),
};

export const maintenanceAPI = {
  copyDatabase: (data) => api.post('/api/maintenance/database/copy', data),
};

// Product API
export const productAPI = {
  getAll: () => api.get('/api/admin/products'),
};

// Sales API
export const salesAPI = {
  createSales: (data) => api.post('/api/sales', data),
  getAllSales: () => api.get('/api/sales'),
  getSaleById: (id) => api.get(`/api/sales/${id}`),
  updateSale: (id, data) => api.put(`/api/sales/${id}`, data),
  deleteSales: (id) => api.delete(`/api/sales/${id}`),
  getSalesBySalesman: (alias) => api.get(`/api/sales/salesman/${alias}`),
  getSalesByDate: (date) => api.get(`/api/sales/date?date=${date}`),
  getSalesBySalesmanDate: (salesman, date) => api.get(`/api/sales/salesman-date?salesman=${encodeURIComponent(salesman)}&date=${date}`),
  getSalesBySalesmanRange: (salesman, startDate, endDate) => api.get(`/api/sales/salesman-range?salesman=${encodeURIComponent(salesman)}&startDate=${startDate}&endDate=${endDate}`),
  getSalesByRange: (startDate, endDate) => api.get(`/api/sales/range?startDate=${startDate}&endDate=${endDate}`),
  getLast7Days: () => api.get('/api/sales/filter/last-7-days'),
  getLast15Days: () => api.get('/api/sales/filter/last-15-days'),
  getLast30Days: () => api.get('/api/sales/filter/last-30-days'),
  getLast90Days: () => api.get('/api/sales/filter/last-90-days'),
  refreshDailySalesRecords: (startDate, endDate) => api.post('/api/sales/refresh', { startDate, endDate }),
  createSaleWithExpenses: (data) => api.post('/api/sales/sales-expense', data),
  getDailySalesDump: () => api.get('/api/sales/dump'),
};

// Product Sales Summary API - UNIFIED ENDPOINT
// All methods now use the same endpoint with different parameters
export const productSalesSummaryAPI = {
  // All-time summary (no params)
  getAllProductSales: () => api.get('/api/sales/summary/product-sales'),

  // Date range (custom)
  getProductSalesByRange: (startDate, endDate) =>
    api.get(`/api/sales/summary/product-sales?startDate=${startDate}&endDate=${endDate}`),

  // Specific date (same start and end date)
  getProductSalesByDate: (date) =>
    api.get(`/api/sales/summary/product-sales?startDate=${date}&endDate=${date}`),

  // Monthly
  getProductSalesByMonth: (year, month) =>
    api.get(`/api/sales/summary/product-sales?year=${year}&month=${month}`),

  // Convenience periods
  getTodayProductSales: () =>
    api.get('/api/sales/summary/product-sales?period=today'),

  getLast7DaysProductSales: () =>
    api.get('/api/sales/summary/product-sales?period=last7days'),

  getLast15DaysProductSales: () =>
    api.get('/api/sales/summary/product-sales?period=last15days'),

  getLast30DaysProductSales: () =>
    api.get('/api/sales/summary/product-sales?period=last30days'),

  getLast90DaysProductSales: () =>
    api.get('/api/sales/summary/product-sales?period=last90days'),

  getCurrentMonthProductSales: () =>
    api.get('/api/sales/summary/product-sales?period=currentMonth'),

  // Flexible method - pass any combination of parameters
  getProductSales: (params = {}) => {
    const queryParams = new URLSearchParams();
    if (params.startDate && params.endDate) {
      queryParams.append('startDate', params.startDate);
      queryParams.append('endDate', params.endDate);
    } else if (params.period) {
      queryParams.append('period', params.period);
    } else if (params.year && params.month) {
      queryParams.append('year', params.year);
      queryParams.append('month', params.month);
    }
    const query = queryParams.toString();
    return api.get(`/api/sales/summary/product-sales${query ? '?' + query : ''}`);
  },
};

// Expense API
export const expenseAPI = {
  getByDate: (salesman, date) => api.get(`/api/daily-expenses/salesman-date?alias=${encodeURIComponent(salesman)}&date=${date}`),
  submitSalesOnly: (data) => api.post('/api/sales/sales-expense', data), // Sales only, no expenses
  submitExpensesOnly: (data) => api.post('/api/sales/expenses', data), // Expenses only
  getAll: () => api.get('/api/daily-expenses'),
  getBySalesman: (salesman) => api.get(`/api/daily-expenses/salesman?alias=${encodeURIComponent(salesman)}`),
  getByDateOnly: (date) => api.get(`/api/daily-expenses/date?date=${date}`),
  getBySalesmanRange: (salesman, startDate, endDate) => api.get(`/api/daily-expenses/salesman-range?alias=${encodeURIComponent(salesman)}&startDate=${startDate}&endDate=${endDate}`),
  getByRange: (startDate, endDate) => api.get(`/api/daily-expenses/range?startDate=${startDate}&endDate=${endDate}`),
  create: (data) => api.post('/api/daily-expenses', data),
  update: (id, data) => api.put(`/api/daily-expenses/${id}`, data),
  delete: (id) => api.delete(`/api/daily-expenses/${id}`),
  // Upsert expense and recalculate summary in one call
  upsertAndRecalculate: (salesmanAlias, expenseDate, totalExpense) =>
    api.post('/api/daily-expenses/upsert', { salesmanAlias, expenseDate, totalExpense }),
};

// Warehouse API
export const warehouseAPI = {
  getAllInventory: () => api.get('/api/warehouse/inventory'),
  getAllLedger: () => api.get('/api/warehouse/ledger'),
  getSalesmenStockSummary: () => api.get('/api/warehouse/salesmen-stock-summary'),
  getSalesmanStock: (salesmanAlias) => api.get(`/api/warehouse/salesman/${salesmanAlias}/stock`),
  getInventoryByProduct: (productCode) => api.get(`/api/warehouse/inventory/${productCode}`),
  getLedgerBySalesman: (salesmanAlias) => api.get(`/api/warehouse/ledger/salesman/${salesmanAlias}`),
  issueStock: (data) => api.post('/api/warehouse/issue', data),
  returnStock: (data) => api.post('/api/warehouse/return', data),
  adjustStock: (data) => api.post('/api/warehouse/adjust', data),
  batchIssueStock: (data) => api.post('/api/warehouse/batch-issue', data),
  batchReturnStock: (data) => api.post('/api/warehouse/batch-return', data),
  returnAllStock: (salesmanAlias, createdBy) => api.post(`/api/warehouse/return-all/${salesmanAlias}?createdBy=${createdBy}`),
};

// Product Cost API
export const productCostAPI = {
  getAll: () => api.get('/api/product-cost/all'),
  create: (data) => api.post('/api/product-cost/add', data),
  add: (data) => api.post('/api/product-cost/add', data), // alias for create
  update: (pid, data) => api.put(`/api/product-cost/update/${pid}`, data),
  delete: (pid) => api.delete(`/api/product-cost/${pid}`),
  getByCode: (code) => api.get(`/api/product-cost/by-code/${code}`),
  getByName: (name) => api.get(`/api/product-cost/by-name/${name}`),
  exists: (productCode) => api.get(`/api/product-cost/exists/${productCode}`),
  checkCodeExists: (productCode) => api.get(`/api/product-cost/exists/${productCode}`).then(res => ({ data: { exists: res.data } })), // alias for exists
};

// Summary API - UNIFIED ENDPOINT
// All query methods now use the same endpoint with different parameters
export const summaryAPI = {
  // POST - still separate (different HTTP method)
  submit: (data) => api.post('/api/summary/submit', data),

  // All summaries (no params)
  getAll: () => api.get('/api/summary'),

  // Single summary (alias + date) - returns single object
  getBySalesmanDate: (alias, date) =>
    api.get(`/api/summary?alias=${encodeURIComponent(alias)}&date=${date}`),

  // Specific salesman (all dates)
  getBySalesman: (alias) =>
    api.get(`/api/summary?alias=${encodeURIComponent(alias)}`),

  // Specific date (all salesmen)
  getByDate: (date) =>
    api.get(`/api/summary?date=${date}`),

  // Salesman in date range
  getBySalesmanRange: (alias, startDate, endDate) =>
    api.get(`/api/summary?alias=${encodeURIComponent(alias)}&startDate=${startDate}&endDate=${endDate}`),

  // All salesmen in date range
  getByRange: (startDate, endDate) =>
    api.get(`/api/summary?startDate=${startDate}&endDate=${endDate}`),

  // Flexible method - pass any combination of parameters
  get: (params = {}) => {
    const queryParams = new URLSearchParams();
    if (params.alias) queryParams.append('alias', params.alias);
    if (params.date) queryParams.append('date', params.date);
    if (params.startDate) queryParams.append('startDate', params.startDate);
    if (params.endDate) queryParams.append('endDate', params.endDate);
    const query = queryParams.toString();
    return api.get(`/api/summary${query ? '?' + query : ''}`);
  },
};
