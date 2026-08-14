import React from 'react';
import { Calendar, CheckCircle2, CreditCard, ShieldCheck, Truck, RefreshCw, ArrowRight } from 'lucide-react';

export const CalculationReview = ({ responseData, onProceedToPayment, onReset }) => {
  if (!responseData) return null;

  const totalCharge = responseData.processingCharge + responseData.packagingAndDeliveryCharge;

  return (
    <div className="card-panel review-panel">
      <div className="card-header-styled success-header">
        <div className="header-icon-box success-icon">
          <CheckCircle2 size={28} />
        </div>
        <div>
          <h3>Return Processing Calculated!</h3>
          <p>Order Request #{responseData.requestId} successfully analyzed by microservices mesh</p>
        </div>
      </div>

      <div className="review-grid">
        {/* Item 1: Processing Fee */}
        <div className="summary-card">
          <div className="summary-icon blue">
            <ShieldCheck size={20} />
          </div>
          <div className="summary-info">
            <span className="summary-label">Component Processing Fee</span>
            <span className="summary-value">₹{responseData.processingCharge.toFixed(2)}</span>
          </div>
        </div>

        {/* Item 2: Packaging Tariff */}
        <div className="summary-card">
          <div className="summary-icon orange">
            <Truck size={20} />
          </div>
          <div className="summary-info">
            <span className="summary-label">Logistics & Packaging Tariff</span>
            <span className="summary-value">₹{responseData.packagingAndDeliveryCharge.toFixed(2)}</span>
          </div>
        </div>

        {/* Item 3: Estimated Delivery Date */}
        <div className="summary-card">
          <div className="summary-icon purple">
            <Calendar size={20} />
          </div>
          <div className="summary-info">
            <span className="summary-label">Estimated Delivery Turnaround</span>
            <span className="summary-value highlight-date">{responseData.dateOfDelivery}</span>
          </div>
        </div>
      </div>

      <div className="total-banner">
        <div className="total-left">
          <span className="total-title">Total Charge Amount:</span>
          <span className="total-subtitle">Includes component inspection, protective sheath & courier shipping</span>
        </div>
        <div className="total-amount">₹{totalCharge.toFixed(2)}</div>
      </div>

      <div className="action-row">
        <button type="button" className="btn btn-secondary" onClick={onReset}>
          <RefreshCw size={16} /> Edit Request
        </button>
        <button type="button" className="btn btn-success btn-lg" onClick={onProceedToPayment}>
          <CreditCard size={18} /> Proceed to Stripe Payment Checkout <ArrowRight size={18} />
        </button>
      </div>
    </div>
  );
};
