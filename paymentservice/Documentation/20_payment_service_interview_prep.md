# Comprehensive Interview Preparation Guide — Payment Microservice (`PaymentService`)

This document contains **20+ technical interview questions and deep-dive answers** covering Payment Gateway integration (Stripe / Razorpay), PCI-DSS security compliance, digital receipt generation, and Spring Boot 3.4+.

---

### Q1: What is the primary role of the `PaymentService` microservice?
**Answer:**  
`PaymentService` is the financial transaction engine. It provides dual-mode processing:
1. **Inter-Service Feign Endpoint (`GET /card/{cardNumber}/{charge}`)**: Validates and deducts balances from the local ledger for microservices like `ComponentProcessing`.
2. **Payment Gateway Layer (`POST /api/v1/payments/stripe-charge`)**: Integrates with Stripe Java SDK to process credit card charges, evaluate issuer authorization rules, enforce PCI-DSS masking, and generate digital payment receipts.

---

### Q2: Why is storing raw credit card details in a local database considered a major anti-pattern in production systems?
**Answer:**  
1. **PCI-DSS Compliance Laws**: Storing unencrypted 16-digit credit card numbers violates Payment Card Industry Data Security Standards (PCI-DSS), exposing companies to severe legal fines and security liabilities.
2. **Production Reality**: Real systems never store card limits. They pass tokenized card references (`tok_1N82...`) to payment gateways like Stripe or Razorpay, which handle vaulting and issuer authorizations securely.

---

### Q3: How did we enforce PCI-DSS compliance in our receipt audit logs?
**Answer:**  
Inside `CardService`, card numbers are masked prior to database persistence using trailing 4-digit masking:
```java
String maskedCard = "**** **** **** " + cardStr.substring(cardStr.length() - 4);
```
Only `cardNumberMasked` is stored in the `payment_receipt` table, ensuring raw 16-digit primary card numbers are never exposed in log files or database tables.

---

### Q4: How does our Stripe Gateway simulator evaluate test card authorization rules?
**Answer:**  
In accordance with standard Stripe Test Mode specifications:
- **Standard Test Cards (`4242 4242 4242 4242` or default numbers)**: Evaluated as `SUCCESS`, generating a transaction ID (`ch_stripe_...`) and receipt number (`REC-2026-XXXXX`).
- **Decline Test Cards (Numbers ending in `0002`)**: Evaluated as `DECLINED` by issuer, returning a failed status payload.

---

### Q5: What information is included in an enterprise digital payment receipt payload (`PaymentReceiptDTO`)?
**Answer:**  
A production payment receipt payload includes:
1. `transactionId`: Unique gateway identifier (e.g. `ch_stripe_7ca25232cb22...`).
2. `receiptNumber`: Human-readable invoice/receipt identifier (`REC-2026-49772`).
3. `cardNumberMasked`: Masked card number (`**** **** **** 6789`).
4. `amountPaid` & `currency`: Transaction amount and ISO currency code (`700.0 INR`).
5. `paymentStatus`: Status code (`SUCCESS`, `DECLINED`, `FAILED`).
6. `paymentProvider`: Gateway provider (`STRIPE_GATEWAY`).
7. `timestamp`: ISO-8601 creation timestamp.
