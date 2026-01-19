CREATE TABLE `blocks` (
                          `block_id`	BIGINT	NOT NULL	COMMENT 'PK',
                          `x`	INT	NOT NULL	COMMENT '픽셀 X 좌표, UNIQUE(x, y)',
                          `y`	INT	NOT NULL	COMMENT '픽셀 Y 좌표, UNIQUE(x, y)',
                          `created_at`	DATETIME	NOT NULL	COMMENT '타일 생성 시각'
);

CREATE TABLE `user_achievements` (
                                     `user_achievement_id`	BIGINT	NOT NULL	COMMENT '유저 업적 ID',
                                     `achieved_at`	DATETIME	NOT NULL	COMMENT '업적 획득 시각',
                                     `user_id`	BIGINT	NOT NULL,
                                     `achievement_id`	BIGINT	NULL
);

CREATE TABLE `mission_records` (
                                   `mission_record_id`	BIGINT	NOT NULL,
                                   `status`	ENUM('SUCCESS','FAIL')	NOT NULL	COMMENT '실패/성공 상태',
                                   `submitted_at`	DATETIME	NOT NULL,
                                   `message`	TEXT	NULL	COMMENT '실패 및 성공 이유',
                                   `mission_video_url`	VARCHAR(255)	NULL	COMMENT '미션 동영상 경로 저장',
                                   `mission_id`	BIGINT	NOT NULL,
                                   `walk_id`	BIGINT	NOT NULL
);

CREATE TABLE `walk_points` (
                               `walk_point_id`	BIGINT	NOT NULL	COMMENT 'PK',
                               `latitude`	DOUBLE	NOT NULL	COMMENT '위도',
                               `longitude`	DOUBLE	NOT NULL	COMMENT '경도',
                               `recorded_at`	DATETIME	NOT NULL	COMMENT '좌표 기록 시각 (클라이언트 기준)',
                               `created_at`	DATETIME	NOT NULL	COMMENT '서버 수신 시각',
                               `walk_id`	BIGINT	NOT NULL
);

CREATE TABLE `dog_rank` (
                            `dog_rank_id`	BIGINT	NOT NULL	COMMENT '개인 거리 랭킹 ID',
                            `period_type`	ENUM('WEEK', 'MONTH', 'YEAR')	NOT NULL	COMMENT '랭킹 기간 타입',
                            `period_value`	VARCHAR(10)	NOT NULL	COMMENT '기간 값 (MONTH: YYYY-MM, YEAR: YYYY)',
                            `total_distance`	DOUBLE	NOT NULL	COMMENT '기간 내 총 이동 거리(m)',
                            `ranking`	INT	NOT NULL	COMMENT '해당 기간 개인 랭킹 순위',
                            `created_at`	DATETIME	NOT NULL	COMMENT '랭킹 집계 생성 시각',
                            `dog_id`	BIGINT	NOT NULL,
                            `region_id`	BIGINT	NOT NULL
);

CREATE TABLE `analysis` (
                            `analysis_id`	BIGINT	NULL	COMMENT '강아지 헬스케어 분석 ID',
                            `summary`	TEXT	NOT NULL	COMMENT 'AI 종합 피드백 (권장 행동)',
                            `risk_level`	VARCHAR(10)	NOT NULL	COMMENT '강아지 종합 위험도',
                            `video_url`	VARCHAR(512)	NOT NULL	COMMENT '분석에 사용된 영상 S3 URL',
                            `patella_risk_score`	INT	NOT NULL	COMMENT '슬개골 위험 점수((0~100)',
                            `patella_risk_desc`	TEXT	NOT NULL	COMMENT '슬개골 위험 분석 설명',
                            `gait_balance_score`	INT	NOT NULL	COMMENT '좌우 보행 균형 점수(0~100)',
                            `gait_balance_desc`	TEXT	NOT NULL	COMMENT '좌우 보행 균형 설명',
                            `knee_mobility_score`	INT	NOT NULL	COMMENT '무릎 관절 가동성 점수(0-100)',
                            `knee_mobility_desc`	TEXT	NOT NULL	COMMENT '무릎 관절 가동성 설명',
                            `gait_stability_score`	INT	NOT NULL	COMMENT '보행 안정성 점수(0-100)',
                            `gait_stability_desc`	TEXT	NOT NULL	COMMENT '보행 안정성 설명',
                            `gait_rhythm_score`	INT	NOT NULL	COMMENT '보행 리듬 점수(0-100)',
                            `gait_rhythm_desc`	TEXT	NOT NULL	COMMENT '보행 리듬 설명',
                            `created_at`	DATETIME	NOT NULL	COMMENT '분석 결과 생성 시각',
                            `dog_id`	BIGINT	NOT NULL
);

