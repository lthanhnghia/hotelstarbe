





CREATE TABLE `accounts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `passwords` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `fullname` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `gender` bit(1) DEFAULT b'1',
  `role_id` int DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `phone` (`phone`),
  UNIQUE KEY `email` (`email`),
  KEY `FK_accounts_role_id` (`role_id`),
  CONSTRAINT `FK_accounts_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) 


CREATE TABLE `amenities_hotel` (
  `id` int NOT NULL AUTO_INCREMENT,
  `amenities_hotel_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `hotel_id` int DEFAULT NULL,
  `descriptions` varchar(225) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_amenities_hotel_hotel_id` (`hotel_id`),
  CONSTRAINT `fk_amenities_hotel_hotel_id` FOREIGN KEY (`hotel_id`) REFERENCES `hotel` (`id`)
) 

CREATE TABLE `amenities_type_room` (
  `id` int NOT NULL AUTO_INCREMENT,
  `amenities_type_room_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) 




CREATE TABLE `booking` (
  `id` int NOT NULL AUTO_INCREMENT,
  `create_at` datetime DEFAULT NULL,
  `start_at` datetime DEFAULT NULL,
  `end_at` datetime DEFAULT NULL,
  `status_payment` bit(1) DEFAULT NULL,
  `method_payment_id` int DEFAULT NULL,
  `account_id` int DEFAULT NULL,
  `status_id` int DEFAULT NULL,
  `discount_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `discount_percent` float DEFAULT NULL,
  `descriptions` varchar(225) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_booking_method_payment_id` (`method_payment_id`),
  KEY `FK_booking_account_id` (`account_id`),
  KEY `FK_booking_status_id` (`status_id`),
  CONSTRAINT `FK_booking_account_id` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`),
  CONSTRAINT `FK_booking_method_payment_id` FOREIGN KEY (`method_payment_id`) REFERENCES `method_payment` (`id`),
  CONSTRAINT `FK_booking_status_id` FOREIGN KEY (`status_id`) REFERENCES `status_booking` (`id`)
) 



CREATE TABLE `booking_room` (
  `id` int NOT NULL AUTO_INCREMENT,
  `check_in` datetime DEFAULT NULL,
  `check_out` datetime DEFAULT NULL,
  `price` double DEFAULT NULL,
  `booking_id` int DEFAULT NULL,
  `room_id` int DEFAULT NULL,
  `id_staff` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_booking_room_booking_id` (`booking_id`),
  KEY `FK_booking_room_room_id` (`room_id`),
  KEY `FK_booking_room_staff_id` (`id_staff`),
  CONSTRAINT `FK_booking_room_booking_id` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`id`),
  CONSTRAINT `FK_booking_room_room_id` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`),
  CONSTRAINT `FK_booking_room_staff_id` FOREIGN KEY (`id_staff`) REFERENCES `accounts` (`id`)
)

CREATE TABLE `booking_room_customer_information` (
  `id` int NOT NULL AUTO_INCREMENT,
  `booking_room_id` int DEFAULT NULL,
  `customer_information_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_booking_room_customer_information_booking_room_id` (`booking_room_id`),
  KEY `FK_booking_room_customer_information_customer_information_id` (`customer_information_id`),
  CONSTRAINT `FK_booking_room_customer_information_booking_room_id` FOREIGN KEY (`booking_room_id`) REFERENCES `booking_room` (`id`),
  CONSTRAINT `FK_booking_room_customer_information_customer_information_id` FOREIGN KEY (`customer_information_id`) REFERENCES `customer_information` (`id`)
) 

CREATE TABLE `booking_room_service_hotel` (
  `id` int NOT NULL AUTO_INCREMENT,
  `create_at` datetime DEFAULT NULL,
  `price` double DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `booking_room_id` int DEFAULT NULL,
  `service_hotel_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_booking_room_service_hotel_booking_room_id` (`booking_room_id`),
  KEY `FK_booking_room_service_hotel_service_hotel_id` (`service_hotel_id`),
  CONSTRAINT `FK_booking_room_service_hotel_booking_room_id` FOREIGN KEY (`booking_room_id`) REFERENCES `booking_room` (`id`),
  CONSTRAINT `FK_booking_room_service_hotel_service_hotel_id` FOREIGN KEY (`service_hotel_id`) REFERENCES `service_hotel` (`id`)
) 

