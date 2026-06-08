import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Legend,
} from 'recharts';
import { insuranceAPI } from '../api/services';

const Ic = ({ d, size = 13 }) => (
  <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.6"
    strokeLinecap="round" strokeLinejoin="round"
    style={{ width: size, height: size, flexShrink: 0 }}>{d}</svg>
);

const P = {
  chat:    (<><path d="M2 2h12v9H9l-3 3v-3H2V2z"/><path d="M5 6h6M5 8.5h4"/></>),
  shield:  (<path d="M8 1.5l5.5 2v4.5C13.5 11.5 8 14.5 8 14.5S2.5 11.5 2.5 8V3.5L8 1.5z"/>),
};

const RISK_LABEL = { HIGH: '높음', MEDIUM: '중간', LOW: '낮음', NORMAL: '정상' };
const RISK_TAG   = { HIGH: 'mc-tag-danger', MEDIUM: 'mc-tag-warning', LOW: 'mc-tag-blue', NORMAL: 'mc-tag-success' };
const RISK_BAR   = { HIGH: 'danger', MEDIUM: 'warning', LOW: 'blue', NORMAL: 'success' };

const formatCurrency = (amount) => {
  if (amount >= 10000000) return `${(amount / 10000000).toFixed(1)}천만원`;
  if (amount >= 1000000)  return `${(amount / 1000000).toFixed(1)}백만원`;
  return new Intl.NumberFormat('ko-KR').format(amount || 0) + '원';
};

const toNumber = (value) => {
  if (value === null || value === undefined || value === '') return 0;
  const parsed = Number(String(value).replace(/[^\d.-]/g, ''));
  return Number.isFinite(parsed) ? parsed : 0;
};

// coverage-comparison API 응답(자가보장액 vs 평균그룹)을 화면용 gap 항목으로 변환
const comparisonToGap = (row) => {
  const current = toNumber(row.selfCoverageAmount ?? row.self_coverage_amount);
  const average = toNumber(row.avgGroupCoverageAmount ?? row.avg_group_coverage_amount);
  const hasAverage = average > 0;
  const gap = hasAverage ? Math.max(average - current, 0) : 0;
  let riskGrade;
  if (current <= 0) riskGrade = 'HIGH';            // 미보유
  else if (!hasAverage) riskGrade = 'NORMAL';      // 비교 불가
  else if (current >= average) riskGrade = 'NORMAL';
  else if (current >= average * 0.7) riskGrade = 'LOW';
  else riskGrade = 'MEDIUM';
  return {
    category: row.coverageName || row.coverage_name || '보장명 정보 없음',
    current,
    recommended: hasAverage ? average : current,
    gap,
    riskGrade,
  };
};

