INSERT INTO users (firebase_uid, name, surname, username, email, age, sex, region, image_url, is_onboarding)
VALUES
('mock-firebase-niko-kapanadze', 'Niko', 'Kapanadze', 'niko_k', 'niko.kapanadze@example.com', 22, 'MALE', 'Tbilisi', 'https://i.pravatar.cc/160?img=11', FALSE),
('mock-firebase-mariam-beridze', 'Mariam', 'Beridze', 'mariam_b', 'mariam.beridze@example.com', 21, 'FEMALE', 'Tbilisi', 'https://i.pravatar.cc/160?img=47', FALSE),
('mock-firebase-giorgi-abashidze', 'Giorgi', 'Abashidze', 'gio_abashidze', 'giorgi.abashidze@example.com', 24, 'MALE', 'Batumi', 'https://i.pravatar.cc/160?img=12', FALSE),
('mock-firebase-ana-japaridze', 'Ana', 'Japaridze', 'ana_j', 'ana.japaridze@example.com', 20, 'FEMALE', 'Kutaisi', 'https://i.pravatar.cc/160?img=32', FALSE),
('mock-firebase-luka-dolidze', 'Luka', 'Dolidze', 'luka_d', 'luka.dolidze@example.com', 23, 'MALE', 'Tbilisi', 'https://i.pravatar.cc/160?img=13', FALSE),
('mock-firebase-saba-metreveli', 'Saba', 'Metreveli', 'saba_m', 'saba.metreveli@example.com', 25, 'MALE', 'Rustavi', 'https://i.pravatar.cc/160?img=14', FALSE),
('mock-firebase-elene-nadiradze', 'Elene', 'Nadiradze', 'elene_n', 'elene.nadiradze@example.com', 22, 'FEMALE', 'Tbilisi', 'https://i.pravatar.cc/160?img=48', FALSE),
('mock-firebase-davit-kiknadze', 'Davit', 'Kiknadze', 'davit_k', 'davit.kiknadze@example.com', 27, 'MALE', 'Batumi', 'https://i.pravatar.cc/160?img=15', FALSE),
('mock-firebase-tamuna-lomidze', 'Tamuna', 'Lomidze', 'tamuna_l', 'tamuna.lomidze@example.com', 19, 'FEMALE', 'Kutaisi', 'https://i.pravatar.cc/160?img=49', FALSE),
('mock-firebase-levan-gelashvili', 'Levan', 'Gelashvili', 'levan_g', 'levan.gelashvili@example.com', 26, 'MALE', 'Tbilisi', 'https://i.pravatar.cc/160?img=16', FALSE),
('mock-firebase-salome-tsiklauri', 'Salome', 'Tsiklauri', 'salome_t', 'salome.tsiklauri@example.com', 23, 'FEMALE', 'Rustavi', 'https://i.pravatar.cc/160?img=50', FALSE),
('mock-firebase-irakli-mchedlishvili', 'Irakli', 'Mchedlishvili', 'irakli_m', 'irakli.mchedlishvili@example.com', 28, 'MALE', 'Tbilisi', 'https://i.pravatar.cc/160?img=17', FALSE),
('mock-firebase-natia-qoridze', 'Natia', 'Qoridze', 'natia_q', 'natia.qoridze@example.com', 24, 'FEMALE', 'Batumi', 'https://i.pravatar.cc/160?img=51', FALSE),
('mock-firebase-zura-tkeshelashvili', 'Zura', 'Tkeshelashvili', 'zura_t', 'zura.tkeshelashvili@example.com', 29, 'MALE', 'Kutaisi', 'https://i.pravatar.cc/160?img=18', FALSE),
('mock-firebase-nini-kharabadze', 'Nini', 'Kharabadze', 'nini_kh', 'nini.kharabadze@example.com', 21, 'FEMALE', 'Tbilisi', 'https://i.pravatar.cc/160?img=52', FALSE),
('mock-firebase-beka-tsereteli', 'Beka', 'Tsereteli', 'beka_t', 'beka.tsereteli@example.com', 25, 'MALE', 'Rustavi', 'https://i.pravatar.cc/160?img=19', FALSE)
ON CONFLICT (firebase_uid) DO NOTHING;

