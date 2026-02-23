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
        <Link to="/admin" className="nav-button admin">Admin</Link>
        <Link to="/admin-operations" className="nav-button admin">Admin Ops</Link>
        <Link to="/daily-sales" className="nav-button">Daily Sales</Link>
        <Link to="/daily-sales-summary" className="nav-button">Daily Sales Table</Link>
      </div>
    </nav>
  );
};

export default Header;
