-- 1. Theme data
INSERT INTO theme (name, description, image, theme_status, created_at)
VALUES ('Dice Friends', '여행에 관한 이야기를 나누는 테마입니다.', 'travel.png', 'THEME_ON', NOW()),
       ('Heart Signal', '맛있는 음식에 관한 이야기를 나누는 테마입니다.', 'food.png', 'THEME_ON', NOW()),
       ('I`m SOLO', '좋아하는 영화에 관한 이야기를 나누는 테마입니다.', 'movie.png', 'THEME_ON', NOW()),
       ('음악', '취향저격 음악에 관한 이야기를 나누는 테마입니다.', 'music.png', 'THEME_ON', NOW()),
       ('게임', '인기 게임에 관한 이야기를 나누는 테마입니다.', 'game.png', 'THEME_CLOSE', NOW());

-- 2. Member data
INSERT INTO member (email, phone, password, name, gender, birth, region, member_status, notification,
                    total_dice, ci, created_at)
VALUES ('q@q.q', '01012345678', '{bcrypt}$2a$10$4z8P8wHcqQgRlvxjNKDeCufjNuCA1GbCLF8jSBw5Mx/4sUMW04Ytq', '김철수', 'MALE', '1990-01-01', '서울',
        'MEMBER_ACTIVE', true, 100, 'ci12345678901', NOW()),
       ('user2@example.com', '01023456789', '$2a$10$abcdefghijklmnopqrstuv', '이영희', 'FEMALE', '1992-05-12', '부산',
        'MEMBER_ACTIVE', true, 150, 'ci23456789012', NOW()),
       ('user3@example.com', '01034567890', '$2a$10$abcdefghijklmnopqrstuv', '박민수', 'MALE', '1988-11-30', '대구',
        'MEMBER_ACTIVE', false, 80, 'ci34567890123', NOW()),
       ('user4@example.com', '01045678901', '$2a$10$abcdefghijklmnopqrstuv', '정다혜', 'FEMALE', '1995-08-22', '인천',
        'MEMBER_SLEEP', true, 200, 'ci45678901234', NOW()),
       ('user5@example.com', '01056789012', '$2a$10$abcdefghijklmnopqrstuv', '최지훈', 'MALE', '1985-03-15', '광주',
        'MEMBER_ACTIVE', true, 120, 'ci56789012345', NOW()),
        ('dice@gmail.com', '010-2401-5119', '{bcrypt}$2a$10$GIuq/QnyYvUXOpHSKcz5k.0cfY4cevVtAjIAwfCunNJGNQddbOfFC', '하나', 'MALE', '1998-12-25', '인천시 연수구',
        'MEMBER_ACTIVE', true, 2000, 'abcdefghijklmnopdice@gmail.com010-2401-5119', NOW()),
        ('dice2@gmail.com', '010-2402-5119', '{bcrypt}$2a$10$/vTg2S4HY1ZvnN1R3lcROuVhz7stWiA98rHH6Lvz/UKpk1nleREJK', '두리', 'MALE', '1998-12-26', '서울시 강남구',
        'MEMBER_ACTIVE', true, 2000, 'abcdefghijklmnopdice@gmail.com010-2402-5119', NOW()),
        ('dice3@gmail.com', '010-2403-5119', '{bcrypt}$2a$10$1hgcPcwsHHSopBqeTsY0XedzTKAHX/Y3G6LuR4vNUymscBRE5j0PW', '세찌', 'MALE', '1998-12-27', '인천시 미추홀구',
         'MEMBER_ACTIVE', true, 2000, 'abcdefghijklmnopdice@gmail.com010-2403-5119', NOW()),
        ('dice4@gmail.com', '010-2404-5119', '{bcrypt}$2a$10$Pg9D4MO/D3gvTKJ3VL7b6uJbAjG5vGDbtyyAbQuv2YmVIltJYZmKq', '네찌', 'MALE', '1998-12-28', '인천시 중구',
        'MEMBER_ACTIVE', true, 2000, 'abcdefghijklmnopdice@gmail.com010-2404-5119', NOW()),
        ('dice5@gmail.com', '010-2405-5119', '{bcrypt}$2a$10$SE5k.fmAwIlAvQ/51CMZn.WOCY8hfzpblGebKRQ1SznlSMChCA8iq', '다오', 'MALE', '1998-12-29', '인천시 서구',
         'MEMBER_ACTIVE', true, 2000, 'abcdefghijklmnopdice@gmail.com010-2405-5119', NOW()),
         ('dice6@gmail.com', '010-2406-5119', '{bcrypt}$2a$10$eD4mfXQd77InF0PTypOn5.z0aiKaz/J4ggnUUdHaZvdpAzO0YGchy', '육댕', 'MALE', '1998-12-20', '인천시 남동구',
         'MEMBER_ACTIVE', true, 2000, 'abcdefghijklmnopdice@gmail.com010-2406-5119', NOW());

