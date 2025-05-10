-- 회원 테스트 데이터
-- 비밀번호는 모두 'password123'으로 BCrypt 암호화됨
INSERT INTO member (id, created_at, updated_at, email, password, name, phone, device_token, member_role)
VALUES 
    (1, NOW(), NOW(), 'admin@example.com', '$2a$10$dl/vW8Ez2RnofaMMKajx2.kAS49w4myIegNJ9cAC074PN41JFAChq', '관리자', '010-1234-5678', NULL, 'ADMIN'),
    (2, NOW(), NOW(), 'user1@example.com', '$2a$10$dl/vW8Ez2RnofaMMKajx2.kAS49w4myIegNJ9cAC074PN41JFAChq', '사용자1', '010-2345-6789', NULL, 'USER'),
    (3, NOW(), NOW(), 'user2@example.com', '$2a$10$dl/vW8Ez2RnofaMMKajx2.kAS49w4myIegNJ9cAC074PN41JFAChq', '사용자2', '010-3456-7890', NULL, 'USER'),
    (4, NOW(), NOW(), 'user3@example.com', '$2a$10$dl/vW8Ez2RnofaMMKajx2.kAS49w4myIegNJ9cAC074PN41JFAChq', '사용자3', '010-4567-8901', NULL, 'USER');

-- 이벤트 테스트 데이터
INSERT INTO event (id, created_at, updated_at, name, content, venue, event_date, sales_start_date, sales_end_date, total_seats, status, is_queue_active)
VALUES 
    (1, NOW(), NOW(), '2024 여름 페스티벌', '2024년 최고의 여름 페스티벌! 국내 최정상 아티스트들이 총출동합니다.', '올림픽 공원', 
     '2024-07-15 18:00:00', '2024-05-15 10:00:00', '2024-07-14 23:59:59', 5000, 'UPCOMING', true),
    
    (2, NOW(), NOW(), '클래식 콘서트', '베토벤과 모차르트의 명곡들을 한자리에서 감상하세요.', '예술의전당', 
     '2024-06-10 19:30:00', '2024-05-01 09:00:00', '2024-06-09 23:59:59', 1000, 'UPCOMING', true),
    
    (3, NOW(), NOW(), '뮤지컬 라이온킹', '세계적인 명작 뮤지컬 라이온킹이 한국에 상륙합니다.', '샤롯데 씨어터', 
     '2024-08-20 14:00:00', '2024-06-01 10:00:00', '2024-08-19 23:59:59', 800, 'UPCOMING', false),
    
    (4, NOW(), NOW(), '재즈 나이트', '감미로운 재즈의 밤으로 여러분을 초대합니다.', '블루노트 서울', 
     '2024-05-30 20:00:00', '2024-04-30 10:00:00', '2024-05-29 23:59:59', 200, 'SOLD_OUT', false);

-- 티켓 타입 테스트 데이터
INSERT INTO ticket_type (id, created_at, updated_at, event_id, name, price, quantity, available_quantity, content)
VALUES 
    (1, NOW(), NOW(), 1, 'VIP', 150000.00, 500, 480, 'VIP 전용 라운지 이용 가능, 기념품 증정'),
    (2, NOW(), NOW(), 1, '일반석', 80000.00, 3000, 2500, '일반 입장권'),
    (3, NOW(), NOW(), 1, '스탠딩', 100000.00, 1500, 1200, '스탠딩 구역 입장권'),
    
    (4, NOW(), NOW(), 2, 'R석', 120000.00, 300, 250, '최상위 시야 좌석'),
    (5, NOW(), NOW(), 2, 'S석', 90000.00, 500, 430, '우수한 시야 좌석'),
    (6, NOW(), NOW(), 2, 'A석', 70000.00, 200, 190, '일반 좌석'),
    
    (7, NOW(), NOW(), 3, 'VIP석', 180000.00, 200, 180, '최고급 시야 및 서비스 제공'),
    (8, NOW(), NOW(), 3, 'R석', 140000.00, 300, 280, '우수한 시야 좌석'),
    (9, NOW(), NOW(), 3, 'S석', 100000.00, 300, 290, '일반 좌석'),
    
    (10, NOW(), NOW(), 4, 'Table Seat', 100000.00, 120, 0, '테이블석 (2인 기준)'),
    (11, NOW(), NOW(), 4, 'Bar Seat', 70000.00, 80, 0, '바 좌석');

