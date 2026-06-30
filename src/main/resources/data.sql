-- ============================================================================
-- CLEAR CURRENT DATA (Safe Ordering via CASCADE)
-- ============================================================================
TRUNCATE TABLE User_Account CASCADE;
TRUNCATE TABLE Sport CASCADE;
TRUNCATE TABLE Group_Table CASCADE;

-- ============================================================================
-- 1. REFERENCE TABLES & INDEPENDENT ENTITIES
-- ============================================================================
INSERT INTO Sport (id, name) VALUES 
(4, 'Volleyball'),
(5, 'Athlétisme'),
(6, 'Rugby'),
(7, 'Hockey'),
(8, 'Cross-Country'),
(9, 'Flag-Football'),
(10, 'Badminton');

SELECT setval('sport_id_seq', (SELECT MAX(id) FROM Sport));

-- Position entries removed as they were uniquely attached to Cycling and Swimming

INSERT INTO Discipline (id, sport_id, name) VALUES 
(5, 5, '100m Sprint'),
(6, 5, 'Long Jump');

SELECT setval('discipline_id_seq', (SELECT MAX(id) FROM Discipline));

INSERT INTO Group_Table (id, name) VALUES 
(1, 'Elite Men'),
(2, 'Elite Women'),
(3, 'Development');

SELECT setval('group_table_id_seq', (SELECT MAX(id) FROM Group_Table));

