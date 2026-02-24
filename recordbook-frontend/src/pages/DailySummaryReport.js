import React, { useState, useEffect } from 'react';
import { notifyError } from '../utils/toast';
import { getTodayDate } from '../utils/dateUtils';

const DailySummaryReport = () => {
    const [salesmen, setSalesmen] = useState([]);

    useEffect(() => {
      const fetchSalesmenAliases = async () => {
        try {
          const response = await (await import('../api')).default.get('/api/v1/admin/salesmen/aliases');
          const data = response.data;
          const aliases = Array.isArray(data) ? data : (data && data.aliases) || [];
          setSalesmen(aliases);
        } catch (err) {
          notifyError('Failed to load salesman aliases');
        }
      };
      fetchSalesmenAliases();
    }, []);
  const [summaries, setSummaries] = useState([]);
  const [loading, setLoading] = useState(false);
  const [salesmanAlias, setSalesmanAlias] = useState('');
  const [saleDate, setSaleDate] = useState(getTodayDate());

  // Fetch summaries using axios (api.js)
  const fetchSummaries = async () => {
    setLoading(true);
    try {
      let url = '/api/summary/all';
      if (salesmanAlias && saleDate) {
        url = `/api/summary/by-salesman-date?alias=${encodeURIComponent(salesmanAlias)}&date=${saleDate}`;
      } else if (salesmanAlias) {
        url = `/api/summary/salesman?alias=${encodeURIComponent(salesmanAlias)}`;
      } else if (saleDate) {
        url = `/api/summary/date?date=${saleDate}`;
      }
      const res = await (await import('../api')).default.get(url);
      const data = res.data;
      setSummaries(Array.isArray(data) ? data : [data]);
    } catch (err) {
      notifyError(err.message);
      setSummaries([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSummaries();
    // eslint-disable-next-line
  }, [salesmanAlias, saleDate]);

  return (
    <div style={{ padding: '24px' }}>
      <h2>Daily Summary Report</h2>
      <div style={{ marginBottom: '16px', display: 'flex', gap: '16px' }}>
        <select
          value={salesmanAlias}
          onChange={e => setSalesmanAlias(e.target.value)}
        >
          <option value="">Select Salesman</option>
          {salesmen.map((alias) => (
            <option key={alias} value={alias}>
              {alias}
            </option>
          ))}
        </select>
        <input
          type="date"
          value={saleDate}
          onChange={e => setSaleDate(e.target.value)}
          style={{ padding: '6px 10px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '13px', fontFamily: 'Calibri, sans-serif', width: '130px' }}
        />
        <button onClick={fetchSummaries} disabled={loading}>
          Refresh
        </button>
      </div>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ background: '#f0f0f0' }}>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>Salesman Alias</th>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>Sale Date</th>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>Material Cost</th>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>Total Expense</th>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>Total Revenue</th>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>Agent Commission</th>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>Net Profit</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr><td colSpan={7} style={{ textAlign: 'center' }}>Loading...</td></tr>
          ) : summaries.length === 0 ? (
            <tr><td colSpan={7} style={{ textAlign: 'center' }}>No data found</td></tr>
          ) : (
            summaries.map((s, idx) => (
              <tr key={idx}>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>{s.salesmanAlias}</td>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>{s.saleDate}</td>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>{s.materialCost}</td>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>{s.totalExpense}</td>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>{s.totalRevenue}</td>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>{s.totalAgentCommission}</td>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>{s.netProfit}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
};

export default DailySummaryReport;
