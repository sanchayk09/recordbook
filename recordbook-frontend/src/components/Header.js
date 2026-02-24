import React from 'react';
import { Link } from 'react-router-dom';
import '../styles/Header.css';

const Header = () => {
  return (
    <nav className="site-nav">
      <div className="brand">
        <strong>UrviClean</strong>
        <span>PVT LTD.</span>
      </div>
      <div className="nav-links">
        <Link to="/" className="nav-button">Home</Link>
        <div className="dropdown">
          <button className="nav-button admin dropdown-toggle">Admin</button>
          <div className="dropdown-menu">
            <Link to="/admin" className="dropdown-item">Admin</Link>
            <Link to="/product-cost-manager" className="dropdown-item">Product Cost Manager</Link>
          </div>
        </div>
        <div className="dropdown">
          <button className="nav-button admin dropdown-toggle">Admin Ops</button>
          <div className="dropdown-menu">
            <Link to="/admin-operations" className="dropdown-item">Admin Operations</Link>
            <Link to="/daily-sales" className="dropdown-item">Add Daily Sale</Link>
            <Link to="/daily-sales-summary" className="dropdown-item">View Daily Sale</Link>
            <Link to="/product-sales-summary" className="dropdown-item">Product Sale Summary</Link>
            <Link to="/daily-summary-report" className="dropdown-item">Daily Summary Report</Link>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Header;