-- 티켓 테스트 데이터 (일부만 생성)
-- 첫 번째 이벤트(여름 페스티벌)의 VIP 티켓 10장
INSERT INTO ticket (id, created_at, updated_at, event_id, ticket_type_id, seat_number, price, status, reserved_by_member_id, reserved_at)
VALUES 
    (1, NOW(), NOW(), 1, 1, 'VIP-A1', 150000.00, 'AVAILABLE', NULL, NULL),
    (2, NOW(), NOW(), 1, 1, 'VIP-A2', 150000.00, 'AVAILABLE', NULL, NULL),
    (3, NOW(), NOW(), 1, 1, 'VIP-A3', 150000.00, 'RESERVED', 2, NOW()),
    (4, NOW(), NOW(), 1, 1, 'VIP-A4', 150000.00, 'SOLD', 3, DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (5, NOW(), NOW(), 1, 1, 'VIP-A5', 150000.00, 'AVAILABLE', NULL, NULL),
    (6, NOW(), NOW(), 1, 1, 'VIP-B1', 150000.00, 'AVAILABLE', NULL, NULL),
    (7, NOW(), NOW(), 1, 1, 'VIP-B2', 150000.00, 'AVAILABLE', NULL, NULL),
    (8, NOW(), NOW(), 1, 1, 'VIP-B3', 150000.00, 'AVAILABLE', NULL, NULL),
    (9, NOW(), NOW(), 1, 1, 'VIP-B4', 150000.00, 'RESERVED', 4, NOW()),
    (10, NOW(), NOW(), 1, 1, 'VIP-B5', 150000.00, 'SOLD', 2, DATE_SUB(NOW(), INTERVAL 3 DAY));

-- 두 번째 이벤트(클래식 콘서트)의 R석 티켓 5장
INSERT INTO ticket (id, created_at, updated_at, event_id, ticket_type_id, seat_number, price, status, reserved_by_member_id, reserved_at)
VALUES 
    (11, NOW(), NOW(), 2, 4, 'R-101', 120000.00, 'AVAILABLE', NULL, NULL),
    (12, NOW(), NOW(), 2, 4, 'R-102', 120000.00, 'RESERVED', 3, NOW()),
    (13, NOW(), NOW(), 2, 4, 'R-103', 120000.00, 'AVAILABLE', NULL, NULL),
    (14, NOW(), NOW(), 2, 4, 'R-104', 120000.00, 'SOLD', 2, DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (15, NOW(), NOW(), 2, 4, 'R-105', 120000.00, 'AVAILABLE', NULL, NULL);

-- 예약 데이터
INSERT INTO reservation (id, created_at, updated_at, member_id, event_id, ticket_id, total_amount, status, payment_id, confirmed_at, cancelled_at, cancel_reason)
VALUES 
    (1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 2, 1, 10, 150000.00, 'CONFIRMED', 'pay_001', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, NULL),
    (2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 3, 1, 4, 150000.00, 'CONFIRMED', 'pay_002', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, NULL),
    (3, NOW(), NOW(), 2, 1, 3, 150000.00, 'PENDING', NULL, NULL, NULL, NULL),
    (4, NOW(), NOW(), 4, 1, 9, 150000.00, 'PENDING', NULL, NULL, NULL, NULL),
    (5, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 2, 2, 14, 120000.00, 'CONFIRMED', 'pay_003', DATE_SUB(NOW(), INTERVAL 5 DAY), NULL, NULL),
    (6, NOW(), NOW(), 3, 2, 12, 120000.00, 'PENDING', NULL, NULL, NULL, NULL);

-- 결제 데이터
INSERT INTO payment (id, created_at, updated_at, member_id, reservation_id, amount, status, payment_method, transaction_id, paid_at, refunded_at, refund_reason)
VALUES 
    (1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 2, 1, 150000.00, 'COMPLETED', 'CREDIT_CARD', 'tx_001', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, NULL),
    (2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 3, 2, 150000.00, 'COMPLETED', 'BANK_TRANSFER', 'tx_002', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, NULL),
    (3, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 2, 5, 120000.00, 'COMPLETED', 'CREDIT_CARD', 'tx_003', DATE_SUB(NOW(), INTERVAL 5 DAY), NULL, NULL);

-- 대기열 항목 데이터
INSERT INTO queue_entry (id, created_at, updated_at, event_id, member_id, queue_position, status, notified_at, entered_at, expires_at)
VALUES 
    (1, NOW(), NOW(), 1, 3, 1, 'WAITING', NULL, NOW(), DATE_ADD(NOW(), INTERVAL 30 MINUTE)),
    (2, NOW(), NOW(), 1, 4, 2, 'WAITING', NULL, NOW(), DATE_ADD(NOW(), INTERVAL 30 MINUTE)),
    (3, NOW(), NOW(), 1, 2, 3, 'EXPIRED', NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
    (4, NOW(), NOW(), 2, 2, 1, 'ADMITTED', NOW(), DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 30 MINUTE));

-- 알림 데이터
INSERT INTO notification (id, created_at, updated_at, member_id, type, title, content, link, status, sent_at, read_at, error_message)
VALUES 
    (1, NOW(), NOW(), 2, 'QUEUE_READY', '대기열 입장 안내', '대기열 입장이 가능합니다. 30분 이내에 입장해주세요.', '/queue/1', 'SENT', NOW(), NULL, NULL),
    (2, NOW(), NOW(), 3, 'TICKET_RESERVED', '티켓 예약 완료', 'VIP-A3 좌석이 예약되었습니다. 24시간 이내에 결제를 완료해주세요.', '/reservations/3', 'SENT', NOW(), NULL, NULL),
    (3, NOW(), NOW(), 4, 'TICKET_RESERVED', '티켓 예약 완료', 'VIP-B4 좌석이 예약되었습니다. 24시간 이내에 결제를 완료해주세요.', '/reservations/4', 'SENT', NOW(), NULL, NULL),
    (4, NOW(), NOW(), 2, 'PAYMENT_COMPLETED', '결제 완료', '결제가 성공적으로 완료되었습니다.', '/tickets/10', 'SENT', NOW(), NOW(), NULL),
    (5, NOW(), NOW(), 3, 'PAYMENT_COMPLETED', '결제 완료', '결제가 성공적으로 완료되었습니다.', '/tickets/4', 'SENT', NOW(), NOW(), NULL),
    (6, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 2, 'EVENT_REMINDER', '공연 D-2 알림', '예매하신 공연이 이틀 후에 시작됩니다.', '/events/2', 'SENT', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), NULL); 