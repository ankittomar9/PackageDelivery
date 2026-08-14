import React from 'react';
import { useAuth } from '../context/AuthContext';
import { PackageCheck, ShieldCheck, LogOut, Cpu, CreditCard, Box } from 'lucide-react';

export const Navbar = ({ activeStep }) => {
  const { user, isAuthenticated, logout } = useAuth();

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <div className="brand-group">
          <div className="logo-badge">
            <PackageCheck className="logo-icon" size={28} />
          </div>
          <div>
            <h1 className="brand-title">ReturnOrder Platform</h1>
            <p className="brand-subtitle">Enterprise Component Return & RMA Management</p>
          </div>
        </div>

        <div className="nav-right">
          {/* Microservices Status Indicator */}
          <div className="services-health-panel">
            <span className="health-label">System Mesh:</span>
            <div className="status-chip" title="jwtAuthentication: Port 8084">
              <span className="status-dot online"></span>
              <ShieldCheck size={14} /> Auth :8084
            </div>
            <div className="status-chip" title="ComponentProcessing: Port 8081">
              <span className="status-dot online"></span>
              <Cpu size={14} /> Processing :8081
            </div>
            <div className="status-chip" title="PackagingAndDelivery: Port 8082">
              <span className="status-dot online"></span>
              <Box size={14} /> Logistics :8082
            </div>
            <div className="status-chip" title="PaymentService: Port 8083">
              <span className="status-dot online"></span>
              <CreditCard size={14} /> Payment :8083
            </div>
          </div>

          {isAuthenticated && (
            <div className="user-profile-badge">
              <div className="avatar-circle">{user ? user.charAt(0).toUpperCase() : 'A'}</div>
              <span className="user-name">{user || 'User'}</span>
              <button className="btn-logout" onClick={logout} title="Sign Out">
                <LogOut size={16} />
              </button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};