-- Add member roles
INSERT INTO member_roles (member_member_id, roles)
VALUES (1, 'ROLE_USER'),
       (2, 'ROLE_USER'),
       (3, 'ROLE_USER'),
       (4, 'ROLE_USER'),
       (5, 'ROLE_USER'),
       (1, 'ROLE_ADMIN');

-- 3. Item data
INSERT INTO item (item_name, description, dice_price, created_at)
VALUES ('프로필 배경화면 1', '봄 테마 배경화면입니다.', 50, NOW()),
       ('프로필 배경화면 2', '여름 테마 배경화면입니다.', 50, NOW()),
       ('프로필 프레임 1', '골드 프레임입니다.', 100, NOW()),
       ('프로필 프레임 2', '실버 프레임입니다.', 80, NOW()),
       ('이모티콘 세트 1', '동물 테마 이모티콘 세트입니다.', 150, NOW());

-- 4. Product data
INSERT INTO product (product_name, price, quantity, created_at)
VALUES ('다이스 100개', 1000, 100, NOW()),
       ('다이스 300개', 2500, 300, NOW()),
       ('다이스 500개', 4000, 500, NOW()),
       ('다이스 1000개', 7000, 1000, NOW()),
       ('다이스 3000개', 20000, 3000, NOW());

-- 5. Event data
INSERT INTO event (event_name, event_status, theme_id, created_at)
VALUES ('여름 맞이 여행 이벤트', 'EVENT_OPEN', 1, NOW()),
       ('맛집 탐방 이벤트', 'EVENT_OPEN', 2, NOW()),
       ('영화 리뷰 이벤트', 'EVENT_OPEN', 3, NOW()),
       ('음악 추천 이벤트', 'EVENT_CLOSE', 4, NOW()),
       ('게임 친구 찾기 이벤트', 'EVENT_OPEN', 5, NOW());

-- 6. Question data
INSERT INTO question (title, content, question_status, question_image, member_id, created_at)
VALUES ('다이스 충전이 안돼요', '결제했는데 다이스가 충전되지 않았어요. 확인 부탁드립니다.', 'QUESTION_REGISTERED', null, 6, NOW()),
       ('프로필 변경 문의', '프로필 사진이 변경되지 않아요. 어떻게 해야 하나요?', 'QUESTION_ANSWERED', null, 6, NOW()),
       ('친구 추가 기능', '친구 추가는 어떻게 하나요?', 'QUESTION_REGISTERED', 'question_img3.jpg', 6, NOW()),
       ('계정 삭제 문의', '계정을 삭제하고 싶습니다. 절차를 알려주세요.', 'QUESTION_ANSWERED', null, 6, NOW()),
       ('채팅방 초대 오', '채팅방에 친구를 초대할 수 없어요.', 'QUESTION_REGISTERED', 'question_img5.jpg', 6, NOW()),
       ('채팅방 초대 ', '채팅방에 친구를 초대할 수 없어요.', 'QUESTION_REGISTERED', 'question_img5.jpg', 6, NOW()),
       ('채팅방 초', '채팅방에 친구를 초대할 수 없어요.', 'QUESTION_REGISTERED', 'question_img5.jpg', 6, NOW()),
       ('채팅방 ', '채팅방에 친구를 초대할 수 없어요.', 'QUESTION_REGISTERED', 'question_img5.jpg', 6, NOW()),
       ('채팅', '채팅방에 친구를 초대할 수 없어요.', 'QUESTION_REGISTERED', 'question_img5.jpg', 6, NOW());

