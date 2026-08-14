import React, { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { LoginModal } from './components/LoginModal';
import { ReturnOrderForm } from './components/ReturnOrderForm';
import { CalculationReview } from './components/CalculationReview';
import { PaymentModal } from './components/PaymentModal';
import { DigitalReceiptModal } from './components/DigitalReceiptModal';
import { OrderHistoryTable } from './components/OrderHistoryTable';
import { submitReturnRequest } from './services/api';
import { AlertCircle, Layers, CheckCircle2, ShieldCheck, Zap } from 'lucide-react';

const DashboardContent = () => {
  const { isAuthenticated } = useAuth();

  // Workflow State Management
  const [step, setStep] = useState(1); // 1: Form, 2: Review, 3: Payment, 4: Receipt
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [requestData, setRequestData] = useState(null);
  const [responseData, setResponseData] = useState(null);
  const [receiptData, setReceiptData] = useState(null);
  const [orderHistory, setOrderHistory] = useState([
    {
      requestId: 1,
      userName: 'john_doe',
      contactNumber: 9876543210,
      componentType: 'Integral',
      componentName: 'MacBook Pro M3 Display Assembly',
      processingCharge: 700.0,
      packagingAndDeliveryCharge: 350.0,
      dateOfDelivery: '2026-08-16',
      isPriorityRequest: true,
    },
  ]);

  if (!isAuthenticated) {
    return <LoginModal />;
  }

  // Handle Form Submission -> Interacts with ComponentProcessing (Port 8081)
  const handleSubmitReturn = async (formData) => {
    setLoading(true);
    setError('');
    try {
      const response = await submitReturnRequest(formData);
      setRequestData(formData);
      setResponseData(response);
      setStep(2);
      setLoading(false);
    } catch (err) {
      setLoading(false);
      setError(
        err.response?.data?.message ||
          'Failed to reach ComponentProcessing (Port 8081). Ensure microservice is running.'
      );
    }
  };

  const handlePaymentSuccess = (receipt) => {
    setReceiptData(receipt);
    setStep(4);

    // Append to order history table
    if (responseData && requestData) {
      const newOrder = {
        ...responseData,
        userName: requestData.userName,
        contactNumber: requestData.contactNumber,
        componentType: requestData.componentType,
        componentName: requestData.componentName,
        isPriorityRequest: requestData.isPriorityRequest,
      };
      setOrderHistory((prev) => [newOrder, ...prev]);
    }
  };

  const resetWorkflow = () => {
    setStep(1);
    setRequestData(null);
    setResponseData(null);
    setReceiptData(null);
  };

  return (
    <div className="main-content-container">
      {/* Workflow Stepper Header */}
      <div className="stepper-bar glass-panel">
        <div className={`step-item ${step >= 1 ? 'active' : ''}`}>
          <div className="step-number">1</div>
          <span>Defective Component</span>
        </div>
        <div className="step-connector"></div>
        <div className={`step-item ${step >= 2 ? 'active' : ''}`}>
          <div className="step-number">2</div>
          <span>Tariff Calculation</span>
        </div>
        <div className="step-connector"></div>
        <div className={`step-item ${step >= 3 ? 'active' : ''}`}>
          <div className="step-number">3</div>
          <span>Stripe Payment</span>
        </div>
        <div className="step-connector"></div>
        <div className={`step-item ${step >= 4 ? 'active' : ''}`}>
          <div className="step-number">4</div>
          <span>Digital Receipt</span>
        </div>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      {/* Step 1: Return Request Input Form */}
      {step === 1 && (
        <ReturnOrderForm onSubmitRequest={handleSubmitReturn} loading={loading} />
      )}

      {/* Step 2: Calculation Breakdown Review */}
      {step === 2 && (
        <CalculationReview
          responseData={responseData}
          onProceedToPayment={() => setStep(3)}
          onReset={resetWorkflow}
        />
      )}

      {/* Step 3: Stripe Payment Modal */}
      {step === 3 && (
        <PaymentModal
          responseData={responseData}
          onPaymentSuccess={handlePaymentSuccess}
          onCancel={() => setStep(2)}
        />
      )}

      {/* Step 4: Digital Receipt Modal */}
      {step === 4 && (
        <DigitalReceiptModal receiptData={receiptData} onFinish={resetWorkflow} />
      )}

      {/* Audit History Table */}
      <OrderHistoryTable orders={orderHistory} />
    </div>
  );
};

export default function App() {
  return (
    <AuthProvider>
      <div className="app-shell">
        <Navbar />
        <DashboardContent />
      </div>
    </AuthProvider>
  );
}
