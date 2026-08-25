-- ============================================================================
-- Migration: V4__create_intelligent_decision_schema.sql
-- Subsystem: Module 4 — Intelligent Decision (Personalized Recommendation Engine)
-- Target Database: Supabase Managed PostgreSQL
-- ============================================================================

-- Ensure pgcrypto / uuid extension is enabled for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. Traveler Profiles Table
CREATE TABLE IF NOT EXISTS traveler_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    age_group VARCHAR(30) NOT NULL,
    budget NUMERIC(10, 2) NOT NULL,
    duration_days INT NOT NULL,
    group_size INT NOT NULL,
    travel_style VARCHAR(50),
    beach_preference INT NOT NULL,
    adventure_preference INT NOT NULL,
    nature_preference INT NOT NULL,
    culture_preference INT NOT NULL,
    nightlife_preference INT NOT NULL,
    relaxation_preference INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_tp_age_group CHECK (age_group IN ('TEEN', 'YOUNG_ADULT', 'ADULT', 'SENIOR')),
    CONSTRAINT chk_tp_budget CHECK (budget > 0),
    CONSTRAINT chk_tp_duration CHECK (duration_days > 0),
    CONSTRAINT chk_tp_group_size CHECK (group_size > 0),
    CONSTRAINT chk_tp_beach_pref CHECK (beach_preference BETWEEN 1 AND 5),
    CONSTRAINT chk_tp_adv_pref CHECK (adventure_preference BETWEEN 1 AND 5),
    CONSTRAINT chk_tp_nature_pref CHECK (nature_preference BETWEEN 1 AND 5),
    CONSTRAINT chk_tp_culture_pref CHECK (culture_preference BETWEEN 1 AND 5),
    CONSTRAINT chk_tp_nightlife_pref CHECK (nightlife_preference BETWEEN 1 AND 5),
    CONSTRAINT chk_tp_relax_pref CHECK (relaxation_preference BETWEEN 1 AND 5)
);

-- 2. Destinations Table
CREATE TABLE IF NOT EXISTS destinations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL UNIQUE,
    province VARCHAR(100) NOT NULL,
    average_daily_cost NUMERIC(10, 2) NOT NULL,
    minimum_days INT NOT NULL,
    maximum_days INT NOT NULL,
    beach_score INT NOT NULL,
    adventure_score INT NOT NULL,
    nature_score INT NOT NULL,
    culture_score INT NOT NULL,
    nightlife_score INT NOT NULL,
    relaxation_score INT NOT NULL,
    difficulty_level INT NOT NULL,
    family_friendly BOOLEAN NOT NULL DEFAULT TRUE,
    couple_friendly BOOLEAN NOT NULL DEFAULT TRUE,
    group_friendly BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_dest_daily_cost CHECK (average_daily_cost > 0),
    CONSTRAINT chk_dest_min_days CHECK (minimum_days > 0),
    CONSTRAINT chk_dest_max_days CHECK (maximum_days >= minimum_days),
    CONSTRAINT chk_dest_beach CHECK (beach_score BETWEEN 1 AND 10),
    CONSTRAINT chk_dest_adv CHECK (adventure_score BETWEEN 1 AND 10),
    CONSTRAINT chk_dest_nature CHECK (nature_score BETWEEN 1 AND 10),
    CONSTRAINT chk_dest_culture CHECK (culture_score BETWEEN 1 AND 10),
    CONSTRAINT chk_dest_nightlife CHECK (nightlife_score BETWEEN 1 AND 10),
    CONSTRAINT chk_dest_relax CHECK (relaxation_score BETWEEN 1 AND 10),
    CONSTRAINT chk_dest_difficulty CHECK (difficulty_level BETWEEN 1 AND 5)
);

-- 3. Travel History Table (Ground-truth dataset for k-NN collaborative filtering)
CREATE TABLE IF NOT EXISTS travel_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    traveler_id UUID NOT NULL REFERENCES traveler_profiles(id) ON DELETE CASCADE,
    destination_id UUID NOT NULL REFERENCES destinations(id) ON DELETE CASCADE,
    rating INT NOT NULL,
    visited BOOLEAN NOT NULL DEFAULT TRUE,
    visited_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_th_rating CHECK (rating BETWEEN 1 AND 5)
);

-- 4. Decision Logs Table (Audit, performance tracking, model re-tuning & explainability)
CREATE TABLE IF NOT EXISTS decision_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    traveler_id UUID REFERENCES traveler_profiles(id) ON DELETE SET NULL,
    destination_id UUID NOT NULL REFERENCES destinations(id) ON DELETE CASCADE,
    tree_score REAL NOT NULL,
    knn_score REAL NOT NULL,
    final_score REAL NOT NULL,
    rank_position INT NOT NULL,
    explanation TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_dl_tree_score CHECK (tree_score BETWEEN 0.0 AND 1.0),
    CONSTRAINT chk_dl_knn_score CHECK (knn_score BETWEEN 0.0 AND 1.0),
    CONSTRAINT chk_dl_final_score CHECK (final_score BETWEEN 0.0 AND 1.0),
    CONSTRAINT chk_dl_rank_pos CHECK (rank_position > 0)
);

-- Strategic Indexes for High-Performance Queries
CREATE INDEX IF NOT EXISTS idx_travel_history_traveler ON travel_history(traveler_id);
CREATE INDEX IF NOT EXISTS idx_travel_history_destination ON travel_history(destination_id);
CREATE INDEX IF NOT EXISTS idx_decision_logs_traveler ON decision_logs(traveler_id);
CREATE INDEX IF NOT EXISTS idx_destinations_cost_duration ON destinations(average_daily_cost, minimum_days, maximum_days);
