import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/common/Layout'
import MedicalRecordsPage from './pages/MedicalRecords/MedicalRecordsPage'
import InsurancePage from './pages/Insurance/InsurancePage'
import HealthCheckupPage from './pages/HealthCheckup/HealthCheckupPage'
import HealthReportPage from './pages/HealthReport/HealthReportPage'
import AiChatPage from './pages/AiChat/AiChatPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Navigate to="/medical" replace />} />
          <Route path="medical" element={<MedicalRecordsPage />} />
          <Route path="insurance" element={<InsurancePage />} />
          <Route path="checkup" element={<HealthCheckupPage />} />
          <Route path="report" element={<HealthReportPage />} />
          <Route path="chat" element={<AiChatPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
