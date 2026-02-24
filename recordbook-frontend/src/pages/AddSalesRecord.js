import React, { useState } from 'react';
import api from '../api';
import { notifySuccess, notifyError } from '../utils/toast';

const AddSalesRecord = ({ onBack }) => {
  const [salesJsonInput, setSalesJsonInput] = useState('');
  const [salesJsonData, setSalesJsonData] = useState(null);
  const [salesStep, setSalesStep] = useState('input'); // input, review, route-select, village-select, salesman-select, confirm
  const [selectedRoute, setSelectedRoute] = useState('');
  const [selectedVillage, setSelectedVillage] = useState('');
  const [selectedSalesman, setSelectedSalesman] = useState('');
  const [submitting, setSubmitting] = useState(false);
  
  // Dropdown data
  const [routes, setRoutes] = useState([]);
  const [villages, setVillages] = useState([]);
  const [salesmen, setSalesmen] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  
  // Loading states
  const [masterLoading, setMasterLoading] = useState(false);
  const [masterLoaded, setMasterLoaded] = useState(false);

  // Add forms states
  const [showAddRoute, setShowAddRoute] = useState(false);
  const [showAddVillage, setShowAddVillage] = useState(false);
  const [showAddSalesman, setShowAddSalesman] = useState(false);
  const [newRoute, setNewRoute] = useState({ routeName: '' });
  const [newVillage, setNewVillage] = useState({ villageName: '' });
  const [newSalesman, setNewSalesman] = useState({ firstName: '', lastName: '', address: '', contactNumber: '' });

  // Load master data on demand (no auto API calls on page load)
  const loadMasterData = async () => {
    setMasterLoading(true);
    try {
      await fetchRoutes();
      await fetchSalesmen();
      await fetchCustomers();
      await fetchProducts();
      setMasterLoaded(true);
    } catch (err) {
      notifyError('Failed to load master data');
    } finally {
      setMasterLoading(false);
    }
  };

  const fetchRoutes = async () => {
    try {
      const response = await api.get('/api/routes');
      setRoutes(response.data || []);
    } catch (err) {
      console.error('Failed to fetch routes:', err);
    }
  };

  const fetchSalesmen = async () => {
    try {
      const response = await api.get('/api/salesmen');
      setSalesmen(response.data || []);
    } catch (err) {
      console.error('Failed to fetch salesmen:', err);
    }
  };

  const fetchCustomers = async () => {
    try {
      const response = await api.get('/api/customers');
      setCustomers(response.data || []);
    } catch (err) {
      console.error('Failed to fetch customers:', err);
    }
  };

  const fetchProducts = async () => {
    try {
      const response = await api.get('/api/products');
      setProducts(response.data || []);
    } catch (err) {
      console.error('Failed to fetch products:', err);
    }
  };

  const fetchVillagesByRoute = async (routeId) => {
    if (!routeId) {
      setVillages([]);
      return;
    }
    try {
      const response = await api.get(`/api/routes/${routeId}/villages`);
      setVillages(response.data || []);
    } catch (err) {
      console.error('Failed to fetch villages:', err);
      setVillages([]);
    }
  };

  const handleParseSalesJson = () => {
    try {
      const parsed = JSON.parse(salesJsonInput);
      setSalesJsonData(parsed);
      setSalesStep('review');
    } catch (err) {
      notifyError('Invalid JSON format. Please check and try again.');
    }
  };

  const handleRouteSelection = (routeId) => {
    setSelectedRoute(routeId);
    if (routeId) {
      fetchVillagesByRoute(routeId);
    } else {
      setVillages([]);
    }
    setSalesStep('village-select');
  };

  const handleAddRoute = async () => {
    if (!newRoute.routeName.trim()) {
      notifyError('Please enter a route name');
      return;
    }
    try {
      await api.post('/api/routes', { routeName: newRoute.routeName });
      notifySuccess('Route added successfully!');
      await fetchRoutes();
      setNewRoute({ routeName: '' });
      setShowAddRoute(false);
    } catch (err) {
      notifyError('Failed to add route');
    }
  };

  const handleVillageSelection = (villageId) => {
    setSelectedVillage(villageId);
    setSalesStep('salesman-select');
  };

  const handleAddVillage = async () => {
    if (!newVillage.villageName.trim() || !selectedRoute) {
      notifyError('Please enter a village name and select a route');
      return;
    }
    try {
      await api.post('/api/routes/villages', { 
        routeId: parseInt(selectedRoute), 
        villageName: newVillage.villageName 
      });
      notifySuccess('Village added successfully!');
      await fetchVillagesByRoute(selectedRoute);
      setNewVillage({ villageName: '' });
      setShowAddVillage(false);
    } catch (err) {
      notifyError('Failed to add village');
    }
  };

  const handleSalesmanSelection = (salesmanId) => {
    setSelectedSalesman(salesmanId);
    setSalesStep('confirm');
  };

  const handleAddSalesman = async () => {
    if (!newSalesman.firstName.trim() || !newSalesman.lastName.trim()) {
      notifyError('Please enter first and last name');
      return;
    }
    try {
      await api.post('/api/v1/admin/salesmen', newSalesman);
      notifySuccess('Salesman added successfully!');
      await fetchSalesmen();
      setNewSalesman({ firstName: '', lastName: '', address: '', contactNumber: '' });
      setShowAddSalesman(false);
    } catch (err) {
      notifyError('Failed to add salesman');
    }
  };

  const handleSubmitSalesJson = async () => {
    if (!selectedRoute || !selectedSalesman) {
      notifyError('Please select a route and salesman');
      return;
    }
    setSubmitting(true);
    try {
      const formatDate = (dateStr) => {
        // Convert DDMMYYYY to YYYY-MM-DD
        if (dateStr && dateStr.length === 8) {
          return `${dateStr.slice(4)}-${dateStr.slice(2, 4)}-${dateStr.slice(0, 2)}`;
        }
        return dateStr;
      };

      // Create one sales record per item
      const items = salesJsonData.items || [];
      if (items.length === 0) {
        notifyError('No items found in sales record');
        setSubmitting(false);
        return;
      }

      // Find matching customer or use data from JSON
      const matchingCustomer = customers.find(c => c.shopName === salesJsonData.customer?.name);
      
      for (const item of items) {
        // Find product by name/code
        const matchingProduct = products.find(p => 
          p.productName === item.productName || p.productCode === item.productCode
        );

        const payload = {
          orderDate: formatDate(salesJsonData.saleDate),
          customerId: matchingCustomer?.customerId || null,
          shopName: salesJsonData.customer?.name,
          customerType: salesJsonData.customer?.type || 'SHOPKEEPER',
          mobileNumber: salesJsonData.customer?.mobileNumber,
          routeId: parseInt(selectedRoute),
          villageId: selectedVillage ? parseInt(selectedVillage) : null,
          salesmanId: parseInt(selectedSalesman),
          productId: matchingProduct?.productId || null,
          productName: item.productName,
          productCode: item.productCode,
          quantity: item.quantity,
          actualRate: item.rate,
          revenue: item.revenue,
          totalRevenue: salesJsonData.totalRevenue,
          amountReceived: salesJsonData.amountReceived,
          balanceDue: salesJsonData.balanceDue,
          paymentMode: salesJsonData.paymentMode,
          transactionReference: salesJsonData.transactionReference
        };
        
        await api.post('/api/admin/sales', payload);
      }

      notifySuccess(`Sales record added successfully! (${items.length} item${items.length > 1 ? 's' : ''})`);
      setSalesJsonInput('');
      setSalesJsonData(null);
      setSalesStep('input');
      setSelectedRoute('');
      setSelectedVillage('');
      setSelectedSalesman('');
      onBack(); // Return to admin dashboard
    } catch (err) {
      console.error('Error:', err);
      notifyError('Failed to save sales record: ' + (err.response?.data?.message || err.message));
    } finally {
      setSubmitting(false);
    }
  };

  // Input Step
  if (salesStep === 'input') {
    return (
      <div style={{ maxWidth: '700px', margin: '20px auto', padding: '20px', backgroundColor: '#f9f9f9', borderRadius: '8px' }}>
        <h3 style={{ marginBottom: '15px' }}>Add Sales Record - JSON Input</h3>
        <p style={{ color: '#666', marginBottom: '15px', fontSize: '14px' }}>
          Paste the sales record JSON from the sales guy. Must include saleDate, customer, items array, and payment details. Route/Village/Salesman will be selected below.
        </p>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
          <button
            type="button"
            onClick={loadMasterData}
            disabled={masterLoading}
            style={{
              padding: '8px 16px',
              backgroundColor: '#16a34a',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: masterLoading ? 'not-allowed' : 'pointer'
            }}
          >
            {masterLoading ? 'Loading...' : masterLoaded ? 'Reload Master Data' : 'Load Master Data'}
          </button>
          <span style={{ fontSize: '12px', color: '#666' }}>
            Loads routes, salesmen, customers, and products
          </span>
        </div>
        <textarea
          value={salesJsonInput}
          onChange={(e) => setSalesJsonInput(e.target.value)}
          placeholder={JSON.stringify({
            saleDate: "21022026",
            customer: {
              name: "Kumar Distributor",
              type: "SHOPKEEPER",
              mobileNumber: "7717748370"
            },
            items: [
              {
                productCode: "UL1L",
                productName: "Urvi Clean Lemon 1L",
                quantity: 60,
                rate: 32,
                revenue: 1920
              }
            ],
            totalRevenue: 1920,
            amountReceived: 1920,
            balanceDue: 0,
            paymentMode: "PHONEPE",
            transactionReference: "PPX78654321"
          }, null, 2)}
          style={{
            width: '100%',
            height: '350px',
            padding: '10px',
            fontFamily: 'monospace',
            fontSize: '12px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            marginBottom: '15px'
          }}
        />
        <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
          <button 
            onClick={onBack}
            style={{
              padding: '8px 16px',
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Cancel
          </button>
          <button 
            onClick={handleParseSalesJson}
            style={{
              padding: '8px 16px',
              backgroundColor: '#16a34a',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Parse & Continue
          </button>
        </div>
      </div>
    );
  }

  // Review Step
  if (salesStep === 'review') {
    const saleData = salesJsonData || {};
    const formatDate = (dateStr) => {
      // Convert DDMMYYYY to YYYY-MM-DD
      if (dateStr && dateStr.length === 8) {
        return `${dateStr.slice(4)}-${dateStr.slice(2, 4)}-${dateStr.slice(0, 2)}`;
      }
      return dateStr;
    };

    return (
      <div style={{ maxWidth: '700px', margin: '20px auto', padding: '20px', backgroundColor: '#f9f9f9', borderRadius: '8px' }}>
        <h3 style={{ marginBottom: '15px' }}>Sales Record Details - Review</h3>
        
        <div style={{
          backgroundColor: 'white',
          padding: '15px',
          borderRadius: '4px',
          border: '1px solid #ddd',
          marginBottom: '15px'
        }}>
          <h4>Basic Info</h4>
          <p><strong>Sale Date:</strong> {formatDate(saleData.saleDate)}</p>
          <p><strong>Route/Village/Salesman:</strong> Selected in next steps</p>
          
          <h4 style={{ marginTop: '15px' }}>Customer</h4>
          <p><strong>Name:</strong> {saleData.customer?.name}</p>
          <p><strong>Type:</strong> {saleData.customer?.type}</p>
          <p><strong>Mobile:</strong> {saleData.customer?.mobileNumber}</p>

          <h4 style={{ marginTop: '15px' }}>Items ({saleData.items?.length || 0})</h4>
          {saleData.items && saleData.items.map((item, idx) => (
            <div key={idx} style={{ 
              padding: '10px', 
              marginBottom: '10px', 
              backgroundColor: '#f0f0f0', 
              borderRadius: '4px'
            }}>
              <p><strong>{item.productName}</strong> ({item.productCode})</p>
              <p>Qty: {item.quantity} × Rate: {item.rate} = ₹{item.revenue}</p>
            </div>
          ))}

          <h4 style={{ marginTop: '15px' }}>Payment Details</h4>
          <p><strong>Total Revenue:</strong> ₹{saleData.totalRevenue}</p>
          <p><strong>Amount Received:</strong> ₹{saleData.amountReceived}</p>
          <p><strong>Balance Due:</strong> ₹{saleData.balanceDue}</p>
          <p><strong>Payment Mode:</strong> {saleData.paymentMode}</p>
          {saleData.transactionReference && <p><strong>Transaction Ref:</strong> {saleData.transactionReference}</p>}
        </div>

        <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
          <button 
            onClick={() => setSalesStep('input')}
            style={{
              padding: '8px 16px',
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Back
          </button>
          <button 
            onClick={() => setSalesStep('route-select')}
            style={{
              padding: '8px 16px',
              backgroundColor: '#16a34a',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Next: Confirm Details
          </button>
        </div>
      </div>
    );
  }

  // Route Selection Step
  if (salesStep === 'route-select') {
    return (
      <div style={{ maxWidth: '700px', margin: '20px auto', padding: '20px', backgroundColor: '#f9f9f9', borderRadius: '8px' }}>
        <h3 style={{ marginBottom: '15px' }}>Select Route</h3>
        
        {showAddRoute ? (
          <div style={{ 
            backgroundColor: 'white', 
            padding: '15px', 
            borderRadius: '4px', 
            marginBottom: '15px',
            border: '1px solid #ddd'
          }}>
            <h4>Add New Route</h4>
            <input
              type="text"
              placeholder="Route Name"
              value={newRoute.routeName}
              onChange={(e) => setNewRoute({ routeName: e.target.value })}
              style={{
                width: '100%',
                padding: '8px',
                marginBottom: '10px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                boxSizing: 'border-box'
              }}
            />
            <div style={{ display: 'flex', gap: '10px' }}>
              <button 
                onClick={() => setShowAddRoute(false)}
                style={{
                  padding: '6px 12px',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Cancel
              </button>
              <button 
                onClick={handleAddRoute}
                style={{
                  padding: '6px 12px',
                  backgroundColor: '#28a745',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Add Route
              </button>
            </div>
          </div>
        ) : (
          <>
            <select
              value={selectedRoute}
              onChange={(e) => handleRouteSelection(e.target.value)}
              style={{
                width: '100%',
                padding: '10px',
                marginBottom: '10px',
                border: '1px solid #ddd',
                borderRadius: '4px'
              }}
            >
              <option value="">Select a Route</option>
              {routes.map(route => (
                <option key={route.routeId} value={route.routeId}>
                  {route.routeName}
                </option>
              ))}
            </select>
            <button 
              onClick={() => setShowAddRoute(true)}
              style={{
                width: '100%',
                padding: '8px',
                marginTop: '10px',
                backgroundColor: '#17a2b8',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              + Add New Route
            </button>
          </>
        )}

        <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '15px' }}>
          <button 
            onClick={() => setSalesStep('review')}
            style={{
              padding: '8px 16px',
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Back
          </button>
          <button 
            onClick={() => handleRouteSelection(selectedRoute)}
            disabled={!selectedRoute}
            style={{
              padding: '8px 16px',
              backgroundColor: selectedRoute ? '#16a34a' : '#ccc',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: selectedRoute ? 'pointer' : 'not-allowed'
            }}
          >
            Next: Select Village
          </button>
        </div>
      </div>
    );
  }

  // Village Selection Step
  if (salesStep === 'village-select') {
    return (
      <div style={{ maxWidth: '700px', margin: '20px auto', padding: '20px', backgroundColor: '#f9f9f9', borderRadius: '8px' }}>
        <h3 style={{ marginBottom: '15px' }}>Select Village (Optional)</h3>
        
        {showAddVillage ? (
          <div style={{ 
            backgroundColor: 'white', 
            padding: '15px', 
            borderRadius: '4px', 
            marginBottom: '15px',
            border: '1px solid #ddd'
          }}>
            <h4>Add New Village</h4>
            <input
              type="text"
              placeholder="Village Name"
              value={newVillage.villageName}
              onChange={(e) => setNewVillage({ villageName: e.target.value })}
              style={{
                width: '100%',
                padding: '8px',
                marginBottom: '10px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                boxSizing: 'border-box'
              }}
            />
            <div style={{ display: 'flex', gap: '10px' }}>
              <button 
                onClick={() => setShowAddVillage(false)}
                style={{
                  padding: '6px 12px',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Cancel
              </button>
              <button 
                onClick={handleAddVillage}
                style={{
                  padding: '6px 12px',
                  backgroundColor: '#28a745',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Add Village
              </button>
            </div>
          </div>
        ) : (
          <>
            <select
              value={selectedVillage}
              onChange={(e) => handleVillageSelection(e.target.value)}
              style={{
                width: '100%',
                padding: '10px',
                marginBottom: '10px',
                border: '1px solid #ddd',
                borderRadius: '4px'
              }}
            >
              <option value="">No Village / Skip</option>
              {villages.map(village => (
                <option key={village.villageId} value={village.villageId}>
                  {village.villageName}
                </option>
              ))}
            </select>
            <button 
              onClick={() => setShowAddVillage(true)}
              style={{
                width: '100%',
                padding: '8px',
                marginTop: '10px',
                backgroundColor: '#17a2b8',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              + Add New Village
            </button>
          </>
        )}

        <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '15px' }}>
          <button 
            onClick={() => setSalesStep('route-select')}
            style={{
              padding: '8px 16px',
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Back
          </button>
          <button 
            onClick={() => setSalesStep('salesman-select')}
            style={{
              padding: '8px 16px',
              backgroundColor: '#16a34a',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Next: Select Salesman
          </button>
        </div>
      </div>
    );
  }

  // Salesman Selection Step
  if (salesStep === 'salesman-select') {
    return (
      <div style={{ maxWidth: '700px', margin: '20px auto', padding: '20px', backgroundColor: '#f9f9f9', borderRadius: '8px' }}>
        <h3 style={{ marginBottom: '15px' }}>Select Salesman</h3>
        
        {showAddSalesman ? (
          <div style={{ 
            backgroundColor: 'white', 
            padding: '15px', 
            borderRadius: '4px', 
            marginBottom: '15px',
            border: '1px solid #ddd'
          }}>
            <h4>Add New Salesman</h4>
            <input
              type="text"
              placeholder="First Name"
              value={newSalesman.firstName}
              onChange={(e) => setNewSalesman({ ...newSalesman, firstName: e.target.value })}
              style={{
                width: '100%',
                padding: '8px',
                marginBottom: '10px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                boxSizing: 'border-box'
              }}
            />
            <input
              type="text"
              placeholder="Last Name"
              value={newSalesman.lastName}
              onChange={(e) => setNewSalesman({ ...newSalesman, lastName: e.target.value })}
              style={{
                width: '100%',
                padding: '8px',
                marginBottom: '10px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                boxSizing: 'border-box'
              }}
            />
            <input
              type="text"
              placeholder="Address"
              value={newSalesman.address}
              onChange={(e) => setNewSalesman({ ...newSalesman, address: e.target.value })}
              style={{
                width: '100%',
                padding: '8px',
                marginBottom: '10px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                boxSizing: 'border-box'
              }}
            />
            <input
              type="text"
              placeholder="Contact Number"
              value={newSalesman.contactNumber}
              onChange={(e) => setNewSalesman({ ...newSalesman, contactNumber: e.target.value })}
              style={{
                width: '100%',
                padding: '8px',
                marginBottom: '10px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                boxSizing: 'border-box'
              }}
            />
            <div style={{ display: 'flex', gap: '10px' }}>
              <button 
                onClick={() => setShowAddSalesman(false)}
                style={{
                  padding: '6px 12px',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Cancel
              </button>
              <button 
                onClick={handleAddSalesman}
                style={{
                  padding: '6px 12px',
                  backgroundColor: '#28a745',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Add Salesman
              </button>
            </div>
          </div>
        ) : (
          <>
            <select
              value={selectedSalesman}
              onChange={(e) => handleSalesmanSelection(e.target.value)}
              style={{
                width: '100%',
                padding: '10px',
                marginBottom: '10px',
                border: '1px solid #ddd',
                borderRadius: '4px'
              }}
            >
              <option value="">Select a Salesman</option>
              {salesmen.map(salesman => (
                <option key={salesman.salesmanId} value={salesman.salesmanId}>
                  {salesman.firstName} {salesman.lastName}
                </option>
              ))}
            </select>
            <button 
              onClick={() => setShowAddSalesman(true)}
              style={{
                width: '100%',
                padding: '8px',
                marginTop: '10px',
                backgroundColor: '#17a2b8',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              + Add New Salesman
            </button>
          </>
        )}

        <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '15px' }}>
          <button 
            onClick={() => setSalesStep('village-select')}
            style={{
              padding: '8px 16px',
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Back
          </button>
          <button 
            onClick={() => handleSalesmanSelection(selectedSalesman)}
            disabled={!selectedSalesman}
            style={{
              padding: '8px 16px',
              backgroundColor: selectedSalesman ? '#16a34a' : '#ccc',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: selectedSalesman ? 'pointer' : 'not-allowed'
            }}
          >
            Next: Review & Submit
          </button>
        </div>
      </div>
    );
  }

  // Confirm Step
  if (salesStep === 'confirm') {
    const saleData = salesJsonData || {};
    const selectedRouteName = routes.find(r => r.routeId === selectedRoute)?.routeName || 'Not selected';
    const selectedVillageName = selectedVillage ? villages.find(v => v.villageId === selectedVillage)?.villageName : 'None';
    const selectedSalesmanObj = salesmen.find(s => s.salesmanId === selectedSalesman);
    const selectedSalesmanName = selectedSalesmanObj ? `${selectedSalesmanObj.firstName} ${selectedSalesmanObj.lastName}` : 'Not selected';

    return (
      <div style={{ maxWidth: '700px', margin: '20px auto', padding: '20px', backgroundColor: '#f9f9f9', borderRadius: '8px' }}>
        <h3 style={{ marginBottom: '15px' }}>Confirm Sales Record</h3>
        
        <div style={{
          backgroundColor: 'white',
          padding: '15px',
          borderRadius: '4px',
          border: '1px solid #ddd',
          marginBottom: '15px'
        }}>
          <h4>Summary:</h4>
          <p><strong>Items:</strong> {saleData.items?.length || 0} product(s)</p>
          <p><strong>Total Revenue:</strong> ₹{saleData.totalRevenue}</p>
          <p><strong>Amount Received:</strong> ₹{saleData.amountReceived}</p>
          <p><strong>Balance Due:</strong> ₹{saleData.balanceDue}</p>
          
          <h4 style={{ marginTop: '15px' }}>Customer:</h4>
          <p><strong>Name:</strong> {saleData.customer?.name}</p>
          <p><strong>Type:</strong> {saleData.customer?.type}</p>
          <p><strong>Mobile:</strong> {saleData.customer?.mobileNumber}</p>
          
          <h4 style={{ marginTop: '15px' }}>Selected Details:</h4>
          <p><strong>Route:</strong> {selectedRouteName}</p>
          <p><strong>Village:</strong> {selectedVillageName}</p>
          <p><strong>Salesman:</strong> {selectedSalesmanName}</p>
          
          <h4 style={{ marginTop: '15px' }}>Items List:</h4>
          {saleData.items && saleData.items.map((item, idx) => (
            <div key={idx} style={{
              padding: '8px',
              marginBottom: '8px',
              backgroundColor: '#f0f0f0',
              borderRadius: '4px',
              fontSize: '13px'
            }}>
              {item.productName} - Qty: {item.quantity} × ₹{item.rate} = ₹{item.revenue}
            </div>
          ))}
        </div>

        <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
          <button 
            onClick={() => setSalesStep('salesman-select')}
            style={{
              padding: '8px 16px',
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Back
          </button>
          <button 
            onClick={handleSubmitSalesJson}
            disabled={submitting}
            style={{
              padding: '8px 16px',
              backgroundColor: submitting ? '#ccc' : '#28a745',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: submitting ? 'not-allowed' : 'pointer'
            }}
          >
            {submitting ? 'Submitting...' : 'Submit Sales Record'}
          </button>
        </div>
      </div>
    );
  }
};

export default AddSalesRecord;
