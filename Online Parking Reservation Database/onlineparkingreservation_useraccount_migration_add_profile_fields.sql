-- Run this once on an existing onlineparkingreservation database before using
-- the integrated customer profile screens.

USE `onlineparkingreservation`;

ALTER TABLE `useraccount`
  ADD COLUMN `Occupation` varchar(45) NOT NULL DEFAULT '' AFTER `Birthdate`,
  ADD COLUMN `Address` varchar(100) NOT NULL DEFAULT '' AFTER `Occupation`;
