# 完整SQL
DROP DATABASE IF EXISTS bot;
CREATE DATABASE bot CHARACTER SET utf8;
USE bot;

# 用户表
DROP TABLE IF EXISTS user;
CREATE TABLE `user` (
                        `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                        `user_id` varchar(255) DEFAULT NULL COMMENT '纸飞机生成的用户id',
                        `username` varchar(255) DEFAULT NULL COMMENT 'liuxiaolon  这样的用户名',
                        `first_name` varchar(255) DEFAULT NULL COMMENT '刘 ',
                        `last_name` varchar(255) DEFAULT NULL COMMENT 'lastName 是小帅',
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;
DROP TABLE IF EXISTS Accounts;

ALTER TABLE accounts MODIFY downing DECIMAL(38, 2);
CREATE TABLE `accounts` (
                            `id` int NOT NULL AUTO_INCREMENT,
                            `total` decimal(11,2) NOT NULL DEFAULT '0.00',
                            `downing` decimal(11,2) DEFAULT '0.00',
                            `down` decimal(11,2) DEFAULT '0.00',
                            `add_time` datetime DEFAULT NULL,
                            `data_status` int DEFAULT NULL,
                            `set_time` datetime DEFAULT NULL,
                            `user_id` varchar(255) DEFAULT NULL,
                            `rate_id` int DEFAULT NULL COMMENT '这个是rate表的id',
                            `call_back_user_id` varchar(255) DEFAULT NULL COMMENT '回复人的用户id',
                            `account_handler_money` decimal(5,2) DEFAULT '0.00',
                            `group_id` varchar(255) DEFAULT NULL,
                            PRIMARY KEY (`id`) USING BTREE,
                            KEY `index_name` (`id`,`user_id`,`rate_id`,`group_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=511 DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS Issue;
CREATE TABLE `issue` (
                         `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,
                         `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                         `downed` decimal(11,2) DEFAULT '0.00',
                         `down` decimal(11,2) DEFAULT '0.00',
                         `add_time` datetime DEFAULT NULL,
                         `data_status` int DEFAULT NULL,
                         `set_time` datetime DEFAULT NULL,
                         `user_message_text` varchar(5000) DEFAULT NULL,
                         `rate_id` int DEFAULT NULL,
                         `group_id` varchar(255) DEFAULT NULL,
                         `call_back_user_id` varchar(255) DEFAULT NULL COMMENT '回复人的用户id',
                         `issue_handler_money` decimal(5,2) DEFAULT '0.00',
                         PRIMARY KEY (`id`) USING BTREE,
                         KEY `index_name` (`id`,`user_id`,`group_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=195 DEFAULT CHARSET=utf8mb4;
DROP TABLE IF EXISTS notification;
CREATE TABLE `notification` (
                                `id` int NOT NULL AUTO_INCREMENT,
                                `user_id` varchar(255) DEFAULT NULL,
                                `add_time` datetime DEFAULT NULL,
                                `username` varchar(255) DEFAULT NULL,
                                `first_name` varchar(255) DEFAULT NULL,
                                `last_name` varchar(255) DEFAULT NULL,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4;
DROP TABLE IF EXISTS Rate;
CREATE TABLE `rate` (
                        `id` int NOT NULL AUTO_INCREMENT,
                        `exchange` decimal(5,2) DEFAULT '0.00',
                        `rate` decimal(5,2) DEFAULT '0.00',
                        `add_time` datetime DEFAULT NULL,
                        `over_due` timestamp NULL DEFAULT NULL,
                        `is_matcher` bigint DEFAULT NULL,
                        `calc_u` bigint DEFAULT NULL,
                        `group_id` varchar(255) DEFAULT NULL,
                        PRIMARY KEY (`id`) USING BTREE,
                        KEY `index_name` (`id`,`add_time`,`group_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=352 DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS status;
CREATE TABLE `status` (
                          `id` int NOT NULL AUTO_INCREMENT,
                          `group_id` varchar(255) DEFAULT NULL,
                          `group_title` varchar(255) DEFAULT NULL,
                          `handle_status` bigint DEFAULT NULL,
                          `call_back_status` bigint DEFAULT NULL,
                          `detail_status` bigint DEFAULT NULL,
                          `show_money_status` bigint DEFAULT NULL,
                          `show_few` bigint DEFAULT NULL,
                          `account_handler_money` decimal(5,2) DEFAULT '0.00',
                          `issue_handler_money` decimal(5,2) DEFAULT '0.00',
                          `show_handler_money_status` bigint DEFAULT NULL,
                          `display_sort` bigint DEFAULT NULL,
                          PRIMARY KEY (`id`),
                          KEY `index_name` (`group_id`,`handle_status`,`call_back_status`,`detail_status`,`show_money_status`,`show_few`,`display_sort`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS user;
CREATE TABLE `user` (
                        `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                        `user_id` varchar(255) DEFAULT NULL COMMENT '纸飞机生成的用户id',
                        `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT 'liuxiaolon  这样的用户名',
                        `first_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '刘 ',
                        `last_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'lastName 是小帅',
                        `is_normal` bigint DEFAULT NULL COMMENT '是否管理员用户 ',
                        `old_username` varchar(255) DEFAULT NULL COMMENT '旧的用户名',
                        `old_first_name` varchar(255) DEFAULT NULL,
                        `old_last_name` varchar(255) DEFAULT NULL,
                        `create_time` datetime DEFAULT NULL,
                        `valid_time` datetime DEFAULT NULL,
                        `superiors_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '管理员用户id',
                        `is_operation` bigint DEFAULT NULL COMMENT '管理员设置操作员 是操作员(1) 否(0)  ',
                        `valid_free` bigint DEFAULT NULL,
                        PRIMARY KEY (`id`),
                        KEY `index_name` (`id`,`user_id`,`username`,`is_normal`)
) ENGINE=InnoDB AUTO_INCREMENT=195 DEFAULT CHARSET=utf8mb4;