-- 7. Answer data
INSERT INTO answer (content, answer_image, question_id, member_id, created_at)
VALUES ('안녕하세요. 결제 내역을 확인해 보겠습니다. 잠시만 기다려주세요.', null, 1, 5, NOW()),
       ('프로필 설정에서 이미지 크기가 너무 크면 업로드가 안될 수 있습니다. 5MB 이하로 줄여서 시도해보세요.', 'answer_img2.jpg', 2, 1, NOW()),
       ('친구 추가는 현재 베타 테스트 중입니다. 다음 업데이트에서 제공될 예정입니다.', null, 3, 2, NOW()),
       ('설정 > 계정 > 계정 삭제 메뉴에서 진행하실 수 있습니다. 탈퇴 시 모든 데이터가 삭제됩니다.', null, 4, 3, NOW()),
       ('채팅방 초대 기능에 일시적인 오류가 있었습니다. 현재 수정 중이니 조금만 기다려주세요.', 'answer_img5.jpg', 5, 4, NOW());

-- 8. Chat Room data
INSERT INTO chat_room (room_type, notice, room_status, theme_id, created_at)
VALUES ('GROUP', '여행 정보를 공유하는 채팅방입니다.', 'ROOM_ACTIVE', 1, NOW()),
       ('GROUP', '맛집 추천 채팅방입니다.', 'ROOM_ACTIVE', 2, NOW()),
       ('COUPLE', '영화 토론방입니다.', 'ROOM_DEACTIVE', 3, NOW()),
       ('GROUP', '음악 공유 채팅방입니다.', 'ROOM_DEACTIVE', 4, NOW()),
       ('COUPLE', '게임 팀원 모집 채팅방입니다.', 'ROOM_DEACTIVE', 5, NOW());

-- 9. Chat Part data
INSERT INTO chat_part (nickname, profile, exit_status, member_id, chat_room_id, created_at)
VALUES ('여행러버', 'profile1.jpg', 'MEMBER_ENTER', 1, 1, NOW()),
       ('맛집헌터', 'profile2.jpg', 'MEMBER_ENTER', 2, 2, NOW()),
       ('영화광', 'profile3.jpg', 'MEMBER_ENTER', 3, 3, NOW()),
       ('음악천재', 'profile4.jpg', 'MEMBER_EXIT', 4, 4, NOW()),
       ('게임마스터', 'profile5.jpg', 'MEMBER_ENTER', 5, 5, NOW()),
       ('등산가', 'profile6.jpg', 'MEMBER_ENTER', 2, 1, NOW()),
       ('요리사', 'profile7.jpg', 'MEMBER_ENTER', 3, 2, NOW()),
       ('평론가', 'profile8.jpg', 'MEMBER_ENTER', 1, 3, NOW()),
       ('작곡가', 'profile9.jpg', 'MEMBER_EXIT', 5, 4, NOW()),
       ('게이머', 'profile10.jpg', 'MEMBER_ENTER', 4, 5, NOW());

-- 10. Chat data
INSERT INTO chat (message, member_id, chat_room_id, created_at, nickname)
VALUES ('여행 계획 세우신 분 있나요?', 1, 1, NOW(), '하나뿐인 하나'),
       ('제주도 맛집 추천해주세요!', 2, 2, NOW(), '두 얼굴의 매력 두리'),
       ('어제 본 영화 정말 재미있었어요.', 3, 3, NOW(), '새침한 세찌'),
       ('이 노래 들어보셨나요? 정말 좋아요.', 4, 4, NOW(), '네모지만 부드러운 네몽'),
       ('같이 게임할 사람 구해요!', 5, 5, NOW(), '단호하지만 다정한 다오'),
       ('저는 다음 주에 부산 여행 갑니다!', 2, 1, NOW(), '하나뿐인 하나'),
       ('제주 흑돼지는 꼭 드셔보세요!', 3, 2, NOW(), '육감적인 직감파 육댕'),
       ('그 영화 저도 봤어요. 결말이 충격적이었죠!', 1, 3, NOW(), '새침한 세찌'),
       ('저도 그 노래 좋아해요. 같은 가수의 다른 노래도 추천합니다.', 5, 4, NOW(), '단호하지만 다정한 다오'),
       ( '저랑 같이 해요! 지금 접속 중이에요.', 4, 5, NOW(), '두 얼굴의 매력 다오');