CREATE TABLE `walks` (
                         `walk_id`	BIGINT	NOT NULL,
                         `start_time`	DATETIME	NOT NULL,
                         `end_time`	DATETIME	NULL,
                         `distance`	DOUBLE	NULL	COMMENT '총 이동 거리 (m)',
                         `duration`	INT	NULL	COMMENT '산책 시간 (초)',
                         `status`	ENUM('IN_PROGRESS', 'FINISHED')	NOT NULL,
                         `created_at`	DATETIME	NOT NULL,
                         `dog_id`	BIGINT	NOT NULL
);

CREATE TABLE `dogs` (
                        `dog_id`	BIGINT	NOT NULL,
                        `name`	VARCHAR(15)	NOT NULL	COMMENT '반려견 이름',
                        `birth_date`	DATE	NULL	COMMENT '생년월일',
                        `gender`	ENUM('MALE', 'FEMALE')	NOT NULL	COMMENT '성별',
                        `is_neutered`	BOOLEAN	NOT NULL	DEFAULT FALSE	COMMENT '중성화 여부',
                        `weight`	FLOAT	NOT NULL	COMMENT '강아지 몸무게',
                        `profile_image_url`	VARCHAR(255)	NULL	COMMENT '반려견 프로필 이미지(Presigned-url key 저장)',
                        `status`	ENUM('ACTIVE', 'DELETED', "DORMANCY")	NOT NULL	COMMENT 'Soft Delete를 위한 컬럼',
                        `dog_key`	VARCHAR(255)	NOT NULL	COMMENT '강아지별 식별 키',
                        `created_at`	DATETIME	NOT NULL,
                        `updated_at`	DATETIME	NOT NULL,
                        `deleted_at`	DATETIME	NULL,
                        `user_id`	BIGINT	NOT NULL,
                        `breed_id`	BIGINT	NOT NULL
);

CREATE TABLE `missions` (
                            `mission_id`	BIGINT	NOT NULL,
                            `title`	VARCHAR(50)	NOT NULL	COMMENT '미션 제목',
                            `description`	TEXT	NOT NULL	COMMENT '미션 설명',
                            `mission_difficulty`	ENUM('EASY', 'MEDIUM', 'HARD')	NOT NULL	COMMENT '미션 난이도',
                            `reward_point`	INT	NOT NULL	COMMENT '보상 점수',
                            `created_at`	DATETIME	NOT NULL
);

CREATE TABLE `region_rank` (
                               `region_rank_id`	BIGINT	NOT NULL	COMMENT '지역 거리 랭킹 ID',
                               `period_type`	ENUM('WEEK', 'MONTH', 'YEAR')	NOT NULL	COMMENT '랭킹 기간 타입',
                               `period_value`	VARCHAR(10)	NOT NULL	COMMENT '기간 값 (MONTH: YYYY-MM, YEAR: YYYY)',
                               `total_distance`	DOUBLE	NOT NULL	COMMENT '해당 기간 지역 총 이동 거리(m)',
                               `ranking`	INT	NOT NULL	COMMENT '해당 기간 지역 랭킹 순위',
                               `created_at`	DATETIME	NOT NULL	COMMENT '랭킹 집계 생성 시각',
                               `region_id`	BIGINT	NOT NULL
);

