# Comprehensive Testing Guide — Component Processing Microservice (`ComponentProcessing`)

This guide provides testing patterns, Mockito mocking practices, and MockMvc controller test configurations for **`ComponentProcessing`**.

---

## 1. Testing Strategy Overview

```
                                  / \
                                 /   \
                                /  E2E \  <-- HTTP .http files / Postman
                               /-------\
                              / Controller\ <-- MockMvc (@WebMvcTest)
                             /-------------\
                            / Service Unit  \ <-- JUnit 5 + Mockito (@ExtendWith)
                           -------------------
```

1. **Service Unit Tests (`IntegralPartServiceTest`, `AccessoryPartServiceTest`, `PaymentServiceTest`)**: Isolated, fast unit tests using `@ExtendWith(MockitoExtension.class)` to verify priority calculation rules, fee assignments, and `Optional` handling.
2. **Controller Slice Tests (`ComponentProcessingControllerTest`)**: Uses `@WebMvcTest` and `@AutoConfigureMockMvc(addFilters = false)` to test REST endpoints, JSON serialization, and HTTP status codes (200 OK vs 401 Unauthorized).

---

## 2. Service Unit Test Patterns (Mockito)

### Testing Priority Calculation in `IntegralPartServiceTest`
```java
@ExtendWith(MockitoExtension.class)
class IntegralPartServiceTest {

    @Mock
    private ProcessRequestRepository processRequestRepository;

    @Mock
    private ProcessResponseRepository processResponseRepository;

    @Mock
    private PackagingAndDeliveryClient packagingAndDeliveryClient;

    @InjectMocks
    private IntegralPartService integralPartService;

    @Test
    @DisplayName("Should expedite processing to 2 days and charge 700 for Priority Integral Request")
    void processDetail_PriorityRequest_Success() {
        ProcessRequest priorityRequest = ProcessRequest.builder()
                .requestId(1L)
                .userName("john_doe")
                .componentType("Integral")
                .quantityOfDefective(1)
                .isPriorityRequest(true)
                .build();

        when(processRequestRepository.findById(1L)).thenReturn(Optional.of(priorityRequest));
        when(packagingAndDeliveryClient.getPackagingAndDeliveryCharge("Integral", 1)).thenReturn(150.0);
        when(processResponseRepository.save(any(ProcessResponse.class))).thenAnswer(i -> i.getArgument(0));

        ProcessResponse response = integralPartService.processDetail(1L);

        assertNotNull(response);
        assertEquals("john_doe", response.getUserName());
        assertEquals(700.0, response.getProcessingCharge()); // 500 base + 200 priority
        assertEquals(LocalDate.now().plusDays(2), response.getDateOfDelivery());

        verify(processRequestRepository, times(1)).findById(1L);
        verify(processResponseRepository, times(1)).save(any());
    }
}
```

---

## 3. Controller Unit Test Patterns (MockMvc & `@WebMvcTest`)

### Why `@AutoConfigureMockMvc(addFilters = false)` is mandatory:
When slicing controller tests with `@WebMvcTest(ComponentProcessingController.class)`, Spring Boot does **not** load custom `SecurityConfig.java`. Disabling security test filters via `@AutoConfigureMockMvc(addFilters = false)` prevents Spring Security test filters from intercepting `POST /service` with 403 Forbidden errors.

```java
@WebMvcTest(ComponentProcessingController.class)
@AutoConfigureMockMvc(addFilters = false)
class ComponentProcessingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IntegralPartService integralPartService;

    @MockitoBean
    private AccessoryPartService accessoryPartService;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private ProcessRequestRepository processRequestRepository;

    @MockitoBean
    private AuthClient authClient;

    @Test
    @DisplayName("POST /service - Should return 200 OK when JWT token is valid")
    void getProcessingDetails_ValidToken_Returns200() throws Exception {
        AuthResponseDTO validAuthResponse = new AuthResponseDTO("dummy-jwt-token", true);
        when(authClient.validateToken(anyString())).thenReturn(validAuthResponse);

        ProcessRequest savedRequest = ProcessRequest.builder().requestId(1L).userName("john_doe").build();
        when(processRequestRepository.save(any())).thenReturn(savedRequest);

        ProcessResponse mockResponse = ProcessResponse.builder()
                .requestId(1L)
                .userName("john_doe")
                .processingCharge(700.0)
                .packagingAndDeliveryCharge(150.0)
                .dateOfDelivery(LocalDate.now().plusDays(2))
                .build();
        when(integralPartService.processDetail(1L)).thenReturn(mockResponse);

        mockMvc.perform(post("/service")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"john_doe\",\"componentType\":\"Integral\",\"isPriorityRequest\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(1))
                .andExpect(jsonPath("$.processingCharge").value(700.0));
    }
}
```

---

## 4. Test Execution Commands

### Run via Maven Command Line:
```bash
# Navigate to componentprocessingservice directory
cd D:\04Aug2026\PackageDelivery\componentprocessingservice

# Run all unit and slice tests
mvn clean test
```

### Run inside IntelliJ IDEA:
1. Open Project Tool Window (`Alt + 1`).
2. Navigate to `src/test/java/com/company/componentprocessingservice`.
3. Right-click folder and select **"Run 'All Tests'"**.
