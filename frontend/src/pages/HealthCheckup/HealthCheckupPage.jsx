import { useState } from 'react'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, Legend,
} from 'recharts'
import { checkupApi } from '../../services/api'
import styles from './HealthCheckupPage.module.css'

const SIMPLE_AUTH_OPTIONS = [
  { value: '1', label: '카카오' },
  { value: '3', label: '삼성패스' },
  { value: '5', label: 'PASS' },
  { value: '6', label: '네이버' },
  { value: '8', label: 'toss' },
]

const METRIC_KEYS = [
  { key: 'resHeight',        label: '키 (cm)' },
  { key: 'resWeight',        label: '체중 (kg)' },
  { key: 'resBmi',           label: 'BMI' },
  { key: 'resBloodPressure', label: '혈압 (수축기)' },
  { key: 'resHemoglobin',    label: '헤모글로빈 (g/dL)' },
  { key: 'resFasting',       label: '공복혈당 (mg/dL)' },
  { key: 'resTotalCholesterol', label: '총콜레스테롤 (mg/dL)' },
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
  const [selectedMetric, setSelectedMetric] = useState('resWeight')

  const handleChange = (e) =>
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const handleRequest = async (e) => {
    e.preventDefault()
    setStep('pending')
    setError(null)
    try {
      const res = await checkupApi.request(form)
      const payload = res.data ?? res
      const code = payload?.result?.code
      if (code === 'CF-03002') {
        setTwoWayMeta(payload.data)
        setStep('twoWay')
      } else {
        setResult(payload)
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
      const res = await checkupApi.certify({ ...twoWayMeta, original: form })
      setResult(res.data ?? res)
      setStep('done')
    } catch (err) {
      setError(err.response?.data?.message || '인증 실패')
      setStep('twoWay')
    }
  }

  const checkupList = result?.resCheckupList ?? result?.checkupList ?? []

  const chartData = checkupList
    .filter((c) => c[selectedMetric] != null)
    .map((c) => ({
      year: c.resCheckupYear || c.checkupYear || '-',
      value: parseFloat(c[selectedMetric]) || 0,
    }))
    .sort((a, b) => String(a.year).localeCompare(String(b.year)))

  const metricLabel = METRIC_KEYS.find((m) => m.key === selectedMetric)?.label || selectedMetric

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
          {error && <p className={styles.error}>{error}</p>}
          <button className={styles.btn} onClick={handleCertify}>인증 완료</button>
          <button className={styles.secondaryBtn} onClick={() => setStep('idle')}>처음으로</button>
        </div>
      )}

      {step === 'done' && (
        <div className={styles.result}>
          <div className={styles.resultHeader}>
            <h2>검진 결과 ({checkupList.length}건)</h2>
            <button className={styles.secondaryBtn} onClick={() => { setStep('idle'); setResult(null) }}>
              다시 조회
            </button>
          </div>

          {checkupList.length === 0 ? (
            <p className={styles.noData}>조회된 건강검진 결과가 없습니다.</p>
          ) : (
            <>
              <div className={styles.chartSection}>
                <div className={styles.chartHeader}>
                  <h3 className={styles.chartTitle}>연도별 추이</h3>
                  <select
                    className={styles.metricSelect}
                    value={selectedMetric}
                    onChange={(e) => setSelectedMetric(e.target.value)}
                  >
                    {METRIC_KEYS.map(({ key, label }) => (
                      <option key={key} value={key}>{label}</option>
                    ))}
                  </select>
                </div>
                {chartData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={240}>
                    <LineChart data={chartData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="year" />
                      <YAxis />
                      <Tooltip formatter={(v) => [v, metricLabel]} />
                      <Legend />
                      <Line
                        type="monotone"
                        dataKey="value"
                        stroke="#1a73e8"
                        strokeWidth={2}
                        dot={{ r: 4 }}
                        name={metricLabel}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                ) : (
                  <p className={styles.noData}>선택한 항목의 데이터가 없습니다.</p>
                )}
              </div>

              <div className={styles.tableWrap}>
                <table className={styles.table}>
                  <thead>
                    <tr>
                      <th>검진 연도</th><th>기관명</th><th>키 (cm)</th>
                      <th>체중 (kg)</th><th>BMI</th><th>혈압</th><th>공복혈당</th>
                    </tr>
                  </thead>
                  <tbody>
                    {checkupList.map((c, i) => (
                      <tr key={i}>
                        <td>{c.resCheckupYear || c.checkupYear || '-'}</td>
                        <td>{c.resOrganNm || c.organNm || '-'}</td>
                        <td>{c.resHeight || '-'}</td>
                        <td>{c.resWeight || '-'}</td>
                        <td>{c.resBmi || '-'}</td>
                        <td>{c.resBloodPressure || '-'}</td>
                        <td>{c.resFasting || '-'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      )}
    </div>
  )
}

export default HealthCheckupPage