CREATE TABLE `walk_images` (
                               `walk_image_id`	BIGINT	NOT NULL	COMMENT '산책 중 이미지 ID (PK)',
                               `image_key`	VARCHAR(255)	NOT NULL	COMMENT 'S3 presigned-url 생성용 object key',
                               `created_at`	DATETIME	NOT NULL	COMMENT '이미지 등록 시각',
                               `walk_id`	BIGINT	NOT NULL
);

CREATE TABLE `region_dog_rank` (
                                   `region_user_rank_id`	BIGINT	NOT NULL	COMMENT '지역 내 유저 기여 랭킹 ID',
                                   `period_type`	ENUM('WEEK', 'MONTH', 'YEAR')	NOT NULL	COMMENT '랭킹 기간 타입',
                                   `period_value`	VARCHAR(10)	NOT NULL	COMMENT '기간 값 (MONTH: YYYY-MM, YEAR: YYYY)',
                                   `dog_distance`	DOUBLE	NOT NULL	COMMENT '해당 기간 해당 지역에서 이동한 거리(m)',
                                   `contribution_rate`	DOUBLE	NOT NULL	COMMENT '지역 총 이동 거리 대비 기여율 (0~1)',
                                   `ranking`	INT	NOT NULL	COMMENT '해당 지역 내 유저 랭킹 순위',
                                   `created_at`	DATETIME	NOT NULL	COMMENT '랭킹 집계 생성 시각',
                                   `dog_id`	BIGINT	NOT NULL,
                                   `region_id`	BIGINT	NOT NULL
);

CREATE TABLE `breed` (
                         `breed_id`	BIGINT	NOT NULL,
                         `name`	VARCHAR(50)	NOT NULL	COMMENT '종 이름(포메라니안, 진돗개, 푸들,  말티푸, 믹스)'
);

CREATE TABLE `achievements` (
                                `achievement_id`	BIGINT	NOT NULL	COMMENT '업적 ID',
                                `code`	VARCHAR(50)	NOT NULL	COMMENT '업적 코드 (내부 식별자)',
                                `title`	VARCHAR(100)	NOT NULL	COMMENT '업적 이름',
                                `description`	VARCHAR(255)	NOT NULL	COMMENT '업적 설명',
                                `category`	VARCHAR(50)	NOT NULL	COMMENT '업적 분류 (DISTANCE, WALK, PIXEL, POINT 등)',
                                `icon_url`	VARCHAR(255)	NULL	COMMENT '업적 아이콘 URL',
                                `created_at`	DATETIME	NOT NULL	COMMENT '업적 생성 시각'
);

CREATE TABLE `block_ownership` (
                                   `block_id`	BIGINT	NOT NULL	COMMENT 'PK',
                                   `acquired_at`	DATETIME	NOT NULL	COMMENT '소유 확정 시각',
                                   `last_passed_at`	DATETIME	NOT NULL	COMMENT '마지막 통과 시각',
                                   `created_at`	DATETIME	NOT NULL	COMMENT '최초 생성 시각',
                                   `updated_at`	DATETIME	NOT NULL	COMMENT '마지막 갱신 시각',
                                   `dog_id`	BIGINT	NOT NULL
);

CREATE TABLE `expressions` (
                               `expression_id`	BIGINT	NOT NULL,
                               `predict_emotion`	ENUM("HAPPY", "SAD", "ANGTY", "RELAXED"))	NOT NULL	COMMENT '강아지 대표 감정',
	`happy`	DOUBLE	NOT NULL	DEFAULT 0	COMMENT '강아지 행복도',
	`angry`	DOUBLE	NOT NULL	DEFAULT 0	COMMENT '강아지 화난 정도',
	`sad`	DOUBLE	NOT NULL	DEFAULT 0	COMMENT '강아지 슬픔 정도',
	`relaxed`	DOUBLE	NOT NULL	DEFAULT 0	COMMENT '강아지 편안함 정도',
	`summary`	TEXT	NOT NULL	COMMENT '강아지 표정 분석 요약 메세지',
	`expressions_image_url`	VARCHAR(255)	NULL	COMMENT '강아지 표정 분석 영상 프레임 이미지(S3)',
	`created_at`	DATETIME	NOT NULL,
	`dog_id`	BIGINT	NOT NULL,
	`walk_id`	BIGINT	NOT NULL
);

