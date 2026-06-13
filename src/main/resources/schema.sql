DROP TABLE IF EXISTS Resultat CASCADE;
DROP TABLE IF EXISTS Tests CASCADE;
DROP TABLE IF EXISTS Batterie_Test CASCADE;
DROP TABLE IF EXISTS Athlete_Sport CASCADE;
DROP TABLE IF EXISTS Athlete CASCADE;
DROP TABLE IF EXISTS Coach CASCADE;
DROP TABLE IF EXISTS Equipe CASCADE;
DROP TABLE IF EXISTS Administrateur CASCADE;
DROP TABLE IF EXISTS Utilisateur CASCADE;
DROP TABLE IF EXISTS Groupe CASCADE;
DROP TABLE IF EXISTS Discipline CASCADE;
DROP TABLE IF EXISTS Position CASCADE;
DROP TABLE IF EXISTS Sport CASCADE;
DROP TABLE IF EXISTS Test_Sport CASCADE;

-- ==========================================
-- 1. REFERENCE TABLES & INDEPENDENT ENTITIES
-- ==========================================

CREATE TABLE Sport (
    id_sport SERIAL PRIMARY KEY,
    nom_sport VARCHAR(100) NOT NULL
);

CREATE TABLE Position (
    id_position SERIAL PRIMARY KEY,
    id_sport INT NOT NULL,
    nom_position VARCHAR(100) NOT NULL,
    CONSTRAINT fk_position_sport FOREIGN KEY (id_sport) REFERENCES Sport(id_sport) ON DELETE CASCADE
);

CREATE TABLE Discipline (
    id_discipline SERIAL PRIMARY KEY,
    id_sport INT NOT NULL,
    nom_discipline VARCHAR(100) NOT NULL,
    CONSTRAINT fk_discipline_sport FOREIGN KEY (id_sport) REFERENCES Sport(id_sport) ON DELETE CASCADE
);

CREATE TABLE Groupe (
    id_groupe SERIAL PRIMARY KEY,
    nom_groupe VARCHAR(25) NOT NULL
);

-- ==========================================
-- 2. CORE USER ENTITIES (Inheritance Strategy)
-- ==========================================

CREATE TABLE Utilisateur (
    id_utilisateur SERIAL PRIMARY KEY,
    prenom VARCHAR(50) NOT NULL,
    nom VARCHAR(50) NOT NULL,
    courriel VARCHAR(254) UNIQUE NOT NULL,
    telephone VARCHAR(20),
    nom_utilisateur VARCHAR(50) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    statut_compte VARCHAR(10) NOT NULL DEFAULT 'Actif',
    date_creation_compte DATE NOT NULL DEFAULT CURRENT_DATE,
    niveau_acces INT NOT NULL,
    CONSTRAINT chk_statut_compte CHECK (statut_compte IN ('Actif', 'Inactif')),
    CONSTRAINT chk_niveau_acces CHECK (niveau_acces IN (1, 2, 3)) 
    -- 1: Administrateur, 2: Coach, 3: Athlete
);

CREATE TABLE Administrateur (
    id_utilisateur INT PRIMARY KEY,
    titre VARCHAR(50),
    CONSTRAINT fk_admin_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id_utilisateur) ON DELETE CASCADE
);

-- ==========================================
-- 3. TEAMS & STAFF
-- ==========================================

CREATE TABLE Equipe (
    id_equipe SERIAL PRIMARY KEY,
    id_sport INT NOT NULL,
    nom_equipe VARCHAR(50) NOT NULL,
    CONSTRAINT fk_equipe_sport FOREIGN KEY (id_sport) REFERENCES Sport(id_sport)
);

CREATE TABLE Coach (
    id_utilisateur INT PRIMARY KEY,
    id_sport INT NOT NULL,
    id_equipe INT NOT NULL,
    titre VARCHAR(50),
    CONSTRAINT fk_coach_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT fk_coach_sport FOREIGN KEY (id_sport) REFERENCES Sport(id_sport),
    CONSTRAINT fk_coach_equipe FOREIGN KEY (id_equipe) REFERENCES Equipe(id_equipe)
);

