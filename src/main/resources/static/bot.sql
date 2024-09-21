# 完整SQL
DROP DATABASE IF EXISTS bot;
CREATE DATABASE bot CHARACTER SET utf8;
USE bot;

# 用户表
DROP TABLE IF EXISTS user;
CREATE TABLE user
(
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户id',
    username VARCHAR(255)   COMMENT '用户名',
    firstname VARCHAR(255)   COMMENT '用户名'
#     create_time  datetime     null comment '创建时间'
);
DROP TABLE IF EXISTS Accounts;
CREATE TABLE Accounts (
                          account_id INT AUTO_INCREMENT PRIMARY KEY,
                          handle VARCHAR(255) NOT NULL,
                          handleFirstName VARCHAR(255),
                          handlestatus int DEFAULT 1,
                          call_back VARCHAR(255)   DEFAULT '',
                          callBackFirstName VARCHAR(255)   DEFAULT '',
                          callBackStatus int DEFAULT 1,
                          total DECIMAL(11, 2) NOT NULL DEFAULT 0.00, -- 假设余额最多有两位小数
                          downing DECIMAL(11, 2)  DEFAULT 0.00, -- 假设余额最多有两位小数

                          down DECIMAL(11, 2)  DEFAULT 0.00, -- 假设余额最多有两位小数
                          addTime datetime,
                          dataStatus int,
                          setTime datetime

);
DROP TABLE IF EXISTS Issue;
CREATE TABLE Issue (
                          account_id INT AUTO_INCREMENT PRIMARY KEY,
                          handle VARCHAR(255) NOT NULL,
                          handleFirstName VARCHAR(255),
                          call_back VARCHAR(255)  DEFAULT '',
                          callBackFirstName VARCHAR(255)  DEFAULT '',

                          downed DECIMAL(11, 2)  DEFAULT 0.00, -- 假设余额最多有两位小数
                          down DECIMAL(11, 2)  DEFAULT 0.00, -- 假设余额最多有两位小数

                          addTime datetime,
                          dataStatus int,
                          setTime datetime

);
DROP TABLE IF EXISTS Rate;
CREATE TABLE Rate (
                          account_id INT AUTO_INCREMENT PRIMARY KEY,
                          exchange DECIMAL(5, 2)  DEFAULT 0.00, -- 假设余额最多有两位小数
                          rate DECIMAL(5, 2)  DEFAULT 0.00,
                            addTime datetime,
                          overDue bigint,
                          handlestatus int DEFAULT 1,
                          callBackStatus int DEFAULT 1,
                          detailStatus int

);