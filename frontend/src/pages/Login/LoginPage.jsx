import {useState} from 'react'
import {Link, useNavigate} from 'react-router-dom'
import {useAuth} from '../../context/AuthContext'
import {authApi} from '../../services/api'
import styles from './LoginPage.module.css'

function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [form, setForm] = useState({ userId: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const handleChange = (e) =>
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const res = await authApi.login(form)
      const { token, userId } = res.data ?? res
      login(token, userId)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(err.response?.data?.message || '로그인에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const handleDemoLogin = () => {
    const demoUserId = form.userId || 'demo_user'
    const demoToken = `demo.${btoa(JSON.stringify({ userId: demoUserId, exp: Date.now() + 86400000 }))}.sig`
    login(demoToken, demoUserId)
    navigate('/dashboard', { replace: true })
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.logo}>
          <span className={styles.logoIcon}>+</span>
          <span className={styles.logoText}>Medicatch</span>
        </div>
        <p className={styles.tagline}>헬스케어 &amp; 보험 통합 플랫폼</p>

        <form className={styles.form} onSubmit={handleSubmit}>
          <label className={styles.label}>
            사용자 ID
            <input
              className={styles.input}
              name="userId"
              value={form.userId}
              onChange={handleChange}
              placeholder="사용자 ID를 입력하세요"
              required
            />
          </label>
          <label className={styles.label}>
            비밀번호
            <input
              className={styles.input}
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              placeholder="비밀번호를 입력하세요"
              required
            />
          </label>

          {error && <p className={styles.error}>{error}</p>}

          <button className={styles.btn} type="submit" disabled={loading}>
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>

        <div className={styles.divider}><span>또는</span></div>

        <button className={styles.demoBtn} onClick={handleDemoLogin} type="button">
          데모 로그인 (인증 서버 없이 체험)
        </button>
        <p className={styles.demoNote}>
          데모 모드에서는 실제 API 호출 시 인증 오류가 발생할 수 있습니다.
        </p>

        <p className={styles.registerLink}>
          계정이 없으신가요? <Link to="/register">회원가입</Link>
        </p>
      </div>
    </div>
  )
}

export default LoginPage
