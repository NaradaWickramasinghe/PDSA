import React, { useState } from 'react';
import { travelerDecisionService } from '../services/travelerDecisionService';

export default function TravelerRecommendationView() {
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

    const handleInputChange = (field, value) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const data = await travelerDecisionService.getRecommendations({
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
            });
            setResponse(data);
        } catch (err) {
            setError(err.message || 'Failed to fetch recommendations.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="traveler-recommendation-container">
            <h2>Personalized Travel Recommendations</h2>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label>Budget ($):</label>
                    <input
                        type="number"
                        min="0"
                        value={formData.budget}
                        onChange={e => handleInputChange('budget', e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>Duration (Days):</label>
                    <input
                        type="number"
                        min="1"
                        value={formData.durationDays}
                        onChange={e => handleInputChange('durationDays', e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>Group Size:</label>
                    <input
                        type="number"
                        min="1"
                        value={formData.groupSize}
                        onChange={e => handleInputChange('groupSize', e.target.value)}
                        required
                    />
                </div>

                <h3>Preferences (1 to 10)</h3>
                {['beachPreference', 'adventurePreference', 'naturePreference', 'culturePreference', 'nightlifePreference', 'relaxationPreference'].map(pref => (
                    <div className="slider-group" key={pref}>
                        <label>{pref}: {formData[pref]}</label>
                        <input
                            type="range"
                            min="1"
                            max="10"
                            value={formData[pref]}
                            onChange={e => handleInputChange(pref, e.target.value)}
                        />
                    </div>
                ))}

                <button type="submit" disabled={loading}>
                    {loading ? 'Evaluating...' : 'Get Recommendations'}
                </button>
            </form>

            {error && <div className="error-alert">{error}</div>}

            {response && (
                <div className="results-container">
                    <h3>Top Recommendations (Primary Decision Category: {response.decisionTreePrimaryPrediction})</h3>
                    {response.recommendations.map(rec => (
                        <div key={rec.rank} className="recommendation-item">
                            <h4>#{rec.rank} {rec.destination} ({rec.province} Province)</h4>
                            <p><strong>Match Score:</strong> {rec.matchPercentage}%</p>
                            <p><strong>Suitability:</strong> {rec.suitabilityLabel}</p>
                            <p><strong>Reason:</strong> {rec.reason}</p>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
