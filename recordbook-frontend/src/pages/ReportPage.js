import React, { useState, useEffect, useMemo } from 'react';
import api from '../api';
import { notifyError } from '../utils/toast';

// ─── Date helpers ─────────────────────────────────────────────────────────────
function todayStr() {
  return new Date().toISOString().split('T')[0];
}

function daysAgoStr(n) {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d.toISOString().split('T')[0];
}

function getMondayOfWeek(dateStr) {
  const d = new Date(dateStr + 'T00:00:00');
  const dow = d.getDay();
  const diff = dow === 0 ? -6 : 1 - dow;
  d.setDate(d.getDate() + diff);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function formatDateShort(dateStr) {
  if (!dateStr) return '';
  const [, m, d] = dateStr.split('-');
  return `${d}/${m}`;
}

function formatCurrency(val) {
  const n = Number(val) || 0;
  if (n >= 100000) return `₹${(n / 100000).toFixed(1)}L`;
  if (n >= 1000) return `₹${(n / 1000).toFixed(1)}k`;
  return `₹${n.toFixed(0)}`;
}

// ─── SVG single-series bar chart ──────────────────────────────────────────────
function BarChart({ data, barColor = '#16a34a', height = 260 }) {
  // data: [{ label: string, value: number }]
  if (!data || data.length === 0) {
    return (
      <p style={{ textAlign: 'center', color: '#aaa', padding: '28px 0', margin: 0, fontSize: '14px' }}>
        No data for the selected period
      </p>
    );
  }

  const LM = 72, BM = 60, TM = 10, RM = 20;
  const slotW = Math.max(Math.floor(620 / data.length), 52);
  const barW = Math.max(slotW - 14, 22);
  const chartW = data.length * slotW + LM + RM;
  const innerH = height - TM - BM;
  const maxVal = Math.max(...data.map(d => Math.abs(Number(d.value) || 0)), 1);
  const TICKS = 5;
  const tickStep = maxVal / TICKS;

  return (
    <div style={{ overflowX: 'auto', overflowY: 'hidden' }}>
      <svg
        role="img"
        aria-label={`Bar chart showing values for ${data.map(d => d.label).join(', ')}`}
        width={Math.max(chartW, 320)}
        height={height}
        style={{ fontFamily: 'Calibri, sans-serif', display: 'block' }}
      >
        {/* Grid lines + Y-axis labels */}
        {Array.from({ length: TICKS + 1 }, (_, i) => {
          const v = tickStep * i;
          const y = TM + innerH - (innerH * i) / TICKS;
          return (
            <g key={i}>
              <line x1={LM} y1={y} x2={chartW - RM} y2={y} stroke="#e5e7eb" strokeWidth="1" />
              <text x={LM - 6} y={y + 4} textAnchor="end" fontSize="11" fill="#6b7280">
                {formatCurrency(v)}
              </text>
            </g>
          );
        })}

        {/* Axes */}
        <line x1={LM} y1={TM} x2={LM} y2={TM + innerH} stroke="#d1d5db" strokeWidth="1.5" />
        <line x1={LM} y1={TM + innerH} x2={chartW - RM} y2={TM + innerH} stroke="#d1d5db" strokeWidth="1.5" />

        {/* Bars + X labels */}
        {data.map((d, i) => {
          const v = Number(d.value) || 0;
          const bH = Math.max((Math.abs(v) / maxVal) * innerH, v !== 0 ? 2 : 0);
          const x = LM + i * slotW + (slotW - barW) / 2;
          const y = TM + innerH - bH;
          const cx = LM + i * slotW + slotW / 2;
          const ly = TM + innerH + 14;
          return (
            <g key={i}>
              <rect x={x} y={y} width={barW} height={bH} fill={barColor} rx="3" opacity="0.88">
                <title>{`${d.label}: ${formatCurrency(v)}`}</title>
              </rect>
              <text
                x={cx}
                y={ly}
                textAnchor="end"
                fontSize="10"
                fill="#6b7280"
                transform={`rotate(-40, ${cx}, ${ly})`}
              >
                {d.label}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
}

// ─── SVG grouped two-bar chart (revenue + profit per group) ───────────────────
function GroupedBarChart({ data }) {
  // data: [{ label, revenue, profit }]
  if (!data || data.length === 0) {
    return (
      <p style={{ textAlign: 'center', color: '#aaa', padding: '28px 0', margin: 0, fontSize: '14px' }}>
        No data for the selected period
      </p>
    );
  }

  const LM = 72, BM = 60, TM = 10, RM = 20;
  const groupW = Math.max(Math.floor(620 / data.length), 66);
  const bW = Math.max(Math.floor((groupW - 14) / 2), 14);
  const gap = 4;
  const chartH = 270;
  const innerH = chartH - TM - BM;
  const chartW = data.length * groupW + LM + RM;
  const maxVal = Math.max(
    ...data.flatMap(d => [Number(d.revenue) || 0, Math.abs(Number(d.profit) || 0)]),
    1
  );
  const TICKS = 5;
  const tickStep = maxVal / TICKS;

  return (
    <div>
      {/* Legend */}
      <div
        role="list"
        aria-label="Chart legend"
        style={{ display: 'flex', gap: '20px', marginBottom: '10px', fontSize: '13px', color: '#374151' }}
      >
        <span role="listitem" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <span aria-hidden="true" style={{ display: 'inline-block', width: 14, height: 14, backgroundColor: '#16a34a', borderRadius: 3 }} />
          Revenue
        </span>
        <span role="listitem" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <span aria-hidden="true" style={{ display: 'inline-block', width: 14, height: 14, backgroundColor: '#0ea5e9', borderRadius: 3 }} />
          Profit
        </span>
      </div>
      <div style={{ overflowX: 'auto', overflowY: 'hidden' }}>
        <svg
          role="img"
          aria-label="Grouped bar chart showing daily revenue and profit"
          width={Math.max(chartW, 320)}
          height={chartH}
          style={{ fontFamily: 'Calibri, sans-serif', display: 'block' }}
        >
          {Array.from({ length: TICKS + 1 }, (_, i) => {
            const v = tickStep * i;
            const y = TM + innerH - (innerH * i) / TICKS;
            return (
              <g key={i}>
                <line x1={LM} y1={y} x2={chartW - RM} y2={y} stroke="#e5e7eb" strokeWidth="1" />
                <text x={LM - 6} y={y + 4} textAnchor="end" fontSize="11" fill="#6b7280">
                  {formatCurrency(v)}
                </text>
              </g>
            );
          })}

          <line x1={LM} y1={TM} x2={LM} y2={TM + innerH} stroke="#d1d5db" strokeWidth="1.5" />
          <line x1={LM} y1={TM + innerH} x2={chartW - RM} y2={TM + innerH} stroke="#d1d5db" strokeWidth="1.5" />

          {data.map((d, i) => {
            const rev = Number(d.revenue) || 0;
            const prof = Number(d.profit) || 0;
            const revH = Math.max((rev / maxVal) * innerH, rev > 0 ? 2 : 0);
            const profH = Math.max((Math.abs(prof) / maxVal) * innerH, prof !== 0 ? 2 : 0);
            const gx = LM + i * groupW;
            const bxStart = gx + (groupW - 2 * bW - gap) / 2;
            const cx = gx + groupW / 2;
            const ly = TM + innerH + 14;
            return (
              <g key={i}>
                <rect x={bxStart} y={TM + innerH - revH} width={bW} height={revH} fill="#16a34a" rx="3" opacity="0.88">
                  <title>{`${d.label} Revenue: ${formatCurrency(rev)}`}</title>
                </rect>
                <rect
                  x={bxStart + bW + gap}
                  y={TM + innerH - Math.max(profH, 1)}
                  width={bW}
                  height={Math.max(profH, 1)}
                  fill={prof >= 0 ? '#0ea5e9' : '#ef4444'}
                  rx="3"
                  opacity="0.88"
                >
                  <title>{`${d.label} Profit: ${formatCurrency(prof)}`}</title>
                </rect>
                <text
                  x={cx}
                  y={ly}
                  textAnchor="end"
                  fontSize="10"
                  fill="#6b7280"
                  transform={`rotate(-40, ${cx}, ${ly})`}
                >
                  {d.label}
                </text>
              </g>
            );
          })}
        </svg>
      </div>
    </div>
  );
}

// ─── Summary card ─────────────────────────────────────────────────────────────
function SummaryCard({ title, value, sub, accentColor = '#16a34a' }) {
  return (
    <div
      style={{
        flex: '1 1 180px',
        backgroundColor: '#fff',
        border: '1px solid #e5e7eb',
        borderRadius: '10px',
        padding: '18px 20px',
        boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
        borderLeft: `4px solid ${accentColor}`,
      }}
    >
      <div style={{ fontSize: '12px', color: '#6b7280', marginBottom: '6px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
        {title}
      </div>
      <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#111827' }}>{value}</div>
      {sub && <div style={{ fontSize: '12px', color: '#9ca3af', marginTop: '4px' }}>{sub}</div>}
    </div>
  );
}

// ─── Chart section wrapper ────────────────────────────────────────────────────
function ChartSection({ title, children }) {
  return (
    <div
      style={{
        backgroundColor: '#fff',
        border: '1px solid #e5e7eb',
        borderRadius: '10px',
        padding: '20px 24px',
        boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
        marginBottom: '24px',
      }}
    >
      <h3 style={{ margin: '0 0 16px', fontSize: '16px', fontWeight: '700', color: '#1f2937' }}>{title}</h3>
      {children}
    </div>
  );
}

// ─── Main Report Page ─────────────────────────────────────────────────────────
const ReportPage = () => {
  const [startDate, setStartDate] = useState(daysAgoStr(29));
  const [endDate, setEndDate] = useState(todayStr());
  const [salesData, setSalesData] = useState([]);
  const [expenseData, setExpenseData] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [salesRes, expenseRes] = await Promise.all([
        api.get('/api/sales/report', { params: { startDate, endDate } }),
        api.get('/api/daily-expenses/range', { params: { startDate, endDate } }),
      ]);
      setSalesData(Array.isArray(salesRes.data) ? salesRes.data : []);
      setExpenseData(Array.isArray(expenseRes.data) ? expenseRes.data : []);
    } catch {
      notifyError('Failed to load report data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // ── Aggregations ──────────────────────────────────────────────────────────
  const dailySummary = useMemo(() => {
    // Sales grouped by date
    const salesByDate = {};
    salesData.forEach(s => {
      const d = s.saleDate;
      if (!salesByDate[d]) salesByDate[d] = { revenue: 0, commission: 0, quantity: 0 };
      salesByDate[d].revenue += Number(s.revenue) || 0;
      salesByDate[d].commission += Number(s.agentCommission) || 0;
      salesByDate[d].quantity += Number(s.quantity) || 0;
    });

    // Expenses grouped by date (sum across all salesmen per day)
    const expByDate = {};
    expenseData.forEach(e => {
      const d = e.expenseDate;
      if (!expByDate[d]) expByDate[d] = 0;
      expByDate[d] += Number(e.totalExpense) || 0;
    });

    const allDates = Array.from(
      new Set([...Object.keys(salesByDate), ...Object.keys(expByDate)])
    ).sort();

    return allDates.map(date => {
      const s = salesByDate[date] || { revenue: 0, commission: 0, quantity: 0 };
      const exp = expByDate[date] || 0;
      return {
        date,
        revenue: s.revenue,
        commission: s.commission,
        expenses: exp,
        profit: s.revenue - s.commission - exp,
        quantity: s.quantity,
      };
    });
  }, [salesData, expenseData]);

  const weeklySummary = useMemo(() => {
    const weeks = {};
    dailySummary.forEach(d => {
      const weekStart = getMondayOfWeek(d.date);
      if (!weeks[weekStart]) weeks[weekStart] = { revenue: 0, profit: 0 };
      weeks[weekStart].revenue += d.revenue;
      weeks[weekStart].profit += d.profit;
    });
    return Object.entries(weeks)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([weekStart, vals]) => ({ label: `Wk ${formatDateShort(weekStart)}`, ...vals }));
  }, [dailySummary]);

  const productSummary = useMemo(() => {
    const products = {};
    salesData.forEach(s => {
      const code = s.productCode || 'Unknown';
      if (!products[code]) products[code] = { revenue: 0, commission: 0, quantity: 0 };
      products[code].revenue += Number(s.revenue) || 0;
      products[code].commission += Number(s.agentCommission) || 0;
      products[code].quantity += Number(s.quantity) || 0;
    });
    return Object.entries(products)
      .sort(([, a], [, b]) => b.revenue - a.revenue)
      .map(([code, vals]) => ({
        label: code,
        revenue: vals.revenue,
        commission: vals.commission,
        quantity: vals.quantity,
        profit: vals.revenue - vals.commission,
      }));
  }, [salesData]);

  const totals = useMemo(
    () =>
      dailySummary.reduce(
        (acc, d) => ({
          revenue: acc.revenue + d.revenue,
          expenses: acc.expenses + d.expenses,
          commission: acc.commission + d.commission,
          profit: acc.profit + d.profit,
          quantity: acc.quantity + d.quantity,
        }),
        { revenue: 0, expenses: 0, commission: 0, profit: 0, quantity: 0 }
      ),
    [dailySummary]
  );

  // ── Render ─────────────────────────────────────────────────────────────────
  return (
    <div style={{ padding: '28px 32px', fontFamily: 'Calibri, sans-serif', backgroundColor: '#f9fafb', minHeight: '100vh' }}>

      {/* Page header + date-range filter */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', flexWrap: 'wrap', gap: '16px', marginBottom: '24px' }}>
        <h2 style={{ margin: 0, fontSize: '22px', fontWeight: '700', color: '#111827' }}>Business Report</h2>
        <div style={{ display: 'flex', alignItems: 'flex-end', gap: '12px', flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '3px' }}>
            <label htmlFor="report-start-date" style={filterLabelStyle}>From</label>
            <input id="report-start-date" type="date" value={startDate} onChange={e => setStartDate(e.target.value)} style={dateInputStyle} />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '3px' }}>
            <label htmlFor="report-end-date" style={filterLabelStyle}>To</label>
            <input id="report-end-date" type="date" value={endDate} onChange={e => setEndDate(e.target.value)} style={dateInputStyle} />
          </div>
          <button
            type="button"
            onClick={fetchData}
            disabled={loading}
            style={{
              backgroundColor: loading ? '#9ca3af' : '#16a34a',
              color: '#fff',
              border: 'none',
              padding: '9px 22px',
              borderRadius: '6px',
              cursor: loading ? 'not-allowed' : 'pointer',
              fontWeight: 'bold',
              fontSize: '14px',
            }}
          >
            {loading ? 'Loading…' : 'Apply'}
          </button>
        </div>
      </div>

      {/* Summary cards */}
      <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap', marginBottom: '28px' }}>
        <SummaryCard
          title="Total Revenue"
          value={`₹${totals.revenue.toFixed(2)}`}
          sub={`${totals.quantity} units sold`}
          accentColor="#16a34a"
        />
        <SummaryCard
          title="Total Expenses"
          value={`₹${totals.expenses.toFixed(2)}`}
          sub="Petrol, food, vehicle rent, etc."
          accentColor="#ef4444"
        />
        <SummaryCard
          title="Agent Commission"
          value={`₹${totals.commission.toFixed(2)}`}
          sub="Commission paid to salesmen"
          accentColor="#f59e0b"
        />
        <SummaryCard
          title="Net Profit"
          value={`₹${totals.profit.toFixed(2)}`}
          sub="Revenue − Expenses − Commission"
          accentColor={totals.profit >= 0 ? '#0ea5e9' : '#ef4444'}
        />
      </div>

      {loading && (
        <div style={{ textAlign: 'center', padding: '80px', color: '#9ca3af', fontSize: '16px' }}>
          Loading report data…
        </div>
      )}

      {!loading && (
        <>
          {/* Daily Revenue & Profit chart */}
          <ChartSection title="Daily Revenue & Profit">
            <GroupedBarChart
              data={dailySummary.map(d => ({
                label: formatDateShort(d.date),
                revenue: d.revenue,
                profit: d.profit,
              }))}
            />
          </ChartSection>

          {/* Weekly Revenue chart */}
          <ChartSection title="Weekly Revenue">
            <BarChart
              data={weeklySummary.map(w => ({ label: w.label, value: w.revenue }))}
              barColor="#16a34a"
            />
          </ChartSection>

          {/* Product variant charts */}
          <ChartSection title="Revenue & Profit by Product Variant">
            <div style={{ display: 'flex', gap: '32px', flexWrap: 'wrap' }}>
              <div style={{ flex: '1 1 280px' }}>
                <p style={{ margin: '0 0 6px', fontSize: '13px', color: '#6b7280', fontWeight: '600' }}>Revenue (₹)</p>
                <BarChart
                  data={productSummary.map(p => ({ label: p.label, value: p.revenue }))}
                  barColor="#16a34a"
                  height={220}
                />
              </div>
              <div style={{ flex: '1 1 280px' }}>
                <p style={{ margin: '0 0 6px', fontSize: '13px', color: '#6b7280', fontWeight: '600' }}>Profit after commission (₹)</p>
                <BarChart
                  data={productSummary.map(p => ({ label: p.label, value: p.profit }))}
                  barColor="#0ea5e9"
                  height={220}
                />
              </div>
            </div>
          </ChartSection>

          {/* Product breakdown table */}
          <ChartSection title="Product Variant Breakdown">
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <caption style={{ captionSide: 'top', textAlign: 'left', fontSize: '13px', color: '#6b7280', marginBottom: '8px', paddingBottom: '6px' }}>
                  Sales breakdown by product variant — revenue, commission, and profit for the selected period
                </caption>
                <thead>
                  <tr style={{ backgroundColor: '#f3f4f6' }}>
                    {['Product', 'Qty Sold', 'Revenue (₹)', 'Commission (₹)', 'Profit (₹)'].map(h => (
                      <th key={h} style={thStyle}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {productSummary.length === 0 ? (
                    <tr>
                      <td colSpan={5} style={{ ...tdStyle, textAlign: 'center', padding: '28px', color: '#9ca3af' }}>
                        No sales data for the selected period
                      </td>
                    </tr>
                  ) : (
                    productSummary.map((p, i) => (
                      <tr key={p.label} style={{ backgroundColor: i % 2 === 0 ? '#fff' : '#f9fafb' }}>
                        <td style={{ ...tdStyle, textAlign: 'left', fontWeight: '600' }}>{p.label}</td>
                        <td style={tdStyle}>{p.quantity}</td>
                        <td style={tdStyle}>{p.revenue.toFixed(2)}</td>
                        <td style={tdStyle}>{p.commission.toFixed(2)}</td>
                        <td style={{ ...tdStyle, color: p.profit >= 0 ? '#16a34a' : '#ef4444', fontWeight: '600' }}>
                          {p.profit.toFixed(2)}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
                {productSummary.length > 0 && (
                  <tfoot>
                    <tr style={{ backgroundColor: '#1f2937', color: '#fff', fontWeight: 'bold' }}>
                      <td style={{ ...tdStyle, textAlign: 'left', border: '1px solid #374151' }}>Total</td>
                      <td style={{ ...tdStyle, border: '1px solid #374151' }}>{totals.quantity}</td>
                      <td style={{ ...tdStyle, border: '1px solid #374151' }}>{totals.revenue.toFixed(2)}</td>
                      <td style={{ ...tdStyle, border: '1px solid #374151' }}>{totals.commission.toFixed(2)}</td>
                      <td
                        style={{
                          ...tdStyle,
                          border: '1px solid #374151',
                          color: totals.profit >= 0 ? '#86efac' : '#fca5a5',
                        }}
                      >
                        {totals.profit.toFixed(2)}
                      </td>
                    </tr>
                  </tfoot>
                )}
              </table>
            </div>
          </ChartSection>
        </>
      )}
    </div>
  );
};

// ─── Styles ───────────────────────────────────────────────────────────────────
const filterLabelStyle = {
  fontSize: '11px',
  color: '#6b7280',
  fontWeight: '600',
  textTransform: 'uppercase',
  letterSpacing: '0.04em',
};

const dateInputStyle = {
  padding: '8px 12px',
  borderRadius: '6px',
  border: '1px solid #d1d5db',
  fontSize: '14px',
  color: '#374151',
};

const thStyle = {
  padding: '10px 12px',
  textAlign: 'right',
  fontWeight: '600',
  color: '#374151',
  border: '1px solid #e5e7eb',
  whiteSpace: 'nowrap',
};

const tdStyle = {
  padding: '9px 12px',
  border: '1px solid #e5e7eb',
  textAlign: 'right',
};

export default ReportPage;
