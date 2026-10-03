-- Schema for Neighbor Smart Living. Contains no imported personal records.
CREATE DATABASE IF NOT EXISTS hmdp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hmdp;

SET NAMES utf8mb4;

CREATE TABLE `tb_blog`  (
                            `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                            `shop_id` bigint(20) NOT NULL COMMENT '商户id',
                            `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
                            `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
                            `images` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '探店的照片，最多9张，多张以\",\"隔开',
                            `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '探店的文字描述',
                            `liked` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '点赞数量',
                            `comments` int(8) UNSIGNED NULL DEFAULT NULL COMMENT '评论数量',
                            `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

CREATE TABLE `tb_blog_comments`  (
                                     `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                                     `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
                                     `blog_id` bigint(20) UNSIGNED NOT NULL COMMENT '探店id',
                                     `parent_id` bigint(20) UNSIGNED NOT NULL COMMENT '关联的1级评论id，如果是一级评论，则值为0',
                                     `answer_id` bigint(20) UNSIGNED NOT NULL COMMENT '回复的评论id',
                                     `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '回复的内容',
                                     `liked` int(8) UNSIGNED NULL DEFAULT NULL COMMENT '点赞数',
                                     `status` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '状态，0：正常，1：被举报，2：禁止查看',
                                     `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

CREATE TABLE `tb_follow`  (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                              `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
                              `follow_user_id` bigint(20) UNSIGNED NOT NULL COMMENT '关联的用户id',
                              `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

CREATE TABLE `tb_seckill_voucher`  (
                                       `voucher_id` bigint(20) UNSIGNED NOT NULL COMMENT '关联的优惠券的id',
                                       `stock` int(8) NOT NULL COMMENT '库存',
                                       `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `begin_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
                                       `end_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '失效时间',
                                       `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       PRIMARY KEY (`voucher_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '秒杀优惠券表，与优惠券是一对一关系' ROW_FORMAT = Compact;

CREATE TABLE `tb_shop`  (
                            `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                            `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商铺名称',
                            `type_id` bigint(20) UNSIGNED NOT NULL COMMENT '商铺类型的id',
                            `images` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商铺图片，多个图片以\',\'隔开',
                            `area` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商圈，例如陆家嘴',
                            `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地址',
                            `x` double UNSIGNED NOT NULL COMMENT '经度',
                            `y` double UNSIGNED NOT NULL COMMENT '维度',
                            `avg_price` bigint(10) UNSIGNED NULL DEFAULT NULL COMMENT '均价，取整数',
                            `sold` int(10) UNSIGNED ZEROFILL NOT NULL COMMENT '销量',
                            `comments` int(10) UNSIGNED ZEROFILL NOT NULL COMMENT '评论数量',
                            `score` int(2) UNSIGNED ZEROFILL NOT NULL COMMENT '评分，1~5分，乘10保存，避免小数',
                            `open_hours` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业时间，例如 10:00-22:00',
                            `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`) USING BTREE,
                            INDEX `foreign_key_type`(`type_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

CREATE TABLE `tb_shop_type`  (
                                 `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型名称',
                                 `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
                                 `sort` int(3) UNSIGNED NULL DEFAULT NULL COMMENT '顺序',
                                 `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

CREATE TABLE `tb_sign`  (
                            `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                            `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
                            `year` year NOT NULL COMMENT '签到的年',
                            `month` tinyint(2) NOT NULL COMMENT '签到的月',
                            `date` date NOT NULL COMMENT '签到的日期',
                            `is_backup` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '是否补签',
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

CREATE TABLE `tb_user`  (
                            `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                            `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号码',
                            `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码，加密存储',
                            `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '昵称，默认是用户id',
                            `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '人物头像',
                            `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`) USING BTREE,
                            UNIQUE INDEX `uniqe_key_phone`(`phone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1010 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

CREATE TABLE `tb_user_info`  (
                                 `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '主键，用户id',
                                 `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '城市名称',
                                 `introduce` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '个人介绍，不要超过128个字符',
                                 `fans` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '粉丝数量',
                                 `followee` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '关注的人的数量',
                                 `gender` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '性别，0：男，1：女',
                                 `birthday` date NULL DEFAULT NULL COMMENT '生日',
                                 `credits` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '积分',
                                 `level` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '会员级别，0~9级,0代表未开通会员',
                                 `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

CREATE TABLE `tb_voucher`  (
                               `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `shop_id` bigint(20) UNSIGNED NULL DEFAULT NULL COMMENT '商铺id',
                               `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '代金券标题',
                               `sub_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '副标题',
                               `rules` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '使用规则',
                               `pay_value` bigint(10) UNSIGNED NOT NULL COMMENT '支付金额，单位是分。例如200代表2元',
                               `actual_value` bigint(10) NOT NULL COMMENT '抵扣金额，单位是分。例如200代表2元',
                               `type` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0,普通券；1,秒杀券',
                               `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '1,上架; 2,下架; 3,过期',
                               `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

CREATE TABLE `tb_voucher_order`  (
                                     `id` bigint(20) NOT NULL COMMENT '主键',
                                     `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '下单的用户id',
                                     `voucher_id` bigint(20) UNSIGNED NOT NULL COMMENT '购买的代金券id',
                                     `pay_type` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '支付方式 1：余额支付；2：支付宝；3：微信',
                                     `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单状态，1：未支付；2：已支付；3：已核销；4：已取消；5：退款中；6：已退款',
                                     `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
                                     `pay_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
                                     `use_time` timestamp NULL DEFAULT NULL COMMENT '核销时间',
                                     `refund_time` timestamp NULL DEFAULT NULL COMMENT '退款时间',
                                     `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     UNIQUE KEY `uk_voucher_order_user_voucher` (`user_id`, `voucher_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

CREATE TABLE `tb_ai_skill` (
                               `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `code` varchar(64) NOT NULL COMMENT '技能编码',
                               `name` varchar(64) NOT NULL COMMENT '技能名称',
                               `scene` varchar(32) NOT NULL DEFAULT 'merchant' COMMENT 'consumer/merchant/admin',
                               `description` varchar(255) DEFAULT NULL COMMENT '技能说明',
                               `input_schema` text COMMENT '输入字段JSON说明',
                               `prompt_template` text NOT NULL COMMENT '固定提示词模板',
                               `output_schema` text COMMENT '输出结构JSON说明',
                               `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
                               `sort` int(8) NOT NULL DEFAULT 0 COMMENT '排序值',
                               `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`) USING BTREE,
                               UNIQUE KEY `uk_ai_skill_code` (`code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI技能定义表';

CREATE TABLE `tb_ai_skill_run` (
                                   `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
                                   `skill_code` varchar(64) NOT NULL COMMENT '技能编码',
                                   `user_id` bigint(20) unsigned NOT NULL COMMENT '调用用户',
                                   `merchant_id` bigint(20) unsigned DEFAULT NULL COMMENT '商家ID',
                                   `input_json` text COMMENT '输入JSON',
                                   `output_json` mediumtext COMMENT '输出JSON',
                                   `status` varchar(16) NOT NULL DEFAULT 'SUCCESS' COMMENT '执行状态',
                                   `error_msg` varchar(255) DEFAULT NULL COMMENT '错误信息',
                                   `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   KEY `idx_ai_skill_run_user` (`user_id`, `create_time`) USING BTREE,
                                   KEY `idx_ai_skill_run_code` (`skill_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI技能调用记录表';

CREATE TABLE `tb_reservation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `user_phone` varchar(32) DEFAULT NULL,
  `shop_id` bigint NOT NULL,
  `reservation_time` datetime NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `num_people` int NOT NULL DEFAULT 1,
  `remark` varchar(255) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reservation_user_time` (`user_id`, `reservation_time`),
  KEY `idx_reservation_shop_time` (`shop_id`, `reservation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
