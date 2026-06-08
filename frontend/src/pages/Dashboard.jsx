import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../store/authStore';
import { healthAPI, insuranceAPI, analysisAPI } from '../api/services';

/**
 * MediCatch 대시보드 — 디자인 handoff에 맞춰 재작성.
 * 시각적 출력물을 픽셀 단위로 재현하는 것이 목표.
 */

// ── SVG 아이콘 헬퍼 ──────────────────────────────
const Icon = ({ children, size = 13 }) => (
  <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.6"
    strokeLinecap="round" strokeLinejoin="round"
    style={{ width: size, height: size, flexShrink: 0 }}>
    {children}
  </svg>
);

const P = {
  arrow:  (<path d="M3 8h10M9 4l4 4-4 4" />),
  check:  (<path d="m3 8 4 4 6-7" />),
  plus:   (<path d="M8 3v10M3 8h10" />),
  search: (<><circle cx="7" cy="7" r="4" /><path d="m10 10 3 3" /></>),
  clip:   (<><rect x="3" y="2" width="10" height="12" rx="1.5" /><path d="M6 2v2h4V2" /><path d="M5.5 8h5M5.5 10.5h3" /></>),
  chart:  (<path d="M2 14h12M4 14V9M7 14V6M10 14V8M13 14V4" />),
  chat:   (<><path d="M2 2h12v9H9l-3 3v-3H2V2z" /><path d="M5 6h6M5 8.5h4" /></>),
  shield: (<path d="M8 1 3 3.5v4C3 10 5.5 12.5 8 14c2.5-1.5 5-4 5-6.5v-4L8 1z" />),
  x:      (<path d="M4 4l8 8M12 4l-8 8" />),
  mapPin: (<><path d="M8 14s5-4.2 5-8a5 5 0 0 0-10 0c0 3.8 5 8 5 8z"/><circle cx="8" cy="6" r="1.6"/></>),
  phone:  (<><path d="M5 2h6v12H5z"/><path d="M7 12h2"/></>),
};

const QUICK_ACTS = [
  { icon: 'search', title: '진료 전 보장 확인',   sub: '병원 가기 전에',  path: '/pre-treatment' },
  { icon: 'clip',   title: '최근 진료 기록',      sub: '방문 내역 확인', path: '/medical-records' },
  { icon: 'chart',  title: '12개월 건강 리포트',  sub: '최신 분석',      path: '/health-report' },
  { icon: 'chat',   title: 'AI 건강 상담',        sub: '지금 채팅',      path: '/chat' },
];

