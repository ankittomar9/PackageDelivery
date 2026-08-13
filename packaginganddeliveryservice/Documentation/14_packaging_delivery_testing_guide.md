# Comprehensive Testing Guide — Packaging & Delivery Microservice (`PackagingAndDelivery`)

This guide outlines unit testing strategies and MockMvc slice test configurations for **`PackagingAndDelivery`**.

---

## 1. Unit Test Implementation (`PackagingAndDeliveryServiceTest.java`)

```java
class PackagingAndDeliveryServiceTest {

    private PackagingAndDeliveryService service;

    @BeforeEach
    void setUp() {
        service = new PackagingAndDeliveryService();
    }

    @Test
    @DisplayName("Should calculate correct tariff for Integral Component (Count 1)")
    void getPackingAndDeliveryCharge_Integral_Success() {
        double result = service.getPackingAndDeliveryCharge("Integral", 1);
        assertEquals(350.0, result);
    }

    @Test
    @DisplayName("Should calculate correct tariff for Accessory Component (Count 2)")
    void getPackingAndDeliveryCharge_Accessory_Success() {
        double result = service.getPackingAndDeliveryCharge("Accessory", 2);
        assertEquals(400.0, result);
    }

    @Test
    @DisplayName("Should throw ComponentTypeNotFoundException for unknown component type")
    void getPackingAndDeliveryCharge_UnknownType_ThrowsException() {
        assertThrows(ComponentTypeNotFoundException.class, 
                () -> service.getPackingAndDeliveryCharge("UnknownType", 1));
    }
}
```

---

## 2. Controller Slice Test Implementation (`PackagingAndDeliveryControllerTest.java`)

```java
@WebMvcTest(PackagingAndDeliveryController.class)
@AutoConfigureMockMvc(addFilters = false)
class PackagingAndDeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PackagingAndDeliveryService packagingAndDeliveryService;

    @Test
    @DisplayName("GET /PackagingAndDeliveryCharge/Integral/1 - Should return 200 OK with cost")
    void getPackagingAndDeliveryCharge_Integral_Returns200() throws Exception {
        when(packagingAndDeliveryService.getPackingAndDeliveryCharge("Integral", 1)).thenReturn(350.0);

        mockMvc.perform(get("/PackagingAndDeliveryCharge/Integral/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("350.0"));
    }

    @Test
    @DisplayName("GET /PackagingAndDeliveryCharge/Invalid/1 - Should return 404 Not Found")
    void getPackagingAndDeliveryCharge_InvalidType_Returns404() throws Exception {
        when(packagingAndDeliveryService.getPackingAndDeliveryCharge("Invalid", 1))
                .thenThrow(new ComponentTypeNotFoundException("Invalid"));

        mockMvc.perform(get("/PackagingAndDeliveryCharge/Invalid/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
```

---

## 3. Maven Execution Commands

```bash
# Navigate to packaginganddeliveryservice directory
cd D:\04Aug2026\PackageDelivery\packaginganddeliveryservice

# Execute unit and slice tests
mvn clean test
```