CREATE TABLE `booking_room_service_room` (
  `id` int NOT NULL AUTO_INCREMENT,
  `create_at` datetime DEFAULT NULL,
  `price` double DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `booking_room_id` int DEFAULT NULL,
  `service_room_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_booking_room_service_room_booking_room_id` (`booking_room_id`),
  KEY `FK_booking_room_service_room_service_room_id` (`service_room_id`),
  CONSTRAINT `FK_booking_room_service_room_booking_room_id` FOREIGN KEY (`booking_room_id`) REFERENCES `booking_room` (`id`),
  CONSTRAINT `FK_booking_room_service_room_service_room_id` FOREIGN KEY (`service_room_id`) REFERENCES `service_room` (`id`)
) 

CREATE TABLE `customer_information` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cccd` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `fullname` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `phone` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `gender` bit(1) DEFAULT b'1',
  `birthday` datetime DEFAULT NULL,
  `img_first_card` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `img_last_card` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  PRIMARY KEY (`id`)
) 

CREATE TABLE `discount` (
  `id` int NOT NULL AUTO_INCREMENT,
  `discount_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `percent` double DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  `type_room_id` int DEFAULT NULL,
  `status` bit(1) DEFAULT b'1',
  PRIMARY KEY (`id`),
  KEY `FK_discount_type_room_id` (`type_room_id`),
  CONSTRAINT `FK_discount_type_room_id` FOREIGN KEY (`type_room_id`) REFERENCES `type_room` (`id`)
)

CREATE TABLE `discount_account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `discount_id` int DEFAULT NULL,
  `account_id` int DEFAULT NULL,
  `status_da` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_discount_account_da` (`discount_id`),
  KEY `fk_discount_account_ad` (`account_id`),
  CONSTRAINT `fk_discount_account_ad` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`),
  CONSTRAINT `fk_discount_account_da` FOREIGN KEY (`discount_id`) REFERENCES `discount` (`id`)
) 

CREATE TABLE `feedback` (
  `id` int NOT NULL AUTO_INCREMENT,
  `content` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `stars` int DEFAULT NULL,
  `create_at` datetime DEFAULT NULL,
  `rating_status` bit(1) DEFAULT b'1',
  `invoice_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_feedback_invoice_id` (`invoice_id`),
  CONSTRAINT `FK_feedback_invoice_id` FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`id`)
) 

CREATE TABLE `floors` (
  `id` int NOT NULL AUTO_INCREMENT,
  `floor_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) 

CREATE TABLE `hotel` (
  `id` int NOT NULL AUTO_INCREMENT,
  `hotel_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `descriptions` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `province` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `district` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `ward` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `hotel_phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) 
CREATE TABLE `hotel_image` (
  `id` int NOT NULL AUTO_INCREMENT,
  `image_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `hotel_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_hotel_image_hotel_id` (`hotel_id`),
  CONSTRAINT `FK_hotel_image_hotel_id` FOREIGN KEY (`hotel_id`) REFERENCES `hotel` (`id`)
) 

CREATE TABLE `invoice` (
  `id` int NOT NULL AUTO_INCREMENT,
  `create_at` datetime DEFAULT NULL,
  `invoice_status` bit(1) DEFAULT b'0',
  `total_amount` double DEFAULT NULL,
  `booking_id` int DEFAULT NULL,
  `method_payments` varchar(225) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_invoice_booking_id` (`booking_id`),
  CONSTRAINT `FK_invoice_booking_id` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`id`)
) 

CREATE TABLE `method_payment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `method_payment_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) 

CREATE TABLE `roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `role_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) 

