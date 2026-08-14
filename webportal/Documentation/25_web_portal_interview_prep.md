# Comprehensive Interview Preparation Guide — React Web Portal (`webportal`)

This document contains **20+ technical interview questions and deep-dive answers** covering React 18, Vite, Axios interceptors, JWT security, and Single-Page Application (SPA) architecture.

---

### Q1: Why did we replace legacy Spring MVC JavaServer Pages (JSP) with a modern React Single-Page Application (SPA)?
**Answer:**  
1. **Server-Side Overhead**: JSP relies on server-side rendering (SSR), requiring Spring Boot to compile HTML on every HTTP request. This increases server load and latency.
2. **User Experience & Responsiveness**: A React SPA loads the application shell once and executes dynamic UI updates asynchronously in the browser using virtual DOM reconciliation, delivering instantaneous UI state transitions without page reloads.
3. **Decoupled Architecture**: Scrapping JSP cleanly separates frontend UI logic from backend Java microservices, enabling independent deployments and mobile application integrations.

---

### Q2: How do Axios Request Interceptors simplify JWT token authorization across microservice calls?
**Answer:**  
In `api.js`, an Axios request interceptor intercepts every outgoing HTTP request:
```javascript
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token');
  if (token) {
    config.headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
  }
  return config;
});
```
This guarantees that any component executing microservice calls automatically attaches the `Authorization: Bearer <TOKEN>` header without repeating token retrieval logic in every component file.

---

### Q3: How is the multi-step return order workflow managed in React?
**Answer:**  
The workflow uses a step state machine (`step: 1 -> 2 -> 3 -> 4`) managed in `App.jsx`:
- **Step 1 (`ReturnOrderForm`)**: Collects component metadata and submits to `ComponentProcessing` (Port 8081).
- **Step 2 (`CalculationReview`)**: Renders processing fees, packaging tariffs, and turnaround dates.
- **Step 3 (`PaymentModal`)**: Executes Stripe Gateway charge against `PaymentService` (Port 8083).
- **Step 4 (`DigitalReceiptModal`)**: Renders official digital receipts (`REC-2026-XXXXX`) and updates the audit history table.

---

### Q4: How does the UI handle print formatting for digital receipts?
**Answer:**  
In `index.css`, `@media print` rules hide non-essential elements during printing:
```css
@media print {
  body { background: white; color: black; }
  .navbar, .stepper-bar, .no-print, .history-panel { display: none !important; }
  .modal-overlay { position: static; background: none; }
  .receipt-paper { box-shadow: none; border: 1px solid #ccc; }
}
```
When the user clicks "Print Receipt", `window.print()` outputs only the physical paper receipt invoice without navigation bars or background overlays.

---

### Q5: How do Vite and Hot Module Replacement (HMR) improve developer experience compared to legacy Maven JSP builds?
**Answer:**  
Vite uses native ES modules (`esm`) in the browser. Unlike legacy Webpack or Maven WAR builds which re-bundle the entire application upon file modification, Vite updates only the modified module in memory, providing near-instantaneous (<100ms) Hot Module Replacement (HMR).
