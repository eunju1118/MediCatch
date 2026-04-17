import { useState } from 'react'
import { medicalApi } from '../../services/api'
import styles from './MedicalRecordsPage.module.css'

const TELECOM_OPTIONS = [
  { value: '0', label: 'SKT' },
  { value: '1', label: 'KT' },
  { value: '2', label: 'LGU+' },
]

function MedicalRecordsPage() {
  const [form, setForm] = useState({
    userName: '', identity: '', phoneNo: '',
    telecom: '0', loginType: '2', loginTypeLevel: '1',
    authMethod: '0', startDate: '', endDate: '', type: '0',
  })
  const [step, setStep] = useState('idle')   // idle | pending | twoWay | done
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
      const res = await medicalApi.request(form)
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
      const res = await medicalApi.certify({ ...twoWayMeta, original: form })
      setResult(res.data ?? res)
      setStep('done')
    } catch (err) {
      setError(err.response?.data?.message || '인증 실패')
      setStep('twoWay')
    }
  }

  const treats = result?.resBasicTreatList ?? []

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>진료 기록 조회</h1>

      {step !== 'done' && (
        <form className={styles.form} onSubmit={handleRequest}>
          <div className={styles.grid}>
            <label>이름
              <input name="userName" value={form.userName} onChange={handleChange} required />
            </label>
            <label>주민번호
              <input name="identity" value={form.identity} onChange={handleChange}
                type="password" placeholder="주민번호 13자리" required />
            </label>
            <label>휴대폰 번호
              <input name="phoneNo" value={form.phoneNo} onChange={handleChange}
                placeholder="010XXXXXXXX" required />
            </label>
            <label>통신사
              <select name="telecom" value={form.telecom} onChange={handleChange}>
                {TELECOM_OPTIONS.map(({ value, label }) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </select>
            </label>
            <label>조회 시작일
              <input name="startDate" value={form.startDate} onChange={handleChange}
                placeholder="20240101" required />
            </label>
            <label>조회 종료일
              <input name="endDate" value={form.endDate} onChange={handleChange}
                placeholder="20241231" required />
            </label>
          </div>
          {error && <p className={styles.error}>{error}</p>}
          <button type="submit" className={styles.btn} disabled={step === 'pending'}>
            {step === 'pending' ? '조회 중...' : '조회 요청'}
          </button>
        </form>
      )}

      {step === 'twoWay' && (
        <div className={styles.twoWay}>
          <p>카카오톡 / PASS / SMS 인증을 완료한 후 아래 버튼을 눌러주세요.</p>
          {error && <p className={styles.error}>{error}</p>}
          <button className={styles.btn} onClick={handleCertify}>인증 완료</button>
          <button className={styles.secondaryBtn} onClick={() => setStep('idle')}>처음으로</button>
        </div>
      )}

      {step === 'done' && (
        <div className={styles.result}>
          <div className={styles.resultHeader}>
            <h2>조회 결과 ({treats.length}건)</h2>
            <button className={styles.secondaryBtn} onClick={() => { setStep('idle'); setResult(null) }}>
              다시 조회
            </button>
          </div>

          {treats.length === 0 ? (
            <p className={styles.noData}>조회된 진료 기록이 없습니다.</p>
          ) : (
            <div className={styles.tableWrap}>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>진료일</th><th>의료기관</th><th>진료과</th>
                    <th>질병명</th><th>총 진료비</th><th>본인부담금</th>
                  </tr>
                </thead>
                <tbody>
                  {treats.map((t, i) => (
                    <tr key={i}>
                      <td>{formatDate(t.reqDate)}</td>
                      <td>{t.resMedInstNm}</td>
                      <td>{t.resDeptCdNm}</td>
                      <td>{t.resDissCdNm || t.resDissCd || '-'}</td>
                      <td className={styles.amount}>{Number(t.resTotalCost || 0).toLocaleString()}원</td>
                      <td className={styles.amount}>{Number(t.resPatPayment || 0).toLocaleString()}원</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

function formatDate(dateStr) {
  if (!dateStr || dateStr.length < 8) return dateStr || '-'
  return `${dateStr.slice(0, 4)}-${dateStr.slice(4, 6)}-${dateStr.slice(6, 8)}`
}

export default MedicalRecordsPage