CREATE TABLE `users` (
                         `user_id`	BIGINT	NOT NULL,
                         `kakao_user_id`	BIGINT	NOT NULL	COMMENT '카카오에서 내려주는 user id',
                         `kakao_account_email`	VARCHAR(20)	NULL	COMMENT '카카오 계정 이메일',
                         `status`	ENUM('ACTIVE', 'DELETED')	NOT NULL	COMMENT 'Soft Delete를 위한 컬럼',
                         `last_login_at`	DATETIME	NOT NULL,
                         `created_at`	DATETIME	NOT NULL,
                         `modified_at`	DATETIME	NULL,
                         `deleted_at`	DATETIME	NULL,
                         `region_id`	BIGINT	NOT NULL
);

CREATE TABLE `point_ledger` (
                                `point_ledger_id`	BIGINT	NOT NULL	COMMENT 'PK',
                                `point_type`	ENUM('BLOCK', 'MISSION', 'WALK', 'HEALTH', 'EXPRESSION')	NOT NULL	COMMENT '포인트 유형',
                                `point_amount`	INT	NOT NULL	COMMENT '포인트 증감 값',
                                `ref_table`	VARCHAR(50)	NOT NULL	COMMENT '근거 테이블명',
                                `ref_id`	BIGINT	NOT NULL	COMMENT '근거 엔티티 ID',
                                `occurred_at`	DATETIME	NOT NULL	COMMENT '이벤트 발생 시각',
                                `created_at`	DATETIME	NOT NULL	COMMENT '원장 기록 시각',
                                `user_id`	BIGINT	NOT NULL
);

CREATE TABLE `regions` (
                           `region_id`	BIGINT	NOT NULL,
                           `name`	VARCHAR(50)	NOT NULL	COMMENT '지역명 (예: 서울특별시, 송파구)',
                           `level`	ENUM('CITY', 'DISTRICT', 'DONG')	NOT NULL	COMMENT '지역 단계',
                           `parent_id`	BIGINT	NULL	COMMENT '상위 지역 ID (자기참조)',
                           `status`	ENUM('ACTIVE', 'INACTIVE')	NOT NULL	COMMENT '사용 여부',
                           `created_at`	DATETIME	NOT NULL,
                           `updated_at`	DATETIME	NULL
);

CREATE TABLE `recap_images` (
                                `recap_image_id`	BIGINT	NOT NULL	COMMENT '리캡 이미지 ID (PK)',
                                `image_key`	VARCHAR(255)	NOT NULL	COMMENT 'S3 presigned-url 생성용 object key',
                                `created_at`	DATETIME	NOT NULL	COMMENT '이미지 등록 시각',
                                `recap_id`	BIGINT	NOT NULL
);

CREATE TABLE `block_pass_logs` (
                                   `pixel_pass_logs_id`	BIGINT	NOT NULL	COMMENT 'PK',
                                   `passed_at`	DATETIME	NOT NULL	COMMENT '타일 통과 시각',
                                   `created_at`	DATETIME	NOT NULL	COMMENT '이벤트 기록 시각',
                                   `dog_id`	BIGINT	NOT NULL,
                                   `walk_id`	BIGINT	NOT NULL,
                                   `pixel_id`	BIGINT	NOT NULL
);