-- ==========================================
-- 4. ATHLETES & RELATIONSHIPS
-- ==========================================

CREATE TABLE Athlete (
    id_utilisateur INT PRIMARY KEY,
    date_naissance DATE NOT NULL,
    sexe VARCHAR(10) NOT NULL,
    taille_metre INT, -- Represented as integer in diagram (e.g., centimeters or scaled)
    poids_kg NUMERIC(4,1),
    bras_dominant VARCHAR(8),
    jambe_dominant VARCHAR(6),
    historique_blessure TEXT, -- Using TEXT for CLOB compatibility
    CONSTRAINT fk_athlete_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT chk_sexe CHECK (sexe IN ('Féminin', 'Masculin')),
    CONSTRAINT chk_bras CHECK (bras_dominant IN ('Droit', 'Gauche')),
    CONSTRAINT chk_jambe CHECK (jambe_dominant IN ('Droit', 'Gauche'))
);

-- Intermediary table mapping Athletes to Sports with optional Position/Discipline
CREATE TABLE Athlete_Sport (
    id_athlete INT NOT NULL,
    id_sport INT NOT NULL,
    id_position INT, -- Nullable: depends on the sport type
    id_discipline INT, -- Nullable: depends on the sport type
    PRIMARY KEY (id_athlete, id_sport),
    CONSTRAINT fk_athlete_sport_athlete FOREIGN KEY (id_athlete) REFERENCES Athlete(id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT fk_athlete_sport_sport FOREIGN KEY (id_sport) REFERENCES Sport(id_sport) ON DELETE CASCADE,
    CONSTRAINT fk_athlete_sport_position FOREIGN KEY (id_position) REFERENCES Position(id_position),
    CONSTRAINT fk_athlete_sport_discipline FOREIGN KEY (id_discipline) REFERENCES Discipline(id_discipline)
);

-- ==========================================
-- 5. TESTING & PERFORMANCE
-- ==========================================

CREATE TABLE Batterie_Test (
    id_batterie_test SERIAL PRIMARY KEY,
    nom_batterie_test VARCHAR(20) NOT NULL,
    id_equipe INT, -- Nullable (indicated by N next to FK)
    id_groupe INT, -- Nullable (indicated by N next to FK)
    CONSTRAINT fk_batterie_equipe FOREIGN KEY (id_equipe) REFERENCES Equipe(id_equipe) ON DELETE SET NULL,
    CONSTRAINT fk_batterie_groupe FOREIGN KEY (id_groupe) REFERENCES Groupe(id_groupe) ON DELETE SET NULL
);

CREATE TABLE Tests (
    id_test SERIAL PRIMARY KEY,
    nom_test VARCHAR(20) NOT NULL,
    unite_mesure VARCHAR(10) NOT NULL,
    protocole TEXT -- Using TEXT for CLOB compatibility
);

CREATE TABLE Resultat (
    id_test INT NOT NULL,
    id_athlete INT NOT NULL,
    resultat VARCHAR(10) NOT NULL,
    preuve_video TEXT,
    preuve_photo TEXT,
    statut VARCHAR(25) NOT NULL DEFAULT 'En cours d''approbation',
    commentaire TEXT,
    PRIMARY KEY (id_test, id_athlete),
    CONSTRAINT fk_resultat_test FOREIGN KEY (id_test) REFERENCES Tests(id_test) ON DELETE CASCADE,
    CONSTRAINT fk_resultat_athlete FOREIGN KEY (id_athlete) REFERENCES Athlete(id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT chk_statut_resultat CHECK (statut IN ('Accepté', 'Refusé', 'En cours d''approbation'))
);

CREATE TABLE Test_Sport (
    id_test INT NOT NULL,
    id_sport INT NOT NULL,
    PRIMARY KEY (id_test, id_sport),
    CONSTRAINT fk_test_sport_test FOREIGN KEY (id_test) REFERENCES Tests(id_test) ON DELETE CASCADE,
    CONSTRAINT fk_test_sport_sport FOREIGN KEY (id_sport) REFERENCES Sport(id_sport) ON DELETE CASCADE
);