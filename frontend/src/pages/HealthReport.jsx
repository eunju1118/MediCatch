import React, { useState, useEffect } from 'react';
import {
  XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, LineChart, Line, Legend, BarChart, Bar,
} from 'recharts';
import { analysisAPI } from '../api/services';

const Ic = ({ d, size = 13 }) => (
  <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.6"
    strokeLinecap="round" strokeLinejoin="round"
    style={{ width: size, height: size, flexShrink: 0 }}>{d}</svg>
);

const P = {
  download: (<><path d="M8 2v8M4 7l4 4 4-4"/><path d="M2 13h12"/></>),
  hospital: (<><path d="M2 14V6l6-3 6 3v8"/><path d="M6 14V9h4v5"/></>),
  syringe:  (<><path d="M10 2l4 4M8 4l4 4-6 6H2v-4z"/></>),
  check:    (<path d="M3 8l3 3 7-7"/>),
  x:        (<path d="M4 4l8 8M12 4l-8 8"/>),
  chart:    (<><path d="M3 13V7M8 13V3M13 13V9"/></>),
  calendar: (<><rect x="2" y="3" width="12" height="11" rx="1.5"/><path d="M2 7h12M5 1v3M11 1v3"/></>),
  heart:    (<path d="M8 14s-5-3-5-7a3 3 0 0 1 5-2 3 3 0 0 1 5 2c0 4-5 7-5 7z"/>),
  lock:     (<><rect x="3" y="7" width="10" height="7" rx="1.5"/><path d="M5.5 7V5.2a2.5 2.5 0 0 1 5 0V7"/></>),
};




const MOCK_RISK_TREND = [
  { year: '2023', stroke: 8,  diabetes: 20, cardio: 10 },
  { year: '2024', stroke: 10, diabetes: 25, cardio: 12 },
  { year: '2025', stroke: 12, diabetes: 28, cardio: 15 },
];

const MOCK_STATS = {
  visitCount: 8,
  checkupCount: 2,
  riskStatus: '주의',
  completedVaccines: 3,
  topHospital: '서울성모병원',
  topDepartment: '내과',
  lastCheckup: '2026-03-15',
};

const CHECKUP_TREND_DATA = [
  { year: '2023', bloodPressure: 135, bloodSugar: 105, cholesterol: 220 },
  { year: '2024', bloodPressure: 132, bloodSugar: 102, cholesterol: 228 },
  { year: '2025', bloodPressure: 128, bloodSugar: 98, cholesterol: 215 },
];


