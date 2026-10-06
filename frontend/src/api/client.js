import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080'
})

api.interceptors.request.use(cfg => {
  const t = localStorage.getItem('nexo_token')
  if (t && t !== 'undefined' && t !== 'null') {
    cfg.headers.Authorization = `Bearer ${t}`
  }
  return cfg
})

api.interceptors.response.use(
  r => r,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('nexo_token')
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  }
)

export default api
