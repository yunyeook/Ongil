-- ==========================================
-- 환자 인사이트 AI 분석용 테스트 데이터
-- PostgreSQL 문법
-- ==========================================
--
-- 사용법:
-- 1. 환자 4명의 ID를 지정 (예: 100, 101, 102, 103)
-- 2. 보호자 1명의 ID를 지정 (예: 1)
-- 3. SQL 실행하면 각 환자별로 다른 유형의 데이터 생성
--
-- 환자 유형:
-- - 환자 100: 정상 (LOW 위험) - 안정적, 큰 이슈 없음, 건강 데이터 포함
-- - 환자 101: 주의 (MEDIUM 위험) - 루틴 변화, 일부 경고, 건강 데이터 포함
-- - 환자 102: 위험 (HIGH 위험) - 여러 플래그 활성화, 긴급 상황, 건강 데이터 포함
-- - 환자 103: 건강 정보 없음 - 활동 데이터만 있고 건강 데이터 없음 (테스트용)
--
-- 중요: 이번 주가 아직 진행 중이므로, 완료된 "지난 주"를 분석합니다.
-- ==========================================

-- ==========================================
-- 변수 설정 (원하는 ID로 변경 가능)
-- ==========================================
DO $$
DECLARE
    guardian_id INT := 1;              -- 보호자 ID
    patient_normal_id INT := 100;      -- 정상 환자 ID
    patient_warning_id INT := 101;     -- 주의 환자 ID
    patient_danger_id INT := 102;      -- 위험 환자 ID
    patient_no_health_id INT := 103;   -- 건강 정보 없는 환자 ID (테스트용)
BEGIN

-- ==========================================
-- 1. 사용자 데이터 (환자 3명)
-- ==========================================

-- 정상 환자 (70대 남성, 안정적)
INSERT INTO users (id, provider, name, birth, phone_number, password, user_type, created_at, updated_at)
VALUES (
    patient_normal_id,
    'LOCAL',
    '김안정',
    '19500315',
    '010-1111-0001',
    '$2a$10$dummyHashedPassword1',
    'PATIENT',
    NOW() - INTERVAL '180 days',
    NOW() - INTERVAL '180 days'
) ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    birth = EXCLUDED.birth,
    phone_number = EXCLUDED.phone_number;

-- 주의 환자 (75대 여성, 경미한 인지 저하)
INSERT INTO users (id, provider, name, birth, phone_number, password, user_type, created_at, updated_at)
VALUES (
    patient_warning_id,
    'LOCAL',
    '이주의',
    '19480622',
    '010-2222-0002',
    '$2a$10$dummyHashedPassword2',
    'PATIENT',
    NOW() - INTERVAL '200 days',
    NOW() - INTERVAL '200 days'
) ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    birth = EXCLUDED.birth,
    phone_number = EXCLUDED.phone_number;

-- 위험 환자 (80대 남성, 심각한 인지 저하)
INSERT INTO users (id, provider, name, birth, phone_number, password, user_type, created_at, updated_at)
VALUES (
    patient_danger_id,
    'LOCAL',
    '박위험',
    '19440108',
    '010-3333-0003',
    '$2a$10$dummyHashedPassword3',
    'PATIENT',
    NOW() - INTERVAL '220 days',
    NOW() - INTERVAL '220 days'
) ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    birth = EXCLUDED.birth,
    phone_number = EXCLUDED.phone_number;

-- ==========================================
-- 2. DashboardCalc 데이터 (이번 주 & 지난 주)
-- ==========================================

-- 정상 환자: 안정적인 패턴
-- 이번 주
INSERT INTO dashboard_calc (patient_id, route_lost, safezone_emer, emer_call, sos_sign, safezone_exit, favorite, created_at, updated_at)
VALUES (
    patient_normal_id,
    0,
    1,
    0,
    0,
    '{"LEVEL1": 1, "LEVEL2": 0, "LEVEL3": 0}'::jsonb,
    '{"집": 25, "마트": 8, "공원": 5, "병원": 2}'::jsonb,
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '1 day'
) ON CONFLICT DO NOTHING;

