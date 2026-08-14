import React, { useState } from 'react';
import { CreditCard, Lock, Calendar, User, ShieldAlert, CheckCircle2, AlertTriangle, ArrowLeft } from 'lucide-react';
import { executeStripeGatewayCharge } from '../services/api';

export const PaymentModal = ({ responseData, onPaymentSuccess, onCancel }) => {
  const totalAmount = responseData ? responseData.processingCharge + responseData.packagingAndDeliveryCharge : 0;

  const [cardForm, setCardForm] = useState({
    cardNumber: '4532890123456789',
    cardHolder: 'JOHN DOE',
    expiry: '12/28',
    cvv: '123',
  });

  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleAutofill = (type) => {
    if (type === 'success') {
      setCardForm({
        cardNumber: '4532890123456789',
        cardHolder: 'JOHN DOE',
        expiry: '12/28',
        cvv: '123',
      });
    } else {
      setCardForm({
        cardNumber: '5412759081230002', // Triggers decline rule in PaymentService!
        cardHolder: 'SARAH CONNOR',
        expiry: '09/27',
        cvv: '999',
      });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMessage('');

    try {
      // Execute Stripe Gateway Charge against Microservice #4 (Port 8083)
      const receipt = await executeStripeGatewayCharge(cardForm.cardNumber, totalAmount, 'INR');

      setLoading(false);
      if (receipt && receipt.paymentStatus === 'SUCCESS') {
        onPaymentSuccess(receipt);
      } else {
        setErrorMessage(receipt.message || 'Payment Declined by Issuer. Please try another card.');
      }
    } catch (err) {
      setLoading(false);
      setErrorMessage(err.response?.data?.message || 'Payment Service (Port 8083) Error. Please try again.');
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content glass-panel">
        <div className="modal-header">
          <div className="header-title-group">
            <CreditCard size={24} className="accent-icon" />
            <div>
              <h3>Stripe Payment Gateway Checkout</h3>
              <p>Secure PCI-DSS 256-bit encrypted checkout for Order #{responseData?.requestId}</p>
            </div>
          </div>
          <button type="button" className="btn-close" onClick={onCancel}>
            &times;
          </button>
        </div>

        <div className="amount-banner">
          <span>Amount Due:</span>
          <span className="banner-price">₹{totalAmount.toFixed(2)} INR</span>
        </div>

        {errorMessage && (
          <div className="alert alert-error">
            <AlertTriangle size={18} />
            <span>{errorMessage}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="payment-form">
          <div className="form-group">
            <label>Credit Card Number</label>
            <div className="input-with-icon">
              <CreditCard size={18} className="input-icon" />
              <input
                type="text"
                maxLength="16"
                value={cardForm.cardNumber}
                onChange={(e) => setCardForm({ ...cardForm, cardNumber: e.target.value })}
                placeholder="16-digit Card Number"
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>Cardholder Name</label>
            <div className="input-with-icon">
              <User size={18} className="input-icon" />
              <input
                type="text"
                value={cardForm.cardHolder}
                onChange={(e) => setCardForm({ ...cardForm, cardHolder: e.target.value })}
                placeholder="Name on card"
                required
              />
            </div>
          </div>

          <div className="form-grid-2">
            <div className="form-group">
              <label>Expiry Date</label>
              <div className="input-with-icon">
                <Calendar size={18} className="input-icon" />
                <input
                  type="text"
                  maxLength="5"
                  value={cardForm.expiry}
                  onChange={(e) => setCardForm({ ...cardForm, expiry: e.target.value })}
                  placeholder="MM/YY"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label>CVV / CVC</label>
              <div className="input-with-icon">
                <Lock size={18} className="input-icon" />
                <input
                  type="password"
                  maxLength="4"
                  value={cardForm.cvv}
                  onChange={(e) => setCardForm({ ...cardForm, cvv: e.target.value })}
                  placeholder="•••"
                  required
                />
              </div>
            </div>
          </div>

          {/* Test Card Quick Selection Buttons */}
          <div className="test-cards-box">
            <p className="test-card-title">Stripe Gateway Test Cards:</p>
            <div className="test-buttons-row">
              <button
                type="button"
                className="btn-test btn-test-success"
                onClick={() => handleAutofill('success')}
              >
                <CheckCircle2 size={14} /> Auto-fill Success Card (Ending 6789)
              </button>
              <button
                type="button"
                className="btn-test btn-test-decline"
                onClick={() => handleAutofill('decline')}
              >
                <ShieldAlert size={14} /> Auto-fill Decline Card (Ending 0002)
              </button>
            </div>
          </div>

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onCancel}>
              <ArrowLeft size={16} /> Back
            </button>
            <button type="submit" className="btn btn-success btn-lg" disabled={loading}>
              {loading ? 'Authorizing Charge...' : `Pay ₹${totalAmount.toFixed(2)} Now`}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
