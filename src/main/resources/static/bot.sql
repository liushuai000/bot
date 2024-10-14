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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3;
DROP TABLE IF EXISTS Accounts;
CREATE TABLE Accounts (
                        id VARCHAR(255) NOT NULL,
                          account_id INT AUTO_INCREMENT PRIMARY KEY,
                          handle VARCHAR(255) NOT NULL,
                        handle_first_name VARCHAR(255),
                        handle_status int DEFAULT 1,
                          call_back VARCHAR(255)   DEFAULT '',
                        call_back_first_name VARCHAR(255)   DEFAULT '',
                        call_back_status int DEFAULT 1,
                        total DECIMAL(11, 2) NOT NULL DEFAULT 0.00, -- 假设余额最多有两位小数
                          downing DECIMAL(11, 2)  DEFAULT 0.00, -- 假设余额最多有两位小数

                          down DECIMAL(11, 2)  DEFAULT 0.00, -- 假设余额最多有两位小数
                          addTime datetime,
                        data_status int,
                        set_time datetime

);
DROP TABLE IF EXISTS Issue;
CREATE TABLE Issue (          id VARCHAR(255) NOT NULL,
                          account_id INT AUTO_INCREMENT PRIMARY KEY,
                          handle VARCHAR(255) NOT NULL,
                              handle_first_name VARCHAR(255),
                          call_back VARCHAR(255)  DEFAULT '',
                              call_back_first_name VARCHAR(255)  DEFAULT '',

                          downed DECIMAL(11, 2)  DEFAULT 0.00, -- 假设余额最多有两位小数
                          down DECIMAL(11, 2)  DEFAULT 0.00, -- 假设余额最多有两位小数

                              add_time datetime,
                              data_status int,
                              set_time datetime

);
DROP TABLE IF EXISTS Rate;
CREATE TABLE Rate (      id VARCHAR(255) NOT NULL,
                          account_id INT AUTO_INCREMENT PRIMARY KEY,
                          exchange DECIMAL(5, 2)  DEFAULT 0.00, -- 假设余额最多有两位小数
                          rate DECIMAL(5, 2)  DEFAULT 0.00,
                         add_time datetime,
                         over_due bigint,
                         handle_status int DEFAULT 1,
                         call_back_status int DEFAULT 1,
                         detail_status int

);