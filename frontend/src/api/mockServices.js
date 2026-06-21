import { DEMO_TOKENS, DEMO_USER } from '../config/demoMode';

const wait = (value, delay = 180) => new Promise((resolve) => {
  window.setTimeout(() => resolve(value), delay);
});

const clone = (value) => JSON.parse(JSON.stringify(value));
const ok = (value) => wait(clone(value));

const medicalRecords = [
  {
    id: 'mr-20260521',
    visitDate: '2026-05-21',
    hospitalName: '서울성모병원',
    hospital: '서울성모병원',
    department: '내과',
    treatmentType: '외래',
    diagnosis: '고혈압 경계',
    diseaseCode: 'I10',
    patientPayment: 21000,
    insurancePayment: 42000,
    totalCost: 63000,
    nonCoveredAmount: 6000,
    claimStatus: '가능',
  },
  {
    id: 'mr-20260412',
    visitDate: '2026-04-12',
    hospitalName: '강남정형외과의원',
    hospital: '강남정형외과의원',
    department: '정형외과',
    treatmentType: '외래',
    diagnosis: '요추 염좌',
    diseaseCode: 'S33',
    patientPayment: 34000,
    insurancePayment: 68000,
    totalCost: 102000,
    nonCoveredAmount: 28000,
    claimStatus: '가능',
  },
  {
    id: 'mr-20260315',
    visitDate: '2026-03-15',
    hospitalName: '서울성모병원',
    hospital: '서울성모병원',
    department: '검진센터',
    treatmentType: '건강검진',
    diagnosis: '혈당 추적 검사',
    diseaseCode: 'Z00',
    patientPayment: 0,
    insurancePayment: 0,
    totalCost: 0,
    nonCoveredAmount: 0,
    claimStatus: '제외',
  },
  {
    id: 'mr-20260203',
    visitDate: '2026-02-03',
    hospitalName: '튼튼약국',
    hospital: '튼튼약국',
    department: '약국',
    treatmentType: '약국',
    diagnosis: '처방 조제',
    diseaseCode: '$',
    patientPayment: 5200,
    insurancePayment: 8800,
    totalCost: 14000,
    nonCoveredAmount: 0,
    claimStatus: '제외',
  },
  {
    id: 'mr-20251108',
    visitDate: '2025-11-08',
    hospitalName: '연세가정의학과',
    hospital: '연세가정의학과',
    department: '가정의학과',
    treatmentType: '외래',
    diagnosis: '알레르기 비염',
    diseaseCode: 'J30',
    patientPayment: 16000,
    insurancePayment: 28000,
    totalCost: 44000,
    nonCoveredAmount: 4000,
    claimStatus: '가능',
  },
];

const checkupResults = [
  {
    id: 'ck-20260315',
    checkupDate: '2026-03-15',
    checkupType: '일반검진',
    organizationName: '서울성모병원',
    bloodPressureSystolic: 128,
    bloodPressureDiastolic: 82,
    glucose: 98,
    totalCholesterol: 215,
    hdlCholesterol: 55,
    ldlCholesterol: 104,
    triglyceride: 132,
    bmi: 26.4,
    height: 172,
    weight: 78,
    waist: 86,
    serumCreatinine: 0.64,
    gammaGtp: 18,
    abnormalFindings: '혈압과 콜레스테롤은 추적 관리가 필요합니다.',
    recommendations: '체중, 혈압, 지질 수치를 6개월 단위로 확인해보세요.',
  },
  {
    id: 'ck-20250310',
    checkupDate: '2025-03-10',
    checkupType: '일반검진',
    organizationName: '서울성모병원',
    bloodPressureSystolic: 124,
    bloodPressureDiastolic: 79,
    glucose: 92,
    totalCholesterol: 205,
    hdlCholesterol: 57,
    ldlCholesterol: 101,
    triglyceride: 118,
    bmi: 25.7,
    height: 172,
    weight: 76,
    waist: 84,
    serumCreatinine: 0.7,
    gammaGtp: 16,
    abnormalFindings: '혈압이 경계 범위에 가까워 생활습관 관리가 권장됩니다.',
    recommendations: '규칙적인 운동과 식단 관리를 유지해보세요.',
  },
];

