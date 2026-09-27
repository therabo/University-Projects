/*Utenti*/
INSERT INTO Utente (email, nome, cognome, telefono, password, carta_identità, metodo_pagamento, tipo_utente)
VALUES ('email@dominio.com', 'Nome','Cognome','4839451230','pass','AY1234567','1234567890123456','Ospite');
INSERT INTO Utente (email, nome, cognome, telefono, password, carta_identità, metodo_pagamento, tipo_utente)
VALUES ('email1@dominio.com', 'Nome1','Cognome1','8207451230','pass1','FA1234567','3444974903635514','Ospite');
INSERT INTO Utente (email, nome, cognome, telefono, password, carta_identità, metodo_pagamento, tipo_utente)
VALUES ('email2@dominio.com', 'Nome2','Cognome2','4831100230','pass2','TR1234567','2959416984496534','Host');
INSERT INTO Utente (email, nome, cognome, telefono, password, carta_identità, metodo_pagamento, tipo_utente)
VALUES ('email3@dominio.com', 'Nome3','Cognome3','4839459485','pass3','GF1234567','1532688294058235','Ospite');
INSERT INTO Utente (email, nome, cognome, telefono, password, carta_identità, metodo_pagamento, tipo_utente)
VALUES ('email4@dominio.com', 'Nome4','Cognome4','4569451230','pass4','LO1234567','4573686481184001','Host');
INSERT INTO Utente (email, nome, cognome, telefono, password, carta_identità, metodo_pagamento, tipo_utente)
VALUES ('email5@dominio.com', 'Nome5','Cognome5','1829457230','pass5','VM1234567','6810506018050947','Host');
INSERT INTO Utente (email, nome, cognome, telefono, password, carta_identità, metodo_pagamento, tipo_utente)
VALUES ('email6@dominio.com', 'Nome6','Cognome5','9979457230','pass6','CD1234567','5078993707728672','Ospite');
INSERT INTO Utente (email, nome, cognome, telefono, password, carta_identità, metodo_pagamento, tipo_utente)
VALUES ('email7@dominio.com', 'Nome7','Cognome7','1823355230','pass7','AZ1234567','0240016058434637','Ospite');
INSERT INTO Utente (email, nome, cognome, telefono, password, carta_identità, metodo_pagamento, tipo_utente)
VALUES ('email8@dominio.com', 'Nome8','Cognome8','1826967230','pass8','QE1234567','6571113543226917','Host');
INSERT INTO Utente (email, nome, cognome, telefono, password, carta_identità, metodo_pagamento, tipo_utente)
VALUES ('email9@dominio.com', 'Nome9','Cognome9','9989457230','pass9','PU1234567','3070028007817923','Host');

