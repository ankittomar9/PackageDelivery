import axios from 'axios';

// Microservices Endpoint Configuration
export const AUTH_SERVICE_URL = 'http://localhost:8084';
export const PROCESSING_SERVICE_URL = 'http://localhost:8081';
export const PACKAGING_SERVICE_URL = 'http://localhost:8082';
export const PAYMENT_SERVICE_URL = 'http://localhost:8083';

// Axios Instance with Automatic JWT Interceptor
const api = axios.create({
  headers: {
    'Content-Type': 'application/json',
  },
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

// 1. JWT Authentication Service (Port 8084)
export const loginUser = async (username, password) => {
  const response = await axios.post(`${AUTH_SERVICE_URL}/login`, { username, password });
  return response.data;
};

export const validateToken = async (token) => {
  const response = await axios.get(`${AUTH_SERVICE_URL}/validate`, {
    headers: { Authorization: token.startsWith('Bearer ') ? token : `Bearer ${token}` }
  });
  return response.data;
};

// 2. Component Processing Service (Port 8081)
export const submitReturnRequest = async (requestData) => {
  const response = await api.post(`${PROCESSING_SERVICE_URL}/service`, requestData);
  return response.data;
};

export const processPayment = async (requestId, creditCardNumber, creditLimit, processingCharge) => {
  const response = await api.post(
    `${PROCESSING_SERVICE_URL}/payment/${requestId}/${creditCardNumber}/${creditLimit}/${processingCharge}`
  );
  return response.data;
};

// 3. Packaging & Delivery Tariff Service (Port 8082)
export const getPackagingTariff = async (componentType, count) => {
  const response = await axios.get(`${PACKAGING_SERVICE_URL}/PackagingAndDeliveryCharge/${componentType}/${count}`);
  return response.data;
};

// 4. Payment Gateway Service (Port 8083)
export const executeStripeGatewayCharge = async (cardNumber, charge, currency = 'INR') => {
  const response = await axios.post(
    `${PAYMENT_SERVICE_URL}/api/v1/payments/stripe-charge?cardNumber=${cardNumber}&charge=${charge}&currency=${currency}`
  );
  return response.data;
};

export default api;
