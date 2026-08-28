import React from 'react';

export default function ConfigPanel({
  nodes,
  formData,
  setFormData,
  onOptimize,
  loading
}) {
  const handleChange = (e) => {
    const { name, value, type } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'number' || type === 'range' ? Number(value) : value
    }));
  };

  return (
    <section className="opt-section">
      <div className="opt-section-title">Trip Configuration</div>

      <div className="opt-form-group">
        <label className="opt-label">Starting Point (Origin):</label>
        <select 
          name="sourceNodeId" 
          value={formData.sourceNodeId} 
          onChange={handleChange} 
          className="opt-select"
        >
          {nodes.map(n => (
            <option key={n.nodeId} value={n.nodeId}>{n.name} ({n.nodeId})</option>
          ))}
        </select>
      </div>

      <div className="opt-form-group">
        <label className="opt-label">Destination (Target):</label>
        <select 
          name="destinationNodeId" 
          value={formData.destinationNodeId} 
          onChange={handleChange} 
          className="opt-select"
        >
          {nodes.map(n => (
            <option key={n.nodeId} value={n.nodeId}>{n.name} ({n.nodeId})</option>
          ))}
        </select>
      </div>

      <div className="opt-form-group">
        <label className="opt-label">Algorithm Selection:</label>
        <select 
          name="algorithm" 
          value={formData.algorithm} 
          onChange={handleChange} 
          className="opt-select"
        >
          <option value="BRANCH_AND_BOUND">1. Branch & Bound (Exact Search)</option>
          <option value="PARETO_DYNAMIC_PROGRAMMING">2. Pareto Dynamic Programming (Trade-offs)</option>
          <option value="GENETIC_ALGORITHM">3. Genetic Algorithm (Metaheuristic - LO3)</option>
        </select>
      </div>

      <div className="opt-section-title" style={{ marginTop: '1.25rem', fontSize: '0.85rem' }}>
        Multi-Objective Weights
      </div>

      <div className="opt-form-group">
        <label className="opt-label">Speed / Travel Time Priority</label>
        <div className="opt-slider-group">
          <input 
            type="range" 
            name="timeWeight" 
            min="0" max="100" 
            value={formData.timeWeight} 
            onChange={handleChange} 
            className="opt-slider" 
          />
          <span className="opt-slider-val">{formData.timeWeight}%</span>
        </div>
      </div>

      <div className="opt-form-group">
        <label className="opt-label">Financial Budget / Cost Priority</label>
        <div className="opt-slider-group">
          <input 
            type="range" 
            name="costWeight" 
            min="0" max="100" 
            value={formData.costWeight} 
            onChange={handleChange} 
            className="opt-slider" 
          />
          <span className="opt-slider-val">{formData.costWeight}%</span>
        </div>
      </div>

      <div className="opt-form-group">
        <label className="opt-label">Safety & Hazard Minimization</label>
        <div className="opt-slider-group">
          <input 
            type="range" 
            name="safetyWeight" 
            min="0" max="100" 
            value={formData.safetyWeight} 
            onChange={handleChange} 
            className="opt-slider" 
          />
          <span className="opt-slider-val">{formData.safetyWeight}%</span>
        </div>
      </div>

      <div className="opt-form-group">
        <label className="opt-label">Road & Track Quality Comfort</label>
        <div className="opt-slider-group">
          <input 
            type="range" 
            name="qualityWeight" 
            min="0" max="100" 
            value={formData.qualityWeight} 
            onChange={handleChange} 
            className="opt-slider" 
          />
          <span className="opt-slider-val">{formData.qualityWeight}%</span>
        </div>
      </div>

      <div className="opt-section-title" style={{ marginTop: '1.25rem', fontSize: '0.85rem' }}>
        Hard Constraints
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem' }}>
        <div className="opt-form-group">
          <label className="opt-label">Max Time (Mins)</label>
          <input 
            type="number" 
            name="maxTimeMinutes" 
            value={formData.maxTimeMinutes} 
            onChange={handleChange} 
            className="opt-input" 
          />
        </div>
        <div className="opt-form-group">
          <label className="opt-label">Max Budget (LKR)</label>
          <input 
            type="number" 
            name="maxBudgetLkr" 
            value={formData.maxBudgetLkr} 
            onChange={handleChange} 
            className="opt-input" 
          />
        </div>
      </div>

      <button 
        className="opt-btn-primary" 
        onClick={onOptimize}
        disabled={loading}
      >
        {loading ? 'Optimizing...' : 'Optimize Hiking Route'}
      </button>
    </section>
  );
}
