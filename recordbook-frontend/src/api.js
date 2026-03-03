import axios from 'axios';
import { notifyError } from './utils/toast';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080',
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

export default api;
