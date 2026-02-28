import React, { useState } from 'react';
import { customerAPI, salesmanAPI } from '../api';
import '../styles/AdminDashboard.css';
import '../styles/AdminOperations.css';
import { notifySuccess, notifyError } from '../utils/toast';
import AddSalesRecord from './AddSalesRecord';

const AdminOperations = () => {
  const [selectedOperation, setSelectedOperation] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [customerForm, setCustomerForm] = useState({
    customerId: '',
    shopName: '',
    customerType: '',
    routeId: '',
    villageId: ''
  });
  const [salesmanForm, setSalesmanForm] = useState({
    firstName: '',
    lastName: '',
    alias: '',
    address: '',
    contactNumber: ''
  });

  const operations = [
    { value: 'add-customer', label: 'Add Customer' },
    { value: 'add-sales', label: 'Add Sales Record' },
    { value: 'add-salesman', label: 'Add Salesman' }
  ];

  const handleOperationChange = (e) => {
    setSelectedOperation(e.target.value);
  };

  const handleCustomerChange = (e) => {
    const { name, value } = e.target;
    setCustomerForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSalesmanChange = (e) => {
    const { name, value } = e.target;
    setSalesmanForm(prev => ({ ...prev, [name]: value }));
  };

  const submitCustomer = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const payload = {
        customerId: customerForm.customerId,
        shopName: customerForm.shopName,
        customerType: customerForm.customerType,
        route: customerForm.routeId ? { routeId: parseInt(customerForm.routeId) } : null,
        village: customerForm.villageId ? { villageId: parseInt(customerForm.villageId) } : null
      };
      await customerAPI.create(payload);
      notifySuccess('Customer added successfully!');
      setCustomerForm({ customerId: '', shopName: '', customerType: '', routeId: '', villageId: '' });
    } catch (err) {
      console.error('Error:', err);
      notifyError('Failed to add customer: ' + (err.response?.data?.message || err.message));
    } finally {
      setSubmitting(false);
    }
  };

  const submitSalesman = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const payload = {
        firstName: salesmanForm.firstName,
        lastName: salesmanForm.lastName,
        alias: salesmanForm.alias,
        address: salesmanForm.address,
        contactNumber: salesmanForm.contactNumber
      };
      await salesmanAPI.create(payload);
      notifySuccess('Salesman added successfully!');
      setSalesmanForm({ firstName: '', lastName: '', alias: '', address: '', contactNumber: '' });
    } catch (err) {
      console.error('Error:', err);
      notifyError('Failed to add salesman: ' + (err.response?.data?.message || err.message));
    } finally {
      setSubmitting(false);
    }
  };

  const renderForm = () => {
    if (!selectedOperation) return null;

    if (selectedOperation === 'add-sales') {
      return <AddSalesRecord onBack={() => setSelectedOperation('')} />;
    }

    if (selectedOperation === 'add-customer') {
      return (
        <form onSubmit={submitCustomer} className="form-grid" style={{ maxWidth: '600px', margin: '20px auto' }}>
          <div className="form-control">
            <label>Customer ID *</label>
            <input
              type="text"
              name="customerId"
              value={customerForm.customerId}
              onChange={handleCustomerChange}
              required
              placeholder="e.g., ShopName_Owner_Location"
              className="input-field"
            />
          </div>
          <div className="form-control">
            <label>Shop Name *</label>
            <input
              type="text"
              name="shopName"
              value={customerForm.shopName}
              onChange={handleCustomerChange}
              required
              placeholder="e.g., Bajrang Store"
              className="input-field"
            />
          </div>
          <div className="form-control">
            <label>Customer Type *</label>
            <select
              name="customerType"
              value={customerForm.customerType}
              onChange={handleCustomerChange}
              required
              className="select-field"
            >
              <option value="">Select Type</option>
              <option value="Dealer">Dealer</option>
              <option value="Subdealer">Subdealer</option>
              <option value="Shopkeeper">Shopkeeper</option>
              <option value="Household">Household</option>
            </select>
          </div>
          <div className="form-control">
            <label>Route ID</label>
            <input
              type="number"
              name="routeId"
              value={customerForm.routeId}
              onChange={handleCustomerChange}
              placeholder="e.g., 1"
              className="input-field"
            />
          </div>
          <div className="form-control">
            <label>Village ID</label>
            <input
              type="number"
              name="villageId"
              value={customerForm.villageId}
              onChange={handleCustomerChange}
              placeholder="e.g., 10"
              className="input-field"
            />
          </div>
          <div className="form-actions" style={{ gridColumn: '1 / -1' }}>
            <button type="submit" disabled={submitting} className={`btn-submit ${submitting ? 'disabled' : ''}`}>
              {submitting ? 'Adding...' : 'Add Customer'}
            </button>
          </div>
        </form>
      );
    }

    if (selectedOperation === 'add-salesman') {
      return (
        <form onSubmit={submitSalesman} className="form-grid" style={{ maxWidth: '600px', margin: '20px auto' }}>
          <div className="form-control">
            <label>First Name *</label>
            <input
              type="text"
              name="firstName"
              value={salesmanForm.firstName}
              onChange={handleSalesmanChange}
              required
              placeholder="e.g., Mukul"
              className="input-field"
            />
          </div>
          <div className="form-control">
            <label>Last Name *</label>
            <input
              type="text"
              name="lastName"
              value={salesmanForm.lastName}
              onChange={handleSalesmanChange}
              required
              placeholder="e.g., Kumar"
              className="input-field"
            />
          </div>
          <div className="form-control">
            <label>Alias *</label>
            <input
              type="text"
              name="alias"
              value={salesmanForm.alias}
              onChange={handleSalesmanChange}
              required
              placeholder="e.g., Mukul/Antrich"
              className="input-field"
            />
          </div>
          <div className="form-control">
            <label>Address</label>
            <input
              type="text"
              name="address"
              value={salesmanForm.address}
              onChange={handleSalesmanChange}
              placeholder="e.g., Gola"
              className="input-field"
            />
          </div>
          <div className="form-control">
            <label>Contact Number</label>
            <input
              type="tel"
              name="contactNumber"
              value={salesmanForm.contactNumber}
              onChange={handleSalesmanChange}
              placeholder="e.g., +91XXXXXXXXXX"
              className="input-field"
            />
          </div>
          <div className="form-actions" style={{ gridColumn: '1 / -1' }}>
            <button type="submit" disabled={submitting} className={`btn-submit ${submitting ? 'disabled' : ''}`}>
              {submitting ? 'Adding...' : 'Add Salesman'}
            </button>
          </div>
        </form>
      );
    }
  };

  return (
    <div className="admin-container">
      <div className="header">
        <h2>Admin Operations</h2>
      </div>

      <div style={{ maxWidth: '600px', margin: '30px auto', textAlign: 'center' }}>
        <label style={{ display: 'block', marginBottom: '10px', fontSize: '18px', fontWeight: 'bold' }}>
          Select Operation
        </label>
        <select
          value={selectedOperation}
          onChange={handleOperationChange}
          className="select-field"
          style={{ width: '100%', padding: '12px', fontSize: '16px' }}
        >
          <option value="">-- Choose an operation --</option>
          {operations.map(op => (
            <option key={op.value} value={op.value}>{op.label}</option>
          ))}
        </select>
      </div>

      {renderForm()}
    </div>
  );
};

export default AdminOperations;
