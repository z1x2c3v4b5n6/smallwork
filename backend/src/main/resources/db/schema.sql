CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS student_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    real_name VARCHAR(50),
    score INT,
    `rank` INT,
    subjects VARCHAR(100),
    province VARCHAR(50),
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS university (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    province VARCHAR(50),
    city VARCHAR(50),
    level VARCHAR(50),
    type VARCHAR(50),
    is_double_top TINYINT(1) DEFAULT 0,
    remark VARCHAR(255)
);
DROP INDEX IF EXISTS idx_university_name ON university;
CREATE INDEX idx_university_name ON university(name);
DROP INDEX IF EXISTS idx_university_province ON university;
CREATE INDEX idx_university_province ON university(province);

CREATE TABLE IF NOT EXISTS major (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    discipline VARCHAR(100),
    subject_req VARCHAR(100),
    level VARCHAR(50),
    remark VARCHAR(255)
);
DROP INDEX IF EXISTS idx_major_name ON major;
CREATE INDEX idx_major_name ON major(name);

CREATE TABLE IF NOT EXISTS university_major (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    university_id BIGINT NOT NULL,
    major_id BIGINT NOT NULL,
    batch VARCHAR(50),
    duration INT,
    tuition INT,
    remark VARCHAR(255),
    CONSTRAINT fk_um_university FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_um_major FOREIGN KEY (major_id) REFERENCES major(id) ON DELETE CASCADE
);
DROP INDEX IF EXISTS idx_um_university ON university_major;
CREATE INDEX idx_um_university ON university_major(university_id);
DROP INDEX IF EXISTS idx_um_major ON university_major;
CREATE INDEX idx_um_major ON university_major(major_id);
DROP INDEX IF EXISTS idx_um_unique ON university_major;
CREATE UNIQUE INDEX idx_um_unique ON university_major(university_id, major_id, batch);

CREATE TABLE IF NOT EXISTS admission_stat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    university_id BIGINT NOT NULL,
    major_id BIGINT NOT NULL,
    year INT NOT NULL,
    batch VARCHAR(50),
    min_score INT,
    min_rank INT,
    avg_score INT,
    avg_rank INT,
    admit_count INT,
    CONSTRAINT fk_stat_university FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_stat_major FOREIGN KEY (major_id) REFERENCES major(id) ON DELETE CASCADE
);
DROP INDEX IF EXISTS idx_stat_university_major_year ON admission_stat;
CREATE INDEX idx_stat_university_major_year ON admission_stat(university_id, major_id, year);

CREATE TABLE IF NOT EXISTS plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);
DROP INDEX IF EXISTS idx_plan_user ON plan;
CREATE INDEX idx_plan_user ON plan(user_id);

CREATE TABLE IF NOT EXISTS plan_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    university_id BIGINT NOT NULL,
    major_id BIGINT NOT NULL,
    batch VARCHAR(50),
    order_no INT,
    CONSTRAINT fk_plan_item_plan FOREIGN KEY (plan_id) REFERENCES plan(id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_item_university FOREIGN KEY (university_id) REFERENCES university(id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_item_major FOREIGN KEY (major_id) REFERENCES major(id) ON DELETE CASCADE
);
DROP INDEX IF EXISTS idx_plan_item_plan ON plan_item;
CREATE INDEX idx_plan_item_plan ON plan_item(plan_id);

INSERT INTO user (username, password, role) VALUES
    ('admin', '$2a$10$Dow1k08LomqKgJo0vvArkOG5vW7/dwZ0pniS3pSke1Mt2rt7NmBG6', 'ROLE_ADMIN'),
    ('student', '$2a$10$Dow1k08LomqKgJo0vvArkOG5vW7/dwZ0pniS3pSke1Mt2rt7NmBG6', 'ROLE_STUDENT')
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO student_profile (user_id, real_name, score, `rank`, subjects, province) VALUES
    ((SELECT id FROM user WHERE username='student'), '张三', 620, 5000, '物理 化学', '浙江')
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name);

INSERT INTO university (name, province, city, level, type, is_double_top, remark) VALUES
    ('浙江大学', '浙江', '杭州', '985', '综合', 1, '双一流A类院校'),
    ('杭州电子科技大学', '浙江', '杭州', '一本', '理工', 0, '信息类优势明显')
ON DUPLICATE KEY UPDATE province = VALUES(province);

INSERT INTO major (name, category, discipline, subject_req, level, remark) VALUES
    ('计算机科学与技术', '工学', '计算机类', '首选物理', '国家级重点', '软件、硬件均衡培养'),
    ('电子信息工程', '工学', '电子信息类', '首选物理', '重点学科', '通信电子方向')
ON DUPLICATE KEY UPDATE category = VALUES(category);

INSERT INTO university_major (university_id, major_id, batch, duration, tuition, remark) VALUES
    ((SELECT id FROM university WHERE name='浙江大学'), (SELECT id FROM major WHERE name='计算机科学与技术'), '本科一批', 4, 5500, '强势专业'),
    ((SELECT id FROM university WHERE name='杭州电子科技大学'), (SELECT id FROM major WHERE name='电子信息工程'), '本科一批', 4, 5200, '就业形势好')
ON DUPLICATE KEY UPDATE tuition = VALUES(tuition);

INSERT INTO admission_stat (university_id, major_id, year, batch, min_score, min_rank, avg_score, avg_rank, admit_count) VALUES
    ((SELECT id FROM university WHERE name='浙江大学'), (SELECT id FROM major WHERE name='计算机科学与技术'), 2023, '本科一批', 650, 1500, 660, 1300, 120),
    ((SELECT id FROM university WHERE name='浙江大学'), (SELECT id FROM major WHERE name='计算机科学与技术'), 2022, '本科一批', 648, 1600, 655, 1400, 118),
    ((SELECT id FROM university WHERE name='杭州电子科技大学'), (SELECT id FROM major WHERE name='电子信息工程'), 2023, '本科一批', 610, 8000, 620, 7000, 200)
ON DUPLICATE KEY UPDATE min_score = VALUES(min_score);

INSERT INTO plan (user_id, name) VALUES
    ((SELECT id FROM user WHERE username='student'), '示例方案')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO plan_item (plan_id, university_id, major_id, batch, order_no) VALUES
    ((SELECT id FROM plan WHERE name='示例方案'), (SELECT id FROM university WHERE name='浙江大学'), (SELECT id FROM major WHERE name='计算机科学与技术'), '本科一批', 1)
ON DUPLICATE KEY UPDATE order_no = VALUES(order_no);
