import React, { useState, useEffect } from 'react';
import { healthAPI } from '../api/services';

const Ic = ({ d, size = 13 }) => (
  <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.6"
    strokeLinecap="round" strokeLinejoin="round"
    style={{ width: size, height: size, flexShrink: 0 }}>{d}</svg>
);

const P = {
  heart:  (<path d="M8 14s-5-3.3-5-7a3 3 0 0 1 5-2 3 3 0 0 1 5 2c0 3.7-5 7-5 7z"/>),
  warn:   (<><path d="M8 3l6 10H2z"/><path d="M8 7v3M8 12v.01"/></>),
  check:  (<path d="M3 8l3 3 7-7"/>),
  cal:    (<><rect x="2" y="3" width="12" height="11" rx="1.5"/><path d="M2 7h12M5 1v3M11 1v3"/></>),
  chart:  (<><path d="M3 13V7M8 13V3M13 13V9"/></>),
  syringe:(<><path d="M10 2l4 4M8 4l4 4-6 6H2v-4z"/></>),
  x:      (<path d="M4 4l8 8M12 4l-8 8"/>),
  lock:   (<><rect x="3" y="7" width="10" height="7" rx="1.5"/><path d="M5.5 7V5.2a2.5 2.5 0 0 1 5 0V7"/></>),
  download:(<><path d="M8 2v8"/><path d="M5 7l3 3 3-3"/><path d="M3 13h10"/></>),
};

const SIMPLE_AUTH_METHODS = [
  { value: 'kakao', label: '카카오톡', tone: '#facc15' },
  { value: 'phone', label: '휴대폰 인증', tone: '#2f6fe8' },
  { value: 'naver', label: '네이버', tone: '#22c55e' },
  { value: 'toss', label: '토스', tone: '#2563eb' },
];

const TELECOM_OPTIONS = [
  { value: '0', label: 'SKT / SKT 알뜰폰' },
  { value: '1', label: 'KT / KT 알뜰폰' },
  { value: '2', label: 'LG U+ / LG U+ 알뜰폰' },
];

const PHONE_AUTH_TYPES = [
  { value: 'sms', label: '문자 인증번호' },
  { value: 'pass', label: 'PASS' },
];