-- 11. Room Event data
INSERT INTO room_event (receiver_id, sender_id, event_id, chat_room_id, room_event_type, message, created_at)
VALUES (1, 2, 1, 1, 'PICK', '널 좋아해', NOW()),
       (2, 1, 1, 1, 'PICK', 'Really', NOW()),
       (1, 3, 1, 1, 'PICK', 'Really Really', NOW()),
       (3, 2, 2, 1, 'PICK', '내 맘을 알아줘', NOW()),
       (1, 3, 2, 1, 'PICK', 'Really Really Really', NOW());

-- 13. Notice data
INSERT INTO notice (notice_type, title, content, image, start_date, end_date, notice_status,
                    notice_importance, created_at)
VALUES ('NOTICE', '서비스 점검 안내', '6월 15일 오전 2시부터 4시까지 서버 점검이 있을 예정입니다.', null, '2023-06-14 00:00:00',
        '2023-06-16 00:00:00', 'ONGOING', 3, NOW()),
       ( 'EVENT', '여름 맞이 이벤트', '여름 맞이 특별 이벤트를 진행합니다. 참여하시고 특별 아이템을 받아가세요!', 'summer_event.jpg', '2023-06-01 00:00:00',
        '2023-06-30 00:00:00', 'ONGOING', 2, NOW()),
       ('NOTICE', '새로운 기능 안내', '프로필 커스터마이징 기능이 추가되었습니다.', 'new_feature.jpg', '2023-05-20 00:00:00',
        '2023-06-20 00:00:00', 'ONGOING', 1, NOW()),
       ( 'EVENT', '친구 초대 이벤트', '친구를 초대하고 다이스를 받아가세요!', 'invite_event.jpg', '2023-07-01 00:00:00',
        '2023-07-31 00:00:00', 'SCHEDULED', 2, NOW()),
       ('NOTICE', '개인정보 처리방침 변경 안내', '개인정보 처리방침이 변경되었습니다. 확인해주세요.', null, '2023-05-01 00:00:00',
        '2023-05-31 00:00:00', 'CLOSED', 3, NOW());

-- 14. Report data
INSERT INTO report (reason, reporter_id, report_status, created_at)
VALUES ('불쾌한 언어 사용', 1, 'REPORT_RECEIVED', NOW()),
       ('스팸 메시지', 2, 'REPORT_UNDER_REVIEW', NOW()),
       ('부적절한 내용', 3, 'REPORT_COMPLETED', NOW()),
       ('괴롭힘', 4, 'REPORT_RECEIVED', NOW()),
       ('개인정보 유출', 5, 'REPORT_DELETED', NOW());

-- 15. Chat Report data
INSERT INTO chat_report (chat_id, created_at)
VALUES (1, NOW()),
       (2, NOW()),
       (3, NOW()),
       (4, NOW()),
       (5, NOW());

-- 16. Dice Log data
INSERT INTO dice_log (quantity, log_type, info, member_id, product_id, item_id, created_at)
VALUES (100, 'DICE_CHARGE', '다이스 100개 구매', 1, 1, null, NOW()),
       (300, 'DICE_CHARGE', '다이스 300개 구매', 2, 2, null, NOW()),
       (50, 'DICE_USED', '프로필 배경화면 구매', 3, null, 1, NOW()),
       (100, 'DICE_USED', '골드 프레임 구매', 4, null, 3, NOW()),
       (500, 'DICE_CHARGE', '다이스 500개 구매', 5, 3, null, NOW());