-- ============================================================================
-- 2. CORE USER ACCOUNTS (Explicit IDs to prevent composite key alignment drift)
-- ============================================================================
INSERT INTO User_Account (id, first_name, last_name, email, phone, username, password, account_status, access_level) VALUES 
(1, 'Zacharie', 'Morin', 'zmorin0@etsmtl.ca', '514-555-0101', 'zmorin0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1), --Password: admin1
(2, 'A.', 'Bun', 'abun0@etsmtl.ca', NULL, 'abun0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1),
(3, 'M.', 'Ambeault', 'mambeault0@etsmtl.ca', NULL, 'mambeault0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1),
(4, 'E.', 'Laforce', 'elaforce0@etsmtl.ca', NULL, 'elaforce0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1),
(5, 'L.', 'Seguin', 'lseguin0@etsmtl.ca', NULL, 'lseguin0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1),

(8, 'Coach', 'Volleyball', 'coach.volleyball@etsmtl.ca', NULL, 'coach-volleyball', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 2),
(9, 'Coach', 'Athlétisme', 'coach.athletisme@etsmtl.ca', NULL, 'coach-athletics', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 2),
(10, 'Coach', 'Rugby', 'coach.rugby@etsmtl.ca', NULL, 'coach-rugby', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 2),
(11, 'Coach', 'Hockey', 'coach.hockey@etsmtl.ca', NULL, 'coach-hockey', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 2),
(12, 'Coach', 'Cross-Country', 'coach.crosscountry@etsmtl.ca', NULL, 'coach-cross-country', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 2),
(13, 'Coach', 'Flag-Football', 'coach.flagfootball@etsmtl.ca', NULL, 'coach-flag-football', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 2),
(14, 'Coach', 'Badminton', 'coach.badminton@etsmtl.ca', NULL, 'coach-badminton', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 2),

(15, 'Track', 'User1', 'trackuser1@etsmtl.ca', NULL, 'trackUser1', '$2a$10$zJbaow2rXTObDtnNmcgbdu9.ufmVcTo6JRAypSkTAetF6HoLAkGva', 'Active', 3), --Password: password
(16, 'Track', 'User2', 'trackuser2@etsmtl.ca', NULL, 'trackUser2', '$2a$10$zJbaow2rXTObDtnNmcgbdu9.ufmVcTo6JRAypSkTAetF6HoLAkGva', 'Active', 3),
(17, 'Track', 'User3', 'trackuser3@etsmtl.ca', NULL, 'trackUser3', '$2a$10$zJbaow2rXTObDtnNmcgbdu9.ufmVcTo6JRAypSkTAetF6HoLAkGva', 'Active', 3);

SELECT setval('user_account_id_seq', (SELECT MAX(id) FROM User_Account));

-- ============================================================================
-- 3. SUB-ROLE INHERITANCE ENTITIES (Admin, Coach, Athlete)
-- ============================================================================
INSERT INTO Administrator (user_id, access_level, title) VALUES 
(1, 1, 'Head System Administrator'),
(2, 1, 'Data Coordinator'),
(3, 1, 'Operations Manager'),
(4, 1, 'Technical Director'),
(5, 1, 'Support Specialist');

INSERT INTO Team (id, sport_id, name) VALUES 
(3, 4, 'Piranhas Volleyball'),
(4, 5, 'Piranhas Athlétisme'),
(5, 6, 'Piranhas Rugby'),
(6, 7, 'Piranhas Hockey'),
(7, 8, 'Piranhas Cross-Country'),
(8, 9, 'Piranhas Flag-Football'),
(9, 10, 'Piranhas Badminton');

SELECT setval('team_id_seq', (SELECT MAX(id) FROM Team));

INSERT INTO Coach (user_id, access_level, sport_id, team_id, title) VALUES 
(8, 2, 4, 3, 'Head Volleyball Coach'),
(9, 2, 5, 4, 'Head Track & Field Coach'),
(10, 2, 6, 5, 'Head Rugby Coach'),
(11, 2, 7, 6, 'Head Hockey Coach'),
(12, 2, 8, 7, 'Head XC Coach'),
(13, 2, 9, 8, 'Head Flag-Football Coach'),
(14, 2, 10, 9, 'Head Badminton Coach');

INSERT INTO Athlete (user_id, access_level, birth_date, gender, height_meters, weight_kg, dominant_arm, dominant_leg, injury_history) VALUES 
(15, 3, '2002-03-11', 'Male', 2, 74.0, 'Right', 'Right', 'None'),
(16, 3, '2003-07-22', 'Female', 1, 61.2, 'Left', 'Right', 'Slight hamstring pull 2025'),
(17, 3, '2002-11-05', 'Male', 2, 80.1, 'Right', 'Left', 'None');

INSERT INTO Athlete_Team (athlete_id, team_id, position_id, discipline_id) VALUES 
(15, 4, NULL, 5), -- Track User 1 explicitly linked to 100m Sprint discipline
(16, 4, NULL, NULL),
(17, 4, NULL, NULL);

-- ============================================================================
-- 4. TESTS & PERFORMANCE RESULTS
-- ============================================================================
-- Re-indexed from 1 to 5 to prevent sequence drift and script statement errors
INSERT INTO Test (id, name, unit_of_measure, protocol, proof_needed) VALUES 
(1, '1RM Back Squat', 'Kg', 'Maximum weight lifted for one repetition cleanly. (Force)', 'Photo'),
(2, 'Beep Test', 'Repetitions', 'Multi-stage 20m shuttle run test to volitional exhaustion. (Endurance)', 'None'),
(3, '30m Sprint', 'Seconds', 'Electronic timing gates or video analysis from stationary start. (Vitesse)', 'Video'),
(4, 'Pro Agility 5-10-5', 'Seconds', 'Lateral shuttle running tracking quick change of direction. (Agilite)', 'None'),
(5, 'Sit and Reach', 'Metres', 'Standard flexibility box baseline metrics. (Souplesse)', 'Photo');

SELECT setval('test_id_seq', (SELECT MAX(id) FROM Test));

INSERT INTO Test_Sport (test_id, sport_id) VALUES 
(1, 5), -- Force test
(2, 5), -- Endurance test
(3, 5), -- Vitesse test
(4, 5), -- Agilite test
(5, 5); -- Souplesse test

INSERT INTO Result (test_id, athlete_id, result_value, status, test_date, comment_text) VALUES 
(1, 15, '140', 'Approved', '2026-06-10', 'Exceeded personal best by 5kg.'),
(1, 15, '145', 'Rejected', '2026-06-12', 'Depth was incomplete during lift attempt.'),
(2, 15, '11.5', 'Approved', '2026-06-15', 'Great conditioning phase results.'),
(3, 15, '3.88', 'Approved', '2026-06-18', 'Strong block start mechanics.'),
(3, 15, '3.82', 'Pending approval', '2026-06-29', 'Awaiting video validation review by coach.'),
(4, 15, '4.25', 'Approved', '2026-06-20', 'Good footwork on secondary cut.'),
(4, 15, NULL, 'Assigned', '2026-06-30', 'Scheduled for upcoming testing block.'),
(5, 15, '0.18', 'Pending approval', '2026-06-30', 'Improving lower back flexibility.');

SELECT setval('result_id_seq', (SELECT MAX(id) FROM Result));