const HEALTH_MBTI_QUESTIONS = [
  {
    key: 'meal',
    title: '평소 식사는 어떤 편인가요?',
    options: [
      { value: 'fresh', label: '채소·단백질을 챙기는 편', desc: '집밥이나 균형식 비율이 높아요.' },
      { value: 'quick', label: '간편식/외식이 잦은 편', desc: '바쁠 때 빠르게 해결하는 편이에요.' },
      { value: 'skip', label: '끼니를 자주 거르는 편', desc: '식사 시간이 불규칙한 편이에요.' },
    ],
  },
  {
    key: 'protein',
    title: '단백질 섭취는 충분한 편인가요?',
    options: [
      { value: 'protein_ok', label: '매끼 조금씩 챙겨요', desc: '달걀, 생선, 두부, 고기, 콩류를 자주 먹어요.' },
      { value: 'protein_low', label: '탄수화물 위주가 많아요', desc: '밥·면·빵으로 때우는 날이 잦아요.' },
      { value: 'protein_unsure', label: '충분한지 잘 모르겠어요', desc: '특별히 계산해서 먹지는 않아요.' },
    ],
  },
  {
    key: 'activity',
    title: '활동량은 어느 쪽에 가까운가요?',
    options: [
      { value: 'active', label: '주 3회 이상 움직여요', desc: '운동이나 걷기를 의식적으로 해요.' },
      { value: 'light', label: '가벼운 산책 정도는 해요', desc: '꾸준하진 않지만 움직이려고 해요.' },
      { value: 'still', label: '앉아있는 시간이 길어요', desc: '하루 대부분을 책상 앞에서 보내요.' },
    ],
  },
  {
    key: 'sleep',
    title: '수면 리듬은 어떤가요?',
    options: [
      { value: 'regular', label: '비교적 규칙적이에요', desc: '자는 시간과 일어나는 시간이 안정적이에요.' },
      { value: 'late', label: '늦게 자는 편이에요', desc: '야간 활동이나 작업이 많은 편이에요.' },
      { value: 'short', label: '수면 시간이 부족해요', desc: '피로가 누적되는 날이 많아요.' },
    ],
  },
  {
    key: 'stress',
    title: '스트레스 관리는 어떤 편인가요?',
    options: [
      { value: 'release', label: '해소 루틴이 있어요', desc: '산책, 취미, 휴식으로 풀어요.' },
      { value: 'hold', label: '참다가 한 번에 풀어요', desc: '쌓인 뒤에야 쉬는 편이에요.' },
      { value: 'high', label: '요즘 계속 높은 편이에요', desc: '긴장 상태가 오래 이어져요.' },
    ],
  },
  {
    key: 'drink',
    title: '수분/카페인 습관은요?',
    options: [
      { value: 'water', label: '물을 자주 마셔요', desc: '카페인보다 물 섭취가 많아요.' },
      { value: 'coffee', label: '커피로 버티는 날이 많아요', desc: '카페인이 루틴의 중심이에요.' },
      { value: 'low', label: '마시는 양 자체가 적어요', desc: '수분 섭취를 자주 잊어요.' },
    ],
  },
  {
    key: 'sun',
    title: '햇빛을 쬐는 시간은 어떤가요?',
    options: [
      { value: 'sun_ok', label: '낮에 밖에 나가는 편', desc: '짧게라도 자연광을 보는 날이 많아요.' },
      { value: 'indoor', label: '실내에 있는 시간이 길어요', desc: '하루 대부분을 실내에서 보내요.' },
      { value: 'night', label: '주로 밤에 활동해요', desc: '낮 시간대 활동이 적은 편이에요.' },
    ],
  },
  {
    key: 'bodySignal',
    title: '최근 몸에서 자주 느끼는 신호가 있나요?',
    options: [
      { value: 'none', label: '특별한 신호는 없어요', desc: '컨디션이 크게 흔들리진 않아요.' },
      { value: 'cramp', label: '근육 뭉침·눈떨림이 있어요', desc: '피로하거나 긴장하면 더 느껴져요.' },
      { value: 'tired', label: '쉽게 피곤하고 회복이 느려요', desc: '자도 개운하지 않은 날이 있어요.' },
    ],
  },
  {
    key: 'mood',
    title: '최근 2주간 마음 상태는 어떤가요?',
    options: [
      { value: 'mood_ok', label: '대체로 괜찮아요', desc: '기분이 크게 무너지지는 않아요.' },
      { value: 'anxious', label: '걱정과 긴장이 많아요', desc: '생각이 많고 쉽게 쉬지 못해요.' },
      { value: 'down', label: '의욕이 낮고 우울감이 있어요', desc: '흥미가 줄거나 무기력한 날이 많아요.' },
    ],
  },
];