-- 지난 주
INSERT INTO dashboard_calc (patient_id, route_lost, safezone_emer, emer_call, sos_sign, safezone_exit, favorite, created_at, updated_at)
VALUES (
    patient_normal_id,
    0,
    1,
    0,
    0,
    '{"LEVEL1": 1, "LEVEL2": 0, "LEVEL3": 0}'::jsonb,
    '{"집": 26, "마트": 7, "공원": 4, "병원": 3}'::jsonb,
    NOW() - INTERVAL '8 days',
    NOW() - INTERVAL '8 days'
) ON CONFLICT DO NOTHING;

-- 주의 환자: 경미한 변화 (MEDIUM 위험도 목표: 플래그 1-2개)
-- 이번 주
INSERT INTO dashboard_calc (patient_id, route_lost, safezone_emer, emer_call, sos_sign, safezone_exit, favorite, created_at, updated_at)
VALUES (
    patient_warning_id,
    1,  -- 경미한 길 잃음 (이전: 2)
    2,  -- 안전구역 이탈 2회 (이전: 4, 3 미만으로 조정)
    0,  -- 긴급 통화 없음 (이전: 1)
    0,  -- SOS 없음 (이전: 1)
    '{"LEVEL1": 2, "LEVEL2": 0, "LEVEL3": 0}'::jsonb,
    '{"집": 20, "마트": 6, "공원": 3}'::jsonb,  -- 다양성 유지
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '1 day'
) ON CONFLICT DO NOTHING;

-- 지난 주
INSERT INTO dashboard_calc (patient_id, route_lost, safezone_emer, emer_call, sos_sign, safezone_exit, favorite, created_at, updated_at)
VALUES (
    patient_warning_id,
    0,
    1,
    0,
    0,
    '{"LEVEL1": 1, "LEVEL2": 0, "LEVEL3": 0}'::jsonb,
    '{"집": 22, "마트": 6, "공원": 5, "약국": 2}'::jsonb,
    NOW() - INTERVAL '8 days',
    NOW() - INTERVAL '8 days'
) ON CONFLICT DO NOTHING;

-- 위험 환자: 심각한 상황
-- 이번 주
INSERT INTO dashboard_calc (patient_id, route_lost, safezone_emer, emer_call, sos_sign, safezone_exit, favorite, created_at, updated_at)
VALUES (
    patient_danger_id,
    5,
    8,
    3,
    4,
    '{"LEVEL1": 3, "LEVEL2": 3, "LEVEL3": 2}'::jsonb,
    '{"집": 15}'::jsonb,
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '1 day'
) ON CONFLICT DO NOTHING;

-- 지난 주
INSERT INTO dashboard_calc (patient_id, route_lost, safezone_emer, emer_call, sos_sign, safezone_exit, favorite, created_at, updated_at)
VALUES (
    patient_danger_id,
    2,
    3,
    1,
    2,
    '{"LEVEL1": 2, "LEVEL2": 1, "LEVEL3": 0}'::jsonb,
    '{"집": 18, "마트": 4, "공원": 2}'::jsonb,
    NOW() - INTERVAL '8 days',
    NOW() - INTERVAL '8 days'
) ON CONFLICT DO NOTHING;

-- ==========================================
-- 3. Abnormal 데이터 (이상행동)
-- ==========================================

