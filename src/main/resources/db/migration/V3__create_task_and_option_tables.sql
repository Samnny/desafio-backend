-- V3: Cria as tabelas para Atividades (Tasks) e suas Alternativas (Options)

CREATE TABLE task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    statement VARCHAR(255) NOT NULL,
    task_order INT NOT NULL,
    created_at DATETIME(6),
    task_type VARCHAR(31) NOT NULL,
    course_id BIGINT NOT NULL,
    CONSTRAINT fk_task_course FOREIGN KEY (course_id) REFERENCES course(id),
    UNIQUE (course_id, statement)
);

CREATE TABLE `option` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    text VARCHAR(80) NOT NULL,
    is_correct BOOLEAN NOT NULL,
    task_id BIGINT NOT NULL,
    CONSTRAINT fk_option_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
);