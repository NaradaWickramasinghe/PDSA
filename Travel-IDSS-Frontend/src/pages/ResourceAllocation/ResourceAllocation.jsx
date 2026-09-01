import React, { useState, useEffect } from 'react';

const destImages = {
  'Ella': { src: 'https://images.unsplash.com/photo-1579244078426-68fb4f15d742?w=500&q=80', title: 'Nine Arch Bridge, Ella' },
  'Kandy': { src: 'https://images.unsplash.com/photo-1620601366914-747d9d0e2e98?w=500&q=80', title: 'Temple of the Tooth, Kandy' },
  'Nuwara Eliya': { src: 'https://images.unsplash.com/photo-1588614959060-4d144f28b207?w=500&q=80', title: 'Tea Country, Nuwara Eliya' },
  'Sigiriya': { src: 'https://images.unsplash.com/photo-1586521995568-39abaa0c2311?w=500&q=80', title: 'Sigiriya Ancient Rock Fortress' },
  'Galle': { src: 'https://images.unsplash.com/photo-1587595431973-160d0d94add1?w=500&q=80', title: 'Galle Fort Lighthouse' },
  'Mirissa': { src: 'https://images.unsplash.com/photo-1602431718042-995f5ab480c5?w=500&q=80', title: 'Mirissa Beach' },
  'Colombo': { src: 'https://images.unsplash.com/photo-1589139316719-21876472251a?w=500&q=80', title: 'Lotus Tower, Colombo' }
};

