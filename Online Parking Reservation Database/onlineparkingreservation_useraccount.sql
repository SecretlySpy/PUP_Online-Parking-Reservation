-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: localhost    Database: onlineparkingreservation
-- ------------------------------------------------------
-- Server version	8.0.19

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `useraccount`
--

DROP TABLE IF EXISTS `useraccount`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `useraccount` (
  `FirstName` varchar(45) NOT NULL,
  `MiddleName` varchar(45) NOT NULL,
  `LastName` varchar(45) NOT NULL,
  `Email` varchar(120) NOT NULL,
  `Password` varchar(255) NOT NULL,
  `RepeatPassword` varchar(255) NOT NULL,
  `Gender` varchar(45) NOT NULL,
  `Birthdate` varchar(45) NOT NULL,
  `Occupation` varchar(45) NOT NULL DEFAULT '',
  `Address` varchar(100) NOT NULL DEFAULT '',
  `MobileNumber` varchar(45) NOT NULL,
  `Username` varchar(45) NOT NULL,
  `PlateNumber` varchar(45) NOT NULL,
  `Brand` varchar(45) NOT NULL,
  `Color` varchar(45) NOT NULL,
  `Type` varchar(45) NOT NULL,
  PRIMARY KEY (`Username`),
  UNIQUE KEY `uk_useraccount_email` (`Email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `useraccount`
--

LOCK TABLES `useraccount` WRITE;
/*!40000 ALTER TABLE `useraccount` DISABLE KEYS */;
INSERT INTO `useraccount`
  (`FirstName`,`MiddleName`,`LastName`,`Email`,`Password`,`RepeatPassword`,`Gender`,`Birthdate`,`Occupation`,`Address`,`MobileNumber`,`Username`,`PlateNumber`,`Brand`,`Color`,`Type`)
VALUES
  ('Joy','Baltazar','Reyes','reyesjoy@gmail.com','qwerty','qwerty','female','1999','','','09717171717','Harayuri','dfskfjewp342345','Ford','Blue','Crossover'),
  ('Lucy','Reyes','Pameroyan','lucy@gmail.com','123456','123456','female','2020','','','09125679340','StrawberryShortCake','l0c1y','Ferrari','black','SUV'),
  ('Michaela','Baltazar','Reyes','michaelareyes@gmail.com','qwerty','qwerty','female','1999','','','09252525252','Lucy','zxcv0987','Honda','Pink','SUV'),
  ('Aldrin Jhan','Germidia','Celedonio','legacy001@example.com','password','password','Male','September 26,  1998','Network Manager','Blk 39, Rd. 53 West Bank Road San Juan District, Taytay Rizal','0000000000','legacy_aldrin_jhan','LEGACY-001','N/A','N/A','N/A'),
  ('Archim Paul','Carpio','Pameroyan','legacy002@example.com','password','password','Male','February 16,  2000','Game Developer','83 Umbel Roxas District, Quezon City','0000000000','legacy_archim_paul','LEGACY-002','N/A','N/A','N/A'),
  ('Chin','Ruado','Ching','legacy003@example.com','password','password','Female','June 19,  1999','Game Designer','136 Don Pedro Marculas District, Valenzuela City','0000000000','legacy_chin','LEGACY-003','N/A','N/A','N/A'),
  ('James Jimmuel','Tolentino','Sambrano','legacy004@example.com','password','password','Male','August 31,  2000','System Administrator','45-C Banahaw Cubao District, Quezon City','0000000000','legacy_james_jimmuel','LEGACY-004','N/A','N/A','N/A'),
  ('Jhon Lawrence','San Luis','Manalad','legacy005@example.com','password','password','Male','September 6,  2000','Software Engineer','21 D Fabella " " District, Mandaluyong City','0000000000','legacy_jhon_lawrence','LEGACY-005','N/A','N/A','N/A'),
  ('Lenard','Santiago','Selda','legacy006@example.com','password','password','Male','April 10,  1999','Network Manager','666 Bagong Nayon San Isidro District, Antipolo Rizal','0000000000','legacy_lenard','LEGACY-006','N/A','N/A','N/A'),
  ('Maria Venus','Cutamora','Jemina','legacy007@example.com','password','password','Female','November 7,  1999','IT Manager','223 Quintina Sta. Mesa District, Manila','0000000000','legacy_maria_venus','LEGACY-007','N/A','N/A','N/A'),
  ('Michaela Joy','Baltazar','Reyes','legacy008@example.com','password','password','Female','January 30,  1999','Web Developer','11-A West Village Town Homes Catanduanes Paltok District, Quezon City','0000000000','legacy_michaela_joy','LEGACY-008','N/A','N/A','N/A'),
  ('Nikki Anderson','Penos','Daluz','legacy009@example.com','password','password','Female','July 26,  2000','Web Developer','287 Alfonso Isidro " " District, Pasay City','0000000000','legacy_nikki_anderson','LEGACY-009','N/A','N/A','N/A'),
  ('Razelle Ann','Manuel','Apas','legacy010@example.com','password','password','Female','December 9,  1999','Computer Programmer','Lot 11 Blk, 9 Karangalan Village Kaligtasan Mangahan District, Pasig City','0000000000','legacy_razelle_ann','LEGACY-010','N/A','N/A','N/A'),
  ('Ruby Ann','Reudeque','Tud','legacy011@example.com','password','password','Female','May 27,  2000','Database Administrator','" " Katuwaan Batasan Hills District, Quezon City','0000000000','legacy_ruby_ann','LEGACY-011','N/A','N/A','N/A'),
  ('Stephanie Jean','Salvador','Ortega','legacy012@example.com','password','password','Female','July 3,  1999','Game Designer','1552 Antipolo Sampaloc District, Manila','0000000000','legacy_stephanie_jean','LEGACY-012','N/A','N/A','N/A');
/*!40000 ALTER TABLE `useraccount` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-03-29 17:53:35