const diseasePredictions = [
  {
    id: 'risk-stroke',
    predictionType: 'STROKE',
    checkupDate: '2026-03-15',
    riskRatio: 18.5,
    averageRatio: '20/100',
    riskGrade: '좋음',
    factors: [
      { riskFactor: '혈압', currentValue: '128/82', averageValue: '120/80', severity: '보통' },
      { riskFactor: 'BMI', currentValue: '26.4', averageValue: '24.0', severity: '보통' },
    ],
    compares: [
      { year: 2025, predictedState: 20 },
      { year: 2026, predictedState: 18.5 },
      { year: 2027, predictedState: 17.2 },
    ],
  },
  {
    id: 'risk-diabetes',
    predictionType: 'DIABETES',
    checkupDate: '2026-03-15',
    riskRatio: 28.5,
    averageRatio: '38/100',
    riskGrade: '보통',
    factors: [
      { riskFactor: '공복혈당', currentValue: '98', averageValue: '92', severity: '보통' },
      { riskFactor: 'BMI', currentValue: '26.4', averageValue: '24.0', severity: '보통' },
    ],
    compares: [
      { year: 2025, predictedState: 28 },
      { year: 2026, predictedState: 25 },
      { year: 2027, predictedState: 23 },
    ],
  },
  {
    id: 'risk-cardio',
    predictionType: 'CARDIO',
    checkupDate: '2026-03-15',
    riskRatio: 20.3,
    averageRatio: '18/100',
    riskGrade: '좋음',
    factors: [
      { riskFactor: 'LDL', currentValue: '104', averageValue: '100', severity: '좋음' },
      { riskFactor: '혈압', currentValue: '128/82', averageValue: '120/80', severity: '보통' },
    ],
    compares: [
      { year: 2025, predictedState: 21 },
      { year: 2026, predictedState: 20.3 },
      { year: 2027, predictedState: 19 },
    ],
  },
];

const healthAge = {
  biologicalAge: 38,
  chronologicalAge: 42,
  age: 42,
  checkupDate: '2026-03-15',
  summaryNote: '실제 나이보다 4세 낮게 예측되었습니다.',
  detailMessage: '혈압과 콜레스테롤은 경계 범위에 가까웠지만, 혈당과 신장 기능 지표가 안정적으로 유지되어 건강나이가 낮게 계산되었습니다.',
  changeAfterMessage: '체중과 혈압을 조금 더 낮추면 건강나이 개선 폭이 더 커질 수 있습니다.',
  factors: [
    { riskFactor: '허리둘레', currentValue: '86', averageValue: '85', severity: '보통' },
    { riskFactor: '혈압', currentValue: '128/82', averageValue: '120/80', severity: '보통' },
    { riskFactor: '공복혈당', currentValue: '98', averageValue: '100 미만', severity: '좋음' },
    { riskFactor: 'HDL콜레스테롤', currentValue: '55', averageValue: '40 이상', severity: '좋음' },
  ],
};

const coverageItems = [
  { id: 'cov-1', name: '상해·질병 입원의료비', itemName: '상해·질병 입원의료비', category: '입원', amount: 50000000, maxBenefitAmount: 50000000, agreementType: '실손', isCovered: true },
  { id: 'cov-2', name: '상해·질병 통원의료비', itemName: '상해·질병 통원의료비', category: '통원', amount: 250000, maxBenefitAmount: 250000, agreementType: '실손', isCovered: true },
  { id: 'cov-3', name: '암 진단비', itemName: '암 진단비', category: '진단', amount: 30000000, maxBenefitAmount: 30000000, agreementType: '정액', isCovered: true },
  { id: 'cov-4', name: '일반 수술비', itemName: '일반 수술비', category: '수술', amount: 2000000, maxBenefitAmount: 2000000, agreementType: '정액', isCovered: true },
  { id: 'cov-5', name: 'MRI·도수치료 특약', itemName: 'MRI·도수치료 특약', category: '검사', amount: 1500000, maxBenefitAmount: 1500000, agreementType: '특약', isCovered: true },
  { id: 'cov-6', name: '입원 일당', itemName: '입원 일당', category: '입원', amount: 50000, maxBenefitAmount: 50000, agreementType: '정액', isCovered: true },
];