CREATE TABLE `recaps` (
                          `recap_id`	BIGINT	NOT NULL	COMMENT '리캡 ID (PK)',
                          `period_type`	ENUM('MONTH', 'YEAR')	NOT NULL	COMMENT '리캡 기간 타입',
                          `period_value`	VARCHAR(7)	NOT NULL	COMMENT '기간 값 (MONTH: YYYY-MM, YEAR: YYYY)',
                          `title`	VARCHAR(100)	NOT NULL	COMMENT 'AI 생성 리캡 제목',
                          `content`	TEXT	NOT NULL	COMMENT 'AI 생성 리캡 본문',
                          `created_at`	DATETIME	NOT NULL	COMMENT '리캡 생성 시각',
                          `dog_id`	BIGINT	NOT NULL
);

CREATE TABLE `walk_diaries` (
                                `walk_diary_id`	BIGINT	NOT NULL,
                                `memo`	TEXT	NULL	COMMENT '산책 메모',
                                `map_image_url`	VARCHAR(255)	NOT NULL	COMMENT '산책 경로 이미지',
                                `created_at`	DATETIME	NOT NULL,
                                `user_id`	BIGINT	NOT NULL,
                                `walk_id`	BIGINT	NOT NULL,
                                `expression_id`	BIGINT	NULL
);

ALTER TABLE `blocks` ADD CONSTRAINT `PK_BLOCKS` PRIMARY KEY (
                                                             `block_id`
    );

ALTER TABLE `user_achievements` ADD CONSTRAINT `PK_USER_ACHIEVEMENTS` PRIMARY KEY (
                                                                                   `user_achievement_id`
    );

ALTER TABLE `mission_records` ADD CONSTRAINT `PK_MISSION_RECORDS` PRIMARY KEY (
                                                                               `mission_record_id`
    );

ALTER TABLE `walk_points` ADD CONSTRAINT `PK_WALK_POINTS` PRIMARY KEY (
                                                                       `walk_point_id`
    );

ALTER TABLE `dog_rank` ADD CONSTRAINT `PK_DOG_RANK` PRIMARY KEY (
                                                                 `dog_rank_id`
    );

ALTER TABLE `analysis` ADD CONSTRAINT `PK_ANALYSIS` PRIMARY KEY (
                                                                 `analysis_id`
    );

ALTER TABLE `walks` ADD CONSTRAINT `PK_WALKS` PRIMARY KEY (
                                                           `walk_id`
    );

ALTER TABLE `dogs` ADD CONSTRAINT `PK_DOGS` PRIMARY KEY (
                                                         `dog_id`
    );

ALTER TABLE `missions` ADD CONSTRAINT `PK_MISSIONS` PRIMARY KEY (
                                                                 `mission_id`
    );

ALTER TABLE `region_rank` ADD CONSTRAINT `PK_REGION_RANK` PRIMARY KEY (
                                                                       `region_rank_id`
    );

ALTER TABLE `walk_images` ADD CONSTRAINT `PK_WALK_IMAGES` PRIMARY KEY (
                                                                       `walk_image_id`
    );

ALTER TABLE `region_dog_rank` ADD CONSTRAINT `PK_REGION_DOG_RANK` PRIMARY KEY (
                                                                               `region_user_rank_id`
    );

ALTER TABLE `breed` ADD CONSTRAINT `PK_BREED` PRIMARY KEY (
                                                           `breed_id`
    );

ALTER TABLE `achievements` ADD CONSTRAINT `PK_ACHIEVEMENTS` PRIMARY KEY (
                                                                         `achievement_id`
    );

ALTER TABLE `block_ownership` ADD CONSTRAINT `PK_BLOCK_OWNERSHIP` PRIMARY KEY (
                                                                               `block_id`
    );

ALTER TABLE `expressions` ADD CONSTRAINT `PK_EXPRESSIONS` PRIMARY KEY (
                                                                       `expression_id`
    );

ALTER TABLE `users` ADD CONSTRAINT `PK_USERS` PRIMARY KEY (
                                                           `user_id`
    );

