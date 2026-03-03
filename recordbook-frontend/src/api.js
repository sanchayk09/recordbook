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
  getAll: () => api.get('/api/admin/routes'),
  getVillages: (routeId) => api.get(`/api/admin/routes/${routeId}/villages`),
  create: (data) => api.post('/api/admin/routes', data),
  addVillage: (data) => api.post('/api/admin/route-villages', data),
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
  createSales: (data) => api.post('/api/admin/sales', data),
  getAllSales: () => api.get('/api/admin/sales'),
  deleteSales: (id) => api.delete(`/api/admin/sales/${id}`),
};

// Product Sales Summary API
export const productSalesSummaryAPI = {
  getTodayProductSales: () => api.get('/api/sales/summary/product/today'),
  getProductSalesByDate: (date) => api.get(`/api/sales/summary/product/date/${date}`),
  getProductSalesByMonth: (year, month) => api.get(`/api/sales/summary/product/month/${year}/${month}`),
  getProductSalesByRange: (startDate, endDate) => api.get(`/api/sales/summary/product/range/${startDate}/${endDate}`),
  getAllProductSales: () => api.get('/api/sales/summary/product/all'),
};

// Expense API
export const expenseAPI = {
  getByDate: (salesman, date) => api.get(`/api/admin/expenses/${salesman}/${date}`),
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
  update: (pid, data) => api.put(`/api/product-cost/update/${pid}`, data),
  delete: (pid) => api.delete(`/api/product-cost/delete/${pid}`),
  getByCode: (code) => api.get(`/api/product-cost/by-code/${code}`),
  getByName: (name) => api.get(`/api/product-cost/by-name/${name}`),
  exists: (productCode) => api.get(`/api/product-cost/exists/${productCode}`),
};

// Summary API
export const summaryAPI = {
  submit: (data) => api.post('/api/summary', data),
};
