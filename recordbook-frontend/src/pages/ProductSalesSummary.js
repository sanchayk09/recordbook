import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { PieChart, Pie, Cell, Sector, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { productSalesSummaryAPI, salesmanAPI, expenseAPI, summaryAPI } from '../api';
import { notifyError, notifySuccess } from '../utils/toast';
import { getTodayDate, getCurrentMonth } from '../utils/dateUtils';
import '../styles/ProductSalesSummary.css';

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
  const [activeQuantitySliceIndex, setActiveQuantitySliceIndex] = useState(null);
  const [activeRevenueSliceIndex, setActiveRevenueSliceIndex] = useState(null);

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
          response = await productSalesSummaryAPI.getTodayProductSales();
          break;
        case 'date':
          response = await productSalesSummaryAPI.getProductSalesByDate(selectedDate);
          break;
        case 'month':
          const [year, month] = selectedMonth.split('-');
          response = await productSalesSummaryAPI.getProductSalesByMonth(year, month);
          break;
        case 'range':
          response = await productSalesSummaryAPI.getProductSalesByRange(startDate, endDate);
          break;
        case 'all-time':
          response = await productSalesSummaryAPI.getAllProductSales();
          break;
        default:
          response = await productSalesSummaryAPI.getTodayProductSales();
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
      const res = await salesmanAPI.getAliases();
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
      const res = await expenseAPI.getByDate(selectedSalesman, expenseDate);
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
      volumeSold: productSales.reduce((acc, rec) => acc + (Number(rec.volumeSold) || 0), 0),
      totalQuantity: productSales.reduce((acc, rec) => acc + (Number(rec.totalQuantity) || 0), 0),
    };
  };

  const handleSubmitSummary = async () => {
    const payload = getSummaryPayload();
    setSubmitLoading(true);
    try {
      await summaryAPI.submit(payload);
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

  const pieChartData = useMemo(() => {
    return productSales
      .map(record => ({
        name: record.productCode,
        value: Number(record.totalQuantity) || 0
      }))
      .filter(item => item.value > 0)
      .sort((a, b) => b.value - a.value);
  }, [productSales]);

  const revenueChartData = useMemo(() => {
    return productSales
      .map(record => ({
        name: record.productCode,
        value: Number(record.totalRevenue) || 0
      }))
      .filter(item => item.value > 0)
      .sort((a, b) => b.value - a.value);
  }, [productSales]);

  const productColorMap = useMemo(() => {
    const COLORS = ['#4CAF50', '#2196F3', '#FF9800', '#9C27B0', '#F44336', '#00BCD4', '#FFEB3B', '#795548', '#607D8B', '#E91E63'];
    const colorMap = {};
    productSales.forEach((record, index) => {
      if (!colorMap[record.productCode]) {
        colorMap[record.productCode] = COLORS[Object.keys(colorMap).length % COLORS.length];
      }
    });
    return colorMap;
  }, [productSales]);

  return (
    <div className="pss-page">
      <div className="pss-header">
        <h2 className="pss-title">Product Sales Summary</h2>
        <button
          type="button"
          onClick={() => navigate('/daily-sales')}
          className="pss-back-btn"
        >
          Back to Daily Sales
        </button>
      </div>

      {/* Date Filter */}
      <div className="pss-filter-box">
        <div className="pss-filter-buttons">
          <button
            onClick={() => setFilterType('today')}
            className={`pss-filter-btn ${filterType === 'today' ? 'is-active' : ''}`}
          >
            Today
          </button>

          <button
            onClick={() => setFilterType('month')}
            className={`pss-filter-btn ${filterType === 'month' ? 'is-active' : ''}`}
          >
            This Month
          </button>

          <button
            onClick={() => setFilterType('date')}
            className={`pss-filter-btn ${filterType === 'date' ? 'is-active' : ''}`}
          >
            Specific Date
          </button>

          <button
            onClick={() => setFilterType('range')}
            className={`pss-filter-btn ${filterType === 'range' ? 'is-active' : ''}`}
          >
            Date Range
          </button>

          <button
            onClick={() => setFilterType('all-time')}
            className={`pss-filter-btn ${filterType === 'all-time' ? 'is-active' : ''}`}
          >
            All Time
          </button>
        </div>

        {/* Filter input based on selected filter type */}
        <div className="pss-filter-row">
          {filterType === 'date' && (
            <>
              <input
                type="date"
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                className="pss-date-input"
              />
            </>
          )}

          {filterType === 'month' && (
            <>
              <input
                type="month"
                value={selectedMonth}
                onChange={(e) => setSelectedMonth(e.target.value)}
                className="pss-month-input"
              />
            </>
          )}

          {filterType === 'range' && (
            <>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="pss-date-input"
              />
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="pss-date-input"
              />
            </>
          )}

          <span className="pss-total-text">
            Total: <strong>{productSales.length}</strong> product{productSales.length !== 1 ? 's' : ''}
          </span>
        </div>
      </div>

      {loading ? (
        <div className="pss-loading">
          Loading product sales data...
        </div>
      ) : (
        <div className="pss-table-wrap">
          <table className="pss-table">
            <thead>
              <tr className="pss-thead-row">
                <th className="pss-th">Rank</th>
                <th className="pss-th">Product Code</th>
                <th className="pss-th">Total Quantity</th>
                <th className="pss-th">Volume Sold</th>
                <th className="pss-th">Total Revenue</th>
                <th className="pss-th">Avg Revenue</th>
                <th className="pss-th">Cost/Unit</th>
                <th className="pss-th">Total material cost</th>
                <th className="pss-th">Op Cost</th>
                <th className="pss-th">Salesman Comm</th>
                <th className="pss-th">Net Profit</th>
                <th className="pss-th">Avg Net Profit</th>
              </tr>
            </thead>
            <tbody>
              {productSales.length === 0 ? (
                <tr>
                  <td colSpan="12" className="pss-td pss-td-center">
                    No product sales data found.
                  </td>
                </tr>
              ) : (
                productSales.map((record, index) => (
                  <tr key={index}>
                    <td className="pss-td">{index + 1}</td>
                    <td className="pss-td pss-td-left">{record.productCode}</td>
                    <td className="pss-td">{record.totalQuantity}</td>
                    <td className="pss-td">{record.volumeSold !== undefined ? Number(record.volumeSold).toFixed(2) : '-'}</td>
                    <td className="pss-td">{record.totalRevenue !== undefined ? Number(record.totalRevenue).toFixed(2) : '-'}</td>
                    <td className="pss-td">{record.averageRevenue !== undefined ? Number(record.averageRevenue).toFixed(2) : '-'}</td>
                    <td className="pss-td">{record.productCost !== undefined ? Number(record.productCost).toFixed(2) : '-'}</td>
                    <td className="pss-td">{record.totalCost !== undefined ? Number(record.totalCost).toFixed(2) : '-'}</td>
                    <td className="pss-td">{record.opCost !== undefined ? Number(record.opCost).toFixed(2) : '-'}</td>
                    <td className="pss-td">{record.salesmanCommission !== undefined ? Number(record.salesmanCommission).toFixed(2) : '-'}</td>
                    <td className="pss-td">{record.netProfit !== undefined ? Number(record.netProfit).toFixed(2) : '-'}</td>
                    <td className="pss-td">{record.avgNetProfit !== undefined ? Number(record.avgNetProfit).toFixed(2) : '-'}</td>
                  </tr>
                ))
              )}
            </tbody>
            {productSales.length > 0 && (
              <tfoot>
                <tr className="pss-tfoot-row">
                  <td colSpan="2" className="pss-td pss-td-right">
                    Total:
                  </td>
                  <td className="pss-td">
                    {productSales.reduce((acc, rec) => acc + (Number(rec.totalQuantity) || 0), 0)}
                  </td>
                  <td className="pss-td">
                    {productSales.reduce((acc, rec) => acc + (Number(rec.volumeSold) || 0), 0).toFixed(2)}
                  </td>
                  <td className="pss-td">
                    {productSales.reduce((acc, rec) => acc + (Number(rec.totalRevenue) || 0), 0).toFixed(2)}
                  </td>
                  <td className="pss-td"></td>
                  <td className="pss-td"></td>
                  <td className="pss-td">
                    {productSales.reduce((acc, rec) => acc + (Number(rec.totalCost) || 0), 0).toFixed(2)}
                  </td>
                  <td className="pss-td">
                    {productSales.reduce((acc, rec) => acc + (Number(rec.opCost) || 0), 0).toFixed(2)}
                  </td>
                  <td className="pss-td">
                    {productSales.reduce((acc, rec) => acc + (Number(rec.salesmanCommission) || 0), 0).toFixed(2)}
                  </td>
                  <td className="pss-td">
                    {productSales.reduce((acc, rec) => acc + (Number(rec.netProfit) || 0), 0).toFixed(2)}
                  </td>
                  <td className="pss-td"></td>
                </tr>
              </tfoot>
            )}
          </table>
        </div>
      )}

      {/* Pie Charts for Product Sales & Revenue Distribution */}
      {productSales.length > 0 && (
        <div className="pss-chart-row">
          <div className="pss-chart-card">
            <h3 className="pss-chart-title">Sales by Quantity</h3>
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie
                  data={pieChartData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                  outerRadius={70}
                  innerRadius={0}
                  paddingAngle={2}
                  fill="#f2c57c"
                  dataKey="value"
                  isAnimationActive={true}
                  animationBegin={150}
                  animationDuration={1200}
                  animationEasing="ease-out"
                  activeIndex={activeQuantitySliceIndex === null ? -1 : activeQuantitySliceIndex}
                  activeShape={renderActiveSlice}
                  onMouseEnter={(_, index) => setActiveQuantitySliceIndex(index)}
                  onMouseLeave={() => setActiveQuantitySliceIndex(null)}
                >
                  {pieChartData.map((entry, index) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={productColorMap[entry.name]}
                      stroke="#d19a4a"
                      strokeWidth={2}
                    />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => `${value} units`} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>

          <div className="pss-chart-card">
            <h3 className="pss-chart-title">Revenue by Product</h3>
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie
                  data={revenueChartData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                  outerRadius={70}
                  innerRadius={0}
                  paddingAngle={2}
                  fill="#f2c57c"
                  dataKey="value"
                  isAnimationActive={true}
                  animationBegin={150}
                  animationDuration={1200}
                  animationEasing="ease-out"
                  activeIndex={activeRevenueSliceIndex === null ? -1 : activeRevenueSliceIndex}
                  activeShape={renderActiveSlice}
                  onMouseEnter={(_, index) => setActiveRevenueSliceIndex(index)}
                  onMouseLeave={() => setActiveRevenueSliceIndex(null)}
                >
                  {revenueChartData.map((entry, index) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={productColorMap[entry.name]}
                      stroke="#d19a4a"
                      strokeWidth={2}
                    />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => `₹${value.toFixed(2)}`} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      <hr className="pss-divider" />
      <h3>Show Daily Expense for Salesman</h3>
      <div className="pss-expense-row">
        <label className="pss-label">Salesman:</label>
        <select value={selectedSalesman} onChange={e => setSelectedSalesman(e.target.value)} className="pss-select">
          {salesmen.map(alias => (
            <option key={alias} value={alias}>{alias}</option>
          ))}
        </select>
        <label className="pss-label">Date:</label>
        <input type="date" value={expenseDate} onChange={e => setExpenseDate(e.target.value)} className="pss-select" />
        <button onClick={() => setExpenseDate(getTodayDate())} className="pss-expense-btn secondary">Today</button>
        <button onClick={fetchExpenseDetail} className="pss-expense-btn primary">Show Expense</button>
      </div>
      {expenseLoading ? (
        <div>Loading expense...</div>
      ) : expenseDetail ? (
        <div className="pss-expense-block">
          <table className="pss-expense-table">
            <thead>
              <tr className="pss-expense-head">
                <th className="pss-th">Salesman</th>
                <th className="pss-th">Date</th>
                <th className="pss-th">Total Expense</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td className="pss-td">{expenseDetail.salesmanAlias}</td>
                <td className="pss-td">{expenseDetail.expenseDate}</td>
                <td className="pss-td">{expenseDetail.totalExpense}</td>
              </tr>
            </tbody>
          </table>
          <button
            onClick={handleSubmitSummary}
            disabled={submitLoading}
            className="pss-submit-btn"
          >
            {submitLoading ? 'Submitting...' : 'Submit Summary'}
          </button>
        </div>
      ) : (
        <div className="pss-expense-empty">No expense found for selected salesman and date.</div>
      )}
    </div>
  );
};

export default ProductSalesSummary;
