-- 더 많은 이벤트 테스트 데이터
INSERT INTO event (id, created_at, updated_at, name, content, venue, event_date, sales_start_date, sales_end_date, total_seats, status, is_queue_active)
VALUES 
    (5, NOW(), NOW(), '2024 댄스 페스티벌', '현대 무용과 스트릿 댄스의 화려한 만남', '서울 올림픽 체조경기장', 
     '2024-09-20 17:00:00', '2024-07-01 10:00:00', '2024-09-19 23:59:59', 3000, 'UPCOMING', true),
    
    (6, NOW(), NOW(), '스탠드업 코미디 쇼', '국내 최고 코미디언들의 웃음 퍼레이드', '잠실 롯데월드타워홀', 
     '2024-06-25 19:00:00', '2024-05-25 09:00:00', '2024-06-24 23:59:59', 500, 'UPCOMING', true),
    
    (7, NOW(), NOW(), '인디 영화제', '독립영화 축제, 신진 감독들의 작품 상영', '서울아트시네마', 
     '2024-10-10 10:00:00', '2024-08-10 10:00:00', '2024-10-09 23:59:59', 300, 'UPCOMING', false),
    
    (8, NOW(), NOW(), '대학가요제', '미래의 스타를 찾아라! 대학생 음악 경연', '고려대학교 화정체육관', 
     '2024-05-20 18:30:00', '2024-04-20 10:00:00', '2024-05-19 23:59:59', 1000, 'CANCELLED', false);

-- 티켓 타입 추가 데이터
INSERT INTO ticket_type (id, created_at, updated_at, event_id, name, price, quantity, available_quantity, content)
VALUES 
    (12, NOW(), NOW(), 5, 'VVIP', 180000.00, 200, 200, '최전방 구역 + 미팅 기회'),
    (13, NOW(), NOW(), 5, 'VIP', 130000.00, 800, 800, '지정석 VIP 구역'),
    (14, NOW(), NOW(), 5, '일반석', 90000.00, 2000, 2000, '일반 지정석'),
    
    (15, NOW(), NOW(), 6, 'VIP', 80000.00, 100, 100, '최전방 지정석 + 기념품'),
    (16, NOW(), NOW(), 6, '일반석', 50000.00, 400, 400, '일반 지정석'),
    
    (17, NOW(), NOW(), 7, '전일권', 100000.00, 100, 100, '모든 영화 관람 가능'),
    (18, NOW(), NOW(), 7, '1일권', 40000.00, 200, 200, '하루 관람권'),
    
    (19, NOW(), NOW(), 8, 'R석', 50000.00, 300, 0, '중앙 지정석'),
    (20, NOW(), NOW(), 8, 'S석', 30000.00, 700, 0, '일반 지정석');

-- 이벤트별 대기열 설정 추가
INSERT INTO queue_entry (id, created_at, updated_at, event_id, member_id, queue_position, status, notified_at, entered_at, expires_at)
VALUES 
    (5, NOW(), NOW(), 5, 2, 1, 'WAITING', NULL, NOW(), DATE_ADD(NOW(), INTERVAL 30 MINUTE)),
    (6, NOW(), NOW(), 5, 3, 2, 'WAITING', NULL, NOW(), DATE_ADD(NOW(), INTERVAL 30 MINUTE)),
    (7, NOW(), NOW(), 5, 4, 3, 'WAITING', NULL, NOW(), DATE_ADD(NOW(), INTERVAL 30 MINUTE)),
    (8, NOW(), NOW(), 6, 3, 1, 'ADMITTED', NOW(), DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 30 MINUTE)); 