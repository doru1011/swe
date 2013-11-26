-- ===============================================================================
-- Jede SQL-Anweisung muss in genau 1 Zeile
-- Kommentare durch -- am Zeilenanfang
-- ===============================================================================


--
-- kunde
--
INSERT INTO kunde (username, id, password, version, nachname, erstellt, email, aktualisiert) VALUES ('user1',100,'Ftw2iom0KLJIVIQxO6Z6ORLKA/KytCQpF0pPiz3ITkQ=',0,'Admin','06.10.2013 12:00:00','admin@hska.de','06.10.2013 12:00:00');
INSERT INTO kunde (username, id, password, version, nachname, erstellt, email, aktualisiert) VALUES ('user2',101,'Ftw2iom0KLJIVIQxO6Z6ORLKA/KytCQpF0pPiz3ITkQ=',0,'Adrianson','06.10.2013 12:00:00','101@hska.de','06.10.2013 12:00:00');
INSERT INTO kunde (username, id, password, version, nachname, erstellt, email, aktualisiert) VALUES ('user3',102,'Ftw2iom0KLJIVIQxO6Z6ORLKA/KytCQpF0pPiz3ITkQ=',0,'Alfredson','06.10.2013 12:00:00','102@hska.de','06.10.2013 12:00:00');
INSERT INTO kunde (username, id, password, version, nachname, erstellt, email, aktualisiert) VALUES ('user4',103,'Ftw2iom0KLJIVIQxO6Z6ORLKA/KytCQpF0pPiz3ITkQ=',0,'Antonson','06.10.2013 12:00:00','103@hska.de','06.10.2013 12:00:00');
INSERT INTO kunde (username, id, password, version, nachname, erstellt, email, aktualisiert) VALUES ('user5',104,'Ftw2iom0KLJIVIQxO6Z6ORLKA/KytCQpF0pPiz3ITkQ=',0,'Dirkson','06.10.2013 12:00:00','104@hska.de','06.10.2013 12:00:00');
INSERT INTO kunde (username, id, password, version, nachname, erstellt, email, aktualisiert) VALUES ('user6',105,'Ftw2iom0KLJIVIQxO6Z6ORLKA/KytCQpF0pPiz3ITkQ=',0,'Emilson','06.10.2013 12:00:00','105@hska.de','06.10.2013 12:00:00');


--
-- adresse
--
INSERT INTO adresse(id, kunde_fk, version, plz, ort, erstellt, aktualisiert) VALUES (200,100,0,'76133','Karlsruhe','06.10.2013 12:00:00','06.10.2013 12:00:00');
INSERT INTO adresse(id, kunde_fk, version, plz, ort, erstellt, aktualisiert) VALUES (201,101,0,'76133','Karlsruhe','06.10.2013 12:00:00','06.10.2013 12:00:00');
INSERT INTO adresse(id, kunde_fk, version, plz, ort, erstellt, aktualisiert) VALUES (202,102,0,'76133','Karlsruhe','06.10.2013 12:00:00','06.10.2013 12:00:00');
INSERT INTO adresse(id, kunde_fk, version, plz, ort, erstellt, aktualisiert) VALUES (203,103,0,'76133','Karlsruhe','06.10.2013 12:00:00','06.10.2013 12:00:00');
INSERT INTO adresse(id, kunde_fk, version, plz, ort, erstellt, aktualisiert) VALUES (204,104,0,'76133','Karlsruhe','06.10.2013 12:00:00','06.10.2013 12:00:00');
INSERT INTO adresse(id, kunde_fk, version, plz, ort, erstellt, aktualisiert) VALUES (205,105,0,'76133','Karlsruhe','06.10.2013 12:00:00','06.10.2013 12:00:00');



--
-- file_tbl
--
-- Die eigene Stored Procedure "insert_file_kunde" fuegt in die Tabelle file_tbl eine Zeile bzw. einen Datensatz ein,
-- der u.a. eine Datei enthaelt 
CALL insert_file_kunde(101,1,0,'image.png','image.png','png','I','06.10.2013 12:00:00','06.10.2013 12:00:00');
CALL insert_file_kunde(102,2,0,'video.mp4','video.mp4','mp4','V','06.10.2013 12:00:00','06.10.2013 12:00:00');

