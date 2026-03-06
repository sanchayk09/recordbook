import React, { useState, useEffect, useCallback } from 'react';
import { warehouseAPI, salesmanAPI, productCostAPI } from '../api';
import { notifySuccess, notifyError } from '../utils/toast';
import { cacheUtils } from '../utils/cacheUtils';
import '../styles/WarehouseDashboard.css';

const WarehouseDashboard = () => {
  const [activeTab, setActiveTab] = useState('inventory');
  const [inventory, setInventory] = useState([]);
  const [ledger, setLedger] = useState([]);
  const [salesmen, setSalesmen] = useState([]);
  const [salesmenStock, setSalesmenStock] = useState([]); // New state for salesmen stock summary
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedSalesmanCard, setSelectedSalesmanCard] = useState(null); // For viewing salesman details

  // New states for improved Issue Stock flow
  const [selectedSalesmanForIssue, setSelectedSalesmanForIssue] = useState('');
  const [showIssueModal, setShowIssueModal] = useState(false);
  const [showIssueConfirmModal, setShowIssueConfirmModal] = useState(false);
  const [issueList, setIssueList] = useState([]); // List of items to issue

  // New states for product modal
  const [selectedProductName, setSelectedProductName] = useState('');
  const [selectedProductCodes, setSelectedProductCodes] = useState({}); // { productCode: quantity }
  const [availableProductNames, setAvailableProductNames] = useState([]); // Unique product names
  const [productCodesByName, setProductCodesByName] = useState({}); // { productName: [codes] }

  // New states for improved Return Stock flow
  const [selectedSalesmanForReturn, setSelectedSalesmanForReturn] = useState('');
  const [salesmanCurrentStock, setSalesmanCurrentStock] = useState([]);
  const [returnList, setReturnList] = useState({}); // { productCode: quantity }
  const [showReturnAllModal, setShowReturnAllModal] = useState(false);
  const [showReturnSelectedModal, setShowReturnSelectedModal] = useState(false);

  const [adjustForm, setAdjustForm] = useState({
    productCode: '',
    quantity: '',
    txnType: 'TRANSFER_IN',
    remarks: '',
  });

  // Wrap all load functions with useCallback to prevent duplicate API calls
  const loadInventory = useCallback(async () => {
    try {
      setLoading(true);
      const response = await warehouseAPI.getAllInventory();
      setInventory(response.data);
    } catch (error) {
      console.error('Failed to load inventory:', error.response?.status, error.response?.data);
      if (error.response?.status === 500) {
        notifyError('Backend error loading inventory. Please check backend server logs.');
      } else {
        notifyError('Failed to load inventory');
      }
    } finally {
      setLoading(false);
    }
  }, []);

  const loadLedger = useCallback(async () => {
    try {
      setLoading(true);
      const response = await warehouseAPI.getAllLedger();
      setLedger(response.data);
    } catch (error) {
      console.error('Failed to load ledger:', error.response?.status, error.response?.data);
      if (error.response?.status === 500) {
        notifyError('Backend error loading ledger. Please check backend server logs.');
      } else {
        notifyError('Failed to load ledger');
      }
    } finally {
      setLoading(false);
    }
  }, []);

  const loadSalesmen = useCallback(async () => {
    try {
      let aliases = [];

      // Check cache first
      const cachedAliases = cacheUtils.getSalesmenAliases();
      if (cachedAliases && cachedAliases.length > 0) {
        aliases = cachedAliases
          .map((alias) => String(alias || '').trim())
          .filter(Boolean)
          .map((alias, idx) => ({
            id: `alias-${idx}`,
            alias,
            displayName: alias,
          }));
      } else {
        try {
          const aliasResponse = await salesmanAPI.getAliases();
          aliases = Array.isArray(aliasResponse.data)
            ? aliasResponse.data
                .map((alias) => String(alias || '').trim())
                .filter(Boolean)
                .map((alias, idx) => ({
                  id: `alias-${idx}`,
                  alias,
                  displayName: alias,
                }))
            : [];
          
          // Cache the raw aliases
          if (aliases.length > 0) {
            const rawAliases = aliases.map(a => a.alias);
            cacheUtils.setSalesmenAliases(rawAliases);
          }
        } catch (error) {
          aliases = [];
        }
      }

      if (aliases.length > 0) {
        setSalesmen(aliases);
        return;
      }

      const response = await salesmanAPI.getAll();
      const mappedSalesmen = Array.isArray(response.data)
        ? response.data
            .map((salesman, idx) => {
              const alias = salesman?.alias || salesman?.salesmanAlias || '';
              const firstName = salesman?.firstName || '';
              const lastName = salesman?.lastName || '';
              const fallbackName = `${firstName} ${lastName}`.trim();
              const displayName = alias || fallbackName;

              if (!displayName) {
                return null;
              }

              return {
                id: salesman?.id || salesman?.salesmanId || `salesman-${idx}`,
                alias: alias || displayName,
                displayName,
              };
            })
            .filter(Boolean)
        : [];

      setSalesmen(mappedSalesmen);
    } catch (error) {
      console.error('Failed to load salesmen:', error.response?.status, error.response?.data);
      if (error.response?.status === 500) {
        notifyError('Backend error loading salesmen. Please check backend server logs.');
      }
      setSalesmen([]);
    }
  }, []);

  const loadProducts = useCallback(async () => {
    try {
      const response = await productCostAPI.getAll();
      const productList = Array.isArray(response.data) ? response.data : [];
      setProducts(productList);

      // Create mapping of product names to product codes
      const names = [];
      const codesByName = {};

      productList.forEach((product) => {
        const name = product.productName || '';
        if (name && !names.includes(name)) {
          names.push(name);
        }
        if (name) {
          if (!codesByName[name]) {
            codesByName[name] = [];
          }
          codesByName[name].push({
            code: product.productCode,
            variant: product.variant,
            metric: product.metric,
            metricQuantity: product.metricQuantity,
          });
        }
      });

      // Sort variants within each product name group
      Object.keys(codesByName).forEach((productName) => {
        codesByName[productName].sort((a, b) => {
          const variantA = (a.variant || a.code || '').toLowerCase();
          const variantB = (b.variant || b.code || '').toLowerCase();
          return variantA.localeCompare(variantB);
        });
      });

      setAvailableProductNames(names.sort());
      setProductCodesByName(codesByName);
    } catch (error) {
      console.error('Failed to load products:', error.response?.status, error.response?.data);
      if (error.response?.status === 500) {
        notifyError('Backend error loading products. Please check backend server logs.');
      }
      setProducts([]);
    }
  }, []);

  const loadSalesmenStock = useCallback(async () => {
    try {
      setLoading(true);
      const response = await warehouseAPI.getSalesmenStockSummary();
      setSalesmenStock(response.data);
    } catch (error) {
      console.error('Failed to load salesmen stock summary:', error.response?.status, error.response?.data);
      if (error.response?.status === 500) {
        notifyError('Backend error loading salesmen stock. Please check backend server logs.');
      }
      setSalesmenStock([]);
    } finally {
      setLoading(false);
    }
  }, []);

  // Load initial data on mount
  useEffect(() => {
    loadInventory();
    loadSalesmen();
    loadProducts();
    loadSalesmenStock();
  }, [loadInventory, loadSalesmen, loadProducts, loadSalesmenStock]);

  // New handlers for modal-based issue stock
  const handleStartIssueForSalesman = (salesmanAlias) => {
    setSelectedSalesmanForIssue(salesmanAlias);
    setShowIssueModal(true);
    setSelectedProductName('');
    setSelectedProductCodes({});
    setIssueList([]);
  };

  const handleProductNameChange = (productName) => {
    setSelectedProductName(productName);

    // Initialize all product codes with empty quantities
    if (productName && productCodesByName[productName]) {
      const initialQuantities = {};
      productCodesByName[productName].forEach(product => {
        initialQuantities[product.code] = '';
      });
      setSelectedProductCodes(initialQuantities);
    } else {
      setSelectedProductCodes({});
    }
  };

  const handleProductCodeQuantityChange = (productCode, quantity) => {
    setSelectedProductCodes({
      ...selectedProductCodes,
      [productCode]: quantity,
    });
  };

  const handleAddProductsFromModal = () => {
    const itemsToAdd = Object.entries(selectedProductCodes).filter(
      ([, quantity]) => quantity && parseInt(quantity) > 0
    );

    if (itemsToAdd.length === 0) {
      notifyError('Please enter quantity for at least one product');
      return;
    }

    // Add all selected products to issue list
    const newItems = itemsToAdd.map(([productCode, quantity]) => ({
      id: Date.now() + Math.random(),
      productCode,
      quantity: parseInt(quantity),
      remarks: '',
    }));

    setIssueList([...issueList, ...newItems]);
    notifySuccess(`${newItems.length} item(s) added to list`);

    // Keep the modal open but show success - user can continue adding
    setShowIssueModal(false);
  };


  const handleRemoveFromIssueList = (itemId) => {
    setIssueList(issueList.filter(item => item.id !== itemId));
  };

  const handleUpdateIssueListQuantity = (itemId, newQuantity) => {
    setIssueList(issueList.map(item =>
      item.id === itemId ? { ...item, quantity: parseInt(newQuantity) || 0 } : item
    ));
  };

  const handleSubmitIssueList = async () => {
    if (issueList.length === 0) {
      notifyError('Please add at least one item');
      return;
    }

    // Show confirmation modal instead of directly issuing
    setShowIssueConfirmModal(true);
  };

  const confirmIssueStock = async () => {
    setShowIssueConfirmModal(false);

    try {
      setLoading(true);
      let successCount = 0;

      // Issue each item in the list
      for (const item of issueList) {
        try {
          await warehouseAPI.issueStock({
            productCode: item.productCode,
            salesmanAlias: selectedSalesmanForIssue,
            quantity: item.quantity,
            // remarks is auto-generated in backend
          });
          successCount++;
        } catch (error) {
          console.error(`Failed to issue ${item.productCode}:`, error);
        }
      }

      notifySuccess(`${successCount}/${issueList.length} items issued successfully`);
      setShowIssueModal(false);
      setSelectedSalesmanForIssue('');
      setIssueList([]);
      loadInventory();
      loadSalesmenStock();
      loadLedger();
    } catch (error) {
      notifyError('Failed to process issue requests');
    } finally {
      setLoading(false);
    }
  };

  // New handlers for improved Return Stock flow
  const handleSelectSalesmanForReturn = async (salesmanAlias) => {
    setSelectedSalesmanForReturn(salesmanAlias);
    setReturnList({});

    if (!salesmanAlias) {
      setSalesmanCurrentStock([]);
      return;
    }

    try {
      setLoading(true);
      // Get all salesmen stock and filter for selected salesman
      const response = await warehouseAPI.getSalesmenStockSummary();
      const salesmenData = response.data;
      const salesmanData = salesmenData.find(s => s.salesmanAlias === salesmanAlias);

      if (salesmanData && salesmanData.products) {
        setSalesmanCurrentStock(salesmanData.products);
      } else {
        setSalesmanCurrentStock([]);
        notifyError('No stock found for this salesman');
      }
    } catch (error) {
      console.error('Failed to load salesman stock:', error);
      setSalesmanCurrentStock([]);
      notifyError('Failed to load salesman stock');
    } finally {
      setLoading(false);
    }
  };

  const handleReturnQuantityChange = (productCode, quantity) => {
    setReturnList(prev => ({
      ...prev,
      [productCode]: quantity
    }));
  };

  const handleReturnSelectedItems = async () => {
    const itemsToReturn = Object.entries(returnList).filter(([_, qty]) => qty && parseInt(qty) > 0);

    if (itemsToReturn.length === 0) {
      notifyError('Please enter quantities to return');
      return;
    }

    // Show confirmation modal
    setShowReturnSelectedModal(true);
  };

  const confirmReturnSelectedItems = async () => {
    setShowReturnSelectedModal(false);

    const itemsToReturn = Object.entries(returnList).filter(([_, qty]) => qty && parseInt(qty) > 0);

    try {
      setLoading(true);
      let successCount = 0;

      for (const [productCode, quantity] of itemsToReturn) {
        try {
          await warehouseAPI.returnStock({
            productCode,
            salesmanAlias: selectedSalesmanForReturn,
            quantity: parseInt(quantity),
          });
          successCount++;
        } catch (error) {
          console.error(`Failed to return ${productCode}:`, error);
        }
      }

      notifySuccess(`${successCount}/${itemsToReturn.length} items returned successfully`);

      // Refresh data
      await handleSelectSalesmanForReturn(selectedSalesmanForReturn);
      loadInventory();
      loadSalesmenStock();
    } catch (error) {
      notifyError('Failed to return stock');
    } finally {
      setLoading(false);
    }
  };

  const handleReturnAllStock = async () => {
    if (salesmanCurrentStock.length === 0) {
      notifyError('No stock to return');
      return;
    }

    // Show confirmation modal instead of alert
    setShowReturnAllModal(true);
  };

  const confirmReturnAllStock = async () => {
    setShowReturnAllModal(false);

    try {
      setLoading(true);
      let successCount = 0;

      for (const product of salesmanCurrentStock) {
        try {
          await warehouseAPI.returnStock({
            productCode: product.productCode,
            salesmanAlias: selectedSalesmanForReturn,
            quantity: product.quantity,
          });
          successCount++;
        } catch (error) {
          console.error(`Failed to return ${product.productCode}:`, error);
        }
      }

      notifySuccess(`${successCount}/${salesmanCurrentStock.length} items returned successfully`);

      // Reset and refresh
      setSelectedSalesmanForReturn('');
      setSalesmanCurrentStock([]);
      setReturnList({});
      loadInventory();
      loadSalesmenStock();
    } catch (error) {
      notifyError('Failed to return all stock');
    } finally {
      setLoading(false);
    }
  };

  // Handle Adjust Stock
  const handleAdjustStock = async (e) => {
    e.preventDefault();
    if (!adjustForm.productCode || !adjustForm.quantity || !adjustForm.txnType) {
      notifyError('Please fill all required fields');
      return;
    }

    try {
      setLoading(true);
      await warehouseAPI.adjustStock({
        ...adjustForm,
        quantity: parseInt(adjustForm.quantity),
      });
      notifySuccess('Stock adjusted successfully');
      setAdjustForm({ productCode: '', quantity: '', txnType: 'TRANSFER_IN', remarks: '' });
      loadInventory();
      loadLedger();
    } catch (error) {
      notifyError(error.response?.data?.message || 'Failed to adjust stock');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
  };

  const getProductLiters = (product) => {
    const metric = String(product?.metric || '').toLowerCase();
    const isLiterMetric = metric === 'lit' || metric === 'ltr' || metric === 'liter' || metric === 'litre';
    const quantity = Number(product?.quantity) || 0;
    const metricQuantity = parseFloat(product?.metricQuantity);

    if (!isLiterMetric || Number.isNaN(metricQuantity)) {
      return 0;
    }

    return quantity * metricQuantity;
  };

  const getTotalLiters = (products = []) => {
    return products.reduce((sum, product) => sum + getProductLiters(product), 0);
  };


  return (
    <div className="warehouse-dashboard">
      <header className="warehouse-header">
        <h1>🏭 Warehouse Management</h1>
        <p>Manage inventory, issue stock to salesmen, and track movements</p>
      </header>

      <div className="warehouse-tabs">
        <button
          className={`tab-button ${activeTab === 'inventory' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('inventory');
            loadInventory();
          }}
        >
          📦 Inventory
        </button>
        <button
          className={`tab-button ${activeTab === 'issue' ? 'active' : ''}`}
          onClick={() => setActiveTab('issue')}
        >
          ➡️ Issue Stock
        </button>
        <button
          className={`tab-button ${activeTab === 'return' ? 'active' : ''}`}
          onClick={() => setActiveTab('return')}
        >
          ⬅️ Return Stock
        </button>
        <button
          className={`tab-button ${activeTab === 'adjust' ? 'active' : ''}`}
          onClick={() => setActiveTab('adjust')}
        >
          🔧 Adjust Stock
        </button>
        <button
          className={`tab-button ${activeTab === 'salesmen-stock' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('salesmen-stock');
            loadSalesmenStock();
          }}
        >
          👥 Salesmen Stock
        </button>
        <button
          className={`tab-button ${activeTab === 'ledger' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('ledger');
            loadLedger();
          }}
        >
          📋 Ledger
        </button>
      </div>

      <div className="warehouse-content">
        {/* INVENTORY TAB */}
        {activeTab === 'inventory' && (
          <div className="tab-content">
            <h2>Current Warehouse Inventory</h2>
            {loading ? (
              <p className="loading">Loading inventory...</p>
            ) : inventory.length === 0 ? (
              <p className="no-data">No inventory records found</p>
            ) : (
              <table className="inventory-table">
                <thead>
                  <tr>
                    <th>Product Code</th>
                    <th>Variant</th>
                    <th>Quantity (pcs)</th>
                    <th>Last Updated</th>
                  </tr>
                </thead>
                <tbody>
                  {inventory.map((item, idx) => (
                    <tr key={idx}>
                      <td className="code">{item.productCode}</td>
                      <td>{item.variant || '-'}</td>
                      <td className="qty">{item.qtyAvailable} pcs</td>
                      <td>{formatDate(item.lastUpdated)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {/* ISSUE STOCK TAB */}
        {activeTab === 'issue' && (
          <div className="tab-content">
            <h2>Issue Stock to Salesman</h2>

            {!selectedSalesmanForIssue ? (
              <div className="warehouse-form">
                <h3>Step 1: Select Salesman</h3>
                <form
                  onSubmit={(e) => {
                    e.preventDefault();
                    if (selectedSalesmanForIssue) {
                      handleStartIssueForSalesman(selectedSalesmanForIssue);
                    }
                  }}
                >
                  <div className="form-group">
                    <label>Select Salesman *</label>
                    <select
                      value={selectedSalesmanForIssue}
                      onChange={(e) => setSelectedSalesmanForIssue(e.target.value)}
                      required
                    >
                      <option value="">-- Choose a Salesman --</option>
                      {salesmen.map((s) => (
                        <option key={s.id} value={s.alias}>
                          {s.displayName}
                        </option>
                      ))}
                    </select>
                  </div>
                  <button type="submit" className="btn-submit">
                    ➡️ Proceed to Issue Stock
                  </button>
                </form>
              </div>
            ) : (
              <div className="warehouse-form">
                <h3>📦 Issue Stock to: <span className="selected-salesman">{selectedSalesmanForIssue}</span></h3>

                <button
                  className="btn-back"
                  onClick={() => setSelectedSalesmanForIssue('')}
                  type="button"
                >
                  ← Back to Salesman Selection
                </button>

                {/* Product Selection Modal */}
                {showIssueModal && (
                  <div className="modal-overlay">
                    <div className="modal-content modal-large">
                      <div className="modal-header">
                        <h2>Select Products to Issue</h2>
                        <button
                          className="modal-close"
                          onClick={() => setShowIssueModal(false)}
                          type="button"
                        >
                          ✕
                        </button>
                      </div>

                      <div className="modal-body">
                        <div className="form-group">
                          <label>Select Product Category *</label>
                          <select
                            value={selectedProductName}
                            onChange={(e) => handleProductNameChange(e.target.value)}
                          >
                            <option value="">-- Choose a Product --</option>
                            {availableProductNames.map((name) => (
                              <option key={name} value={name}>
                                {name}
                              </option>
                            ))}
                          </select>
                        </div>

                        {selectedProductName && productCodesByName[selectedProductName] && (
                          <div className="product-codes-section">
                            <h4>Variants for &quot;{selectedProductName}&quot;</h4>
                            <p className="helper-text">Enter quantities for products you want to issue</p>
                            <div className="product-codes-grid">
                              {productCodesByName[selectedProductName].map((product) => (
                                <div key={product.code} className="product-code-item-new">
                                  <div className="product-code-header">
                                    <span className="product-code-badge">{product.code}</span>
                                    {product.variant && (
                                      <span className="product-variant-text">{product.variant}</span>
                                    )}
                                  </div>
                                  {product.metric && product.metricQuantity && (
                                    <div className="product-metric">
                                      {product.metricQuantity} {product.metric}
                                    </div>
                                  )}
                                  <input
                                    type="number"
                                    min="0"
                                    value={selectedProductCodes[product.code] || ''}
                                    onChange={(e) =>
                                      handleProductCodeQuantityChange(product.code, e.target.value)
                                    }
                                    placeholder="Quantity"
                                    className="quantity-input-new"
                                  />
                                </div>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>

                      <div className="modal-footer">
                        <button
                          className="btn-cancel"
                          onClick={() => setShowIssueModal(false)}
                          type="button"
                        >
                          Cancel
                        </button>
                        <button
                          className="btn-submit"
                          onClick={handleAddProductsFromModal}
                          type="button"
                        >
                          ➕ Add to Issue List
                        </button>
                      </div>
                    </div>
                  </div>
                )}

                <button
                  className="btn-add-item"
                  onClick={() => {
                    setShowIssueModal(true);
                    setSelectedProductName('');
                    setSelectedProductCodes({});
                  }}
                  type="button"
                >
                  ➕ Add Items
                </button>


                {issueList.length > 0 && (
                  <div className="issue-list-section">
                    <h4>Items to Issue ({issueList.length})</h4>
                    <table className="issue-list-table">
                      <thead>
                        <tr>
                          <th>Product Code</th>
                          <th>Quantity (pcs)</th>
                          <th>Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {[...issueList]
                          .sort((a, b) => a.productCode.localeCompare(b.productCode))
                          .map((item) => (
                          <tr key={item.id}>
                            <td className="product-code-cell">{item.productCode}</td>
                            <td className="quantity-cell">
                              <input
                                type="number"
                                min="1"
                                value={item.quantity}
                                onChange={(e) => handleUpdateIssueListQuantity(item.id, e.target.value)}
                                className="quantity-edit-input"
                              />
                              <span className="unit-text">pcs</span>
                            </td>
                            <td>
                              <button
                                className="btn-remove"
                                onClick={() => handleRemoveFromIssueList(item.id)}
                                type="button"
                                title="Remove from list"
                              >
                                ❌
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>

                    <div className="issue-actions">
                      <button
                        className="btn-submit"
                        onClick={handleSubmitIssueList}
                        disabled={loading}
                        type="button"
                      >
                        {loading ? 'Processing...' : '✅ Issue All Items'}
                      </button>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {/* RETURN STOCK TAB */}
        {/* RETURN STOCK TAB */}
        {activeTab === 'return' && (
          <div className="tab-content">
            <h2>⬅️ Return Stock from Salesman</h2>
            <p className="subtitle">Select a salesman to view and return their stock</p>

            {/* Step 1: Select Salesman */}
            <div className="warehouse-form" style={{ maxWidth: '600px', margin: '0 auto' }}>
              <div className="form-group">
                <label>Select Salesman *</label>
                <select
                  value={selectedSalesmanForReturn}
                  onChange={(e) => handleSelectSalesmanForReturn(e.target.value)}
                  className="salesman-select"
                >
                  <option value="">-- Choose Salesman --</option>
                  {salesmen.map((s) => (
                    <option key={s.id} value={s.alias}>
                      {s.displayName}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Step 2: Show Salesman's Current Stock */}
            {selectedSalesmanForReturn && (
              <div className="return-stock-container">
                {loading ? (
                  <p className="loading">Loading stock...</p>
                ) : salesmanCurrentStock.length === 0 ? (
                  <div className="no-data">
                    <p>No stock found for {selectedSalesmanForReturn}</p>
                  </div>
                ) : (
                  <>
                    <div className="stock-header">
                      <h3>Current Stock with {selectedSalesmanForReturn}</h3>
                      <button
                        onClick={handleReturnAllStock}
                        className="btn-return-all"
                        disabled={loading}
                      >
                        🔄 Return All Stock
                      </button>
                    </div>

                    <div className="return-stock-list">
                      {salesmanCurrentStock.map((product, idx) => (
                        <div key={idx} className="return-stock-item">
                          <div className="product-info-return">
                            <div className="product-code-badge">{product.productCode}</div>
                            <div className="product-details">
                              <span className="product-variant">{product.variant || product.productName}</span>
                              <span className="current-stock-info">
                                Current: <strong>{product.quantity} pcs</strong>
                                {product.metric === 'lit' && product.metricQuantity && (
                                  <span className="volume">
                                    {' '}({(product.quantity * parseFloat(product.metricQuantity)).toFixed(2)} L)
                                  </span>
                                )}
                              </span>
                            </div>
                          </div>
                          <div className="return-quantity-input">
                            <label>Return Qty:</label>
                            <input
                              type="number"
                              min="0"
                              max={product.quantity}
                              value={returnList[product.productCode] || ''}
                              onChange={(e) => handleReturnQuantityChange(product.productCode, e.target.value)}
                              placeholder="0"
                              className="qty-input"
                            />
                            <button
                              onClick={() => handleReturnQuantityChange(product.productCode, product.quantity)}
                              className="btn-max"
                              title="Return all"
                            >
                              Max
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>

                    <div className="return-actions">
                      <button
                        onClick={handleReturnSelectedItems}
                        className="btn-submit"
                        disabled={loading}
                      >
                        {loading ? 'Processing...' : '✓ Return Selected Items'}
                      </button>
                    </div>
                  </>
                )}
              </div>
            )}
          </div>
        )}

        {/* ADJUST STOCK TAB */}
        {activeTab === 'adjust' && (
          <div className="tab-content">
            <h2>Adjust Warehouse Stock</h2>
            <form onSubmit={handleAdjustStock} className="warehouse-form">
              <div className="form-group">
                <label>Product Code *</label>
                <select
                  required
                  value={adjustForm.productCode}
                  onChange={(e) => setAdjustForm({ ...adjustForm, productCode: e.target.value })}
                >
                  <option value="">Select Product</option>
                  {products.map((p) => (
                    <option key={p.productCode} value={p.productCode}>
                      {p.productCode} - {p.productName}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Transaction Type *</label>
                <select
                  required
                  value={adjustForm.txnType}
                  onChange={(e) => setAdjustForm({ ...adjustForm, txnType: e.target.value })}
                >
                  <option value="TRANSFER_IN">Transfer In (Stock Received)</option>
                  <option value="DAMAGE">Damage (Stock Lost)</option>
                  <option value="MANUAL_ADJUST">Manual Adjustment</option>
                </select>
              </div>

              <div className="form-group">
                <label>Quantity (pcs) *</label>
                <input
                  type="number"
                  required
                  value={adjustForm.quantity}
                  onChange={(e) => setAdjustForm({ ...adjustForm, quantity: e.target.value })}
                  placeholder="Enter quantity (positive for add, will be adjusted for damage)"
                />
              </div>

              <div className="form-group">
                <label>Remarks</label>
                <textarea
                  value={adjustForm.remarks}
                  onChange={(e) => setAdjustForm({ ...adjustForm, remarks: e.target.value })}
                  placeholder="Optional notes"
                  rows="3"
                />
              </div>

              <button type="submit" disabled={loading} className="btn-submit">
                {loading ? 'Processing...' : 'Adjust Stock'}
              </button>
            </form>
          </div>
        )}

        {/* SALESMEN STOCK TAB */}
        {activeTab === 'salesmen-stock' && (
          <div className="tab-content">
            <h2>👥 Salesmen Stock Summary</h2>
            <p className="subtitle">Click on a card to view details • Click again to close</p>

            {loading ? (
              <p className="loading">Loading salesmen stock...</p>
            ) : salesmenStock.length === 0 ? (
              <p className="no-data">No stock with salesmen currently</p>
            ) : (
              <div className="salesmen-tickets-container">
                {salesmenStock.map((salesman, idx) => (
                  <div
                    key={idx}
                    className={`salesman-ticket ${selectedSalesmanCard?.salesmanAlias === salesman.salesmanAlias ? 'selected' : ''}`}
                    onClick={() => {
                      // Toggle: if same card is clicked, close it; otherwise, open the clicked card
                      if (selectedSalesmanCard?.salesmanAlias === salesman.salesmanAlias) {
                        setSelectedSalesmanCard(null);
                      } else {
                        setSelectedSalesmanCard(salesman);
                      }
                    }}
                  >
                    <div className="ticket-id">#{idx + 1}</div>
                    <div className="ticket-header">
                      <div className="ticket-title">
                        {salesman.firstName} {salesman.lastName}
                      </div>
                      <div className="ticket-alias">{salesman.salesmanAlias}</div>
                    </div>
                    <div className="ticket-footer">
                      <span className="ticket-badge products">
                        📦 {salesman.totalProducts}
                      </span>
                      <span className="ticket-badge quantity">
                        {salesman.totalQuantity} pcs
                      </span>
                      <span className="ticket-badge quantity">
                        {getTotalLiters(salesman.products).toFixed(2)} L
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Detail Panel - Shows when a card is selected */}
            {selectedSalesmanCard && (
              <div className="salesman-detail-panel">
                <div className="detail-panel-header">
                  <div className="detail-header-left">
                    <h3>
                      {selectedSalesmanCard.firstName} {selectedSalesmanCard.lastName}
                      <span className="detail-alias">({selectedSalesmanCard.salesmanAlias})</span>
                    </h3>
                    <div className="detail-summary">
                      <span className="summary-item">
                        <strong>{selectedSalesmanCard.totalProducts}</strong> {selectedSalesmanCard.totalProducts === 1 ? 'Product' : 'Products'}
                      </span>
                      <span className="summary-separator">•</span>
                      <span className="summary-item">
                        <strong>{selectedSalesmanCard.totalQuantity}</strong> Total Pieces
                      </span>
                      <span className="summary-separator">•</span>
                      <span className="summary-item">
                        <strong>{getTotalLiters(selectedSalesmanCard.products).toFixed(2)}</strong> Total Liters
                      </span>
                    </div>
                  </div>
                  <button
                    className="btn-close-detail"
                    onClick={(e) => {
                      e.stopPropagation();
                      setSelectedSalesmanCard(null);
                    }}
                  >
                    ✕
                  </button>
                </div>

                <div className="detail-products-list">
                  <h4>📋 Product Details</h4>
                  <div className="detail-products-grid">
                    {selectedSalesmanCard.products.map((product, pIdx) => (
                      <div key={pIdx} className="detail-product-card">
                        <div className="detail-product-header">
                          <span className="detail-product-code">{product.productCode}</span>
                          <span className="detail-qty-badge">{product.quantity} pcs</span>
                        </div>
                        <div className="detail-product-name">
                          {product.variant || product.productName}
                        </div>
                        {getProductLiters(product) > 0 && (
                          <div className="detail-volume-info">
                            📊 Volume: {getProductLiters(product).toFixed(2)} L
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* LEDGER TAB */}
        {activeTab === 'ledger' && (
          <div className="tab-content">
            <h2>Warehouse Ledger (Transaction History)</h2>
            {loading ? (
              <p className="loading">Loading ledger...</p>
            ) : ledger.length === 0 ? (
              <p className="no-data">No transactions found</p>
            ) : (
              <table className="ledger-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Product Code</th>
                    <th>Type</th>
                    <th>Change (Qty)</th>
                    <th>Before</th>
                    <th>After</th>
                    <th>Salesman</th>
                    <th>Remarks</th>
                  </tr>
                </thead>
                <tbody>
                  {ledger.map((entry, idx) => (
                    <tr key={idx} className={`txn-${entry.txnType.toLowerCase()}`}>
                      <td>{formatDate(entry.createdAt)}</td>
                      <td className="code">{entry.productCode}</td>
                      <td>
                        <span className={`badge badge-${entry.txnType.toLowerCase()}`}>
                          {entry.txnType.replace(/_/g, ' ')}
                        </span>
                      </td>
                      <td className={entry.deltaQty > 0 ? 'positive' : 'negative'}>
                        {entry.deltaQty > 0 ? '+' : ''}{entry.deltaQty}
                      </td>
                      <td>{entry.qtyBefore}</td>
                      <td>{entry.qtyAfter}</td>
                      <td>{entry.salesmanAlias || '-'}</td>
                      <td>{entry.remarks || '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>

      {/* Return All Confirmation Modal */}
      {showReturnAllModal && (
        <div className="modal-overlay" onClick={() => setShowReturnAllModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>🔄 Return All Stock</h2>
              <button className="modal-close" onClick={() => setShowReturnAllModal(false)}>
                ✕
              </button>
            </div>
            <div className="modal-body">
              <p className="modal-warning">
                Are you sure you want to return <strong>ALL</strong> stock from{' '}
                <strong>{selectedSalesmanForReturn}</strong>?
              </p>
              <div className="modal-summary">
                <div className="summary-item">
                  <span className="summary-label">Total Products:</span>
                  <span className="summary-value">{salesmanCurrentStock.length}</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">Total Quantity:</span>
                  <span className="summary-value">
                    {salesmanCurrentStock.reduce((sum, p) => sum + p.quantity, 0)} pcs
                  </span>
                </div>
              </div>
              <div className="modal-products-list">
                <h4>Products to be returned:</h4>
                {salesmanCurrentStock.map((product, idx) => (
                  <div key={idx} className="modal-product-item">
                    <span className="modal-product-code">{product.productCode}</span>
                    <span className="modal-product-name">{product.variant || product.productName}</span>
                    <span className="modal-product-qty">{product.quantity} pcs</span>
                  </div>
                ))}
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn-cancel" onClick={() => setShowReturnAllModal(false)}>
                Cancel
              </button>
              <button className="btn-confirm-return" onClick={confirmReturnAllStock}>
                Yes, Return All
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Issue Stock Confirmation Modal */}
      {showIssueConfirmModal && (
        <div className="modal-overlay" onClick={() => setShowIssueConfirmModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header modal-header-blue">
              <h2>➡️ Issue Stock Confirmation</h2>
              <button className="modal-close" onClick={() => setShowIssueConfirmModal(false)}>
                ✕
              </button>
            </div>
            <div className="modal-body">
              <p className="modal-warning">
                Are you sure you want to issue the following stock to{' '}
                <strong>{selectedSalesmanForIssue}</strong>?
              </p>
              <div className="modal-summary modal-summary-blue">
                <div className="summary-item">
                  <span className="summary-label">Total Products:</span>
                  <span className="summary-value">{issueList.length}</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">Total Quantity:</span>
                  <span className="summary-value">
                    {issueList.reduce((sum, item) => sum + item.quantity, 0)} pcs
                  </span>
                </div>
              </div>
              <div className="modal-products-list">
                <h4>Products to be issued:</h4>
                {issueList.map((item, idx) => (
                  <div key={idx} className="modal-product-item">
                    <span className="modal-product-code">{item.productCode}</span>
                    <span className="modal-product-name">{item.variant || item.productCode}</span>
                    <span className="modal-product-qty">{item.quantity} pcs</span>
                  </div>
                ))}
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn-cancel" onClick={() => setShowIssueConfirmModal(false)}>
                Cancel
              </button>
              <button className="btn-confirm-issue" onClick={confirmIssueStock}>
                Yes, Issue Stock
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Return Selected Items Confirmation Modal */}
      {showReturnSelectedModal && (
        <div className="modal-overlay" onClick={() => setShowReturnSelectedModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>⬅️ Return Selected Items</h2>
              <button className="modal-close" onClick={() => setShowReturnSelectedModal(false)}>
                ✕
              </button>
            </div>
            <div className="modal-body">
              <p className="modal-warning">
                Are you sure you want to return the following items from{' '}
                <strong>{selectedSalesmanForReturn}</strong>?
              </p>
              <div className="modal-summary">
                <div className="summary-item">
                  <span className="summary-label">Total Products:</span>
                  <span className="summary-value">
                    {Object.entries(returnList).filter(([_, qty]) => qty && parseInt(qty) > 0).length}
                  </span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">Total Quantity:</span>
                  <span className="summary-value">
                    {Object.entries(returnList)
                      .filter(([_, qty]) => qty && parseInt(qty) > 0)
                      .reduce((sum, [_, qty]) => sum + parseInt(qty), 0)} pcs
                  </span>
                </div>
              </div>
              <div className="modal-products-list">
                <h4>Products to be returned:</h4>
                {Object.entries(returnList)
                  .filter(([_, qty]) => qty && parseInt(qty) > 0)
                  .map(([productCode, quantity], idx) => {
                    const product = salesmanCurrentStock.find(p => p.productCode === productCode);
                    return (
                      <div key={idx} className="modal-product-item">
                        <span className="modal-product-code">{productCode}</span>
                        <span className="modal-product-name">
                          {product?.variant || product?.productName || productCode}
                        </span>
                        <span className="modal-product-qty">{quantity} pcs</span>
                      </div>
                    );
                  })}
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn-cancel" onClick={() => setShowReturnSelectedModal(false)}>
                Cancel
              </button>
              <button className="btn-confirm-return" onClick={confirmReturnSelectedItems}>
                Yes, Return Items
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default WarehouseDashboard;

