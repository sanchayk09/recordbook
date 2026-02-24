import React, { useEffect, useState } from 'react';
import api from '../api';
import { notifyError, notifySuccess } from '../utils/toast';
import { getTodayDate } from '../utils/dateUtils';

const ProductCostManager = () => {
  const [costs, setCosts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ productName: '', productCode: '', cost: '' });
  const [checkingCode, setCheckingCode] = useState('');
  const [codeExists, setCodeExists] = useState(null);
  const [searchCode, setSearchCode] = useState('');
  const [searchName, setSearchName] = useState('');
  const [editId, setEditId] = useState(null);
  const [editForm, setEditForm] = useState({ productName: '', productCode: '', cost: '' });
  const [salesmen, setSalesmen] = useState([]);
  const [selectedSalesman, setSelectedSalesman] = useState('');
  const [expenseDate, setExpenseDate] = useState(getTodayDate());
  const [expenseDetail, setExpenseDetail] = useState(null);
  const [expenseLoading, setExpenseLoading] = useState(false);

  useEffect(() => {
    fetchCosts();
    fetchSalesmen();
  }, []);

  const fetchCosts = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/product-cost/all');
      setCosts(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      notifyError('Failed to fetch product costs');
    } finally {
      setLoading(false);
    }
  };

  const fetchSalesmen = async () => {
    try {
      const res = await api.get('/api/v1/admin/salesmen/aliases');
      setSalesmen(Array.isArray(res.data) ? res.data : []);
      if (res.data && res.data.length > 0) setSelectedSalesman(res.data[0]);
    } catch (e) {
      setSalesmen([]);
    }
  };

  const handleInputChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleAdd = async (e) => {
    e.preventDefault();
    try {
      await api.post('/api/product-cost/add', {
        productName: form.productName,
        productCode: form.productCode,
        cost: form.cost,
      });
      notifySuccess('Product cost added');
      setForm({ productName: '', productCode: '', cost: '' });
      fetchCosts();
    } catch (err) {
      notifyError('Failed to add product cost: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleDelete = async (pid) => {
    if (!window.confirm('Delete this product cost?')) return;
    try {
      await api.delete(`/api/product-cost/delete/${pid}`);
      notifySuccess('Product cost deleted');
      fetchCosts();
    } catch (err) {
      notifyError('Failed to delete: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleCheckCode = async () => {
    if (!checkingCode) return;
    setCodeExists(null);
    try {
      const res = await api.get(`/api/product-cost/exists/${checkingCode}`);
      setCodeExists(res.data.exists);
    } catch (err) {
      notifyError('Failed to check code');
      setCodeExists(null);
    }
  };

  // Search by code
  const handleSearchCode = async () => {
    if (!searchCode) return;
    setLoading(true);
    try {
      const res = await api.get(`/api/product-cost/by-code/${searchCode}`);
      setCosts(res.data ? [res.data] : []);
    } catch (err) {
      notifyError('Not found');
      setCosts([]);
    } finally {
      setLoading(false);
    }
  };

  // Search by name
  const handleSearchName = async () => {
    if (!searchName) return;
    setLoading(true);
    try {
      const res = await api.get(`/api/product-cost/by-name/${searchName}`);
      setCosts(res.data ? [res.data] : []);
    } catch (err) {
      notifyError('Not found');
      setCosts([]);
    } finally {
      setLoading(false);
    }
  };

  // Edit support
  const startEdit = (row) => {
    setEditId(row.pid);
    setEditForm({ productName: row.productName, productCode: row.productCode, cost: row.cost });
  };
  const cancelEdit = () => {
    setEditId(null);
    setEditForm({ productName: '', productCode: '', cost: '' });
  };
  const handleEditChange = (e) => {
    setEditForm({ ...editForm, [e.target.name]: e.target.value });
  };
  const handleEditSave = async (pid) => {
    try {
      await api.put(`/api/product-cost/update/${pid}`, editForm);
      notifySuccess('Product cost updated');
      setEditId(null);
      fetchCosts();
    } catch (err) {
      notifyError('Failed to update: ' + (err.response?.data?.message || err.message));
    }
  };

  // Expense fetch
  const fetchExpenseDetail = async () => {
    if (!selectedSalesman || !expenseDate) return;
    setExpenseLoading(true);
    setExpenseDetail(null);
    try {
      const res = await api.get(`/api/daily-expenses/salesman-date?alias=${selectedSalesman}&date=${expenseDate}`);
      setExpenseDetail(res.data);
    } catch (err) {
      setExpenseDetail(null);
    } finally {
      setExpenseLoading(false);
    }
  };

  return (
    <div style={{ padding: 30, fontFamily: 'Calibri, sans-serif', maxWidth: 900, margin: '0 auto' }}>
      <h2>Product Cost Manager</h2>
      <form onSubmit={handleAdd} style={{ display: 'flex', gap: 10, marginBottom: 20, flexWrap: 'wrap' }}>
        <input
          name="productName"
          value={form.productName}
          onChange={handleInputChange}
          placeholder="Product Name"
          required
          style={{ padding: 8, borderRadius: 4, border: '1px solid #ccc', minWidth: 120 }}
        />
        <input
          name="productCode"
          value={form.productCode}
          onChange={handleInputChange}
          placeholder="Product Code"
          required
          style={{ padding: 8, borderRadius: 4, border: '1px solid #ccc', minWidth: 80 }}
        />
        <input
          name="cost"
          type="number"
          step="0.01"
          value={form.cost}
          onChange={handleInputChange}
          placeholder="Cost"
          required
          style={{ padding: 8, borderRadius: 4, border: '1px solid #ccc', minWidth: 80 }}
        />
        <button type="submit" style={{ padding: '8px 16px', borderRadius: 4, background: '#007bff', color: '#fff', border: 'none', fontWeight: 'bold' }}>Add</button>
      </form>

      <div style={{ marginBottom: 20, display: 'flex', gap: 10, flexWrap: 'wrap', alignItems: 'center' }}>
        <input
          value={checkingCode}
          onChange={e => setCheckingCode(e.target.value)}
          placeholder="Check Product Code"
          style={{ padding: 8, borderRadius: 4, border: '1px solid #ccc', minWidth: 80 }}
        />
        <button onClick={handleCheckCode} style={{ padding: '8px 16px', borderRadius: 4, background: '#28a745', color: '#fff', border: 'none', fontWeight: 'bold' }}>Check</button>
        {codeExists !== null && (
          <span style={{ color: codeExists ? 'red' : 'green', fontWeight: 'bold' }}>
            {codeExists ? 'Code Exists' : 'Code Available'}
          </span>
        )}
        <input
          value={searchCode}
          onChange={e => setSearchCode(e.target.value)}
          placeholder="Search by Code"
          style={{ padding: 8, borderRadius: 4, border: '1px solid #ccc', minWidth: 80 }}
        />
        <button onClick={handleSearchCode} style={{ padding: '8px 16px', borderRadius: 4, background: '#6c757d', color: '#fff', border: 'none', fontWeight: 'bold' }}>Search</button>
        <input
          value={searchName}
          onChange={e => setSearchName(e.target.value)}
          placeholder="Search by Name"
          style={{ padding: 8, borderRadius: 4, border: '1px solid #ccc', minWidth: 80 }}
        />
        <button onClick={handleSearchName} style={{ padding: '8px 16px', borderRadius: 4, background: '#6c757d', color: '#fff', border: 'none', fontWeight: 'bold' }}>Search</button>
        <button onClick={fetchCosts} style={{ padding: '8px 16px', borderRadius: 4, background: '#ffc107', color: '#333', border: 'none', fontWeight: 'bold' }}>Show All</button>
      </div>

      <h3>Product Cost List</h3>
      {loading ? (
        <div>Loading...</div>
      ) : (
        <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 10 }}>
          <thead>
            <tr style={{ background: '#2c3e50', color: '#fff' }}>
              <th style={thStyle}>PID</th>
              <th style={thStyle}>Product Name</th>
              <th style={thStyle}>Product Code</th>
              <th style={thStyle}>Cost</th>
              <th style={thStyle}>Created At</th>
              <th style={thStyle}>Updated At</th>
              <th style={thStyle}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {costs.length === 0 ? (
              <tr><td colSpan="7" style={{ textAlign: 'center', padding: 30 }}>No product costs found.</td></tr>
            ) : (
              costs.map(row => (
                <tr key={row.pid} style={{ background: '#fff' }}>
                  <td style={tdStyle}>{row.pid}</td>
                  <td style={tdStyle}>
                    {editId === row.pid ? (
                      <input name="productName" value={editForm.productName} onChange={handleEditChange} style={{ padding: 4, borderRadius: 3, border: '1px solid #ccc' }} />
                    ) : (
                      row.productName
                    )}
                  </td>
                  <td style={tdStyle}>
                    {editId === row.pid ? (
                      <input name="productCode" value={editForm.productCode} onChange={handleEditChange} style={{ padding: 4, borderRadius: 3, border: '1px solid #ccc' }} />
                    ) : (
                      row.productCode
                    )}
                  </td>
                  <td style={tdStyle}>
                    {editId === row.pid ? (
                      <input name="cost" type="number" step="0.01" value={editForm.cost} onChange={handleEditChange} style={{ padding: 4, borderRadius: 3, border: '1px solid #ccc' }} />
                    ) : (
                      row.cost
                    )}
                  </td>
                  <td style={tdStyle}>{row.createdAt}</td>
                  <td style={tdStyle}>{row.updatedAt}</td>
                  <td style={tdStyle}>
                    {editId === row.pid ? (
                      <>
                        <button onClick={() => handleEditSave(row.pid)} style={{ background: '#28a745', color: '#fff', border: 'none', borderRadius: 3, padding: '4px 10px', cursor: 'pointer', fontWeight: 'bold', marginRight: 4 }}>Save</button>
                        <button onClick={cancelEdit} style={{ background: '#6c757d', color: '#fff', border: 'none', borderRadius: 3, padding: '4px 10px', cursor: 'pointer', fontWeight: 'bold' }}>Cancel</button>
                      </>
                    ) : (
                      <>
                        <button onClick={() => startEdit(row)} style={{ background: '#007bff', color: '#fff', border: 'none', borderRadius: 3, padding: '4px 10px', cursor: 'pointer', fontWeight: 'bold', marginRight: 4 }}>Edit</button>
                        <button onClick={() => handleDelete(row.pid)} style={{ background: '#dc3545', color: '#fff', border: 'none', borderRadius: 3, padding: '4px 10px', cursor: 'pointer', fontWeight: 'bold' }}>Delete</button>
                      </>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      )}
      <hr style={{ margin: '40px 0 20px 0' }} />
      <h3>Show Daily Product Sales & Expense</h3>
      <div style={{ display: 'flex', gap: 10, alignItems: 'center', marginBottom: 20 }}>
        <label style={{ fontWeight: 'bold' }}>Salesman:</label>
        <select value={selectedSalesman} onChange={e => setSelectedSalesman(e.target.value)} style={{ padding: 8, borderRadius: 4, border: '1px solid #ccc', minWidth: 120 }}>
          {salesmen.map(alias => (
            <option key={alias} value={alias}>{alias}</option>
          ))}
        </select>
        <label style={{ fontWeight: 'bold' }}>Date:</label>
        <input type="date" value={expenseDate} onChange={e => setExpenseDate(e.target.value)} style={{ padding: 8, borderRadius: 4, border: '1px solid #ccc', minWidth: 120 }} />
        <button onClick={fetchExpenseDetail} style={{ padding: '8px 16px', borderRadius: 4, background: '#007bff', color: '#fff', border: 'none', fontWeight: 'bold' }}>Show Expense</button>
      </div>
      {expenseLoading ? (
        <div>Loading expense...</div>
      ) : expenseDetail ? (
        <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 10 }}>
          <thead>
            <tr style={{ background: '#2c3e50', color: '#fff' }}>
              <th style={thStyle}>Salesman</th>
              <th style={thStyle}>Date</th>
              <th style={thStyle}>Total Expense</th>
              <th style={thStyle}>Created At</th>
              <th style={thStyle}>Updated At</th>
            </tr>
          </thead>
          <tbody>
            <tr style={{ background: '#fff' }}>
              <td style={tdStyle}>{expenseDetail.salesmanAlias}</td>
              <td style={tdStyle}>{expenseDetail.expenseDate}</td>
              <td style={tdStyle}>{expenseDetail.totalExpense}</td>
              <td style={tdStyle}>{expenseDetail.createdAt}</td>
              <td style={tdStyle}>{expenseDetail.updatedAt}</td>
            </tr>
          </tbody>
        </table>
      ) : (
        <div style={{ color: '#888', marginTop: 10 }}>No expense found for selected salesman and date.</div>
      )}
    </div>
  );
};

const thStyle = {
  padding: '8px',
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

export default ProductCostManager;