--
-- artikel
--
INSERT INTO artikel (id, version, aufLager, name, beschreibung, kategorie_fk, preis, aktualisiert, erstellt) VALUES (300,0,1,'Tisch'     ,'Oval'   ,'BU',80.00,'06.10.2013 12:00:00','06.10.2013 12:00:00');
INSERT INTO artikel (id, version, aufLager, name, beschreibung, kategorie_fk, preis, aktualisiert, erstellt) VALUES (301,0,0,'Stuhl'     ,'Normal' ,'W' ,82.00,'06.10.2013 12:00:00','06.10.2013 12:00:00');
INSERT INTO artikel (id, version, aufLager, name, beschreibung, kategorie_fk, preis, aktualisiert, erstellt) VALUES (302,0,1,'Bett'      ,'Weich'  ,'S' ,80.00,'06.10.2013 12:00:00','06.10.2013 12:00:00');
INSERT INTO artikel (id, version, aufLager, name, beschreibung, kategorie_fk, preis, aktualisiert, erstellt) VALUES (303,0,1,'Schrank'   ,'klein'  ,'W' ,721.00,'06.10.2013 12:00:00','06.10.2013 12:00:00');
INSERT INTO artikel (id, version, aufLager, name, beschreibung, kategorie_fk, preis, aktualisiert, erstellt) VALUES (304,0,0,'Bank'      ,'stabil' ,'G' ,230.00,'06.10.2013 12:00:00','06.10.2013 12:00:00');
INSERT INTO artikel (id, version, aufLager, name, beschreibung, kategorie_fk, preis, aktualisiert, erstellt) VALUES (305,0,1,'Lampe'     ,'dunkel' ,'BA',40.00,'06.10.2013 12:00:00','06.10.2013 12:00:00');
INSERT INTO artikel (id, version, aufLager, name, beschreibung, kategorie_fk, preis, aktualisiert, erstellt) VALUES (306,0,1,'Nachttisch','sperrig','S' ,10.00,'06.10.2013 12:00:00','06.10.2013 12:00:00');

--
-- bestellung
--
INSERT INTO bestellung (id, idx, version, kunde_fk, erstellt, aktualisiert, ausgeliefert) VALUES (400,1,0,100,'06.10.2013 12:00:00','06.10.2013 12:00:00',1);
INSERT INTO bestellung (id, idx, version, kunde_fk, erstellt, aktualisiert, ausgeliefert) VALUES (401,2,0,101,'06.10.2013 12:00:00','06.10.2013 12:00:00',1);
INSERT INTO bestellung (id, idx, version, kunde_fk, erstellt, aktualisiert, ausgeliefert) VALUES (402,3,0,102,'06.10.2013 12:00:00','06.10.2013 12:00:00',1);
INSERT INTO bestellung (id, idx, version, kunde_fk, erstellt, aktualisiert, ausgeliefert) VALUES (403,4,0,103,'06.10.2013 12:00:00','06.10.2013 12:00:00',1);
INSERT INTO bestellung (id, idx, version, kunde_fk, erstellt, aktualisiert, ausgeliefert) VALUES (404,5,0,103,'06.10.2013 12:00:00','06.10.2013 12:00:00',1);

--
-- bestellposition
--
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, menge, idx) VALUES (500,0,400,300,1,0);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, menge, idx) VALUES (501,0,400,301,4,1);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, menge, idx) VALUES (502,0,401,302,5,0);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, menge, idx) VALUES (503,0,402,303,3,0);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, menge, idx) VALUES (504,0,402,304,2,1);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, menge, idx) VALUES (505,0,403,305,1,0);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, menge, idx) VALUES (506,0,404,300,5,0);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, menge, idx) VALUES (507,0,404,300,2,1);
INSERT INTO bestellposition (id, version, bestellung_fk, artikel_fk, menge, idx) VALUES (508,0,404,301,8,2);

--
-- lieferant
--
INSERT INTO lieferant (id, version, name, versandkosten, erstellt, aktualisiert) VALUES (600,0,'UPS','6,90','06.10.2013 12:00:00','06.10.2013 12:00:00');
INSERT INTO lieferant (id, version, name, versandkosten, erstellt, aktualisiert) VALUES (601,0,'DHL','5,90','06.10.2013 12:00:00','06.10.2013 12:00:00');

--
-- kunde_rolle
--
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (101,'admin');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (101,'mitarbeiter');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (101,'kunde');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (102,'mitarbeiter');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (102,'kunde');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (103,'mitarbeiter');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (103,'kunde');
INSERT INTO kunde_rolle (kunde_fk, rolle) VALUES (104,'kunde');