const SCREENING_HOSPITALS = [
    { name: '서울성모병원', phone: '1588-1511', address: '서울 서초구 반포대로 222', province: '서울특별시', city: '서초구' },
    { name: '세브란스병원', phone: '1599-1004', address: '서울 서대문구 연세로 50-1', province: '서울특별시', city: '서대문구' },
    { name: '서울대학교병원', phone: '1588-5700', address: '서울 종로구 대학로 101', province: '서울특별시', city: '종로구' },
    { name: '삼성서울병원', phone: '1599-3114', address: '서울 강남구 일원로 81', province: '서울특별시', city: '강남구' },
    { name: '서울아산병원', phone: '1688-7575', address: '서울 송파구 올림픽로43길 88', province: '서울특별시', city: '송파구' },
    { name: '국민건강보험 일산병원', phone: '1577-0013', address: '경기 고양시 일산동구 일산로 100', province: '경기도', city: '고양시' },
    { name: '분당서울대학교병원', phone: '1588-3369', address: '경기 성남시 분당구 구미로173번길 82', province: '경기도', city: '성남시' },
    { name: '아주대학교병원', phone: '1688-6114', address: '경기 수원시 영통구 월드컵로 164', province: '경기도', city: '수원시' },
    { name: '한림대학교성심병원', phone: '031-380-1500', address: '경기 안양시 동안구 관평로170번길 22', province: '경기도', city: '안양시' },
    { name: '인하대병원', phone: '032-890-2114', address: '인천 중구 인항로 27', province: '인천광역시', city: '중구' },
    { name: '가천대 길병원', phone: '1577-2299', address: '인천 남동구 남동대로774번길 21', province: '인천광역시', city: '남동구' },
    { name: '강원대학교병원', phone: '033-258-2000', address: '강원 춘천시 백령로 156', province: '강원특별자치도', city: '춘천시' },
    { name: '원주세브란스기독병원', phone: '033-741-0114', address: '강원 원주시 일산로 20', province: '강원특별자치도', city: '원주시' },
    { name: '충남대학교병원', phone: '1599-7123', address: '대전 중구 문화로 282', province: '대전광역시', city: '중구' },
    { name: '건양대학교병원', phone: '1577-3330', address: '대전 서구 관저동로 158', province: '대전광역시', city: '서구' },
    { name: '충북대학교병원', phone: '043-269-6114', address: '충북 청주시 서원구 1순환로 776', province: '충청북도', city: '청주시' },
    { name: '단국대학교병원', phone: '1588-0063', address: '충남 천안시 동남구 망향로 201', province: '충청남도', city: '천안시' },
    { name: '세종충남대학교병원', phone: '1800-3114', address: '세종특별자치시 보듬7로 20', province: '세종특별자치시', city: '세종시' },
    { name: '전북대학교병원', phone: '1577-7877', address: '전북 전주시 덕진구 건지로 20', province: '전라북도', city: '전주시' },
    { name: '원광대학교병원', phone: '1577-3773', address: '전북 익산시 무왕로 895', province: '전라북도', city: '익산시' },
    { name: '전남대학교병원', phone: '1899-0000', address: '광주 동구 제봉로 42', province: '광주광역시', city: '광주광역시' },
    { name: '조선대학교병원', phone: '062-220-3321', address: '광주 동구 필문대로 365', province: '광주광역시', city: '광주광역시' },
    { name: '화순전남대학교병원', phone: '1899-0000', address: '전남 화순군 화순읍 서양로 322', province: '전라남도', city: '화순군' },
    { name: '부산대학교병원', phone: '051-240-7000', address: '부산 서구 구덕로 179', province: '부산광역시', city: '부산광역시' },
    { name: '동아대학교병원', phone: '051-240-2000', address: '부산 서구 대신공원로 26', province: '부산광역시', city: '부산광역시' },
    { name: '인제대학교 부산백병원', phone: '051-890-6114', address: '부산 부산진구 복지로 75', province: '부산광역시', city: '부산광역시' },
    { name: '경북대학교병원', phone: '1666-5114', address: '대구 중구 동덕로 130', province: '대구광역시', city: '대구광역시' },
    { name: '영남대학교병원', phone: '1522-3114', address: '대구 남구 현충로 170', province: '대구광역시', city: '대구광역시' },
    { name: '울산대학교병원', phone: '052-250-7000', address: '울산 동구 대학병원로 25', province: '울산광역시', city: '울산광역시' },
    { name: '경상국립대학교병원', phone: '055-750-8000', address: '경남 진주시 강남로 79', province: '경상남도', city: '진주시' },
    { name: '창원경상국립대학교병원', phone: '055-214-1000', address: '경남 창원시 성산구 삼정자로 11', province: '경상남도', city: '창원시' },
    { name: '양산부산대학교병원', phone: '1577-7512', address: '경남 양산시 물금읍 금오로 20', province: '경상남도', city: '양산시' },
    { name: '제주대학교병원', phone: '064-717-1114', address: '제주 제주시 아란13길 15', province: '제주특별자치도', city: '제주시' },
    { name: '제주한라병원', phone: '064-740-5000', address: '제주 제주시 도령로 65', province: '제주특별자치도', city: '제주시' },
];


