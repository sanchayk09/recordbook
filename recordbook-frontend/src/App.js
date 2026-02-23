import React from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import Header from './components/Header';
import { ToastContainer } from 'react-toastify';
import AboutUs from './pages/AboutUs';
import AdminDashboard from './pages/AdminDashboard';
import AdminOperations from './pages/AdminOperations';
import DailySalesDump from './pages/DailySalesDump';
import DailySalesSummary from './pages/DailySalesSummary';

function AppContent() {
  const location = useLocation();

  return (
    <>
      <Header />
      <ToastContainer />
      <div className="page-container">
        <Routes>
          <Route path="/" element={<AboutUs />} />
          <Route path="/admin" element={<AdminDashboard />} />
          <Route path="/admin-operations" element={<AdminOperations />} />
          <Route path="/daily-sales" element={<DailySalesDump />} />
          <Route path="/daily-sales-summary" element={<DailySalesSummary />} />
        </Routes>
      </div>
    </>
  );
}

function App() {
  return (
    <Router>
      <AppContent />
    </Router>
  );
}

export default App;