const getHealthMbtiResult = (answers) => {
  const values = Object.values(answers);
  const routineScore = ['fresh', 'regular', 'release', 'water', 'sun_ok'].filter((v) => values.includes(v)).length;
  const activeScore = ['active', 'light'].includes(answers.activity) ? 1 : 0;
  const nourishScore = ['fresh', 'water', 'protein_ok'].filter((v) => values.includes(v)).length;
  const pressureScore = ['late', 'short', 'hold', 'high', 'coffee', 'low', 'skip', 'indoor', 'night', 'cramp', 'tired', 'anxious', 'down'].filter((v) => values.includes(v)).length;

  const type = `${routineScore >= 3 ? 'R' : 'F'}${activeScore ? 'A' : 'S'}${nourishScore >= 2 ? 'N' : 'T'}${pressureScore >= 4 ? 'P' : 'B'}`;
  const isBalanced = type.includes('R') && type.includes('A') && type.includes('N') && type.includes('B');
  const needsRecovery = type.endsWith('P');
  const isStill = type[1] === 'S';

  const nutrientNeeds = [];
  if (['protein_low', 'protein_unsure'].includes(answers.protein) || ['quick', 'skip'].includes(answers.meal)) {
    nutrientNeeds.push({ name: '단백질', reason: '끼니가 불규칙하거나 탄수화물 위주일 때 부족해지기 쉬워요.', supplement: '단백질 파우더, 두부·달걀·생선·콩류' });
  }
  if (['cramp'].includes(answers.bodySignal) || ['high', 'hold'].includes(answers.stress) || ['short', 'late'].includes(answers.sleep)) {
    nutrientNeeds.push({ name: '마그네슘', reason: '긴장, 수면 부족, 근육 뭉침 신호가 있을 때 관리 후보가 될 수 있어요.', supplement: '마그네슘 보충제, 견과류, 녹색 채소' });
  }
  if (['indoor', 'night'].includes(answers.sun) || answers.activity === 'still') {
    nutrientNeeds.push({ name: '비타민 D', reason: '실내 생활이 길면 햇빛 노출이 부족해질 수 있어요.', supplement: '비타민 D, 낮 시간 10분 산책' });
  }
  if (answers.bodySignal === 'tired' || answers.meal === 'skip') {
    nutrientNeeds.push({ name: '철분/B군', reason: '피로감이 잦거나 식사가 불규칙하면 에너지 대사 영양을 점검해볼 만해요.', supplement: '비타민 B군, 철분은 검사 후 필요 시' });
  }
  const uniqueNutrients = nutrientNeeds.filter((item, idx, arr) => arr.findIndex((x) => x.name === item.name) === idx).slice(0, 3);
  if (uniqueNutrients.length === 0) {
    uniqueNutrients.push({ name: '기본 균형 유지', reason: '현재 응답상 큰 결핍 신호는 적어요.', supplement: '수분, 단백질, 채소 섭취 루틴 유지' });
  }

  const mindHealth = answers.mood === 'down'
    ? '최근 2주 이상 우울감·무기력·흥미 저하가 이어진다면 혼자 버티기보다 정신건강의학과나 상담센터 상담을 권장해요. 이 결과는 진단이 아니라 도움 신호예요.'
    : answers.mood === 'anxious'
      ? '걱정과 긴장이 오래 지속된다면 호흡·산책 같은 짧은 안정 루틴을 만들고, 일상 기능이 흔들릴 정도라면 상담을 받아보는 것이 좋아요.'
      : '마음건강 신호는 비교적 안정적으로 보여요. 지금의 회복 루틴을 유지해보세요.';

  const base = isBalanced ? {
    title: '루틴 밸런서형',
    tone: 'blue',
    summary: '식사·활동·휴식의 기본 루틴이 안정적인 편이에요.',
    tips: ['지금 루틴을 유지하면서 근력 운동을 조금 더해보세요.', '정기 검진 수치를 추적하면 장점이 더 잘 보여요.', '무리한 관리보다 지속 가능한 습관을 유지하세요.'],
  } : needsRecovery ? {
    title: '회복 우선형',
    tone: 'amber',
    summary: '건강 관리보다 피로와 스트레스 회복이 먼저 필요한 타입이에요.',
    tips: ['수면 시간을 먼저 고정하고 카페인 시간을 앞당겨보세요.', '하루 10분 산책처럼 작은 회복 루틴부터 시작하세요.', '검진 수치 중 혈압·혈당 변화를 함께 보는 걸 추천해요.'],
  } : isStill ? {
    title: '저활동 보완형',
    tone: 'green',
    summary: '식습관은 나쁘지 않아도 활동량이 부족해지기 쉬운 타입이에요.',
    tips: ['엘리베이터 대신 계단 1층처럼 작은 활동을 추가해보세요.', '식후 10분 걷기는 혈당 관리에 도움이 돼요.', '주 2회 가벼운 근력 루틴을 붙이면 좋아요.'],
  } : {
    title: '습관 리빌드형',
    tone: 'violet',
    summary: '좋은 습관과 흔들리는 습관이 섞여 있어 재정비 효과가 큰 타입이에요.',
    tips: ['식사·수면·활동 중 하나만 먼저 고정해보세요.', '주간 목표를 작게 잡으면 성공률이 높아요.', '건강 리포트의 위험도 변화를 한 달 단위로 확인해보세요.'],
  };

  return { type, ...base, nutrients: uniqueNutrients, mindHealth };
};


