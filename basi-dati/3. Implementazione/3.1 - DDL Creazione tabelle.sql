
CREATE TABLE Utente (
    "email"                 VARCHAR(50),
    "nome"                  VARCHAR(20) NOT NULL,
    "cognome"               VARCHAR(20) NOT NULL,
    "telefono"              CHAR(10) UNIQUE  DEFAULT NULL,
    "password"              VARCHAR NOT NULL,
    "carta_identità"        CHAR(9) UNIQUE DEFAULT NULL,
    "metodo_pagamento"      CHAR(16) UNIQUE NOT NULL,
    "tipo_utente"           VARCHAR(15) NOT NULL,
    "superhost"             BOOLEAN  DEFAULT FALSE,
    CONSTRAINT ck_telefono CHECK(telefono ~ '^[0-9]{10}$'),
    CONSTRAINT ck_codice_identità  CHECK (carta_identità ~ '^[A-Z]{2}[0-9]{7}$'),
    CONSTRAINT ck_codice_carta CHECK (metodo_pagamento ~ '^[0-9]{16}$'),
    CONSTRAINT ck_tipo_utente  CHECK(tipo_utente IN ('Ospite', 'Host')),
    PRIMARY KEY(email)
);

CREATE TABLE Alloggio (
    "id_alloggio"           SERIAL,
    "nome"                  VARCHAR(20) NOT NULL,
    "prezzo_per_persona"    FLOAT(10) NOT NULL, 
    "numero_letti"          INT NOT NULL,
    "costo_pulizia"         FLOAT(10) NOT NULL DEFAULT 0,
    "n_recensioni"          INT NOT NULL DEFAULT 0,
    "ora_checkin"           TIME NOT NULL,
    "ora_checkout"          TIME NOT NULL,
    "tipo_struttura"        VARCHAR(20) NOT NULL,
    "proprietario"          VARCHAR(50) NOT NULL,
    "comune"                CHAR(2),
    "via"                   VARCHAR(20),
    "civico"                VARCHAR(5),
    "descrizione"           TEXT NOT NULL,
    CONSTRAINT ck_tipo_struttura CHECK (tipo_struttura IN('Appartamento','Stanza Privata','Stanza Condivisa','Altra struttura')),
    CONSTRAINT ck_orari CHECK (ora_checkout < ora_checkin),
    PRIMARY KEY (id_alloggio),
    FOREIGN KEY(proprietario) REFERENCES Utente(email) ON UPDATE CASCADE ON DELETE CASCADE 

);

CREATE TABLE Prenotazione (
    "id_prenotazione"       SERIAL,
    "stato_prenotazione"    VARCHAR(12) NOT NULL,
    "numero_ospiti"         INT NOT NULL,
    "data_inizio"           DATE NOT NULL, 
    "data_fine"             DATE NOT NULL,
    "conclusa"              BOOLEAN DEFAULT FALSE,
    "alloggio"              SERIAL,
    "gestore_prenotazione"  VARCHAR(50) NOT NULL,
    CONSTRAINT ck_stato_prenotazione CHECK(stato_prenotazione IN('Attesa', 'Confermata', 'Rifiutata', 'Cancellata')),
    CONSTRAINT ck_date CHECK (data_inizio < data_fine),
    CONSTRAINT ck_conclusa CHECK( conclusa = FALSE OR (conclusa = TRUE AND stato_prenotazione = 'Confermata')),
    PRIMARY KEY(id_prenotazione),
    FOREIGN KEY (alloggio) REFERENCES Alloggio(id_alloggio) ON UPDATE CASCADE ON DELETE SET NULL,
    FOREIGN KEY (gestore_prenotazione) REFERENCES Utente(email) ON UPDATE CASCADE  ON DELETE CASCADE

    
);

CREATE TABLE Recensione (
    "id_recensione"         SERIAL,
    "data"                  DATE NOT NULL,
    "ora"                   TIME NOT NULL,
    "prenotazione"          SERIAL,
    "visibile"              BOOLEAN DEFAULT FALSE,
    PRIMARY KEY(id_recensione),
    FOREIGN KEY (prenotazione) REFERENCES Prenotazione(id_prenotazione) ON UPDATE CASCADE ON DELETE SET NULL
    
);

CREATE TABLE Recensione_Alloggio (
    "recensione"            SERIAL,
    "testo"                 TEXT NOT NULL,
    "pulizia"               INT NOT NULL,
    "comunicazione"         INT NOT NULL,
    "qualità_prezzo"        INT NOT NULL,
    "posizione"             INT NOT NULL,
    CONSTRAINT ck_pulizia CHECK (pulizia >= 1 AND pulizia <= 5),
    CONSTRAINT ck_comunicazione CHECK (comunicazione >= 1 AND comunicazione  <= 5),
    CONSTRAINT ck_qualità_prezzo CHECK (qualità_prezzo >= 1 AND qualità_prezzo <= 5),
    CONSTRAINT ck_posizione CHECK (posizione >= 1 AND posizione <= 5),
    PRIMARY KEY(recensione),
    FOREIGN KEY (recensione) REFERENCES Recensione(id_recensione) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Recensione_Ospite (
    "recensione"            SERIAL,
    "commento_ospite"       TEXT NOT NULL,
    PRIMARY KEY(recensione),
    FOREIGN KEY (recensione) REFERENCES Recensione(id_recensione) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Recensione_Host (
    "recensione"            SERIAL,
    "commento_host"         TEXT NOT NULL,
    PRIMARY KEY(recensione),
    FOREIGN KEY (recensione) REFERENCES Recensione(id_recensione) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Commento (
    "afferenza"             SERIAL,
    "autore"                VARCHAR(50),
    "data"                  DATE,
    "ora"                   TIME,
    "commento"              TEXT NOT NULL,
    PRIMARY KEY(afferenza,autore,data,ora),
    FOREIGN KEY (afferenza) REFERENCES Recensione(id_recensione) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (autore) REFERENCES Utente(email) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Partecipazione (
    "prenotazione"          SERIAL,
    "ospite"                VARCHAR(50),
    PRIMARY KEY(prenotazione,ospite),
    FOREIGN KEY (prenotazione) REFERENCES Prenotazione(id_prenotazione) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (ospite) REFERENCES Utente(email) ON UPDATE CASCADE ON DELETE CASCADE

);

CREATE TABLE Fornito (
    "servizio"              VARCHAR(30),
    "alloggio"              SERIAL,
    PRIMARY KEY(servizio,alloggio),
    FOREIGN KEY (servizio) REFERENCES Servizio(nome) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (alloggio) REFERENCES Alloggio(id_alloggio) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Immagine (
    "alloggio"              SERIAL,
    "foto"                  BYTEA DEFAULT NULL,
    PRIMARY KEY(alloggio),
    FOREIGN KEY (alloggio) REFERENCES Alloggio(id_alloggio) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Preferito(
    "utente"                VARCHAR(50),
    "alloggio"              SERIAL,
    PRIMARY KEY(utente,alloggio),
    FOREIGN KEY (utente) REFERENCES Utente(email) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (alloggio) REFERENCES Alloggio(id_alloggio) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Servizio (
    "nome"                  VARCHAR(30),
    PRIMARY KEY(nome)
);