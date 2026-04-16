import { useState } from 'react'
import { medicalApi } from '../../services/api'
import styles from './MedicalRecordsPage.module.css'

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

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))
  }

  const handleRequest = async (e) => {
    e.preventDefault()
    setStep('pending')
    setError(null)
    try {
      const res = await medicalApi.request(form)
      const code = res?.data?.result?.code
      if (code === 'CF-03002') {
        // 2-Way 인증 필요
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
      const res = await medicalApi.certify({ ...twoWayMeta, ...form })
      setResult(res.data)
      setStep('done')
    } catch (err) {
      setError(err.response?.data?.message || '인증 실패')
      setStep('twoWay')
    }
  }

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
                <option value="0">SKT</option>
                <option value="1">KT</option>
                <option value="2">LGU+</option>
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
          <button className={styles.btn} onClick={handleCertify}>
            인증 완료
          </button>
        </div>
      )}

      {step === 'done' && result && (
        <div className={styles.result}>
          <h2>조회 결과</h2>
          <pre>{JSON.stringify(result, null, 2)}</pre>
        </div>
      )}
    </div>
  )
}

export default MedicalRecordsPage