const REGION_GROUPS = [
  { province: '서울특별시', short: '서울', cities: ['전체', '서초구', '서대문구', '종로구', '강남구', '송파구'] },
  { province: '경기도', short: '경기', cities: ['전체', '고양시', '성남시', '수원시', '안양시'] },
  { province: '인천광역시', short: '인천', cities: ['전체', '중구', '남동구'] },
  { province: '강원특별자치도', short: '강원', cities: ['전체', '춘천시', '원주시'] },
  { province: '충청도', short: '충청', cities: ['전체', '대전광역시', '청주시', '천안시', '세종시'] },
  { province: '전라도', short: '전라', cities: ['전체', '광주광역시', '전주시', '익산시', '화순군'] },
  { province: '경상북도', short: '경북', cities: ['전체', '대구광역시'] },
  { province: '경상남도', short: '경남', cities: ['전체', '부산광역시', '울산광역시', '창원시', '진주시', '양산시'] },
  { province: '제주특별자치도', short: '제주', cities: ['전체', '제주시'] },
];

const provinceMatches = (hospitalProvince, selectedProvince) => {
  if (!selectedProvince) return true;
  if (selectedProvince === '충청도') return ['대전광역시', '충청남도', '충청북도', '세종특별자치시'].includes(hospitalProvince);
  if (selectedProvince === '전라도') return ['광주광역시', '전라남도', '전라북도'].includes(hospitalProvince);
  if (selectedProvince === '경상북도') return ['경상북도', '대구광역시'].includes(hospitalProvince);
  if (selectedProvince === '경상남도') return ['경상남도', '부산광역시', '울산광역시'].includes(hospitalProvince);
  return hospitalProvince === selectedProvince;
};

const RISK_GRADE = { LOW: { label: '낮음', cls: 'lo' }, MEDIUM: { label: '주의', cls: 'mid' }, HIGH: { label: '위험', cls: 'hi' } };
const formatKRW = (n) => new Intl.NumberFormat('ko-KR').format(n || 0) + '원';

