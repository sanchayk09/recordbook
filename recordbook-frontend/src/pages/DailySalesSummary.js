import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';
import { notifyError, notifySuccess } from '../utils/toast';

const DailySalesSummary = () => {
  const [salesRecords, setSalesRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editingRecord, setEditingRecord] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editFormData, setEditFormData] = useState({});
  const [salesmen, setSalesmen] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchSalesRecords = async () => {
      setLoading(true);
      try {
        const response = await api.get('/api/sales');
        const records = Array.isArray(response.data) ? response.data : [];
        setSalesRecords(records);

        // Fetch salesmen for dropdown
        const salesmenResponse = await api.get('/api/v1/admin/salesmen/aliases');
        setSalesmen(Array.isArray(salesmenResponse.data) ? salesmenResponse.data : []);
      } catch (error) {
        console.error('Error loading data:', error);
        notifyError('Failed to load data: ' + (error.response?.data?.message || error.message));
        setSalesRecords([]);
      } finally {
        setLoading(false);
      }
    };
    fetchSalesRecords();
  }, []);

  const totals = useMemo(() => {
    return salesRecords.reduce(
      (acc, record) => {
        acc.quantity += Number(record.quantity) || 0;
        acc.revenue += Number(record.revenue) || 0;
        acc.agentCommission += Number(record.agentCommission) || 0;
        return acc;
      },
      { quantity: 0, revenue: 0, agentCommission: 0 }
    );
  }, [salesRecords]);

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
      await api.delete(`/api/sales/${recordId}`);
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
      await api.put(`/api/sales/${editingRecord.id}`, editFormData);
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
    <div style={{ padding: '30px', fontFamily: 'Calibri, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2 style={{ margin: 0 }}>Daily Sales Summary</h2>
        <button
          type="button"
          onClick={() => navigate('/daily-sales')}
          style={{
            backgroundColor: '#2c3e50',
            color: '#fff',
            border: 'none',
            padding: '10px 16px',
            borderRadius: '6px',
            cursor: 'pointer',
          }}
        >
          Back to Daily Sales
        </button>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '40px', color: '#999' }}>
          Loading sales records...
        </div>
      ) : (
        <div style={{ overflowX: 'auto', border: '1px solid #e0e0e0', borderRadius: '8px' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '1200px' }}>
            <thead>
              <tr style={{ backgroundColor: '#2c3e50', color: '#fff' }}>
                <th style={thStyle}>SL No</th>
                <th style={thStyle}>Sale Date</th>
                <th style={thStyle}>Salesman</th>
                <th style={thStyle}>Customer Name</th>
                <th style={thStyle}>Type</th>
                <th style={thStyle}>Village</th>
                <th style={thStyle}>Mobile</th>
                <th style={thStyle}>Product</th>
                <th style={thStyle}>Qty</th>
                <th style={thStyle}>Rate</th>
                <th style={thStyle}>Revenue</th>
                <th style={thStyle}>Agent Commission</th>
                <th style={thStyle}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {salesRecords.length === 0 ? (
                <tr>
                  <td colSpan="13" style={{ ...tdStyle, textAlign: 'center', padding: '40px' }}>
                    No sales records found.
                  </td>
                </tr>
              ) : (
                salesRecords.map((record, index) => (
                  <tr key={record.id || index} style={{ backgroundColor: index % 2 === 0 ? '#fff' : '#f7f9fc' }}>
                    <td style={tdStyle}>{record.slNo}</td>
                    <td style={tdStyle}>{record.saleDate}</td>
                    <td style={tdStyle}>{record.salesmanName}</td>
                    <td style={{ ...tdStyle, textAlign: 'left' }}>{record.customerName}</td>
                    <td style={tdStyle}>{record.customerType}</td>
                    <td style={tdStyle}>{record.village}</td>
                    <td style={tdStyle}>{record.mobileNumber}</td>
                    <td style={tdStyle}>{record.productCode}</td>
                    <td style={tdStyle}>{record.quantity}</td>
                    <td style={tdStyle}>{record.rate}</td>
                    <td style={tdStyle}>{record.revenue}</td>
                    <td style={tdStyle}>{record.agentCommission || 0}</td>
                    <td style={tdStyle}>
                      <button
                        onClick={() => handleEdit(record.id)}
                        style={{
                          backgroundColor: '#007bff',
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
            {salesRecords.length > 0 && (
              <tfoot>
                <tr style={{ backgroundColor: '#2c3e50', color: '#fff', fontWeight: 'bold' }}>
                  <td colSpan="10" style={{ ...tdStyle, textAlign: 'right', padding: '10px 8px', border: '1px solid #d0d0d0' }}>
                    Total:
                  </td>
                  <td style={{ ...tdStyle, padding: '10px 8px', border: '1px solid #d0d0d0' }}>
                    {totals.revenue.toFixed(2)}
                  </td>
                  <td style={{ ...tdStyle, padding: '10px 8px', border: '1px solid #d0d0d0' }}>
                    {totals.agentCommission.toFixed(2)}
                  </td>
                  <td style={{ ...tdStyle, padding: '10px 8px', border: '1px solid #d0d0d0' }}></td>
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
