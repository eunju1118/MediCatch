import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  login: (data) => api.post('/auth/login', data),
}

export const medicalApi = {
  request: (data) => api.post('/health/medical/info', data),
  certify: (data) => api.post('/health/medical/info/certify', data),
}

export const checkupApi = {
  request: (data) => api.post('/health/checkup/request', data),
  certify: (data) => api.post('/health/checkup/certify', data),
}

export const insuranceApi = {
  register: (data) => api.post('/insurance/register', data),
  registerCertify: (data) => api.post('/insurance/register/certify', data),
  getContracts: (data) => api.post('/insurance/contract', data),
  certifyContracts: (data) => api.post('/insurance/contract/certify', data),
}

export const reportApi = {
  getReport: (months = 12) => api.get('/health/report', { params: { months } }),
}

export const chatApi = {
  sendMessage: (data) => api.post('/chat/message', data),
}

/**
 * Opens an SSE stream to GET /api/chat/stream?message=...
 * Returns a cleanup function that aborts the stream.
 */
export function createChatStream(message, onChunk, onDone, onError) {
  const token = localStorage.getItem('token')
  const params = new URLSearchParams({ message })
  const controller = new AbortController()

  fetch(`/api/chat/stream?${params}`, {
    headers: {
      Authorization: token ? `Bearer ${token}` : '',
      Accept: 'text/event-stream',
    },
    signal: controller.signal,
  })
    .then((res) => {
      if (!res.ok) throw new Error(`SSE error: ${res.status}`)
      const reader = res.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      function pump() {
        reader.read().then(({ done, value }) => {
          if (done) { onDone(); return }
          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop()
          for (const line of lines) {
            if (line.startsWith('data:')) {
              const payload = line.slice(5).trim()
              if (payload === '[DONE]') { onDone(); return }
              if (payload) onChunk(payload)
            }
          }
          pump()
        }).catch((err) => {
          if (err.name !== 'AbortError') onError(err)
        })
      }
      pump()
    })
    .catch((err) => {
      if (err.name !== 'AbortError') onError(err)
    })

  return () => controller.abort()
}

export default api
