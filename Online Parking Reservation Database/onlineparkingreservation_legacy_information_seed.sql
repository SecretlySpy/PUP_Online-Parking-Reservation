-- Optional seed data converted from the archived Information Form applet.
-- Required parking-account fields use deterministic demo placeholders.

USE `onlineparkingreservation`;

INSERT IGNORE INTO `useraccount`
  (`FirstName`,`MiddleName`,`LastName`,`Email`,`Password`,`RepeatPassword`,`Gender`,`Birthdate`,`Occupation`,`Address`,`MobileNumber`,`Username`,`PlateNumber`,`Brand`,`Color`,`Type`)
VALUES
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