const policies = [
  {
    id: 'policy-1',
    policyNumber: 'D-2026-0001',
    companyName: '삼성생명보험',
    insurer_name: '삼성생명보험',
    productName: '실손의료비4.0 기본[질병입원,선택Ⅱ]',
    policy_details: '실손의료비4.0 기본[질병입원,선택Ⅱ]',
    policyType: 'SUPPLEMENTARY',
    insurance_type: 'SUPPLEMENTARY',
    contractStatus: 'ACTIVE',
    status: 'ACTIVE',
    isActive: true,
    startDate: '2024-11-06',
    endDate: '2096-11-06',
    monthlyPremium: 48500,
    premiumAmount: 48500,
    paymentCycle: '월납',
    hasSupplementaryCoverage: true,
    coverageItems: coverageItems.slice(0, 2),
  },
  {
    id: 'policy-2',
    policyNumber: 'D-2026-0002',
    companyName: '현대해상',
    insurer_name: '현대해상',
    productName: '굿앤굿 건강종합보험',
    policy_details: '굿앤굿 건강종합보험',
    policyType: 'HEALTH',
    insurance_type: 'HEALTH',
    contractStatus: 'ACTIVE',
    status: 'ACTIVE',
    isActive: true,
    startDate: '2023-04-21',
    endDate: '2087-04-21',
    monthlyPremium: 89700,
    premiumAmount: 89700,
    paymentCycle: '월납',
    hasSupplementaryCoverage: false,
    coverageItems: coverageItems.slice(2, 5),
  },
  {
    id: 'policy-3',
    policyNumber: 'D-2026-0003',
    companyName: 'NH농협생명',
    insurer_name: 'NH농협생명',
    productName: 'NH치료보험 기본형',
    policy_details: 'NH치료보험 기본형',
    policyType: 'HEALTH',
    insurance_type: 'HEALTH',
    contractStatus: 'ACTIVE',
    status: 'ACTIVE',
    isActive: true,
    startDate: '2022-08-29',
    endDate: '2096-08-29',
    monthlyPremium: 67127,
    premiumAmount: 67127,
    paymentCycle: '월납',
    hasSupplementaryCoverage: false,
    coverageItems: coverageItems.slice(5),
  },
];

const coverageComparison = [
  {
    id: 'gap-dental',
    coverageName: '치아 보장',
    coverageCode: 'DENTAL',
    selfCoverageAmount: 0,
    avgGroupCoverageAmount: 2000000,
  },
  {
    id: 'gap-cancer',
    coverageName: '암 보장',
    coverageCode: 'CANCER',
    selfCoverageAmount: 30000000,
    avgGroupCoverageAmount: 50000000,
  },
  {
    id: 'gap-brain',
    coverageName: '뇌질환 보장',
    coverageCode: 'BRAIN',
    selfCoverageAmount: 10000000,
    avgGroupCoverageAmount: 30000000,
  },
  {
    id: 'gap-heart',
    coverageName: '심혈관 보장',
    coverageCode: 'CARDIO',
    selfCoverageAmount: 15000000,
    avgGroupCoverageAmount: 30000000,
  },
  {
    id: 'gap-surgery',
    coverageName: '수술 보장',
    coverageCode: 'SURGERY',
    selfCoverageAmount: 2000000,
    avgGroupCoverageAmount: 5000000,
  },
];

const hospitals = [
  { id: 'hospital-1', yadmNm: '서울성모병원', addr: '서울특별시 서초구 반포대로 222', telno: '1588-1511', lat: 37.5019, lng: 127.0048, clCdNm: '상급종합' },
  { id: 'hospital-2', yadmNm: '강남세브란스병원', addr: '서울특별시 강남구 언주로 211', telno: '1599-6114', lat: 37.4927, lng: 127.0469, clCdNm: '상급종합' },
  { id: 'hospital-3', yadmNm: '연세가정의학과', addr: '서울특별시 강남구 테헤란로 123', telno: '02-1234-5678', lat: 37.5012, lng: 127.0396, clCdNm: '의원' },
];

