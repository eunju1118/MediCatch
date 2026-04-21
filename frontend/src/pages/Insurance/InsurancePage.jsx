import {useState} from 'react'
import {insuranceApi} from '../../services/api'
import styles from './InsurancePage.module.css'

const TYPE_OPTIONS = [
  { value: '0', label: '전체' },
  { value: '1', label: '정액형' },
  { value: '2', label: '실손형' },
  { value: '6', label: '자동차' },
  { value: '7', label: '저축성' },
]

function InsurancePage() {
  const [tab, setTab] = useState('contracts')
  const [contractForm, setContractForm] = useState({
    id: '', password: '', type: '0',
    userName: '', phoneNo: '', telecom: '0', authMethod: '0', identity: ''
  })
  const [step, setStep] = useState('idle')
  const [twoWayMeta, setTwoWayMeta] = useState(null)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)

  const handleChange = (e) =>
      setContractForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const handleContractRequest = async (e) => {
    e.preventDefault();
    setStep('pending');
    setError(null);
    try {
      const res = await insuranceApi.getContracts(contractForm);
      const payload = res.data?.data || res.data;

      // 만약 데이터가 성공(CF-00000)이라면
      if (payload.resultCode === 'CF-00000' || payload.success) {
        // payload 안에 리스트가 직접 있거나, contractInfo 안에 있을 수 있음
        const info = payload.contractInfo || payload;
        setResult(info);
        setStep('done');
      } else if (payload.twoWayRequired) {
        setTwoWayMeta(payload.twoWayContext);
        setStep('twoWay');
      } else {
        setError(payload.resultMessage || "조회 실패");
        setStep('idle');
      }
    } catch (err) {
      setError('통신 오류가 발생했습니다.');
      setStep('idle');
    }
  };

  const handleCertify = async () => {
    setStep('pending');
    try {
      const res = await insuranceApi.certifyContracts({ ...twoWayMeta, original: contractForm });
      const payload = res.data?.data;
      setResult(payload?.contractInfo);
      setStep('done');
    } catch (err) {
      setError(err.response?.data?.message || '인증 실패');
      setStep('twoWay');
    }
  };

  // 3. result가 contractInfo이므로 리스트에 바로 접근
  const flatList = result?.resFlatRateContractList ?? [];
  const actualList = result?.resActualLossContractList ?? [];

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

        {tab === 'register' && (
            <div className={styles.infoBox}>
              <p>내보험다보여(생명·손해보험협회) 서비스 회원가입이 필요합니다.</p>
              <p>먼저 회원가입 후 계약정보 조회를 이용해주세요.</p>
            </div>
        )}

        {tab === 'contracts' && step === 'idle' && (
            <form className={styles.form} onSubmit={handleContractRequest}>
              <div className={styles.grid}>
                <label>로그인 ID
                  <input name="id" value={contractForm.id} onChange={handleChange} required />
                </label>
                <label>비밀번호
                  <input name="password" type="password" value={contractForm.password}
                         onChange={handleChange} required />
                </label>
                <label>이름
                  <input name="userName" value={contractForm.userName} onChange={handleChange} required />
                </label>
                <label>휴대폰 번호
                  <input name="phoneNo" value={contractForm.phoneNo} onChange={handleChange}
                         placeholder="010XXXXXXXX" required />
                </label>
                <label>주민번호
                  <input name="identity" value={contractForm.identity} onChange={handleChange}
                         type="password" placeholder="주민번호 13자리" required />
                </label>
                <label>보험 유형
                  <select name="type" value={contractForm.type} onChange={handleChange}>
                    {TYPE_OPTIONS.map(({ value, label }) => (
                        <option key={value} value={value}>{label}</option>
                    ))}
                  </select>
                </label>
              </div>
              {error && <p className={styles.error}>{error}</p>}
              <button type="submit" className={styles.btn}>보험 조회</button>
            </form>
        )}

        {tab === 'contracts' && step === 'pending' && (
            <div className={styles.loading}>
              <p>데이터를 불러오고 있습니다. 잠시만 기다려주세요...</p>
            </div>
        )}

        {tab === 'contracts' && step === 'twoWay' && (
            <div className={styles.twoWay}>
              <p className={styles.infoText}>카카오톡 / PASS / SMS 인증을 완료한 후 아래 버튼을 눌러주세요.</p>
              {error && <p className={styles.error}>{error}</p>}
              <button className={styles.btn} onClick={handleCertify}>인증 완료</button>
              <button className={styles.secondaryBtn} onClick={() => setStep('idle')}>처음으로</button>
            </div>
        )}

        {tab === 'contracts' && step === 'done' && (
            <div className={styles.result}>
              <div className={styles.resultHeader}>
                <h2>계약 정보</h2>
                <button className={styles.secondaryBtn} onClick={() => { setStep('idle'); setResult(null) }}>
                  다시 조회
                </button>
              </div>

              {flatList.length > 0 && (
                  <>
                    <h3 className={styles.sectionTitle}>정액보험 ({flatList.length}건)</h3>
                    <ContractTable contracts={flatList} />
                  </>
              )}

              {actualList.length > 0 && (
                  <>
                    <h3 className={styles.sectionTitle}>실손보험 ({actualList.length}건)</h3>
                    <ContractTable contracts={actualList} />
                  </>
              )}

              {flatList.length === 0 && actualList.length === 0 && (
                  <p className={styles.noData}>조회된 보험 계약이 없습니다.</p>
              )}
            </div>
        )}
      </div>
  )
}

/** * 날짜 포맷팅 함수 (YYYYMMDD -> YYYY-MM-DD)
 */
const formatDate = (dateStr) => {
  if (!dateStr || dateStr.length !== 8) return dateStr || '-';
  return `${dateStr.substring(0, 4)}-${dateStr.substring(4, 6)}-${dateStr.substring(6, 8)}`;
}

function ContractTable({ contracts }) {
  return (
      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
          <tr>
            <th>상품명</th><th>보험사</th><th>계약일</th><th>만기일</th><th>보험료</th><th>상태</th>
          </tr>
          </thead>
          <tbody>
          {contracts.map((c, i) => {
            // 실손형은 날짜가 resCoverageLists[0] 안에 있을 수 있으므로 방어 코드 작성
            const startDate = c.commStartDate || c.resCoverageLists?.[0]?.commStartDate;
            const endDate = c.commEndDate || c.resCoverageLists?.[0]?.commEndDate;

            return (
                <tr key={i}>
                  {/* 1. resProductNm 대신 resInsuranceName 사용 */}
                  <td>{c.resInsuranceName || '-'}</td>
                  <td>{c.resCompanyNm || '-'}</td>
                  {/* 2. 날짜 필드 수정 및 포맷팅 적용 */}
                  <td>{formatDate(startDate)}</td>
                  <td>{formatDate(endDate)}</td>
                  {/* 3. resMonthlyPremium 대신 resPremium 사용 */}
                  <td className={styles.amount}>
                    {c.resPremium ? `${Number(c.resPremium).toLocaleString()}원` : '-'}
                  </td>
                  <td>
                  <span className={c.resContractStatus?.includes('정') ? styles.statusOk : styles.statusOther}>
                    {c.resContractStatus || '-'}
                  </span>
                  </td>
                </tr>
            );
          })}
          </tbody>
        </table>
      </div>
  )
}

export default InsurancePage