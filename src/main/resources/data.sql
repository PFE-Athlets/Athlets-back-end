-- ============================================================================
-- CLEAR CURRENT DATA (Safe Ordering via CASCADE)
-- ============================================================================
TRUNCATE TABLE User_Account CASCADE;
TRUNCATE TABLE Sport CASCADE;
TRUNCATE TABLE Group_Table CASCADE;

-- ============================================================================
-- 1. REFERENCE TABLES & INDEPENDENT ENTITIES
-- ============================================================================
INSERT INTO Sport (name) VALUES 
('Volleyball'),
('Athlétisme'),
('Rugby'),
('Hockey'),
('Cross-Country'),
('Flag-Football'),
('Badminton'),
('Golf');

INSERT INTO Position (sport_id, name) VALUES
((SELECT id FROM Sport WHERE name = 'Flag-Football'), 'Quart-arrière'),
((SELECT id FROM Sport WHERE name = 'Flag-Football'), 'Centre/snapper'),
((SELECT id FROM Sport WHERE name = 'Flag-Football'), 'Receveur'),
((SELECT id FROM Sport WHERE name = 'Flag-Football'), 'Porteur de ballon'),
((SELECT id FROM Sport WHERE name = 'Flag-Football'), 'Rusher'),
((SELECT id FROM Sport WHERE name = 'Flag-Football'), 'Demi défensif'),
((SELECT id FROM Sport WHERE name = 'Flag-Football'), 'Safety'),
((SELECT id FROM Sport WHERE name = 'Flag-Football'), 'Linebacker'),
((SELECT id FROM Sport WHERE name = 'Hockey'), 'Gardien'),
((SELECT id FROM Sport WHERE name = 'Hockey'), 'Défenseur gauche'),
((SELECT id FROM Sport WHERE name = 'Hockey'), 'Défenseur droit'),
((SELECT id FROM Sport WHERE name = 'Hockey'), 'Centre'),
((SELECT id FROM Sport WHERE name = 'Hockey'), 'Ailier gauche'),
((SELECT id FROM Sport WHERE name = 'Hockey'), 'Ailier droit'),
((SELECT id FROM Sport WHERE name = 'Rugby'), 'Pilier'),
((SELECT id FROM Sport WHERE name = 'Rugby'), 'Talonneur'),
((SELECT id FROM Sport WHERE name = 'Rugby'), 'Deuxième ligne'),
((SELECT id FROM Sport WHERE name = 'Rugby'), 'Troisième ligne'),
((SELECT id FROM Sport WHERE name = 'Rugby'), 'Demi de mêlée'),
((SELECT id FROM Sport WHERE name = 'Rugby'), 'Demi d’ouverture'),
((SELECT id FROM Sport WHERE name = 'Rugby'), 'Centre'),
((SELECT id FROM Sport WHERE name = 'Rugby'), 'Ailier'),
((SELECT id FROM Sport WHERE name = 'Rugby'), 'Arrière'),
((SELECT id FROM Sport WHERE name = 'Volleyball'), 'Passeur'),
((SELECT id FROM Sport WHERE name = 'Volleyball'), 'Attaquant extérieur'),
((SELECT id FROM Sport WHERE name = 'Volleyball'), 'Opposé'),
((SELECT id FROM Sport WHERE name = 'Volleyball'), 'Central'),
((SELECT id FROM Sport WHERE name = 'Volleyball'), 'Libéro'),
((SELECT id FROM Sport WHERE name = 'Volleyball'), 'Spécialiste défensif');

INSERT INTO Discipline (sport_id, name) VALUES
((SELECT id FROM Sport WHERE name = 'Athlétisme'), 'Sprint'),
((SELECT id FROM Sport WHERE name = 'Athlétisme'), 'Demi-fond'),
((SELECT id FROM Sport WHERE name = 'Athlétisme'), 'Fond'),
((SELECT id FROM Sport WHERE name = 'Athlétisme'), 'Haies'),
((SELECT id FROM Sport WHERE name = 'Athlétisme'), 'Saut en longueur'),
((SELECT id FROM Sport WHERE name = 'Athlétisme'), 'Saut en hauteur'),
((SELECT id FROM Sport WHERE name = 'Athlétisme'), 'Triple saut'),
((SELECT id FROM Sport WHERE name = 'Athlétisme'), 'Saut à la perche'),
((SELECT id FROM Sport WHERE name = 'Athlétisme'), 'Lancer du poids'),
((SELECT id FROM Sport WHERE name = 'Athlétisme'), 'Lancer du disque'),
((SELECT id FROM Sport WHERE name = 'Athlétisme'), 'Lancer du javelot'),
((SELECT id FROM Sport WHERE name = 'Athlétisme'), 'Lancer du marteau'),
((SELECT id FROM Sport WHERE name = 'Athlétisme'), 'Épreuves combinées');

