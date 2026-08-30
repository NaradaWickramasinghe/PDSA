// src/pages/DecisionSupport/DecisionSupport.jsx
// Exact Original Module 4 (Intelligent Decision Engine & Travel Recommendation System) Component

import { useState } from 'react';
import './DecisionSupport.css';

const PRESETS = [
  {
    label: "Beach & Relaxation",
    data: { budget: 1000, durationDays: 5, groupSize: 2, beach: 10, adventure: 4, nature: 6, culture: 3, nightlife: 7, relaxation: 9 }
  },
  {
    label: "Nature & Adventure",
    data: { budget: 700, durationDays: 4, groupSize: 2, beach: 2, adventure: 10, nature: 9, culture: 4, nightlife: 2, relaxation: 5 }
  },
  {
    label: "Cultural Explorer",
    data: { budget: 600, durationDays: 3, groupSize: 1, beach: 2, adventure: 4, nature: 5, culture: 10, nightlife: 3, relaxation: 6 }
  },
  {
    label: "Family Getaway",
    data: { budget: 1500, durationDays: 6, groupSize: 4, beach: 7, adventure: 5, nature: 8, culture: 6, nightlife: 3, relaxation: 8 }
  }
];

export default function DecisionSupport() {
  const [formData, setFormData] = useState({
    budget: 800,
    durationDays: 5,
    groupSize: 2,
    beachPreference: 3,
    adventurePreference: 9,
    naturePreference: 8,
    culturePreference: 6,
    nightlifePreference: 4,
    relaxationPreference: 5
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [response, setResponse] = useState(null);

  const handleChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const applyPreset = (preset) => {
    setFormData({
      budget: preset.data.budget,
      durationDays: preset.data.durationDays,
      groupSize: preset.data.groupSize,
      beachPreference: preset.data.beach,
      adventurePreference: preset.data.adventure,
      naturePreference: preset.data.nature,
      culturePreference: preset.data.culture,
      nightlifePreference: preset.data.nightlife,
      relaxationPreference: preset.data.relaxation
    });
    setError(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const res = await fetch('http://localhost:8085/api/decisions/recommend', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify({
          budget: parseFloat(formData.budget),
          durationDays: parseInt(formData.durationDays, 10),
          groupSize: parseInt(formData.groupSize, 10),
          beachPreference: parseInt(formData.beachPreference, 10),
          adventurePreference: parseInt(formData.adventurePreference, 10),
          naturePreference: parseInt(formData.naturePreference, 10),
          culturePreference: parseInt(formData.culturePreference, 10),
          nightlifePreference: parseInt(formData.nightlifePreference, 10),
          relaxationPreference: parseInt(formData.relaxationPreference, 10),
          topN: 5
        })
      });

      const json = await res.json();

      if (!res.ok || !json.success) {
        throw new Error(json.message || `Error: ${res.status}`);
      }

      setResponse(json.data);
    } catch (err) {
      setError(err.message || "Failed to reach recommendation service.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="orig-ds-root">
      <div className="orig-ds-container">
        <header className="orig-app-header">
          <h1 className="orig-app-title">Travel Recommendation System</h1>
          <p className="orig-app-subtitle">Task 4 — Personalized Destination Decision Engine</p>
        </header>

        <div className="orig-layout-grid">
          {/* Form Panel */}
          <div className="orig-card">
            <div className="orig-preset-section">
              <div className="orig-preset-label">Sample Presets</div>
              <div className="orig-preset-buttons">
                {PRESETS.map((p, idx) => (
                  <button key={idx} type="button" className="orig-btn-preset" onClick={() => applyPreset(p)}>
                    {p.label}
                  </button>
                ))}
              </div>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="orig-card-header-title" style={{ fontSize: '14px', marginBottom: '12px' }}>
                Trip Details
              </div>

              <div className="orig-form-grid-3">
                <div className="orig-form-group">
                  <label className="orig-form-label">Budget (Rs)</label>
                  <input
                    type="number"
                    className="orig-form-control"
                    min="0"
                    value={formData.budget}
                    onChange={e => handleChange('budget', e.target.value)}
                    required
                  />
                </div>
                <div className="orig-form-group">
                  <label className="orig-form-label">Days</label>
                  <input
                    type="number"
                    className="orig-form-control"
                    min="1"
                    max="14"
                    value={formData.durationDays}
                    onChange={e => handleChange('durationDays', e.target.value)}
                    required
                  />
                </div>
                <div className="orig-form-group">
                  <label className="orig-form-label">Group</label>
                  <input
                    type="number"
                    className="orig-form-control"
                    min="1"
                    max="10"
                    value={formData.groupSize}
                    onChange={e => handleChange('groupSize', e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="orig-card-header-title" style={{ fontSize: '14px', margin: '16px 0 12px' }}>
                Preferences (1 to 10)
              </div>

              {[
                { key: 'adventurePreference', label: 'Adventure' },
                { key: 'naturePreference', label: 'Nature' },
                { key: 'beachPreference', label: 'Beach' },
                { key: 'culturePreference', label: 'Culture' },
                { key: 'nightlifePreference', label: 'Nightlife' },
                { key: 'relaxationPreference', label: 'Relaxation' }
              ].map(item => (
                <div className="orig-slider-item" key={item.key}>
                  <div className="orig-slider-header">
                    <span className="orig-slider-title">{item.label}</span>
                    <span className="orig-slider-val">{formData[item.key]}</span>
                  </div>
                  <input
                    type="range"
                    className="orig-range-slider"
                    min="1"
                    max="10"
                    value={formData[item.key]}
                    onChange={e => handleChange(item.key, e.target.value)}
                  />
                </div>
              ))}

              <button type="submit" className="orig-btn-primary" disabled={loading}>
                {loading ? 'Finding Recommendations...' : 'Find Recommendations'}
              </button>
            </form>
          </div>

          {/* Results Panel */}
          <div>
            {error && (
              <div className="orig-alert-error">
                {error}
              </div>
            )}

            {!response && !loading && !error && (
              <div className="orig-card orig-empty-box">
                <h3>No Recommendations Requested Yet</h3>
                <p>Select a sample preset or enter your trip details and click <strong>Find Recommendations</strong>.</p>
              </div>
            )}

            {response && (
              <div>
                <div className="orig-results-header">
                  <div>
                    <h2 className="orig-results-heading">Top Recommendations</h2>
                    <div className="orig-results-sub">
                      Evaluated {response.totalCandidatesEvaluated} destinations • Category: <strong>{response.decisionTreePrimaryPrediction}</strong>
                    </div>
                  </div>
                </div>

                <div className="orig-rec-list">
                  {response.recommendations.map((rec) => (
                    <div key={rec.rank} className={`orig-rec-card rank-${rec.rank}`}>
                      <div className="orig-rec-header">
                        <div className="orig-rec-title-wrap">
                          <span className="orig-badge-rank">#{rec.rank}</span>
                          <div>
                            <span className="orig-rec-name">{rec.destination}</span>
                            <span className="orig-rec-province"> — {rec.province} Province</span>
                          </div>
                        </div>
                        <div className="orig-match-badge">
                          <span className="orig-match-score">{rec.matchPercentage}%</span>
                          <div className="orig-match-text">Match</div>
                        </div>
                      </div>

                      {rec.suitabilityLabel && (
                        <span className={`orig-suitability-tag ${rec.suitabilityLabel}`}>
                          {rec.suitabilityLabel.replace('_', ' ')}
                        </span>
                      )}

                      <div className="orig-reason-text">
                        {rec.reason}
                      </div>

                      <div className="orig-specs-row">
                        <div className="orig-spec-cell">
                          Avg: <strong>${rec.averageDailyCost}/day</strong>
                        </div>
                        <div className="orig-spec-cell">
                          Stay: <strong>{rec.minimumDays}-{rec.maximumDays} days</strong>
                        </div>
                        {rec.difficultyLevel && (
                          <div className="orig-spec-cell">
                            Difficulty: <strong>Lvl {rec.difficultyLevel}</strong>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