const InsurancePlan = () => {
  const navigate = useNavigate();
  const [gaps, setGaps] = useState([]);
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [priorityTip, setPriorityTip] = useState({ visible: false, x: 0, y: 0 });

  useEffect(() => {
    const fetchPlan = async () => {
      setLoading(true);
      try {
        const [comparisonRows, policyRows] = await Promise.all([
          insuranceAPI.getCoverageComparison(),
          insuranceAPI.getPolicies(),
        ]);
        const mapped = Array.isArray(comparisonRows) ? comparisonRows.map(comparisonToGap) : [];
        setGaps(mapped);
        setPolicies(Array.isArray(policyRows) ? policyRows : []);
      } catch (error) {
        console.error('Failed to fetch plan:', error);
        setGaps([]);
        setPolicies([]);
      } finally {
        setLoading(false);
      }
    };
    fetchPlan();
  }, []);

  // 보장 점수: 평균 대비 충족 비율
  const comparable = gaps.filter((g) => g.recommended > 0);
  const score = comparable.length
    ? Math.round(comparable.reduce((sum, g) => sum + Math.min((g.current / g.recommended) * 100, 100), 0) / comparable.length)
    : 0;
  const gapCount = gaps.filter((g) => g.gap > 0).length;
  const highRiskGapCount = gaps.filter((g) => g.gap > 0 && g.riskGrade === 'HIGH').length;
  const totalGapAmount = gaps.reduce((sum, g) => sum + (g.gap || 0), 0);
  const highRiskItems = gaps.filter((g) => g.gap > 0 && g.riskGrade === 'HIGH');
  // 월 보험료: 활성 보험 합계
  const currentPremium = policies
    .filter((p) => p.isActive === true || p.is_active === true || ['정상','계약부활','ACTIVE'].includes(p.status || p.contractStatus || ''))
    .reduce((sum, p) => sum + toNumber(p.monthlyPremium ?? p.monthly_premium), 0);
  const chartData = gaps.map((g) => ({
    category: g.category,
    current: Math.round((g.current || 0) / 1000000),
    recommended: Math.round((g.recommended || 0) / 1000000),
  }));
  const peerAveragePremium = null;
  const movePriorityTip = (event) => {
    setPriorityTip({ visible: true, x: event.clientX, y: event.clientY });
  };
  const showPriorityTipFromCard = (event) => {
    const rect = event.currentTarget.getBoundingClientRect();
    setPriorityTip({ visible: true, x: rect.left + rect.width - 24, y: rect.top + 18 });
  };
  const hidePriorityTip = () => setPriorityTip((tip) => ({ ...tip, visible: false }));

  return (
    <div className="mc-page fade-in">
      <div className="mc-page-top">
        <div>
          <div className="mc-page-title">보험 공백</div>
          <div className="mc-page-subtitle">내 건강 데이터와 가입 보험을 기준으로 부족한 보장 항목만 확인해요.</div>
        </div>
        <div className="mc-page-top-right">
          <button className="mc-btn" onClick={() => navigate('/chat?query=보험 보장 최적화')}>
            <Ic d={P.chat} size={12}/> AI에게 물어보기
          </button>
        </div>
      </div>

      {/* 점수 카드 + 요약 2열 */}
      <div className="mc-two-col mc-insurance-summary-row" style={{ gridTemplateColumns: '360px 1fr' }}>
        <div className="mc-card mc-card-body mc-coverage-score-card" style={{
          background: 'linear-gradient(145deg, rgba(255,255,255,.88) 0%, rgba(239,247,255,.72) 54%, rgba(220,238,255,.52) 100%)',
          color: '#17324d',
          borderColor: 'rgba(157, 190, 230, .42)',
          boxShadow: 'inset 0 1px 0 rgba(255,255,255,.9), 0 18px 38px rgba(47,111,232,.10)',
          backdropFilter: 'blur(12px)',
        }}>
          <div style={{
            display: 'inline-flex', alignItems: 'center', gap: 6,
            fontSize: 11, fontWeight: 700, letterSpacing: '0.06em',
            textTransform: 'uppercase', color: '#48739f',
          }}>
            <Ic d={P.shield} size={12}/> 보장 점수
          </div>
          <div style={{
            fontSize: 44, fontWeight: 800, letterSpacing: '-1px',
            marginTop: 8, lineHeight: 1, color: '#2F6F5C',
          }}>
            {score}<span style={{ fontSize: 22, fontWeight: 600, marginLeft: 4 }}>/ 100</span>
          </div>
          <div style={{ fontSize: 12.5, marginTop: 8, color: '#5f7891', fontWeight: 650 }}>
            현재 보장 수준 · 보장 공백 {gapCount}개 발견
          </div>
          <div className="mc-pbar" style={{ marginTop: 14, background: 'rgba(91, 147, 204, .16)' }}>
            <div className="mc-pbar-fill" style={{ width: `${score}%`, background: 'linear-gradient(90deg, rgba(47,111,232,.58), rgba(112,174,152,.72))' }}/>
          </div>
        </div>

        <div className="mc-grid-2 mc-insurance-summary-grid">
          <div className="mc-card mc-card-body">
            <div className="mc-field-label">현재 월 보험료</div>
            <div className="mc-stat-value" style={{ marginTop: 4 }}>
              {currentPremium > 0 ? formatCurrency(currentPremium) : '-'}
            </div>
            <div className="mc-stat-sub">매월 납입 중</div>
          </div>
          <div className="mc-card mc-card-body mc-card-accent-warning">
            <div className="mc-field-label">보장 공백</div>
            <div className="mc-stat-value" style={{ marginTop: 4, color: '#8A7040' }}>
              {gapCount}개
            </div>
            <div className="mc-stat-sub">부족 항목 기준</div>
          </div>
          <div
            className="mc-card mc-card-body mc-card-accent-blue mc-priority-card"
            onMouseEnter={movePriorityTip}
            onMouseMove={movePriorityTip}
            onMouseLeave={hidePriorityTip}
            onFocus={showPriorityTipFromCard}
            onBlur={hidePriorityTip}
            tabIndex={0}
          >
            <div className="mc-row-between">
              <div>
                <div className="mc-field-label">우선 확인 필요 항목</div>
                <div className="mc-stat-value mc-risk-high-text" style={{ marginTop: 4 }}>
                  고위험 {highRiskGapCount}개
                </div>
              </div>
              <span className="mc-tag mc-tag-blue">총 부족 {formatCurrency(totalGapAmount)}</span>
            </div>
          </div>
          <div className="mc-card mc-card-body mc-peer-premium-card">
            <div className="mc-row-between">
              <div>
                <div className="mc-field-label">또래 평균 보험료</div>
                <div className="mc-stat-value" style={{ marginTop: 4 }}>-</div>
                <div className="mc-stat-sub mc-peer-age-sub">데이터 준비 중</div>
              </div>
            </div>
          </div>
        </div>
      </div>


      {priorityTip.visible && (
        <div
          className="mc-priority-floating-tip"
          style={{
            left: `min(${priorityTip.x + 20}px, calc(100vw - 250px))`,
            top: `min(${priorityTip.y + 18}px, calc(100vh - 130px))`,
          }}
          role="tooltip"
        >
          {highRiskItems.map((item) => (
            <div className="mc-priority-floating-row" key={item.category}>
              <span>{item.category}</span>
              <b>부족 {formatCurrency(item.gap)}</b>
            </div>
          ))}
        </div>
      )}

      {/* 보장 범위 분석 차트 */}
      <div className="mc-sec-head" style={{ marginTop: 18 }}>
        <span className="mc-sec-title">보장 범위 분석</span>
      </div>
      <div className="mc-card mc-card-body">
        <div className="mc-chart-wrap">
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={chartData} margin={{ top: 10, right: 10, left: 0, bottom: 10 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#EBEEF4"/>
              <XAxis
                dataKey="category" interval={0}
                tick={{ fill: '#4A5568', fontSize: 11 }}
                axisLine={{ stroke: '#DDE1EA' }}
              />
              <YAxis tick={{ fill: '#9AA3B2', fontSize: 11 }} axisLine={{ stroke: '#DDE1EA' }}/>
              <Tooltip
                formatter={(v) => `${v}만원`}
                contentStyle={{
                  background: '#fff', border: '1px solid #DDE1EA', borderRadius: 6,
                  fontSize: 12, color: '#0D1520',
                }}
              />
              <Legend wrapperStyle={{ fontSize: 12, color: '#4A5568' }}/>
              <Bar dataKey="current"     fill="#7A6A8F" name="현재 보장"/>
              <Bar dataKey="recommended" fill="#E08A3E" name="권장 보장"/>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* 보장 부족 항목 */}
      <div className="mc-sec-head" style={{ marginTop: 18 }}>
        <span className="mc-sec-title">보장 공백 상세</span>
      </div>
      <div className="mc-stack-sm">
        {gaps.length === 0 && (
          <div className="mc-card mc-card-body" style={{ textAlign: 'center', color: 'var(--text-3)' }}>
            {loading ? '보장 공백 분석 중…' : '보험 보장 비교 데이터가 없어요. 보험을 연동하면 표시됩니다.'}
          </div>
        )}
        {gaps.map((gap, idx) => {
          const pct = gap.recommended
            ? Math.min((gap.current / gap.recommended) * 100, 100)
            : 100;
          return (
            <div key={idx} className="mc-card mc-card-body">
              <div className="mc-row-between" style={{ marginBottom: 10 }}>
                <div style={{ fontSize: 14, fontWeight: 700, color: 'var(--text-1)' }}>
                  {gap.category}
                </div>
                <span className={`mc-tag ${RISK_TAG[gap.riskGrade]}`}>
                  위험도 {RISK_LABEL[gap.riskGrade]}
                </span>
              </div>
              <div className="mc-pbar" style={{ height: 10 }}>
                <div
                  className={`mc-pbar-fill ${RISK_BAR[gap.riskGrade]}`}
                  style={{ width: `${pct}%` }}
                />
              </div>
              <div className="mc-row-between" style={{ marginTop: 10 }}>
                <div className="mc-card-sub">
                  현재 <strong style={{ color: 'var(--text-1)' }}>{formatCurrency(gap.current)}</strong>
                  <span style={{ margin: '0 6px', color: 'var(--text-3)' }}>→</span>
                  권장 <strong style={{ color: 'var(--blue)' }}>{formatCurrency(gap.recommended)}</strong>
                </div>
                {gap.gap > 0 ? (
                  <div style={{ fontSize: 13, fontWeight: 700, color: '#8A7040' }}>
                    부족 {formatCurrency(gap.gap)}
                  </div>
                ) : (
                  <span className="mc-tag mc-tag-success">충족</span>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {loading && (
        <div className="mc-alert mc-alert-blue" style={{ marginTop: 16 }}>
          <div>
            <div className="mc-alert-title">보장 분석 불러오는 중…</div>
            <div className="mc-alert-body">잠시만 기다려주세요.</div>
          </div>
        </div>
      )}
    </div>
  );
};

export default InsurancePlan;
