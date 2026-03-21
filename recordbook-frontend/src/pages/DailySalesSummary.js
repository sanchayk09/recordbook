import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { salesAPI, salesmanAPI } from '../api';
import { notifyError, notifySuccess } from '../utils/toast';
import { getTodayDate, getCurrentMonth } from '../utils/dateUtils';
import { cacheUtils } from '../utils/cacheUtils';
import '../styles/DailySalesSummary.css';

let salesSummaryLoadPromise = null;

const DailySalesSummary = () => {
  const [salesRecords, setSalesRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editingRecord, setEditingRecord] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editFormData, setEditFormData] = useState({});
  const [salesmen, setSalesmen] = useState([]);
  const [selectedDate, setSelectedDate] = useState(getTodayDate());
  const [filterType, setFilterType] = useState('today'); // 'today', 'date', 'week', 'month', 'range'
  const [startDate, setStartDate] = useState(getTodayDate());
  const [endDate, setEndDate] = useState(getTodayDate());
  const [selectedMonth, setSelectedMonth] = useState(getCurrentMonth()); // YYYY-MM format
  const [selectedProduct, setSelectedProduct] = useState(''); // Empty string means no product filter
  const [selectedCustomerName, setSelectedCustomerName] = useState(''); // Customer name filter
  const [selectedVillage, setSelectedVillage] = useState(''); // Village filter
  const [selectedSalesman, setSelectedSalesman] = useState(''); // Salesman filter
  const [showCustomerSuggestions, setShowCustomerSuggestions] = useState(false); // Show customer autocomplete
  const [showVillageSuggestions, setShowVillageSuggestions] = useState(false); // Show village autocomplete
  const [sortOrder, setSortOrder] = useState('asc'); // 'asc' or 'desc' for serial number sorting
  const navigate = useNavigate();

  // Reuse in-flight request to avoid duplicate initial calls in StrictMode
  const fetchSalesRecords = useCallback(async () => {
    if (!salesSummaryLoadPromise) {
      salesSummaryLoadPromise = (async () => {
        const salesResponse = await salesAPI.getAllSales();
        
        // Check cache first for aliases
        let aliases = cacheUtils.getSalesmenAliases();
        if (!aliases) {
          const salesmenResponse = await salesmanAPI.getAliases();
          aliases = Array.isArray(salesmenResponse.data) ? salesmenResponse.data : [];
          cacheUtils.setSalesmenAliases(aliases);
        }
        
        const records = Array.isArray(salesResponse.data) ? salesResponse.data : [];
        return { records, aliases };
      })()
        .finally(() => {
          salesSummaryLoadPromise = null;
        });
    }

    return salesSummaryLoadPromise;
  }, []);

  useEffect(() => {
    let isActive = true;

    const load = async () => {
      setLoading(true);
      try {
        const { records, aliases } = await fetchSalesRecords();
        if (!isActive) return;
        setSalesRecords(records);
        setSalesmen(aliases);
      } catch (error) {
        if (!isActive) return;
        console.error('Error loading data:', error);
        notifyError('Failed to load data: ' + (error.response?.data?.message || error.message));
        setSalesRecords([]);
      } finally {
        if (isActive) setLoading(false);
      }
    };

    load();

    return () => {
      isActive = false;
    };
  }, [fetchSalesRecords]);

  const uniqueProducts = useMemo(() => {
    const products = new Set(salesRecords.map(record => record.productCode).filter(Boolean));
    return Array.from(products).sort();
  }, [salesRecords]);

  const uniqueCustomerNames = useMemo(() => {
    const names = new Set(salesRecords.map(record => record.customerName).filter(Boolean));
    return Array.from(names).sort();
  }, [salesRecords]);

  const uniqueVillages = useMemo(() => {
    const villages = new Set(salesRecords.map(record => record.village).filter(Boolean));
    return Array.from(villages).sort();
  }, [salesRecords]);

  const uniqueSalesmen = useMemo(() => {
    const names = new Set(salesRecords.map(record => record.salesmanName).filter(Boolean));
    return Array.from(names).sort();
  }, [salesRecords]);

  const filteredCustomerSuggestions = useMemo(() => {
    if (!selectedCustomerName.trim()) return [];
    const lowerInput = selectedCustomerName.toLowerCase();
    return uniqueCustomerNames.filter(name =>
      name.toLowerCase().includes(lowerInput)
    );
  }, [selectedCustomerName, uniqueCustomerNames]);

  const filteredVillageSuggestions = useMemo(() => {
    if (!selectedVillage.trim()) return [];
    const lowerInput = selectedVillage.toLowerCase();
    return uniqueVillages.filter(village =>
      village.toLowerCase().includes(lowerInput)
    );
  }, [selectedVillage, uniqueVillages]);

  const getFilteredRecords = useCallback(() => {
    let filtered = salesRecords;

    // Apply date filter
    if (filterType === 'today') {
      const today = new Date().toISOString().split('T')[0];
      filtered = filtered.filter(record => record.saleDate === today);
    } else if (filterType === 'date') {
      filtered = filtered.filter(record => record.saleDate === selectedDate);
    } else if (filterType === 'week') {
      const today = new Date(selectedDate);
      const weekStart = new Date(today);
      weekStart.setDate(today.getDate() - today.getDay());
      const weekEnd = new Date(weekStart);
      weekEnd.setDate(weekStart.getDate() + 6);

      const startStr = weekStart.toISOString().split('T')[0];
      const endStr = weekEnd.toISOString().split('T')[0];
      filtered = filtered.filter(record => record.saleDate >= startStr && record.saleDate <= endStr);
    } else if (filterType === 'month') {
      filtered = filtered.filter(record => record.saleDate.startsWith(selectedMonth));
    } else if (filterType === 'range') {
      filtered = filtered.filter(record => record.saleDate >= startDate && record.saleDate <= endDate);
    } else if (filterType === 'last-7-days') {
      const today = new Date();
      const startDate = new Date(today);
      startDate.setDate(today.getDate() - 6);
      const startStr = startDate.toISOString().split('T')[0];
      const endStr = today.toISOString().split('T')[0];
      filtered = filtered.filter(record => record.saleDate >= startStr && record.saleDate <= endStr);
    } else if (filterType === 'last-15-days') {
      const today = new Date();
      const startDate = new Date(today);
      startDate.setDate(today.getDate() - 14);
      const startStr = startDate.toISOString().split('T')[0];
      const endStr = today.toISOString().split('T')[0];
      filtered = filtered.filter(record => record.saleDate >= startStr && record.saleDate <= endStr);
    } else if (filterType === 'last-30-days') {
      const today = new Date();
      const startDate = new Date(today);
      startDate.setDate(today.getDate() - 29);
      const startStr = startDate.toISOString().split('T')[0];
      const endStr = today.toISOString().split('T')[0];
      filtered = filtered.filter(record => record.saleDate >= startStr && record.saleDate <= endStr);
    } else if (filterType === 'last-90-days') {
      const today = new Date();
      const startDate = new Date(today);
      startDate.setDate(today.getDate() - 89);
      const startStr = startDate.toISOString().split('T')[0];
      const endStr = today.toISOString().split('T')[0];
      filtered = filtered.filter(record => record.saleDate >= startStr && record.saleDate <= endStr);
    }

    // Apply product filter
    if (selectedProduct) {
      filtered = filtered.filter(record => record.productCode === selectedProduct);
    }

    // Apply customer name filter
    if (selectedCustomerName) {
      filtered = filtered.filter(record =>
        record.customerName && record.customerName.toLowerCase().includes(selectedCustomerName.toLowerCase())
      );
    }

    // Apply village filter
    if (selectedVillage) {
      filtered = filtered.filter(record =>
        record.village && record.village.toLowerCase().includes(selectedVillage.toLowerCase())
      );
    }

    // Apply salesman filter
    if (selectedSalesman) {
      filtered = filtered.filter(record =>
        record.salesmanName && record.salesmanName.toLowerCase() === selectedSalesman.toLowerCase()
      );
    }

    return filtered;
  }, [salesRecords, filterType, selectedDate, startDate, endDate, selectedMonth, selectedProduct, selectedCustomerName, selectedVillage, selectedSalesman]);

  const filteredRecords = useMemo(() => {
    return getFilteredRecords();
  }, [getFilteredRecords]);

  const sortedRecords = useMemo(() => {
    const records = [...filteredRecords];
    return records.sort((a, b) => {
      const dateA = new Date(a.saleDate);
      const dateB = new Date(b.saleDate);
      return sortOrder === 'asc' ? dateA - dateB : dateB - dateA;
    });
  }, [filteredRecords, sortOrder]);

  const totals = useMemo(() => {
    return filteredRecords.reduce(
      (acc, record) => {
        acc.quantity += Number(record.quantity) || 0;
        acc.revenue += Number(record.revenue) || 0;
        acc.agentCommission += Number(record.agentCommission) || 0;
        acc.volumeSold += Number(record.volumeSold) || 0;
        return acc;
      },
      { quantity: 0, revenue: 0, agentCommission: 0, volumeSold: 0 }
    );
  }, [filteredRecords]);

  const handleEdit = (recordId) => {
    const record = salesRecords.find(r => r.id === recordId);
    if (record) {
      setEditingRecord(record);
      setEditFormData({
        slNo: record.slNo,
        saleDate: record.saleDate,
        salesmanName: record.salesmanName,
        customerName: record.customerName,
        customerType: record.customerType,
        village: record.village,
        mobileNumber: record.mobileNumber,
        productCode: record.productCode,
        quantity: record.quantity,
        rate: record.rate,
      });
      setShowEditModal(true);
    }
  };

  const handleDelete = (recordId) => {
    if (window.confirm('Are you sure you want to delete this sales record?')) {
      deleteRecord(recordId);
    }
  };

  const deleteRecord = async (recordId) => {
    try {
      await salesAPI.deleteSales(recordId);
      notifySuccess('Sales record deleted successfully');
      setSalesRecords(salesRecords.filter(record => record.id !== recordId));
    } catch (error) {
      console.error('Error deleting record:', error);
      notifyError('Failed to delete sales record: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleEditFormChange = (field, value) => {
    setEditFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSaveEdit = async () => {
    try {
      await salesAPI.updateSale(editingRecord.id, editFormData);
      notifySuccess('Sales record updated successfully');
      setSalesRecords(salesRecords.map(r => 
        r.id === editingRecord.id 
          ? { ...r, ...editFormData }
          : r
      ));
      setShowEditModal(false);
      setEditingRecord(null);
    } catch (error) {
      console.error('Error updating record:', error);
      notifyError('Failed to update sales record: ' + (error.response?.data?.message || error.message));
    }
  };

  return (
    <div className="dss-page">
      <div className="dss-header">
        <h2>Daily Sales Summary</h2>
        <button
          type="button"
          onClick={() => navigate('/daily-sales')}
          className="dss-back-button"
        >
          Back to Daily Sales
        </button>
      </div>

      {/* Date Filter */}
      <div className="dss-filter-section">
        <label className="dss-filter-label">Filter By:</label>

        <div className="dss-filter-buttons-group">
          <button
            onClick={() => setFilterType('today')}
            className={`dss-filter-button ${filterType === 'today' ? 'active' : ''}`}
          >
            Today
          </button>

          <button
            onClick={() => setFilterType('week')}
            className={`dss-filter-button ${filterType === 'week' ? 'active' : ''}`}
          >
            This Week
          </button>

          <button
            onClick={() => setFilterType('month')}
            className={`dss-filter-button ${filterType === 'month' ? 'active' : ''}`}
          >
            This Month
          </button>

          <button
            onClick={() => setFilterType('date')}
            className={`dss-filter-button ${filterType === 'date' ? 'active' : ''}`}
          >
            Specific Date
          </button>

          <div className="dss-dropdown-container">
            <select
              value={filterType.startsWith('last-') ? filterType : ''}
              onChange={(e) => e.target.value && setFilterType(e.target.value)}
              className={`dss-filter-button dss-dropdown-btn ${filterType.startsWith('last-') ? 'active' : ''}`}
            >
              <option value="">Last X Days ▼</option>
              <option value="last-7-days">Last 7 Days</option>
              <option value="last-15-days">Last 15 Days</option>
              <option value="last-30-days">Last 30 Days</option>
              <option value="last-90-days">Last 90 Days</option>
            </select>
          </div>

          <button
            onClick={() => setFilterType('range')}
            className={`dss-filter-button ${filterType === 'range' ? 'active' : ''}`}
          >
            Date Range
          </button>
        </div>

        {/* Filter input based on selected filter type */}
        <div className="dss-filter-inputs">
          {filterType === 'date' && (
            <>
              <label className="dss-filter-input-label">Date:</label>
              <input
                type="date"
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                className="dss-filter-input date-input"
              />
            </>
          )}

          {filterType === 'month' && (
            <>
              <label className="dss-filter-input-label">Month:</label>
              <input
                type="month"
                value={selectedMonth}
                onChange={(e) => setSelectedMonth(e.target.value)}
                className="dss-filter-input month-input"
              />
            </>
          )}

          {filterType === 'week' && (
            <>
              <label className="dss-filter-input-label">Week starting:</label>
              <input
                type="date"
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                className="dss-filter-input date-input"
              />
            </>
          )}

          {filterType === 'range' && (
            <>
              <label className="dss-filter-input-label">From:</label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="dss-filter-input date-input"
              />
              <label className="dss-filter-input-label">To:</label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="dss-filter-input date-input"
              />
            </>
          )}

          <label className="dss-filter-input-label" style={{ marginLeft: '20px' }}>Product:</label>
          <select
            value={selectedProduct}
            onChange={(e) => setSelectedProduct(e.target.value)}
            className="dss-filter-input select-input"
          >
            <option value="">All Products</option>
            {uniqueProducts.map((product, idx) => (
              <option key={idx} value={product}>
                {product}
              </option>
            ))}
          </select>

          <label className="dss-filter-input-label" style={{ marginLeft: '20px' }}>Customer Name:</label>
          <div style={{ position: 'relative' }}>
            <input
              type="text"
              value={selectedCustomerName}
              onChange={(e) => setSelectedCustomerName(e.target.value)}
              onFocus={() => setShowCustomerSuggestions(true)}
              onBlur={() => setTimeout(() => setShowCustomerSuggestions(false), 200)}
              placeholder="Type customer name..."
              className="dss-filter-input text-input"
              style={{ width: '200px' }}
            />
            {showCustomerSuggestions && selectedCustomerName && filteredCustomerSuggestions.length > 0 && (
              <div style={{
                position: 'absolute',
                top: '100%',
                left: 0,
                right: 0,
                backgroundColor: '#fff',
                border: '1px solid #ddd',
                borderTop: 'none',
                borderRadius: '0 0 4px 4px',
                maxHeight: '200px',
                overflowY: 'auto',
                zIndex: 1000,
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
              }}>
                {filteredCustomerSuggestions.map((customer, idx) => (
                  <div
                    key={idx}
                    onClick={() => {
                      setSelectedCustomerName(customer);
                      setShowCustomerSuggestions(false);
                    }}
                    style={{
                      padding: '10px 12px',
                      cursor: 'pointer',
                      borderBottom: '1px solid #f0f0f0',
                      backgroundColor: idx % 2 === 0 ? '#fafafa' : '#fff'
                    }}
                    onMouseEnter={(e) => e.target.style.backgroundColor = '#e8f5e9'}
                    onMouseLeave={(e) => e.target.style.backgroundColor = idx % 2 === 0 ? '#fafafa' : '#fff'}
                  >
                    {customer}
                  </div>
                ))}
              </div>
            )}
          </div>

          <label className="dss-filter-input-label" style={{ marginLeft: '20px' }}>Village:</label>
          <div style={{ position: 'relative' }}>
            <input
              type="text"
              value={selectedVillage}
              onChange={(e) => setSelectedVillage(e.target.value)}
              onFocus={() => setShowVillageSuggestions(true)}
              onBlur={() => setTimeout(() => setShowVillageSuggestions(false), 200)}
              placeholder="Type village name..."
              className="dss-filter-input text-input"
              style={{ width: '200px' }}
            />
            {showVillageSuggestions && selectedVillage && filteredVillageSuggestions.length > 0 && (
              <div style={{
                position: 'absolute',
                top: '100%',
                left: 0,
                right: 0,
                backgroundColor: '#fff',
                border: '1px solid #ddd',
                borderTop: 'none',
                borderRadius: '0 0 4px 4px',
                maxHeight: '200px',
                overflowY: 'auto',
                zIndex: 1000,
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
              }}>
                {filteredVillageSuggestions.map((village, idx) => (
                  <div
                    key={idx}
                    onClick={() => {
                      setSelectedVillage(village);
                      setShowVillageSuggestions(false);
                    }}
                    style={{
                      padding: '10px 12px',
                      cursor: 'pointer',
                      borderBottom: '1px solid #f0f0f0',
                      backgroundColor: idx % 2 === 0 ? '#fafafa' : '#fff'
                    }}
                    onMouseEnter={(e) => e.target.style.backgroundColor = '#e8f5e9'}
                    onMouseLeave={(e) => e.target.style.backgroundColor = idx % 2 === 0 ? '#fafafa' : '#fff'}
                  >
                    {village}
                  </div>
                ))}
              </div>
            )}
          </div>

          <label className="dss-filter-input-label" style={{ marginLeft: '20px' }}>Salesman:</label>
          <select
            value={selectedSalesman}
            onChange={(e) => setSelectedSalesman(e.target.value)}
            className="dss-filter-input select-input"
          >
            <option value="">All Salesmen</option>
            {uniqueSalesmen.map((salesman, idx) => (
              <option key={idx} value={salesman}>
                {salesman}
              </option>
            ))}
          </select>

          <span className="dss-filter-total">
            Total: <strong>{filteredRecords.length}</strong> record{filteredRecords.length !== 1 ? 's' : ''}
          </span>
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '40px', color: '#999' }}>
          Loading sales records...
        </div>
      ) : (
        <div style={{ overflowX: 'auto', border: '1px solid #e0e0e0', borderRadius: '8px' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '1200px' }}>
            <thead>
              <tr style={{ backgroundColor: '#66a37f', color: '#fff' }}>
                <th 
                  onClick={() => setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')}
                  style={{
                    ...thStyle,
                    cursor: 'pointer',
                    userSelect: 'none',
                    backgroundColor: '#0d5a1f',
                  }}
                  title="Click to sort by Sale Date"
                >
                  SL No {sortOrder === 'asc' ? '↑' : '↓'}
                </th>
                <th style={thStyle}>Sale Date</th>
                <th style={thStyle}>Salesman</th>
                <th style={thStyle}>Customer Name</th>
                <th style={thStyle}>Type</th>
                <th style={thStyle}>Village</th>
                <th style={thStyle}>Mobile</th>
                <th style={thStyle}>Product</th>
                <th style={thStyle}>Qty</th>
                <th style={thStyle}>Rate</th>
                <th style={thStyle}>Volume Sold</th>
                <th style={thStyle}>Revenue</th>
                <th style={thStyle}>Agent Commission</th>
                <th style={thStyle}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {sortedRecords.length === 0 ? (
                <tr>
                  <td colSpan="13" style={{ ...tdStyle, textAlign: 'center', padding: '40px' }}>
                    No sales records found.
                  </td>
                </tr>
              ) : (
                sortedRecords.map((record, index) => (
                  <tr key={record.id || index} style={{ backgroundColor: index % 2 === 0 ? '#fff' : '#f7f9fc' }}>
                    <td style={tdStyle}>{index + 1}</td>
                    <td style={tdStyle}>{record.saleDate}</td>
                    <td style={tdStyle}>{record.salesmanName}</td>
                    <td style={{ ...tdStyle, textAlign: 'left' }}>{record.customerName}</td>
                    <td style={tdStyle}>{record.customerType}</td>
                    <td style={tdStyle}>{record.village}</td>
                    <td style={tdStyle}>{record.mobileNumber}</td>
                    <td style={tdStyle}>{record.productCode}</td>
                    <td style={tdStyle}>{record.quantity}</td>
                    <td style={tdStyle}>{record.rate}</td>
                    <td style={tdStyle}>{record.volumeSold ?? ''}</td>
                    <td style={tdStyle}>{record.revenue}</td>
                    <td style={tdStyle}>{record.agentCommission || 0}</td>
                    <td style={tdStyle}>
                      <button
                        onClick={() => handleEdit(record.id)}
                        style={{
                          backgroundColor: '#16a34a',
                          color: '#fff',
                          border: 'none',
                          padding: '4px 8px',
                          marginRight: '4px',
                          borderRadius: '3px',
                          cursor: 'pointer',
                          fontSize: '11px',
                          fontWeight: 'bold',
                        }}
                      >
                        ✎ Edit
                      </button>
                      <button
                        onClick={() => handleDelete(record.id)}
                        style={{
                          backgroundColor: '#dc3545',
                          color: '#fff',
                          border: 'none',
                          padding: '4px 8px',
                          borderRadius: '3px',
                          cursor: 'pointer',
                          fontSize: '11px',
                          fontWeight: 'bold',
                        }}
                      >
                        🗑 Delete
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
            {filteredRecords.length > 0 && (
              <tfoot>
                <tr style={{ backgroundColor: '#66a37f', color: '#fff', fontWeight: 'bold' }}>
                  {/* SL No */}
                  <td style={{ ...tdStyle }}></td>
                  {/* Sale Date */}
                  <td style={{ ...tdStyle }}></td>
                  {/* Salesman */}
                  <td style={{ ...tdStyle }}></td>
                  {/* Customer Name */}
                  <td style={{ ...tdStyle }}></td>
                  {/* Type */}
                  <td style={{ ...tdStyle }}></td>
                  {/* Village */}
                  <td style={{ ...tdStyle }}></td>
                  {/* Mobile */}
                  <td style={{ ...tdStyle }}></td>
                  {/* Product */}
                  <td style={{ ...tdStyle, textAlign: 'right', fontWeight: 'bold' }}>Total:</td>
                  {/* Quantity */}
                  <td style={{ ...tdStyle, fontWeight: 'bold' }}>{totals.quantity}</td>
                  {/* Rate (no total) */}
                  <td style={{ ...tdStyle }}></td>
                  {/* Volume Sold */}
                  <td style={{ ...tdStyle, fontWeight: 'bold' }}>{totals.volumeSold}</td>
                  {/* Revenue */}
                  <td style={{ ...tdStyle, fontWeight: 'bold' }}>{totals.revenue.toFixed(2)}</td>
                  {/* Agent Commission */}
                  <td style={{ ...tdStyle, fontWeight: 'bold' }}>{totals.agentCommission.toFixed(2)}</td>
                  {/* Actions */}
                  <td style={{ ...tdStyle }}></td>
                </tr>
              </tfoot>
            )}
          </table>
        </div>
      )}

      {/* Edit Modal */}
      {showEditModal && editingRecord && (
        <div style={modalOverlayStyle}>
          <div style={modalContentStyle}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ margin: 0 }}>Edit Sales Record</h3>
              <button
                onClick={() => setShowEditModal(false)}
                style={{ background: 'none', border: 'none', fontSize: '24px', cursor: 'pointer' }}
              >
                ✕
              </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', marginBottom: '20px' }}>
              <div>
                <label style={labelStyle}>Sale Date</label>
                <input
                  type="date"
                  value={editFormData.saleDate}
                  onChange={(e) => handleEditFormChange('saleDate', e.target.value)}
                  style={inputStyle}
                />
              </div>

              <div>
                <label style={labelStyle}>Salesman</label>
                <select
                  value={editFormData.salesmanName}
                  onChange={(e) => handleEditFormChange('salesmanName', e.target.value)}
                  style={inputStyle}
                >
                  <option value="">Select Salesman</option>
                  {salesmen.map((alias, idx) => (
                    <option key={idx} value={alias}>
                      {alias}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label style={labelStyle}>Customer Name</label>
                <input
                  type="text"
                  value={editFormData.customerName}
                  onChange={(e) => handleEditFormChange('customerName', e.target.value)}
                  style={inputStyle}
                />
              </div>

              <div>
                <label style={labelStyle}>Type</label>
                <select
                  value={editFormData.customerType}
                  onChange={(e) => handleEditFormChange('customerType', e.target.value)}
                  style={inputStyle}
                >
                  <option value="">Select Type</option>
                  <option value="SHOPKEEPER">Shopkeeper</option>
                  <option value="CUSTOMER">Customer</option>
                </select>
              </div>

              <div>
                <label style={labelStyle}>Village</label>
                <input
                  type="text"
                  value={editFormData.village}
                  onChange={(e) => handleEditFormChange('village', e.target.value)}
                  style={inputStyle}
                />
              </div>

              <div>
                <label style={labelStyle}>Mobile</label>
                <input
                  type="text"
                  value={editFormData.mobileNumber}
                  onChange={(e) => handleEditFormChange('mobileNumber', e.target.value)}
                  style={inputStyle}
                />
              </div>

              <div>
                <label style={labelStyle}>Product</label>
                <input
                  type="text"
                  value={editFormData.productCode}
                  onChange={(e) => handleEditFormChange('productCode', e.target.value)}
                  style={inputStyle}
                  placeholder="Enter product code"
                />
              </div>

              <div>
                <label style={labelStyle}>Quantity</label>
                <input
                  type="number"
                  value={editFormData.quantity}
                  onChange={(e) => handleEditFormChange('quantity', e.target.value)}
                  style={inputStyle}
                />
              </div>

              <div>
                <label style={labelStyle}>Rate</label>
                <input
                  type="number"
                  step="0.01"
                  value={editFormData.rate}
                  onChange={(e) => handleEditFormChange('rate', e.target.value)}
                  style={inputStyle}
                />
              </div>
            </div>

            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
              <button
                onClick={() => setShowEditModal(false)}
                style={{
                  backgroundColor: '#6c757d',
                  color: '#fff',
                  border: 'none',
                  padding: '10px 20px',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontWeight: 'bold',
                }}
              >
                Cancel
              </button>
              <button
                onClick={handleSaveEdit}
                style={{
                  backgroundColor: '#28a745',
                  color: '#fff',
                  border: 'none',
                  padding: '10px 20px',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontWeight: 'bold',
                }}
              >
                Save Changes
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

const thStyle = {
  padding: '10px 8px',
  border: '1px solid #d0d0d0',
  fontWeight: 'bold',
  textAlign: 'center',
  whiteSpace: 'nowrap',
};

const tdStyle = {
  padding: '8px',
  border: '1px solid #e0e0e0',
  textAlign: 'center',
  fontSize: '14px',
};

const modalOverlayStyle = {
  position: 'fixed',
  top: 0,
  left: 0,
  right: 0,
  bottom: 0,
  backgroundColor: 'rgba(0, 0, 0, 0.5)',
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  zIndex: 1000,
};

const modalContentStyle = {
  backgroundColor: '#fff',
  padding: '30px',
  borderRadius: '8px',
  boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
  maxWidth: '700px',
  width: '90%',
  maxHeight: '90vh',
  overflowY: 'auto',
};

const labelStyle = {
  display: 'block',
  marginBottom: '5px',
  fontWeight: 'bold',
  color: '#333',
};

const inputStyle = {
  width: '100%',
  padding: '10px',
  border: '1px solid #ddd',
  borderRadius: '4px',
  fontSize: '14px',
  fontFamily: 'Calibri, sans-serif',
  boxSizing: 'border-box',
};

export default DailySalesSummary;