INSERT INTO Group_Table (name) VALUES
('Elite Men'),
('Elite Women'),
('Development');

-- ============================================================================
-- 2. CORE USER ACCOUNTS (Explicit IDs to prevent composite key alignment drift)
-- ============================================================================
INSERT INTO User_Account (first_name, last_name, email, phone, username, password, account_status, access_level) VALUES 
('Zacharie', 'Morin', 'zmorin0@etsmtl.ca', '514-555-0101', 'zmorin0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1), --Password: admin1
('A.', 'Bun', 'abun0@etsmtl.ca', NULL, 'abun0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1),
('M.', 'Ambeault', 'mambeault0@etsmtl.ca', NULL, 'mambeault0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1),
('E.', 'Laforce', 'elaforce0@etsmtl.ca', NULL, 'elaforce0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1),
('L.', 'Seguin', 'lseguin0@etsmtl.ca', NULL, 'lseguin0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1),

('Coach', 'Volleyball', 'coach.volleyball@etsmtl.ca', NULL, 'coach-volleyball', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 2),
('Coach', 'Athlétisme', 'coach.athletisme@etsmtl.ca', NULL, 'coach-athletics', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 2),
('Coach', 'Rugby', 'coach.rugby@etsmtl.ca', NULL, 'coach-rugby', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 2),
('Coach', 'Hockey', 'coach.hockey@etsmtl.ca', NULL, 'coach-hockey', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 2),
('Coach', 'Cross-Country', 'coach.crosscountry@etsmtl.ca', NULL, 'coach-cross-country', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 2),
('Coach', 'Flag-Football', 'coach.flagfootball@etsmtl.ca', NULL, 'coach-flag-football', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 2),
('Coach', 'Badminton', 'coach.badminton@etsmtl.ca', NULL, 'coach-badminton', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 2),

('Track', 'User1', 'trackuser1@etsmtl.ca', NULL, 'trackUser1', '$2a$10$zJbaow2rXTObDtnNmcgbdu9.ufmVcTo6JRAypSkTAetF6HoLAkGva', 'Active', 3), --Password: password
('Track', 'User2', 'trackuser2@etsmtl.ca', NULL, 'trackUser2', '$2a$10$zJbaow2rXTObDtnNmcgbdu9.ufmVcTo6JRAypSkTAetF6HoLAkGva', 'Active', 3),
('Track', 'User3', 'trackuser3@etsmtl.ca', NULL, 'trackUser3', '$2a$10$zJbaow2rXTObDtnNmcgbdu9.ufmVcTo6JRAypSkTAetF6HoLAkGva', 'Active', 3);

-- ============================================================================
-- 3. SUB-ROLE INHERITANCE ENTITIES (Admin, Coach, Athlete)
-- ============================================================================
INSERT INTO Administrator (user_id, access_level, title) VALUES 
((SELECT id FROM User_Account WHERE username = 'zmorin0'), 1, 'Head System Administrator'),
((SELECT id FROM User_Account WHERE username = 'abun0'), 1, 'Data Coordinator'),
((SELECT id FROM User_Account WHERE username = 'mambeault0'), 1, 'Operations Manager'),
((SELECT id FROM User_Account WHERE username = 'elaforce0'), 1, 'Technical Director'),
((SELECT id FROM User_Account WHERE username = 'lseguin0'), 1, 'Support Specialist');

INSERT INTO Team (sport_id, name) VALUES 
((SELECT id FROM Sport WHERE name = 'Volleyball'), 'Piranhas Volleyball'),
((SELECT id FROM Sport WHERE name = 'Athlétisme'), 'Piranhas Athlétisme'),
((SELECT id FROM Sport WHERE name = 'Rugby'), 'Piranhas Rugby'),
((SELECT id FROM Sport WHERE name = 'Hockey'), 'Piranhas Hockey'),
((SELECT id FROM Sport WHERE name = 'Cross-Country'), 'Piranhas Cross-Country'),
((SELECT id FROM Sport WHERE name = 'Flag-Football'), 'Piranhas Flag-Football'),
((SELECT id FROM Sport WHERE name = 'Badminton'), 'Piranhas Badminton');

INSERT INTO Coach (user_id, access_level, sport_id, team_id, title, is_head_coach) VALUES 
((SELECT id FROM User_Account WHERE username = 'coach-volleyball'), 2, (SELECT id FROM Sport WHERE name = 'Volleyball'), (SELECT id FROM Team WHERE name = 'Piranhas Volleyball'), 'Head Volleyball Coach', true),
((SELECT id FROM User_Account WHERE username = 'coach-athletics'), 2, (SELECT id FROM Sport WHERE name = 'Athlétisme'), (SELECT id FROM Team WHERE name = 'Piranhas Athlétisme'), 'Head Track & Field Coach', true),
((SELECT id FROM User_Account WHERE username = 'coach-rugby'), 2, (SELECT id FROM Sport WHERE name = 'Rugby'), (SELECT id FROM Team WHERE name = 'Piranhas Rugby'), 'Head Rugby Coach', true),
((SELECT id FROM User_Account WHERE username = 'coach-hockey'), 2, (SELECT id FROM Sport WHERE name = 'Hockey'), (SELECT id FROM Team WHERE name = 'Piranhas Hockey'), 'Head Hockey Coach', true),
((SELECT id FROM User_Account WHERE username = 'coach-cross-country'), 2, (SELECT id FROM Sport WHERE name = 'Cross-Country'), (SELECT id FROM Team WHERE name = 'Piranhas Cross-Country'), 'Head XC Coach', true),
((SELECT id FROM User_Account WHERE username = 'coach-flag-football'), 2, (SELECT id FROM Sport WHERE name = 'Flag-Football'), (SELECT id FROM Team WHERE name = 'Piranhas Flag-Football'), 'Head Flag-Football Coach', true),
((SELECT id FROM User_Account WHERE username = 'coach-badminton'), 2, (SELECT id FROM Sport WHERE name = 'Badminton'), (SELECT id FROM Team WHERE name = 'Piranhas Badminton'), 'Head Badminton Coach', true);

INSERT INTO Athlete (user_id, access_level, birth_date, gender, height_meters, weight_kg, dominant_arm, dominant_leg, injury_history) VALUES 
((SELECT id FROM User_Account WHERE username = 'trackUser1'), 3, '2002-03-11', 'Male', 2, 74.0, 'Right', 'Right', 'None'),
((SELECT id FROM User_Account WHERE username = 'trackUser2'), 3, '2003-07-22', 'Female', 1, 61.2, 'Left', 'Right', 'Slight hamstring pull 2025'),
((SELECT id FROM User_Account WHERE username = 'trackUser3'), 3, '2002-11-05', 'Male', 2, 80.1, 'Right', 'Left', 'None');

INSERT INTO Athlete_Team (athlete_id, team_id) VALUES 
((SELECT id FROM User_Account WHERE username = 'trackUser1'), (SELECT id FROM Team WHERE name = 'Piranhas Athlétisme')),
((SELECT id FROM User_Account WHERE username = 'trackUser2'), (SELECT id FROM Team WHERE name = 'Piranhas Athlétisme')),
((SELECT id FROM User_Account WHERE username = 'trackUser3'), (SELECT id FROM Team WHERE name = 'Piranhas Athlétisme'));

INSERT INTO Athlete_Team_Discipline (athlete_id, team_id, discipline_id) VALUES
((SELECT id FROM User_Account WHERE username = 'trackUser1'), 
 (SELECT id FROM Team WHERE name = 'Piranhas Athlétisme'), 
 (SELECT id FROM Discipline WHERE name = 'Sprint')),

((SELECT id FROM User_Account WHERE username = 'trackUser1'), 
 (SELECT id FROM Team WHERE name = 'Piranhas Athlétisme'), 
 (SELECT id FROM Discipline WHERE name = 'Haies')),

((SELECT id FROM User_Account WHERE username = 'trackUser1'), 
 (SELECT id FROM Team WHERE name = 'Piranhas Athlétisme'), 
 (SELECT id FROM Discipline WHERE name = 'Saut en longueur'));

-- ============================================================================
-- 4. TESTS & PERFORMANCE RESULTS
-- ============================================================================
-- ============================================================================
-- 5. Batteries de test 
-- ============================================================================

INSERT INTO Unite_Mesure (nom, symbole) VALUES 
('Kilogramme', 'kg'),
('Centimètre', 'cm'),
('Seconde', 's'),
('Minute', 'min'),
('Heure', 'h'),
('Répétition', 'rep'),
('Pourcentage', '%'),
('Watt', 'W'),
('Pouce', 'po'),
('Mètre', 'm'),
('Livre', 'lb'),
('Newton-seconde', 'N.s'),
('Newton par kilogramme', 'N/kg'),
('Watt par kilogramme', 'W/kg'),
('Kilomètre par heure', 'km/h'),
('Mètre par seconde', 'm/s'),
('Battement par minute', 'bpm');

INSERT INTO Qualite_Physique (nom) VALUES 
('Force maximale des membres inférieurs'),
('Force maximale des membres supérieurs'),
('Puissance verticale'),
('Puissance horizontale'),
('Vitesse'),
('Agilité'),
('Endurance anaérobie'),
('Composition corporelle'),
('Endurance aérobie'),
('Mobilité'),
('Équilibre'),
('Coordination');
