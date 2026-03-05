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
  getVillages: (routeId) => api.get('/api/routes/villages').then(res => ({ data: Array.isArray(res.data) ? res.data.filter(v => v.routeId == routeId) : [] })), // Client-side filter
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
  createSaleWithExpenses: (data) => api.post('/api/sales/sales-expense', data),
  getDailySalesDump: () => api.get('/api/sales/dump'),
  getProductSalesSummary: () => api.get('/api/sales/summary/product-sales'),
  getTodayProductSalesSummary: () => api.get('/api/sales/summary/product-sales/today'),
};

// Product Sales Summary API
export const productSalesSummaryAPI = {
  getTodayProductSales: () => api.get('/api/sales/summary/product-sales/today'),
  getProductSalesByDate: (date) => api.get(`/api/sales/summary/product-sales/date?date=${date}`),
  getProductSalesByMonth: (year, month) => api.get(`/api/sales/summary/product-sales/month?year=${year}&month=${month}`),
  getProductSalesByRange: (startDate, endDate) => api.get(`/api/sales/summary/product-sales/range?startDate=${startDate}&endDate=${endDate}`),
  getAllProductSales: () => api.get('/api/sales/summary/product-sales'),
  getLast7DaysProductSales: () => api.get('/api/sales/summary/product-sales/last-7-days'),
  getLast15DaysProductSales: () => api.get('/api/sales/summary/product-sales/last-15-days'),
  getLast30DaysProductSales: () => api.get('/api/sales/summary/product-sales/last-30-days'),
  getLast90DaysProductSales: () => api.get('/api/sales/summary/product-sales/last-90-days'),
};

// Expense API
export const expenseAPI = {
  getByDate: (salesman, date) => api.get(`/api/daily-expenses/salesman-date?salesman=${encodeURIComponent(salesman)}&date=${date}`),
  submitSalesWithExpense: (data) => api.post('/api/sales/sales-expense', data),
  getAll: () => api.get('/api/daily-expenses'),
  getBySalesman: (salesman) => api.get(`/api/daily-expenses/salesman?salesman=${encodeURIComponent(salesman)}`),
  getByDateOnly: (date) => api.get(`/api/daily-expenses/date?date=${date}`),
  getBySalesmanRange: (salesman, startDate, endDate) => api.get(`/api/daily-expenses/salesman-range?salesman=${encodeURIComponent(salesman)}&startDate=${startDate}&endDate=${endDate}`),
  getByRange: (startDate, endDate) => api.get(`/api/daily-expenses/range?startDate=${startDate}&endDate=${endDate}`),
  create: (data) => api.post('/api/daily-expenses', data),
  update: (id, data) => api.put(`/api/daily-expenses/${id}`, data),
  delete: (id) => api.delete(`/api/daily-expenses/${id}`),
};

// Warehouse API
export const warehouseAPI = {
  getAllInventory: () => api.get('/api/warehouse/inventory'),
  getAllLedger: () => api.get('/api/warehouse/ledger'),
  getSalesmenStockSummary: () => api.get('/api/warehouse/salesmen-stock-summary'),
  getSalesmanStock: (salesmanAlias) => api.get(`/api/warehouse/salesman/${salesmanAlias}/stock`),
  getSalesmanStockByProduct: (salesmanAlias, productCode) => api.get(`/api/warehouse/salesman/${salesmanAlias}/stock?productCode=${encodeURIComponent(productCode)}`),
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

// Summary API
export const summaryAPI = {
  submit: (data) => api.post('/api/summary/submit', data),
  getBySalesmanDate: (alias, date) => api.get(`/api/summary/by-salesman-date?alias=${encodeURIComponent(alias)}&date=${date}`),
  getAll: () => api.get('/api/summary/all'),
  getBySalesman: (alias) => api.get(`/api/summary/salesman?alias=${encodeURIComponent(alias)}`),
  getByDate: (date) => api.get(`/api/summary/date?date=${date}`),
  getBySalesmanRange: (alias, startDate, endDate) => api.get(`/api/summary/salesman-range?alias=${encodeURIComponent(alias)}&startDate=${startDate}&endDate=${endDate}`),
  getByRange: (startDate, endDate) => api.get(`/api/summary/range?startDate=${startDate}&endDate=${endDate}`),
};