export default function Dashboard() {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const [showReservationModal, setShowReservationModal] = useState(false);
  const [selectedProvince, setSelectedProvince] = useState('서울특별시');
  const [selectedCity, setSelectedCity] = useState('전체');

  const [visits, setVisits] = useState([]);
  const [risks, setRisks] = useState([]);
  const [gaps, setGaps] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAll = async () => {
      setLoading(true);
      try {
        const [records, predictions, coverageGaps, insuranceSummary] = await Promise.allSettled([
          healthAPI.getMedicalRecords({ size: 3 }),
          healthAPI.getDiseasePredictions(),
          analysisAPI.getCoverageGap(),
          insuranceAPI.getSummary(),
        ]);
        if (records.status === 'fulfilled' && Array.isArray(records.value)) setVisits(records.value.slice(0, 3));
        if (predictions.status === 'fulfilled' && Array.isArray(predictions.value)) setRisks(predictions.value);
        if (coverageGaps.status === 'fulfilled' && Array.isArray(coverageGaps.value)) setGaps(coverageGaps.value.slice(0, 2));
        if (insuranceSummary.status === 'fulfilled') setSummary(insuranceSummary.value);
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, []);

  const selectedRegion = REGION_GROUPS.find((r) => r.province === selectedProvince) || REGION_GROUPS[0];
  const reservationHospitals = SCREENING_HOSPITALS.filter((h) => (
    provinceMatches(h.province, selectedProvince) && (selectedCity === '전체' || h.city === selectedCity || h.province === selectedCity)
  ));

  const totalPremium = summary?.totalMonthlyPremium ?? summary?.monthlyPremium ?? null;
  const stats = [
    { lbl: '월 보험료 합계', val: totalPremium != null ? formatKRW(totalPremium) : '-', meta: '보험사 통합', blue: false },
    { lbl: '최근 진료 기록', val: visits.length ? `${visits.length}건` : '-', meta: '최근 기록 기준', blue: true },
    { lbl: '건강 위험도', val: risks.length ? (risks.some(r => r.riskGrade === 'HIGH') ? '고위험' : risks.some(r => r.riskGrade === 'MEDIUM') ? '중위험' : '양호') : '-', meta: '질병 예측 기반', blue: false },
    { lbl: '보험 공백', val: gaps.length ? `${gaps.length}개 항목` : '없음', meta: gaps.length ? '즉시 개선 권장' : '모두 양호', blue: false },
  ];

  return (
    <div className="mc-page fade-in">
      {/* Header */}
      <div className="mc-page-top">
        <div>
          <div className="mc-greeting-name">안녕하세요, {user?.name || '김사용'} 님</div>
          <div className="mc-greeting-sub">오늘도 건강한 하루 되세요. 내 건강 기록과 보험 현황을 한눈에 확인해보세요.</div>
        </div>
        <div className="mc-page-top-right">
          <button className="mc-btn mc-btn-primary" onClick={() => navigate('/insurance')}>
            <Icon size={12}>{P.shield}</Icon> 내 보험 현황 보기
          </button>
        </div>
      </div>

      {/* Stats strip */}
      <div className="mc-stats-strip">
        {stats.map((s, i) => (
          <div className="mc-stat-cell" key={i}>
            <div className="mc-stat-lbl">{s.lbl}</div>
            <div className={`mc-stat-val${s.blue ? ' blue' : ''}`}>{s.val}</div>
            {s.pill
              ? <span className="mc-stat-pill">{s.pill}</span>
              : <div className="mc-stat-meta">{s.meta}</div>}
          </div>
        ))}
      </div>

      {/* Quick actions + Insurance gap + Risk */}
      <div className="mc-two-col mc-dashboard-top-tools">
        <div className="mc-dashboard-feature-pair">
          {/* Quick actions */}
          <div className="mc-dashboard-feature-col">
            <div className="mc-sec-head">
              <span className="mc-sec-title">빠른 기능</span>
            </div>
            <div className="mc-action-grid">
              {QUICK_ACTS.map((a, i) => (
                <button className="mc-action-cell" key={i} onClick={() => navigate(a.path)}>
                  <div className="mc-action-icon"><Icon size={13}>{P[a.icon]}</Icon></div>
                  <div className="mc-action-title">{a.title}</div>
                  <div className="mc-action-sub">{a.sub}</div>
                </button>
              ))}
            </div>
          </div>

          {/* Insurance gap */}
          <div className="mc-dashboard-feature-col">
            <div className="mc-sec-head">
              <span className="mc-sec-title">보험 공백</span>
              <button className="mc-sec-link" onClick={() => navigate('/insurance-plan')}>
                개선하기 <Icon>{P.arrow}</Icon>
              </button>
            </div>
            <div className="mc-gap-list">
              {gaps.length > 0 ? gaps.map((g, i) => (
                <div className="mc-gap-row" key={i}>
                  <div className="mc-gap-accent" style={{ background: '#BBA8A8' }} />
                  <div className="mc-gap-info">
                    <div className="mc-gap-name">{g.coverageName || g.gapName || g.name}</div>
                    <div className="mc-gap-sub">{g.description || g.desc || ''}</div>
                  </div>
                  <span className="mc-gap-tag" style={{ color: '#7A5050', background: '#F2ECEC' }}>{g.priority || g.level || '확인'}</span>
                </div>
              )) : (
                <div style={{ padding: '16px', color: 'var(--text-3)', fontSize: 13 }}>보험 공백이 없어요 👍</div>
              )}
              <div className="mc-gap-footer">
                <button
                  className="mc-btn mc-btn-primary"
                  style={{ width: '100%', justifyContent: 'center', fontSize: 13 }}
                  onClick={() => navigate('/insurance-plan')}
                >
                  <Icon size={12}>{P.plus}</Icon> 보험 공백 확인
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Risk */}
        <div>
          <div className="mc-sec-head">
            <span className="mc-sec-title">건강 위험도</span>
            <button className="mc-sec-link" onClick={() => navigate('/health-report')}>
              리포트 <Icon>{P.arrow}</Icon>
            </button>
          </div>
          <div className="mc-risk-list">
            {risks.length > 0 ? risks.map((r, i) => {
              const grade = RISK_GRADE[r.riskGrade] || { label: r.riskGrade, cls: 'lo' };
              const pct = Math.min((r.myRisk || r.avgProbability || 0), 100);
              return (
                <div className="mc-risk-row" key={i}>
                  <div className="mc-risk-meta">
                    <span className="mc-risk-name">{r.type || r.diseaseName}</span>
                    <span className={`mc-risk-lvl ${grade.cls}`}>{grade.label}</span>
                  </div>
                  <div className="mc-risk-bar">
                    <div className={`mc-risk-fill ${grade.cls}`} style={{ width: `${pct}%` }} />
                  </div>
                </div>
              );
            }) : (
              <div style={{ padding: '16px', color: 'var(--text-3)', fontSize: 13 }}>
                {loading ? '불러오는 중...' : '질병 위험도 데이터가 없어요.'}
              </div>
            )}
          </div>
          <div className="mc-ai-strip" onClick={() => navigate('/chat')}>
            <div>
              <strong>AI 인사이트</strong>
              <span>건강 이력 기반 맞춤 보험·보건 어드바이스</span>
            </div>
            <em>→</em>
          </div>
        </div>
      </div>

      {/* Medical records + Upcoming widgets */}
      <div className="mc-two-col">
        {/* 최근 진료 기록 */}
        <div>
          <div className="mc-sec-head">
            <span className="mc-sec-title">최근 진료 기록</span>
            <button className="mc-sec-link" onClick={() => navigate('/medical-records')}>
              전체 보기 <Icon>{P.arrow}</Icon>
            </button>
          </div>
          <table className="mc-tbl">
            <thead>
              <tr>
                <th>병원 / 내역</th>
                <th>날짜</th>
                <th>구분</th>
              </tr>
            </thead>
            <tbody>
              {visits.length > 0 ? visits.map((c, i) => (
                <tr key={i} onClick={() => navigate('/medical-records')}>
                  <td>
                    <div className="mc-tbl-hospital">{c.hospitalName || c.hospital}</div>
                    <div className="mc-tbl-detail">{c.visitDate || c.detail}</div>
                  </td>
                  <td><span className="mc-tbl-date">{c.visitDate || c.date}</span></td>
                  <td><span className="mc-tbl-tag">{c.treatmentType || c.type}</span></td>
                </tr>
              )) : (
                <tr><td colSpan={3} style={{ textAlign: 'center', color: 'var(--text-3)', padding: 16 }}>
                  {loading ? '불러오는 중...' : '진료 기록이 없어요.'}
                </td></tr>
              )}
            </tbody>
          </table>
          <div className="mc-tbl-footer">
            <span className="mc-tbl-footer-label">최근 방문 기록</span>
            <span className="mc-tbl-footer-value">총 {visits.length}건</span>
          </div>
        </div>

        {/* Upcoming widgets */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <div className="mc-sec-head">
            <span className="mc-sec-title">다가오는 검진</span>
          </div>
          <div className="mc-widget">
            <div className="mc-widget-title">국가건강검진</div>
            <div className="mc-widget-sub">2024년 대상자 · 예약 필요</div>
            <button
              className="mc-btn"
              style={{ width: '100%', justifyContent: 'center', fontSize: 12.5 }}
              onClick={() => setShowReservationModal(true)}
            >
              예약하기
            </button>
          </div>
          <div className="mc-widget mc-widget-tight">
            <div className="mc-widget-section-lbl">최근 진료 요약</div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 5 }}>
              <span style={{ color: 'var(--text-2)' }}>최근 방문</span>
              <span style={{ fontWeight: 700, color: 'var(--blue)' }}>{visits.length}건</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13 }}>
              <span style={{ color: 'var(--text-2)' }}>주요 진료과</span>
              <span style={{ fontWeight: 700, color: 'var(--text-1)' }}>내과</span>
            </div>
          </div>
        </div>
      </div>

      {showReservationModal && (
        <div className="mc-modal-backdrop" onClick={() => setShowReservationModal(false)}>
          <div className="mc-modal mc-reservation-modal" onClick={(e) => e.stopPropagation()}>
            <div className="mc-modal-head">
              <div>
                <div className="mc-modal-title">국가건강검진 예약 정보</div>
                <div className="mc-reservation-sub">전국 국가건강검진 가능 병원을 지도와 목록으로 확인하세요.</div>
              </div>
              <button className="mc-modal-close" type="button" onClick={() => setShowReservationModal(false)} aria-label="닫기">
                <Icon size={15}>{P.x}</Icon>
              </button>
            </div>
            <div className="mc-modal-body mc-reservation-body mc-reservation-region-body">
              <div className="mc-reservation-map mc-korea-region-map">
                <div className="mc-reservation-map-title">전국 지역 선택</div>
                <div className="mc-korea-map-shape" aria-label="남한 지역 선택 지도">
                  <svg className="mc-korea-silhouette" viewBox="0 0 220 330" role="img" aria-label="한반도 중 남한 강조 지도">
                    <defs>
                      <linearGradient id="koreaSouthGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0" stopColor="#dbeafe" />
                        <stop offset="1" stopColor="#bbf7d0" />
                      </linearGradient>
                      <filter id="koreaSoftShadow" x="-20%" y="-20%" width="140%" height="140%">
                        <feDropShadow dx="0" dy="10" stdDeviation="10" floodColor="#2563eb" floodOpacity="0.12" />
                      </filter>
                    </defs>
                    <path className="mc-korea-north" d="M92 2 C67 18 55 42 58 68 C61 93 47 111 40 132 C34 151 44 169 60 176 C79 184 96 173 105 154 C112 137 128 127 141 112 C156 94 158 70 146 49 C135 29 116 10 92 2Z" />
                    <path className="mc-korea-south" filter="url(#koreaSoftShadow)" d="M105 131 C87 137 73 149 65 166 C56 187 64 207 77 222 C86 232 85 248 75 263 C65 278 70 297 86 309 C102 322 126 318 137 300 C148 282 157 267 178 259 C199 251 210 229 202 208 C195 188 176 181 163 166 C151 151 133 124 105 131Z" />
                    <path className="mc-korea-jeju" d="M83 305 C96 297 119 298 130 307 C119 319 96 320 83 305Z" />
                    <path className="mc-korea-sea-line" d="M132 93 C122 121 118 148 124 173 C130 199 124 223 111 247" />
                  </svg>
                  {REGION_GROUPS.map((region, i) => (
                    <button
                      key={region.province}
                      type="button"
                      className={`mc-region-chip mc-region-${i} ${selectedProvince === region.province ? 'active' : ''}`}
                      onClick={() => { setSelectedProvince(region.province); setSelectedCity('전체'); }}
                    >
                      {region.short}
                    </button>
                  ))}
                </div>
                <div className="mc-region-helper">먼저 시/도를 선택한 뒤 세부 지역을 고르면 오른쪽 병원 목록이 바뀝니다.</div>
              </div>

              <div className="mc-reservation-region-panel">
                <div className="mc-region-panel-head">
                  <div>
                    <div className="mc-region-panel-kicker">선택 지역</div>
                    <div className="mc-region-panel-title">{selectedRegion.province}</div>
                  </div>
                  <span>{reservationHospitals.length}곳</span>
                </div>
                <div className="mc-city-chip-row">
                  {selectedRegion.cities.map((city) => (
                    <button
                      key={city}
                      type="button"
                      className={selectedCity === city ? 'active' : ''}
                      onClick={() => setSelectedCity(city)}
                    >
                      {city}
                    </button>
                  ))}
                </div>
                <div className="mc-reservation-list">
                  {reservationHospitals.length > 0 ? reservationHospitals.map((h, i) => (
                    <div className="mc-reservation-hospital" key={h.name}>
                      <div className="mc-reservation-num">{i + 1}</div>
                      <div className="mc-reservation-info">
                        <div className="mc-reservation-name">{h.name}</div>
                        <div className="mc-reservation-meta"><Icon size={11}>{P.mapPin}</Icon>{h.address}</div>
                        <div className="mc-reservation-meta"><Icon size={11}>{P.phone}</Icon>{h.phone}</div>
                      </div>
                      <span className="mc-tag mc-tag-success">검진 가능</span>
                    </div>
                  )) : (
                    <div className="mc-reservation-empty">해당 지역의 샘플 병원 데이터가 아직 없어요.</div>
                  )}
                </div>
              </div>
            </div>
            <div className="mc-modal-foot">
              <button className="mc-btn" type="button" onClick={() => setShowReservationModal(false)}>닫기</button>
              <button className="mc-btn mc-btn-primary" type="button" onClick={() => navigate('/checkup')}>검진 기록 보기</button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
