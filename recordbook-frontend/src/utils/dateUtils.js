/**
 * Utility functions for consistent date formatting across the application
 * Format: yyyy-mm-dd (ISO 8601)
 */

/**
 * Get today's date in yyyy-mm-dd format
 */
export const getTodayDate = () => {
  return new Date().toISOString().split('T')[0];
};

/**
 * Get current month in yyyy-mm format
 */
export const getCurrentMonth = () => {
  return new Date().toISOString().substring(0, 7);
};

/**
 * Convert DDMMYYYY format to yyyy-mm-dd format
 * @param {string} dateStr - Date string in DDMMYYYY format
 * @returns {string} Date in yyyy-mm-dd format
 */
export const convertFromDDMMYYYY = (dateStr) => {
  if (dateStr && dateStr.length === 8) {
    const day = dateStr.slice(0, 2);
    const month = dateStr.slice(2, 4);
    const year = dateStr.slice(4);
    return `${year}-${month}-${day}`;
  }
  return dateStr;
};

/**
 * Convert yyyy-mm-dd to DDMMYYYY format
 * @param {string} dateStr - Date string in yyyy-mm-dd format
 * @returns {string} Date in DDMMYYYY format
 */
export const convertToDDMMYYYY = (dateStr) => {
  if (dateStr && dateStr.length === 10) {
    const parts = dateStr.split('-');
    return `${parts[2]}${parts[1]}${parts[0]}`;
  }
  return dateStr;
};

/**
 * Format date for display (yyyy-mm-dd)
 * This ensures consistent display across the application
 * @param {string} dateStr - Date string in any format
 * @returns {string} Formatted date in yyyy-mm-dd
 */
export const formatDateDisplay = (dateStr) => {
  if (!dateStr) return '';
  
  // Already in correct format
  if (dateStr.match(/^\d{4}-\d{2}-\d{2}$/)) {
    return dateStr;
  }
  
  // DDMMYYYY format
  if (dateStr.length === 8 && !isNaN(dateStr)) {
    return convertFromDDMMYYYY(dateStr);
  }
  
  return dateStr;
};

/**
 * Validate if date string is in yyyy-mm-dd format
 * @param {string} dateStr - Date string to validate
 * @returns {boolean} True if valid yyyy-mm-dd format
 */
export const isValidYYYYMMDD = (dateStr) => {
  return /^\d{4}-\d{2}-\d{2}$/.test(dateStr);
};

export default {
  getTodayDate,
  getCurrentMonth,
  convertFromDDMMYYYY,
  convertToDDMMYYYY,
  formatDateDisplay,
  isValidYYYYMMDD
};
