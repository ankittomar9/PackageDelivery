# Comprehensive Testing Guide — React Web Portal (`webportal`)

This guide outlines component testing strategies and E2E test suite setups for **`webportal`**.

---

## 1. Component Unit Testing setup (`Vitest` & `React Testing Library`)

```javascript
import { render, screen, fireEvent } from '@testing-library/react';
import { ReturnOrderForm } from '../components/ReturnOrderForm';

describe('ReturnOrderForm Component', () => {
  test('Renders defective item category dropdown', () => {
    render(<ReturnOrderForm onSubmitRequest={jest.fn()} loading={false} />);
    
    expect(screen.getByText(/Component Category/i)).toBeInTheDocument();
    expect(screen.getByText(/Integral Component/i)).toBeInTheDocument();
  });

  test('Toggles expedited priority checkbox when Integral category selected', () => {
    render(<ReturnOrderForm onSubmitRequest={jest.fn()} loading={false} />);
    
    const priorityCheckbox = screen.getByRole('checkbox');
    expect(priorityCheckbox).toBeChecked();
    
    fireEvent.click(priorityCheckbox);
    expect(priorityCheckbox).not.toBeChecked();
  });
});
```

---

## 2. End-to-End (E2E) Flow Testing (`Cypress`)

```javascript
describe('End-to-End Return Order Flow', () => {
  it('Executes complete RMA return and payment flow', () => {
    cy.visit('http://localhost:5173');
    
    // 1. Sign In
    cy.get('input[placeholder="Enter username"]').type('admin');
    cy.get('input[placeholder="Enter password"]').type('admin');
    cy.get('button[type="submit"]').click();

    // 2. Submit Return Form
    cy.contains('Calculate Return & Charges').click();

    // 3. Review Calculation Breakdown
    cy.contains('Proceed to Stripe Payment Checkout').click();

    // 4. Execute Payment
    cy.contains('Auto-fill Success Card').click();
    cy.contains('Pay ₹1050.00 Now').click();

    // 5. Verify Receipt
    cy.contains('Payment Authorized & Confirmed!').should('be.visible');
    cy.contains('REC-2026-').should('be.visible');
  });
});
```

---

## 3. Development Server Launch Command

```bash
cd D:\04Aug2026\PackageDelivery\webportal
npm run dev
```