const HealthReport = () => {
  const [riskTrend, setRiskTrend] = useState(MOCK_RISK_TREND);
  const [stats, setStats] = useState(MOCK_STATS);
  const [loading, setLoading] = useState(false);
  const [showMbtiModal, setShowMbtiModal] = useState(false);
  const [mbtiStep, setMbtiStep] = useState(0);
  const [mbtiAnswers, setMbtiAnswers] = useState({});
  const [mbtiResult, setMbtiResult] = useState(null);

  useEffect(() => {
    const fetchReport = async () => {
      setLoading(true);
      try {
        const data = await analysisAPI.getHealthReport();
        if (data?.riskTrend) setRiskTrend(data.riskTrend);
        if (data?.stats) {
          setStats((prev) => ({
            ...prev,
            visitCount: data.stats.visitCount ?? data.stats.visit_count ?? prev.visitCount,
            checkupCount: data.stats.checkupCount ?? data.stats.checkup_count ?? prev.checkupCount,
            riskStatus: data.stats.riskStatus ?? data.stats.risk_status ?? prev.riskStatus,
            completedVaccines: data.stats.completedVaccines ?? data.stats.completed_vaccines ?? prev.completedVaccines,
            topHospital: data.stats.topHospital ?? data.stats.top_hospital ?? prev.topHospital,
            topDepartment: data.stats.topDepartment ?? data.stats.top_department ?? prev.topDepartment,
            lastCheckup: data.stats.lastCheckup ?? data.stats.last_checkup ?? prev.lastCheckup,
          }));
        }
      } catch (error) {
        console.error('Failed to fetch report:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchReport();
  }, []);

  const handlePDFDownload = () => {
    window.print();
  };

  const currentMbtiQuestion = HEALTH_MBTI_QUESTIONS[mbtiStep];
  const mbtiProgress = Math.round(((mbtiStep + 1) / HEALTH_MBTI_QUESTIONS.length) * 100);
  const openMbtiSurvey = () => {
    setShowMbtiModal(true);
    if (!mbtiResult) {
      setMbtiStep(0);
      setMbtiAnswers({});
    }
  };
  const selectMbtiAnswer = (value) => {
    const nextAnswers = { ...mbtiAnswers, [currentMbtiQuestion.key]: value };
    setMbtiAnswers(nextAnswers);
    if (mbtiStep < HEALTH_MBTI_QUESTIONS.length - 1) {
      setMbtiStep((n) => n + 1);
    } else {
      setMbtiResult(getHealthMbtiResult(nextAnswers));
    }
  };
  const resetMbtiSurvey = () => {
    setMbtiResult(null);
    setMbtiAnswers({});
    setMbtiStep(0);
  };

  return (
    <div className="mc-page fade-in">
      <div className="mc-page-top">
        <div>
          <div className="mc-page-title">통합 건강 리포트</div>
          <div className="mc-page-subtitle">건강검진, 진료 방문, 위험도 변화를 한눈에 확인하세요.</div>
        </div>
        <div className="mc-page-top-right">
          <button className="mc-btn mc-btn-ghost mc-health-mbti-top-btn mc-print-hide" onClick={openMbtiSurvey}>
            건강 MBTI
          </button>
          <button className="mc-btn mc-btn-primary mc-report-pdf-btn mc-print-hide" onClick={handlePDFDownload}>
            <Ic d={P.download} size={12}/> PDF 다운로드
          </button>
        </div>
      </div>


      <div className="mc-stats-strip">
        <div className="mc-stat">
          <div className="mc-stat-label">진료 방문</div>
          <div className="mc-stat-value">{stats.visitCount}회</div>
          <div className="mc-stat-sub">최근 12개월</div>
        </div>
        <div className="mc-stat">
          <div className="mc-stat-label">건강검진</div>
          <div className="mc-stat-value">{stats.checkupCount}건</div>
          <div className="mc-stat-sub">최근 검진 {stats.lastCheckup}</div>
        </div>
        <div className="mc-stat">
          <div className="mc-stat-label">위험도 상태</div>
          <div className="mc-stat-value" style={{ color: '#8A7040' }}>{stats.riskStatus}</div>
          <div className="mc-stat-sub">주요 지표 추적 중</div>
        </div>
        <div className="mc-stat">
          <div className="mc-stat-label">예방접종</div>
          <div className="mc-stat-value">{stats.completedVaccines}건</div>
          <div className="mc-stat-sub">접종 완료 기록</div>
        </div>
      </div>


      <div className="mc-sec-head" style={{ marginTop: 18 }}>
        <span className="mc-sec-title">3년 추이 분석</span>
      </div>
      <div className="mc-card mc-card-body">
        <div className="mc-chart-wrap">
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={CHECKUP_TREND_DATA} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#EBEEF4"/>
              <XAxis dataKey="year" tick={{ fill: '#4A5568', fontSize: 11 }} axisLine={{ stroke: '#DDE1EA' }}/>
              <YAxis tick={{ fill: '#9AA3B2', fontSize: 11 }} axisLine={{ stroke: '#DDE1EA' }}/>
              <Tooltip
                contentStyle={{
                  background: '#fff', border: '1px solid #DDE1EA', borderRadius: 6,
                  fontSize: 12, color: '#0D1520',
                }}
              />
              <Legend wrapperStyle={{ fontSize: 12, color: '#4A5568' }}/>
              <Bar dataKey="bloodPressure" fill="#D99AAA" fillOpacity={0.82} name="혈압"/>
              <Bar dataKey="bloodSugar" fill="#7FC9BC" fillOpacity={0.82} name="혈당"/>
              <Bar dataKey="cholesterol" fill="#63B7E6" fillOpacity={0.84} name="콜레스테롤"/>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="mc-two-col" style={{ gridTemplateColumns: '1.35fr 1fr', marginTop: 18 }}>
        <div>
          <div className="mc-sec-head">
            <span className="mc-sec-title">질병 위험도 추이</span>
          </div>
          <div className="mc-card mc-card-body">
            <div className="mc-chart-wrap">
              <ResponsiveContainer width="100%" height={370}>
                <LineChart data={riskTrend} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#EBEEF4"/>
                  <XAxis dataKey="year" tick={{ fill: '#4A5568', fontSize: 11 }} axisLine={{ stroke: '#DDE1EA' }}/>
                  <YAxis tick={{ fill: '#9AA3B2', fontSize: 11 }} axisLine={{ stroke: '#DDE1EA' }}/>
                  <Tooltip
                    contentStyle={{
                      background: '#fff', border: '1px solid #DDE1EA', borderRadius: 6,
                      fontSize: 12, color: '#0D1520',
                    }}
                  />
                  <Legend wrapperStyle={{ fontSize: 12, color: '#4A5568' }}/>
                  <Line type="monotone" dataKey="stroke"   stroke="#9A6060" name="뇌졸중"   strokeWidth={2} dot={{ r: 3 }}/>
                  <Line type="monotone" dataKey="diabetes" stroke="#8A7040" name="당뇨"     strokeWidth={2} dot={{ r: 3 }}/>
                  <Line type="monotone" dataKey="cardio"   stroke="#2F6FE8" name="심뇌혈관" strokeWidth={2} dot={{ r: 3 }}/>
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        <div className="mc-report-side">
          <div className="mc-report-side-section mc-print-hide">
            <div className="mc-sec-head">
              <span className="mc-sec-title">건강 MBTI</span>
            </div>
            <div className="mc-card mc-health-mbti-card">
              <div className="mc-health-mbti-orb">{mbtiResult ? mbtiResult.type : 'MBTI'}</div>
              <div className="mc-health-mbti-body">
                <div className="mc-health-mbti-copy">
                  <div className="mc-health-mbti-kicker">생활습관 설문</div>
                  <div className="mc-health-mbti-title">나의 건강 타입</div>
                  <p>{mbtiResult ? `${mbtiResult.title} · ${mbtiResult.summary}` : '식습관, 활동량, 수면 리듬을 선택하고 나의 건강 타입을 확인해보세요.'}</p>
                </div>
                <button className="mc-btn mc-btn-primary" type="button" onClick={openMbtiSurvey}>
                  {mbtiResult ? '결과 다시 보기' : '설문 시작하기'}
                </button>
              </div>
            </div>
          </div>

          <div className="mc-report-side-section">
            <div className="mc-sec-head mc-health-summary-head">
              <span className="mc-sec-title">건강 요약</span>
            </div>
            <div className="mc-card mc-card-body">
              <div className="mc-stack-sm">
                <div className="mc-kv">
                  <span className="mc-kv-key" style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}>
                    <Ic d={P.hospital} size={14}/> 주요 방문 병원
                  </span>
                  <span className="mc-kv-val">{stats.topHospital}</span>
                </div>
                <div className="mc-kv">
                  <span className="mc-kv-key" style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}>
                    <Ic d={P.chart} size={14}/> 주요 진료과
                  </span>
                  <span className="mc-kv-val">{stats.topDepartment}</span>
                </div>
                <div className="mc-kv">
                  <span className="mc-kv-key" style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}>
                    <Ic d={P.calendar} size={14}/> 최근 건강검진
                  </span>
                  <span className="mc-kv-val">{stats.lastCheckup}</span>
                </div>
                <div className="mc-kv">
                  <span className="mc-kv-key" style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}>
                    <Ic d={P.heart} size={14}/> 종합 상태
                  </span>
                  <span className="mc-kv-val">{stats.riskStatus}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>




      {showMbtiModal && (
        <div className="mc-modal-backdrop mc-print-hide" onClick={() => setShowMbtiModal(false)}>
          <div className="mc-modal mc-health-mbti-modal" onClick={(e) => e.stopPropagation()}>
            <div className="mc-modal-head">
              <div>
                <div className="mc-modal-title">건강 MBTI</div>
                <div className="mc-health-mbti-modal-sub">생활습관 선택으로 보는 나의 건강 관리 타입</div>
              </div>
              <button className="mc-modal-close" type="button" onClick={() => setShowMbtiModal(false)} aria-label="닫기">×</button>
            </div>
            <div className="mc-modal-body">
              {!mbtiResult ? (
                <div className="mc-health-mbti-survey">
                  <div className="mc-health-mbti-progress-row">
                    <span>{mbtiStep + 1}/{HEALTH_MBTI_QUESTIONS.length}</span>
                    <b>{mbtiProgress}%</b>
                  </div>
                  <div className="mc-health-mbti-progress"><i style={{ width: `${mbtiProgress}%` }} /></div>
                  <div className="mc-health-mbti-question">{currentMbtiQuestion.title}</div>
                  <div className="mc-health-mbti-options">
                    {currentMbtiQuestion.options.map((option) => (
                      <button key={option.value} type="button" onClick={() => selectMbtiAnswer(option.value)}>
                        <span>{option.label}</span>
                        <small>{option.desc}</small>
                      </button>
                    ))}
                  </div>
                  {mbtiStep > 0 && (
                    <button className="mc-health-mbti-back" type="button" onClick={() => setMbtiStep((n) => Math.max(0, n - 1))}>이전 질문</button>
                  )}
                </div>
              ) : (
                <div className={`mc-health-mbti-result ${mbtiResult.tone}`}>
                  <div className="mc-health-mbti-result-type">{mbtiResult.type}</div>
                  <div className="mc-health-mbti-result-title">{mbtiResult.title}</div>
                  <p>{mbtiResult.summary}</p>
                  <div className="mc-health-mbti-tip-list">
                    {mbtiResult.tips.map((tip) => <div key={tip}>{tip}</div>)}
                  </div>
                  <div className="mc-health-mbti-section-title">부족 가능 영양 · 추천</div>
                  <div className="mc-health-mbti-nutrient-list">
                    {mbtiResult.nutrients.map((item) => (
                      <div key={item.name} className="mc-health-mbti-nutrient-card">
                        <b>{item.name}</b>
                        <span>{item.reason}</span>
                        <small>추천: {item.supplement}</small>
                      </div>
                    ))}
                  </div>
                  <div className="mc-health-mbti-section-title">마음 건강 체크</div>
                  <div className={`mc-health-mbti-mind-note ${mbtiAnswers.mood === 'down' ? 'warning' : ''}`}>{mbtiResult.mindHealth}</div>
                  <div className="mc-health-mbti-disclaimer">영양제 추천은 생활습관 기반 참고용이며, 질환·복용약이 있으면 전문가와 먼저 상의해주세요.</div>
                  <div className="mc-health-mbti-result-actions">
                    <button className="mc-btn mc-btn-ghost" type="button" onClick={resetMbtiSurvey}>다시 하기</button>
                    <button className="mc-btn mc-btn-primary" type="button" onClick={() => setShowMbtiModal(false)}>결과 저장</button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}


      {loading && (
        <div className="mc-alert mc-alert-blue" style={{ marginTop: 16 }}>
          <div>
            <div className="mc-alert-title">데이터 불러오는 중...</div>
            <div className="mc-alert-body">잠시만 기다려주세요.</div>
          </div>
        </div>
      )}
    </div>
  );
};

export default HealthReport;
