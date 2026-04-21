import {useState} from 'react'
import {recommendationApi} from '../../services/api'
import styles from './InsuranceRecommendationPage.module.css'

// 보장 공백 유형 한글 매핑 (백엔드 GAP_TYPE 상수와 일치)
const GAP_TYPE_LABEL = {
  DENTAL:           '치과·구강',
  MENTAL_HEALTH:    '정신건강',
  ORIENTAL_MEDICINE: '한방',
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
      // API 응답 구조에 따라 데이터 추출 (res.data 또는 res)
      const data = res.data ?? res
      setResult(data)
    } catch (err) {
      console.error('Recommendation Error:', err)
      setError(
          err.response?.data?.message ||
          '추천 분석에 실패했습니다. 진료 기록과 보험 정보를 먼저 조회했는지 확인해주세요.'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
      <div className={styles.page}>
        <div className={styles.topRow}>
          <div>
            <h1 className={styles.title}>AI 보험 추천</h1>
            <p className={styles.subtitle}>진료 기록과 보험 계약을 정밀 분석하여 보장 공백을 찾아드립니다.</p>
          </div>
          <button
              className={styles.btn}
              onClick={handleFetch}
              disabled={loading}
          >
            {loading ? '데이터 분석 중...' : '맞춤 추천 분석 시작'}
          </button>
        </div>

        {error && (
            <div className={styles.errorBox}>
              <p className={styles.error}>{error}</p>
            </div>
        )}

        {!result && !loading && !error && (
            <div className={styles.empty}>
              <div className={styles.emptyIcon}>🛡️</div>
              <p className={styles.emptyText}>데이터 기반의 맞춤 분석을 시작해보세요.</p>
              <p className={styles.emptyHint}>
                ※ 정확한 분석을 위해 <b>[병원/약국 진료 기록]</b>과 <b>[내 보험 조회]</b>를 <br />
                최소 1회 실행한 후 분석 버튼을 눌러주세요.
              </p>
            </div>
        )}

        {loading && (
            <div className={styles.loadingArea}>
              <div className={styles.spinner}></div>
              <p>사용자의 건강 데이터를 기반으로 AI가 보장 공백을 계산하고 있습니다...</p>
            </div>
        )}

        {result && !loading && (
            <>
              {/* 데이터 연동 상태 섹션 */}
              <div className={styles.statusRow}>
                <div className={result.healthDataAvailable ? styles.statusOk : styles.statusWarn}>
                  <span className={styles.dot}></span>
                  {result.healthDataAvailable ? '진료 기록 분석 완료' : '진료 기록 연동 필요'}
                </div>
                <div className={result.insuranceDataAvailable ? styles.statusOk : styles.statusWarn}>
                  <span className={styles.dot}></span>
                  {result.insuranceDataAvailable ? '보험 계약 분석 완료' : '보험 정보 연동 필요'}
                </div>
                <div className={styles.statusCount}>
                  총 <b>{result.analyzedTreatmentCount}건</b>의 진료 / <b>{result.currentContractCount}건</b>의 보험 분석
                </div>
              </div>

              {/* AI 추천 리포트 카드 */}
              <div className={styles.aiCard}>
                <div className={styles.aiHeader}>
                  <span className={styles.aiIcon}>✨</span>
                  <span className={styles.aiLabel}>Medicatch AI 분석 리포트</span>
                </div>
                <div className={styles.aiContent}>
                  {result.aiMessage ? (
                      <p className={styles.aiMessage}>{result.aiMessage}</p>
                  ) : (
                      <p className={styles.aiMessage}>분석된 추천 메시지가 없습니다. 보장 공백을 확인해주세요.</p>
                  )}
                </div>
              </div>

              {/* 추천 보험 유형 (태그 형태) */}
              {result.recommendedTypes?.length > 0 && (
                  <div className={styles.section}>
                    <h2 className={styles.sectionTitle}>이런 보험이 필요할 것 같아요!</h2>
                    <div className={styles.typeList}>
                      {result.recommendedTypes.map((type, i) => (
                          <span key={i} className={styles.typeBadge}>{type}</span>
                      ))}
                    </div>
                  </div>
              )}

              {/* 보장 공백 리스트 */}
              <div className={styles.section}>
                <h2 className={styles.sectionTitle}>
                  보장 공백 상세 분석
                  <span className={styles.gapCount}>{result.coverageGaps?.length ?? 0}</span>
                </h2>

                {result.coverageGaps?.length === 0 ? (
                    <div className={styles.noGap}>
                      <span className={styles.checkIcon}>✅</span>
                      <p>현재 모든 진료 영역이 기존 보험으로 잘 보장되고 있습니다.</p>
                    </div>
                ) : (
                    <div className={styles.gapGrid}>
                      {result.coverageGaps.map((gap, i) => (
                          <div key={i} className={styles.gapCard}>
                            <div className={styles.gapHeader}>
                      <span className={styles.gapLabel}>
                        {GAP_TYPE_LABEL[gap.gapType] || gap.gapType}
                      </span>
                              <span className={styles.gapDeptNm}>{gap.department}</span>
                            </div>

                            <div className={styles.gapBody}>
                              <p className={styles.gapDesc}>{gap.description}</p>
                              <div className={styles.gapData}>
                                <div className={styles.dataItem}>
                                  <span className={styles.dataLabel}>방문 횟수</span>
                                  <span className={styles.dataValue}>{gap.visitCount}회</span>
                                </div>
                                <div className={styles.dataItem}>
                                  <span className={styles.dataLabel}>누적 본인부담금</span>
                                  <span className={styles.dataValue}>{gap.totalPatientPayment?.toLocaleString()}원</span>
                                </div>
                              </div>
                            </div>

                            <div className={styles.gapFooter}>
                              <div className={styles.recommendBox}>
                                <span className={styles.recTag}>AI 추천</span>
                                <span className={styles.recValue}>{gap.recommendedInsuranceType}</span>
                              </div>
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