import { NavLink } from 'react-router-dom'
import styles from './Sidebar.module.css'

const NAV_ITEMS = [
  { to: '/dashboard',      label: '대시보드',      icon: '🏠' },
  { to: '/medical',        label: '진료 기록',     icon: '🏥' },
  { to: '/insurance',      label: '보험 조회',     icon: '🛡️' },
  { to: '/checkup',        label: '건강검진 결과', icon: '🩺' },
  { to: '/report',         label: '통합 리포트',   icon: '📊' },
  { to: '/recommendation', label: '보험 추천',     icon: '💡' },
  { to: '/chat',           label: 'AI 건강 채팅',  icon: '🤖' },
]

function Sidebar() {
  return (
    <nav className={styles.sidebar}>
      <ul className={styles.navList}>
        {NAV_ITEMS.map(({ to, label, icon }) => (
          <li key={to}>
            <NavLink
              to={to}
              className={({ isActive }) =>
                `${styles.navItem} ${isActive ? styles.active : ''}`
              }
            >
              <span className={styles.icon}>{icon}</span>
              <span className={styles.label}>{label}</span>
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  )
}

export default Sidebar
