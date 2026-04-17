import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import styles from './DashboardPage.module.css'

const QUICK_LINKS = [
  { to: '/medical',   icon: '🏥', title: '진료 기록',     desc: '최근 진료 내역 및 진료비 조회' },
  { to: '/checkup',   icon: '🩺', title: '건강검진 결과', desc: '건강검진 항목별 결과 및 추이 확인' },
  { to: '/insurance', icon: '🛡️', title: '보험 조회',     desc: '내 보험 계약 목록 및 보장 내용' },
  { to: '/report',    icon: '📊', title: '통합 리포트',   desc: '진료 기록과 보험을 교차 분석한 리포트' },
  { to: '/chat',      icon: '🤖', title: 'AI 건강 채팅',  desc: 'GPT-4o 기반 개인화 건강 상담' },
]

function DashboardPage() {
  const { userId } = useAuth()
  const navigate = useNavigate()

  return (
    <div className={styles.page}>
      <div className={styles.welcome}>
        <h1 className={styles.greeting}>안녕하세요, <span className={styles.userId}>{userId}</span> 님</h1>
        <p className={styles.subtitle}>Medicatch로 건강과 보험을 한 곳에서 관리하세요.</p>
      </div>

      <div className={styles.cardGrid}>
        {QUICK_LINKS.map(({ to, icon, title, desc }) => (
          <button
            key={to}
            className={styles.card}
            onClick={() => navigate(to)}
          >
            <span className={styles.cardIcon}>{icon}</span>
            <div>
              <p className={styles.cardTitle}>{title}</p>
              <p className={styles.cardDesc}>{desc}</p>
            </div>
          </button>
        ))}
      </div>

      <div className={styles.infoBox}>
        <p className={styles.infoTitle}>이용 안내</p>
        <ul className={styles.infoList}>
          <li>진료 기록 또는 건강검진 조회 시 본인 인증(간편인증)이 필요합니다.</li>
          <li>보험 조회 시 내보험다보여 서비스를 통해 계약정보를 가져옵니다.</li>
          <li>통합 리포트는 진료 기록과 보험 계약정보 조회 후 자동으로 생성됩니다.</li>
          <li>AI 건강 채팅은 조회된 나의 건강 데이터를 바탕으로 맞춤 상담을 제공합니다.</li>
        </ul>
      </div>
    </div>
  )
}

export default DashboardPage
