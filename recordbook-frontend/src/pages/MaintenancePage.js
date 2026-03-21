import React, { useState } from 'react';
import { maintenanceAPI } from '../api';
import { notifyError, notifySuccess } from '../utils/toast';
import '../styles/AdminDashboard.css';

const DB_NAME_PATTERN = /^[A-Za-z0-9_]+$/;

const MaintenancePage = () => {
  const [form, setForm] = useState({
    sourceDbName: '',
    backupDbName: ''
  });
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const validate = () => {
    if (!form.sourceDbName.trim() || !form.backupDbName.trim()) {
      notifyError('Source DB name and backup DB name are required.');
      return false;
    }

    if (!DB_NAME_PATTERN.test(form.sourceDbName) || !DB_NAME_PATTERN.test(form.backupDbName)) {
      notifyError('Only letters, numbers, and underscore are allowed in database names.');
      return false;
    }

    if (form.sourceDbName.trim().toLowerCase() === form.backupDbName.trim().toLowerCase()) {
      notifyError('Source and backup database names must be different.');
      return false;
    }

    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setResult(null);

    if (!validate()) {
      return;
    }

    setSubmitting(true);
    try {
      const response = await maintenanceAPI.copyDatabase({
        sourceDbName: form.sourceDbName.trim(),
        backupDbName: form.backupDbName.trim()
      });
      setResult(response.data);
      notifySuccess('Database copy completed successfully.');
    } catch (err) {
      notifyError('Failed to copy database: ' + (err.response?.data?.message || err.message));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="admin-container">
      <div className="header">
        <h2>Maintenance</h2>
        <p style={{ marginTop: '8px', color: '#555' }}>
          Copy one MySQL database into another on the same configured server.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="form-grid" style={{ maxWidth: '700px', margin: '30px auto' }}>
        <div className="form-control">
          <label>Source Database *</label>
          <input
            type="text"
            name="sourceDbName"
            value={form.sourceDbName}
            onChange={handleChange}
            required
            placeholder="e.g., urviclean_trial_05032026"
            className="input-field"
          />
        </div>

        <div className="form-control">
          <label>Backup Database *</label>
          <input
            type="text"
            name="backupDbName"
            value={form.backupDbName}
            onChange={handleChange}
            required
            placeholder="e.g., urviclean_trial_backup"
            className="input-field"
          />
        </div>

        <div className="form-actions" style={{ gridColumn: '1 / -1' }}>
          <button type="submit" disabled={submitting} className={`btn-submit ${submitting ? 'disabled' : ''}`}>
            {submitting ? 'Copying Database...' : 'Start Database Copy'}
          </button>
        </div>
      </form>

      {result && (
        <div className="content-card" style={{ maxWidth: '700px', margin: '0 auto 30px' }}>
          <h3>Copy Result</h3>
          <p><strong>Source DB:</strong> {result.sourceDbName}</p>
          <p><strong>Backup DB:</strong> {result.backupDbName}</p>
          <p><strong>Tables Copied:</strong> {result.tablesCopied}</p>
          <p><strong>Rows Copied:</strong> {result.rowsCopied}</p>
          <p><strong>Message:</strong> {result.message}</p>
          <div style={{ marginTop: '16px' }}>
            <strong>Copied Tables</strong>
            <ul style={{ marginTop: '8px' }}>
              {(result.copiedTables || []).map((tableName) => (
                <li key={tableName}>{tableName}</li>
              ))}
            </ul>
          </div>
        </div>
      )}
    </div>
  );
};

export default MaintenancePage;

