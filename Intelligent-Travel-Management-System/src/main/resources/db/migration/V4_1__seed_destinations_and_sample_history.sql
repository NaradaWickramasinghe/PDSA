-- ============================================================================
-- Migration: V4_1__seed_destinations_and_sample_history.sql
-- Subsystem: Module 4 — Intelligent Decision
-- Target Database: Supabase Managed PostgreSQL
-- Purpose: Realistic seed dataset of Sri Lankan destinations and historical ratings
-- ============================================================================

-- 1. Realistic Destinations Seed Data
INSERT INTO destinations (id, name, province, average_daily_cost, minimum_days, maximum_days, beach_score, adventure_score, nature_score, culture_score, nightlife_score, relaxation_score, difficulty_level, family_friendly, couple_friendly, group_friendly)
VALUES
    ('11111111-1111-1111-1111-111111110001', 'Ella', 'Uva', 65.00, 2, 5, 1, 9, 10, 4, 6, 7, 3, TRUE, TRUE, TRUE),
    ('11111111-1111-1111-1111-111111110002', 'Mirissa', 'Southern', 75.00, 2, 6, 10, 6, 7, 3, 9, 8, 1, TRUE, TRUE, TRUE),
    ('11111111-1111-1111-1111-111111110003', 'Sigiriya', 'Central', 80.00, 1, 3, 1, 8, 8, 10, 2, 5, 3, TRUE, TRUE, TRUE),
    ('11111111-1111-1111-1111-111111110004', 'Kandy', 'Central', 60.00, 2, 4, 1, 4, 7, 10, 3, 6, 2, TRUE, TRUE, TRUE),
    ('11111111-1111-1111-1111-111111110005', 'Nuwara Eliya', 'Central', 85.00, 2, 5, 1, 5, 9, 6, 3, 9, 2, TRUE, TRUE, TRUE),
    ('11111111-1111-1111-1111-111111110006', 'Arugam Bay', 'Eastern', 70.00, 3, 7, 9, 9, 6, 3, 8, 7, 2, FALSE, TRUE, TRUE),
    ('11111111-1111-1111-1111-111111110007', 'Yala National Park', 'Southern', 120.00, 1, 3, 3, 8, 10, 2, 1, 5, 3, TRUE, TRUE, TRUE),
    ('11111111-1111-1111-1111-111111110008', 'Galle Fort', 'Southern', 90.00, 1, 3, 8, 3, 5, 9, 7, 8, 1, TRUE, TRUE, TRUE),
    ('11111111-1111-1111-1111-111111110009', 'Knuckles Mountain Range', 'Central', 50.00, 2, 4, 1, 10, 10, 2, 1, 4, 5, FALSE, FALSE, TRUE),
    ('11111111-1111-1111-1111-111111110010', 'Bentota', 'Western', 95.00, 2, 5, 9, 6, 6, 4, 5, 10, 1, TRUE, TRUE, TRUE),
    ('11111111-1111-1111-1111-111111110011', 'Anuradhapura', 'North Central', 55.00, 2, 4, 1, 3, 6, 10, 1, 7, 2, TRUE, TRUE, TRUE),
    ('11111111-1111-1111-1111-111111110012', 'Trincomalee', 'Eastern', 70.00, 2, 6, 10, 6, 8, 7, 4, 9, 1, TRUE, TRUE, TRUE)
ON CONFLICT (id) DO NOTHING;

-- 2. Baseline Historical Traveler Profiles for k-NN Similarity
INSERT INTO traveler_profiles (id, age_group, budget, duration_days, group_size, travel_style, beach_preference, adventure_preference, nature_preference, culture_preference, nightlife_preference, relaxation_preference)
VALUES
    ('22222222-2222-2222-2222-222222220001', 'YOUNG_ADULT', 700.00, 5, 2, 'ADVENTURE', 2, 5, 5, 2, 3, 3),
    ('22222222-2222-2222-2222-222222220002', 'YOUNG_ADULT', 800.00, 6, 4, 'FRIENDS', 5, 4, 3, 2, 5, 4),
    ('22222222-2222-2222-2222-222222220003', 'ADULT', 1200.00, 7, 2, 'COUPLE', 2, 2, 4, 5, 1, 5),
    ('22222222-2222-2222-2222-222222220004', 'ADULT', 1500.00, 6, 4, 'FAMILY', 4, 2, 5, 4, 1, 5),
    ('22222222-2222-2222-2222-222222220005', 'YOUNG_ADULT', 500.00, 4, 1, 'SOLO', 1, 5, 5, 2, 2, 2),
    ('22222222-2222-2222-2222-222222220006', 'SENIOR', 1000.00, 5, 2, 'COUPLE', 2, 1, 3, 5, 1, 5),
    ('22222222-2222-2222-2222-222222220007', 'YOUNG_ADULT', 600.00, 4, 3, 'FRIENDS', 5, 5, 3, 1, 5, 3)
ON CONFLICT (id) DO NOTHING;

-- 3. Baseline Historical Travel History (Ratings)
INSERT INTO travel_history (id, traveler_id, destination_id, rating, visited)
VALUES
    ('33333333-3333-3333-3333-333333330001', '22222222-2222-2222-2222-222222220001', '11111111-1111-1111-1111-111111110001', 5, TRUE), -- Young Adult Adv -> Ella (5)
    ('33333333-3333-3333-3333-333333330002', '22222222-2222-2222-2222-222222220001', '11111111-1111-1111-1111-111111110009', 5, TRUE), -- Young Adult Adv -> Knuckles (5)
    ('33333333-3333-3333-3333-333333330003', '22222222-2222-2222-2222-222222220002', '11111111-1111-1111-1111-111111110002', 5, TRUE), -- Friends Group -> Mirissa (5)
    ('33333333-3333-3333-3333-333333330004', '22222222-2222-2222-2222-222222220002', '11111111-1111-1111-1111-111111110006', 4, TRUE), -- Friends Group -> Arugam Bay (4)
    ('33333333-3333-3333-3333-333333330005', '22222222-2222-2222-2222-222222220003', '11111111-1111-1111-1111-111111110004', 5, TRUE), -- Culture Couple -> Kandy (5)
    ('33333333-3333-3333-3333-333333330006', '22222222-2222-2222-2222-222222220003', '11111111-1111-1111-1111-111111110003', 5, TRUE), -- Culture Couple -> Sigiriya (5)
    ('33333333-3333-3333-3333-333333330007', '22222222-2222-2222-2222-222222220004', '11111111-1111-1111-1111-111111110010', 5, TRUE), -- Family Relax -> Bentota (5)
    ('33333333-3333-3333-3333-333333330008', '22222222-2222-2222-2222-222222220004', '11111111-1111-1111-1111-111111110005', 4, TRUE), -- Family Relax -> Nuwara Eliya (4)
    ('33333333-3333-3333-3333-333333330009', '22222222-2222-2222-2222-222222220005', '11111111-1111-1111-1111-111111110009', 5, TRUE), -- Solo Trekker -> Knuckles (5)
    ('33333333-3333-3333-3333-333333330010', '22222222-2222-2222-2222-222222220006', '11111111-1111-1111-1111-111111110011', 5, TRUE), -- Senior Couple -> Anuradhapura (5)
    ('33333333-3333-3333-3333-333333330011', '22222222-2222-2222-2222-222222220007', '11111111-1111-1111-1111-111111110006', 5, TRUE)  -- Surfers Group -> Arugam Bay (5)
ON CONFLICT (id) DO NOTHING;
