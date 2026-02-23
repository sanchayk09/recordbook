import React, { useState, useEffect } from 'react';
import api from '../api';
import '../styles/AdminDashboard.css';
import { notifySuccess, notifyError } from '../utils/toast';
import { filterSales, sortSales } from '../utils/salesUtils';
import { useRef } from 'react';

const AdminDashboard = () => {
  const [allSales, setAllSales] = useState([]);
  const [sales, setSales] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedVariant, setSelectedVariant] = useState('');
  const [selectedSize, setSelectedSize] = useState('');
  const [variants, setVariants] = useState([]);
  const [sizes, setSizes] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [sortBy, setSortBy] = useState('');
  const [sortOrder, setSortOrder] = useState('asc');
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    saleDate: new Date().toISOString().split('T')[0],
    salesman: '',
    location: '',
    customerName: '',
    mobileNumber: '',
    productName: 'Phenyl',
    variant: '',
    size: '',
    quantity: '',
    mfgCost: '',
    sellingPrice: '',
    dailyExpenseShare: ''
  });
  const [submitting, setSubmitting] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState({ show: false, id: null, deleting: false });

  useEffect(() => {
    fetchSales();
  }, []);

  const fetchSales = async () => {
    try {
      // basic validation
      if (formData.mobileNumber && !/^[0-9]{7,15}$/.test(formData.mobileNumber)) {
        notifyError('Please enter a valid mobile number');
        return;
      }
      if (formData.quantity && isNaN(Number(formData.quantity))) {
        notifyError('Quantity must be a number');
        return;
      }
      setLoading(true);
      const response = await api.get('/api/sales');
      setAllSales(response.data);
      setSales(response.data);
      
      // Extract unique variants and sizes
      const uniqueVariants = [...new Set(response.data.map(sale => sale.variant))].filter(Boolean);
      const uniqueSizes = [...new Set(response.data.map(sale => sale.size))].filter(Boolean);
      
      setVariants(uniqueVariants.sort());
      setSizes(uniqueSizes.sort());
      setLoading(false);
    } catch (err) {
      console.error("Error fetching data:", err);
      setError("Failed to load records");
      setLoading(false);
    }
  };

  const applyFilters = (variant = selectedVariant, size = selectedSize) => {
    const filtered = filterSales(allSales, { variant, size });
    setSales(filtered);
  };

  const handleVariantChange = (e) => {
    const value = e.target.value;
    setSelectedVariant(value);
    applyFilters(value, selectedSize);
  };

  const handleSizeChange = (e) => {
    const value = e.target.value;
    setSelectedSize(value);
    applyFilters(selectedVariant, value);
  };

  const resetFilters = () => {
    setSelectedVariant('');
    setSelectedSize('');
    setSales(allSales);
  };

  const handleSort = (type) => {
    let order = 'asc';
    if (sortBy === type && sortOrder === 'asc') {
      order = 'desc';
    }
    setSortBy(type);
    setSortOrder(order);

    const sortedData = sortSales(sales, type, order);
    setSales(sortedData);
  };

  const getSortIcon = (type) => {
    if (sortBy !== type) return ' ⇅';
    return sortOrder === 'asc' ? ' ↑' : ' ↓';
  };

  const modalOverlayRef = useRef(null);
  const modalContentRef = useRef(null);

  const openEditForm = (sale) => {
    setEditingId(sale.recordId);
    setFormData({
      saleDate: sale.saleDate,
      salesman: sale.salesman || '',
      location: sale.location || '',
      customerName: sale.customerName,
      mobileNumber: sale.mobileNumber || '',
      productName: sale.productName,
      variant: sale.variant,
      size: sale.size,
      quantity: sale.quantity,
      mfgCost: sale.mfgCost,
      sellingPrice: sale.sellingPrice,
      dailyExpenseShare: sale.dailyExpenseShare || ''
    });
    setShowForm(true);
  };

  const closeEditForm = () => {
    setEditingId(null);
    setFormData({
      saleDate: new Date().toISOString().split('T')[0],
      salesman: '',
      location: '',
      customerName: '',
      mobileNumber: '',
      productName: 'Phenyl',
      variant: '',
      size: '',
      quantity: '',
      mfgCost: '',
      sellingPrice: '',
      dailyExpenseShare: ''
    });
    setShowForm(false);
  };

  // Close on Escape and manage focus when modal opens
  useEffect(() => {
    const handleKey = (e) => {
      if (e.key === 'Escape' && showForm) closeEditForm();
    };
    if (showForm) {
      document.addEventListener('keydown', handleKey);
      // focus first input in modal
      setTimeout(() => {
        const firstInput = modalContentRef.current?.querySelector('input,select,button,textarea');
        firstInput?.focus();
      }, 0);
    }
    return () => document.removeEventListener('keydown', handleKey);
  }, [showForm]);

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validation
    if (!formData.saleDate || !formData.customerName || !formData.variant || !formData.size) {
      notifyError('Please fill in all required fields');
      return;
    }

    try {
      setSubmitting(true);
      const payload = {
        ...formData,
        quantity: parseInt(formData.quantity),
        mfgCost: parseFloat(formData.mfgCost),
        sellingPrice: parseFloat(formData.sellingPrice),
        dailyExpenseShare: parseFloat(formData.dailyExpenseShare)
      };

      if (editingId) {
        // Update existing record
        await api.put(`/api/sales/${editingId}`, payload);
        notifySuccess('Record updated successfully!');
      } else {
        // Create new record
        await api.post('/api/sales', payload);
        notifySuccess('Record added successfully!');
      }
      
      closeEditForm();
      
      // Refresh data
      fetchSales();
      setSubmitting(false);
    } catch (err) {
      console.error("Error:", err);
      notifyError('Failed to save record: ' + (err.response?.data?.message || err.message));
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    setDeleteConfirm({ show: true, id, deleting: false });
  };

  const confirmDelete = async () => {
    if (!deleteConfirm.id) return;
    try {
      setDeleteConfirm(prev => ({ ...prev, deleting: true }));
      await api.delete(`/api/sales/${deleteConfirm.id}`);
      notifySuccess('Record deleted successfully!');
      setDeleteConfirm({ show: false, id: null, deleting: false });
      fetchSales();
    } catch (err) {
      console.error("Error deleting record:", err);
      notifyError('Failed to delete record: ' + (err.response?.data?.message || err.message));
      setDeleteConfirm(prev => ({ ...prev, deleting: false }));
    }
  };

  const cancelDelete = () => {
    setDeleteConfirm({ show: false, id: null, deleting: false });
  };

  if (error) return <div className="error">{error}</div>;

  return (
    <div className="admin-container">
      <div className="header">
        <h2>Sales Records</h2>
        <button onClick={() => setShowForm(true)} className="add-btn">+ Add New Record</button>
      </div>

      {/* Add Record Form Modal */}
      {showForm && (
        <div
          className="modal-overlay"
          ref={modalOverlayRef}
          onClick={(e) => {
            if (e.target === modalOverlayRef.current) closeEditForm();
          }}
        >
          <div className="modal" ref={modalContentRef} role="dialog" aria-modal="true">
            <h3>{editingId ? 'Edit Sales Record' : 'Add New Sales Record'}</h3>
            <form onSubmit={handleSubmit}>
              <div className="form-grid">
                <div className="form-control">
                  <label>Sale Date *</label>
                  <input
                    type="date"
                    name="saleDate"
                    value={formData.saleDate}
                    onChange={handleFormChange}
                    required
                    className="input-field"
                  />
                </div>

                <div className="form-control">
                  <label>Salesman</label>
                  <input
                    type="text"
                    name="salesman"
                    value={formData.salesman}
                    onChange={handleFormChange}
                    placeholder="e.g., Mukul"
                    className="input-field"
                  />
                </div>

                <div className="form-control">
                  <label>Location</label>
                  <input
                    type="text"
                    name="location"
                    value={formData.location}
                    onChange={handleFormChange}
                    placeholder="e.g., Bokaro Sector 4"
                    className="input-field"
                  />
                </div>

                <div className="form-control">
                  <label>Customer Name *</label>
                  <input
                    type="text"
                    name="customerName"
                    value={formData.customerName}
                    onChange={handleFormChange}
                    required
                    placeholder="e.g., Bajrang Store"
                    className="input-field"
                  />
                </div>

                <div className="form-control">
                  <label>Mobile Number</label>
                  <input
                    type="tel"
                    name="mobileNumber"
                    value={formData.mobileNumber}
                    onChange={handleFormChange}
                    placeholder="e.g., 9876543210"
                    className="input-field"
                  />
                </div>

                <div className="form-control">
                  <label>Variant *</label>
                  <select
                    name="variant"
                    value={formData.variant}
                    onChange={(e) => {
                      setFormData(prev => ({
                        ...prev,
                        variant: e.target.value
                      }));
                    }}
                    required
                    className="select-field"
                  >
                    <option value="">Select Variant</option>
                    <option value="Lemon">Lemon</option>
                    <option value="Neem">Neem</option>
                  </select>
                </div>

                <div className="form-control">
                  <label>Size *</label>
                  <select
                    name="size"
                    value={formData.size}
                    onChange={(e) => {
                      const size = e.target.value;
                      let mfgCost = '';
                      if (size === '1') {
                        mfgCost = '18';
                      } else if (size === '500') {
                        mfgCost = '13';
                      } else if (size === '5') {
                        mfgCost = '72';
                      }
                      setFormData(prev => ({
                        ...prev,
                        size: size,
                        mfgCost: mfgCost
                      }));
                    }}
                    required
                    className="select-field"
                  >
                    <option value="">Select Size</option>
                    <option value="1">1</option>
                    <option value="500">500</option>
                    <option value="5">5</option>
                  </select>
                </div>

                <div className="form-control">
                  <label>Quantity *</label>
                  <input
                    type="number"
                    name="quantity"
                    value={formData.quantity}
                    onChange={handleFormChange}
                    required
                    placeholder="e.g., 10"
                    className="input-field"
                  />
                </div>

                <div className="form-control">
                  <label>Mfg Cost *</label>
                  <input
                    type="number"
                    step="0.01"
                    name="mfgCost"
                    value={formData.mfgCost}
                    onChange={handleFormChange}
                    required
                    placeholder="e.g., 150.00"
                    className="input-field"
                  />
                </div>

                <div className="form-control">
                  <label>Selling Price *</label>
                  <input
                    type="number"
                    step="0.01"
                    name="sellingPrice"
                    value={formData.sellingPrice}
                    onChange={handleFormChange}
                    required
                    placeholder="e.g., 250.00"
                    className="input-field"
                  />
                </div>

                <div className="form-control">
                  <label>Daily Expense Share</label>
                  <input
                    type="number"
                    step="0.01"
                    name="dailyExpenseShare"
                    value={formData.dailyExpenseShare}
                    onChange={handleFormChange}
                    placeholder="e.g., 20.00"
                    className="input-field"
                  />
                </div>
              </div>

              <div className="form-actions">
                <button type="button" onClick={closeEditForm} className="btn-cancel">Cancel</button>
                <button type="submit" disabled={submitting} className={`btn-submit ${submitting ? 'disabled' : ''}`}>
                  {submitting ? (editingId ? 'Updating...' : 'Adding...') : (editingId ? 'Update Record' : 'Add Record')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {loading && <div className="loading">Loading...</div>}

      {!loading && (
        <>
          {/* Filter Section */}
          <div className="filter-bar">
            <div className="form-control">
              <label>Variant:</label>
              <select value={selectedVariant} onChange={handleVariantChange} className="select-field">
                <option value="">All Variants</option>
                {variants.map((variant) => (
                  <option key={variant} value={variant}>
                    {variant}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-control">
              <label>Size:</label>
              <select value={selectedSize} onChange={handleSizeChange} className="select-field">
                <option value="">All Sizes</option>
                {sizes.map((size) => (
                  <option key={size} value={size}>
                    {size}
                  </option>
                ))}
              </select>
            </div>

            <button onClick={resetFilters} className="reset-btn">Reset Filters</button>

            <span className="info-text">Showing {sales.length} of {allSales.length} records</span>
          </div>

          {/* Sorting Section */}
          <div className="sort-bar">
            <span className="sort-label">Sort by:</span>
            <button onClick={() => handleSort('date')} className={`sort-btn ${sortBy === 'date' ? 'active' : ''}`}>
              Date{getSortIcon('date')}
            </button>
            <button onClick={() => handleSort('quantity')} className={`sort-btn ${sortBy === 'quantity' ? 'active' : ''}`}>
              Quantity{getSortIcon('quantity')}
            </button>
            <button onClick={() => handleSort('revenue')} className={`sort-btn ${sortBy === 'revenue' ? 'active' : ''}`}>
              Revenue{getSortIcon('revenue')}
            </button>
          </div>

          {/* Records Table */}
          {sales.length === 0 ? (
            <p>No records found</p>
          ) : (
            <table className="records-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Customer</th>
                  <th>Variant</th>
                  <th>Size</th>
                  <th>Quantity</th>
                  <th>Rate</th>
                  <th>Total Revenue</th>
                  <th className="actions-header">Actions</th>
                </tr>
              </thead>
              <tbody>
                {sales.map((sale) => (
                  <tr key={sale.recordId}>
                    <td>{sale.saleDate}</td>
                    <td>{sale.customerName}</td>
                    <td>{sale.variant}</td>
                    <td>{sale.size}</td>
                    <td>₹{sale.quantity}</td>
                    <td>₹{sale.sellingPrice}</td>
                    <td>₹{sale.totalRevenue}</td>
                    <td className="actions-cell">
                      <button onClick={() => openEditForm(sale)} className="action-btn btn-edit">Edit</button>
                      <button onClick={() => handleDelete(sale.recordId)} className="action-btn btn-delete">Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}

      {/* Delete Confirmation Modal */}
      {deleteConfirm.show && (
        <div className="modal-overlay" onClick={cancelDelete}>
          <div className="modal delete-modal" onClick={(e) => e.stopPropagation()}>
            <div className="delete-icon">⚠️</div>
            <h3>Delete Record?</h3>
            <p>Are you sure you want to delete this record? This action cannot be undone.</p>
            <div className="delete-actions">
              <button onClick={cancelDelete} disabled={deleteConfirm.deleting} className="btn-cancel">
                Cancel
              </button>
              <button onClick={confirmDelete} disabled={deleteConfirm.deleting} className="btn-delete-confirm">
                {deleteConfirm.deleting ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;