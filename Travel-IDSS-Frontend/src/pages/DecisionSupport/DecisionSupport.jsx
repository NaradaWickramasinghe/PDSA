// src/pages/DecisionSupport/DecisionSupport.jsx
// Module 4: Intelligent Decision Support System with Multi-Algorithm Analytics

import React, { useState, useMemo } from 'react';
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

const ALGORITHM_MODES = [
  { id: 'ensemble', label: 'Hybrid Ensemble (Decision Tree + KNN + 6D Vector)', badge: '⚖️ Ensemble', desc: 'Combines all algorithms with multi-criteria weighted scoring for optimal destination recommendation.' },
  { id: 'tree', label: 'Decision Tree Classifier', badge: '🌳 Decision Tree', desc: 'Evaluates categorical suitability tiers using hierarchical decision rules (35% weight).' },
  { id: 'knn', label: 'K-Nearest Neighbors (KNN)', badge: '👥 KNN Similarity', desc: 'Finds destinations favored by similar historical traveler clusters via Cosine Similarity (25% weight).' },
  { id: 'preference', label: '6D Preference Matching', badge: '🎯 Vector Cosine', desc: 'Direct cosine similarity matching across 6 travel dimension preference vectors (30% weight).' }
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
  const [selectedAlgorithm, setSelectedAlgorithm] = useState('ensemble');
  const [showScoreDetails, setShowScoreDetails] = useState(true);

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

  // Re-rank items dynamically based on the selected algorithm mode
  const displayedRecommendations = useMemo(() => {
    if (!response || !response.recommendations) return [];

    const items = [...response.recommendations];

    switch (selectedAlgorithm) {
      case 'tree':
        items.sort((a, b) => (b.treeScore ?? 0) - (a.treeScore ?? 0) || (b.score ?? 0) - (a.score ?? 0));
        break;
      case 'knn':
        items.sort((a, b) => (b.knnEvidenceScore ?? 0) - (a.knnEvidenceScore ?? 0) || (b.score ?? 0) - (a.score ?? 0));
        break;
      case 'preference':
        items.sort((a, b) => (b.preferenceScore ?? 0) - (a.preferenceScore ?? 0) || (b.score ?? 0) - (a.score ?? 0));
        break;
      case 'ensemble':
      default:
        items.sort((a, b) => (b.score ?? 0) - (a.score ?? 0));
        break;
    }

    return items;
  }, [response, selectedAlgorithm]);

  // Helper to compute active algorithm score to display
  const getActiveScore = (rec) => {
    switch (selectedAlgorithm) {
      case 'tree':
        return { score: ((rec.treeScore ?? 0) * 100).toFixed(1), label: 'Tree Score' };
      case 'knn':
        return { score: ((rec.knnEvidenceScore ?? 0) * 100).toFixed(1), label: 'KNN Match' };
      case 'preference':
        return { score: ((rec.preferenceScore ?? 0) * 100).toFixed(1), label: 'Vector Match' };
      case 'ensemble':
      default:
        return { score: rec.matchPercentage?.toFixed(1) || ((rec.score ?? 0) * 100).toFixed(1), label: 'Ensemble Match' };
    }
  };

  return (
    <div className="orig-ds-root">
      <div className="orig-ds-container">
        <header className="orig-app-header">
          <div className="orig-header-flex">
            <div>
              <div className="orig-topic-tag">
                <span className="orig-topic-dot" /> Module 4
              </div>
              <h1 className="orig-app-title">Decision Support</h1>
              <p className="orig-app-subtitle">Personalized Travel Recommendations via Multi-Algorithm Decision Engine</p>
            </div>
            <div className="orig-header-badges">
              <span className="orig-engine-badge">🌳 Decision Tree (35%)</span>
              <span className="orig-engine-badge">👥 KNN Cosine (25%)</span>
              <span className="orig-engine-badge">🎯 6D Preferences (30%)</span>
            </div>
          </div>
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
                  <label className="orig-form-label">Budget ($)</label>
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
                {loading ? 'Executing Multi-Algorithm Evaluation...' : 'Find Recommendations'}
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
                <div className="orig-algo-preview-pills">
                  <span>Evaluates Decision Tree Suitability</span>
                  <span>Calculates KNN Traveler Similarity</span>
                  <span>Performs 6D Cosine Vector Ranking</span>
                </div>
              </div>
            )}

            {response && (
              <div>
                {/* Algorithm Selector Bar */}
                <div className="orig-algo-selector-card">
                  <div className="orig-algo-selector-header">
                    <div>
                      <span className="orig-algo-selector-title">🔍 Algorithm Perspective</span>
                      <span className="orig-algo-selector-sub">Select an algorithm to inspect how it ranks destinations:</span>
                    </div>
                    <button
                      type="button"
                      className="orig-toggle-details-btn"
                      onClick={() => setShowScoreDetails(!showScoreDetails)}
                    >
                      {showScoreDetails ? 'Hide Score Breakdown' : 'Show Score Breakdown'}
                    </button>
                  </div>

                  <div className="orig-algo-tabs">
                    {ALGORITHM_MODES.map((algo) => (
                      <button
                        key={algo.id}
                        type="button"
                        className={`orig-algo-tab ${selectedAlgorithm === algo.id ? 'active' : ''}`}
                        onClick={() => setSelectedAlgorithm(algo.id)}
                      >
                        <span className="orig-algo-tab-badge">{algo.badge}</span>
                      </button>
                    ))}
                  </div>
                  <p className="orig-algo-desc-text">
                    {ALGORITHM_MODES.find(m => m.id === selectedAlgorithm)?.desc}
                  </p>
                </div>

                <div className="orig-results-header">
                  <div>
                    <h2 className="orig-results-heading">
                      {selectedAlgorithm === 'ensemble' ? 'Top Recommendations' : `Ranked by ${ALGORITHM_MODES.find(m => m.id === selectedAlgorithm)?.label}`}
                    </h2>
                    <div className="orig-results-sub">
                      Evaluated {response.totalCandidatesEvaluated} candidate destinations • Decision Tree Primary Class: <strong>{response.decisionTreePrimaryPrediction}</strong>
                    </div>
                  </div>
                </div>

                <div className="orig-rec-list">
                  {displayedRecommendations.map((rec, index) => {
                    const activeScoreInfo = getActiveScore(rec);
                    const currentRank = index + 1;

                    return (
                      <div key={rec.destinationId || rec.destination} className={`orig-rec-card rank-${currentRank}`}>
                        <div className="orig-rec-header">
                          <div className="orig-rec-title-wrap">
                            <span className="orig-badge-rank">#{currentRank}</span>
                            <div>
                              <span className="orig-rec-name">{rec.destination}</span>
                              <span className="orig-rec-province"> — {rec.province} Province</span>
                            </div>
                          </div>
                          <div className="orig-match-badge">
                            <span className="orig-match-score">{activeScoreInfo.score}%</span>
                            <div className="orig-match-text">{activeScoreInfo.label}</div>
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

                        {/* Multi-Algorithm Score Breakdown Bars */}
                        {showScoreDetails && (
                          <div className="orig-algo-breakdown-box">
                            <div className="orig-algo-breakdown-title">Algorithm Contributions:</div>
                            <div className="orig-algo-bars-grid">
                              <div className="orig-algo-bar-item">
                                <div className="orig-algo-bar-label">
                                  <span>🌳 Decision Tree</span>
                                  <strong>{((rec.treeScore ?? 0) * 100).toFixed(0)}%</strong>
                                </div>
                                <div className="orig-progress-track">
                                  <div className="orig-progress-fill tree" style={{ width: `${Math.min(100, Math.max(0, (rec.treeScore ?? 0) * 100))}%` }} />
                                </div>
                              </div>

                              <div className="orig-algo-bar-item">
                                <div className="orig-algo-bar-label">
                                  <span>👥 KNN Neighbors</span>
                                  <strong>{((rec.knnEvidenceScore ?? 0) * 100).toFixed(0)}%</strong>
                                </div>
                                <div className="orig-progress-track">
                                  <div className="orig-progress-fill knn" style={{ width: `${Math.min(100, Math.max(0, (rec.knnEvidenceScore ?? 0) * 100))}%` }} />
                                </div>
                              </div>

                              <div className="orig-algo-bar-item">
                                <div className="orig-algo-bar-label">
                                  <span>🎯 6D Preferences</span>
                                  <strong>{((rec.preferenceScore ?? 0) * 100).toFixed(0)}%</strong>
                                </div>
                                <div className="orig-progress-track">
                                  <div className="orig-progress-fill preference" style={{ width: `${Math.min(100, Math.max(0, (rec.preferenceScore ?? 0) * 100))}%` }} />
                                </div>
                              </div>
                            </div>
                          </div>
                        )}

                        <div className="orig-specs-row">
                          <div className="orig-spec-cell">
                            Avg Cost: <strong>${rec.averageDailyCost}/day</strong>
                          </div>
                          <div className="orig-spec-cell">
                            Duration: <strong>{rec.minimumDays}-{rec.maximumDays} days</strong>
                          </div>
                          {rec.difficultyLevel && (
                            <div className="orig-spec-cell">
                              Difficulty: <strong>Level {rec.difficultyLevel}</strong>
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Algorithm Summary & Comparative Insights under the page */}
        {response && (
          <section className="orig-summary-section">
            <h2 className="orig-summary-heading">📊 Intelligent Decision Engine Summary & Algorithm Analysis</h2>
            <p className="orig-summary-sub">
              Cross-algorithm evaluation and scoring breakdown generated by the Subsystem Decision Pipeline
            </p>

            <div className="orig-summary-grid">
              {/* Rationale & Explainability Card */}
              <div className="orig-card orig-summary-card">
                <div className="orig-card-header-title">
                  <span>🧠 Machine Learning Rationale</span>
                  <span className="orig-badge-pill">C4.5 + KNN + MCDM</span>
                </div>
                <div className="orig-rationale-box">
                  {response.summaryRationale}
                </div>
                <div className="orig-rationale-meta">
                  <div>Primary Classification: <strong>{response.decisionTreePrimaryPrediction}</strong></div>
                  <div>Evaluated Pool: <strong>{response.totalCandidatesEvaluated} Destinations</strong></div>
                </div>
              </div>

              {/* Algorithm Weights Card */}
              <div className="orig-card orig-summary-card">
                <div className="orig-card-header-title">
                  <span>⚖️ Multi-Criteria Algorithm Formulation</span>
                </div>
                <p className="orig-weight-intro">
                  Final score = (0.35 × Decision Tree) + (0.30 × 6D Preference Vector) + (0.25 × KNN Clustering)
                </p>
                <div className="orig-weights-list">
                  <div className="orig-weight-item">
                    <span className="orig-weight-name">Decision Tree Suitability Classifier</span>
                    <div className="orig-weight-bar-wrap">
                      <div className="orig-weight-bar tree" style={{ width: '40%' }}>35%</div>
                    </div>
                  </div>
                  <div className="orig-weight-item">
                    <span className="orig-weight-name">6D Preference Vector Cosine Match</span>
                    <div className="orig-weight-bar-wrap">
                      <div className="orig-weight-bar preference" style={{ width: '35%' }}>30%</div>
                    </div>
                  </div>
                  <div className="orig-weight-item">
                    <span className="orig-weight-name">KNN Historical Traveler Clustering</span>
                    <div className="orig-weight-bar-wrap">
                      <div className="orig-weight-bar knn" style={{ width: '25%' }}>25%</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Algorithm Comparative Matrix Table */}
            <div className="orig-card orig-matrix-card">
              <div className="orig-card-header-title">
                <span>📋 Algorithm Comparison Matrix</span>
                <span className="orig-matrix-badge">{response.recommendations?.length} Top Candidates</span>
              </div>
              <div className="orig-table-responsive">
                <table className="orig-matrix-table">
                  <thead>
                    <tr>
                      <th>Rank</th>
                      <th>Destination</th>
                      <th>Province</th>
                      <th>Decision Tree</th>
                      <th>KNN Similarity</th>
                      <th>6D Vector Match</th>
                      <th>Ensemble Match</th>
                    </tr>
                  </thead>
                  <tbody>
                    {response.recommendations?.map((rec) => (
                      <tr key={rec.destinationId || rec.destination}>
                        <td>
                          <span className="orig-table-rank">#{rec.rank}</span>
                        </td>
                        <td className="orig-table-dest">
                          <strong>{rec.destination}</strong>
                        </td>
                        <td>{rec.province}</td>
                        <td>
                          <span className="orig-cell-score tree">{((rec.treeScore ?? 0) * 100).toFixed(0)}%</span>
                        </td>
                        <td>
                          <span className="orig-cell-score knn">{((rec.knnEvidenceScore ?? 0) * 100).toFixed(0)}%</span>
                        </td>
                        <td>
                          <span className="orig-cell-score preference">{((rec.preferenceScore ?? 0) * 100).toFixed(0)}%</span>
                        </td>
                        <td>
                          <strong className="orig-cell-score ensemble">{rec.matchPercentage?.toFixed(1)}%</strong>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </section>
        )}
      </div>
    </div>
  );
}

