TRUNCATE TABLE user_account CASCADE;

INSERT INTO user_account (
    first_name, 
    last_name, 
    email, 
    phone, 
    username, 
    password, 
    account_status, 
    access_level
) VALUES 
('Zacharie', 'Morin', 'zmorin0@etsmtl.ca', '514-555-0101', 'zmorin0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1),    -- admin1 (Admin)
('A.', 'Bun', 'abun0@etsmtl.ca', NULL, 'abun0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1),                     -- admin1 (Admin)
('M.', 'Ambeault', 'mambeault0@etsmtl.ca', NULL, 'mambeault0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1),       -- admin1 (Admin)
('E.', 'Laforce', 'elaforce0@etsmtl.ca', NULL, 'elaforce0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1),        -- admin1 (Admin)
('L.', 'Seguin', 'lseguin0@etsmtl.ca', NULL, 'lseguin0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Active', 1),         -- admin1 (Admin)

('Test', 'Coach', 'testcoach@etsmtl.ca', NULL, 'testcoach', '$2a$10$zJbaow2rXTObDtnNmcgbdu9.ufmVcTo6JRAypSkTAetF6HoLAkGva', 'Active', 2),        -- password (Coach)
('Test', 'Athlete', 'testAthlete@etsmtl.ca', NULL, 'testAthlete', '$2a$10$zJbaow2rXTObDtnNmcgbdu9.ufmVcTo6JRAypSkTAetF6HoLAkGva', 'Active', 3);          -- password (Athlete)

INSERT INTO Sport (
    name
) VALUES 
('Golf'),
('Hockey'),
('Basketball'),
('Volleyball');

INSERT INTO Team (
    sport_id, 
    name
) VALUES 
(1, 'Golf1'),
(1, 'Golf2'),
(1, 'Golf3'),
(2, 'Hockey1'),
(2, 'Hockey2'),
(2, 'Hockey3'),
(3, 'Basketball1'),
(3, 'Basketball2'),
(3, 'Basketball3'),
(4, 'Volleyball1'),
(4, 'Volleyball2'),
(4, 'Volleyball3');

INSERT INTO Coach (
    user_id, 
    access_level, 
    sport_id, 
    team_id, 
    title
) VALUES 
(6, 2, 1, 1, 'Test coach title');