-- 정상 환자: 이상행동 거의 없음
INSERT INTO abnormal_logs (
    patient_id, abnormal_type, safe_zone_level,
    latitude, longitude, center_latitude, center_longitude,
    distance_from_center, boundary_radius,
    created_at, updated_at
)
VALUES
    (patient_normal_id, 'SAFEZONE_EXIT', 'FIRST',
     37.5012, 127.0396, 37.5010, 127.0395,
     50.0, 100.0,
     NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

-- 주의 환자: 이상행동 증가 (시간대별 분산 - 주로 12-18시, 18-24시)
INSERT INTO abnormal_logs (
    patient_id, abnormal_type, safe_zone_level,
    latitude, longitude, center_latitude, center_longitude,
    distance_from_center, boundary_radius,
    elapsed_time, threshold_time,
    created_at, updated_at
)
VALUES
    -- 최근 7일간, 다양한 시간대
    -- 1일 전: 오후~저녁 (14시, 19시)
    (patient_warning_id, 'SAFEZONE_EXIT', 'SECOND',
     37.5025, 127.0420, 37.5010, 127.0395,
     250.0, 200.0,
     NULL, NULL,
     NOW() - INTERVAL '1 day' + INTERVAL '14 hours', NOW() - INTERVAL '1 day' + INTERVAL '14 hours'),
    (patient_warning_id, 'DEVIATE_FROM_THE_PATH', NULL,
     37.5040, 127.0440, NULL, NULL,
     NULL, NULL,
     NULL, NULL,
     NOW() - INTERVAL '1 day' + INTERVAL '19 hours', NOW() - INTERVAL '1 day' + INTERVAL '19 hours'),

    -- 2일 전: 오후 (15시)
    (patient_warning_id, 'SAFEZONE_EXIT', 'SECOND',
     37.5028, 127.0425, 37.5010, 127.0395,
     280.0, 200.0,
     NULL, NULL,
     NOW() - INTERVAL '2 days' + INTERVAL '15 hours', NOW() - INTERVAL '2 days' + INTERVAL '15 hours'),

    -- 3일 전: 오후 (13시)
    (patient_warning_id, 'DEVIATE_FROM_THE_PATH', NULL,
     37.5035, 127.0430, NULL, NULL,
     NULL, NULL,
     NULL, NULL,
     NOW() - INTERVAL '3 days' + INTERVAL '13 hours', NOW() - INTERVAL '3 days' + INTERVAL '13 hours'),

    -- 4일 전: 아침 (10시)
    (patient_warning_id, 'WANDER', 'FIRST',
     37.5015, 127.0400, 37.5010, 127.0395,
     80.0, 100.0,
     720, 600,
     NOW() - INTERVAL '4 days' + INTERVAL '10 hours', NOW() - INTERVAL '4 days' + INTERVAL '10 hours'),

    -- 5일 전: 저녁 (20시)
    (patient_warning_id, 'SAFEZONE_EXIT', 'FIRST',
     37.5020, 127.0410, 37.5010, 127.0395,
     150.0, 200.0,
     NULL, NULL,
     NOW() - INTERVAL '5 days' + INTERVAL '20 hours', NOW() - INTERVAL '5 days' + INTERVAL '20 hours');

-- 위험 환자: 이상행동 매우 많음 (모든 시간대 골고루, 야간 집중)
INSERT INTO abnormal_logs (
    patient_id, abnormal_type, safe_zone_level,
    latitude, longitude, center_latitude, center_longitude,
    distance_from_center, boundary_radius,
    elapsed_time, threshold_time,
    created_at, updated_at
)
VALUES
    -- 최근 7일간, 다양한 시간대 (야간 집중)
    -- 1일 전: 새벽~밤 (2시, 13시, 21시, 23시)
    (patient_danger_id, 'WANDER', 'FIRST',
     37.5020, 127.0410, 37.5010, 127.0395,
     150.0, 100.0,
     1200, 600,
     NOW() - INTERVAL '1 day' + INTERVAL '2 hours',
     NOW() - INTERVAL '1 day' + INTERVAL '2 hours'),
    (patient_danger_id, 'DEVIATE_FROM_THE_PATH', NULL,
     37.5060, 127.0520, NULL, NULL,
     NULL, NULL,
     NULL, NULL,
     NOW() - INTERVAL '1 day' + INTERVAL '13 hours', NOW() - INTERVAL '1 day' + INTERVAL '13 hours'),
    (patient_danger_id, 'SAFEZONE_EXIT', 'THIRD',
     37.5050, 127.0500, 37.5010, 127.0395,
     650.0, 500.0,
     NULL, NULL,
     NOW() - INTERVAL '1 day' + INTERVAL '21 hours', NOW() - INTERVAL '1 day' + INTERVAL '21 hours'),
    (patient_danger_id, 'WANDER', 'SECOND',
     37.5025, 127.0415, 37.5010, 127.0395,
     200.0, 150.0,
     1500, 600,
     NOW() - INTERVAL '1 day' + INTERVAL '23 hours',
     NOW() - INTERVAL '1 day' + INTERVAL '23 hours'),

    -- 2일 전: 새벽~밤 (1시, 15시, 22시)
    (patient_danger_id, 'WANDER', 'FIRST',
     37.5018, 127.0405, 37.5010, 127.0395,
     120.0, 100.0,
     900, 600,
     NOW() - INTERVAL '2 days' + INTERVAL '1 hour',
     NOW() - INTERVAL '2 days' + INTERVAL '1 hour'),
    (patient_danger_id, 'DEVIATE_FROM_THE_PATH', NULL,
     37.5065, 127.0530, NULL, NULL,
     NULL, NULL,
     NULL, NULL,
     NOW() - INTERVAL '2 days' + INTERVAL '15 hours', NOW() - INTERVAL '2 days' + INTERVAL '15 hours'),
    (patient_danger_id, 'SAFEZONE_EXIT', 'THIRD',
     37.5055, 127.0510, 37.5010, 127.0395,
     700.0, 500.0,
     NULL, NULL,
     NOW() - INTERVAL '2 days' + INTERVAL '22 hours', NOW() - INTERVAL '2 days' + INTERVAL '22 hours'),

    -- 3일 전: 오후~밤 (14시, 20시)
    (patient_danger_id, 'DEVIATE_FROM_THE_PATH', NULL,
     37.5070, 127.0540, NULL, NULL,
     NULL, NULL,
     NULL, NULL,
     NOW() - INTERVAL '3 days' + INTERVAL '14 hours', NOW() - INTERVAL '3 days' + INTERVAL '14 hours'),
    (patient_danger_id, 'SAFEZONE_EXIT', 'SECOND',
     37.5030, 127.0430, 37.5010, 127.0395,
     300.0, 200.0,
     NULL, NULL,
     NOW() - INTERVAL '3 days' + INTERVAL '20 hours', NOW() - INTERVAL '3 days' + INTERVAL '20 hours'),

    -- 4일 전: 아침~밤 (8시, 16시, 23시)
    (patient_danger_id, 'SAFEZONE_EXIT', 'FIRST',
     37.5025, 127.0420, 37.5010, 127.0395,
     180.0, 200.0,
     NULL, NULL,
     NOW() - INTERVAL '4 days' + INTERVAL '8 hours', NOW() - INTERVAL '4 days' + INTERVAL '8 hours'),
    (patient_danger_id, 'DEVIATE_FROM_THE_PATH', NULL,
     37.5075, 127.0550, NULL, NULL,
     NULL, NULL,
     NULL, NULL,
     NOW() - INTERVAL '4 days' + INTERVAL '16 hours', NOW() - INTERVAL '4 days' + INTERVAL '16 hours'),
    (patient_danger_id, 'WANDER', 'FIRST',
     37.5022, 127.0412, 37.5010, 127.0395,
     180.0, 100.0,
     850, 600,
     NOW() - INTERVAL '4 days' + INTERVAL '23 hours', NOW() - INTERVAL '4 days' + INTERVAL '23 hours'),

    -- 5일 전: 밤~새벽 (3시, 19시)
    (patient_danger_id, 'WANDER', 'FIRST',
     37.5019, 127.0407, 37.5010, 127.0395,
     140.0, 100.0,
     1000, 600,
     NOW() - INTERVAL '5 days' + INTERVAL '3 hours',
     NOW() - INTERVAL '5 days' + INTERVAL '3 hours'),
    (patient_danger_id, 'DEVIATE_FROM_THE_PATH', NULL,
     37.5080, 127.0560, NULL, NULL,
     NULL, NULL,
     NULL, NULL,
     NOW() - INTERVAL '5 days' + INTERVAL '19 hours', NOW() - INTERVAL '5 days' + INTERVAL '19 hours'),

    -- 6일 전: 오전~저녁 (7시, 17시)
    (patient_danger_id, 'SAFEZONE_EXIT', 'SECOND',
     37.5032, 127.0432, 37.5010, 127.0395,
     320.0, 200.0,
     NULL, NULL,
     NOW() - INTERVAL '6 days' + INTERVAL '7 hours', NOW() - INTERVAL '6 days' + INTERVAL '7 hours'),
    (patient_danger_id, 'WANDER', 'FIRST',
     37.5021, 127.0411, 37.5010, 127.0395,
     160.0, 100.0,
     950, 600,
     NOW() - INTERVAL '6 days' + INTERVAL '17 hours',
     NOW() - INTERVAL '6 days' + INTERVAL '17 hours');

-- ==========================================
-- 4. SOS 데이터 (긴급 요청)
-- ==========================================

-- 정상 환자: SOS 없음

-- 주의 환자: SOS 1건 (응답함)
INSERT INTO sos_logs (guardian_id, patient_id, latitude, longitude, is_responsed, created_at, updated_at)
VALUES
    (guardian_id, patient_warning_id, 37.5025, 127.0420, true, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

-- 위험 환자: SOS 4건 (미응답 2건 포함)
INSERT INTO sos_logs (guardian_id, patient_id, latitude, longitude, is_responsed, created_at, updated_at)
VALUES
    (guardian_id, patient_danger_id, 37.5050, 127.0500, false, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    (guardian_id, patient_danger_id, 37.5055, 127.0510, false, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (guardian_id, patient_danger_id, 37.5060, 127.0520, true, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (guardian_id, patient_danger_id, 37.5065, 127.0530, true, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

-- ==========================================
-- 5. HealthData (건강 데이터) - 최근 7일
-- ========================================== 

-- 정상 환자: 안정적인 건강 상태
-- 수면 (평균 7시간, 안정적)
INSERT INTO health_data (patient_id, type, average, max, min, unit, measured_at, created_at, updated_at)
VALUES
    (patient_normal_id, 'SLEEP', 7.2, 8.0, 6.5, 'hours', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    (patient_normal_id, 'SLEEP', 7.0, 7.8, 6.3, 'hours', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (patient_normal_id, 'SLEEP', 7.3, 8.2, 6.8, 'hours', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (patient_normal_id, 'SLEEP', 7.1, 7.9, 6.4, 'hours', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
    (patient_normal_id, 'SLEEP', 7.4, 8.1, 6.7, 'hours', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    (patient_normal_id, 'SLEEP', 7.2, 8.0, 6.6, 'hours', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
    (patient_normal_id, 'SLEEP', 7.0, 7.7, 6.2, 'hours', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

-- 걸음수 (평균 5000보, 안정적)
INSERT INTO health_data (patient_id, type, average, max, min, unit, measured_at, created_at, updated_at)
VALUES
    (patient_normal_id, 'STEP_COUNT', 5200, 6500, 4000, 'steps', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    (patient_normal_id, 'STEP_COUNT', 5100, 6300, 3900, 'steps', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (patient_normal_id, 'STEP_COUNT', 5300, 6700, 4200, 'steps', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (patient_normal_id, 'STEP_COUNT', 5000, 6200, 3800, 'steps', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
    (patient_normal_id, 'STEP_COUNT', 5400, 6800, 4300, 'steps', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    (patient_normal_id, 'STEP_COUNT', 5200, 6500, 4100, 'steps', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
    (patient_normal_id, 'STEP_COUNT', 5100, 6400, 4000, 'steps', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

-- 심박수 (정상 범위)
INSERT INTO health_data (patient_id, type, average, max, min, unit, measured_at, created_at, updated_at)
VALUES
    -- 심박수 변동성 낮음 (variability = max - min ≈ 25, 정상)
    (patient_normal_id, 'HEART_RATE', 72, 90, 65, 'bpm', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    (patient_normal_id, 'HEART_RATE', 73, 91, 66, 'bpm', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (patient_normal_id, 'HEART_RATE', 71, 89, 64, 'bpm', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (patient_normal_id, 'HEART_RATE', 74, 92, 67, 'bpm', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
    (patient_normal_id, 'HEART_RATE', 72, 90, 65, 'bpm', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    (patient_normal_id, 'HEART_RATE', 73, 91, 66, 'bpm', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
    (patient_normal_id, 'HEART_RATE', 71, 89, 64, 'bpm', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

-- 산소포화도 (정상)
INSERT INTO health_data (patient_id, type, average, max, min, unit, measured_at, created_at, updated_at)
VALUES
    (patient_normal_id, 'OXYGEN_SATURATION', 97.5, 99.0, 96.0, '%', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    (patient_normal_id, 'OXYGEN_SATURATION', 97.8, 99.0, 96.5, '%', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (patient_normal_id, 'OXYGEN_SATURATION', 97.6, 99.0, 96.2, '%', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (patient_normal_id, 'OXYGEN_SATURATION', 97.9, 99.0, 96.8, '%', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
    (patient_normal_id, 'OXYGEN_SATURATION', 97.7, 99.0, 96.4, '%', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    (patient_normal_id, 'OXYGEN_SATURATION', 97.5, 99.0, 96.0, '%', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
    (patient_normal_id, 'OXYGEN_SATURATION', 97.8, 99.0, 96.6, '%', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

-- 주의 환자: 건강 상태 약간 저하
-- 수면 (평균 5.5시간, 감소 추세)
INSERT INTO health_data (patient_id, type, average, max, min, unit, measured_at, created_at, updated_at)
VALUES
    (patient_warning_id, 'SLEEP', 5.2, 6.5, 4.0, 'hours', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    (patient_warning_id, 'SLEEP', 5.5, 6.8, 4.2, 'hours', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (patient_warning_id, 'SLEEP', 5.3, 6.6, 4.0, 'hours', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (patient_warning_id, 'SLEEP', 5.8, 7.0, 4.5, 'hours', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
    (patient_warning_id, 'SLEEP', 6.5, 7.5, 5.5, 'hours', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),  -- 지난 주는 더 좋았음
    (patient_warning_id, 'SLEEP', 6.8, 7.8, 5.8, 'hours', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
    (patient_warning_id, 'SLEEP', 6.6, 7.6, 5.6, 'hours', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

-- 걸음수 (감소 추세)
INSERT INTO health_data (patient_id, type, average, max, min, unit, measured_at, created_at, updated_at)
VALUES
    (patient_warning_id, 'STEP_COUNT', 3500, 5000, 2000, 'steps', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    (patient_warning_id, 'STEP_COUNT', 3700, 5200, 2200, 'steps', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (patient_warning_id, 'STEP_COUNT', 3600, 5100, 2100, 'steps', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (patient_warning_id, 'STEP_COUNT', 4500, 6000, 3000, 'steps', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),  -- 지난 주
    (patient_warning_id, 'STEP_COUNT', 4700, 6200, 3200, 'steps', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    (patient_warning_id, 'STEP_COUNT', 4600, 6100, 3100, 'steps', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
    (patient_warning_id, 'STEP_COUNT', 4800, 6300, 3300, 'steps', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

-- 심박수 (변동성 증가)
INSERT INTO health_data (patient_id, type, average, max, min, unit, measured_at, created_at, updated_at)
VALUES
    (patient_warning_id, 'HEART_RATE', 78, 110, 65, 'bpm', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),  -- 변동성 45
    (patient_warning_id, 'HEART_RATE', 79, 112, 66, 'bpm', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (patient_warning_id, 'HEART_RATE', 77, 108, 64, 'bpm', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (patient_warning_id, 'HEART_RATE', 75, 98, 62, 'bpm', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),  -- 지난 주 더 안정적
    (patient_warning_id, 'HEART_RATE', 76, 99, 63, 'bpm', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    (patient_warning_id, 'HEART_RATE', 74, 97, 61, 'bpm', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
    (patient_warning_id, 'HEART_RATE', 75, 98, 62, 'bpm', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

-- 산소포화도 (정상)
INSERT INTO health_data (patient_id, type, average, max, min, unit, measured_at, created_at, updated_at)
VALUES
    (patient_warning_id, 'OXYGEN_SATURATION', 96.5, 98.5, 94.5, '%', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    (patient_warning_id, 'OXYGEN_SATURATION', 96.8, 98.8, 94.8, '%', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (patient_warning_id, 'OXYGEN_SATURATION', 96.6, 98.6, 94.6, '%', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (patient_warning_id, 'OXYGEN_SATURATION', 97.0, 99.0, 95.0, '%', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
    (patient_warning_id, 'OXYGEN_SATURATION', 96.9, 98.9, 94.9, '%', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    (patient_warning_id, 'OXYGEN_SATURATION', 96.7, 98.7, 94.7, '%', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
    (patient_warning_id, 'OXYGEN_SATURATION', 97.0, 99.0, 95.0, '%', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

-- 위험 환자: 건강 상태 심각하게 저하
-- 수면 (평균 4시간, 매우 부족)
INSERT INTO health_data (patient_id, type, average, max, min, unit, measured_at, created_at, updated_at)
VALUES
    (patient_danger_id, 'SLEEP', 3.8, 5.0, 2.5, 'hours', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    (patient_danger_id, 'SLEEP', 4.2, 5.5, 3.0, 'hours', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (patient_danger_id, 'SLEEP', 3.5, 4.8, 2.2, 'hours', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (patient_danger_id, 'SLEEP', 5.8, 7.0, 4.5, 'hours', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),  -- 지난 주는 더 나았음
    (patient_danger_id, 'SLEEP', 6.0, 7.2, 4.8, 'hours', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    (patient_danger_id, 'SLEEP', 5.9, 7.1, 4.7, 'hours', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
    (patient_danger_id, 'SLEEP', 6.1, 7.3, 4.9, 'hours', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

-- 걸음수 (급격히 감소)
INSERT INTO health_data (patient_id, type, average, max, min, unit, measured_at, created_at, updated_at)
VALUES
    (patient_danger_id, 'STEP_COUNT', 2000, 3500, 800, 'steps', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    (patient_danger_id, 'STEP_COUNT', 2200, 3800, 1000, 'steps', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (patient_danger_id, 'STEP_COUNT', 1800, 3200, 600, 'steps', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (patient_danger_id, 'STEP_COUNT', 4800, 6500, 3000, 'steps', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),  -- 지난 주는 훨씬 활동적
    (patient_danger_id, 'STEP_COUNT', 5000, 6800, 3200, 'steps', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    (patient_danger_id, 'STEP_COUNT', 4900, 6700, 3100, 'steps', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
    (patient_danger_id, 'STEP_COUNT', 5100, 6900, 3300, 'steps', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

-- 심박수 (변동성 매우 높음)
INSERT INTO health_data (patient_id, type, average, max, min, unit, measured_at, created_at, updated_at)
VALUES
    (patient_danger_id, 'HEART_RATE', 85, 125, 70, 'bpm', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),  -- 변동성 55
    (patient_danger_id, 'HEART_RATE', 87, 128, 72, 'bpm', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (patient_danger_id, 'HEART_RATE', 83, 122, 68, 'bpm', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (patient_danger_id, 'HEART_RATE', 76, 100, 64, 'bpm', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),  -- 지난 주 더 안정적
    (patient_danger_id, 'HEART_RATE', 77, 102, 65, 'bpm', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    (patient_danger_id, 'HEART_RATE', 75, 99, 63, 'bpm', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
    (patient_danger_id, 'HEART_RATE', 78, 103, 66, 'bpm', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

-- 산소포화도 (저하)
INSERT INTO health_data (patient_id, type, average, max, min, unit, measured_at, created_at, updated_at)
VALUES
    (patient_danger_id, 'OXYGEN_SATURATION', 94.5, 96.5, 92.0, '%', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),  -- 95% 미만
    (patient_danger_id, 'OXYGEN_SATURATION', 94.8, 97.0, 92.5, '%', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    (patient_danger_id, 'OXYGEN_SATURATION', 94.2, 96.0, 91.5, '%', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    (patient_danger_id, 'OXYGEN_SATURATION', 96.5, 98.5, 94.5, '%', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),  -- 지난 주는 정상
    (patient_danger_id, 'OXYGEN_SATURATION', 96.8, 98.8, 94.8, '%', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    (patient_danger_id, 'OXYGEN_SATURATION', 96.6, 98.6, 94.6, '%', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
    (patient_danger_id, 'OXYGEN_SATURATION', 96.9, 98.9, 94.9, '%', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

-- ==========================================
-- 환자 103: 건강 정보 없는 환자 (HealthData 없음)
-- 목적: AI가 건강 데이터 없이도 분석을 잘 수행하는지 테스트
-- ==========================================

-- 1. 사용자 데이터 (환자 103) - 건강 정보 없는 환자
INSERT INTO users (id, provider, name, birth, phone_number, password, user_type, created_at, updated_at)
VALUES (
    patient_no_health_id,
    'LOCAL',
    '최건강무',
    '19500101',
    '010-3333-0103',
    '$2a$10$dummyHashedPassword103',
    'PATIENT',
    NOW() - INTERVAL '30 days',
    NOW() - INTERVAL '30 days'
) ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    birth = EXCLUDED.birth,
    phone_number = EXCLUDED.phone_number;

-- 2. DashboardCalc (이번 주 & 지난 주) - 중간 위험도
INSERT INTO dashboard_calc (patient_id, route_lost, safezone_emer, emer_call, sos_sign, safezone_exit, favorite, created_at, updated_at)
VALUES
    -- 이번 주
    (patient_no_health_id, 1, 2, 0, 1, '{"exits": [{"time": "2025-11-13 14:00:00", "level": "SECOND"}]}'::jsonb, '{"집": 18, "마트": 5}'::jsonb, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    -- 지난 주
    (patient_no_health_id, 0, 1, 0, 0, '{"exits": [{"time": "2025-11-06 10:00:00", "level": "FIRST"}]}'::jsonb, '{"집": 20, "마트": 6, "공원": 3}'::jsonb, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days')
ON CONFLICT DO NOTHING;

-- 3. Abnormal Logs (이번 주 기준)
INSERT INTO abnormal_logs (
    patient_id, abnormal_type, safe_zone_level,
    latitude, longitude, center_latitude, center_longitude,
    distance_from_center, boundary_radius,
    elapsed_time, threshold_time,
    created_at, updated_at
)
VALUES
    -- 지난 주: SAFEZONE_EXIT 1단계 (마포 근처)
    (patient_no_health_id, 'SAFEZONE_EXIT', 'FIRST',
     37.5510, 126.9100, 37.5500, 126.9100,
     150.0, 200.0,
     NULL, NULL,
     NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),

    -- 이번 주: SAFEZONE_EXIT 2단계 (용산 근처)
    (patient_no_health_id, 'SAFEZONE_EXIT', 'SECOND',
     37.5323, 126.9900, 37.5300, 126.9900,
     220.0, 200.0,
     NULL, NULL,
     NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

    -- 이번 주: 길 잃음(DEVIATE_FROM_THE_PATH) (강남 근처)
    (patient_no_health_id, 'DEVIATE_FROM_THE_PATH', NULL,
     37.4979, 127.0276, NULL, NULL,
     NULL, NULL,
     NULL, NULL,
     NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days')
ON CONFLICT DO NOTHING;

-- 4. SOS Logs (이번 주 기준)
INSERT INTO sos_logs (guardian_id, patient_id, latitude, longitude, is_responsed, created_at, updated_at)
VALUES
    -- 이번 주: 상암동 근처에서 SOS, 보호자 응답함
    (guardian_id, patient_no_health_id,
     37.5796, 126.8900,
     TRUE,
     NOW() - INTERVAL '3 days',
     NOW() - INTERVAL '3 days')
ON CONFLICT DO NOTHING;

-- 5. 건강 데이터 없음
-- *** 건강 데이터 (health_data)는 의도적으로 추가하지 않음 ***
-- AI가 활동 데이터만으로도 인사이트를 생성할 수 있는지 테스트

END $$;

-- ==========================================
-- 완료 메시지
-- ==========================================
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE '테스트 데이터 생성 완료!';
    RAISE NOTICE '========================================';
    RAISE NOTICE '환자 ID:';
    RAISE NOTICE '  - 정상 (LOW): 100 (건강 데이터 포함)';
    RAISE NOTICE '  - 주의 (MEDIUM): 101 (건강 데이터 포함)';
    RAISE NOTICE '  - 위험 (HIGH): 102 (건강 데이터 포함)';
    RAISE NOTICE '  - 건강 정보 없음: 103 (건강 데이터 없음, 활동만)';
    RAISE NOTICE '';
    RAISE NOTICE '다음 API로 인사이트 생성 (완료된 지난 주 분석):';
    RAISE NOTICE '  POST /api/v1/patients/100/insights/weekly';
    RAISE NOTICE '  POST /api/v1/patients/101/insights/weekly';
    RAISE NOTICE '  POST /api/v1/patients/102/insights/weekly';
    RAISE NOTICE '  POST /api/v1/patients/103/insights/weekly  (건강 정보 없음 테스트)';
    RAISE NOTICE '';
    RAISE NOTICE '주의: 이번 주가 아직 진행 중이므로, 완료된 "지난 주"를 분석합니다.';
    RAISE NOTICE '========================================';
END $$;