const preTreatmentResult = (query) => ({
  query,
  matched: true,
  confidence: 'HIGH',
  matchSource: 'DB_RULE',
  classification: {
    injuryDiseaseType: query.includes('골절') ? 'INJURY' : 'DISEASE',
    careType: query.includes('입원') ? 'INPATIENT' : query.includes('수술') ? 'SURGERY' : 'OUTPATIENT',
    benefitType: query.includes('도수') || query.includes('MRI') ? 'NON_COVERED' : 'MIXED',
    cautionMessage: '실제 지급 여부는 진료비 영수증의 급여/비급여 구분과 약관 조건에 따라 달라질 수 있습니다.',
  },
  fixedBenefits: {
    applicable: true,
    ownedGroups: [
      {
        category: 'SURGERY',
        displayName: '수술비',
        owned: true,
        matchedItemCount: 1,
        totalCoverageAmount: 2000000,
        matchedItems: [
          { policyId: 'policy-2', itemName: '일반 수술비', coverageAmount: 2000000 },
        ],
      },
      {
        category: 'TEST',
        displayName: 'MRI·검사',
        owned: true,
        matchedItemCount: 1,
        totalCoverageAmount: 1500000,
        matchedItems: [
          { policyId: 'policy-2', itemName: 'MRI·도수치료 특약', coverageAmount: 1500000 },
        ],
      },
    ],
  },
  actualLoss: {
    applicable: true,
    ownedPolicies: [
      {
        policyId: 'policy-1',
        policyName: '실손의료비4.0 기본',
        insurerName: '삼성생명보험',
        generationLabel: '4세대 실손',
        estimatedGenerationCode: 'G4',
        matchedCoverageNames: ['상해·질병 통원의료비', '상해·질병 입원의료비'],
        matchedCoverageItems: coverageItems.slice(0, 2),
      },
    ],
    selectedRules: [
      {
        generationCode: 'G4',
        actualLossCategory: query.includes('MRI') || query.includes('도수') ? 'NON_COVERED_THREE' : 'GENERAL_OUTPATIENT',
        careType: query.includes('입원') ? 'INPATIENT' : 'OUTPATIENT',
        benefitType: query.includes('MRI') || query.includes('도수') ? 'NON_COVERED' : 'MIXED',
        reimbursementRate: 70,
        patientCopayRate: 30,
        fixedDeductible: 30000,
        deductibleMethod: 'MAX_FIXED_OR_RATE',
        limitAmount: 250000,
        limitCount: 1,
        requiresRider: query.includes('MRI') || query.includes('도수'),
        note: '비급여 3종은 특약과 횟수 조건을 함께 확인해야 합니다.',
      },
    ],
  },
  nextQuestions: [
    '진료비 예상 금액이 어느 정도인가요?',
    '입원/통원 여부가 정해졌나요?',
    '비급여 항목이 포함되어 있나요?',
  ],
});

const peerTable = [
  { min: 0, max: 9, label: '10대 이하', average: 32000 },
  { min: 10, max: 19, label: '10대', average: 58000 },
  { min: 20, max: 29, label: '20대', average: 104000 },
  { min: 30, max: 39, label: '30대', average: 221000 },
  { min: 40, max: 49, label: '40대', average: 395661 },
  { min: 50, max: 59, label: '50대', average: 468000 },
  { min: 60, max: 69, label: '60대', average: 512000 },
  { min: 70, max: 79, label: '70대', average: 548000 },
  { min: 80, max: 89, label: '80대', average: 576000 },
  { min: 90, max: 200, label: '90대 이상', average: 602000 },
];

const getPeerBenchmark = (age) => {
  const targetAge = Number(age || healthAge.chronologicalAge || 42);
  const bucket = peerTable.find((item) => targetAge >= item.min && targetAge <= item.max) || peerTable[4];
  const monthlyPremium = policies.reduce((sum, policy) => sum + Number(policy.monthlyPremium || 0), 0);
  const difference = monthlyPremium - bucket.average;
  return {
    ageGroupLabel: bucket.label,
    averageMonthlyPremium: bucket.average,
    userMonthlyPremium: monthlyPremium,
    difference,
    percentage: bucket.average > 0 ? Math.round((monthlyPremium / bucket.average) * 100) : 0,
    status: difference > 0 ? '또래보다 높음' : '또래보다 낮음',
    source: 'PORTFOLIO_DEMO',
  };
};

