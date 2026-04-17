import { useState } from 'react'
import { recommendationApi } from '../../services/api'
import styles from './InsuranceRecommendationPage.module.css'

const GAP_TYPE_LABEL = {
  DENTAL:           '치과·구강',
  MENTAL_HEALTH:    '정신건강',
  ORIENTAL_MEDICINE:'한방',
  OPHTHALMOLOGY:    '안과',
  DERMATOLOGY:      '피부',
}

function InsuranceRecommendationPage() {
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const handleFetch = async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await recommendationApi.getRecommendation()
      setResult(res.data ?? res)
    } catch (err) {
      setError(err.response?.data?.message || '추천 조회에 실패했습니다. 먼저 진료 기록과 보험 계약 정보를 조회해주세요.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.topRow}>
        <div>
          <h1 className={styles.title}>보험 추천</h1>
          <p className={styles.subtitle}>진료 기록과 현재 보험 계약을 비교 분석한 맞춤형 보험 추천</p>
        </div>
        <button className={styles.btn} onClick={handleFetch} disabled={loading}>
          {loading ? '분석 중...' : '추천 분석 시작'}
        </button>
      </div>

      {error && <p className={styles.error}>{error}</p>}

      {!result && !loading && !error && (
        <div className={styles.empty}>
          <span className={styles.emptyIcon}>🛡️</span>
          <p>위 버튼을 눌러 맞춤 보험 추천을 받아보세요.</p>
          <p className={styles.emptyHint}>진료 기록 및 보험 계약 정보가 먼저 조회되어 있어야 정확한 분석이 가능합니다.</p>
        </div>
      )}

      {result && (
        <>
          {/* 데이터 상태 배지 */}
          <div className={styles.statusRow}>
            <span className={result.healthDataAvailable ? styles.statusOk : styles.statusWarn}>
              {result.healthDataAvailable ? '✓ 진료 기록 연동' : '✗ 진료 기록 없음'}
            </span>
            <span className={result.insuranceDataAvailable ? styles.statusOk : styles.statusWarn}>
              {result.insuranceDataAvailable ? '✓ 보험 계약 연동' : '✗ 보험 계약 없음'}
            </span>
            <span className={styles.statusInfo}>
              분석 진료 {result.analyzedTreatmentCount}건 · 가입 보험 {result.currentContractCount}건
            </span>
          </div>

          {/* AI 추천 메시지 */}
          <div className={styles.aiCard}>
            <div className={styles.aiHeader}>
              <span className={styles.aiIcon}>🤖</span>
              <span className={styles.aiLabel}>AI 맞춤 추천 메시지</span>
            </div>
            <p className={styles.aiMessage}>{result.aiMessage}</p>
          </div>

          {/* 추천 보험 유형 */}
          {result.recommendedTypes?.length > 0 && (
            <div className={styles.section}>
              <h2 className={styles.sectionTitle}>추천 보험 유형</h2>
              <div className={styles.typeList}>
                {result.recommendedTypes.map((type, i) => (
                  <span key={i} className={styles.typeBadge}>{type}</span>
                ))}
              </div>
            </div>
          )}

          {/* 보장 공백 */}
          <div className={styles.section}>
            <h2 className={styles.sectionTitle}>
              보장 공백 분석
              <span className={styles.gapCount}>{result.coverageGaps?.length ?? 0}건</span>
            </h2>

            {result.coverageGaps?.length === 0 ? (
              <div className={styles.noGap}>
                <span>✓</span>
                <p>현재 진료 기록 기준으로 보장 공백이 없습니다.</p>
              </div>
            ) : (
              <div className={styles.gapGrid}>
                {result.coverageGaps.map((gap, i) => (
                  <div key={i} className={styles.gapCard}>
                    <div className={styles.gapTop}>
                      <span className={styles.gapTypeBadge}>
                        {GAP_TYPE_LABEL[gap.gapType] ?? gap.gapType}
                      </span>
                      <span className={styles.gapDept}>{gap.department}</span>
                    </div>
                    <p className={styles.gapDesc}>{gap.description}</p>
                    <div className={styles.gapStats}>
                      <span>방문 {gap.visitCount}회</span>
                      <span>본인부담금 {gap.totalPatientPayment?.toLocaleString()}원</span>
                    </div>
                    <div className={styles.gapRec}>
                      <span className={styles.gapRecLabel}>추천</span>
                      <span>{gap.recommendedInsuranceType}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  )
}

export default InsuranceRecommendationPage
