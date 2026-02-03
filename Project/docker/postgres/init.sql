-- init.sql: database bootstrap for feedback application

-- Allow pgcrypto extension for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Create app_user table
CREATE TABLE IF NOT EXISTS app_user (
    id VARCHAR(255) PRIMARY KEY,
    organizer BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create survey table
CREATE TABLE IF NOT EXISTS survey (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES app_user(id)
);

-- Create form table
CREATE TABLE IF NOT EXISTS form (
    id VARCHAR(255) PRIMARY KEY,
    draft_id VARCHAR(255),
    satisfaction INTEGER DEFAULT 0,
    user_id VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES app_user(id)
);

-- Create question table
CREATE TABLE IF NOT EXISTS question (
    id VARCHAR(255) PRIMARY KEY,
    text TEXT,
    survey_id VARCHAR(255),
    FOREIGN KEY (survey_id) REFERENCES survey(id)
);

-- Create response table
CREATE TABLE IF NOT EXISTS response (
    id VARCHAR(255) PRIMARY KEY,
    question_id VARCHAR(255),
    user_id VARCHAR(255),
    response TEXT,
    FOREIGN KEY (question_id) REFERENCES question(id),
    FOREIGN KEY (user_id) REFERENCES app_user(id)
);

-- Create session_metadata table for new feedback features
CREATE TABLE IF NOT EXISTS session_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    form_id VARCHAR(255) NOT NULL,
    qr_code VARCHAR(500) NOT NULL UNIQUE,
    feedback_start TIMESTAMP,
    feedback_end TIMESTAMP,
    allow_anonymous BOOLEAN DEFAULT TRUE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    FOREIGN KEY (form_id) REFERENCES form(id)
);

-- Create feedback_answer table for storing ratings, comments, and sentiment
CREATE TABLE IF NOT EXISTS feedback_answer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    form_id VARCHAR(255) NOT NULL,
    question_id VARCHAR(255) NOT NULL,
    rating INTEGER,
    comment_text VARCHAR(1000),
    is_anonymous BOOLEAN DEFAULT TRUE,
    sentiment VARCHAR(50) DEFAULT 'NEUTRAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (form_id) REFERENCES form(id),
    FOREIGN KEY (question_id) REFERENCES question(id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_session_metadata_qr_code ON session_metadata(qr_code);
CREATE INDEX IF NOT EXISTS idx_session_metadata_form_id ON session_metadata(form_id);
CREATE INDEX IF NOT EXISTS idx_feedback_answer_form_id ON feedback_answer(form_id);
CREATE INDEX IF NOT EXISTS idx_feedback_answer_question_id ON feedback_answer(question_id);
CREATE INDEX IF NOT EXISTS idx_feedback_answer_sentiment ON feedback_answer(sentiment);

-- Insert sample data for testing
INSERT INTO app_user (id, organizer) VALUES ('org-1', TRUE) ON CONFLICT DO NOTHING;
INSERT INTO app_user (id, organizer) VALUES ('participant-1', FALSE) ON CONFLICT DO NOTHING;
