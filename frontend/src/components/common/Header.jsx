import styles from './Header.module.css'

function Header() {
  return (
    <header className={styles.header}>
      <div className={styles.logo}>
        <span className={styles.logoIcon}>+</span>
        <span className={styles.logoText}>Medicatch</span>
      </div>
      <p className={styles.tagline}>헬스케어 &amp; 보험 통합 플랫폼</p>
    </header>
  )
}

export default Header
