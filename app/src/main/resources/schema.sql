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
