DROP TABLE IF EXISTS Batterie_Test CASCADE;
DROP TABLE IF EXISTS Batterie CASCADE;
DROP TABLE IF EXISTS Valeur_Resultat CASCADE;
DROP TABLE IF EXISTS Resultat CASCADE;
DROP TABLE IF EXISTS Test_Equipement CASCADE;
DROP TABLE IF EXISTS Equipement CASCADE;
DROP TABLE IF EXISTS Type_Resultat CASCADE;
DROP TABLE IF EXISTS Tests CASCADE;
DROP TABLE IF EXISTS Unite_Mesure CASCADE;
DROP TABLE IF EXISTS Qualite_Physique CASCADE;
DROP TABLE IF EXISTS Result CASCADE;
DROP TABLE IF EXISTS Test CASCADE;
DROP TABLE IF EXISTS Test_Battery CASCADE;
DROP TABLE IF EXISTS Athlete_Team CASCADE;
DROP TABLE IF EXISTS Athlete CASCADE;
DROP TABLE IF EXISTS Coach CASCADE;
DROP TABLE IF EXISTS Team CASCADE;
DROP TABLE IF EXISTS Administrator CASCADE;
DROP TABLE IF EXISTS User_Account CASCADE;
DROP TABLE IF EXISTS Group_Table CASCADE;
DROP TABLE IF EXISTS Discipline CASCADE;
DROP TABLE IF EXISTS Position CASCADE;
DROP TABLE IF EXISTS Sport CASCADE;
DROP TABLE IF EXISTS Test_Sport CASCADE;
DROP TABLE IF EXISTS athlete_team_position CASCADE;
DROP TABLE IF EXISTS athlete_team_discipline CASCADE;


-- ==========================================
-- 1. REFERENCE TABLES & INDEPENDENT ENTITIES
-- ==========================================

