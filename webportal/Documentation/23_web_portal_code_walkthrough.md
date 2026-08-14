# Detailed Code Walkthrough — React Web Portal (`webportal`)

This document provides a line-by-line breakdown of every core React component and Axios API service in **`webportal`**.

---

## 1. `api.js` (Centralized Microservices API Layer)

```javascript
import axios from 'axios';

export const AUTH_SERVICE_URL = 'http://localhost:8084';
export const PROCESSING_SERVICE_URL = 'http://localhost:8081';
export const PACKAGING_SERVICE_URL = 'http://localhost:8082';
export const PAYMENT_SERVICE_URL = 'http://localhost:8083';

const api = axios.create({
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);
```

### 💡 Line-by-Line Explanation
- **Lines 3-6**: Defines explicit microservice port addresses matching our Spring Boot services.
- **Lines 12-20**: Implements an automatic request interceptor. Whenever any API request is sent to `ComponentProcessing` (Port 8081), Axios automatically reads `auth_token` from `localStorage` and injects `Authorization: Bearer <TOKEN>`, ensuring zero authorization header boilerplate inside UI components!

---

## 2. `PaymentModal.jsx` (Stripe Gateway Integration Component)

```javascript
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
```

### 💡 Line-by-Line Explanation
- **Success Autofill (`4532...6789`)**: Autofills standard test card numbers that execute cleanly on `PaymentService` (Port 8083).
- **Decline Autofill (`5412...0002`)**: Autofills test cards ending in `0002`, triggering the decline rule in `PaymentService` to demonstrate card decline handling on the frontend.
