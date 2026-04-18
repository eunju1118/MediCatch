import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Layout from './components/common/Layout'
import LoginPage from './pages/Login/LoginPage'
import RegisterPage from './pages/Register/RegisterPage'
import DashboardPage from './pages/Dashboard/DashboardPage'
import MedicalRecordsPage from './pages/MedicalRecords/MedicalRecordsPage'
import InsurancePage from './pages/Insurance/InsurancePage'
import HealthCheckupPage from './pages/HealthCheckup/HealthCheckupPage'
import HealthReportPage from './pages/HealthReport/HealthReportPage'
import AiChatPage from './pages/AiChat/AiChatPage'
import InsuranceRecommendationPage from './pages/InsuranceRecommendation/InsuranceRecommendationPage'

function ProtectedRoute({ children }) {
  const { isLoggedIn } = useAuth()
  return isLoggedIn ? children : <Navigate to="/login" replace />
}

function AppRoutes() {
  const { isLoggedIn } = useAuth()

  return (
    <Routes>
      <Route
        path="/login"
        element={isLoggedIn ? <Navigate to="/dashboard" replace /> : <LoginPage />}
      />
      <Route
        path="/register"
        element={isLoggedIn ? <Navigate to="/dashboard" replace /> : <RegisterPage />}
      />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="medical"   element={<MedicalRecordsPage />} />
        <Route path="insurance" element={<InsurancePage />} />
        <Route path="checkup"   element={<HealthCheckupPage />} />
        <Route path="report"    element={<HealthReportPage />} />
        <Route path="chat"           element={<AiChatPage />} />
        <Route path="recommendation" element={<InsuranceRecommendationPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
