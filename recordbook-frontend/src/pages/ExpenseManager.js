import React, { useState, useEffect, useCallback } from 'react';
import { salesmanAPI, expenseAPI } from '../api';
import { notifySuccess, notifyError } from '../utils/toast';
import { cacheUtils } from '../utils/cacheUtils';
import '../styles/ExpenseManager.css';

const ExpenseManager = () => {
  const [selectedSalesman, setSelectedSalesman] = useState('');
  const [selectedDate, setSelectedDate] = useState(() => {
    const today = new Date();
    return today.toISOString().split('T')[0];
  });
  const [salesmenList, setSalesmenList] = useState([]);
  const [loadingSalesmen, setLoadingSalesmen] = useState(false);
  const [expenses, setExpenses] = useState([]);
  const [loadingExpenses, setLoadingExpenses] = useState(false);
  const [saving, setSaving] = useState(false);
  const [totalExpense, setTotalExpense] = useState(0);

  // Load salesmen on mount
  const loadSalesmen = useCallback(async () => {
    setLoadingSalesmen(true);
    try {
      let salesmen = cacheUtils.getSalesmenAliases();
      if (!salesmen) {
        const response = await salesmanAPI.getAliases();
        salesmen = Array.isArray(response.data) ? response.data : [];
        cacheUtils.setSalesmenAliases(salesmen);
      }
      setSalesmenList(salesmen);
    } catch (error) {
      console.error('Error loading salesmen:', error);
      notifyError('Failed to load salesmen');
      setSalesmenList([]);
    } finally {
      setLoadingSalesmen(false);
    }
  }, []);

  useEffect(() => {
    loadSalesmen();
  }, [loadSalesmen]);

  // Load expenses when salesman or date changes
  const loadExpenses = useCallback(async () => {
    if (!selectedSalesman || !selectedDate) {
      setExpenses([]);
      setTotalExpense(0);
      return;
    }

    setLoadingExpenses(true);
    try {
      const response = await expenseAPI.getByDate(selectedSalesman, selectedDate);
      const dailyExpense = response.data;

      if (dailyExpense && dailyExpense.totalExpense) {
        setTotalExpense(dailyExpense.totalExpense);
        // Initialize with one empty row or show existing expense as single total
        setExpenses([{ category: 'Total', amount: dailyExpense.totalExpense }]);
      } else {
        setTotalExpense(0);
        setExpenses([{ category: '', amount: '' }]);
      }
    } catch (error) {
      console.error('Error loading expenses:', error);
      // If no expense found, start with empty row
      setTotalExpense(0);
      setExpenses([{ category: '', amount: '' }]);
    } finally {
      setLoadingExpenses(false);
    }
  }, [selectedSalesman, selectedDate]);

  useEffect(() => {
    loadExpenses();
  }, [loadExpenses]);

  const handleAddExpenseRow = () => {
    setExpenses([...expenses, { category: '', amount: '' }]);
  };

  const handleRemoveExpenseRow = (index) => {
    if (expenses.length === 1) {
      notifyError('At least one expense row is required');
      return;
    }
    setExpenses(expenses.filter((_, i) => i !== index));
  };

  const handleExpenseChange = (index, field, value) => {
    const updated = [...expenses];
    updated[index] = { ...updated[index], [field]: value };
    setExpenses(updated);
  };

  const calculateTotal = () => {
    return expenses.reduce((sum, exp) => {
      const amount = parseFloat(exp.amount) || 0;
      return sum + amount;
    }, 0);
  };

  const handleSaveExpenses = async () => {
    if (!selectedSalesman) {
      notifyError('Please select a salesman');
      return;
    }

    if (!selectedDate) {
      notifyError('Please select a date');
      return;
    }

    // Filter out empty rows
    const validExpenses = expenses.filter(e => e.category?.trim() && e.amount);

    if (validExpenses.length === 0) {
      notifyError('Please add at least one expense entry with category and amount');
      return;
    }

    try {
      setSaving(true);

      const requestBody = {
        salesmanAlias: selectedSalesman,
        date: selectedDate,
        expenses: validExpenses.map(e => ({
          expenseDate: selectedDate,
          category: e.category,
          amount: parseFloat(e.amount)
        }))
      };

      await expenseAPI.submitExpensesOnly(requestBody);

      notifySuccess('Expenses saved successfully');
      loadExpenses(); // Reload to show updated data
    } catch (error) {
      notifyError('Failed to save expenses: ' + (error.response?.data?.message || error.message));
    } finally {
      setSaving(false);
    }
  };

  const handleClearExpenses = () => {
    setExpenses([{ category: '', amount: '' }]);
  };

  const computedTotal = calculateTotal();

  return (
    <div className="expense-manager-page">
      <div className="em-header">
        <h2>Expense Manager</h2>
        <p className="em-subtitle">View and manage daily expenses for salesmen</p>
      </div>

      <div className="em-filters">
        <div className="em-filter-group">
          <label htmlFor="salesman-select">Salesman</label>
          <select
            id="salesman-select"
            value={selectedSalesman}
            onChange={(e) => setSelectedSalesman(e.target.value)}
            disabled={loadingSalesmen}
            className="em-select"
          >
            <option value="">{loadingSalesmen ? 'Loading...' : 'Select salesman'}</option>
            {salesmenList.map((alias, idx) => (
              <option key={idx} value={alias}>
                {alias}
              </option>
            ))}
          </select>
        </div>

        <div className="em-filter-group">
          <label htmlFor="date-select">Date</label>
          <input
            id="date-select"
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="em-date-input"
          />
        </div>

        <button
          onClick={loadExpenses}
          disabled={!selectedSalesman || !selectedDate || loadingExpenses}
          className="em-load-btn"
        >
          {loadingExpenses ? 'Loading...' : 'Load Expenses'}
        </button>
      </div>

      {selectedSalesman && selectedDate && (
        <div className="em-content">
          <div className="em-info-bar">
            <div className="em-info-item">
              <span className="em-info-label">Salesman:</span>
              <span className="em-info-value">{selectedSalesman}</span>
            </div>
            <div className="em-info-item">
              <span className="em-info-label">Date:</span>
              <span className="em-info-value">{selectedDate}</span>
            </div>
            <div className="em-info-item">
              <span className="em-info-label">Current Total:</span>
              <span className="em-info-value">₹{totalExpense.toFixed(2)}</span>
            </div>
          </div>

          <div className="em-expense-section">
            <div className="em-section-header">
              <h3>Expense Entries</h3>
              <div className="em-header-actions">
                <button onClick={handleAddExpenseRow} className="em-add-btn">
                  + Add Expense Row
                </button>
                <button onClick={handleClearExpenses} className="em-clear-btn">
                  Clear All
                </button>
              </div>
            </div>

            <div className="em-expense-list">
              {expenses.map((expense, index) => (
                <div key={index} className="em-expense-row">
                  <div className="em-expense-field">
                    <label>Category</label>
                    <input
                      type="text"
                      value={expense.category}
                      onChange={(e) => handleExpenseChange(index, 'category', e.target.value)}
                      placeholder="e.g., Fuel, Food, Transport"
                      className="em-input"
                    />
                  </div>

                  <div className="em-expense-field">
                    <label>Amount (₹)</label>
                    <input
                      type="number"
                      value={expense.amount}
                      onChange={(e) => handleExpenseChange(index, 'amount', e.target.value)}
                      placeholder="0.00"
                      min="0"
                      step="0.01"
                      className="em-input em-input-number"
                    />
                  </div>

                  <div className="em-expense-actions">
                    <button
                      onClick={() => handleRemoveExpenseRow(index)}
                      disabled={expenses.length === 1}
                      className="em-remove-btn"
                      title="Remove this expense"
                    >
                      🗑️ Remove
                    </button>
                  </div>
                </div>
              ))}
            </div>

            <div className="em-total-section">
              <div className="em-total-label">Computed Total:</div>
              <div className="em-total-value">₹{computedTotal.toFixed(2)}</div>
            </div>

            <div className="em-action-buttons">
              <button
                onClick={handleSaveExpenses}
                disabled={saving || !selectedSalesman || !selectedDate}
                className="em-save-btn"
              >
                {saving ? 'Saving...' : 'Save Expenses'}
              </button>
            </div>
          </div>
        </div>
      )}

      {!selectedSalesman && (
        <div className="em-empty-state">
          <p>👆 Select a salesman and date to view/edit expenses</p>
        </div>
      )}
    </div>
  );
};

export default ExpenseManager;

