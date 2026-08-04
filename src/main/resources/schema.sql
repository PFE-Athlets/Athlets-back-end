DROP TABLE IF EXISTS Battery_Test CASCADE;
DROP TABLE IF EXISTS Battery CASCADE;
DROP TABLE IF EXISTS Result_Value CASCADE;
DROP TABLE IF EXISTS Result CASCADE;
DROP TABLE IF EXISTS Test_Equipment CASCADE;
DROP TABLE IF EXISTS Equipment CASCADE;
DROP TABLE IF EXISTS Result_Type CASCADE;
DROP TABLE IF EXISTS Tests CASCADE;
DROP TABLE IF EXISTS Unit_Measure CASCADE;
DROP TABLE IF EXISTS Physical_Quality CASCADE;
DROP TABLE IF EXISTS Result CASCADE;
DROP TABLE IF EXISTS Test CASCADE;
DROP TABLE IF EXISTS Test_Battery CASCADE;
DROP TABLE IF EXISTS Athlete_Team CASCADE;
DROP TABLE IF EXISTS Kine_Team CASCADE;
DROP TABLE IF EXISTS Athlete CASCADE;
DROP TABLE IF EXISTS Coach CASCADE;
DROP TABLE IF EXISTS Kine CASCADE;
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
    access_level INT NOT NULL, -- 1: Administrator, 2: Coach, 3: Athlete, 4: Kinesiologist
    CONSTRAINT chk_account_status CHECK (account_status IN ('Active', 'Inactive','Pending')),
    CONSTRAINT chk_access_level CHECK (access_level IN (1, 2, 3, 4)),
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

CREATE TABLE Kine (
    user_id INT PRIMARY KEY,
    access_level INT NOT NULL DEFAULT 4,

    CONSTRAINT chk_is_kine CHECK (access_level = 4),
    CONSTRAINT fk_kine_user FOREIGN KEY (user_id, access_level) 
        REFERENCES User_Account(id, access_level) ON DELETE CASCADE
);

CREATE TABLE Kine_Team (
    user_id INT,
    team_id INT,
    
    PRIMARY KEY (user_id, team_id),
    CONSTRAINT fk_kine_user_id FOREIGN KEY (user_id) REFERENCES User_Account(id) ON DELETE CASCADE,
    CONSTRAINT fk_kine_team_id FOREIGN KEY (team_id) REFERENCES Team(id) ON DELETE CASCADE
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
-- ============================================================================
-- 5. BATTERIES DE TEST
-- ============================================================================

CREATE TABLE Physical_Quality (
    id_physical_quality SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL CONSTRAINT uq_physical_quality_name UNIQUE,
    description VARCHAR(500)
);

CREATE TABLE Unit_Measure (
    id_unit SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL CONSTRAINT uq_unit_measure_name UNIQUE,
    symbole VARCHAR(20) NOT NULL CONSTRAINT uq_unit_measure_symbole UNIQUE
);

CREATE TABLE Tests (
    id_test SERIAL PRIMARY KEY,
    id_physical_quality INT NOT NULL,
    test_name VARCHAR(100) NOT NULL CONSTRAINT uq_tests_name UNIQUE,
    protocole TEXT NOT NULL,
    supervised BOOLEAN DEFAULT FALSE NOT NULL,
    informations TEXT,
    proof_required BOOLEAN DEFAULT FALSE NOT NULL,

    CONSTRAINT fk_tests_physical_quality
        FOREIGN KEY (id_physical_quality)
        REFERENCES Physical_Quality(id_physical_quality)
);

CREATE TABLE Result_Type (
    id_result_type SERIAL PRIMARY KEY,
    id_test INT NOT NULL,
    id_unit_measure INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    data_type VARCHAR(20) DEFAULT 'DECIMAL' NOT NULL,

    CONSTRAINT fk_result_type_test
        FOREIGN KEY (id_test)
        REFERENCES Tests(id_test)
        ON DELETE CASCADE,

    CONSTRAINT fk_result_type_unit
        FOREIGN KEY (id_unit_measure)
        REFERENCES Unit_Measure(id_unit),

    CONSTRAINT uq_result_type_test_name
        UNIQUE (id_test, name),

    CONSTRAINT ck_result_type_type
        CHECK (
            data_type IN (
                'INTEGER',
                'DECIMAL',
                'TEXT',
                'BOOLEAN'
            )
        )
);

CREATE TABLE Equipment (
    id_equipment SERIAL PRIMARY KEY,
    name_equipment VARCHAR(100) NOT NULL CONSTRAINT uq_equipment_name UNIQUE
);

CREATE TABLE Test_Equipment (
    id_test INT NOT NULL,
    id_equipment INT NOT NULL,
    required_quantity INT DEFAULT 1 NOT NULL,

    CONSTRAINT pk_test_equipment
        PRIMARY KEY (id_test, id_equipment),

    CONSTRAINT fk_test_equipment_test
        FOREIGN KEY (id_test)
        REFERENCES Tests(id_test)
        ON DELETE CASCADE,

    CONSTRAINT fk_test_equipment_equipment
        FOREIGN KEY (id_equipment)
        REFERENCES Equipment(id_equipment),

    CONSTRAINT ck_test_equipment_quantity
        CHECK (required_quantity >= 1)
);

CREATE TABLE Result (
    id_result SERIAL PRIMARY KEY,
    id_test INT NOT NULL,
    id_athlete INT NOT NULL,
    proof VARCHAR(500),
    status VARCHAR(20) DEFAULT 'Assigned' NOT NULL,
    comment TEXT,
    date_result DATE DEFAULT CURRENT_DATE NOT NULL,

    CONSTRAINT fk_result_test
        FOREIGN KEY (id_test)
        REFERENCES Tests(id_test),

    CONSTRAINT fk_result_athlete
        FOREIGN KEY (id_athlete)
        REFERENCES Athlete(user_id),

    CONSTRAINT ck_result_status
        CHECK (
            status IN (
                'Assigned',
                'Accepted',
                'Rejected',
                'Pending approval'
            )
        )
);

CREATE TABLE Result_Value (
    id_result_value SERIAL PRIMARY KEY,
    id_result INT NOT NULL,
    id_result_type INT NOT NULL,
    value NUMERIC(10,2) NOT NULL,

    CONSTRAINT fk_value_result_result
        FOREIGN KEY (id_result)
        REFERENCES Result(id_result)
        ON DELETE CASCADE,

    CONSTRAINT fk_value_result_type
        FOREIGN KEY (id_result_type)
        REFERENCES Result_Type(id_result_type),

    CONSTRAINT uq_valeur_result_result_type
        UNIQUE (id_result, id_result_type)
);

CREATE TABLE Battery (
    id_battery SERIAL PRIMARY KEY,
    id_team INT NOT NULL,
    name_battery VARCHAR(100) NOT NULL,
    status BOOLEAN DEFAULT FALSE NOT NULL,

    CONSTRAINT fk_battery_team
        FOREIGN KEY (id_team)
        REFERENCES Team(id),

    CONSTRAINT uq_battery_team_name
        UNIQUE (id_team, name_battery)
);

CREATE TABLE Battery_Test (
    id_battery INT NOT NULL,
    id_test INT NOT NULL,

    CONSTRAINT pk_battery_test
        PRIMARY KEY (id_battery, id_test),

    CONSTRAINT fk_battery
        FOREIGN KEY (id_battery)
        REFERENCES Battery(id_battery)
        ON DELETE CASCADE,

    CONSTRAINT fk_test
        FOREIGN KEY (id_test)
        REFERENCES Tests(id_test)
);