CREATE TABLE `room` (
  `id` int NOT NULL AUTO_INCREMENT,
  `room_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `floor_id` int DEFAULT NULL,
  `type_room_id` int DEFAULT NULL,
  `status_room_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_room_floor_id` (`floor_id`),
  KEY `FK_room_type_room_id` (`type_room_id`),
  KEY `FK_room_status_room_id` (`status_room_id`),
  CONSTRAINT `FK_room_floor_id` FOREIGN KEY (`floor_id`) REFERENCES `floors` (`id`),
  CONSTRAINT `FK_room_status_room_id` FOREIGN KEY (`status_room_id`) REFERENCES `status_room` (`id`),
  CONSTRAINT `FK_room_type_room_id` FOREIGN KEY (`type_room_id`) REFERENCES `type_room` (`id`)
) 

CREATE TABLE `service_hotel` (
  `id` int NOT NULL AUTO_INCREMENT,
  `service_hotel_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `price` double DEFAULT NULL,
  `image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `hotel_id` int DEFAULT NULL,
  `descriptions` varchar(225) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_service_hotel_hotel_id` (`hotel_id`),
  CONSTRAINT `fk_service_hotel_hotel_id` FOREIGN KEY (`hotel_id`) REFERENCES `hotel` (`id`)
) 
CREATE TABLE `service_package` (
  `id` int NOT NULL AUTO_INCREMENT,
  `service_package_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `price` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) 
CREATE TABLE `service_room` (
  `id` int NOT NULL AUTO_INCREMENT,
  `service_room_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `price` double DEFAULT NULL,
  `image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `type_service_room_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_type_service_room_` (`type_service_room_id`),
  CONSTRAINT `FK_type_service_room_` FOREIGN KEY (`type_service_room_id`) REFERENCES `type_service_room` (`id`)
) 
CREATE TABLE `status_booking` (
  `id` int NOT NULL AUTO_INCREMENT,
  `status_booking_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) 
CREATE TABLE `status_room` (
  `id` int NOT NULL AUTO_INCREMENT,
  `status_room_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) 
CREATE TABLE `type_bed` (
  `id` int NOT NULL AUTO_INCREMENT,
  `bed_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
)
CREATE TABLE `type_room` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type_room_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `price` double DEFAULT NULL,
  `bed_count` int DEFAULT NULL,
  `acreage` double DEFAULT NULL,
  `guest_limit` int DEFAULT NULL,
  `type_bed_id` int DEFAULT NULL,
  `describes` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_type_bed` (`type_bed_id`),
  CONSTRAINT `FK_type_bed` FOREIGN KEY (`type_bed_id`) REFERENCES `type_bed` (`id`)
) 
CREATE TABLE `type_room_amenities_type_room` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type_room_id` int DEFAULT NULL,
  `amenities_type_room_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_type_room_amenities_type_room_type_room_id` (`type_room_id`),
  KEY `FK_type_room_amenities_type_room_amenities_type_room_id` (`amenities_type_room_id`),
  CONSTRAINT `FK_type_room_amenities_type_room_amenities_type_room_id` FOREIGN KEY (`amenities_type_room_id`) REFERENCES `amenities_type_room` (`id`),
  CONSTRAINT `FK_type_room_amenities_type_room_type_room_id` FOREIGN KEY (`type_room_id`) REFERENCES `type_room` (`id`)
) 
CREATE TABLE `type_room_image` (
  `id` int NOT NULL AUTO_INCREMENT,
  `image_name` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `type_room_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_type_room_image_type_room_id` (`type_room_id`),
  CONSTRAINT `FK_type_room_image_type_room_id` FOREIGN KEY (`type_room_id`) REFERENCES `type_room` (`id`)
)
CREATE TABLE `type_room_service_package` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type_room_id` int DEFAULT NULL,
  `service_package_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_type_room_service_package_type_room_id` (`type_room_id`),
  KEY `FK_type_room_service_package_service_package_id` (`service_package_id`),
  CONSTRAINT `FK_type_room_service_package_service_package_id` FOREIGN KEY (`service_package_id`) REFERENCES `service_package` (`id`),
  CONSTRAINT `FK_type_room_service_package_type_room_id` FOREIGN KEY (`type_room_id`) REFERENCES `type_room` (`id`)
) 
CREATE TABLE `type_service_room` (
  `id` int NOT NULL AUTO_INCREMENT,
  `service_room_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `duration` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
)
