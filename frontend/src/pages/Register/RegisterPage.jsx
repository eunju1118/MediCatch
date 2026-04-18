import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { authApi } from '../../services/api'
import styles from './RegisterPage.module.css'

function RegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    userId: '',
    password: '',
    confirmPassword: '',
    name: '',
    email: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(false)

  const handleChange = (e) =>
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const validate = () => {
    if (!form.userId.trim()) return '사용자 ID를 입력해주세요.'
    if (form.userId.length < 4) return '사용자 ID는 4자 이상이어야 합니다.'
    if (!form.name.trim()) return '이름을 입력해주세요.'
    if (!form.email.trim()) return '이메일을 입력해주세요.'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return '올바른 이메일 형식이 아닙니다.'
    if (form.password.length < 8) return '비밀번호는 8자 이상이어야 합니다.'
    if (form.password !== form.confirmPassword) return '비밀번호가 일치하지 않습니다.'
    return null
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const validationError = validate()
    if (validationError) { setError(validationError); return }

    setLoading(true)
    setError(null)
    try {
      await authApi.register({
        userId: form.userId,
        password: form.password,
        name: form.name,
        email: form.email,
      })
      setSuccess(true)
    } catch (err) {
      setError(err.response?.data?.message || '회원가입에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  if (success) {
    return (
      <div className={styles.page}>
        <div className={styles.card}>
          <div className={styles.successIcon}>✓</div>
          <h2 className={styles.successTitle}>회원가입 완료</h2>
          <p className={styles.successMsg}>
            <strong>{form.userId}</strong>님, 환영합니다!<br />
            로그인 페이지에서 시작하세요.
          </p>
          <button className={styles.btn} onClick={() => navigate('/login', { replace: true })}>
            로그인하러 가기
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.logo}>
          <span className={styles.logoIcon}>+</span>
          <span className={styles.logoText}>Medicatch</span>
        </div>
        <p className={styles.tagline}>회원가입</p>

        <form className={styles.form} onSubmit={handleSubmit}>
          <label className={styles.label}>
            사용자 ID
            <input
              className={styles.input}
              name="userId"
              value={form.userId}
              onChange={handleChange}
              placeholder="4자 이상의 영문/숫자"
              required
            />
          </label>
          <label className={styles.label}>
            이름
            <input
              className={styles.input}
              name="name"
              value={form.name}
              onChange={handleChange}
              placeholder="실명을 입력하세요"
              required
            />
          </label>
          <label className={styles.label}>
            이메일
            <input
              className={styles.input}
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              placeholder="example@email.com"
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
              placeholder="8자 이상"
              required
            />
          </label>
          <label className={styles.label}>
            비밀번호 확인
            <input
              className={styles.input}
              name="confirmPassword"
              type="password"
              value={form.confirmPassword}
              onChange={handleChange}
              placeholder="비밀번호를 다시 입력하세요"
              required
            />
          </label>

          {error && <p className={styles.error}>{error}</p>}

          <button className={styles.btn} type="submit" disabled={loading}>
            {loading ? '가입 중...' : '회원가입'}
          </button>
        </form>

        <p className={styles.loginLink}>
          이미 계정이 있으신가요? <Link to="/login">로그인</Link>
        </p>
      </div>
    </div>
  )
}

export default RegisterPage