ALTER TABLE `point_ledger` ADD CONSTRAINT `PK_POINT_LEDGER` PRIMARY KEY (
                                                                         `point_ledger_id`
    );

ALTER TABLE `regions` ADD CONSTRAINT `PK_REGIONS` PRIMARY KEY (
                                                               `region_id`
    );

ALTER TABLE `recap_images` ADD CONSTRAINT `PK_RECAP_IMAGES` PRIMARY KEY (
                                                                         `recap_image_id`
    );

ALTER TABLE `block_pass_logs` ADD CONSTRAINT `PK_BLOCK_PASS_LOGS` PRIMARY KEY (
                                                                               `pixel_pass_logs_id`
    );

ALTER TABLE `recaps` ADD CONSTRAINT `PK_RECAPS` PRIMARY KEY (
                                                             `recap_id`
    );

ALTER TABLE `walk_diaries` ADD CONSTRAINT `PK_WALK_DIARIES` PRIMARY KEY (
                                                                         `walk_diary_id`
    );

ALTER TABLE `user_achievements` ADD CONSTRAINT `FK_users_TO_user_achievements_1` FOREIGN KEY (
                                                                                              `user_id`
    )
    REFERENCES `users` (
                        `user_id`
        );

ALTER TABLE `user_achievements` ADD CONSTRAINT `FK_achievements_TO_user_achievements_1` FOREIGN KEY (
                                                                                                     `achievement_id`
    )
    REFERENCES `achievements` (
                               `achievement_id`
        );

ALTER TABLE `mission_records` ADD CONSTRAINT `FK_missions_TO_mission_records_1` FOREIGN KEY (
                                                                                             `mission_id`
    )
    REFERENCES `missions` (
                           `mission_id`
        );

ALTER TABLE `mission_records` ADD CONSTRAINT `FK_walks_TO_mission_records_1` FOREIGN KEY (
                                                                                          `walk_id`
    )
    REFERENCES `walks` (
                        `walk_id`
        );

ALTER TABLE `walk_points` ADD CONSTRAINT `FK_walks_TO_walk_points_1` FOREIGN KEY (
                                                                                  `walk_id`
    )
    REFERENCES `walks` (
                        `walk_id`
        );

ALTER TABLE `dog_rank` ADD CONSTRAINT `FK_dogs_TO_dog_rank_1` FOREIGN KEY (
                                                                           `dog_id`
    )
    REFERENCES `dogs` (
                       `dog_id`
        );

ALTER TABLE `dog_rank` ADD CONSTRAINT `FK_regions_TO_dog_rank_1` FOREIGN KEY (
                                                                              `region_id`
    )
    REFERENCES `regions` (
                          `region_id`
        );

ALTER TABLE `analysis` ADD CONSTRAINT `FK_dogs_TO_analysis_1` FOREIGN KEY (
                                                                           `dog_id`
    )
    REFERENCES `dogs` (
                       `dog_id`
        );

ALTER TABLE `walks` ADD CONSTRAINT `FK_dogs_TO_walks_1` FOREIGN KEY (
                                                                     `dog_id`
    )
    REFERENCES `dogs` (
                       `dog_id`
        );

ALTER TABLE `dogs` ADD CONSTRAINT `FK_users_TO_dogs_1` FOREIGN KEY (
                                                                    `user_id`
    )
    REFERENCES `users` (
                        `user_id`
        );

ALTER TABLE `dogs` ADD CONSTRAINT `FK_breed_TO_dogs_1` FOREIGN KEY (
                                                                    `breed_id`
    )
    REFERENCES `breed` (
                        `breed_id`
        );

ALTER TABLE `region_rank` ADD CONSTRAINT `FK_regions_TO_region_rank_1` FOREIGN KEY (
                                                                                    `region_id`
    )
    REFERENCES `regions` (
                          `region_id`
        );

ALTER TABLE `walk_images` ADD CONSTRAINT `FK_walks_TO_walk_images_1` FOREIGN KEY (
                                                                                  `walk_id`
    )
    REFERENCES `walks` (
                        `walk_id`
        );

