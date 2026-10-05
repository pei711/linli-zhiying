USE hmdp;

CREATE TABLE IF NOT EXISTS `tb_shop_owner` (
    `shop_id` bigint(20) UNSIGNED NOT NULL COMMENT '店铺ID',
    `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '负责该店铺的商家用户ID',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`shop_id`),
    KEY `idx_shop_owner_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺与商家账号映射';

CREATE TABLE IF NOT EXISTS `tb_chat_message` (
    `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    `message_id` char(36) NOT NULL COMMENT 'Kafka消息幂等键',
    `shop_id` bigint(20) UNSIGNED NOT NULL,
    `customer_user_id` bigint(20) UNSIGNED NOT NULL,
    `sender_user_id` bigint(20) UNSIGNED NOT NULL,
    `sender_role` varchar(16) NOT NULL COMMENT 'customer 或 merchant',
    `content` varchar(2000) NOT NULL,
    `create_time` timestamp(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chat_message_id` (`message_id`),
    KEY `idx_chat_conversation` (`shop_id`, `customer_user_id`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户即时消息';