export default function ResourceAllocation() {
  const [formData, setFormData] = useState({
    destination: 'Ella',
    tripDurationDays: 3,
    travellerCount: 2,
    totalBudget: 50000,
    emergencyReserve: 5000,
    availableHours: 18,
    luggageCapacity: 15,
    selectedAlgorithm: 'PIPELINE'
  });

  const [activeTab, setActiveTab] = useState('ALL');
  const [currentData, setCurrentData] = useState(null);
  const [isAdvancedOpen, setIsAdvancedOpen] = useState(false);
  const [isCompOpen, setIsCompOpen] = useState(false);
  
  const [isGenerating, setIsGenerating] = useState(false);
  const [isBenchmarking, setIsBenchmarking] = useState(false);
  const [benchmarkResults, setBenchmarkResults] = useState([]);
  const [errorMessage, setErrorMessage] = useState('');

  // Update hours when days change
  useEffect(() => {
    setFormData(prev => ({ ...prev, availableHours: prev.tripDurationDays * 6 }));
  }, [formData.tripDurationDays]);

  const handleChange = (e) => {
    const { name, value, type } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'number' ? Number(value) : value
    }));
  };

  const submitForm = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    if (formData.emergencyReserve > formData.totalBudget) {
      setErrorMessage(`Emergency reserve (LKR ${formData.emergencyReserve.toLocaleString()}) cannot exceed total budget (LKR ${formData.totalBudget.toLocaleString()}).`);
      return;
    }

    setIsGenerating(true);

    try {
      const res = await fetch('http://localhost:8080/api/resource-allocation/allocate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      const data = await res.json();

      if (!res.ok) {
        let msg = data.message || 'Validation error occurred.';
        if (data.details && data.details.length > 0) {
          msg += '<ul>' + data.details.map(d => `<li>${d}</li>`).join('') + '</ul>';
        }
        setErrorMessage(msg);
      } else {
        setCurrentData({
          ...data,
          originalInputs: { ...formData }
        });
      }
    } catch (err) {
      setErrorMessage('Connection error: Unable to reach backend server. Verify it is running.');
    } finally {
      setIsGenerating(false);
    }
  };

  const runBenchmark = async () => {
    setIsBenchmarking(true);
    setBenchmarkResults([]);

    const payloadBase = { ...formData };
    
    try {
      const fetchAlgo = async (algo) => {
        const res = await fetch('http://localhost:8080/api/resource-allocation/allocate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ ...payloadBase, selectedAlgorithm: algo })
        });
        return await res.json();
      };

      const [greedyData, dpData, geneticData] = await Promise.all([
        fetchAlgo('GREEDY'),
        fetchAlgo('DYNAMIC_PROGRAMMING'),
        fetchAlgo('GENETIC')
      ]);

      setBenchmarkResults([
        { name: 'Budget-First Strategy (Greedy)', data: greedyData },
        { name: 'Optimal Value Strategy (DP)', data: dpData },
        { name: 'Balanced Discovery Strategy (GA)', data: geneticData }
      ]);
    } catch (err) {
      console.error(err);
    } finally {
      setIsBenchmarking(false);
    }
  };

  const getSubLabel = (res) => {
    if (res.description && res.description.trim().length > 0) return res.description;
    const name = res.name.toLowerCase();
    if (name.includes('train')) return 'First class reserved observation seats';
    if (name.includes('tuk')) return 'Unlimited transport around local attractions';
    if (name.includes('hotel') || name.includes('resort') || name.includes('villa')) return 'Comfortable room accommodation';
    if (name.includes('hik') || name.includes('trek')) return 'Scenic guided trekking experience';
    if (name.includes('falls') || name.includes('safari')) return 'Natural wildlife and scenic expedition';
    if (name.includes('backpack')) return 'High-capacity durable travel backpack';
    if (name.includes('first aid')) return 'Emergency medical supplies';
    if (name.includes('power bank')) return 'Portable fast charger';
    return res.category === 'TRANSPORTATION' ? 'Transportation' : 'Travel Option';
  };

  const getDisplayCategory = (res) => {
    if (res.category === 'ACCOMMODATION' || res.name.includes('Hotel') || res.name.includes('Resort') || res.name.includes('Villa') || res.name.includes('Lodge') || res.name.includes('Inn') || res.name.includes('Guesthouse')) return 'ACCOMMODATION';
    return res.category;
  };

  const getResourceIconAndTags = (res, nights, travellers) => {
    const cat = getDisplayCategory(res);
    let icon = '🎒', iconClass = 'icon-gear', tagClass = 'tag-gear', catLabel = 'Equipment';
    let chips = [], priceMain = `LKR ${res.cost.toLocaleString()}`, priceSub = '';

    if (cat === 'TRANSPORTATION') {
      catLabel = 'Transportation'; tagClass = 'tag-trans'; iconClass = 'icon-trans';
      const name = res.name.toLowerCase();
      icon = name.includes('tuk') ? '🛺' : name.includes('bus') ? '🚌' : name.includes('train') ? '🚆' : '🚗';
      chips = [
        `⏱️ ${res.durationHours > 0 ? `${res.durationHours.toFixed(1)} hours` : 'Direct'}`,
        `👥 Capacity: ${res.capacity ? `${res.capacity} people` : 'Standard'}`,
        `🚗 ${res.transportType || 'Transit'}`
      ];
      priceSub = 'Allocated transit fare';
    } else if (cat === 'ACCOMMODATION') {
      catLabel = 'Accommodation'; tagClass = 'tag-stay'; iconClass = 'icon-stay'; icon = '🏨';
      const ratePerNight = Math.round(res.cost / Math.max(1, nights));
      chips = [
        `📅 ${nights} nights`,
        `👥 Capacity: ${res.capacity ? `${res.capacity} guests` : 'Room capacity'}`,
        `💵 LKR ${ratePerNight.toLocaleString()} / night`
      ];
      priceMain = `LKR ${res.cost.toLocaleString()} total`;
      priceSub = `Total for ${nights} nights`;
    } else if (cat === 'ACTIVITY') {
      catLabel = 'Activity'; tagClass = 'tag-act'; iconClass = 'icon-act';
      const name = res.name.toLowerCase();
      icon = (name.includes('water') || name.includes('fall') || name.includes('beach') || name.includes('surf')) ? '🌊' :
             (name.includes('temple') || name.includes('heritage') || name.includes('fort')) ? '🏛️' : '⛰️';
      chips = [
        `⏱️ ${res.durationHours.toFixed(1)} hours`,
        `👥 ${travellers > 1 ? `For ${travellers} travellers` : 'Per ticket'}`
      ];
      priceMain = `LKR ${res.cost.toLocaleString()}`;
      priceSub = travellers > 1 ? 'Total group cost' : 'Activity fee';
    } else if (cat === 'PHYSICAL_ITEM') {
      catLabel = 'Equipment'; tagClass = 'tag-gear'; iconClass = 'icon-gear';
      const name = res.name.toLowerCase();
      icon = (name.includes('shoe') || name.includes('pole')) ? '🥾' :
             (name.includes('medical') || name.includes('first aid')) ? '🩹' :
             (name.includes('power') || name.includes('torch') || name.includes('lamp')) ? '🔦' : '🎒';
      chips = [
        `🎒 ${res.weightKg > 0 ? `${res.weightKg.toFixed(1)} kg` : '0.2 kg'}`,
        (res.usefulness && res.usefulness >= 90) ? '⭐ Highly Recommended' : 'Essential Gear'
      ];
      priceSub = 'Item cost / rental';
    }
    
    return { icon, iconClass, tagClass, catLabel, chips, priceMain, priceSub };
  };

  const renderSelectedResources = () => {
    if (!currentData || !currentData.selectedResources) return null;
    
    let list = currentData.selectedResources.filter(res => {
      const cat = getDisplayCategory(res);
      if (activeTab === 'ALL') return true;
      if (activeTab === 'TRANSPORTATION') return cat === 'TRANSPORTATION';
      if (activeTab === 'ACCOMMODATION') return cat === 'ACCOMMODATION';
      if (activeTab === 'ACTIVITIES') return cat === 'ACTIVITY';
      if (activeTab === 'EQUIPMENT') return cat === 'PHYSICAL_ITEM';
      return true;
    });

    if (list.length === 0) {
      return <div style={{textAlign: 'center', padding: '1.5rem', color: '#64748b', fontSize: '0.85rem'}}>No resources selected in this category.</div>;
    }

    const nights = currentData.originalInputs.tripDurationDays || 3;
    const travellers = currentData.originalInputs.travellerCount || 2;

    return list.map((res, idx) => {
      const { icon, iconClass, tagClass, catLabel, chips, priceMain, priceSub } = getResourceIconAndTags(res, nights, travellers);
      const isHighlight = (res.usefulness && res.usefulness >= 92);

      return (
        <div key={idx} className="res-card-item">
          <div className="res-card-left">
            <div className={`res-cat-icon-box ${iconClass}`}>{icon}</div>
            <div className="res-card-details">
              <div className="res-tag-row">
                <span className={`badge-cat-tag ${tagClass}`}>{catLabel}</span>
                {isHighlight && <span style={{background:'#fef3c7', color:'#92400e', fontSize:'0.68rem', fontWeight:700, padding:'1px 6px', borderRadius:'4px', marginLeft:'6px'}}>⭐ Top Pick</span>}
              </div>
              <div className="res-card-title">{res.name}</div>
              <div className="res-card-desc">{getSubLabel(res)}</div>
              <div className="res-attributes-chips">
                {chips.map((chip, i) => <span key={i} className="attr-chip">{chip}</span>)}
              </div>
            </div>
          </div>
          <div className="res-card-right">
            <div className="res-price-main">{priceMain}</div>
            <div className="res-price-sub">{priceSub}</div>
          </div>
        </div>
      );
    });
  };

  return (
    <>
      <div className="title-header-row">
        <div>
          <h1>Intelligent Resource Allocation</h1>
          <p>Smart, multi-constraint travel resource optimization for Sri Lanka <span className="module-badge">Module 2</span></p>
        </div>
        <button type="button" className="btn-outline-comp" onClick={() => setIsCompOpen(!isCompOpen)}>
          <span>⚡</span> Compare Planning Options
        </button>
      </div>

      {isCompOpen && (
        <div style={{ marginBottom: '1.5rem' }}>
          <div className="comp-container">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.65rem' }}>
              <div>
                <h3 style={{ fontSize: '0.95rem', fontWeight: 700, color: '#0f172a' }}>Compare Planning Strategies</h3>
                <p style={{ fontSize: '0.78rem', color: '#64748b' }}>Evaluate trade-offs between Budget-First, Maximum Value, and Balanced Discovery approaches.</p>
              </div>
              <button type="button" className="btn-outline-comp" onClick={runBenchmark} disabled={isBenchmarking}>
                <span>⚡</span> {isBenchmarking ? 'Evaluating Options...' : 'Compare Options'}
              </button>
            </div>

            {benchmarkResults.length > 0 && (
              <div style={{ overflowX: 'auto' }}>
                <table className="comp-table-ref">
                  <thead>
                    <tr>
                      <th>Strategy</th>
                      <th>Status</th>
                      <th>Total Cost</th>
                      <th>Travel Time</th>
                      <th>Equipment Weight</th>
                      <th>Execution Time</th>
                    </tr>
                  </thead>
                  <tbody>
                    {benchmarkResults.map((item, i) => (
                      <tr key={i}>
                        <td><strong>{item.name}</strong></td>
                        <td>{item.data.feasible ? '✅ Feasible' : '❌ Infeasible'}</td>
                        <td>LKR {item.data.totalCost ? item.data.totalCost.toLocaleString() : 0}</td>
                        <td>{item.data.totalTimeUsed ? item.data.totalTimeUsed.toFixed(1) : 0} h</td>
                        <td>{item.data.totalWeight ? item.data.totalWeight.toFixed(1) : 0} kg</td>
                        <td>{item.data.executionTimeMs} ms</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      <div className="two-col-grid">
        <div className="panel-card">
          {errorMessage && (
            <div className="alert-danger-box" dangerouslySetInnerHTML={{__html: errorMessage}}></div>
          )}

          <form onSubmit={submitForm}>
            {/* 1. Trip & Destination Context */}
            <div className="form-num-section">
              <div className="form-num-title"><span className="num-circle">1</span> Trip & Destination Context</div>
              <div style={{ marginBottom: '0.65rem' }}>
                <label style={{ fontSize: '0.78rem', color: '#475569', display: 'block', marginBottom: '0.3rem', fontWeight: 500 }}>Destination City</label>
                <div className="dest-row">
                  <select name="destination" value={formData.destination} onChange={handleChange} className="ref-input no-icon" style={{ flex: 1 }}>
                    {Object.keys(destImages).map(d => <option key={d} value={d}>📍 {d}</option>)}
                  </select>
                  <img src={destImages[formData.destination]?.src || ''} alt={destImages[formData.destination]?.title} className="dest-thumb" title={destImages[formData.destination]?.title} />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.65rem' }}>
                <div>
                  <label style={{ fontSize: '0.78rem', color: '#475569', display: 'block', marginBottom: '0.3rem', fontWeight: 500 }}>Trip Duration</label>
                  <div className="input-box-wrapper">
                    <span className="input-icon">📅</span>
                    <input type="number" name="tripDurationDays" min="1" max="14" value={formData.tripDurationDays} onChange={handleChange} className="ref-input" required />
                    <span className="input-suffix">Days</span>
                  </div>
                </div>
                <div>
                  <label style={{ fontSize: '0.78rem', color: '#475569', display: 'block', marginBottom: '0.3rem', fontWeight: 500 }}>Travellers</label>
                  <div className="input-box-wrapper">
                    <span className="input-icon">👥</span>
                    <input type="number" name="travellerCount" min="1" value={formData.travellerCount} onChange={handleChange} className="ref-input" required />
                    <span className="input-suffix">People</span>
                  </div>
                </div>
              </div>
            </div>

            {/* 2. Financial Resources */}
            <div className="form-num-section">
              <div className="form-num-title"><span className="num-circle">2</span> Financial Resources</div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.65rem' }}>
                <div>
                  <label style={{ fontSize: '0.78rem', color: '#475569', display: 'block', marginBottom: '0.3rem', fontWeight: 500 }}>Total Budget (LKR)</label>
                  <input type="number" name="totalBudget" min="0" step="1000" value={formData.totalBudget} onChange={handleChange} className="ref-input no-icon" required />
                </div>
                <div>
                  <label style={{ fontSize: '0.78rem', color: '#475569', display: 'block', marginBottom: '0.3rem', fontWeight: 500 }}>Emergency Reserve (LKR)</label>
                  <input type="number" name="emergencyReserve" min="0" step="500" value={formData.emergencyReserve} onChange={handleChange} className="ref-input no-icon" required />
                </div>
              </div>
              <div className="spendable-banner">
                <span>💵</span> Available for Allocation: LKR {Math.max(0, formData.totalBudget - formData.emergencyReserve).toLocaleString()}
              </div>
            </div>

            {/* 3. Time Resources */}
            <div className="form-num-section">
              <div className="form-num-title"><span className="num-circle">3</span> Time Resources</div>
              <div>
                <label style={{ fontSize: '0.78rem', color: '#475569', display: 'block', marginBottom: '0.3rem', fontWeight: 500 }}>Available Travel Time</label>
                <div className="input-box-wrapper">
                  <span className="input-icon">🕒</span>
                  <input type="number" name="availableHours" min="0.5" step="0.5" value={formData.availableHours} onChange={handleChange} className="ref-input" required />
                  <span className="input-suffix">Hours</span>
                </div>
              </div>
            </div>

            {/* 4. Physical Resources */}
            <div className="form-num-section">
              <div className="form-num-title"><span className="num-circle">4</span> Physical Resources</div>
              <div>
                <label style={{ fontSize: '0.78rem', color: '#475569', display: 'block', marginBottom: '0.3rem', fontWeight: 500 }}>Luggage / Capacity</label>
                <div className="input-box-wrapper">
                  <span className="input-icon">🎒</span>
                  <input type="number" name="luggageCapacity" min="0" step="0.5" value={formData.luggageCapacity} onChange={handleChange} className="ref-input" required />
                  <span className="input-suffix">kg</span>
                </div>
              </div>
            </div>

            {/* Advanced Preferences */}
            <div>
              <button type="button" onClick={() => setIsAdvancedOpen(!isAdvancedOpen)} style={{ background: 'none', border: 'none', color: '#16a34a', fontSize: '0.78rem', fontWeight: 600, cursor: 'pointer' }}>
                <span>⚙️ Advanced Planning Preferences</span>
              </button>
            </div>
            
            {isAdvancedOpen && (
              <div style={{ background: '#f8fafc', padding: '0.65rem', borderRadius: '0.4rem', border: '1px solid #e2e8f0', marginTop: '0.35rem' }}>
                <label style={{ fontSize: '0.75rem', fontWeight: 600, color: '#334155' }}>Planning Optimization Mode</label>
                <select name="selectedAlgorithm" value={formData.selectedAlgorithm} onChange={handleChange} className="ref-input no-icon" style={{ marginTop: '0.2rem', padding: '0.45rem' }}>
                  <option value="PIPELINE">Smart Multi-Tier Optimizer (Recommended)</option>
                  <option value="DYNAMIC_PROGRAMMING">Maximum Value Focus</option>
                  <option value="GREEDY">Budget-First Focus</option>
                  <option value="GENETIC">Flexible Discovery Focus</option>
                </select>
              </div>
            )}

            <button type="submit" className="btn-green-submit" disabled={isGenerating}>
              <span>🎯</span> {isGenerating ? 'Generating Travel Plan...' : 'Find My Travel Resource Plan'}
            </button>
          </form>
        </div>

        {/* RIGHT COLUMN — Allocation Results */}
        <div className="panel-card">
          <div className="result-header-row">
            <div>
              <h2>🛡️ Your Travel Resource Plan</h2>
              {currentData && (
                <div style={{ marginTop: '0.35rem', display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                  <span style={{ background: '#e0f2fe', color: '#0369a1', fontSize: '0.74rem', fontWeight: 700, padding: '0.2rem 0.55rem', borderRadius: '0.3rem' }}>
                    Allocation Pipeline
                  </span>
                </div>
              )}
            </div>
            {currentData && currentData.feasible && <span className="feasible-badge">✅ Feasible Plan Found</span>}
          </div>

          {!currentData ? (
            <div style={{ textAlign: 'center', color: '#94a3b8', padding: '4rem 1rem' }}>
              <div style={{ fontSize: '3rem', marginBottom: '0.5rem' }}>🌴</div>
              <h3 style={{ color: '#334155', fontSize: '1.05rem', marginBottom: '0.4rem' }}>No Plan Generated Yet</h3>
              <p style={{ fontSize: '0.85rem' }}>Configure your travel request on the left and click <strong>"Find My Travel Resource Plan"</strong> to generate recommendations.</p>
            </div>
          ) : (
            <div>
              <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: '0.5rem', padding: '0.65rem 0.85rem', marginBottom: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem', fontSize: '0.82rem', color: '#166534' }}>
                <div><strong>📍 Destination:</strong> {currentData.originalInputs.destination}</div>
                <div><strong>📅 Duration:</strong> {currentData.originalInputs.tripDurationDays} Days</div>
                <div><strong>👥 Travellers:</strong> {currentData.originalInputs.travellerCount} Persons</div>
              </div>

              <div className="metrics-4-grid">
                <div className="metric-mini-card">
                  <div className="card-icon">💳</div>
                  <div>
                    <label>Total Budget</label>
                    <div className="val">LKR {currentData.originalInputs.totalBudget.toLocaleString()}</div>
                  </div>
                </div>
                <div className="metric-mini-card">
                  <div className="card-icon">🧮</div>
                  <div>
                    <label>Allocated Cost</label>
                    <div className="val">LKR {(currentData.totalCost || 0).toLocaleString()}</div>
                  </div>
                </div>
                <div className="metric-mini-card">
                  <div className="card-icon">🛡️</div>
                  <div>
                    <label>Emergency Reserve</label>
                    <div className="val">LKR {currentData.originalInputs.emergencyReserve.toLocaleString()}</div>
                  </div>
                </div>
                <div className="metric-mini-card">
                  <div className="card-icon">💰</div>
                  <div>
                    <label>Remaining Budget</label>
                    <div className="val" style={{ color: '#16a34a' }}>LKR {(currentData.remainingBudget || 0).toLocaleString()}</div>
                  </div>
                </div>
              </div>

              <div className="tabs-header-row">
                <h3>Selected Resources ({(currentData.selectedResources || []).length})</h3>
                <div className="tab-pills">
                  {['ALL', 'TRANSPORTATION', 'ACCOMMODATION', 'ACTIVITIES', 'EQUIPMENT'].map(tab => (
                    <button key={tab} type="button" className={`tab-pill ${activeTab === tab ? 'active' : ''}`} onClick={() => setActiveTab(tab)}>
                      {tab.charAt(0) + tab.slice(1).toLowerCase()}
                    </button>
                  ))}
                </div>
              </div>

              <div className="res-cards-list">
                {renderSelectedResources()}
              </div>

              <div className="bottom-pills-grid">
                <div className="pill-stat-box pill-blue">
                  <span>🎒</span> Equipment Weight: {(currentData.totalWeight || 0).toFixed(1)} kg / {currentData.originalInputs.luggageCapacity} kg
                </div>
                <div className={`pill-stat-box ${currentData.totalTimeUsed > currentData.originalInputs.availableHours ? 'pill-orange' : 'pill-blue'}`}>
                  <span>{currentData.totalTimeUsed > currentData.originalInputs.availableHours ? '⚠️' : '🕒'}</span> Total Travel Time: {(currentData.totalTimeUsed || 0).toFixed(1)} h / {currentData.originalInputs.availableHours} h
                </div>
              </div>

              <div className="pill-green-note">
                🍃 This plan gives you the best experience within your constraints. Happy travels! 🍃
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
}
