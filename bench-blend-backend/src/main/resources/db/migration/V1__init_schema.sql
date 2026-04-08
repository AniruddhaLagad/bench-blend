-- V1: Initial Schema for BenchBlend Exam Seating System

-- Admin users table
CREATE TABLE admin_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Exam sessions (e.g., March-April 2026, Oct-Nov 2026)
CREATE TABLE exam_sessions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Exam schedule uploaded per day
CREATE TABLE exam_schedules (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES exam_sessions(id),
    exam_date DATE NOT NULL,
    time_slot VARCHAR(50) NOT NULL,
    subject_code VARCHAR(50) NOT NULL,
    semester VARCHAR(10) NOT NULL,
    subject_name VARCHAR(255) NOT NULL,
    class_name VARCHAR(100) NOT NULL,
    student_count INT NOT NULL,
    version VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Room skeleton uploaded per day
CREATE TABLE room_blocks (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES exam_sessions(id),
    exam_date DATE NOT NULL,
    block_no INT NOT NULL,
    room_no VARCHAR(20) NOT NULL,
    strength INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seating arrangements generated
CREATE TABLE seating_arrangements (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES exam_sessions(id),
    exam_date DATE NOT NULL,
    block_no INT NOT NULL,
    room_no VARCHAR(20) NOT NULL,
    strength INT NOT NULL,
    class_name VARCHAR(100),
    time_slot VARCHAR(50),
    subject_code VARCHAR(50),
    semester VARCHAR(10),
    subject_name VARCHAR(255),
    benches_used INT NOT NULL,
    seating_mode VARCHAR(10) NOT NULL DEFAULT 'SINGLE',
    side VARCHAR(5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Track block usage per class per session (for rotation/no-repeat rule)
CREATE TABLE class_block_history (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES exam_sessions(id),
    class_name VARCHAR(100) NOT NULL,
    subject_code VARCHAR(50) NOT NULL,
    block_no INT NOT NULL,
    room_no VARCHAR(20) NOT NULL,
    exam_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_exam_schedules_date ON exam_schedules(exam_date);
CREATE INDEX idx_room_blocks_date ON room_blocks(exam_date);
CREATE INDEX idx_seating_date ON seating_arrangements(exam_date);
CREATE INDEX idx_class_block_history_session ON class_block_history(session_id, class_name);