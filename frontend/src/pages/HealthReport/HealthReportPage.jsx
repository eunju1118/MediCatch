import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, Legend, ResponsiveContainer, LineChart, Line,
} from 'recharts'
import styles from './HealthReportPage.module.css'

// 샘플 데이터 — 실제 API 연동 시 교체
const MONTHLY_DATA = [
  { month: '1월', visits: 2, cost: 45000 },
  { month: '2월', visits: 1, cost: 15000 },
  { month: '3월', visits: 3, cost: 82000 },
  { month: '4월', visits: 1, cost: 12000 },
  { month: '5월', visits: 4, cost: 120000 },
  { month: '6월', visits: 2, cost: 38000 },
]

const DEPT_DATA = [
  { dept: '내과', visits: 7 },
  { dept: '정형외과', visits: 3 },
  { dept: '안과', visits: 2 },
  { dept: '피부과', visits: 1 },
]

function HealthReportPage() {
  return (
    <div className={styles.page}>
      <h1 className={styles.title}>건강 통합 리포트</h1>
      <p className={styles.subtitle}>최근 12개월 의료 이용 현황 및 보험 보장 분석</p>

      <div className={styles.grid}>
        <div className={styles.card}>
          <h2 className={styles.cardTitle}>월별 진료비 추이</h2>
          <ResponsiveContainer width="100%" height={250}>
            <LineChart data={MONTHLY_DATA}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip formatter={(v) => `${v.toLocaleString()}원`} />
              <Line type="monotone" dataKey="cost" stroke="#1a73e8" strokeWidth={2} name="진료비" />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className={styles.card}>
          <h2 className={styles.cardTitle}>월별 방문 횟수</h2>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={MONTHLY_DATA}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="visits" fill="#34a853" name="방문 횟수" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className={styles.card}>
          <h2 className={styles.cardTitle}>진료과별 이용 현황</h2>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={DEPT_DATA} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis type="number" />
              <YAxis dataKey="dept" type="category" width={80} />
              <Tooltip />
              <Bar dataKey="visits" fill="#1a73e8" name="방문 횟수" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className={`${styles.card} ${styles.gapCard}`}>
          <h2 className={styles.cardTitle}>보장 공백 분석</h2>
          <ul className={styles.gapList}>
            <li className={styles.gapItem}>
              <span className={styles.gapBadge}>미가입</span>
              <span>치과 치료 보장 없음</span>
            </li>
            <li className={styles.gapItem}>
              <span className={styles.gapBadge}>부족</span>
              <span>정신건강 의학과 보장 한도 초과 위험</span>
            </li>
            <li className={styles.gapItem}>
              <span className={styles.gapCovered}>보장중</span>
              <span>실손 의료비 보장 중</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  )
}

export default HealthReportPage
