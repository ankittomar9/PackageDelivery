# Low-Level Design (LLD) — React Web Portal (`webportal`)

## 1. React Component Tree & State Hierarchy

```mermaid
classDiagram
    class App {
        +state step: int
        +state loading: boolean
        +state error: string
        +state requestData: object
        +state responseData: object
        +state receiptData: object
        +state orderHistory: Array
        +handleSubmitReturn(formData) void
        +handlePaymentSuccess(receipt) void
        +resetWorkflow() void
    }

    class AuthProvider {
        +state token: string
        +state user: string
        +state isAuthenticated: boolean
        +login(username, password) Promise
        +logout() void
    }

    class Navbar {
        +props activeStep: int
    }

    class LoginModal {
        +handleSubmit() void
        +autofillDemo() void
    }

    class ReturnOrderForm {
        +state formData: object
        +handleSubmit() void
        +handleTypeChange() void
    }

    class CalculationReview {
        +props responseData: object
        +onProceedToPayment() void
        +onReset() void
    }

    class PaymentModal {
        +state cardForm: object
        +handleAutofill(type) void
        +handleSubmit() void
    }

    class DigitalReceiptModal {
        +props receiptData: object
        +handlePrint() void
    }

    class OrderHistoryTable {
        +props orders: Array
    }

    App --> AuthProvider
    App --> Navbar
    App --> LoginModal
    App --> ReturnOrderForm
    App --> CalculationReview
    App --> PaymentModal
    App --> DigitalReceiptModal
    App --> OrderHistoryTable
```

---

## 2. Stepper Progression State Machine Specifications

```mermaid
stateDiagram-v2
    [*] --> Step1_FormInput: User Authenticated
    Step1_FormInput --> Step2_CalculationReview: Submit Request (POST :8081/service)
    Step2_CalculationReview --> Step1_FormInput: Edit Request
    Step2_CalculationReview --> Step3_StripePayment: Click "Proceed to Payment"
    Step3_StripePayment --> Step2_CalculationReview: Click "Back"
    Step3_StripePayment --> Step4_DigitalReceipt: Payment Approved (POST :8083/stripe-charge)
    Step4_DigitalReceipt --> Step1_FormInput: Click "Return to Dashboard"
```

---

## 3. Directory Layout

```text
webportal/
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── main.jsx
    ├── App.jsx
    ├── index.css
    ├── context/
    │   └── AuthContext.jsx
    ├── services/
    │   └── api.js
    └── components/
        ├── Navbar.jsx
        ├── LoginModal.jsx
        ├── ReturnOrderForm.jsx
        ├── CalculationReview.jsx
        ├── PaymentModal.jsx
        ├── DigitalReceiptModal.jsx
        └── OrderHistoryTable.jsx
```

---

## 4. Axios API Interceptor Specification (`api.js`)

```javascript
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token');
  if (token) {
    config.headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
  }
  return config;
});
```
