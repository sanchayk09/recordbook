import React, { useMemo, useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { salesmanAPI, expenseAPI } from '../api';
import { notifyError, notifySuccess } from '../utils/toast';
import { cacheUtils } from '../utils/cacheUtils';
import '../styles/DailySalesDump.css';

const DailySalesDump = () => {
  const navigate = useNavigate();
  const [selectedSalesman, setSelectedSalesman] = useState('');
  const [salesmanId, setSalesmanId] = useState('');
  const [jsonInput, setJsonInput] = useState('');
  const [salesData, setSalesData] = useState([]);
  const [parseError, setParseError] = useState('');
  const [expenseDate, setExpenseDate] = useState(() => {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  });
  const [expenseEntries, setExpenseEntries] = useState([
    { category: '', amount: '' },
  ]);
  const [saving, setSaving] = useState(false);
  const [showExpenseModal, setShowExpenseModal] = useState(false);
  const [salesmenList, setSalesmenList] = useState([]);
  const [loadingSalesmen, setLoadingSalesmen] = useState(false);
  const [editingRowIndex, setEditingRowIndex] = useState(null);
  const [editFormData, setEditFormData] = useState({});

  // Memoize loadSalesmen to prevent duplicate calls
  const loadSalesmen = useCallback(async () => {
    setLoadingSalesmen(true);
    try {
      // Check cache first
      let salesmen = cacheUtils.getSalesmenAliases();
      if (!salesmen) {
        const response = await salesmanAPI.getAliases();
        salesmen = Array.isArray(response.data) ? response.data : [];
        cacheUtils.setSalesmenAliases(salesmen);
        console.log('Salesmen API Response (fresh):', response.data);
      } else {
        console.log('Salesmen loaded from cache:', salesmen);
      }
      console.log('Processed salesmen list:', salesmen);
      setSalesmenList(salesmen);
    } catch (error) {
      console.error('Error loading salesmen:', error);
      notifyError('Failed to load salesmen: ' + (error.response?.data?.message || error.message));
      setSalesmenList([]);
    } finally {
      setLoadingSalesmen(false);
    }
  }, []);

  useEffect(() => {
    loadSalesmen();
  }, [loadSalesmen]);

  const convertToInputDate = (value) => {
    if (!value) return '';
    // Accept dd/mm/yyyy or yyyy-mm-dd
    if (/^\d{4}-\d{2}-\d{2}$/.test(value)) return value;
    const parts = value.split('/');
    if (parts.length === 3) {
      const [dd, mm, yyyy] = parts;
      if (yyyy && mm && dd) return `${yyyy}-${mm.padStart(2, '0')}-${dd.padStart(2, '0')}`;
    }
    return '';
  };

  const handleParseJson = () => {
    setParseError('');
    if (!jsonInput.trim()) {
      setSalesData([]);
      return;
    }
    try {
      const parsed = JSON.parse(jsonInput);
      const rows = Array.isArray(parsed) ? parsed : [parsed];
      setSalesData(rows);
      const firstDate = rows[0]?.saleDate || '';
      const normalized = convertToInputDate(firstDate);
      if (normalized) {
        setExpenseDate(normalized);
      }
    } catch (error) {
      setSalesData([]);
      setParseError('Invalid JSON. Please paste a valid JSON array.');
    }
  };

  const handleEditRow = (index, row) => {
    setEditingRowIndex(index);
    setEditFormData({ ...row });
  };

  const handleCancelEdit = () => {
    setEditingRowIndex(null);
    setEditFormData({});
  };

  const handleSaveRow = (index) => {
    const updated = [...salesData];
    updated[index] = editFormData;
    setSalesData(updated);
    setEditingRowIndex(null);
    setEditFormData({});
    notifySuccess('Row updated successfully');
  };

  const handleDeleteRow = (index) => {
    if (window.confirm('Are you sure you want to delete this row?')) {
      setSalesData(salesData.filter((_, i) => i !== index));
      notifySuccess('Row deleted successfully');
    }
  };

  const handleAddRow = () => {
    const newRow = {
      slNo: (salesData.length > 0 ? Math.max(...salesData.map(r => r.slNo || 0)) : 0) + 1,
      saleDate: expenseDate,
      customerName: '',
      customerType: '',
      village: '',
      mobileNumber: '',
      quantity: 0,
      rate: 0,
      revenue: 0,
      productCode: '',
    };
    setSalesData([...salesData, newRow]);
  };

  const handleEditFieldChange = (field, value) => {
    setEditFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSave = async () => {
    if (!salesmanId) {
      notifyError('Please select a salesman.');
      return;
    }
    setShowExpenseModal(true);
  };

  const handleConfirmSave = async () => {
    if (!salesmanId) {
      notifyError('Please select a salesman.');
      return;
    }

    try {
      setSaving(true);

      const requestBody = {
        salesmanAlias: selectedSalesman,
        date: expenseDate, // Keep as yyyy-mm-dd format
        expenses: expenseEntries.filter(e => e.category?.trim() && e.amount !== '').map(e => ({
          expenseDate: expenseDate, // Keep as yyyy-mm-dd format
          category: e.category,
          amount: Number(e.amount)
        })),
        dailySales: salesData
      };

      await expenseAPI.submitSalesWithExpense(requestBody);

      notifySuccess('Data saved successfully.');
      setShowExpenseModal(false);
      navigate('/daily-sales-summary');
    } catch (error) {
      notifyError('Failed to save data: ' + (error.response?.data?.message || error.message));
    } finally {
      setSaving(false);
    }
  };

  const totals = useMemo(() => {
    return salesData.reduce(
      (acc, row) => {
        acc.quantity += Number(row.quantity) || 0;
        acc.rate += Number(row.rate) || 0;
        acc.revenue += Number(row.revenue) || 0;
        return acc;
      },
      { quantity: 0, rate: 0, revenue: 0 }
    );
  }, [salesData]);

  return (
    <div className="dsd-page">
      <div className="dsd-header">
        <h2>Daily Sales Dump</h2>
        <div className="dsd-header-actions">
          <label htmlFor="salesman" className="dsd-salesman-label">Salesman</label>
          <select
            id="salesman"
            value={selectedSalesman}
            onChange={(e) => {
              const selectedName = e.target.value;
              setSelectedSalesman(selectedName);
              setSalesmanId(selectedName); // Use alias as ID
            }}
            className="dsd-salesman-select"
            disabled={loadingSalesmen}
          >
            <option value="">{loadingSalesmen ? 'Loading...' : 'Select salesman'}</option>
            {salesmenList.map((alias, idx) => (
              <option key={idx} value={alias}>
                {alias}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="dsd-json-section">
        <label htmlFor="sales-json" className="dsd-json-label">
          Paste JSON data
        </label>
        <textarea
          id="sales-json"
          value={jsonInput}
          onChange={(e) => setJsonInput(e.target.value)}
          placeholder='[
  {"slNo":1,"saleDate":"19/02/2026","customerName":"ABC Store","customerType":"S","village":"Gola","mobileNumber":"9000000000","quantity":1,"rate":40,"revenue":40,"productCode":"N1"}
]'
          rows={6}
          className="dsd-json-textarea"
        />
        <div className="dsd-json-actions">
          <button
            type="button"
            onClick={handleParseJson}
            className="dsd-button dsd-parse-button"
          >
            Load JSON
          </button>
          <button
            type="button"
            onClick={() => { setJsonInput(''); setSalesData([]); setParseError(''); }}
            className="dsd-button dsd-clear-button"
          >
            Clear
          </button>
        </div>
        {parseError && (
          <div className="dsd-parse-error">
            {parseError}
          </div>
        )}
      </div>

      <div style={{ marginBottom: '20px', display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
        <button
          type="button"
          onClick={handleAddRow}
          disabled={salesData.length === 0}
          style={{
            backgroundColor: salesData.length === 0 ? '#9ca3af' : '#66a37f',
            color: '#fff',
            border: 'none',
            padding: '10px 16px',
            borderRadius: '6px',
            cursor: salesData.length === 0 ? 'not-allowed' : 'pointer',
          }}
        >
          + Add Row
        </button>
        <button
          type="button"
          onClick={handleSave}
          disabled={saving || salesData.length === 0}
          style={{
            backgroundColor: saving || salesData.length === 0 ? '#9ca3af' : '#1f7a4d',
            color: '#fff',
            border: 'none',
            padding: '10px 16px',
            borderRadius: '6px',
            cursor: saving || salesData.length === 0 ? 'not-allowed' : 'pointer',
          }}
        >
          {saving ? 'Saving...' : 'Save Data'}
        </button>
      </div>

      <div style={{ overflowX: 'auto', border: '1px solid #e0e0e0', borderRadius: '8px' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '1100px' }}>
          <thead>
            <tr style={{ backgroundColor: '#66a37f', color: '#fff' }}>
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
              <th style={thStyle}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {salesData.length === 0 ? (
              <tr>
                <td style={{ ...tdStyle, textAlign: 'center' }} colSpan={12}>
                  Paste JSON and click “Load JSON” to view data.
                </td>
              </tr>
            ) : (
              <>
                {salesData.map((row, index) => (
                  editingRowIndex === index ? (
                    <tr key={`edit-${index}`} style={{ backgroundColor: '#fffaed' }}>
                      <td style={tdStyle}>
                        <input type="number" value={editFormData.slNo || ''} onChange={(e) => handleEditFieldChange('slNo', parseInt(e.target.value) || '')} style={{ width: '50px', padding: '4px' }} />
                      </td>
                      <td style={tdStyle}>
                        <input type="date" value={editFormData.saleDate || ''} onChange={(e) => handleEditFieldChange('saleDate', e.target.value)} style={{ width: '100px', padding: '4px' }} />
                      </td>
                      <td style={tdStyle}>{selectedSalesman || ''}</td>
                      <td style={tdStyle}>
                        <input type="text" value={editFormData.customerName || ''} onChange={(e) => handleEditFieldChange('customerName', e.target.value)} style={{ width: '100px', padding: '4px' }} />
                      </td>
                      <td style={tdStyle}>
                        <input type="text" value={editFormData.customerType || ''} onChange={(e) => handleEditFieldChange('customerType', e.target.value)} style={{ width: '60px', padding: '4px' }} />
                      </td>
                      <td style={tdStyle}>
                        <input type="text" value={editFormData.village || ''} onChange={(e) => handleEditFieldChange('village', e.target.value)} style={{ width: '80px', padding: '4px' }} />
                      </td>
                      <td style={tdStyle}>
                        <input type="text" value={editFormData.mobileNumber || ''} onChange={(e) => handleEditFieldChange('mobileNumber', e.target.value)} style={{ width: '90px', padding: '4px' }} />
                      </td>
                      <td style={tdStyle}>
                        <input type="text" value={editFormData.productCode || ''} onChange={(e) => handleEditFieldChange('productCode', e.target.value)} style={{ width: '70px', padding: '4px' }} />
                      </td>
                      <td style={tdStyle}>
                        <input type="number" value={editFormData.quantity || ''} onChange={(e) => handleEditFieldChange('quantity', parseFloat(e.target.value) || '')} style={{ width: '60px', padding: '4px' }} />
                      </td>
                      <td style={tdStyle}>
                        <input type="number" value={editFormData.rate || ''} onChange={(e) => handleEditFieldChange('rate', parseFloat(e.target.value) || '')} style={{ width: '60px', padding: '4px' }} />
                      </td>
                      <td style={tdStyle}>
                        <input type="number" value={editFormData.revenue || ''} onChange={(e) => handleEditFieldChange('revenue', parseFloat(e.target.value) || '')} style={{ width: '60px', padding: '4px' }} />
                      </td>
                      <td style={tdStyle}>
                        <button onClick={() => handleSaveRow(index)} style={{ backgroundColor: '#1f7a4d', color: '#fff', border: 'none', padding: '4px 8px', borderRadius: '4px', cursor: 'pointer', fontSize: '12px' }}>Save</button>
                        <button onClick={handleCancelEdit} style={{ backgroundColor: '#e0e0e0', color: '#333', border: 'none', padding: '4px 8px', borderRadius: '4px', cursor: 'pointer', fontSize: '12px', marginLeft: '4px' }}>Cancel</button>
                      </td>
                    </tr>
                  ) : (
                    <tr key={row.slNo ?? index} style={{ backgroundColor: index % 2 === 0 ? '#fff' : '#f7f9fc' }}>
                      <td style={tdStyle}>{row.slNo}</td>
                      <td style={tdStyle}>{row.saleDate}</td>
                      <td style={tdStyle}>{selectedSalesman || ''}</td>
                      <td style={{ ...tdStyle, textAlign: 'left' }}>{row.customerName}</td>
                      <td style={tdStyle}>{row.customerType}</td>
                      <td style={tdStyle}>{row.village}</td>
                      <td style={tdStyle}>{row.mobileNumber}</td>
                      <td style={tdStyle}>{row.productCode}</td>
                      <td style={tdStyle}>{row.quantity}</td>
                      <td style={tdStyle}>{row.rate}</td>
                      <td style={tdStyle}>{row.revenue}</td>
                      <td style={tdStyle}>
                        <button onClick={() => handleEditRow(index, row)} style={{ backgroundColor: '#66a37f', color: '#fff', border: 'none', padding: '4px 8px', borderRadius: '4px', cursor: 'pointer', fontSize: '12px' }}>Edit</button>
                        <button onClick={() => handleDeleteRow(index)} style={{ backgroundColor: '#dc2626', color: '#fff', border: 'none', padding: '4px 8px', borderRadius: '4px', cursor: 'pointer', fontSize: '12px', marginLeft: '4px' }}>Delete</button>
                      </td>
                    </tr>
                  )
                ))}
                <tr style={{ backgroundColor: '#eef2f7', fontWeight: 'bold' }}>
                  <td style={tdStyle} colSpan={8}>Totals</td>
                  <td style={tdStyle}>{totals.quantity}</td>
                  <td style={tdStyle}>{totals.rate}</td>
                  <td style={tdStyle}>{totals.revenue}</td>
                  <td style={tdStyle}></td>
                </tr>
              </>
            )}
          </tbody>
        </table>
      </div>

      {showExpenseModal && (
        <div
          role="dialog"
          aria-modal="true"
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0,0,0,0.4)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 50,
          }}
        >
          <div style={{ backgroundColor: '#fff', borderRadius: '10px', width: '700px', maxWidth: '95%', padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
              <h3 style={{ margin: 0 }}>Add Expense Entries</h3>
              <button
                type="button"
                onClick={() => setShowExpenseModal(false)}
                style={{ background: 'transparent', border: 'none', fontSize: '20px', cursor: 'pointer' }}
                aria-label="Close"
              >
                ×
              </button>
            </div>

            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', marginBottom: '12px' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label htmlFor="expense-date" style={{ fontWeight: 'bold' }}>Expense Date</label>
                <input
                  id="expense-date"
                  type="date"
                  value={expenseDate}
                  onChange={(e) => setExpenseDate(e.target.value)}
                  style={{ padding: '8px 10px', borderRadius: '6px', border: '1px solid #ccc', width: '180px' }}
                />
              </div>
            </div>

            {expenseEntries.map((entry, index) => (
              <div key={index} style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', marginBottom: '10px' }}>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  <label style={{ fontWeight: 'bold' }}>Category</label>
                  <input
                    type="text"
                    value={entry.category}
                    onChange={(e) => {
                      const next = [...expenseEntries];
                      next[index] = { ...next[index], category: e.target.value };
                      setExpenseEntries(next);
                    }}
                    placeholder="Category"
                    style={{ padding: '8px 10px', borderRadius: '6px', border: '1px solid #ccc', width: '220px' }}
                  />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  <label style={{ fontWeight: 'bold' }}>Amount</label>
                  <input
                    type="number"
                    value={entry.amount}
                    onChange={(e) => {
                      const next = [...expenseEntries];
                      next[index] = { ...next[index], amount: e.target.value };
                      setExpenseEntries(next);
                    }}
                    placeholder="0"
                    style={{ padding: '8px 10px', borderRadius: '6px', border: '1px solid #ccc', width: '140px' }}
                  />
                </div>
                <div style={{ display: 'flex', alignItems: 'flex-end' }}>
                  <button
                    type="button"
                    onClick={() => {
                      setExpenseEntries(expenseEntries.filter((_, i) => i !== index));
                    }}
                    disabled={expenseEntries.length === 1}
                    style={{
                      backgroundColor: expenseEntries.length === 1 ? '#e5e7eb' : '#f5f5f5',
                      color: '#333',
                      border: '1px solid #d1d5db',
                      padding: '8px 12px',
                      borderRadius: '6px',
                      cursor: expenseEntries.length === 1 ? 'not-allowed' : 'pointer',
                    }}
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}

            <div style={{ display: 'flex', gap: '10px', marginTop: '12px', justifyContent: 'space-between' }}>
              <button
                type="button"
                onClick={() => setExpenseEntries([...expenseEntries, { category: '', amount: '' }])}
                style={{
                  backgroundColor: '#66a37f',
                  color: '#fff',
                  border: 'none',
                  padding: '8px 14px',
                  borderRadius: '6px',
                  cursor: 'pointer',
                }}
              >
                Add Expense Row
              </button>
              <div style={{ display: 'flex', gap: '10px' }}>
                <button
                  type="button"
                  onClick={() => setShowExpenseModal(false)}
                  style={{
                    backgroundColor: '#e0e0e0',
                    color: '#333',
                    border: 'none',
                    padding: '8px 14px',
                    borderRadius: '6px',
                    cursor: 'pointer',
                  }}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleConfirmSave}
                  disabled={saving}
                  style={{
                    backgroundColor: saving ? '#9ca3af' : '#1f7a4d',
                    color: '#fff',
                    border: 'none',
                    padding: '8px 14px',
                    borderRadius: '6px',
                    cursor: saving ? 'not-allowed' : 'pointer',
                  }}
                >
                  {saving ? 'Saving...' : 'Save Expense + Sales'}
                </button>
              </div>
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

export default DailySalesDump;
