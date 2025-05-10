-- 현재 진행 중인 이벤트 추가
INSERT INTO event (id, created_at, updated_at, name, content, venue, event_date, sales_start_date, sales_end_date, total_seats, status, is_queue_active)
VALUES 
    (9, NOW(), NOW(), '현재 진행중인 공연', '지금 이 시간 진행 중인 매력적인 공연', '홍대 예술의 전당', 
     NOW(), DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 500, 'ONGOING', false),
    
    (10, NOW(), NOW(), '오픈 예정 공연', '곧 예매가 시작될 기대되는 공연', '코엑스 아티움', 
     DATE_ADD(NOW(), INTERVAL 60 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 59 DAY), 800, 'UPCOMING', false);

-- 인기 있는 이벤트용 티켓 타입 추가
INSERT INTO ticket_type (id, created_at, updated_at, event_id, name, price, quantity, available_quantity, content)
VALUES 
    (21, NOW(), NOW(), 9, 'VIP', 100000.00, 100, 0, 'VIP 서비스 포함'),
    (22, NOW(), NOW(), 9, '일반석', 70000.00, 400, 0, '일반 입장권'),
    
    (23, NOW(), NOW(), 10, 'VVIP', 200000.00, 100, 100, '최고급 서비스 + 백스테이지 투어'),
    (24, NOW(), NOW(), 10, 'VIP', 150000.00, 200, 200, 'VIP 전용 라운지 이용'),
    (25, NOW(), NOW(), 10, '일반석', 80000.00, 500, 500, '일반 입장권');

-- 인기 있는 이벤트에 대한 티켓 추가 (모두 판매됨)
INSERT INTO ticket (id, created_at, updated_at, event_id, ticket_type_id, seat_number, price, status, reserved_by_member_id, reserved_at)
VALUES 
    (16, NOW(), NOW(), 9, 21, 'VIP-1', 100000.00, 'SOLD', 2, DATE_SUB(NOW(), INTERVAL 20 DAY)),
    (17, NOW(), NOW(), 9, 21, 'VIP-2', 100000.00, 'SOLD', 3, DATE_SUB(NOW(), INTERVAL 20 DAY)),
    (18, NOW(), NOW(), 9, 21, 'VIP-3', 100000.00, 'SOLD', 4, DATE_SUB(NOW(), INTERVAL 19 DAY)),
    (19, NOW(), NOW(), 9, 22, 'A-1', 70000.00, 'SOLD', 2, DATE_SUB(NOW(), INTERVAL 18 DAY)),
    (20, NOW(), NOW(), 9, 22, 'A-2', 70000.00, 'SOLD', 3, DATE_SUB(NOW(), INTERVAL 17 DAY));

-- 테스트용 이벤트 통계 데이터
INSERT INTO event (id, created_at, updated_at, name, content, venue, event_date, sales_start_date, sales_end_date, total_seats, status, is_queue_active)
VALUES 
    (11, NOW(), NOW(), '통계 테스트 이벤트', '통계 및 리포트 테스트용 이벤트', '멀티캠퍼스 대강당', 
     DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 9 DAY), 100, 'UPCOMING', true);

-- 통계 테스트용 티켓 타입
INSERT INTO ticket_type (id, created_at, updated_at, event_id, name, price, quantity, available_quantity, content)
VALUES 
    (26, NOW(), NOW(), 11, 'Premium', 120000.00, 20, 15, '프리미엄 좌석'),
    (27, NOW(), NOW(), 11, 'Standard', 80000.00, 50, 40, '스탠다드 좌석'),
    (28, NOW(), NOW(), 11, 'Economy', 50000.00, 30, 25, '이코노미 좌석');

-- 통계 테스트용 티켓
INSERT INTO ticket (id, created_at, updated_at, event_id, ticket_type_id, seat_number, price, status, reserved_by_member_id, reserved_at)
VALUES 
    (21, NOW(), NOW(), 11, 26, 'P-1', 120000.00, 'AVAILABLE', NULL, NULL),
    (22, NOW(), NOW(), 11, 26, 'P-2', 120000.00, 'AVAILABLE', NULL, NULL),
    (23, NOW(), NOW(), 11, 26, 'P-3', 120000.00, 'RESERVED', 2, NOW()),
    (24, NOW(), NOW(), 11, 26, 'P-4', 120000.00, 'SOLD', 3, DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (25, NOW(), NOW(), 11, 26, 'P-5', 120000.00, 'SOLD', 4, DATE_SUB(NOW(), INTERVAL 3 DAY)),
    
    (26, NOW(), NOW(), 11, 27, 'S-1', 80000.00, 'AVAILABLE', NULL, NULL),
    (27, NOW(), NOW(), 11, 27, 'S-2', 80000.00, 'AVAILABLE', NULL, NULL),
    (28, NOW(), NOW(), 11, 27, 'S-3', 80000.00, 'RESERVED', 3, NOW()),
    (29, NOW(), NOW(), 11, 27, 'S-4', 80000.00, 'RESERVED', 4, NOW()),
    (30, NOW(), NOW(), 11, 27, 'S-5', 80000.00, 'SOLD', 2, DATE_SUB(NOW(), INTERVAL 1 DAY)),
    
    (31, NOW(), NOW(), 11, 28, 'E-1', 50000.00, 'AVAILABLE', NULL, NULL),
    (32, NOW(), NOW(), 11, 28, 'E-2', 50000.00, 'AVAILABLE', NULL, NULL),
    (33, NOW(), NOW(), 11, 28, 'E-3', 50000.00, 'RESERVED', 2, NOW()),
    (34, NOW(), NOW(), 11, 28, 'E-4', 50000.00, 'CANCELLED', NULL, NULL),
    (35, NOW(), NOW(), 11, 28, 'E-5', 50000.00, 'CANCELLED', NULL, NULL); 