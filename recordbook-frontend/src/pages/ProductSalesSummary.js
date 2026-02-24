import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';
import { notifyError, notifySuccess } from '../utils/toast';
import { getTodayDate, getCurrentMonth } from '../utils/dateUtils';

const ProductSalesSummary = () => {
  const [productSales, setProductSales] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filterType, setFilterType] = useState('today');
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
  const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0]);
  const [selectedMonth, setSelectedMonth] = useState(new Date().toISOString().substring(0, 7));
  const navigate = useNavigate();
  const [salesmen, setSalesmen] = useState([]);
  const [selectedSalesman, setSelectedSalesman] = useState('');
  const [expenseDate, setExpenseDate] = useState(new Date().toISOString().split('T')[0]);
  const [expenseDetail, setExpenseDetail] = useState(null);
  const [expenseLoading, setExpenseLoading] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);

  useEffect(() => {
    fetchProductSales();
    fetchSalesmen();
  }, [filterType, selectedDate, startDate, endDate, selectedMonth]);

  const fetchProductSales = async () => {
    setLoading(true);
    try {
      let response;

      switch (filterType) {
        case 'today':
          response = await api.get('/api/sales/summary/product-sales/today');
          break;
        case 'date':
          response = await api.get(`/api/sales/summary/product-sales/date?date=${selectedDate}`);
          break;
        case 'month':
          const [year, month] = selectedMonth.split('-');
          response = await api.get(`/api/sales/summary/product-sales/month?year=${year}&month=${month}`);
          break;
        case 'range':
          response = await api.get(`/api/sales/summary/product-sales/range?startDate=${startDate}&endDate=${endDate}`);
          break;
        case 'all-time':
          response = await api.get('/api/sales/summary/product-sales');
          break;
        default:
          response = await api.get('/api/sales/summary/product-sales/today');
      }

      console.log('Product Sales:', response.data);
      const records = Array.isArray(response.data) ? response.data : [];
      setProductSales(records);
    } catch (error) {
      console.error('Error loading data:', error);
      notifyError('Failed to load product sales data: ' + (error.response?.data?.message || error.message));
      setProductSales([]);
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

  const getSummaryPayload = () => {
    return {
      materialCost: productSales.reduce((acc, rec) => acc + (Number(rec.totalCost) || 0), 0),
      totalExpense: expenseDetail ? Number(expenseDetail.totalExpense) : 0,
      saleDate: expenseDate,
      totalAgentCommission: productSales.reduce((acc, rec) => acc + (Number(rec.agentCommission) || 0), 0),
      salesmanAlias: selectedSalesman,
      totalRevenue: productSales.reduce((acc, rec) => acc + (Number(rec.totalRevenue) || 0), 0),
    };
  };

  const handleSubmitSummary = async () => {
    const payload = getSummaryPayload();
    setSubmitLoading(true);
    try {
      await api.post('/api/summary/submit', payload);
      notifySuccess('Summary submitted successfully!');
    } catch (err) {
      notifyError('Failed to submit summary: ' + (err.response?.data?.message || err.message));
    } finally {
      setSubmitLoading(false);
    }
  };

  const totals = useMemo(() => {
    return productSales.reduce(
      (acc, record) => {
        acc.totalQuantity += Number(record.totalQuantity) || 0;
        acc.productCount += 1;
        return acc;
      },
      { totalQuantity: 0, productCount: 0 }
    );
  }, [productSales]);

  return (
    <div style={{ padding: '30px', fontFamily: 'Calibri, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2 style={{ margin: 0 }}>Product Sales Summary</h2>
        <button
          type="button"
          onClick={() => navigate('/daily-sales')}
          style={{
            backgroundColor: '#66a37f',
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

      {/* Date Filter */}
      <div style={{ marginBottom: '8px', padding: '6px', backgroundColor: '#f8f9fa', borderRadius: '6px', border: '1px solid #e0e0e0' }}>
        <div style={{ display: 'flex', gap: '6px', marginBottom: '6px', flexWrap: 'wrap' }}>
          <button
            onClick={() => setFilterType('today')}
            style={{
              padding: '4px 10px',
              borderRadius: '4px',
              border: 'none',
              cursor: 'pointer',
              fontWeight: 'bold',
              fontSize: '12px',
              backgroundColor: filterType === 'today' ? '#16a34a' : '#e9ecef',
              color: filterType === 'today' ? '#fff' : '#333',
            }}
          >
            Today
          </button>

          <button
            onClick={() => setFilterType('month')}
            style={{
              padding: '4px 10px',
              borderRadius: '4px',
              border: 'none',
              cursor: 'pointer',
              fontWeight: 'bold',
              fontSize: '12px',
              backgroundColor: filterType === 'month' ? '#16a34a' : '#e9ecef',
              color: filterType === 'month' ? '#fff' : '#333',
            }}
          >
            This Month
          </button>

          <button
            onClick={() => setFilterType('date')}
            style={{
              padding: '4px 10px',
              borderRadius: '4px',
              border: 'none',
              cursor: 'pointer',
              fontWeight: 'bold',
              fontSize: '12px',
              backgroundColor: filterType === 'date' ? '#16a34a' : '#e9ecef',
              color: filterType === 'date' ? '#fff' : '#333',
            }}
          >
            Specific Date
          </button>

          <button
            onClick={() => setFilterType('range')}
            style={{
              padding: '4px 10px',
              borderRadius: '4px',
              border: 'none',
              cursor: 'pointer',
              fontWeight: 'bold',
              fontSize: '12px',
              backgroundColor: filterType === 'range' ? '#16a34a' : '#e9ecef',
              color: filterType === 'range' ? '#fff' : '#333',
            }}
          >
            Date Range
          </button>

          <button
            onClick={() => setFilterType('all-time')}
            style={{
              padding: '4px 10px',
              borderRadius: '4px',
              border: 'none',
              cursor: 'pointer',
              fontWeight: 'bold',
              fontSize: '12px',
              backgroundColor: filterType === 'all-time' ? '#16a34a' : '#e9ecef',
              color: filterType === 'all-time' ? '#fff' : '#333',
            }}
          >
            All Time
          </button>
        </div>

        {/* Filter input based on selected filter type */}
        <div style={{ display: 'flex', gap: '6px', alignItems: 'center', flexWrap: 'wrap' }}>
          {filterType === 'date' && (
            <>
              <input
                type="date"
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                style={{
                  padding: '4px 8px',
                  width: '130px',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '12px',
                  fontFamily: 'Calibri, sans-serif',
                }}
              />
            </>
          )}

          {filterType === 'month' && (
            <>
              <input
                type="month"
                value={selectedMonth}
                onChange={(e) => setSelectedMonth(e.target.value)}
                style={{
                  padding: '4px 8px',
                  width: '110px',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '12px',
                  fontFamily: 'Calibri, sans-serif',
                }}
              />
            </>
          )}

          {filterType === 'range' && (
            <>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                style={{
                  padding: '4px 8px',
                  width: '130px',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '12px',
                  fontFamily: 'Calibri, sans-serif',
                }}
              />
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                style={{
                  padding: '4px 8px',
                  width: '130px',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '12px',
                  fontFamily: 'Calibri, sans-serif',
                }}
              />
            </>
          )}

          <span style={{ color: '#666', fontSize: '12px', marginLeft: 'auto' }}>
            Showing <strong>{productSales.length}</strong> product{productSales.length !== 1 ? 's' : ''}
          </span>
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '40px', color: '#999' }}>
          Loading product sales data...
        </div>
      ) : (
        <div style={{ overflowX: 'auto', border: '1px solid #e0e0e0', borderRadius: '8px' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '600px' }}>
            <thead>
              <tr style={{ backgroundColor: '#66a37f', color: '#fff' }}>
                <th style={thStyle}>Rank</th>
                <th style={thStyle}>Product Code</th>
                <th style={thStyle}>Total Quantity</th>
                <th style={thStyle}>Total Revenue</th>
                <th style={thStyle}>Avg Revenue</th>
                <th style={thStyle}>Cost/Unit</th>
                <th style={thStyle}>Total material cost</th>
                <th style={thStyle}>Profit without op cost</th>
                <th style={thStyle}>Avg Profit</th>
                <th style={thStyle}>Op Cost</th>
                <th style={thStyle}>Salesman Comm</th>
                <th style={thStyle}>Net Profit</th>
                <th style={thStyle}>Avg Net Profit</th>
              </tr>
            </thead>
            <tbody>
              {productSales.length === 0 ? (
                <tr>
                  <td colSpan="9" style={{ ...tdStyle, textAlign: 'center', padding: '40px' }}>
                    No product sales data found.
                  </td>
                </tr>
              ) : (
                productSales.map((record, index) => (
                  <tr key={index} style={{ backgroundColor: index % 2 === 0 ? '#fff' : '#f7f9fc' }}>
                    <td style={tdStyle}>{index + 1}</td>
                    <td style={{ ...tdStyle, textAlign: 'left', fontWeight: 'bold' }}>{record.productCode}</td>
                    <td style={tdStyle}>{record.totalQuantity}</td>
                    <td style={tdStyle}>{record.totalRevenue !== undefined ? Number(record.totalRevenue).toFixed(2) : '-'}</td>
                    <td style={tdStyle}>{record.averageRevenue !== undefined ? Number(record.averageRevenue).toFixed(2) : '-'}</td>
                    <td style={tdStyle}>{record.productCost !== undefined ? Number(record.productCost).toFixed(2) : '-'}</td>
                    <td style={tdStyle}>{record.totalCost !== undefined ? Number(record.totalCost).toFixed(2) : '-'}</td>
                    <td style={tdStyle}>{record.totalProfit !== undefined ? Number(record.totalProfit).toFixed(2) : '-'}</td>
                    <td style={tdStyle}>{record.avgProfit !== undefined ? Number(record.avgProfit).toFixed(2) : '-'}</td>
                    <td style={tdStyle}>{record.opCost !== undefined ? Number(record.opCost).toFixed(2) : '-'}</td>
                    <td style={tdStyle}>{record.salesmanCommission !== undefined ? Number(record.salesmanCommission).toFixed(2) : '-'}</td>
                    <td style={tdStyle}>{record.netProfit !== undefined ? Number(record.netProfit).toFixed(2) : '-'}</td>
                    <td style={tdStyle}>{record.avgNetProfit !== undefined ? Number(record.avgNetProfit).toFixed(2) : '-'}</td>
                  </tr>
                ))
              )}
            </tbody>
            {productSales.length > 0 && (
              <tfoot>
                <tr style={{ backgroundColor: '#66a37f', color: '#fff', fontWeight: 'bold' }}>
                  <td colSpan="2" style={{ ...tdStyle, textAlign: 'right', padding: '10px 8px', border: '1px solid #d0d0d0' }}>
                    Total:
                  </td>
                  <td style={{ ...tdStyle, padding: '10px 8px', border: '1px solid #d0d0d0' }}>
                    {productSales.reduce((acc, rec) => acc + (Number(rec.totalQuantity) || 0), 0)}
                  </td>
                  <td style={{ ...tdStyle, padding: '10px 8px', border: '1px solid #d0d0d0' }}>
                    {productSales.reduce((acc, rec) => acc + (Number(rec.totalRevenue) || 0), 0).toFixed(2)}
                  </td>
                  <td style={{ ...tdStyle, padding: '10px 8px', border: '1px solid #d0d0d0' }}></td>
                  <td style={{ ...tdStyle, padding: '10px 8px', border: '1px solid #d0d0d0' }}></td>
                  <td style={{ ...tdStyle, padding: '10px 8px', border: '1px solid #d0d0d0' }}>
                    {productSales.reduce((acc, rec) => acc + (Number(rec.totalCost) || 0), 0).toFixed(2)}
                  </td>
                  <td style={{ ...tdStyle, padding: '10px 8px', border: '1px solid #d0d0d0' }}>
                    {productSales.reduce((acc, rec) => acc + (Number(rec.totalProfit) || 0), 0).toFixed(2)}
                  </td>
                  <td style={{ ...tdStyle, padding: '10px 8px', border: '1px solid #d0d0d0' }}></td>
                  <td style={{ ...tdStyle, padding: '10px 8px', border: '1px solid #d0d0d0' }}>
                    {productSales.reduce((acc, rec) => acc + (Number(rec.opCost) || 0), 0).toFixed(2)}
                  </td>
                  <td style={{ ...tdStyle, padding: '10px 8px', border: '1px solid #d0d0d0' }}>
                    {productSales.reduce((acc, rec) => acc + (Number(rec.salesmanCommission) || 0), 0).toFixed(2)}
                  </td>
                  <td style={{ ...tdStyle, padding: '10px 8px', border: '1px solid #d0d0d0' }}>
                    {productSales.reduce((acc, rec) => acc + (Number(rec.netProfit) || 0), 0).toFixed(2)}
                  </td>
                  <td style={{ ...tdStyle, padding: '10px 8px', border: '1px solid #d0d0d0' }}></td>
                </tr>
              </tfoot>
            )}
          </table>
        </div>
      )}
      <hr style={{ margin: '40px 0 20px 0' }} />
      <h3>Show Daily Expense for Salesman</h3>
      <div style={{ display: 'flex', gap: 10, alignItems: 'center', marginBottom: 20 }}>
        <label style={{ fontWeight: 'bold' }}>Salesman:</label>
        <select value={selectedSalesman} onChange={e => setSelectedSalesman(e.target.value)} style={{ padding: 8, borderRadius: 4, border: '1px solid #ccc', minWidth: 120 }}>
          {salesmen.map(alias => (
            <option key={alias} value={alias}>{alias}</option>
          ))}
        </select>
        <label style={{ fontWeight: 'bold' }}>Date:</label>
        <input type="date" value={expenseDate} onChange={e => setExpenseDate(e.target.value)} style={{ padding: 8, borderRadius: 4, border: '1px solid #ccc', minWidth: 120 }} />
        <button onClick={() => setExpenseDate(getTodayDate())} style={{ padding: '8px 16px', borderRadius: 4, background: '#6c757d', color: '#fff', border: 'none', fontWeight: 'bold' }}>Today</button>
        <button onClick={fetchExpenseDetail} style={{ padding: '8px 16px', borderRadius: 4, background: '#16a34a', color: '#fff', border: 'none', fontWeight: 'bold' }}>Show Expense</button>
      </div>
      {expenseLoading ? (
        <div>Loading expense...</div>
      ) : expenseDetail ? (
        <div style={{ marginTop: 10 }}>
          <table style={{ width: 400, borderCollapse: 'collapse', border: '1px solid #e0e0e0', borderRadius: 8 }}>
            <thead>
              <tr style={{ background: '#66a37f', color: '#fff' }}>
                <th style={thStyle}>Salesman</th>
                <th style={thStyle}>Date</th>
                <th style={thStyle}>Total Expense</th>
              </tr>
            </thead>
            <tbody>
              <tr style={{ background: '#fff' }}>
                <td style={tdStyle}>{expenseDetail.salesmanAlias}</td>
                <td style={tdStyle}>{expenseDetail.expenseDate}</td>
                <td style={tdStyle}>{expenseDetail.totalExpense}</td>
              </tr>
            </tbody>
          </table>
          <button
            onClick={handleSubmitSummary}
            disabled={submitLoading}
            style={{ marginTop: 16, padding: '10px 24px', borderRadius: 4, background: '#28a745', color: '#fff', border: 'none', fontWeight: 'bold', fontSize: 16 }}
          >
            {submitLoading ? 'Submitting...' : 'Submit Summary'}
          </button>
        </div>
      ) : (
        <div style={{ color: '#888', marginTop: 10 }}>No expense found for selected salesman and date.</div>
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

export default ProductSalesSummary;