INSERT INTO user_sports (user_id, sport_id, skill_level)
SELECT u.id, s.id, v.skill_level
FROM (
    VALUES
    ('niko_k', 'Football', 'ADVANCED'),
    ('niko_k', 'Padel', 'INTERMEDIATE'),
    ('mariam_b', 'Football', 'INTERMEDIATE'),
    ('mariam_b', 'Volleyball', 'ADVANCED'),
    ('gio_abashidze', 'Basketball', 'ADVANCED'),
    ('ana_j', 'Tennis', 'ADVANCED'),
    ('luka_d', 'Football', 'INTERMEDIATE'),
    ('luka_d', 'Running', 'BEGINNER'),
    ('saba_m', 'Padel', 'ADVANCED'),
    ('saba_m', 'Boxing', 'INTERMEDIATE'),
    ('elene_n', 'Volleyball', 'INTERMEDIATE'),
    ('davit_k', 'Basketball', 'INTERMEDIATE'),
    ('tamuna_l', 'Tennis', 'INTERMEDIATE'),
    ('levan_g', 'Football', 'ADVANCED'),
    ('salome_t', 'Padel', 'BEGINNER'),
    ('irakli_m', 'Basketball', 'ADVANCED'),
    ('natia_q', 'Volleyball', 'ADVANCED'),
    ('zura_t', 'Football', 'INTERMEDIATE'),
    ('nini_kh', 'Tennis', 'BEGINNER'),
    ('beka_t', 'Boxing', 'ADVANCED')
) AS v(username, sport_name, skill_level)
JOIN users u ON u.username = v.username
JOIN sports s ON s.sport_name = v.sport_name
ON CONFLICT (user_id, sport_id) DO NOTHING;

WITH team_seed AS (
    SELECT *
    FROM (
        VALUES
        ('Tbilisi Strikers', 'Football', 'Tbilisi', 'Weekend football team looking for reliable players for 7v7 and 11v11 matches.', 'https://api.dicebear.com/8.x/shapes/svg?seed=tbilisi-strikers', 'niko_k', 11, 5, TRUE),
        ('Saburtalo Falcons', 'Football', 'Tbilisi', 'Competitive football squad with weekly training and friendly matches around Saburtalo.', 'https://api.dicebear.com/8.x/shapes/svg?seed=saburtalo-falcons', 'levan_g', 11, 6, TRUE),
        ('Batumi Ballers', 'Basketball', 'Batumi', 'Fast-paced basketball group for evening games near the boulevard courts.', 'https://api.dicebear.com/8.x/shapes/svg?seed=batumi-ballers', 'gio_abashidze', 12, 4, TRUE),
        ('Vake Hoopers', 'Basketball', 'Tbilisi', 'Casual but organized basketball team open to guards and forwards.', 'https://api.dicebear.com/8.x/shapes/svg?seed=vake-hoopers', 'irakli_m', 10, 5, TRUE),
        ('Kutaisi Aces', 'Tennis', 'Kutaisi', 'Small tennis team for doubles practice and friendly weekend tournaments.', 'https://api.dicebear.com/8.x/shapes/svg?seed=kutaisi-aces', 'ana_j', 4, 1, TRUE),
        ('Tbilisi Volley Crew', 'Volleyball', 'Tbilisi', 'Mixed volleyball team for indoor games, practice sessions, and local scrimmages.', 'https://api.dicebear.com/8.x/shapes/svg?seed=tbilisi-volley-crew', 'mariam_b', 8, 3, TRUE),
        ('Rustavi Padel Club', 'Padel', 'Rustavi', 'Beginner-friendly padel group searching for consistent doubles partners.', 'https://api.dicebear.com/8.x/shapes/svg?seed=rustavi-padel-club', 'saba_m', 6, 2, TRUE),
        ('Gldani Boxing Team', 'Boxing', 'Tbilisi', 'Closed boxing training group for sparring partners with prior experience.', 'https://api.dicebear.com/8.x/shapes/svg?seed=gldani-boxing-team', 'beka_t', 6, 0, FALSE)
    ) AS t(name, sport_name, region, description, logo_url, captain_username, max_players, open_spots, is_recruiting)
)
INSERT INTO teams (name, sport_id, region, description, logo_url, captain_id, max_players, open_spots, is_recruiting)
SELECT ts.name, s.id, ts.region, ts.description, ts.logo_url, captain.id, ts.max_players, ts.open_spots, ts.is_recruiting
FROM team_seed ts
JOIN sports s ON s.sport_name = ts.sport_name
JOIN users captain ON captain.username = ts.captain_username
WHERE NOT EXISTS (
    SELECT 1
    FROM teams existing_team
    WHERE existing_team.name = ts.name
);

