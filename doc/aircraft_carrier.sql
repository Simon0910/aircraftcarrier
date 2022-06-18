-- --------------------------------------------------------
-- 主机:                           127.0.0.1
-- 服务器版本:                        5.7.20-log - MySQL Community Server (GPL)
-- 服务器操作系统:                      Win64
-- HeidiSQL 版本:                  11.3.0.6295
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- 导出 aircraftcarrier 的数据库结构
CREATE DATABASE IF NOT EXISTS `aircraftcarrier` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `aircraftcarrier`;

-- 导出  表 aircraftcarrier.demo 结构
CREATE TABLE IF NOT EXISTS `demo` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_no` varchar(32) NOT NULL COMMENT '业务主键',
  `seller_no` varchar(32) NOT NULL COMMENT '商家编码',
  `seller_name` varchar(80) NOT NULL COMMENT '商家名称',
  `description` varchar(80) NOT NULL DEFAULT '' COMMENT '说明',
  `data_type` int(2) NOT NULL COMMENT '类型',
  `create_user` varchar(32) NOT NULL COMMENT '创建人',
  `update_user` varchar(32) NOT NULL COMMENT '修改人',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` int(1) NOT NULL DEFAULT '0' COMMENT '删除标识,0:正常,1:删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='配置表';

-- 数据导出被取消选择。

-- 导出  表 aircraftcarrier.product 结构
CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods_no` varchar(50) NOT NULL DEFAULT '0' COMMENT '商品编号',
  `amount` int(11) DEFAULT NULL COMMENT '金额',
  `inventory` int(11) DEFAULT NULL COMMENT '库存',
  `version` bigint(20) DEFAULT NULL COMMENT '版本号',
  `create_user` varchar(32) NOT NULL COMMENT '创建人',
  `update_user` varchar(32) NOT NULL COMMENT '修改人',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` int(1) NOT NULL DEFAULT '0' COMMENT '删除标识,0:正常,1:删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='产品表';

-- 数据导出被取消选择。

-- 导出  表 aircraftcarrier.product_details 结构
CREATE TABLE IF NOT EXISTS `product_details` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `weight` int(1) NOT NULL COMMENT '宽度',
  `exist` bigint(20) NOT NULL COMMENT '是否存在',
  `biz_no` varchar(32) NOT NULL DEFAULT '' COMMENT '业务主键',
  `seller_no` varchar(50) NOT NULL COMMENT '商家编码',
  `seller_name` varchar(50) NOT NULL COMMENT '商家名称',
  `description` varchar(50) NOT NULL COMMENT '说明',
  `data_type` tinyint(4) NOT NULL COMMENT '类型',
  `birthday` datetime NOT NULL COMMENT '生日',
  `create_user` varchar(50) NOT NULL COMMENT '创建人',
  `update_user` varchar(32) NOT NULL COMMENT '修改人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` int(1) NOT NULL DEFAULT '0' COMMENT '删除标识,0:正常,1:删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='产品详情表';

-- 数据导出被取消选择。

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
