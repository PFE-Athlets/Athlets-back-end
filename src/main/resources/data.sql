TRUNCATE TABLE "utilisateur" CASCADE;

INSERT INTO "utilisateur" (
    prenom, 
    nom, 
    courriel, 
    telephone, 
    nom_utilisateur, 
    mot_de_passe, 
    statut_compte, 
    niveau_acces
) VALUES 
('Zacharie', 'Morin', 'zmorin0@etsmtl.ca', '514-555-0101', 'zmorin0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Actif', 1),    -- admin1 (Admin)
('A.', 'Bun', 'abun0@etsmtl.ca', NULL, 'abun0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Actif', 1),                     -- admin1 (Admin)
('M.', 'Ambeault', 'mambeault0@etsmtl.ca', NULL, 'mambeault0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Actif', 1),       -- admin1 (Admin)
('E.', 'Laforce', 'elaforce0@etsmtl.ca', NULL, 'elaforce0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Actif', 1),        -- admin1 (Admin)
('L.', 'Seguin', 'lseguin0@etsmtl.ca', NULL, 'lseguin0', '$2a$10$z5IAiKe5qGL8VdSEutXZA.UbLnugSqxufxEK4H4QQ2k0R6Mdgop7y', 'Actif', 1),         -- admin1 (Admin)
('Test', 'User', 'testuser0@etsmtl.ca', NULL, 'testUser0', '$2a$10$zJbaow2rXTObDtnNmcgbdu9.ufmVcTo6JRAypSkTAetF6HoLAkGva', 'Actif', 3);          -- password (Athlète)