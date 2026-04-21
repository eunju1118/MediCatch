import {useNavigate} from 'react-router-dom'
import {useAuth} from '../../context/AuthContext'
import styles from './Header.module.css'

function Header() {
  const { userId, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <header className={styles.header}>
      <div className={styles.left}>
        <div className={styles.logo}>
          <span className={styles.logoIcon}>+</span>
          <span className={styles.logoText}>Medicatch</span>
        </div>
        <p className={styles.tagline}>헬스케어 &amp; 보험 통합 플랫폼</p>
      </div>

      {userId && (
        <div className={styles.right}>
          <span className={styles.userInfo}>{userId}</span>
          <button className={styles.logoutBtn} onClick={handleLogout}>
            로그아웃
          </button>
        </div>
      )}
    </header>
  )
}

export default Header
