import React, { useState, useEffect, useMemo } from 'react';
import { LineChart, Line, PieChart, Pie, Cell, Sector, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { notifyError } from '../utils/toast';
import { getTodayDate } from '../utils/dateUtils';
import '../styles/DailySummaryReport.css';

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
  const [filterType, setFilterType] = useState('today'); // today, week, month, dateRange
  const [saleDate, setSaleDate] = useState(getTodayDate());
  const [startDate, setStartDate] = useState(getTodayDate());
  const [endDate, setEndDate] = useState(getTodayDate());
  const [showProfitColumns, setShowProfitColumns] = useState(false);
  const [activeSliceIndex, setActiveSliceIndex] = useState(null);

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

  // Fetch summaries using axios (api.js)
  const fetchSummaries = async () => {
    setLoading(true);
    try {
      let url = '/api/summary/all';

      if (filterType === 'today') {
        if (salesmanAlias) {
          url = `/api/summary/by-salesman-date?alias=${encodeURIComponent(salesmanAlias)}&date=${getTodayDate()}`;
        } else {
          url = `/api/summary/date?date=${getTodayDate()}`;
        }
      } else if (filterType === 'week') {
        const { start, end } = getWeekDates(saleDate);
        if (salesmanAlias) {
          url = `/api/summary/salesman-range?alias=${encodeURIComponent(salesmanAlias)}&startDate=${start}&endDate=${end}`;
        } else {
          url = `/api/summary/range?startDate=${start}&endDate=${end}`;
        }
      } else if (filterType === 'month') {
        const { start, end } = getMonthDates(saleDate);
        if (salesmanAlias) {
          url = `/api/summary/salesman-range?alias=${encodeURIComponent(salesmanAlias)}&startDate=${start}&endDate=${end}`;
        } else {
          url = `/api/summary/range?startDate=${start}&endDate=${end}`;
        }
      } else if (filterType === 'dateRange') {
        if (salesmanAlias) {
          url = `/api/summary/salesman-range?alias=${encodeURIComponent(salesmanAlias)}&startDate=${startDate}&endDate=${endDate}`;
        } else {
          url = `/api/summary/range?startDate=${startDate}&endDate=${endDate}`;
        }
      }

      const res = await (await import('../api')).default.get(url);
      const data = res.data;
      setSummaries(Array.isArray(data) ? data : (data ? [data] : []));
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
  }, [filterType, salesmanAlias]);

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

  return (
    <div className="dsr-page">
      <h2 className="dsr-header">Daily Summary Report</h2>
      <div className="dsr-controls">
        <select
          value={salesmanAlias}
          onChange={e => setSalesmanAlias(e.target.value)}
          className="dsr-select"
        >
          <option value="">All Salesmen</option>
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
          onClick={() => setShowProfitColumns(!showProfitColumns)}
          className={`dsr-profit-toggle ${showProfitColumns ? 'is-active' : ''}`}
        >
          {showProfitColumns ? 'Hide Profit' : 'Show Profit'}
        </button>
      </div>

      <table className="dsr-table">
        <thead>
          <tr className="dsr-thead-row">
            <th className="dsr-th">Salesman Alias</th>
            <th className="dsr-th">Sale Date</th>
            <th className="dsr-th">Total Quantity</th>
            <th className="dsr-th">Volume Sold (L)</th>
            <th className="dsr-th">Material Cost</th>
            <th className="dsr-th">Total Expense</th>
            <th className="dsr-th">Total Revenue</th>
            <th className="dsr-th">Agent Commission</th>
            <th className="dsr-th">A.C./Unit</th>
            {showProfitColumns && <th className="dsr-th">Net Profit</th>}
            {showProfitColumns && <th className="dsr-th">Net Profit/Unit</th>}
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr><td colSpan={9} className="dsr-td dsr-center">Loading...</td></tr>
          ) : summaries.length === 0 ? (
            <tr><td colSpan={9} className="dsr-td dsr-center">No data found</td></tr>
          ) : (
            summaries.map((s, idx) => {
              const acPerUnit = s.totalQuantity > 0 ? (s.totalAgentCommission / s.totalQuantity).toFixed(2) : '0.00';
              const netProfitPerUnit = s.totalQuantity > 0 ? (s.netProfit / s.totalQuantity).toFixed(2) : '0.00';
              return (
              <tr key={idx}>
                <td className="dsr-td">{s.salesmanAlias}</td>
                <td className="dsr-td">{s.saleDate}</td>
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
            })
          )}
        </tbody>
        {summaries.length > 0 && (
          <tfoot>
            <tr className="dsr-tfoot-row">
              <td colSpan={2} className="dsr-td dsr-right">Total:</td>
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

      {/* Charts container - all three side by side */}
      {['week', 'month', 'dateRange'].includes(filterType) && (
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
    </div>
  );
};

export default DailySummaryReport;
