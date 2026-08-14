# Comprehensive Testing Guide — Payment Microservice (`PaymentService`)

This guide outlines unit testing strategies and MockMvc slice test configurations for **`PaymentService`**.

---

## 1. Service Unit Test Implementation (`CardServiceTest.java`)

```java
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private PaymentReceiptRepository receiptRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    @DisplayName("Should deduct charge and return remaining limit when limit is sufficient")
    void processPayment_Success() {
        CreditCard validCard = CreditCard.builder().cardNumber(4532890123456789L).cardLimit(50000.0).build();
        when(cardRepository.findByCardNumber(4532890123456789L)).thenReturn(Optional.of(validCard));

        double remainingBalance = cardService.processPayment(4532890123456789L, 700.0);

        assertEquals(49300.0, remainingBalance);
        verify(cardRepository, times(1)).save(validCard);
    }

    @Test
    @DisplayName("Should execute Stripe Gateway charge and generate digital receipt")
    void executeStripeGatewayCharge_Success() {
        when(receiptRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PaymentReceiptDTO receipt = cardService.executeStripeGatewayCharge(4532890123456789L, 700.0, "INR");

        assertNotNull(receipt);
        assertTrue(receipt.transactionId().startsWith("ch_stripe_"));
        assertEquals("SUCCESS", receipt.paymentStatus());
    }
}
```

---

## 2. Controller Slice Test Implementation (`CardControllerTest.java`)

```java
@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @Test
    @DisplayName("GET /card/{cardNumber}/{charge} - Should return remaining balance (Feign Endpoint)")
    void getBalance_Success() throws Exception {
        when(cardService.processPayment(4532890123456789L, 700.0)).thenReturn(49300.0);

        mockMvc.perform(get("/card/4532890123456789/700.0"))
                .andExpect(status().isOk())
                .andExpect(content().string("49300.0"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/stripe-charge - Should return Digital Payment Receipt")
    void executeStripeCharge_Success() throws Exception {
        PaymentReceiptDTO receipt = new PaymentReceiptDTO(
                "ch_stripe_abc123",
                "REC-2026-98123",
                "**** **** **** 6789",
                700.0,
                "INR",
                "SUCCESS",
                "STRIPE_GATEWAY",
                LocalDateTime.now(),
                "Payment Charge Authorized Successfully"
        );

        when(cardService.executeStripeGatewayCharge(4532890123456789L, 700.0, "INR")).thenReturn(receipt);

        mockMvc.perform(post("/api/v1/payments/stripe-charge")
                        .param("cardNumber", "4532890123456789")
                        .param("charge", "700.0")
                        .param("currency", "INR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("ch_stripe_abc123"))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));
    }
}
```

---

## 3. Maven Execution Commands

```bash
# Navigate to paymentservice directory
cd D:\04Aug2026\PackageDelivery\paymentservice

# Execute unit and slice tests
mvn clean test
```
