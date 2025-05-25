-- 회원 데이터
INSERT INTO member (email, phone, password, name, gender, birth, region, member_status, notification, total_dice, ci, created_at, last_modified_at)
VALUES 
('admin@example.com', '01012345678', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '관리자', 'MALE', '19900101', '서울', 'MEMBER_ACTIVE', true, 1000, 'CI123456', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user1@example.com', '01023456789', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '일반회원1', 'MALE', '19910101', '부산', 'MEMBER_ACTIVE', true, 500, 'CI234567', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user2@example.com', '01034567890', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '일반회원2', 'FEMALE', '19920101', '인천', 'MEMBER_ACTIVE', true, 300, 'CI345678', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 회원 권한 데이터
INSERT INTO member_roles (member_member_id, roles)
VALUES 
(1, 'ROLE_ADMIN'),
(2, 'ROLE_USER'),
(2, 'ROLE_USER'),
(3, 'ROLE_USER');

-- 테마 데이터
INSERT INTO theme (name, description, image, theme_status, created_at, last_modified_at)
VALUES 
('일상 대화', '일상적인 대화를 나누는 공간', 'theme1.jpg', 'THEME_ON', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('취미 공유', '취미를 공유하는 공간', 'theme2.jpg', 'THEME_ON', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('음악 이야기', '음악에 대해 이야기하는 공간', 'theme3.jpg', 'THEME_ON', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 채팅방 데이터
INSERT INTO chat_room (room_type, notice, room_status, theme_id, created_at, last_modified_at)
VALUES 
('GROUP', '환영합니다!', 'ROOM_ACTIVE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('GROUP', '취미를 공유해보세요', 'ROOM_ACTIVE', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('GROUP', '음악 이야기 나눠요', 'ROOM_ACTIVE', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 채팅방 참여자 데이터
INSERT INTO chat_part (nickname, profile, exit_status, member_id, chat_room_id, created_at, last_modified_at)
VALUES 
('관리자', 'profile1.jpg', 'MEMBER_ENTER', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('일반회원1', 'profile2.jpg', 'MEMBER_ENTER', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('일반회원2', 'profile3.jpg', 'MEMBER_ENTER', 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 채팅 데이터
INSERT INTO chat (message, nickname, member_id, chat_room_id, created_at, last_modified_at)
VALUES 
('안녕하세요!', '관리자', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('반갑습니다!', '일반회원1', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('환영합니다!', '일반회원2', 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 공지사항 데이터
INSERT INTO notice (notice_type, title, content, start_date, end_date, notice_status, notice_importance, created_at, last_modified_at)
VALUES 
('NOTICE', '서비스 이용 안내', '서비스 이용 방법을 안내드립니다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30' DAY, 'ONGOING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('EVENT', '신규 가입 이벤트', '신규 가입 시 다이스 100개 증정!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '7' DAY, 'ONGOING', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 문의사항 데이터
INSERT INTO question (title, content, question_status, member_id, created_at, last_modified_at)
VALUES 
('서비스 이용 문의', '서비스 이용 방법을 알고 싶습니다.', 'QUESTION_REGISTERED', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('결제 관련 문의', '결제가 되지 않습니다.', 'QUESTION_REGISTERED', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 답변 데이터
INSERT INTO answer (content, question_id, member_id, created_at, last_modified_at)
VALUES 
('서비스 이용 방법은 다음과 같습니다...', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('결제 관련 문제는 다음 단계로 해결할 수 있습니다...', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 신고 데이터
INSERT INTO report (reason, reporter_id, reported_member_id, report_status, created_at, last_modified_at)
VALUES 
('부적절한 채팅 내용', 2, 3, 'REPORT_RECEIVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('비매너 행위', 3, 2, 'REPORT_RECEIVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 채팅 신고 데이터
INSERT INTO chat_report (report_id, chat_id, created_at, last_modified_at)
VALUES 
(1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 상품 데이터
INSERT INTO product (product_name, product_image, price, quantity, created_at, last_modified_at)
VALUES 
('다이스 100개', 'dice100.jpg', 10000, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('다이스 500개', 'dice500.jpg', 45000, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('다이스 1000개', 'dice1000.jpg', 80000, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 아이템 데이터
INSERT INTO item (item_name, item_image, description, dice_price, created_at, last_modified_at)
VALUES 
('프로필 프레임', 'frame1.jpg', '특별한 프로필 프레임', 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('이모티콘 세트', 'emoticon1.jpg', '귀여운 이모티콘 세트', 200, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 다이스 로그 데이터
INSERT INTO dice_log (quantity, log_type, info, member_id, product_id, created_at, last_modified_at)
VALUES 
(100, 'DICE_CHARGE', '다이스 100개 충전', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(500, 'DICE_CHARGE', '다이스 500개 충전', 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 알림 데이터
INSERT INTO notification (is_read, content, member_id, type, created_at, last_modified_at)
VALUES 
(false, '새로운 공지사항이 등록되었습니다.', 2, 'NOTICE_EVENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(false, '다이스 충전이 완료되었습니다.', 3, 'PAYMENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 