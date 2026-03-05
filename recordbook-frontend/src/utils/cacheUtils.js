/**
 * Simple in-memory cache for frequently-accessed data
 * Cache is valid for the session; clears on page reload
 */

let cache = {
  salesmenAliases: null,
  lastFetchTime: {},
};

// Cache TTL in milliseconds (5 minutes)
const CACHE_TTL = 5 * 60 * 1000;

export const cacheUtils = {
  /**
   * Get cached salesmen aliases if valid, otherwise null
   */
  getSalesmenAliases: () => {
    const now = Date.now();
    const lastFetch = cache.lastFetchTime.salesmenAliases || 0;
    
    // Return cached data if it exists and is still fresh
    if (cache.salesmenAliases && (now - lastFetch) < CACHE_TTL) {
      return cache.salesmenAliases;
    }
    return null;
  },

  /**
   * Store salesmen aliases in cache
   */
  setSalesmenAliases: (aliases) => {
    cache.salesmenAliases = aliases;
    cache.lastFetchTime.salesmenAliases = Date.now();
  },

  /**
   * Clear salesmen aliases cache (call after adding/modifying salesmen)
   */
  clearSalesmenAliases: () => {
    cache.salesmenAliases = null;
    cache.lastFetchTime.salesmenAliases = 0;
  },

  /**
   * Clear all cached data
   */
  clearAll: () => {
    cache = {
      salesmenAliases: null,
      lastFetchTime: {},
    };
  },
};

export default cacheUtils;
