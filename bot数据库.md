用户表

| 字段名称  | 描述   | 数据类型 |
| :-------: | ------ | -------- |
|  user_id  | 主键   | bigint   |
| username  | 用户名 | varchar  |
| firstname | 昵称   | varchar  |

账户表（accounts）

|     字段名称      | 描述                                  | 数据类型  |
| :---------------: | ------------------------------------- | --------- |
|    account_id     | 主键                                  | bigint    |
|      handle       | 操作人                                | varchar   |
|  handleFirstName  | 操作人昵称                            | varchar   |
|   handlestatus    | 操作人显示状态:1表示不显示，0表示显示 | int       |
|     call_back     | 回复人                                | varchar   |
| callBackFirstName | 回复人昵称                            | varchar   |
|  callBackStatus   | 回复人显示状态:1表示不显示，0表示显示 | int       |
|      addTime      | 添加时间                              | timestamp |
|       total       | 总入账                                | bigint    |
|      downing      | 应下发                                | bigint    |
|       down        | 未下发                                | bigint    |
|    dataStatus     | 时间状态:1表示过期，0表示未过期       | int       |
|      setTime      | 设置的过期时间                        | data      |

下发表（Issue）

|     字段名称      | 描述                            | 数据类型  |
| :---------------: | ------------------------------- | --------- |
|    account_id     | 主键                            | bigint    |
|      handle       | 操作人                          | varchar   |
|  handleFirstName  | 操作人昵称                      | varchar   |
|     call_back     | 回复人                          | varchar   |
| callBackFirstName | 回复人昵称                      | varchar   |
|      addTime      | 添加时间                        | timestamp |
|      downed       | 已下发                          | bigint    |
|       down        | 未下发                          | bigint    |
|    dataStatus     | 时间状态:1表示过期，0表示未过期 | int       |
|      setTime      | 设置的过期时间                  | data      |

汇率/费率表（Rate）

|    字段名称    | 描述                                     | 数据类型   |
| :------------: | ---------------------------------------- | ---------- |
|    exchange    | 汇率                                     | BigDecimal |
|      rate      | 费率                                     | BigDecimal |
|    addTime     | 添加时间                                 | Data       |
|    overDue     | 过期时间                                 | Long       |
|  handlestatus  | 操作人的显示状态，1表示不显示，0表示显示 | int        |
| callBackStatus | 回复人的显示状态，1表示不显示，0表示显示 | int        |
|  detailStatus  | 明细显示状态：1表示不显示，0表示显示     | int        |

```mysql
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
```