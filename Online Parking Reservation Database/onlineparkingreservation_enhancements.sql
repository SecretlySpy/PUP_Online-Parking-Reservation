USE `onlineparkingreservation`;

CREATE TABLE IF NOT EXISTS `parking_slots` (
  `slot_id` int NOT NULL,
  `floor` varchar(20) NOT NULL,
  `slot_type` varchar(30) NOT NULL,
  `base_status` varchar(20) NOT NULL DEFAULT 'available',
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`slot_id`),
  KEY `idx_parking_slots_floor_type` (`floor`,`slot_type`,`base_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT IGNORE INTO `parking_slots` (`slot_id`, `floor`, `slot_type`, `base_status`, `is_active`) VALUES
  (1, 'Ground', 'Standard', 'available', 1),
  (2, 'Ground', 'Standard', 'available', 1),
  (3, 'Ground', 'Compact', 'available', 1),
  (4, 'Ground', 'Accessible', 'available', 1),
  (5, 'Second', 'Standard', 'available', 1),
  (6, 'Second', 'Standard', 'available', 1),
  (7, 'Second', 'Compact', 'available', 1),
  (8, 'Second', 'EV', 'available', 1),
  (9, 'Third', 'Standard', 'available', 1),
  (10, 'Third', 'Compact', 'available', 1),
  (11, 'Third', 'Motorcycle', 'available', 1),
  (12, 'Third', 'Motorcycle', 'available', 1);

CREATE TABLE IF NOT EXISTS `reservations` (
  `reservation_id` int NOT NULL AUTO_INCREMENT,
  `reservation_code` varchar(40) NOT NULL,
  `username` varchar(45) NOT NULL,
  `slot_id` int NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'reserved',
  `customer_name` varchar(120) NOT NULL,
  `email` varchar(120) NOT NULL,
  `phone` varchar(45) NOT NULL,
  `vehicle_plate` varchar(45) NOT NULL,
  `qr_payload` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`reservation_id`),
  UNIQUE KEY `uk_reservations_code` (`reservation_code`),
  KEY `idx_reservations_slot_time_status` (`slot_id`,`start_time`,`end_time`,`status`),
  KEY `idx_reservations_user_created` (`username`,`created_at`),
  CONSTRAINT `fk_reservations_slot` FOREIGN KEY (`slot_id`) REFERENCES `parking_slots` (`slot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `notification_log` (
  `notification_id` int NOT NULL AUTO_INCREMENT,
  `reservation_code` varchar(40) NOT NULL,
  `channel` varchar(20) NOT NULL,
  `recipient` varchar(120) NOT NULL,
  `message` varchar(255) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'queued',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  KEY `idx_notification_reservation` (`reservation_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `system_activity` (
  `activity_id` int NOT NULL AUTO_INCREMENT,
  `actor` varchar(80) NOT NULL,
  `activity_type` varchar(40) NOT NULL,
  `activity_message` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`activity_id`),
  KEY `idx_system_activity_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `password_reset_tokens` (
  `reset_id` int NOT NULL AUTO_INCREMENT,
  `account_type` varchar(20) NOT NULL,
  `username` varchar(45) NOT NULL,
  `token_hash` char(64) NOT NULL,
  `expires_at` datetime NOT NULL,
  `used_at` datetime NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`reset_id`),
  UNIQUE KEY `uk_password_reset_token_hash` (`token_hash`),
  KEY `idx_password_reset_account` (`account_type`,`username`,`expires_at`,`used_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
