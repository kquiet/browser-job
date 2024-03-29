CREATE TABLE `real_estate` (
  `url` varchar(512) NOT NULL,
  `imageUrl` varchar(1024) DEFAULT '',
  `title` varchar(128) DEFAULT NULL,
  `type` varchar(16) DEFAULT NULL,
  `layout` varchar(64) DEFAULT NULL,
  `community` varchar(64) DEFAULT NULL,
  `address` varchar(1024) DEFAULT NULL,
  `floor` varchar(32) DEFAULT NULL,
  `age` decimal(5,2) unsigned DEFAULT NULL,
  `parking` varchar(64) DEFAULT NULL,
  `priceTotal` decimal(12,2) unsigned DEFAULT NULL,
  `priceAveragePing` decimal(10,2) unsigned DEFAULT NULL,
  `areaLand` decimal(8,2) unsigned DEFAULT NULL,
  `areaTotal` decimal(8,2) unsigned DEFAULT NULL,
  `areaMain` decimal(8,2) unsigned DEFAULT NULL,
  `site` varchar(32) NOT NULL DEFAULT '',
  `source` varchar(64) DEFAULT NULL,
  `postDate` datetime DEFAULT NULL,
  `createUser` varchar(45) NOT NULL DEFAULT '',
  `createDate` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`url`),
  KEY `idx_real_estate_createDate` (`createDate`),
  KEY `idx_real_estate_postDate` (`postDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;