const mockAuthAPI = {
  login: (data) => ok({
    ...DEMO_USER,
    codefId: data?.codefId || DEMO_USER.codefId,
    ...DEMO_TOKENS,
  }),
  signupStep1: () => ok({ sessionKey: 'demo-signup-session' }),
  signupStep2: () => ok({ success: true }),
  signupStep3: () => ok({ success: true }),
  signupStep4: () => ok({ success: true }),
  refresh: () => ok(DEMO_TOKENS),
  profile: () => ok(DEMO_USER),
  changeEmailStep1: () => ok({ sessionKey: 'demo-change-email-session' }),
  changeEmailStep2: () => ok({ success: true }),
  changeEmailStep3: () => ok({ success: true }),
  changePwdStep1: () => ok({ sessionKey: 'demo-change-pwd-session' }),
  changePwdStep2: () => ok({ success: true }),
  changePwdStep3: () => ok({ success: true }),
  forgotPwdStep1: () => ok({ sessionKey: 'demo-forgot-session' }),
  forgotPwdStep2: () => ok({ verified: true, tempPasswordRequired: false }),
  forgotPwdStep3: () => ok({ success: true }),
  forgotPwdStep4: () => ok({ success: true }),
  withdraw: () => ok({ success: true }),
};

const mockHealthAPI = {
  getMedicalRecords: () => ok(medicalRecords),
  getMedications: () => ok([
    { id: 'med-1', medicineName: '혈압약', prescribedDate: '2026-05-21', days: 14 },
  ]),
  getCheckupResults: () => ok(checkupResults),
  getDiseasePredictions: () => ok(diseasePredictions),
  getHealthAge: () => ok(healthAge),
  syncCheckupStep1: () => ok({ sessionKey: 'demo-checkup-session' }, 420),
  syncCheckupStep2: () => ok({ savedCheckups: checkupResults.length }, 420),
  syncMedicalStep1: () => ok({ sessionKey: 'demo-medical-session' }, 420),
  syncMedicalStep2: () => ok({ savedMedicals: medicalRecords.length, savedMedications: 1 }, 420),
  getHospitals: () => ok(hospitals),
};

const mockInsuranceAPI = {
  getPolicies: () => ok(policies),
  getCoverage: (policyId) => ok(policies.find((policy) => policy.id === policyId)?.coverageItems || coverageItems),
  getCoverageComparison: () => ok(coverageComparison),
  getPeerPremiumBenchmark: (params = {}) => ok(getPeerBenchmark(params.age)),
  getSummary: () => ok({
    totalMonthlyPremium: policies.reduce((sum, policy) => sum + Number(policy.monthlyPremium || 0), 0),
    activePolicyCount: policies.length,
    totalCoverageAmount: policies.reduce((sum, policy) => (
      sum + policy.coverageItems.reduce((inner, item) => inner + Number(item.amount || 0), 0)
    ), 0),
  }),
  sync: () => ok({ savedPolicies: policies.length }, 420),
};

const mockAnalysisAPI = {
  searchPreTreatment: ({ query }) => ok(preTreatmentResult(query || '도수치료'), 320),
  searchTreatment: (keyword) => ok(preTreatmentResult(keyword || '도수치료'), 320),
  getCoverageGap: () => ok(coverageComparison.filter((item) => (
    Number(item.avgGroupCoverageAmount || 0) > 0
    && Number(item.selfCoverageAmount || 0) < Number(item.avgGroupCoverageAmount || 0)
  ))),
};

const mockChatAPI = {
  sendMessage: (message) => ok({
    role: 'assistant',
    message: `데모 응답입니다. "${message}"에 대해 현재 mock 보험·건강 데이터를 기준으로 보면, 실손 보장 여부와 보장 공백을 함께 확인하는 것이 좋아요.\n\n- 건강검진: 혈압과 콜레스테롤은 추적 관리 권장\n- 보험: 치아, 뇌질환, 심혈관 보장이 평균 대비 낮은 편\n- 다음 단계: 진료 전 검색에서 치료명으로 보장 조건을 확인해보세요.`,
  }, 480),
  getHistory: () => ok({ messages: [] }),
  clearHistory: () => ok({ success: true }),
};

export const mockAPIs = {
  authAPI: mockAuthAPI,
  healthAPI: mockHealthAPI,
  insuranceAPI: mockInsuranceAPI,
  analysisAPI: mockAnalysisAPI,
  chatAPI: mockChatAPI,
};
