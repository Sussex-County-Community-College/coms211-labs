-- Instructor setup: use a dedicated lab account, never a student production database.
CREATE DATABASE IF NOT EXISTS coms211_task_lab CHARACTER SET utf8mb4;
USE coms211_task_lab;
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE
);
