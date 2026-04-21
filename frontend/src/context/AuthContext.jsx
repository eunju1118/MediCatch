import {createContext, useCallback, useContext, useState} from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'))
  const [userId, setUserId] = useState(() => localStorage.getItem('userId'))

  const login = useCallback((newToken, newUserId) => {
    localStorage.setItem('token', newToken)
    localStorage.setItem('userId', newUserId)
    setToken(newToken)
    setUserId(newUserId)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    setToken(null)
    setUserId(null)
  }, [])

  return (
    <AuthContext.Provider value={{ token, userId, login, logout, isLoggedIn: !!token }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
