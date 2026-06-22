DROP TABLE IF EXISTS Result CASCADE;
DROP TABLE IF EXISTS Test CASCADE;
DROP TABLE IF EXISTS Test_Battery CASCADE;
DROP TABLE IF EXISTS Athlete_Sport CASCADE;
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
    CONSTRAINT fk_position_sport FOREIGN KEY (sport_id) REFERENCES Sport(id) ON DELETE CASCADE
);

CREATE TABLE Discipline (
    id SERIAL PRIMARY KEY,
    sport_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_discipline_sport FOREIGN KEY (sport_id) REFERENCES Sport(id) ON DELETE CASCADE
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
    access_level INT NOT NULL,
    CONSTRAINT chk_account_status CHECK (account_status IN ('Active', 'Inactive')),
    CONSTRAINT chk_access_level CHECK (access_level IN (1, 2, 3)) 
    -- 1: Administrator, 2: Coach, 3: Athlete
);

CREATE TABLE Administrator (
    user_id INT PRIMARY KEY,
    title VARCHAR(50),
    CONSTRAINT fk_admin_user FOREIGN KEY (user_id) REFERENCES User_Account(id) ON DELETE CASCADE
);

-- ==========================================
-- 3. TEAMS & STAFF
-- ==========================================

CREATE TABLE Team (
    id SERIAL PRIMARY KEY,
    sport_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT fk_team_sport FOREIGN KEY (sport_id) REFERENCES Sport(id)
);

CREATE TABLE Coach (
    user_id INT PRIMARY KEY,
    sport_id INT NOT NULL,
    team_id INT NOT NULL,
    title VARCHAR(50),
    CONSTRAINT fk_coach_user FOREIGN KEY (user_id) REFERENCES User_Account(id) ON DELETE CASCADE,
    CONSTRAINT fk_coach_sport FOREIGN KEY (sport_id) REFERENCES Sport(id),
    CONSTRAINT fk_coach_team FOREIGN KEY (team_id) REFERENCES Team(id)
);

-- ==========================================
-- 4. ATHLETES & RELATIONSHIPS
-- ==========================================

CREATE TABLE Athlete (
    user_id INT PRIMARY KEY,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    height_meters INT,
    weight_kg NUMERIC(4,1),
    dominant_arm VARCHAR(8),
    dominant_leg VARCHAR(6),
    injury_history TEXT,
    CONSTRAINT fk_athlete_user FOREIGN KEY (user_id) REFERENCES User_Account(id) ON DELETE CASCADE,
    CONSTRAINT chk_gender CHECK (gender IN ('Female', 'Male')),
    CONSTRAINT chk_arm CHECK (dominant_arm IN ('Right', 'Left')),
    CONSTRAINT chk_leg CHECK (dominant_leg IN ('Right', 'Left'))
);

-- Intermediary table mapping Athletes to Sports with optional Position/Discipline
CREATE TABLE Athlete_Sport (
    athlete_id INT NOT NULL,
    sport_id INT NOT NULL,
    position_id INT,
    discipline_id INT,
    PRIMARY KEY (athlete_id, sport_id),
    CONSTRAINT fk_athlete_sport_athlete FOREIGN KEY (athlete_id) REFERENCES Athlete(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_athlete_sport_sport FOREIGN KEY (sport_id) REFERENCES Sport(id) ON DELETE CASCADE,
    CONSTRAINT fk_athlete_sport_position FOREIGN KEY (position_id) REFERENCES Position(id),
    CONSTRAINT fk_athlete_sport_discipline FOREIGN KEY (discipline_id) REFERENCES Discipline(id)
);

-- ==========================================
-- 5. TESTING & PERFORMANCE
-- ==========================================

CREATE TABLE Test_Battery (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    team_id INT,
    group_id INT,
    CONSTRAINT fk_battery_team FOREIGN KEY (team_id) REFERENCES Team(id) ON DELETE SET NULL,
    CONSTRAINT fk_battery_group FOREIGN KEY (group_id) REFERENCES Group_Table(id) ON DELETE SET NULL
);

CREATE TABLE Test (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    unit_of_measure VARCHAR(10) NOT NULL,
    protocol TEXT,
    proof_needed VARCHAR(20)
);

CREATE TABLE Result (
    test_id INT NOT NULL,
    athlete_id INT NOT NULL,
    result_value VARCHAR(10) NOT NULL,
    video_proof TEXT,
    photo_proof TEXT,
    status VARCHAR(25) NOT NULL DEFAULT 'Pending approval',
    comment_text TEXT,
    PRIMARY KEY (test_id, athlete_id),
    CONSTRAINT fk_result_test FOREIGN KEY (test_id) REFERENCES Test(id) ON DELETE CASCADE,
    CONSTRAINT fk_result_athlete FOREIGN KEY (athlete_id) REFERENCES Athlete(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_result_status CHECK (status IN ('Accepted', 'Rejected', 'Pending approval'))
);

CREATE TABLE Test_Sport (
    test_id INT NOT NULL,
    sport_id INT NOT NULL,
    PRIMARY KEY (test_id, sport_id),
    CONSTRAINT fk_test_sport_test FOREIGN KEY (test_id) REFERENCES Test(id) ON DELETE CASCADE,
    CONSTRAINT fk_test_sport_sport FOREIGN KEY (sport_id) REFERENCES Sport(id) ON DELETE CASCADE
);