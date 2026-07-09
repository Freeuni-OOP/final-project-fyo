-- Seed 1v1 matches + reviews so profile match history / ratings have demo data.
-- Existing V3 matches are team-based (home_user_id/away_user_id null).

INSERT INTO matches (sport_id, format, home_user_id, away_user_id, location, proposed_datetime, status)
SELECT s.id, v.format, home_user.id, away_user.id, v.location, v.proposed_datetime::timestamptz, v.status
FROM (
    VALUES
    ('Tennis', 'ONE_VS_ONE', 'niko_k', 'mariam_b', 'Vake Tennis Club, Tbilisi', '2026-05-10 17:00:00+04', 'COMPLETED'),
    ('Padel', 'ONE_VS_ONE', 'gio_abashidze', 'ana_j', 'Batumi Padel Court', '2026-05-12 18:30:00+04', 'COMPLETED'),
    ('Badminton', 'ONE_VS_ONE', 'niko_k', 'luka_d', 'Saburtalo Sports Hall, Tbilisi', '2026-06-20 19:00:00+04', 'UPCOMING')
) AS v(sport_name, format, home_username, away_username, location, proposed_datetime, status)
JOIN sports s ON s.sport_name = v.sport_name
JOIN users home_user ON home_user.username = v.home_username
JOIN users away_user ON away_user.username = v.away_username
WHERE NOT EXISTS (
    SELECT 1
    FROM matches existing_match
    WHERE existing_match.home_user_id = home_user.id
      AND existing_match.away_user_id = away_user.id
      AND existing_match.proposed_datetime = v.proposed_datetime::timestamptz
);

INSERT INTO match_results (match_id, home_score, away_score, winner, submitted_by_user_id, confirmation_status, confirmed_at)
SELECT m.id, v.home_score, v.away_score, v.winner, submitter.id, 'CONFIRMED', v.confirmed_at::timestamptz
FROM (
    VALUES
    ('niko_k', 'mariam_b', '2026-05-10 17:00:00+04', 6, 4, 'HOME', 'niko_k', '2026-05-10 18:30:00+04'),
    ('gio_abashidze', 'ana_j', '2026-05-12 18:30:00+04', 2, 6, 'AWAY', 'ana_j', '2026-05-12 20:00:00+04')
) AS v(home_username, away_username, proposed_datetime, home_score, away_score, winner, submitter_username, confirmed_at)
JOIN users home_user ON home_user.username = v.home_username
JOIN users away_user ON away_user.username = v.away_username
JOIN users submitter ON submitter.username = v.submitter_username
JOIN matches m
  ON m.home_user_id = home_user.id
 AND m.away_user_id = away_user.id
 AND m.proposed_datetime = v.proposed_datetime::timestamptz
WHERE NOT EXISTS (
    SELECT 1
    FROM match_results existing_result
    WHERE existing_result.match_id = m.id
);

INSERT INTO user_reviews (match_id, review_user_id, reviewed_user_id, score, comment)
SELECT m.id, reviewer.id, reviewed.id, v.score, v.comment
FROM (
    VALUES
    ('niko_k', 'mariam_b', '2026-05-10 17:00:00+04', 'mariam_b', 'niko_k', 5, 'Great match, very fair play.'),
    ('niko_k', 'mariam_b', '2026-05-10 17:00:00+04', 'niko_k', 'mariam_b', 4, 'Strong opponent, fun rally.'),
    ('gio_abashidze', 'ana_j', '2026-05-12 18:30:00+04', 'gio_abashidze', 'ana_j', 5, 'Excellent padel skills.'),
    ('gio_abashidze', 'ana_j', '2026-05-12 18:30:00+04', 'ana_j', 'gio_abashidze', 3, 'Good game, a bit late to start.')
) AS v(home_username, away_username, proposed_datetime, reviewer_username, reviewed_username, score, comment)
JOIN users home_user ON home_user.username = v.home_username
JOIN users away_user ON away_user.username = v.away_username
JOIN users reviewer ON reviewer.username = v.reviewer_username
JOIN users reviewed ON reviewed.username = v.reviewed_username
JOIN matches m
  ON m.home_user_id = home_user.id
 AND m.away_user_id = away_user.id
 AND m.proposed_datetime = v.proposed_datetime::timestamptz
WHERE NOT EXISTS (
    SELECT 1
    FROM user_reviews existing_review
    WHERE existing_review.match_id = m.id
      AND existing_review.review_user_id = reviewer.id
      AND existing_review.reviewed_user_id = reviewed.id
);
