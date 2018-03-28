-- CREATE DATABASE IF NOT EXISTS ebook DEFAULT CHARSET utf8 COLLATE utf8_general_ci;
-- 图书信息
-- drop table `ebook`.`book`;
CREATE TABLE `ebook`.`book` (
  `book_id`          INT UNSIGNED NOT NULL,
  `book_name`        CHAR(50)     NOT NULL
  COMMENT '图书名称',
  `author`           CHAR(50)     NOT NULL
  COMMENT '图书作者',
  `introduction`     CHAR(50) COMMENT '图书简介',
  `image`            CHAR(200)    NOT NULL
  COMMENT '封面URL',
  `last_update_date` DATETIME     NOT NULL
  COMMENT '最后更新时间',
  PRIMARY KEY (`book_id`)
)
  ENGINE = INNODB
  CHARSET = utf8
  COMMENT '图书信息';
-- 图书外链
-- drop table `ebook`.`book_link`;
CREATE TABLE `ebook`.`book_link` (
  `book_id`   INT UNSIGNED NOT NULL,
  `book_name` CHAR(50)     NOT NULL
  COMMENT '图书名称',
  `link_url`  CHAR(200)    NOT NULL
  COMMENT '外链地址',
  PRIMARY KEY (`book_id`)
)
  ENGINE = INNODB
  CHARSET = utf8
  COMMENT '图书外链';

-- 图书索引
-- drop table `ebook`.`book_index`;
CREATE TABLE `ebook`.`book_index` (
  `book_id`     INT UNSIGNED NOT NULL,
  `title`       CHAR(50)     NOT NULL
  COMMENT '标题',
  `page_number` INT COMMENT '页面编号',
  `create_date` DATETIME     NOT NULL
  COMMENT '创建时间'
)
  ENGINE = INNODB
  CHARSET = utf8
  COMMENT '图书索引';

-- 图书章节
-- drop table `ebook`.`book_chapter`;
CREATE TABLE `ebook`.`book_chapter` (
  `book_id`          INT UNSIGNED NOT NULL,
  `page_number`      INT          NOT NULL
  COMMENT '页码',
  `previous_chapter` INT          NOT NULL
  COMMENT '上一章',
  `next_chapter`     INT          NOT NULL
  COMMENT '下一章',
  `title`            VARCHAR(50)  NOT NULL
  COMMENT '标题',
  `content`          LONGTEXT COMMENT '章节内容',
  `create_date`      DATETIME     NOT NULL
  COMMENT '创建时间'
)
  ENGINE = INNODB
  CHARSET = utf8
  COMMENT '图书章节';
SHOW TABLES;
SELECT *
FROM book;
SELECT *
FROM book_link;
SELECT *
FROM book_index;
SELECT *
FROM book_chapter;
