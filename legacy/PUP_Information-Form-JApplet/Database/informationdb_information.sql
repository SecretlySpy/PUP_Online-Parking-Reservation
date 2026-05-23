-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: localhost    Database: informationdb
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
-- Table structure for table `information`
--

DROP TABLE IF EXISTS `information`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `information` (
  `FName` varchar(45) NOT NULL,
  `MName` varchar(45) NOT NULL,
  `LName` varchar(45) NOT NULL,
  `Birthdate` varchar(45) NOT NULL,
  `Gender` varchar(45) NOT NULL,
  `Occupation` varchar(45) NOT NULL,
  `Address` varchar(100) NOT NULL,
  PRIMARY KEY (`FName`,`MName`,`LName`,`Birthdate`,`Gender`,`Occupation`,`Address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `information`
--

LOCK TABLES `information` WRITE;
/*!40000 ALTER TABLE `information` DISABLE KEYS */;
INSERT INTO `information` VALUES ('Aldrin Jhan','Germidia','Celedonio','September 26,  1998','Male','Network Manager','Blk 39, Rd. 53 West Bank Road San Juan District, Taytay Rizal'),('Archim Paul','Carpio','Pameroyan','February 16,  2000','Male','Game Developer','83 Umbel Roxas District, Quezon City'),('Chin','Ruado','Ching','June 19,  1999','Female','Game Designer','136 Don Pedro Marculas District, Valenzuela City'),('James Jimmuel','Tolentino','Sambrano','August 31,  2000','Male','System Administrator','45-C Banahaw Cubao District, Quezon City'),('Jhon Lawrence','San Luis','Manalad','September 6,  2000','Male','Software Engineer','21 D Fabella \" \" District, Mandaluyong City'),('Lenard','Santiago','Selda','April 10,  1999','Male','Network Manager','666 Bagong Nayon San Isidro District, Antipolo Rizal'),('Maria Venus','Cutamora','Jemina','November 7,  1999','Female','IT Manager','223 Quintina Sta. Mesa District, Manila'),('Michaela Joy','Baltazar','Reyes','January 30,  1999','Female','Web Developer','11-A West Village Town Homes Catanduanes Paltok District, Quezon City'),('Nikki Anderson','Penos','Daluz','July 26,  2000','Female','Web Developer','287 Alfonso Isidro \" \" District, Pasay City'),('Razelle Ann','Manuel','Apas','December 9,  1999','Female','Computer Programmer','Lot 11 Blk, 9 Karangalan Village Kaligtasan Mangahan District, Pasig City'),('Ruby Ann','Reudeque','Tud','May 27,  2000','Female','Database Administrator','\" \" Katuwaan Batasan Hills District, Quezon City'),('Stephanie Jean','Salvador','Ortega','July 3,  1999','Female','Game Designer','1552 Antipolo Sampaloc District, Manila');
/*!40000 ALTER TABLE `information` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-06-20  1:57:43
