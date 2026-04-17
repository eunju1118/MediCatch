import { useState, useEffect } from 'react'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, LineChart, Line,
} from 'recharts'
import { reportApi } from '../../services/api'
import styles from './HealthReportPage.module.css'

function HealthReportPage() {
  const [report, setReport] = useState(null)
  const [months, setMonths] = useState(12)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const fetchReport = async (m = months) => {
    setLoading(true)
    setError(null)
    try {
      const res = await reportApi.getReport(m)
      setReport(res.data ?? res)
    } catch (err) {
      setError(err.response?.data?.message || '리포트 조회에 실패했습니다. 먼저 진료 기록 또는 보험 정보를 조회해주세요.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchReport() }, [])

  const monthlyData = report
    ? Object.entries(report.monthlyVisitCount || {}).map(([month, visits]) => ({ month, visits }))
    : []

  const deptData = report
    ? Object.entries(report.departmentUsage || {}).map(([dept, visits]) => ({ dept, visits }))
    : []

  return (
    <div className={styles.page}>
      <div className={styles.topRow}>
        <div>
          <h1 className={styles.title}>건강 통합 리포트</h1>
          <p className={styles.subtitle}>진료 기록과 보험 계약정보를 교차 분석한 리포트</p>
        </div>
        <div className={styles.controls}>
          <select
            className={styles.select}
            value={months}
            onChange={(e) => setMonths(Number(e.target.value))}
          >
            {[3, 6, 12, 24, 36].map((m) => (
              <option key={m} value={m}>{m}개월</option>
            ))}
          </select>
          <button className={styles.btn} onClick={() => fetchReport(months)} disabled={loading}>
            {loading ? '조회 중...' : '리포트 조회'}
          </button>
        </div>
      </div>

      {error && <p className={styles.error}>{error}</p>}

      {!report && !loading && !error && (
        <div className={styles.empty}>
          <p>리포트를 조회하려면 위의 버튼을 클릭하세요.</p>
          <p className={styles.emptyHint}>진료 기록 및 보험 정보가 먼저 조회되어 있어야 분석이 가능합니다.</p>
        </div>
      )}

      {report && (
        <>
          <div className={styles.summaryRow}>
            <div className={styles.summaryCard}>
              <p className={styles.summaryLabel}>총 방문 건수</p>
              <p className={styles.summaryValue}>{report.totalVisitCount?.toLocaleString()}회</p>
            </div>
            <div className={styles.summaryCard}>
              <p className={styles.summaryLabel}>총 진료비</p>
              <p className={styles.summaryValue}>{report.totalMedicalCost?.toLocaleString()}원</p>
            </div>
            <div className={styles.summaryCard}>
              <p className={styles.summaryLabel}>본인부담금 합계</p>
              <p className={styles.summaryValue}>{report.totalPatientCost?.toLocaleString()}원</p>
            </div>
            <div className={`${styles.summaryCard} ${report.insuranceDataAvailable ? styles.available : styles.unavailable}`}>
              <p className={styles.summaryLabel}>보험 데이터</p>
              <p className={styles.summaryValue}>{report.insuranceDataAvailable ? '연동됨' : '미연동'}</p>
            </div>
          </div>

          <div className={styles.grid}>
            <div className={styles.card}>
              <h2 className={styles.cardTitle}>월별 방문 횟수</h2>
              {monthlyData.length > 0 ? (
                <ResponsiveContainer width="100%" height={220}>
                  <BarChart data={monthlyData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="month" tick={{ fontSize: 11 }} />
                    <YAxis />
                    <Tooltip />
                    <Bar dataKey="visits" fill="#1a73e8" name="방문 횟수" />
                  </BarChart>
                </ResponsiveContainer>
              ) : <p className={styles.noData}>데이터 없음</p>}
            </div>

            <div className={styles.card}>
              <h2 className={styles.cardTitle}>진료과별 이용 현황</h2>
              {deptData.length > 0 ? (
                <ResponsiveContainer width="100%" height={220}>
                  <BarChart data={deptData} layout="vertical">
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis type="number" />
                    <YAxis dataKey="dept" type="category" width={80} tick={{ fontSize: 11 }} />
                    <Tooltip />
                    <Bar dataKey="visits" fill="#34a853" name="방문 횟수" />
                  </BarChart>
                </ResponsiveContainer>
              ) : <p className={styles.noData}>데이터 없음</p>}
            </div>

            {report.insuranceClaimable?.length > 0 && (
              <div className={`${styles.card} ${styles.wide}`}>
                <h2 className={styles.cardTitle}>보험 청구 가능 항목</h2>
                <div className={styles.tableWrap}>
                  <table className={styles.table}>
                    <thead>
                      <tr>
                        <th>진료일</th><th>의료기관</th><th>진료과</th>
                        <th>본인부담금</th><th>계약유형</th><th>청구 사유</th>
                      </tr>
                    </thead>
                    <tbody>
                      {report.insuranceClaimable.map((item, i) => (
                        <tr key={i}>
                          <td>{item.treatmentDate}</td>
                          <td>{item.hospitalName}</td>
                          <td>{item.department}</td>
                          <td className={styles.amount}>{item.patientPayment?.toLocaleString()}원</td>
                          <td><span className={styles.badge}>{item.contractType}</span></td>
                          <td>{item.claimableReason}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {report.coverageGap?.length > 0 && (
              <div className={`${styles.card} ${styles.wide}`}>
                <h2 className={styles.cardTitle}>보장 공백 분석</h2>
                <ul className={styles.gapList}>
                  {report.coverageGap.map((gap, i) => (
                    <li key={i} className={styles.gapItem}>
                      <div className={styles.gapLeft}>
                        <span className={styles.gapBadge}>{gap.gapType}</span>
                        <div>
                          <p className={styles.gapDesc}>{gap.gapDescription}</p>
                          <p className={styles.gapStats}>
                            {gap.visitCount}회 방문 · 본인부담금 {gap.totalPatientPayment?.toLocaleString()}원
                          </p>
                        </div>
                      </div>
                      <p className={styles.gapRec}>{gap.recommendation}</p>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  )
}

export default HealthReportPage