ALTER TABLE `region_dog_rank` ADD CONSTRAINT `FK_dogs_TO_region_dog_rank_1` FOREIGN KEY (
                                                                                         `dog_id`
    )
    REFERENCES `dogs` (
                       `dog_id`
        );

ALTER TABLE `region_dog_rank` ADD CONSTRAINT `FK_regions_TO_region_dog_rank_1` FOREIGN KEY (
                                                                                            `region_id`
    )
    REFERENCES `regions` (
                          `region_id`
        );

ALTER TABLE `block_ownership` ADD CONSTRAINT `FK_blocks_TO_block_ownership_1` FOREIGN KEY (
                                                                                           `block_id`
    )
    REFERENCES `blocks` (
                         `block_id`
        );

ALTER TABLE `block_ownership` ADD CONSTRAINT `FK_dogs_TO_block_ownership_1` FOREIGN KEY (
                                                                                         `dog_id`
    )
    REFERENCES `dogs` (
                       `dog_id`
        );

ALTER TABLE `expressions` ADD CONSTRAINT `FK_dogs_TO_expressions_1` FOREIGN KEY (
                                                                                 `dog_id`
    )
    REFERENCES `dogs` (
                       `dog_id`
        );

ALTER TABLE `expressions` ADD CONSTRAINT `FK_walks_TO_expressions_1` FOREIGN KEY (
                                                                                  `walk_id`
    )
    REFERENCES `walks` (
                        `walk_id`
        );

ALTER TABLE `users` ADD CONSTRAINT `FK_regions_TO_users_1` FOREIGN KEY (
                                                                        `region_id`
    )
    REFERENCES `regions` (
                          `region_id`
        );

ALTER TABLE `point_ledger` ADD CONSTRAINT `FK_users_TO_point_ledger_1` FOREIGN KEY (
                                                                                    `user_id`
    )
    REFERENCES `users` (
                        `user_id`
        );

ALTER TABLE `recap_images` ADD CONSTRAINT `FK_recaps_TO_recap_images_1` FOREIGN KEY (
                                                                                     `recap_id`
    )
    REFERENCES `recaps` (
                         `recap_id`
        );

ALTER TABLE `block_pass_logs` ADD CONSTRAINT `FK_dogs_TO_block_pass_logs_1` FOREIGN KEY (
                                                                                         `dog_id`
    )
    REFERENCES `dogs` (
                       `dog_id`
        );

ALTER TABLE `block_pass_logs` ADD CONSTRAINT `FK_walks_TO_block_pass_logs_1` FOREIGN KEY (
                                                                                          `walk_id`
    )
    REFERENCES `walks` (
                        `walk_id`
        );

ALTER TABLE `block_pass_logs` ADD CONSTRAINT `FK_blocks_TO_block_pass_logs_1` FOREIGN KEY (
                                                                                           `pixel_id`
    )
    REFERENCES `blocks` (
                         `block_id`
        );

ALTER TABLE `recaps` ADD CONSTRAINT `FK_dogs_TO_recaps_1` FOREIGN KEY (
                                                                       `dog_id`
    )
    REFERENCES `dogs` (
                       `dog_id`
        );

ALTER TABLE `walk_diaries` ADD CONSTRAINT `FK_users_TO_walk_diaries_1` FOREIGN KEY (
                                                                                    `user_id`
    )
    REFERENCES `users` (
                        `user_id`
        );

ALTER TABLE `walk_diaries` ADD CONSTRAINT `FK_walks_TO_walk_diaries_1` FOREIGN KEY (
                                                                                    `walk_id`
    )
    REFERENCES `walks` (
                        `walk_id`
        );

ALTER TABLE `walk_diaries` ADD CONSTRAINT `FK_expressions_TO_walk_diaries_1` FOREIGN KEY (
                                                                                          `expression_id`
    )
    REFERENCES `expressions` (
                              `expression_id`
        );