INSERT INTO team_members (team_id, user_id, role)
SELECT t.id, u.id, v.role
FROM (
    VALUES
    ('Tbilisi Strikers', 'niko_k', 'CAPTAIN'),
    ('Tbilisi Strikers', 'mariam_b', 'MEMBER'),
    ('Tbilisi Strikers', 'luka_d', 'MEMBER'),
    ('Tbilisi Strikers', 'levan_g', 'MEMBER'),
    ('Tbilisi Strikers', 'zura_t', 'MEMBER'),
    ('Tbilisi Strikers', 'nini_kh', 'MEMBER'),
    ('Saburtalo Falcons', 'levan_g', 'CAPTAIN'),
    ('Saburtalo Falcons', 'zura_t', 'MEMBER'),
    ('Saburtalo Falcons', 'luka_d', 'MEMBER'),
    ('Saburtalo Falcons', 'irakli_m', 'MEMBER'),
    ('Saburtalo Falcons', 'davit_k', 'MEMBER'),
    ('Batumi Ballers', 'gio_abashidze', 'CAPTAIN'),
    ('Batumi Ballers', 'davit_k', 'MEMBER'),
    ('Batumi Ballers', 'irakli_m', 'MEMBER'),
    ('Batumi Ballers', 'natia_q', 'MEMBER'),
    ('Batumi Ballers', 'beka_t', 'MEMBER'),
    ('Batumi Ballers', 'saba_m', 'MEMBER'),
    ('Batumi Ballers', 'salome_t', 'MEMBER'),
    ('Batumi Ballers', 'tamuna_l', 'MEMBER'),
    ('Vake Hoopers', 'irakli_m', 'CAPTAIN'),
    ('Vake Hoopers', 'gio_abashidze', 'MEMBER'),
    ('Vake Hoopers', 'davit_k', 'MEMBER'),
    ('Vake Hoopers', 'niko_k', 'MEMBER'),
    ('Vake Hoopers', 'saba_m', 'MEMBER'),
    ('Kutaisi Aces', 'ana_j', 'CAPTAIN'),
    ('Kutaisi Aces', 'tamuna_l', 'MEMBER'),
    ('Kutaisi Aces', 'nini_kh', 'MEMBER'),
    ('Tbilisi Volley Crew', 'mariam_b', 'CAPTAIN'),
    ('Tbilisi Volley Crew', 'elene_n', 'MEMBER'),
    ('Tbilisi Volley Crew', 'natia_q', 'MEMBER'),
    ('Tbilisi Volley Crew', 'salome_t', 'MEMBER'),
    ('Tbilisi Volley Crew', 'ana_j', 'MEMBER'),
    ('Rustavi Padel Club', 'saba_m', 'CAPTAIN'),
    ('Rustavi Padel Club', 'salome_t', 'MEMBER'),
    ('Rustavi Padel Club', 'niko_k', 'MEMBER'),
    ('Rustavi Padel Club', 'nini_kh', 'MEMBER'),
    ('Gldani Boxing Team', 'beka_t', 'CAPTAIN'),
    ('Gldani Boxing Team', 'saba_m', 'MEMBER'),
    ('Gldani Boxing Team', 'zura_t', 'MEMBER'),
    ('Gldani Boxing Team', 'davit_k', 'MEMBER'),
    ('Gldani Boxing Team', 'levan_g', 'MEMBER'),
    ('Gldani Boxing Team', 'irakli_m', 'MEMBER')
) AS v(team_name, username, role)
JOIN teams t ON t.name = v.team_name
JOIN users u ON u.username = v.username
ON CONFLICT (team_id, user_id) DO NOTHING;

INSERT INTO matches (sport_id, format, home_team_id, away_team_id, location, proposed_datetime, status)
SELECT s.id, v.format, home_team.id, away_team.id, v.location, v.proposed_datetime::timestamptz, v.status
FROM (
    VALUES
    ('Football', 'TEAM', 'Tbilisi Strikers', 'Saburtalo Falcons', 'Mziuri Stadium, Tbilisi', '2026-06-15 19:00:00+04', 'UPCOMING'),
    ('Basketball', 'TEAM', 'Batumi Ballers', 'Vake Hoopers', 'Batumi Boulevard Court', '2026-06-18 20:30:00+04', 'UPCOMING'),
    ('Football', 'TEAM', 'Saburtalo Falcons', 'Tbilisi Strikers', 'Saburtalo Arena, Tbilisi', '2026-05-24 18:00:00+04', 'COMPLETED')
) AS v(sport_name, format, home_team_name, away_team_name, location, proposed_datetime, status)
JOIN sports s ON s.sport_name = v.sport_name
JOIN teams home_team ON home_team.name = v.home_team_name
JOIN teams away_team ON away_team.name = v.away_team_name
WHERE NOT EXISTS (
    SELECT 1
    FROM matches existing_match
    WHERE existing_match.home_team_id = home_team.id
      AND existing_match.away_team_id = away_team.id
      AND existing_match.proposed_datetime = v.proposed_datetime::timestamptz
);

INSERT INTO match_results (match_id, home_score, away_score, winner, submitted_by_user_id, confirmation_status, confirmed_at)
SELECT m.id, 3, 2, 'HOME', u.id, 'CONFIRMED', '2026-05-24 20:00:00+04'::timestamptz
FROM matches m
JOIN teams home_team ON home_team.id = m.home_team_id
JOIN teams away_team ON away_team.id = m.away_team_id
JOIN users u ON u.username = 'levan_g'
WHERE home_team.name = 'Saburtalo Falcons'
  AND away_team.name = 'Tbilisi Strikers'
  AND m.proposed_datetime = '2026-05-24 18:00:00+04'::timestamptz
  AND NOT EXISTS (
      SELECT 1
      FROM match_results existing_result
      WHERE existing_result.match_id = m.id
  );
