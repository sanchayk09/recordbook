import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { LineChart, Line, PieChart, Pie, Cell, Sector, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { notifyError } from '../utils/toast';
import { getTodayDate } from '../utils/dateUtils';
import '../styles/DailySummaryReport.css';

const DailySummaryReport = () => {
  const ALL_SALESMEN_VALUE = '__ALL__';
    const [salesmen, setSalesmen] = useState([]);

    // Memoize fetchSalesmenAliases to prevent duplicate calls
    const fetchSalesmenAliases = useCallback(async () => {
      try {
        const response = await (await import('../api')).default.get('/api/admin/salesmen/aliases');
        const data = response.data;
        const aliases = Array.isArray(data) ? data : (data && data.aliases) || [];
        setSalesmen(aliases);
      } catch (err) {
        notifyError('Failed to load salesman aliases');
      }
    }, []);

    useEffect(() => {
      fetchSalesmenAliases();
    }, [fetchSalesmenAliases]);

  const [summaries, setSummaries] = useState([]);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [salesmanAlias, setSalesmanAlias] = useState(ALL_SALESMEN_VALUE);
  const [filterType, setFilterType] = useState('last30days'); // today, week, month, dateRange
  const [saleDate, setSaleDate] = useState(getTodayDate());
  const [startDate, setStartDate] = useState(getTodayDate());
  const [endDate, setEndDate] = useState(getTodayDate());
  const [showProfitColumns, setShowProfitColumns] = useState(false);
  const [viewMode, setViewMode] = useState('table');
  const [activeSliceIndex, setActiveSliceIndex] = useState(null);
  const [sortBy, setSortBy] = useState('date');
  const [sortOrder, setSortOrder] = useState('desc');

  // Helper function to get week start and end dates
  const getWeekDates = (date) => {
    const d = new Date(date);
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1);
    const weekStart = new Date(d.setDate(diff));
    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekEnd.getDate() + 6);
    return {
      start: weekStart.toISOString().split('T')[0],
      end: weekEnd.toISOString().split('T')[0]
    };
  };

  // Helper function to get month start and end dates
  const getMonthDates = (date) => {
    const d = new Date(date);
    const monthStart = new Date(d.getFullYear(), d.getMonth(), 1);
    const monthEnd = new Date(d.getFullYear(), d.getMonth() + 1, 0);
    return {
      start: monthStart.toISOString().split('T')[0],
      end: monthEnd.toISOString().split('T')[0]
    };
  };

  // Helper function to get last X days range
  const getLastXDays = (days) => {
    const today = new Date();
    const startDate = new Date(today);
    startDate.setDate(today.getDate() - days + 1);
    return {
      start: startDate.toISOString().split('T')[0],
      end: today.toISOString().split('T')[0]
    };
  };

  // Fetch summaries using unified API endpoint
  const fetchSummaries = useCallback(async () => {
    setLoading(true);
    try {
      // Build query parameters based on filter type
      const params = new URLSearchParams();

      // Add salesman if selected
      if (salesmanAlias && salesmanAlias !== ALL_SALESMEN_VALUE) {
        params.append('alias', salesmanAlias);
      }

      // Add date/range parameters based on filter type
      if (filterType === 'today') {
        params.append('date', getTodayDate());
      } else if (filterType === 'week') {
        const { start, end } = getWeekDates(saleDate);
        params.append('startDate', start);
        params.append('endDate', end);
      } else if (filterType === 'month') {
        const { start, end } = getMonthDates(saleDate);
        params.append('startDate', start);
        params.append('endDate', end);
      } else if (filterType === 'last7days' || filterType === 'last15days' || filterType === 'last30days' || filterType === 'last90days') {
        const daysMap = {
          'last7days': 7,
          'last15days': 15,
          'last30days': 30,
          'last90days': 90
        };
        const { start, end } = getLastXDays(daysMap[filterType]);
        params.append('startDate', start);
        params.append('endDate', end);
      } else if (filterType === 'dateRange') {
        params.append('startDate', startDate);
        params.append('endDate', endDate);
      }
      // If filterType is 'all', no params needed

      // Build URL with query params
      const query = params.toString();
      const url = `/api/summary${query ? '?' + query : ''}`;

      const res = await (await import('../api')).default.get(url);
      const data = res.data;
      setSummaries(Array.isArray(data) ? data : (data ? [data] : []));
    } catch (err) {
      notifyError(err.message);
      setSummaries([]);
    } finally {
      setLoading(false);
    }
  }, [filterType, salesmanAlias, saleDate, startDate, endDate, ALL_SALESMEN_VALUE]);

  useEffect(() => {
    fetchSummaries();
  }, [fetchSummaries]);

  const chartData = useMemo(() => {
    // Aggregate revenue and net profit by sale date
    const aggregated = {};
    summaries.forEach(record => {
      if (!aggregated[record.saleDate]) {
        aggregated[record.saleDate] = { revenue: 0, netProfit: 0 };
      }
      aggregated[record.saleDate].revenue += Number(record.totalRevenue) || 0;
      aggregated[record.saleDate].netProfit += Number(record.netProfit) || 0;
    });

    // Convert to array and sort by date
    return Object.entries(aggregated)
      .map(([date, data]) => ({
        date,
        revenue: Number(data.revenue.toFixed(2)),
        netProfit: Number(data.netProfit.toFixed(2))
      }))
      .sort((a, b) => a.date.localeCompare(b.date));
  }, [summaries]);

  const volumeChartData = useMemo(() => {
    // Aggregate volume sold and quantity by sale date
    const aggregated = {};
    summaries.forEach(record => {
      if (!aggregated[record.saleDate]) {
        aggregated[record.saleDate] = { volumeSold: 0, quantity: 0 };
      }
      aggregated[record.saleDate].volumeSold += Number(record.volumeSold) || 0;
      aggregated[record.saleDate].quantity += Number(record.totalQuantity) || 0;
    });

    // Convert to array and sort by date
    return Object.entries(aggregated)
      .map(([date, data]) => ({
        date,
        volumeSold: Number(data.volumeSold.toFixed(2)),
        quantity: Number(data.quantity.toFixed(2))
      }))
      .sort((a, b) => a.date.localeCompare(b.date));
  }, [summaries]);

  const totals = useMemo(() => {
    return summaries.reduce(
      (acc, record) => {
        acc.totalQuantity += Number(record.totalQuantity) || 0;
        acc.volumeSold += Number(record.volumeSold) || 0;
        acc.materialCost += Number(record.materialCost) || 0;
        acc.totalExpense += Number(record.totalExpense) || 0;
        acc.totalRevenue += Number(record.totalRevenue) || 0;
        acc.totalAgentCommission += Number(record.totalAgentCommission) || 0;
        acc.netProfit += Number(record.netProfit) || 0;
        return acc;
      },
      { totalQuantity: 0, volumeSold: 0, materialCost: 0, totalExpense: 0, totalRevenue: 0, totalAgentCommission: 0, netProfit: 0 }
    );
  }, [summaries]);

  const groupedByDate = useMemo(() => {
    const grouped = summaries.reduce((acc, record) => {
      const key = record.saleDate || 'Unknown Date';
      if (!acc[key]) acc[key] = [];
      acc[key].push(record);
      return acc;
    }, {});

    const groupsWithTotals = Object.entries(grouped).map(([date, rows]) => {
      const totalsByDate = rows.reduce(
        (acc, s) => {
          acc.volumeSold += Number(s.volumeSold) || 0;
          acc.totalRevenue += Number(s.totalRevenue) || 0;
          acc.totalAgentCommission += Number(s.totalAgentCommission) || 0;
          acc.netProfit += Number(s.netProfit) || 0;
          return acc;
        },
        { volumeSold: 0, totalRevenue: 0, totalAgentCommission: 0, netProfit: 0 }
      );

      return { date, rows, totalsByDate };
    });

    groupsWithTotals.sort((a, b) => {
      let valueA;
      let valueB;

      if (sortBy === 'date') {
        valueA = new Date(a.date).getTime() || 0;
        valueB = new Date(b.date).getTime() || 0;
      } else if (sortBy === 'revenue') {
        valueA = a.totalsByDate.totalRevenue;
        valueB = b.totalsByDate.totalRevenue;
      } else if (sortBy === 'volume') {
        valueA = a.totalsByDate.volumeSold;
        valueB = b.totalsByDate.volumeSold;
      } else if (sortBy === 'ac') {
        valueA = a.totalsByDate.totalAgentCommission;
        valueB = b.totalsByDate.totalAgentCommission;
      } else if (sortBy === 'netProfit') {
        valueA = a.totalsByDate.netProfit;
        valueB = b.totalsByDate.netProfit;
      } else {
        valueA = 0;
        valueB = 0;
      }

      return sortOrder === 'asc' ? valueA - valueB : valueB - valueA;
    });

    return groupsWithTotals;
  }, [summaries, sortBy, sortOrder]);

  const handleSort = (column) => {
    if (sortBy === column) {
      setSortOrder((prev) => (prev === 'asc' ? 'desc' : 'asc'));
      return;
    }
    setSortBy(column);
    setSortOrder('asc');
  };

  const getSortIndicator = (column) => {
    if (sortBy !== column) return ' ⇅';
    return sortOrder === 'asc' ? ' ↑' : ' ↓';
  };

  const showSalesmanDetails = salesmanAlias !== '';
  const visibleColumnCount = showProfitColumns
    ? (showSalesmanDetails ? 11 : 10)
    : (showSalesmanDetails ? 9 : 8);

  const pieChartData = useMemo(() => {
    return [
      { name: 'Material Cost', value: Number(totals.materialCost.toFixed(2)) },
      { name: 'Total Expense', value: Number(totals.totalExpense.toFixed(2)) },
      { name: 'Agent Commission', value: Number(totals.totalAgentCommission.toFixed(2)) },
      { name: 'Net Profit', value: Number(totals.netProfit.toFixed(2)) }
    ].filter(item => item.value > 0);
  }, [totals]);

  const renderActiveSlice = (props) => {
    const RADIAN = Math.PI / 180;
    const {
      cx,
      cy,
      midAngle,
      innerRadius,
      outerRadius,
      startAngle,
      endAngle,
      fill
    } = props;
    const sin = Math.sin(-RADIAN * midAngle);
    const cos = Math.cos(-RADIAN * midAngle);
    const offset = 10;
    const sx = cx + offset * cos;
    const sy = cy + offset * sin;

    return (
      <g>
        <Sector
          cx={sx}
          cy={sy}
          innerRadius={innerRadius}
          outerRadius={outerRadius + 6}
          startAngle={startAngle}
          endAngle={endAngle}
          fill={fill}
          stroke="#d19a4a"
          strokeWidth={2}
        />
      </g>
    );
  };

  const handleRefreshRecords = async () => {
    try {
      // Determine date range based on current filter
      let refreshStartDate = startDate;
      let refreshEndDate = endDate;

      if (filterType === 'today') {
        const today = getTodayDate();
        refreshStartDate = today;
        refreshEndDate = today;
      } else if (filterType === 'week') {
        const { start, end } = getWeekDates(saleDate);
        refreshStartDate = start;
        refreshEndDate = end;
      } else if (filterType === 'month') {
        const { start, end } = getMonthDates(saleDate);
        refreshStartDate = start;
        refreshEndDate = end;
      } else if (filterType === 'last7days' || filterType === 'last15days' || filterType === 'last30days' || filterType === 'last90days') {
        const daysMap = {
          'last7days': 7,
          'last15days': 15,
          'last30days': 30,
          'last90days': 90
        };
        const { start, end } = getLastXDays(daysMap[filterType]);
        refreshStartDate = start;
        refreshEndDate = end;
      }

      setRefreshing(true);

      const api = await import('../api');
      const { notifySuccess } = await import('../utils/toast');
      const response = await api.salesAPI.refreshDailySalesRecords(refreshStartDate, refreshEndDate);

      notifySuccess(`✅ Refreshed ${response.data.refreshedCount || 0} records for all salesmen (${refreshStartDate} to ${refreshEndDate})`);

      // Reload summaries
      fetchSummaries();
    } catch (error) {
      console.error('Error refreshing records:', error);
      const { notifyError } = await import('../utils/toast');
      notifyError('Failed to refresh records: ' + (error.response?.data?.message || error.message));
    } finally {
      setRefreshing(false);
    }
  };

  return (
    <div className="dsr-page">
      <h2 className="dsr-header">Daily Summary Report</h2>
      <div className="dsr-controls">
        <select
          value={salesmanAlias}
          onChange={e => setSalesmanAlias(e.target.value)}
          className="dsr-select"
        >
          <option value="">No Salesman (Daily Total)</option>
          <option value={ALL_SALESMEN_VALUE}>All Salesmen</option>
          {salesmen.map((alias) => (
            <option key={alias} value={alias}>
              {alias}
            </option>
          ))}
        </select>

        <div className="dsr-filter-buttons">
          <button
            onClick={() => setFilterType('today')}
            className={`dsr-filter-btn ${filterType === 'today' ? 'is-active' : ''}`}
          >
            Today
          </button>
          <button
            onClick={() => setFilterType('week')}
            className={`dsr-filter-btn ${filterType === 'week' ? 'is-active' : ''}`}
          >
            This Week
          </button>
          <button
            onClick={() => setFilterType('month')}
            className={`dsr-filter-btn ${filterType === 'month' ? 'is-active' : ''}`}
          >
            This Month
          </button>

          <div className="dsr-dropdown-container">
            <select
              value={filterType.startsWith('last') && filterType.includes('days') ? filterType : ''}
              onChange={(e) => e.target.value && setFilterType(e.target.value)}
              className={`dsr-filter-btn dsr-dropdown-btn ${(filterType.startsWith('last') && filterType.includes('days')) ? 'is-active' : ''}`}
            >
              <option value="">Last X Days ▼</option>
              <option value="last7days">Last 7 Days</option>
              <option value="last15days">Last 15 Days</option>
              <option value="last30days">Last 30 Days</option>
              <option value="last90days">Last 90 Days</option>
            </select>
          </div>

          <button
            onClick={() => setFilterType('dateRange')}
            className={`dsr-filter-btn ${filterType === 'dateRange' ? 'is-active' : ''}`}
          >
            Date Range
          </button>
        </div>

        {(filterType === 'week' || filterType === 'month') && (
          <input
            type="date"
            value={saleDate}
            onChange={e => setSaleDate(e.target.value)}
            className="dsr-date-input"
          />
        )}

        {filterType === 'dateRange' && (
          <>
            <input
              type="date"
              value={startDate}
              onChange={e => setStartDate(e.target.value)}
              className="dsr-date-input"
            />
            <span>to</span>
            <input
              type="date"
              value={endDate}
              onChange={e => setEndDate(e.target.value)}
              className="dsr-date-input"
            />
          </>
        )}

        <button onClick={fetchSummaries} disabled={loading} className="dsr-action-btn">
          Refresh
        </button>
        <button
          onClick={handleRefreshRecords}
          disabled={refreshing}
          className="dsr-action-btn dsr-refresh-records-btn"
          title="Refresh daily_sale_record for all salesmen in date range"
        >
          {refreshing ? '⟳ Refreshing...' : '⟳ Refresh Records'}
        </button>
        <div className="dsr-view-toggle" role="group" aria-label="View mode">
          <button
            onClick={() => setViewMode('table')}
            className={`dsr-view-btn ${viewMode === 'table' ? 'is-active' : ''}`}
          >
            Table
          </button>
          <button
            onClick={() => setViewMode('graph')}
            className={`dsr-view-btn ${viewMode === 'graph' ? 'is-active' : ''}`}
          >
            Graph
          </button>
        </div>
        {viewMode === 'table' && (
          <button 
            onClick={() => setShowProfitColumns(!showProfitColumns)}
            className={`dsr-profit-toggle ${showProfitColumns ? 'is-active' : ''}`}
          >
            {showProfitColumns ? 'Hide Profit' : 'Show Profit'}
          </button>
        )}
      </div>

      {viewMode === 'table' && (
      <table className="dsr-table">
        <thead>
          <tr className="dsr-thead-row">
            <th className="dsr-th" onClick={() => handleSort('date')} style={{ cursor: 'pointer' }}>
              Sale Date{getSortIndicator('date')}
            </th>
            {showSalesmanDetails && <th className="dsr-th">Salesman Alias</th>}
            <th className="dsr-th">Total Quantity</th>
            <th className="dsr-th" onClick={() => handleSort('volume')} style={{ cursor: 'pointer' }}>
              Volume Sold (L){getSortIndicator('volume')}
            </th>
            <th className="dsr-th">Material Cost</th>
            <th className="dsr-th">Total Expense</th>
            <th className="dsr-th" onClick={() => handleSort('revenue')} style={{ cursor: 'pointer' }}>
              Total Revenue{getSortIndicator('revenue')}
            </th>
            <th className="dsr-th" onClick={() => handleSort('ac')} style={{ cursor: 'pointer' }}>
              Agent Commission{getSortIndicator('ac')}
            </th>
            <th className="dsr-th">A.C./Unit</th>
            {showProfitColumns && (
              <th className="dsr-th" onClick={() => handleSort('netProfit')} style={{ cursor: 'pointer' }}>
                Net Profit{getSortIndicator('netProfit')}
              </th>
            )}
            {showProfitColumns && <th className="dsr-th">Net Profit/Unit</th>}
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr><td colSpan={visibleColumnCount} className="dsr-td dsr-center">Loading...</td></tr>
          ) : groupedByDate.length === 0 ? (
            <tr><td colSpan={visibleColumnCount} className="dsr-td dsr-center">No data found</td></tr>
          ) : (
            groupedByDate.flatMap(({ date, rows }) => {
              if (!showSalesmanDetails) {
                const dateTotals = rows.reduce(
                  (acc, s) => {
                    acc.totalQuantity += Number(s.totalQuantity) || 0;
                    acc.volumeSold += Number(s.volumeSold) || 0;
                    acc.materialCost += Number(s.materialCost) || 0;
                    acc.totalExpense += Number(s.totalExpense) || 0;
                    acc.totalRevenue += Number(s.totalRevenue) || 0;
                    acc.totalAgentCommission += Number(s.totalAgentCommission) || 0;
                    acc.netProfit += Number(s.netProfit) || 0;
                    return acc;
                  },
                  {
                    totalQuantity: 0,
                    volumeSold: 0,
                    materialCost: 0,
                    totalExpense: 0,
                    totalRevenue: 0,
                    totalAgentCommission: 0,
                    netProfit: 0,
                  }
                );

                const totalAcPerUnit = dateTotals.totalQuantity > 0
                  ? (dateTotals.totalAgentCommission / dateTotals.totalQuantity).toFixed(2)
                  : '0.00';
                const totalNetProfitPerUnit = dateTotals.totalQuantity > 0
                  ? (dateTotals.netProfit / dateTotals.totalQuantity).toFixed(2)
                  : '0.00';

                return [
                  <tr key={`${date}-daily-total`} className="dsr-date-total-row">
                    <td className="dsr-td"><strong>{date}</strong></td>
                    <td className="dsr-td"><strong>{dateTotals.totalQuantity}</strong></td>
                    <td className="dsr-td"><strong>{dateTotals.volumeSold.toFixed(2)}</strong></td>
                    <td className="dsr-td"><strong>{dateTotals.materialCost.toFixed(2)}</strong></td>
                    <td className="dsr-td"><strong>{dateTotals.totalExpense.toFixed(2)}</strong></td>
                    <td className="dsr-td"><strong>{dateTotals.totalRevenue.toFixed(2)}</strong></td>
                    <td className="dsr-td"><strong>{dateTotals.totalAgentCommission.toFixed(2)}</strong></td>
                    <td className="dsr-td"><strong>{totalAcPerUnit}</strong></td>
                    {showProfitColumns && <td className="dsr-td"><strong>{dateTotals.netProfit.toFixed(2)}</strong></td>}
                    {showProfitColumns && <td className="dsr-td"><strong>{totalNetProfitPerUnit}</strong></td>}
                  </tr>
                ];
              }

              const hasMultipleRows = rows.length > 1;
              const dateTotals = rows.reduce(
                (acc, s) => {
                  acc.totalQuantity += Number(s.totalQuantity) || 0;
                  acc.volumeSold += Number(s.volumeSold) || 0;
                  acc.materialCost += Number(s.materialCost) || 0;
                  acc.totalExpense += Number(s.totalExpense) || 0;
                  acc.totalRevenue += Number(s.totalRevenue) || 0;
                  acc.totalAgentCommission += Number(s.totalAgentCommission) || 0;
                  acc.netProfit += Number(s.netProfit) || 0;
                  return acc;
                },
                {
                  totalQuantity: 0,
                  volumeSold: 0,
                  materialCost: 0,
                  totalExpense: 0,
                  totalRevenue: 0,
                  totalAgentCommission: 0,
                  netProfit: 0,
                }
              );

              const detailRows = rows.map((s, idx) => {
                const acPerUnit = s.totalQuantity > 0 ? (s.totalAgentCommission / s.totalQuantity).toFixed(2) : '0.00';
                const netProfitPerUnit = s.totalQuantity > 0 ? (s.netProfit / s.totalQuantity).toFixed(2) : '0.00';
                const rowKey = `${date}-${s.salesmanAlias || 'unknown'}-${idx}`;

                return (
                  <tr key={rowKey}>
                    {idx === 0 && (
                      <td className="dsr-td" rowSpan={rows.length + (hasMultipleRows ? 1 : 0)} style={{ fontWeight: 600, verticalAlign: 'middle' }}>
                        {date}
                      </td>
                    )}
                    <td className="dsr-td">{s.salesmanAlias}</td>
                    <td className="dsr-td">{s.totalQuantity || 0}</td>
                    <td className="dsr-td">{s.volumeSold ? Number(s.volumeSold).toFixed(2) : '0.00'}</td>
                    <td className="dsr-td">{s.materialCost}</td>
                    <td className="dsr-td">{s.totalExpense}</td>
                    <td className="dsr-td">{s.totalRevenue}</td>
                    <td className="dsr-td">{s.totalAgentCommission}</td>
                    <td className="dsr-td">{acPerUnit}</td>
                    {showProfitColumns && <td className="dsr-td">{s.netProfit}</td>}
                    {showProfitColumns && <td className="dsr-td">{netProfitPerUnit}</td>}
                  </tr>
                );
              });

              const totalAcPerUnit = dateTotals.totalQuantity > 0
                ? (dateTotals.totalAgentCommission / dateTotals.totalQuantity).toFixed(2)
                : '0.00';
              const totalNetProfitPerUnit = dateTotals.totalQuantity > 0
                ? (dateTotals.netProfit / dateTotals.totalQuantity).toFixed(2)
                : '0.00';

              const summaryRow = (
                <tr key={`${date}-summary`} className="dsr-date-total-row">
                  <td className="dsr-td"><strong>Date Total</strong></td>
                  <td className="dsr-td"><strong>{dateTotals.totalQuantity}</strong></td>
                  <td className="dsr-td"><strong>{dateTotals.volumeSold.toFixed(2)}</strong></td>
                  <td className="dsr-td"><strong>{dateTotals.materialCost.toFixed(2)}</strong></td>
                  <td className="dsr-td"><strong>{dateTotals.totalExpense.toFixed(2)}</strong></td>
                  <td className="dsr-td"><strong>{dateTotals.totalRevenue.toFixed(2)}</strong></td>
                  <td className="dsr-td"><strong>{dateTotals.totalAgentCommission.toFixed(2)}</strong></td>
                  <td className="dsr-td"><strong>{totalAcPerUnit}</strong></td>
                  {showProfitColumns && <td className="dsr-td"><strong>{dateTotals.netProfit.toFixed(2)}</strong></td>}
                  {showProfitColumns && <td className="dsr-td"><strong>{totalNetProfitPerUnit}</strong></td>}
                </tr>
              );

              return hasMultipleRows ? [...detailRows, summaryRow] : detailRows;
            })
          )}
        </tbody>
        {summaries.length > 0 && (
          <tfoot>
            <tr className="dsr-tfoot-row">
              <td colSpan={showSalesmanDetails ? 2 : 1} className="dsr-td dsr-right">Total:</td>
              <td className="dsr-td">{totals.totalQuantity}</td>
              <td className="dsr-td">{totals.volumeSold.toFixed(2)} L</td>
              <td className="dsr-td">{totals.materialCost.toFixed(2)}</td>
              <td className="dsr-td">{totals.totalExpense.toFixed(2)}</td>
              <td className="dsr-td">{totals.totalRevenue.toFixed(2)}</td>
              <td className="dsr-td">{totals.totalAgentCommission.toFixed(2)}</td>
              <td className="dsr-td">{totals.totalQuantity > 0 ? (totals.totalAgentCommission / totals.totalQuantity).toFixed(2) : '0.00'}</td>
              {showProfitColumns && <td className="dsr-td">{totals.netProfit.toFixed(2)}</td>}
              {showProfitColumns && <td className="dsr-td">{totals.totalQuantity > 0 ? (totals.netProfit / totals.totalQuantity).toFixed(2) : '0.00'}</td>}
            </tr>
          </tfoot>
        )}
      </table>
      )}

      {/* Charts container - all three side by side */}
      {viewMode === 'graph' && summaries.length > 0 && (
        <div className="dsr-charts">
          {/* Revenue & Net Profit Trend Chart */}
          <div className="dsr-chart-card">
            <h3 className="dsr-chart-title">Revenue & Net Profit Trend</h3>
            {chartData.length === 0 ? (
              <div className="dsr-empty">
                No data available
              </div>
            ) : (
              <ResponsiveContainer width="100%" height={280}>
                <LineChart data={chartData} margin={{ top: 5, right: 10, left: 0, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis 
                    dataKey="date" 
                  />
                  <YAxis 
                    label={{ value: '₹', angle: -90, position: 'insideLeft', offset: 5 }}
                  />
                  <Tooltip 
                    formatter={(value) => `₹${value.toFixed(2)}`}
                    labelFormatter={(label) => `Date: ${label}`}
                  />
                  <Legend />
                  <Line 
                    type="monotone" 
                    dataKey="revenue" 
                    stroke="#4CAF50" 
                    strokeWidth={1.5}
                    dot={{ fill: '#4CAF50', r: 3 }}
                    activeDot={{ r: 5 }}
                    name="Revenue"
                  />
                  <Line 
                    type="monotone" 
                    dataKey="netProfit" 
                    stroke="#2196F3" 
                    strokeWidth={1.5}
                    dot={{ fill: '#2196F3', r: 3 }}
                    activeDot={{ r: 5 }}
                    name="Net Profit"
                  />
                </LineChart>
              </ResponsiveContainer>
            )}
          </div>

          {/* Volume & Quantity Trend Chart */}
          <div className="dsr-chart-card">
            <h3 className="dsr-chart-title">Volume & Quantity Sold Trend</h3>
            {volumeChartData.length === 0 ? (
              <div className="dsr-empty">
                No data available
              </div>
            ) : (
              <ResponsiveContainer width="100%" height={280}>
                <LineChart data={volumeChartData} margin={{ top: 5, right: 10, left: 0, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis 
                    dataKey="date" 
                  />
                  <YAxis 
                    label={{ value: 'Units / Liters', angle: -90, position: 'insideLeft', offset: 5 }}
                  />
                  <Tooltip 
                    formatter={(value, name) => {
                      if (name === 'Volume Sold') return `${value.toFixed(2)} L`;
                      if (name === 'Quantity Sold') return `${value.toFixed(2)} units`;
                      return value;
                    }}
                    labelFormatter={(label) => `Date: ${label}`}
                  />
                  <Legend />
                  <Line 
                    type="monotone" 
                    dataKey="volumeSold" 
                    stroke="#FF9800" 
                    strokeWidth={1.5}
                    dot={{ fill: '#FF9800', r: 3 }}
                    activeDot={{ r: 5 }}
                    name="Volume Sold"
                  />
                  <Line 
                    type="monotone" 
                    dataKey="quantity" 
                    stroke="#9C27B0" 
                    strokeWidth={1.5}
                    dot={{ fill: '#9C27B0', r: 3 }}
                    activeDot={{ r: 5 }}
                    name="Quantity Sold"
                  />
                </LineChart>
              </ResponsiveContainer>
            )}
          </div>

          {/* Revenue Breakdown Pie Chart */}
          {summaries.length > 0 && totals.totalRevenue > 0 && (
            <div className="dsr-chart-card">
              <h3 className="dsr-chart-title">Revenue Breakdown</h3>
              <ResponsiveContainer width="100%" height={280}>
                <PieChart>
                  <Pie
                    data={pieChartData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                    outerRadius={78}
                    innerRadius={0}
                    paddingAngle={2}
                    fill="#f2c57c"
                    dataKey="value"
                    isAnimationActive={true}
                    animationBegin={150}
                    animationDuration={1400}
                    animationEasing="ease-out"
                    activeIndex={activeSliceIndex === null ? -1 : activeSliceIndex}
                    activeShape={renderActiveSlice}
                    onMouseEnter={(_, index) => setActiveSliceIndex(index)}
                    onMouseLeave={() => setActiveSliceIndex(null)}
                >
                  {pieChartData.map((entry, index) => {
                    const COLORS = ['#e76f51', '#f4a261', '#2a9d8f', '#e63946'];
                    return (
                      <Cell
                        key={`cell-${index}`}
                        fill={COLORS[index % COLORS.length]}
                        stroke="#d19a4a"
                        strokeWidth={2}
                      />
                    );
                  })}
                </Pie>
                <Tooltip formatter={(value) => `₹${value.toFixed(2)}`} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>
      )}
      {viewMode === 'graph' && summaries.length === 0 && !loading && (
        <div className="dsr-empty">No data available for charts</div>
      )}
    </div>
  );
};

export default DailySummaryReport;
