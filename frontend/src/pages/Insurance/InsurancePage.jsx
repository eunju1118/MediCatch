import { useState } from 'react'
import { insuranceApi } from '../../services/api'
import styles from './InsurancePage.module.css'

function InsurancePage() {
  const [tab, setTab] = useState('contracts')  // contracts | register
  const [contractForm, setContractForm] = useState({
    id: '', password: '', type: '0',
    userName: '', phoneNo: '', telecom: '0', authMethod: '0',
  })
  const [step, setStep] = useState('idle')
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)

  const handleChange = (setter) => (e) =>
    setter((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const handleContractRequest = async (e) => {
    e.preventDefault()
    setStep('pending')
    setError(null)
    try {
      const res = await insuranceApi.getContracts(contractForm)
      setResult(res.data)
      setStep('done')
    } catch (err) {
      setError(err.response?.data?.message || '조회 실패')
      setStep('idle')
    }
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>보험 조회</h1>

      <div className={styles.tabs}>
        <button
          className={`${styles.tab} ${tab === 'contracts' ? styles.activeTab : ''}`}
          onClick={() => setTab('contracts')}
        >
          계약정보 조회
        </button>
        <button
          className={`${styles.tab} ${tab === 'register' ? styles.activeTab : ''}`}
          onClick={() => setTab('register')}
        >
          내보험다보여 회원가입
        </button>
      </div>

      {tab === 'contracts' && (
        <form className={styles.form} onSubmit={handleContractRequest}>
          <div className={styles.grid}>
            <label>로그인 ID
              <input name="id" value={contractForm.id}
                onChange={handleChange(setContractForm)} required />
            </label>
            <label>비밀번호
              <input name="password" type="password" value={contractForm.password}
                onChange={handleChange(setContractForm)} required />
            </label>
            <label>이름
              <input name="userName" value={contractForm.userName}
                onChange={handleChange(setContractForm)} required />
            </label>
            <label>휴대폰 번호
              <input name="phoneNo" value={contractForm.phoneNo}
                onChange={handleChange(setContractForm)} placeholder="010XXXXXXXX" required />
            </label>
            <label>보험 유형
              <select name="type" value={contractForm.type}
                onChange={handleChange(setContractForm)}>
                <option value="0">전체</option>
                <option value="1">정액형</option>
                <option value="2">실손형</option>
                <option value="6">자동차</option>
                <option value="7">저축성</option>
              </select>
            </label>
          </div>
          {error && <p className={styles.error}>{error}</p>}
          <button type="submit" className={styles.btn} disabled={step === 'pending'}>
            {step === 'pending' ? '조회 중...' : '보험 조회'}
          </button>
        </form>
      )}

      {step === 'done' && result && (
        <div className={styles.result}>
          <h2>계약 정보</h2>
          <pre>{JSON.stringify(result, null, 2)}</pre>
        </div>
      )}
    </div>
  )
}

export default InsurancePage
