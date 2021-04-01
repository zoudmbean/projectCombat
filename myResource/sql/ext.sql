-- 补充SQL
-- 1. 创建MQ消息补发表
CREATE TABLE `mq_message`(
	message_id CHAR(32) NOT NULL,
	content TEXT,
	to_exchange VARCHAR(255) DEFAULT NULL,
	routing_key VARCHAR(255) DEFAULT NULL,
	class_type VARCHAR(255) DEFAULT NULL,
	message_status INT(1) DEFAULT '0' COMMENT '0新建 1已发送  2错误抵达 3已抵达',
	create_time DATETIME DEFAULT NULL,
	update_time DATETIME DEFAULT NULL,
	PRIMARY KEY(message_id)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
;
