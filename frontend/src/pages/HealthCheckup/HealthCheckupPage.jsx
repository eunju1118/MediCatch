import { useState } from 'react'
import { checkupApi } from '../../services/api'
import styles from './HealthCheckupPage.module.css'

const SIMPLE_AUTH_OPTIONS = [
  { value: '1', label: '카카오' },
  { value: '3', label: '삼성패스' },
  { value: '5', label: 'PASS' },
  { value: '6', label: '네이버' },
  { value: '8', label: 'toss' },
]

function HealthCheckupPage() {
  const [form, setForm] = useState({
    loginType: '5', loginTypeLevel: '1',
    identity: '', birthDate: '',
    inquiryType: '0',
    searchStartYear: '2023', searchEndYear: '2024',
    type: '0',
  })
  const [step, setStep] = useState('idle')
  const [twoWayMeta, setTwoWayMeta] = useState(null)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)

  const handleChange = (e) =>
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const handleRequest = async (e) => {
    e.preventDefault()
    setStep('pending')
    setError(null)
    try {
      const res = await checkupApi.request(form)
      const code = res?.data?.result?.code
      if (code === 'CF-03002') {
        setTwoWayMeta(res.data.data)
        setStep('twoWay')
      } else {
        setResult(res.data)
        setStep('done')
      }
    } catch (err) {
      setError(err.response?.data?.message || '조회 실패')
      setStep('idle')
    }
  }

  const handleCertify = async () => {
    setStep('pending')
    try {
      const res = await checkupApi.certify({ ...twoWayMeta, ...form })
      setResult(res.data)
      setStep('done')
    } catch (err) {
      setError(err.response?.data?.message || '인증 실패')
      setStep('twoWay')
    }
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>건강검진 결과 조회</h1>

      {step !== 'done' && (
        <form className={styles.form} onSubmit={handleRequest}>
          <div className={styles.grid}>
            <label>주민번호
              <input name="identity" type="password" value={form.identity}
                onChange={handleChange} placeholder="주민번호 13자리" required />
            </label>
            <label>생년월일 (yymmdd)
              <input name="birthDate" value={form.birthDate}
                onChange={handleChange} placeholder="예: 900101" required />
            </label>
            <label>간편인증 수단
              <select name="loginTypeLevel" value={form.loginTypeLevel} onChange={handleChange}>
                {SIMPLE_AUTH_OPTIONS.map(({ value, label }) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </select>
            </label>
            <label>조회 유형
              <select name="inquiryType" value={form.inquiryType} onChange={handleChange}>
                <option value="0">일반</option>
                <option value="1">상세 + PDF</option>
                <option value="4">문진 JSON</option>
              </select>
            </label>
            <label>검색 시작 연도
              <input name="searchStartYear" value={form.searchStartYear}
                onChange={handleChange} placeholder="2023" required />
            </label>
            <label>검색 종료 연도
              <input name="searchEndYear" value={form.searchEndYear}
                onChange={handleChange} placeholder="2024" required />
            </label>
          </div>
          {error && <p className={styles.error}>{error}</p>}
          <button type="submit" className={styles.btn} disabled={step === 'pending'}>
            {step === 'pending' ? '조회 중...' : '검진결과 조회'}
          </button>
        </form>
      )}

      {step === 'twoWay' && (
        <div className={styles.twoWay}>
          <p>선택한 간편인증 수단으로 인증을 완료한 후 버튼을 눌러주세요.</p>
          <button className={styles.btn} onClick={handleCertify}>인증 완료</button>
        </div>
      )}

      {step === 'done' && result && (
        <div className={styles.result}>
          <h2>검진 결과</h2>
          <pre>{JSON.stringify(result, null, 2)}</pre>
        </div>
      )}
    </div>
  )
}

export default HealthCheckupPage