const STATUS_LABEL = { NORMAL: '정상', WARNING: '주의', DANGER: '경고' };
const STATUS_CLASS = { NORMAL: 'mc-tag-success', WARNING: 'mc-tag-warning', DANGER: 'mc-tag-danger' };
const GRADE_LABEL  = { LOW: '낮음', MEDIUM: '중간', HIGH: '높음' };
const GRADE_CLASS  = { LOW: 'mc-tag-success', MEDIUM: 'mc-tag-warning', HIGH: 'mc-tag-danger' };
const PBAR_CLASS = { LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' };

const getBodyProfile = (bmi) => {
  if (bmi < 18.5) return { key: 'slim', label: '마른 체형', shoulder: 18, chest: 15, waist: 10, hip: 14, limb: 4.2, tone: '#7fb5c8' };
  if (bmi < 25) return { key: 'normal', label: '표준 체형', shoulder: 22, chest: 18, waist: 14, hip: 18, limb: 5.2, tone: '#7aac9a' };
  if (bmi < 30) return { key: 'over', label: '과체중 경향', shoulder: 25, chest: 23, waist: 22, hip: 23, limb: 6.3, tone: '#b7a36b' };
  return { key: 'high', label: '고체중 경향', shoulder: 28, chest: 27, waist: 27, hip: 28, limb: 7.4, tone: '#b8877f' };
};

const BodyPreview = ({ bmi }) => {
  const profile = getBodyProfile(Number(bmi) || 0);
  const cx = 45;
  const torsoW = profile.key === 'slim' ? 18 : profile.key === 'normal' ? 23 : profile.key === 'over' ? 30 : 34;
  const waistW = profile.key === 'slim' ? 13 : profile.key === 'normal' ? 18 : profile.key === 'over' ? 27 : 32;
  const hipW = profile.key === 'slim' ? 19 : profile.key === 'normal' ? 24 : profile.key === 'over' ? 31 : 35;
  const limbW = profile.key === 'slim' ? 4.2 : profile.key === 'normal' ? 5 : profile.key === 'over' ? 6.4 : 7.4;
  const tooltip = `BMI ${bmi} · ${profile.label}`;

  return (
    <div className="mc-body-preview" aria-label={`BMI 기반 ${profile.label} 예상 이미지`}>
      <svg viewBox="0 0 90 160" role="img" className="mc-simple-body-svg">
        <defs>
          <linearGradient id={`simpleBodyGrad-${profile.key}`} x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stopColor="#eef4f1" />
            <stop offset="100%" stopColor={profile.tone} />
          </linearGradient>
        </defs>
        <circle cx={cx} cy="17" r="8" fill={`url(#simpleBodyGrad-${profile.key})`} />
        <path
          d={`M ${cx - torsoW} 33
             C ${cx - torsoW - 4} 48, ${cx - waistW - 3} 66, ${cx - waistW} 84
             C ${cx - hipW} 98, ${cx - hipW + 4} 109, ${cx - 13} 112
             L ${cx + 13} 112
             C ${cx + hipW - 4} 109, ${cx + hipW} 98, ${cx + waistW} 84
             C ${cx + waistW + 3} 66, ${cx + torsoW + 4} 48, ${cx + torsoW} 33
             C ${cx + 13} 28, ${cx - 13} 28, ${cx - torsoW} 33 Z`}
          fill={`url(#simpleBodyGrad-${profile.key})`}
        />
        <path d={`M ${cx - torsoW + 2} 39 C ${cx - 33} 58, ${cx - 32} 82, ${cx - 25} 103`} fill="none" stroke={profile.tone} strokeWidth={limbW} strokeLinecap="round" />
        <path d={`M ${cx + torsoW - 2} 39 C ${cx + 33} 58, ${cx + 32} 82, ${cx + 25} 103`} fill="none" stroke={profile.tone} strokeWidth={limbW} strokeLinecap="round" />
        <path d={`M ${cx - 9} 111 C ${cx - 15} 126, ${cx - 16} 142, ${cx - 17} 153`} fill="none" stroke={profile.tone} strokeWidth={limbW + 1.2} strokeLinecap="round" />
        <path d={`M ${cx + 9} 111 C ${cx + 15} 126, ${cx + 16} 142, ${cx + 17} 153`} fill="none" stroke={profile.tone} strokeWidth={limbW + 1.2} strokeLinecap="round" />
        <path d={`M ${cx} 30 L ${cx} 113`} stroke="rgba(255,255,255,.46)" strokeWidth="1.1" strokeLinecap="round" />
        <path d={`M ${cx - waistW + 3} 82 C ${cx - 8} 86, ${cx + 8} 86, ${cx + waistW - 3} 82`} stroke="rgba(255,255,255,.48)" strokeWidth="1.2" fill="none" strokeLinecap="round" />
      </svg>
      <div className="mc-body-preview-tooltip">{tooltip}</div>
    </div>
  );
};

const CheckupRecords = () => {
  const [selectedYear, setSelectedYear] = useState(null);
  const [checkups, setCheckups] = useState([]);
  const [diseases, setDiseases] = useState([]);
  const [loading, setLoading] = useState(false);
  const [vaccineAuth, setVaccineAuth] = useState(false);
  const [vaccineAuthStarted, setVaccineAuthStarted] = useState(false);
  const [vaccineAuthRequested, setVaccineAuthRequested] = useState(false);
  const [vaccineAuthMessage, setVaccineAuthMessage] = useState('');
  const [vaccineAuthForm, setVaccineAuthForm] = useState({
    method: 'kakao',
    name: localStorage.getItem('userName') || '',
    identityFront: '',
    identityBack: '',
    phoneNo: localStorage.getItem('phoneNo') || '',
    telecom: '0',
    phoneAuthType: 'sms',
    code: '',
  });

  useEffect(() => {
    const fetchCheckups = async () => {
      setLoading(true);
      try {
        const [checkupData, diseaseData] = await Promise.allSettled([
          healthAPI.getCheckupResults(),
          healthAPI.getDiseasePredictions(),
        ]);
        if (checkupData.status === 'fulfilled' && Array.isArray(checkupData.value) && checkupData.value.length) {
          setCheckups(checkupData.value);
          setSelectedYear(checkupData.value[0]?.year || null);
        }
        if (diseaseData.status === 'fulfilled' && Array.isArray(diseaseData.value)) {
          setDiseases(diseaseData.value);
        }
      } catch (error) {
        console.error('Failed to fetch checkups:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchCheckups();
  }, []);

  const currentCheckup = checkups.find((c) => c.year === selectedYear) || checkups[0];
  if (!currentCheckup) return (
    <div className="mc-page fade-in">
      <div className="mc-page-top"><div><div className="mc-page-title">건강검진 기록</div></div></div>
      <div style={{ padding: 32, textAlign: 'center', color: 'var(--text-3)' }}>
        {loading ? '불러오는 중...' : '건강검진 기록이 없어요. 데이터 연동 후 확인해주세요.'}
      </div>
    </div>
  );
  const ageDelta = currentCheckup.healthAge - currentCheckup.actualAge;
  const isYounger = ageDelta < 0;
  const cleanDigits = (value) => value.replace(/\D/g, '');
  const normalOf = (category, fallback) => currentCheckup.results?.find((r) => r.category === category)?.normal || fallback;

  const handleVaccineAuthRequest = (e) => {
    e.preventDefault();
    const identity13 = `${vaccineAuthForm.identityFront}${vaccineAuthForm.identityBack}`;
    if (!vaccineAuthForm.name.trim()) { setVaccineAuthMessage('성명을 입력해주세요.'); return; }
    if (identity13.length !== 13) { setVaccineAuthMessage('주민등록번호 13자리를 입력해주세요.'); return; }
    if (!cleanDigits(vaccineAuthForm.phoneNo)) { setVaccineAuthMessage('전화번호를 입력해주세요.'); return; }
    setVaccineAuthRequested(true);
    setVaccineAuthMessage(vaccineAuthForm.method === 'phone' && vaccineAuthForm.phoneAuthType === 'sms'
      ? '인증번호를 발송했습니다. 데모 환경에서는 000000을 입력해주세요.'
      : '간편인증 요청을 보냈습니다. 앱에서 승인 후 인증 완료를 눌러주세요.');
  };

  const handlePDFDownload = () => {
    window.print();
  };

  const handleVaccineAuthVerify = () => {
    if (vaccineAuthForm.method === 'phone' && vaccineAuthForm.phoneAuthType === 'sms' && vaccineAuthForm.code.trim() !== '000000') {
      setVaccineAuthMessage('인증 번호를 다시 확인해주세요.');
      return;
    }
    setVaccineAuth(true);
    setVaccineAuthMessage('예방접종 현황 인증이 완료되었습니다.');
  };

  return (
    <div className="mc-page fade-in">
      <div className="mc-page-top">
        <div>
          <div className="mc-page-title">건강검진 기록</div>
          <div className="mc-page-subtitle">연도별 검진 결과와 질병 위험도를 확인하세요.</div>
        </div>
        <div className="mc-page-top-right">
          <button className="mc-btn mc-btn-primary mc-report-pdf-btn mc-print-hide" onClick={handlePDFDownload}>
            <Ic d={P.download} size={12}/> PDF 다운로드
          </button>
        </div>
      </div>

      {/* 건강나이 카드 + 주요 지표 요약 */}
      <div className="mc-checkup-summary-grid">
        <div className={`mc-card mc-card-body mc-health-age-card ${isYounger ? 'mc-card-accent-success' : 'mc-card-accent-warning'}`}>
          <BodyPreview bmi={currentCheckup.bmi} />
          <div className="mc-health-age-info">
            <div className="mc-field-label">건강나이</div>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: 10, marginTop: 6 }}>
              <div style={{
                fontSize: 36, fontWeight: 800, letterSpacing: '-0.5px',
                color: isYounger ? '#3A7A62' : '#8A7040',
              }}>
                {currentCheckup.healthAge}세
              </div>
              <span className={`mc-tag ${isYounger ? 'mc-tag-success' : 'mc-tag-warning'}`}>
                {ageDelta > 0 ? `+${ageDelta}세` : `${ageDelta}세`}
              </span>
            </div>
            <div className="mc-card-sub" style={{ marginTop: 8 }}>
              실제나이 {currentCheckup.actualAge}세 · {selectedYear}년 기준
            </div>
            {currentCheckup.riskFactors?.length > 0 && (
              <div className="mc-row-wrap" style={{ marginTop: 14 }}>
                {currentCheckup.riskFactors.map((r) => (
                  <span key={r} className="mc-tag mc-tag-warning">
                    <Ic d={P.warn} size={10}/> {r}
                  </span>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="mc-grid-2">
          <div className="mc-card mc-card-body mc-metric-card">
            <div className="mc-field-label">혈압</div>
            <div className="mc-stat-value" style={{ marginTop: 4 }}>{currentCheckup.bloodPressure}</div>
            <div className="mc-stat-sub">mmHg</div>
            <div className="mc-metric-tooltip">정상 기준: {normalOf('혈압', '120/80 미만')}</div>
          </div>
          <div className="mc-card mc-card-body mc-metric-card">
            <div className="mc-field-label">혈당</div>
            <div className="mc-stat-value" style={{ marginTop: 4 }}>{currentCheckup.bloodSugar}</div>
            <div className="mc-stat-sub">mg/dL</div>
            <div className="mc-metric-tooltip">정상 기준: {normalOf('혈당', '100 미만')}</div>
          </div>
          <div className="mc-card mc-card-body mc-metric-card">
            <div className="mc-field-label">콜레스테롤</div>
            <div className="mc-stat-value" style={{ marginTop: 4 }}>{currentCheckup.cholesterol}</div>
            <div className="mc-stat-sub">mg/dL</div>
            <div className="mc-metric-tooltip">정상 기준: {normalOf('콜레스테롤', '200 미만')}</div>
          </div>
          <div className="mc-card mc-card-body mc-metric-card">
            <div className="mc-field-label">BMI</div>
            <div className="mc-stat-value" style={{ marginTop: 4 }}>{currentCheckup.bmi}</div>
            <div className="mc-stat-sub">{currentCheckup.height}cm · {currentCheckup.weight}kg</div>
            <div className="mc-metric-tooltip">정상 기준: {normalOf('BMI', '18.5~24.9')}</div>
          </div>
        </div>
      </div>

      {/* 연도 탭 */}
      <div className="mc-sec-head" style={{ marginTop: 18 }}>
        <span className="mc-sec-title">검사 연도</span>
      </div>
      <div className="mc-row-wrap">
        {checkups.map((c) => (
          <button
            key={c.year}
            className={`mc-chip ${selectedYear === c.year ? 'active' : ''}`}
            onClick={() => setSelectedYear(c.year)}
          >
            <Ic d={P.cal} size={10}/> {c.year}년
          </button>
        ))}
      </div>

      {/* 검사 결과 테이블 */}
      {currentCheckup.results.length > 0 && (
        <>
          <div className="mc-sec-head" style={{ marginTop: 18 }}>
            <span className="mc-sec-title">검사 결과</span>
          </div>
          <div className="mc-card">
            <table className="mc-tbl">
              <thead>
                <tr>
                  <th>검사항목</th>
                  <th>측정값</th>
                  <th>정상범위</th>
                  <th>상태</th>
                </tr>
              </thead>
              <tbody>
                {currentCheckup.results.map((result, idx) => (
                  <tr key={idx}>
                    <td style={{ fontWeight: 600 }}>{result.category}</td>
                    <td><strong>{result.value}</strong></td>
                    <td style={{ color: 'var(--text-2)' }}>{result.normal}</td>
                    <td>
                      <span className={`mc-tag ${STATUS_CLASS[result.status]}`}>
                        {STATUS_LABEL[result.status]}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}


      {/* 질병 위험도 */}
      <div className="mc-sec-head mc-checkup-risk-head">
        <span className="mc-sec-title">질병 위험도</span>
      </div>
      <div className="mc-risk-guide">
        <span><b>낮음</b> 20% 미만</span>
        <span><b>중간</b> 20~30%</span>
        <span><b>높음</b> 30% 이상</span>
      </div>
      <div className="mc-grid-auto-sm">
        {diseases.map((d, idx) => {
          const maxRisk = 50;
          const myRisk = d.myRisk ?? d.avgProbability ?? 0;
          return (
            <div key={idx} className="mc-card mc-card-body mc-disease-risk-card">
              <div className="mc-card-head" style={{ padding: 0, border: 'none' }}>
                <div className="mc-card-title" style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}>
                  <Ic d={P.heart} size={14}/> {d.type}
                </div>
                <span className={`mc-tag ${GRADE_CLASS[d.riskGrade]}`}>
                  {GRADE_LABEL[d.riskGrade]}
                </span>
              </div>

              <div className="mc-risk-main-value">
                <span>나의 추정 위험률</span>
                <strong>{myRisk}%</strong>
              </div>

              <div className="mc-risk-scale">
                <div className="mc-risk-threshold low" style={{ left: '40%' }}>20%</div>
                <div className="mc-risk-threshold mid" style={{ left: '60%' }}>30%</div>
                <div className="mc-risk-track">
                  <div className={`mc-risk-fill-wide ${PBAR_CLASS[d.riskGrade]}`} style={{ width: `${Math.min((myRisk / maxRisk) * 100, 100)}%` }} />
                </div>
                <div className="mc-risk-scale-labels"><span>0%</span><span>50%+</span></div>
              </div>

              <div className="mc-card-sub" style={{ marginTop: 10 }}>
                위험요인: {d.riskFactors.join(', ')}
              </div>
            </div>
          );
        })}
      </div>

      {/* 필수 검진 대상 */}
      <div className="mc-sec-head" style={{ marginTop: 18 }}>
        <span className="mc-sec-title">필수 검진 대상</span>
      </div>
      <div className="mc-grid-auto-sm">
        <div className="mc-card mc-card-body mc-card-accent-warning">
          <div className="mc-row-between">
            <div>
              <div className="mc-card-title" style={{ fontSize: 14 }}>국가건강검진</div>
              <div className="mc-card-sub" style={{ marginTop: 4 }}>건강보험공단 기준 해당 연도 대상자 확인 필요</div>
            </div>
            <span className="mc-tag mc-tag-warning">확인 필요</span>
          </div>
        </div>
      </div>

      <div className="mc-sec-head" style={{ marginTop: 18 }}>
        <span className="mc-sec-title">예방접종 현황</span>
      </div>
      <div className="mc-card">
        {!vaccineAuth && !vaccineAuthStarted ? (
          <div className="mc-vaccine-split-gate">
            <div className="mc-vaccine-gate-panel">
              <div className="mc-vaccine-gate-top">
                <span className="mc-vaccine-auth-icon"><Ic d={P.lock} size={16}/></span>
                <span className="mc-vaccine-gate-badge">본인인증 필요</span>
              </div>
              <div className="mc-vaccine-gate-copyblock">
                <div className="mc-vaccine-auth-title">내 예방접종 내역 조회</div>
                <div className="mc-vaccine-auth-sub">접종 완료·미접종 내역은 추가 본인인증 후 안전하게 불러옵니다.</div>
              </div>
              <div className="mc-vaccine-gate-steps">
                <div><b>간편인증</b><span>카카오톡·휴대폰·네이버·토스</span></div>
                <div><b>개인정보 보호</b><span>입력 정보는 암호화 처리</span></div>
                <div><b>결과 확인</b><span>접종 완료/미접종 목록 표시</span></div>
              </div>
              <div className="mc-vaccine-gate-actions">
                <button className="mc-btn mc-btn-primary" type="button" onClick={() => setVaccineAuthStarted(true)}>
                  내 접종 내역 인증하기
                </button>
              </div>
            </div>

            <aside className="mc-vaccine-recommend-panel">
              <div className="mc-vaccine-recommend-head">
                <div className="mc-vaccine-recommend-title">권장 예방접종 체크</div>
                <div className="mc-vaccine-recommend-kicker">{currentCheckup.actualAge}세 기준 추천</div>
              </div>
              <div className="mc-vaccine-recommend-list">
                <div><b>독감</b><span>매년 1회 접종 권장</span></div>
                <div><b>코로나</b><span>최근 접종/감염 이력에 따라 확인</span></div>
                <div><b>B형간염</b><span>항체가 없거나 미접종이면 권장</span></div>
              </div>
            </aside>
          </div>
        ) : !vaccineAuth ? (
          <form className="mc-vaccine-auth" onSubmit={handleVaccineAuthRequest}>
            <div className="mc-vaccine-auth-head">
              <span className="mc-vaccine-auth-icon"><Ic d={P.lock} size={16}/></span>
              <div>
                <div className="mc-vaccine-auth-title">예방접종 내역 본인인증</div>
                <div className="mc-vaccine-auth-sub">성명, 주민등록번호, 전화번호로 본인확인을 완료해주세요.</div>
              </div>
            </div>

            <div className="mc-vaccine-auth-layout">
              <div className="mc-vaccine-auth-main">
                <div className="mc-vaccine-step-label"><b>1</b> 인증 방식 선택</div>
                <div className="mc-vaccine-method-grid">
                  {SIMPLE_AUTH_METHODS.map((method) => (
                    <button
                      key={method.value}
                      type="button"
                      className={`mc-vaccine-method ${vaccineAuthForm.method === method.value ? 'active' : ''}`}
                      onClick={() => { setVaccineAuthForm((f) => ({ ...f, method: method.value, code: '' })); setVaccineAuthRequested(false); setVaccineAuthMessage(''); }}
                    >
                      <span style={{ background: method.tone }} />
                      {method.label}
                    </button>
                  ))}
                </div>

                <div className="mc-vaccine-step-label"><b>2</b> 본인확인 정보 입력</div>
                <div className="mc-vaccine-auth-grid">
                  <label>
                    <span>성명</span>
                    <input className="mc-input" value={vaccineAuthForm.name} onChange={(e) => { setVaccineAuthForm((f) => ({ ...f, name: e.target.value })); setVaccineAuthMessage(''); }} />
                  </label>
                  <label>
                    <span>주민등록번호</span>
                    <div className="mc-vaccine-identity">
                      <input className="mc-input" inputMode="numeric" maxLength={6} value={vaccineAuthForm.identityFront} onChange={(e) => { setVaccineAuthForm((f) => ({ ...f, identityFront: cleanDigits(e.target.value).slice(0, 6) })); setVaccineAuthMessage(''); }} />
                      <b>-</b>
                      <input className="mc-input" type="password" inputMode="numeric" maxLength={7} value={vaccineAuthForm.identityBack} onChange={(e) => { setVaccineAuthForm((f) => ({ ...f, identityBack: cleanDigits(e.target.value).slice(0, 7) })); setVaccineAuthMessage(''); }} />
                    </div>
                  </label>
                  <label>
                    <span>전화번호</span>
                    <input className="mc-input" inputMode="tel" value={vaccineAuthForm.phoneNo} onChange={(e) => { setVaccineAuthForm((f) => ({ ...f, phoneNo: cleanDigits(e.target.value) })); setVaccineAuthMessage(''); }} placeholder="01012345678" />
                  </label>
                  {vaccineAuthForm.method === 'phone' && (
                    <label className="mc-vaccine-wide-field">
                      <span>통신사 / 인증방식</span>
                      <div className="mc-vaccine-phone-row">
                        <select className="mc-input" value={vaccineAuthForm.telecom} onChange={(e) => setVaccineAuthForm((f) => ({ ...f, telecom: e.target.value }))}>
                          {TELECOM_OPTIONS.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                        </select>
                        <select className="mc-input" value={vaccineAuthForm.phoneAuthType} onChange={(e) => { setVaccineAuthForm((f) => ({ ...f, phoneAuthType: e.target.value, code: '' })); setVaccineAuthRequested(false); setVaccineAuthMessage(''); }}>
                          {PHONE_AUTH_TYPES.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                        </select>
                      </div>
                    </label>
                  )}
                </div>

                {vaccineAuthRequested && vaccineAuthForm.method === 'phone' && vaccineAuthForm.phoneAuthType === 'sms' && (
                  <label className="mc-vaccine-code">
                    <span>인증번호</span>
                    <input className="mc-input" inputMode="numeric" maxLength={8} value={vaccineAuthForm.code} onChange={(e) => { setVaccineAuthForm((f) => ({ ...f, code: cleanDigits(e.target.value).slice(0, 8) })); setVaccineAuthMessage(''); }} placeholder="인증번호 입력" />
                  </label>
                )}

                {vaccineAuthMessage && <div className="mc-vaccine-message">{vaccineAuthMessage}</div>}

                <div className="mc-vaccine-actions">
                  <button className="mc-btn mc-btn-ghost" type="button" onClick={() => { setVaccineAuthStarted(false); setVaccineAuthRequested(false); setVaccineAuthMessage(''); }}>돌아가기</button>
                  <button className="mc-btn mc-btn-primary" type="submit">인증 요청</button>
                  <button className="mc-btn mc-btn-soft" type="button" onClick={handleVaccineAuthVerify} disabled={!vaccineAuthRequested}>인증 완료</button>
                </div>
              </div>

              <aside className="mc-vaccine-recommend-panel">
                <div className="mc-vaccine-recommend-head">
                  <div className="mc-vaccine-recommend-title">권장 예방접종 체크</div>
                  <div className="mc-vaccine-recommend-kicker">{currentCheckup.actualAge}세 기준 추천</div>
                </div>
                <div className="mc-vaccine-recommend-list">
                  <div><b>독감</b><span>매년 1회 접종 권장</span></div>
                  <div><b>코로나</b><span>최근 접종/감염 이력에 따라 확인</span></div>
                  <div><b>B형간염</b><span>항체가 없거나 미접종이면 권장</span></div>
                  </div>
              </aside>
            </div>
          </form>
        ) : (
          <table className="mc-tbl">
            <thead>
              <tr>
                <th>백신명</th>
                <th>접종 상태</th>
                <th>접종일</th>
              </tr>
            </thead>
            <tbody>
              {VACCINATION_DATA.map((vacc, idx) => (
                <tr key={idx}>
                  <td style={{ fontWeight: 600 }}>
                    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 8, color: 'var(--text-1)' }}>
                      <Ic d={P.syringe} size={12}/> {vacc.name}
                    </span>
                  </td>
                  <td>
                    <span className={`mc-tag ${vacc.status ? 'mc-tag-success' : 'mc-tag-warning'}`}>
                      <Ic d={vacc.status ? P.check : P.x} size={10}/>
                      {vacc.status ? ' 접종 완료' : ' 미접종'}
                    </span>
                  </td>
                  <td style={{ color: 'var(--text-2)' }}>{vacc.date || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {loading && (
        <div className="mc-alert mc-alert-blue" style={{ marginTop: 16 }}>
          <div>
            <div className="mc-alert-title">검진 기록 불러오는 중…</div>
            <div className="mc-alert-body">잠시만 기다려주세요.</div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CheckupRecords;