/*Alloggio*/
INSERT INTO Alloggio (id_alloggio, nome, prezzo_per_persona, numero_letti, costo_pulizia,ora_checkin, ora_checkout, tipo_struttura, proprietario,comune, via, civico,descrizione)
VALUES(1, 'theHouse', 65.50, 5, 100, '16:00','11:00','Appartamento','email2@dominio.com','TO','Via Roma', '30',
'Aappartamento si trova nel Quadrilatero Romano, a pochi passi dalla splendida chiesa della Consolata e del Bicerin, uno dei caffè più antichi della città 
dove potrete gustare l’omonima bevanda prediletta da Camillo Benso conte di Cavour.
A 10/15 minuti di cammino si trovano i principali monumenti e musei del centro storico: Palazzo Reale, Palazzo Madama, Museo del Risorgimento e Museo Egizio.');

INSERT INTO Alloggio (id_alloggio, nome, prezzo_per_persona, numero_letti, costo_pulizia,ora_checkin, ora_checkout, tipo_struttura, proprietario,comune, via, civico,descrizione)
VALUES(2, 'theHouse1', 130, 3, 80, '13:00','10:00','Stanza Privata','email4@dominio.com','RM','Via Einstein', '27',
'Camera spaziosa e confortevole in appartamento luminoso e silenzioso situato nei pressi di Viale Marconi, a 150 mt dalla stazione Trastevere,
e poco distante dal centro storico, a cui è ben collegato con i trasporti pubblici .');

INSERT INTO Alloggio (id_alloggio, nome, prezzo_per_persona, numero_letti, costo_pulizia,ora_checkin, ora_checkout, tipo_struttura, proprietario,comune, via, civico,descrizione)
VALUES(3, 'theHouse2', 21.36, 4, 20, '14:00','9:00','Stanza Condivisa','email5@dominio.com','BO','Via Newton', '10',
'La casa è composta da 2 camere da letto, un soggiorno abitabile, cucina attrezzata con elettrodomestici in cui si può gustare il gusto della cucina, ci sono lavatrice,
asciugacapelli e stoviglie di base! Non esitate a contattarci per maggiori informazioni');

INSERT INTO Alloggio (id_alloggio, nome, prezzo_per_persona, numero_letti, costo_pulizia,ora_checkin, ora_checkout, tipo_struttura, proprietario,comune, via, civico,descrizione)
VALUES(4, 'theHouse3', 1000, 5, 10, '15:00','11:30','Altra struttura','email8@dominio.com','MI','Via Da Vinci', '2',
'Esclusivo attico di 75 mq con terrazza, cucina abitabile, camino, salotto, camera da letto, grande bagno con doccia doppia.');

INSERT INTO Alloggio (id_alloggio, nome, prezzo_per_persona, numero_letti,ora_checkin, ora_checkout, tipo_struttura, proprietario,comune, via, civico,descrizione)
VALUES(5, 'theHouse4', 96.33, 5, '17:00','10:30','Appartamento','email9@dominio.com','CA','Via Galileo', '130/A',
'Intero appartamento di 70mq nel centro città, a pochissimi metri dalla via Roma, immerso nel quartiere Marina.
Uscendo di casa vi ritroverete di fronte i più famosi ristoranti e locali della città, ma anche il porto, la stazione,
le fermate dei bus e tutto quel che la parte più centrale della città garantisce.');


/*Prenotazioni*/

INSERT INTO Prenotazione(id_prenotazione,stato_prenotazione,numero_ospiti,data_inizio,data_fine,conclusa,alloggio,gestore_prenotazione)
VALUES(1,'Confermata',5,'2/15/2022','2/22/2022',TRUE,1,'email@dominio.com');
INSERT INTO Prenotazione(id_prenotazione,stato_prenotazione,numero_ospiti,data_inizio,data_fine,alloggio,gestore_prenotazione)
VALUES(2,'Cancellata',3,'4/10/2022','4/12/2022',2,'email1@dominio.com');
INSERT INTO Prenotazione(id_prenotazione,stato_prenotazione,numero_ospiti,data_inizio,data_fine,alloggio,gestore_prenotazione)
VALUES(3,'Attesa',4,'8/10/2022','8/20/2022',3,'email3@dominio.com');
INSERT INTO Prenotazione(id_prenotazione,stato_prenotazione,numero_ospiti,data_inizio,data_fine,alloggio,gestore_prenotazione)
VALUES(4,'Rifiutata',5,'10/12/2022','10/15/2022',4,'email6@dominio.com');
INSERT INTO Prenotazione(id_prenotazione,stato_prenotazione,numero_ospiti,data_inizio,data_fine,conclusa,alloggio,gestore_prenotazione)
VALUES(5,'Confermata',5,'4/1/2022','4/5/2022',TRUE,5,'email7@dominio.com');
INSERT INTO Prenotazione(id_prenotazione,stato_prenotazione,numero_ospiti,data_inizio,data_fine,alloggio,gestore_prenotazione)
VALUES(6,'Attesa',3,'9/12/2022','9/19/2022',2,'email1@dominio.com');
INSERT INTO Prenotazione(id_prenotazione,stato_prenotazione,numero_ospiti,data_inizio,data_fine,conclusa,alloggio,gestore_prenotazione)
VALUES(7,'Confermata',5,'5/15/2022','5/22/2022',TRUE,5,'email3@dominio.com');

/*Recensione*/

INSERT INTO Recensione(id_recensione,data,ora,prenotazione,visibile)
VALUES(1,'2/25/2022','15:37',1,TRUE);

INSERT INTO Recensione(id_recensione,data,ora,prenotazione)
VALUES(2,'4/5/2022','18:50',5);

INSERT INTO Recensione(id_recensione,data,ora,prenotazione,visibile)
VALUES(3,'5/23/2022','16:22',7,TRUE);


/*Recensione Alloggio*/

INSERT INTO Recensione_Alloggio(recensione,testo,pulizia,comunicazione,qualità_prezzo,posizione)
VALUES(1,'Bellissimo posto!',4,4,4,5);

INSERT INTO Recensione_Alloggio(recensione,testo,pulizia,comunicazione,qualità_prezzo,posizione)
VALUES(2,'Non mi è piaciuto.',1,1,1,2);

INSERT INTO Recensione_Alloggio(recensione,testo,pulizia,comunicazione,qualità_prezzo,posizione)
VALUES(3,'Ottima sistemazione',4,4,5,5);


/*Recensione Ospite*/
INSERT INTO Recensione_Ospite(recensione,commento_ospite)
VALUES(1,'Gli ospiti sono stati molto riguardevoli verso la struttura.');

INSERT INTO Recensione_Ospite(recensione,commento_ospite)
VALUES(3,'Hanno lasciato tutto pulito e in ordine');


/*Recensione Host*/
INSERT INTO Recensione_Host(recensione,commento_host)VALUES(1,'Host cordiale, ci accolto con dei dolci tipici.');
INSERT INTO Recensione_Host(recensione,commento_host)VALUES(2,'Host si è presentato al checkin con due ore di ritardo.');
INSERT INTO Recensione_Host(recensione,commento_host)VALUES(3,'Host molto disponibile.');

/*Commento*/
INSERT INTO Commento(afferenza,autore,data,ora,commento)VALUES(1,'email@dominio.com','2/25/2022','10:20','Piacevole soggiorno, ci ha fatto piacere essere ospitati qui');
INSERT INTO Commento(afferenza,autore,data,ora,commento)VALUES(1,'email2@dominio.com','2/25/2022','15:06','Grazie a voi!');
INSERT INTO Commento(afferenza,autore,data,ora,commento)VALUES(1,'email6@dominio.com','4/5/2022','9:10','Sapete indicarmi qualche ristorante nei dintorni della struttura? Grazie');
INSERT INTO Commento(afferenza,autore,data,ora,commento)VALUES(1,'email@dominio.com','4/6/2022','19:44','Certamente, guardi qui: https://www.ristoranteitaliano.it/');

/*Servizi*/
INSERT INTO Servizio(nome) VALUES('Cucina');
INSERT INTO Servizio(nome) VALUES('Wi-Fi');
INSERT INTO Servizio(nome) VALUES('Garage');
INSERT INTO Servizio(nome) VALUES('Lavatrice');
INSERT INTO Servizio(nome) VALUES('TV');
INSERT INTO Servizio(nome) VALUES('Asciugacapelli');
INSERT INTO Servizio(nome) VALUES('Aria condizionata');
INSERT INTO Servizio(nome) VALUES('Asciugatrice');
INSERT INTO Servizio(nome) VALUES('Cassaforte');
INSERT INTO Servizio(nome) VALUES('Piscina');
INSERT INTO Servizio(nome) VALUES('Macchina per il caffè');
INSERT INTO Servizio(nome) VALUES('Animali inclusi');
INSERT INTO Servizio(nome) VALUES('Palestra');
INSERT INTO Servizio(nome) VALUES('Lenzuola');


/*Immagine*/

INSERT INTO Immagine(alloggio,foto) VALUES(1, 'pic1.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(1, 'pic2.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(1, 'pic3.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(1, 'pic4.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(1, 'pic5.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(2, 'pic1.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(2, 'pic2.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(2, 'pic3.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(2, 'pic4.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(2, 'pic5.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(3, 'pic1.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(3, 'pic2.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(3, 'pic3.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(4, 'pic1.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(4, 'pic2.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(5, 'pic1.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(5, 'pic2.jpg');
INSERT INTO Immagine(alloggio,foto) VALUES(5, 'pic3.jpg');


/*Partecipazione*/
INSERT INTO Partecipazione(prenotazione,ospite) VALUES(1,'email@dominio.com');
INSERT INTO Partecipazione(prenotazione,ospite) VALUES(1,'email1@dominio.com');
INSERT INTO Partecipazione(prenotazione,ospite) VALUES(1,'email3@dominio.com');
INSERT INTO Partecipazione(prenotazione,ospite) VALUES(5,'email7@dominio.com');
INSERT INTO Partecipazione(prenotazione,ospite) VALUES(5,'email8@dominio.com');
INSERT INTO Partecipazione(prenotazione,ospite) VALUES(3,'email3@dominio.com');
INSERT INTO Partecipazione(prenotazione,ospite) VALUES(3,'email1@dominio.com');
INSERT INTO Partecipazione(prenotazione,ospite) VALUES(3,'email@dominio.com');



/*Fornito*/
INSERT INTO Fornito(servizio,alloggio) VALUES('Cucina',1);
INSERT INTO Fornito(servizio,alloggio) VALUES('Wi-Fi',1);
INSERT INTO Fornito(servizio,alloggio) VALUES('Lavatrice',1);
INSERT INTO Fornito(servizio,alloggio) VALUES('TV',1);
INSERT INTO Fornito(servizio,alloggio) VALUES('Asciugacapelli',1);
INSERT INTO Fornito(servizio,alloggio) VALUES('Cucina',2);
INSERT INTO Fornito(servizio,alloggio) VALUES('Wi-Fi',2);
INSERT INTO Fornito(servizio,alloggio) VALUES('Lavatrice',2);
INSERT INTO Fornito(servizio,alloggio) VALUES('Animali inclusi',2);
INSERT INTO Fornito(servizio,alloggio) VALUES('Aria condizionata',2);
INSERT INTO Fornito(servizio,alloggio) VALUES('Wi-Fi',3);
INSERT INTO Fornito(servizio,alloggio) VALUES('Lavatrice',3);
INSERT INTO Fornito(servizio,alloggio) VALUES('Animali inclusi',3);
INSERT INTO Fornito(servizio,alloggio) VALUES('Aria condizionata',4);
INSERT INTO Fornito(servizio,alloggio) VALUES('Lenzuola',4);
INSERT INTO Fornito(servizio,alloggio) VALUES('Garage',5);
INSERT INTO Fornito(servizio,alloggio) VALUES('Wi-Fi',5);
INSERT INTO Fornito(servizio,alloggio) VALUES('Macchina per il caffè',5);


/*Preferito*/
INSERT INTO Preferito(utente,alloggio) VALUES('email@dominio.com',1);
INSERT INTO Preferito(utente,alloggio) VALUES('email@dominio.com',3);
INSERT INTO Preferito(utente,alloggio) VALUES('email@dominio.com',5);
INSERT INTO Preferito(utente,alloggio) VALUES('email3@dominio.com',4);
INSERT INTO Preferito(utente,alloggio) VALUES('email3@dominio.com',5);
INSERT INTO Preferito(utente,alloggio) VALUES('email5@dominio.com',2);
INSERT INTO Preferito(utente,alloggio) VALUES('email5@dominio.com',3);
INSERT INTO Preferito(utente,alloggio) VALUES('email6@dominio.com',1);
INSERT INTO Preferito(utente,alloggio) VALUES('email6@dominio.com',2);
INSERT INTO Preferito(utente,alloggio) VALUES('email6@dominio.com',3);
INSERT INTO Preferito(utente,alloggio) VALUES('email7@dominio.com',3);
INSERT INTO Preferito(utente,alloggio) VALUES('email7@dominio.com',5);