CREATE TABLE Sport (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE Position (
    id SERIAL PRIMARY KEY,
    sport_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,

    CONSTRAINT fk_position_sport FOREIGN KEY (sport_id) REFERENCES Sport(id) ON DELETE CASCADE,
    CONSTRAINT uq_position_sport_name UNIQUE (sport_id, name)
);

CREATE TABLE Discipline (
    id SERIAL PRIMARY KEY,
    sport_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,

    CONSTRAINT fk_discipline_sport FOREIGN KEY (sport_id) REFERENCES Sport(id) ON DELETE CASCADE,
    CONSTRAINT uq_discipline_sport_name UNIQUE (sport_id, name)
);

CREATE TABLE Group_Table (
    id SERIAL PRIMARY KEY,
    name VARCHAR(25) NOT NULL
);

-- ==========================================
-- 2. CORE USER ENTITIES (Inheritance Strategy)
-- ==========================================

CREATE TABLE User_Account (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(254) UNIQUE NOT NULL,
    phone VARCHAR(20),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    account_status VARCHAR(10) NOT NULL DEFAULT 'Active',
    account_creation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    access_level INT NOT NULL, -- 1: Administrator, 2: Coach, 3: Athlete
    CONSTRAINT chk_account_status CHECK (account_status IN ('Active', 'Inactive','Pending')),
    CONSTRAINT chk_access_level CHECK (access_level IN (1, 2, 3)),
    CONSTRAINT uq_user_and_role UNIQUE (id, access_level)
);

CREATE TABLE Administrator (
    user_id INT PRIMARY KEY,
    access_level INT NOT NULL DEFAULT 1,
    title VARCHAR(50),
    
    CONSTRAINT chk_is_admin CHECK (access_level = 1), 
    CONSTRAINT fk_admin_user FOREIGN KEY (user_id, access_level) 
        REFERENCES User_Account(id, access_level) ON DELETE CASCADE
);

-- ==========================================
-- 3. TEAMS & STAFF
-- ==========================================

CREATE TABLE Team (
    id SERIAL PRIMARY KEY,
    sport_id INT NOT NULL,
    name VARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT fk_team_sport FOREIGN KEY (sport_id) REFERENCES Sport(id)
);

CREATE TABLE Coach (
    user_id INT PRIMARY KEY,
    access_level INT NOT NULL DEFAULT 2,
    sport_id INT,
    team_id INT,
    title VARCHAR(50),
    is_head_coach BOOLEAN,
    
    CONSTRAINT chk_is_coach CHECK (access_level = 2), 
    CONSTRAINT fk_coach_user FOREIGN KEY (user_id, access_level) 
        REFERENCES User_Account(id, access_level) ON DELETE CASCADE,
    CONSTRAINT fk_coach_sport FOREIGN KEY (sport_id) REFERENCES Sport(id),
    CONSTRAINT fk_coach_team FOREIGN KEY (team_id) REFERENCES Team(id)
);

-- ==========================================
-- 4. ATHLETES & RELATIONSHIPS
-- ==========================================

CREATE TABLE Athlete (
    user_id INT PRIMARY KEY,
    access_level INT NOT NULL DEFAULT 3,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    height_meters INT,
    weight_kg NUMERIC(4,1),
    dominant_arm VARCHAR(8),
    dominant_leg VARCHAR(6),
    injury_history TEXT,
    
    CONSTRAINT chk_is_athlete CHECK (access_level = 3), 
    CONSTRAINT fk_athlete_user FOREIGN KEY (user_id, access_level) 
        REFERENCES User_Account(id, access_level) ON DELETE CASCADE,
    CONSTRAINT chk_gender CHECK (gender IN ('Female', 'Male')),
    CONSTRAINT chk_arm CHECK (dominant_arm IN ('Right', 'Left')),
    CONSTRAINT chk_leg CHECK (dominant_leg IN ('Right', 'Left'))
);

CREATE TABLE Athlete_Team (
     athlete_id INT NOT NULL,
     team_id INT NOT NULL,
     
     PRIMARY KEY (athlete_id, team_id),
     CONSTRAINT fk_athlete_team_athlete FOREIGN KEY (athlete_id) REFERENCES Athlete(user_id) ON DELETE CASCADE,
     CONSTRAINT fk_athlete_team_team FOREIGN KEY (team_id) REFERENCES Team(id) ON DELETE CASCADE
);

CREATE TABLE Athlete_Team_Position (
    id SERIAL PRIMARY KEY,
    athlete_id INT NOT NULL,
    team_id INT NOT NULL,
    position_id INT NOT NULL,
    
    CONSTRAINT fk_atp_athlete FOREIGN KEY (athlete_id) REFERENCES Athlete(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_atp_team FOREIGN KEY (team_id) REFERENCES Team(id) ON DELETE CASCADE,
    CONSTRAINT fk_atp_position FOREIGN KEY (position_id) REFERENCES Position(id) ON DELETE CASCADE,
    CONSTRAINT uq_athlete_team_position UNIQUE (athlete_id, team_id, position_id)
);

CREATE TABLE Athlete_Team_Discipline (
    id SERIAL PRIMARY KEY,
    athlete_id INT NOT NULL,
    team_id INT NOT NULL,
    discipline_id INT NOT NULL,
    
    CONSTRAINT fk_atd_athlete FOREIGN KEY (athlete_id) REFERENCES Athlete(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_atd_team FOREIGN KEY (team_id) REFERENCES Team(id) ON DELETE CASCADE,
    CONSTRAINT fk_atd_discipline FOREIGN KEY (discipline_id) REFERENCES Discipline(id) ON DELETE CASCADE,
    CONSTRAINT uq_athlete_team_discipline UNIQUE (athlete_id, team_id, discipline_id)
);
-- ==========================================
-- 5. TESTING & PERFORMANCE
-- ==========================================

-- ============================================================================
-- 6. BATTERIES DE TEST
-- ============================================================================

CREATE TABLE Qualite_Physique (
    id_qualite_physique SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL CONSTRAINT uq_qualite_physique_nom UNIQUE,
    description VARCHAR(500)
);

CREATE TABLE Unite_Mesure (
    id_unite SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL CONSTRAINT uq_unite_mesure_nom UNIQUE,
    symbole VARCHAR(20) NOT NULL CONSTRAINT uq_unite_mesure_symbole UNIQUE
);

CREATE TABLE Tests (
    id_test SERIAL PRIMARY KEY,
    id_qualite_physique INT NOT NULL,
    nom_test VARCHAR(100) NOT NULL CONSTRAINT uq_tests_nom UNIQUE,
    protocole TEXT NOT NULL,
    supervise BOOLEAN DEFAULT FALSE NOT NULL,
    informations TEXT,
    preuve_requise BOOLEAN DEFAULT FALSE NOT NULL,

    CONSTRAINT fk_tests_qualite_physique
        FOREIGN KEY (id_qualite_physique)
        REFERENCES Qualite_Physique(id_qualite_physique)
);

CREATE TABLE Type_Resultat (
    id_type_resultat SERIAL PRIMARY KEY,
    id_test INT NOT NULL,
    id_unite_mesure INT NOT NULL,
    nom VARCHAR(200) NOT NULL,
    type_donnee VARCHAR(20) DEFAULT 'DECIMAL' NOT NULL,

    CONSTRAINT fk_type_resultat_test
        FOREIGN KEY (id_test)
        REFERENCES Tests(id_test)
        ON DELETE CASCADE,

    CONSTRAINT fk_type_resultat_unite
        FOREIGN KEY (id_unite_mesure)
        REFERENCES Unite_Mesure(id_unite),

    CONSTRAINT uq_type_resultat_test_nom
        UNIQUE (id_test, nom),

    CONSTRAINT ck_type_resultat_type
        CHECK (
            type_donnee IN (
                'ENTIER',
                'DECIMAL',
                'TEXTE',
                'BOOLEEN'
            )
        )
);

CREATE TABLE Equipement (
    id_equipement SERIAL PRIMARY KEY,
    nom_equipement VARCHAR(100) NOT NULL CONSTRAINT uq_equipement_nom UNIQUE
);

CREATE TABLE Test_Equipement (
    id_test INT NOT NULL,
    id_equipement INT NOT NULL,
    quantite_requise INT DEFAULT 1 NOT NULL,

    CONSTRAINT pk_test_equipement
        PRIMARY KEY (id_test, id_equipement),

    CONSTRAINT fk_test_equipement_test
        FOREIGN KEY (id_test)
        REFERENCES Tests(id_test)
        ON DELETE CASCADE,

    CONSTRAINT fk_test_equipement_equipement
        FOREIGN KEY (id_equipement)
        REFERENCES Equipement(id_equipement),

    CONSTRAINT ck_test_equipement_quantite
        CHECK (quantite_requise >= 1)
);

CREATE TABLE Resultat (
    id_resultat SERIAL PRIMARY KEY,
    id_test INT NOT NULL,
    id_athlete INT NOT NULL,
    preuve VARCHAR(500),
    statut VARCHAR(20) DEFAULT 'EN_ATTENTE' NOT NULL,
    commentaire TEXT,
    date_resultat DATE DEFAULT CURRENT_DATE NOT NULL,

    CONSTRAINT fk_resultat_test
        FOREIGN KEY (id_test)
        REFERENCES Tests(id_test),

    CONSTRAINT fk_resultat_athlete
        FOREIGN KEY (id_athlete)
        REFERENCES Athlete(user_id),

    CONSTRAINT ck_resultat_statut
        CHECK (
            statut IN (
                'EN_ATTENTE',
                'ACCEPTE',
                'REFUSE'
            )
        )
);

CREATE TABLE Valeur_Resultat (
    id_valeur_resultat SERIAL PRIMARY KEY,
    id_resultat INT NOT NULL,
    id_type_resultat INT NOT NULL,
    valeur NUMERIC(10,2) NOT NULL,

    CONSTRAINT fk_valeur_resultat_resultat
        FOREIGN KEY (id_resultat)
        REFERENCES Resultat(id_resultat)
        ON DELETE CASCADE,

    CONSTRAINT fk_valeur_resultat_type
        FOREIGN KEY (id_type_resultat)
        REFERENCES Type_Resultat(id_type_resultat),

    CONSTRAINT uq_valeur_resultat
        UNIQUE (id_resultat, id_type_resultat)
);

CREATE TABLE Batterie (
    id_batterie SERIAL PRIMARY KEY,
    id_equipe INT NOT NULL,
    nom_batterie VARCHAR(100) NOT NULL,
    statut BOOLEAN DEFAULT FALSE NOT NULL,

    CONSTRAINT fk_batterie_equipe
        FOREIGN KEY (id_equipe)
        REFERENCES Team(id),

    CONSTRAINT uq_batterie_equipe_nom
        UNIQUE (id_equipe, nom_batterie)
);

CREATE TABLE Batterie_Test (
    id_batterie INT NOT NULL,
    id_test INT NOT NULL,

    CONSTRAINT pk_batterie_test
        PRIMARY KEY (id_batterie, id_test),

    CONSTRAINT fk_batterie
        FOREIGN KEY (id_batterie)
        REFERENCES Batterie(id_batterie)
        ON DELETE CASCADE,

    CONSTRAINT fk_test
        FOREIGN KEY (id_test)
        REFERENCES Tests(id_test)
);