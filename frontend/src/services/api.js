import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

// JWT 토큰 자동 첨부
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 공통 에러 처리
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// --- Health Service ---
export const medicalApi = {
  request: (data) => api.post('/health/medical/request', data),
  certify: (twoWayData) => api.post('/health/medical/certify', twoWayData),
}

export const checkupApi = {
  request: (data) => api.post('/health/checkup/request', data),
  certify: (twoWayData) => api.post('/health/checkup/certify', twoWayData),
}

// --- Insurance Service ---
export const insuranceApi = {
  register: (data) => api.post('/insurance/insurance/register', data),
  getContracts: (data) => api.post('/insurance/insurance/contracts', data),
  certifyContracts: (data, params) =>
    api.post('/insurance/insurance/contracts/certify', data, { params }),
}

// --- Chat Service ---
export const chatApi = {
  sendMessage: (data) => api.post('/chat/chat/message', data),
}

export default api
