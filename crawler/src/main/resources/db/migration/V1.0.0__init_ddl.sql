CREATE TABLE `botconfig` (
  `botname` varchar(64) NOT NULL,
  `key` varchar(64) NOT NULL,
  `value` varchar(512) NOT NULL,
  PRIMARY KEY (`botname`,`key`),
  KEY `idx_botconfig_botname` (`botname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `rent_house` (
  `site` varchar(45) NOT NULL DEFAULT '',
  `url` varchar(512) NOT NULL,
  `imageUrl` varchar(512) NOT NULL DEFAULT '',
  `description` varchar(2048) NOT NULL DEFAULT '',
  `price` varchar(45) NOT NULL DEFAULT '',
  `createuser` varchar(45) NOT NULL DEFAULT '',
  `createdate` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`url`),
  KEY `idx_rent_house_createdate` (`createdate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `sale_house` (
  `site` varchar(45) NOT NULL DEFAULT '',
  `url` varchar(512) NOT NULL,
  `imageUrl` varchar(512) NOT NULL DEFAULT '',
  `description` varchar(2048) NOT NULL DEFAULT '',
  `price` varchar(45) NOT NULL DEFAULT '',
  `createuser` varchar(45) NOT NULL DEFAULT '',
  `createdate` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`url`),
  KEY `idx_sale_house_createdate` (`createdate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;