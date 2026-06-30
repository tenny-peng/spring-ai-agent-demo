CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(64) NOT NULL UNIQUE,
    `email` VARCHAR(128) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(32) NOT NULL DEFAULT 'USER', -- USER, ADMIN
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 默认管理员，密码: admin123（BCrypt 加密）
INSERT IGNORE INTO `user` (`username`, `email`, `password`, `role`, `created_at`, `updated_at`)
VALUES ('admin', 'admin@example.com',
        '$2a$10$iUMWdWTK3IGdV7Sb9sJEf.r1r5uSsDVlq5pXLSn00eI6Il12kuyxW',
        'ADMIN', NOW(), NOW());

CREATE TABLE IF NOT EXISTS `conversation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `conversation_id` VARCHAR(64) NOT NULL UNIQUE,  -- 对应 Redis 里的 threadId
    `title` VARCHAR(255) DEFAULT '新对话',
    `status` VARCHAR(20) DEFAULT 'ACTIVE',           -- ACTIVE, ARCHIVED
    `message_count` INT DEFAULT 0,                   -- 消息条数（方便展示）
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `conversation_id` VARCHAR(64) NOT NULL,
    `role` VARCHAR(10) NOT NULL,
    `content` TEXT NOT NULL,
    `created_at` DATETIME NOT NULL,
    INDEX `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文档记录（一个文件对应一条记录）
CREATE TABLE IF NOT EXISTS `document` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `filename` VARCHAR(255) NOT NULL,          -- 原始文件名
    `file_type` VARCHAR(20) DEFAULT 'CSV',      -- 文件类型: CSV, TXT
    `chunk_count` INT DEFAULT 0,               -- 导入到向量库的切片数
    `status` VARCHAR(20) DEFAULT 'COMPLETED',   -- IMPORTING / COMPLETED / FAILED
    `uploaded_by` BIGINT,                      -- 上传者 User.id
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文档切片记录（文件里的每一行/每一段对应一条记录）
CREATE TABLE IF NOT EXISTS `document_chunk` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `document_id` BIGINT NOT NULL,             -- FK -> document.id
    `vector_id` VARCHAR(128) NOT NULL,          -- Redis VectorStore 中的 Document ID
    `content` TEXT,                             -- 切片内容（QA 对）
    `chunk_index` INT,                          -- 序号
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_memory` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `content` TEXT NOT NULL COMMENT '记忆内容，如"用户喜欢简洁回答"',
    `category` VARCHAR(32) NOT NULL DEFAULT 'OTHER' COMMENT 'PREFERENCE / PERSONAL_INFO / HABIT / OTHER',
    `source` VARCHAR(32) NOT NULL DEFAULT 'MANUAL' COMMENT 'CHAT_EXTRACT / MANUAL / SYSTEM',
    `source_conversation_id` VARCHAR(64) DEFAULT NULL COMMENT '来源会话ID（CHAT_EXTRACT时记录）',
    `confidence` INT DEFAULT 1 COMMENT '置信度 1-65535',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_category` (`category`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户特征记忆表';