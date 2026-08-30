import React, { useState } from 'react';

const API_BASE_URL = 'http://localhost:8080/api/resource-allocation';

export default function ResourceAllocationForm() {
  const [formData, setFormData] = useState({
    destination: 'Ella',
    tripDurationDays: 3,
    travellerCount: 2,
    totalBudget: 50000,
    emergencyReserve: 5000,
    availableHours: 12,
    luggageCapacity: 15,
    transportTrain: true,
    transportBus: true,
    transportTukTuk: false,
    transportTaxi: false,
    selectedAlgorithm: 'PIPELINE'
  });

  const [activeTab, setActiveTab] = useState('ALL');
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [showComparison, setShowComparison] = useState(false);
  const [loading, setLoading] = useState(false);
  const [comparing, setComparing] = useState(false);
  const [response, setResponse] = useState(null);
  const [comparisonResults, setComparisonResults] = useState(null);
  const [error, setError] = useState(null);
  const [validationErrors, setValidationErrors] = useState([]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    
    if (type === 'checkbox') {
      setFormData(prev => ({ ...prev, [name]: checked }));
    } else if (name === 'tripDurationDays') {
      const days = value === '' ? '' : Math.max(1, Number(value));
      setFormData(prev => ({
        ...prev,
        tripDurationDays: days,
        availableHours: days !== '' ? days * 6 : prev.availableHours
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: ['selectedAlgorithm', 'destination'].includes(name)
          ? value
          : (value === '' ? '' : Number(value))
      }));
    }
  };

  const validateForm = () => {
    const errors = [];
    if (formData.totalBudget === '' || formData.totalBudget < 0) {
      errors.push('Total budget must be LKR 0 or greater.');
    }
    if (formData.emergencyReserve === '' || formData.emergencyReserve < 0) {
      errors.push('Emergency reserve must be LKR 0 or greater.');
    }
    if (Number(formData.emergencyReserve) > Number(formData.totalBudget)) {
      errors.push(`Emergency reserve (LKR ${Number(formData.emergencyReserve).toLocaleString()}) cannot exceed total budget (LKR ${Number(formData.totalBudget).toLocaleString()}).`);
    }
    if (formData.availableHours === '' || formData.availableHours <= 0) {
      errors.push('Available travel time must be greater than 0 hours.');
    }
    if (formData.luggageCapacity === '' || formData.luggageCapacity < 0) {
      errors.push('Luggage carrying capacity must be 0 kg or greater.');
    }
    return errors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setResponse(null);
    setComparisonResults(null);

    const clientErrors = validateForm();
    if (clientErrors.length > 0) {
      setValidationErrors(clientErrors);
      return;
    }
    setValidationErrors([]);
    setLoading(true);

    try {
      const res = await fetch(`${API_BASE_URL}/allocate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          destination: formData.destination,
          tripDurationDays: formData.tripDurationDays,
          travellerCount: formData.travellerCount,
          totalBudget: formData.totalBudget,
          emergencyReserve: formData.emergencyReserve,
          availableHours: formData.availableHours,
          luggageCapacity: formData.luggageCapacity,
          selectedAlgorithm: formData.selectedAlgorithm
        })
      });

      const data = await res.json();

      if (!res.ok) {
        if (data.details && data.details.length > 0) {
          setValidationErrors(data.details);
        } else {
          setError(data.message || 'An error occurred during resource allocation.');
        }
      } else {
        setResponse(data);
      }
    } catch (err) {
      setError('Connection refused. Please ensure backend Spring Boot application is running on port 8080.');
    } finally {
      setLoading(false);
    }
  };

  const handleRunComparison = async () => {
    setComparing(true);
    setComparisonResults(null);

    const payloadBase = {
      destination: formData.destination,
      tripDurationDays: formData.tripDurationDays,
      travellerCount: formData.travellerCount,
      totalBudget: formData.totalBudget,
      emergencyReserve: formData.emergencyReserve,
      availableHours: formData.availableHours,
      luggageCapacity: formData.luggageCapacity
    };

    try {
      const fetchAlgo = async (algo) => {
        const res = await fetch(`${API_BASE_URL}/allocate`, {
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

      setComparisonResults([
        { name: 'Greedy Algorithm', data: greedyData },
        { name: 'Dynamic Programming', data: dpData },
        { name: 'Genetic Algorithm', data: geneticData }
      ]);
    } catch (err) {
      console.error('Benchmark execution error:', err);
    } finally {
      setComparing(false);
    }
  };

  const availableForAllocation = Math.max(0, formData.totalBudget - formData.emergencyReserve);

  const getSubLabel = (res) => {
    if (res.name.includes('Scenic Train')) return 'First Class Reserved Seats (2)';
    if (res.name.includes('Local Tuk Tuk')) return 'Transport for local travel (3 days)';
    if (res.name.includes('Heritage Hotel')) return 'Standard Double Room (3 Nights)';
    if (res.name.includes('Hiking')) return 'Guided hiking experience';
    if (res.name.includes('Falls')) return 'Scenic waterfall experience';
    if (res.name.includes('Backpack')) return 'High quality travel backpack';
    if (res.name.includes('First Aid')) return 'Basic medical supplies';
    if (res.name.includes('Power Bank')) return 'Portable charger';
    return res.category === 'TRANSPORTATION' ? 'Transportation' : 'Travel Option';
  const getDisplayCategory = (res) => {
    if (!res) return '';
    if (res.category === 'ACCOMMODATION' || res.name.includes('Hotel') || res.name.includes('Resort') || res.name.includes('Villa') || res.name.includes('Lodge') || res.name.includes('Inn') || res.name.includes('Guesthouse') || res.name.includes('Hostel') || res.name.includes('Chalet') || res.name.includes('Suites')) {
      return 'ACCOMMODATION';
    }
    return res.category;
  };

  const filterResources = (resources) => {
    if (!resources) return [];
    if (activeTab === 'ALL') return resources;
    return resources.filter(res => {
      const cat = getDisplayCategory(res);
      if (activeTab === 'TRANSPORTATION') return cat === 'TRANSPORTATION';
      if (activeTab === 'ACCOMMODATION') return cat === 'ACCOMMODATION';
      if (activeTab === 'ACTIVITIES') return cat === 'ACTIVITY';
      if (activeTab === 'EQUIPMENT') return cat === 'PHYSICAL_ITEM';
      return true;
    });
  };

  const filteredResources = response ? filterResources(response.selectedResources) : [];

  const countCategory = (catName) => {
    if (!response || !response.selectedResources) return 0;
    if (catName === 'ALL') return response.selectedResources.length;
    return response.selectedResources.filter(res => {
      const cat = getDisplayCategory(res);
      if (catName === 'TRANSPORTATION') return cat === 'TRANSPORTATION';
      if (catName === 'ACCOMMODATION') return cat === 'ACCOMMODATION';
      if (catName === 'ACTIVITIES') return cat === 'ACTIVITY';
      if (catName === 'EQUIPMENT') return cat === 'PHYSICAL_ITEM';
      return false;
    }).length;
  };

  const getInfeasibleGuidance = () => {
    const effective = formData.totalBudget - formData.emergencyReserve;
    const notes = [];
    if (effective < 20000) notes.push('Spendable budget (after emergency reserve) is too low.');
    if (formData.availableHours < 5) notes.push('Available travel time duration is insufficient.');
    if (formData.luggageCapacity < 2) notes.push('Luggage carrying capacity is too restrictive.');
    if (notes.length === 0) notes.push('No combination satisfied all constraints simultaneously.');
    return notes;
  };

  return (
    <div className="reference-app-layout">
      <style>{`
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Inter', system-ui, -apple-system, sans-serif; }

        .reference-app-layout {
          display: flex;
          min-height: 100vh;
          background-color: #f8fafc;
          color: #1e293b;
        }

        /* SIDEBAR STYLING */
        .app-sidebar {
          width: 240px;
          background: #ffffff;
          border-right: 1px solid #e2e8f0;
          display: flex;
          flex-direction: column;
          padding: 1.5rem 1rem;
          flex-shrink: 0;
        }
        .sidebar-brand {
          display: flex;
          align-items: center;
          gap: 0.6rem;
          margin-bottom: 2rem;
          padding-left: 0.5rem;
        }
        .sidebar-brand .logo-icon {
          width: 32px;
          height: 32px;
          background: #16a34a;
          color: #ffffff;
          border-radius: 0.5rem;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 700;
          font-size: 1.1rem;
        }
        .sidebar-brand .brand-title {
          font-size: 1.05rem;
          font-weight: 700;
          color: #0f172a;
        }
        .sidebar-brand .brand-sub {
          font-size: 0.72rem;
          color: #64748b;
        }

        .nav-list { list-style: none; display: flex; flex-direction: column; gap: 0.35rem; }
        .nav-item {
          display: flex;
          align-items: center;
          gap: 0.75rem;
          padding: 0.65rem 0.85rem;
          border-radius: 0.5rem;
          color: #475569;
          font-size: 0.88rem;
          font-weight: 500;
          cursor: pointer;
          transition: all 0.15s ease;
        }
        .nav-item:hover { background: #f1f5f9; color: #0f172a; }
        .nav-item.active {
          background: #e6f4ea;
          color: #137333;
          font-weight: 600;
        }
        .nav-item .icon { font-size: 1rem; width: 20px; text-align: center; }

        .sidebar-footer {
          margin-top: auto;
          padding-top: 1.5rem;
          border-top: 1px solid #f1f5f9;
          font-size: 0.75rem;
          color: #94a3b8;
          text-align: center;
        }

        /* MAIN WRAPPER STYLING */
        .main-wrapper {
          flex: 1;
          display: flex;
          flex-direction: column;
          min-width: 0;
        }

        /* TOP HEADER STYLING */
        .top-header {
          height: 64px;
          background: #ffffff;
          border-bottom: 1px solid #e2e8f0;
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 0 2rem;
        }
        .top-header .menu-icon { font-size: 1.2rem; color: #64748b; cursor: pointer; }
        .top-header .user-area {
          display: flex;
          align-items: center;
          gap: 1rem;
        }
        .user-greeting-badge {
          background: #f0fdf4;
          border: 1px solid #bbf7d0;
          color: #166534;
          font-size: 0.78rem;
          font-weight: 600;
          padding: 0.3rem 0.75rem;
          border-radius: 2rem;
          display: flex;
          align-items: center;
          gap: 0.35rem;
        }
        .avatar-circle {
          width: 34px; height: 34px; border-radius: 50%; background: #e2e8f0; color: #475569;
          display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 0.9rem;
        }

        /* MAIN CONTENT AREA */
        .content-container {
          padding: 2rem;
          max-width: 1280px;
          margin: 0 auto;
          width: 100%;
        }

        .title-header-row {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 1.75rem;
        }
        .title-header-row h1 {
          font-size: 1.65rem;
          font-weight: 700;
          color: #0f172a;
          margin-bottom: 0.2rem;
        }
        .title-header-row p { color: #64748b; font-size: 0.9rem; display: flex; align-items: center; gap: 0.5rem; }
        .module-badge {
          background: #e6f4ea;
          color: #137333;
          font-size: 0.72rem;
          font-weight: 600;
          padding: 0.15rem 0.5rem;
          border-radius: 0.25rem;
        }

        .btn-outline-comp {
          background: #ffffff;
          border: 1px solid #cbd5e1;
          color: #334155;
          padding: 0.5rem 0.9rem;
          border-radius: 0.45rem;
          font-size: 0.84rem;
          font-weight: 600;
          cursor: pointer;
          display: flex;
          align-items: center;
          gap: 0.4rem;
          transition: all 0.15s ease;
        }
        .btn-outline-comp:hover { background: #f8fafc; border-color: #94a3b8; }

        /* TWO COLUMN GRID */
        .two-col-grid {
          display: grid;
          grid-template-columns: 440px 1fr;
          gap: 1.5rem;
        }
        @media (max-width: 1100px) {
          .two-col-grid { grid-template-columns: 1fr; }
        }

        /* CARD PANEL STYLING */
        .panel-card {
          background: #ffffff;
          border: 1px solid #e2e8f0;
          border-radius: 0.75rem;
          padding: 1.5rem;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
        }

        .panel-header-title {
          font-size: 1.1rem;
          font-weight: 700;
          color: #0f172a;
          margin-bottom: 1.25rem;
        }

        /* FORM SECTION STYLING */
        .form-num-section {
          margin-bottom: 1.15rem;
        }
        .form-num-title {
          font-size: 0.85rem;
          font-weight: 700;
          color: #0f172a;
          margin-bottom: 0.65rem;
          display: flex;
          align-items: center;
          gap: 0.45rem;
        }
        .num-circle {
          width: 20px; height: 20px; border-radius: 50%; background: #16a34a; color: #ffffff;
          font-size: 0.72rem; font-weight: 700; display: flex; align-items: center; justify-content: center;
        }

        .dest-row {
          display: flex;
          gap: 0.75rem;
          align-items: center;
        }
        .dest-thumb {
          width: 100px;
          height: 64px;
          border-radius: 0.45rem;
          object-fit: cover;
          border: 1px solid #e2e8f0;
        }

        .input-box-wrapper {
          position: relative;
        }
        .input-box-wrapper .input-icon {
          position: absolute; left: 0.75rem; top: 50%; transform: translateY(-50%); color: #64748b; font-size: 0.88rem;
        }
        .input-box-wrapper .input-suffix {
          position: absolute; right: 0.75rem; top: 50%; transform: translateY(-50%); color: #64748b; font-size: 0.8rem;
        }
        .ref-input {
          width: 100%;
          padding: 0.6rem 0.75rem 0.6rem 2.2rem;
          border: 1px solid #cbd5e1;
          border-radius: 0.45rem;
          font-size: 0.88rem;
          color: #0f172a;
          background: #ffffff;
        }
        .ref-input:focus { outline: none; border-color: #16a34a; box-shadow: 0 0 0 3px rgba(22, 163, 74, 0.12); }
        .ref-input.no-icon { padding-left: 0.75rem; }

        .spendable-banner {
          background: #e6f4ea;
          border: 1px solid #c6e7ce;
          color: #137333;
          font-size: 0.82rem;
          font-weight: 600;
          padding: 0.5rem 0.75rem;
          border-radius: 0.45rem;
          margin-top: 0.6rem;
          display: flex;
          align-items: center;
          gap: 0.4rem;
        }

        .checkbox-grid {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 0.5rem;
          margin-top: 0.4rem;
        }
        .checkbox-label {
          display: flex;
          align-items: center;
          gap: 0.4rem;
          font-size: 0.82rem;
          color: #334155;
          cursor: pointer;
        }
        .checkbox-label input { accent-color: #16a34a; }

        .btn-green-submit {
          width: 100%;
          padding: 0.85rem;
          background: #16a34a;
          color: #ffffff;
          border: none;
          border-radius: 0.5rem;
          font-size: 0.95rem;
          font-weight: 600;
          cursor: pointer;
          margin-top: 1rem;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 0.45rem;
          transition: background-color 0.15s;
        }
        .btn-green-submit:hover { background: #15803d; }
        .btn-green-submit:disabled { background: #94a3b8; cursor: not-allowed; }

        /* RIGHT RESULT PANEL STYLING */
        .result-header-row {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 1.25rem;
        }
        .result-header-row h2 {
          font-size: 1.15rem;
          font-weight: 700;
          color: #0f172a;
          display: flex;
          align-items: center;
          gap: 0.45rem;
        }
        .result-header-row p { font-size: 0.78rem; color: #64748b; margin-top: 0.15rem; }
        .feasible-badge {
          background: #dcfce7;
          border: 1px solid #bbf7d0;
          color: #15803d;
          font-size: 0.76rem;
          font-weight: 600;
          padding: 0.25rem 0.65rem;
          border-radius: 2rem;
        }

        /* 4 METRIC CARDS ROW */
        .metrics-4-grid {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 0.75rem;
          margin-bottom: 1.25rem;
        }
        @media (max-width: 768px) {
          .metrics-4-grid { grid-template-columns: 1fr 1fr; }
        }
        .metric-mini-card {
          background: #f8fafc;
          border: 1px solid #e2e8f0;
          border-radius: 0.5rem;
          padding: 0.65rem 0.75rem;
          display: flex;
          align-items: center;
          gap: 0.6rem;
        }
        .metric-mini-card .card-icon {
          width: 32px; height: 32px; border-radius: 0.4rem; background: #ffffff; border: 1px solid #e2e8f0;
          display: flex; align-items: center; justify-content: center; font-size: 0.95rem; flex-shrink: 0;
        }
        .metric-mini-card label { font-size: 0.7rem; color: #64748b; display: block; font-weight: 500; }
        .metric-mini-card .val { font-size: 0.92rem; font-weight: 700; color: #0f172a; margin-top: 0.1rem; }

        /* TABS STYLING */
        .tabs-header-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 0.85rem;
        }
        .tabs-header-row h3 { font-size: 0.92rem; font-weight: 700; color: #0f172a; }
        .tab-pills { display: flex; gap: 0.4rem; }
        .tab-pill {
          background: #f1f5f9;
          border: 1px solid transparent;
          color: #475569;
          font-size: 0.76rem;
          font-weight: 500;
          padding: 0.25rem 0.65rem;
          border-radius: 2rem;
          cursor: pointer;
        }
        .tab-pill.active {
          background: #16a34a;
          color: #ffffff;
          font-weight: 600;
        }

        /* RESOURCE LIST TABLE */
        .res-table-header {
          display: grid;
          grid-template-columns: 1fr 90px 70px 70px;
          padding: 0.4rem 0.75rem;
          font-size: 0.72rem;
          font-weight: 600;
          color: #94a3b8;
          border-bottom: 1px solid #f1f5f9;
        }
        .res-item-row {
          display: grid;
          grid-template-columns: 1fr 90px 70px 70px;
          align-items: center;
          padding: 0.65rem 0.75rem;
          border-bottom: 1px solid #f8fafc;
        }
        .res-item-row:last-child { border-bottom: none; }
        .res-info { display: flex; align-items: center; gap: 0.65rem; }
        .res-icon-thumb {
          width: 36px; height: 36px; border-radius: 0.4rem; object-fit: cover; background: #e2e8f0;
          display: flex; align-items: center; justify-content: center; font-size: 1rem; flex-shrink: 0;
        }
        .res-title-text { font-size: 0.85rem; font-weight: 600; color: #0f172a; }
        .res-sub-text { font-size: 0.72rem; color: #64748b; }
        .res-cat-tag { font-size: 0.68rem; font-weight: 700; color: #16a34a; text-transform: uppercase; margin-bottom: 0.1rem; display: block; }
        .res-val-col { font-size: 0.82rem; font-weight: 600; color: #334155; }

        /* BOTTOM PILLS ROW */
        .bottom-pills-grid {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 0.75rem;
          margin-top: 1.25rem;
        }
        .pill-stat-box {
          padding: 0.55rem 0.85rem;
          border-radius: 0.45rem;
          font-size: 0.8rem;
          font-weight: 600;
          display: flex;
          align-items: center;
          gap: 0.4rem;
        }
        .pill-blue { background: #f0f9ff; border: 1px solid #bae6fd; color: #0369a1; }
        .pill-orange { background: #fff7ed; border: 1px solid #ffedd5; color: #c2410c; }
        .pill-green-note {
          background: #f0fdf4;
          border: 1px solid #bbf7d0;
          color: #15803d;
          font-size: 0.8rem;
          font-weight: 500;
          text-align: center;
          padding: 0.55rem;
          border-radius: 0.45rem;
          margin-top: 0.75rem;
        }

        .alert-danger-box {
          background: #fef2f2; border: 1px solid #fecaca; color: #991b1b; padding: 0.75rem; border-radius: 0.45rem; font-size: 0.82rem; margin-bottom: 1rem;
        }
        .alert-danger-box ul { margin-left: 1.2rem; margin-top: 0.2rem; }

        /* BENCHMARK COMPARISON MODAL / DRAWER */
        .comp-container {
          background: #ffffff; border: 1px solid #cbd5e1; border-radius: 0.65rem; padding: 1rem; margin-top: 1rem;
        }
        .comp-table-ref { width: 100%; border-collapse: collapse; font-size: 0.78rem; margin-top: 0.5rem; }
        .comp-table-ref th, .comp-table-ref td { padding: 0.5rem 0.6rem; border: 1px solid #cbd5e1; text-align: left; }
        .comp-table-ref th { background: #f1f5f9; color: #334155; font-weight: 600; }
      `}</style>

      {/* LEFT SIDEBAR */}
      <aside className="app-sidebar">
        <div className="sidebar-brand">
          <div className="logo-icon">🌴</div>
          <div>
            <div className="brand-title">Travel Planner</div>
            <div className="brand-sub">Sri Lanka</div>
          </div>
        </div>

        <ul className="nav-list">
          <li className="nav-item"><span className="icon">🏠</span> Dashboard</li>
          <li className="nav-item"><span className="icon">📍</span> Destinations</li>
          <li className="nav-item"><span className="icon">🗺️</span> Trip Planning</li>
          <li className="nav-item"><span className="icon">🎒</span> Resources</li>
          <li className="nav-item active"><span className="icon">🟩</span> Resource Allocation</li>
          <li className="nav-item"><span className="icon">📑</span> My Plans</li>
          <li className="nav-item"><span className="icon">🎫</span> Bookings</li>
          <li className="nav-item"><span className="icon">👤</span> Profile</li>
          <li className="nav-item"><span className="icon">⚙️</span> Settings</li>
        </ul>

        <div className="sidebar-footer">
          <div style={{ fontWeight: 600, color: '#475569', marginBottom: '0.2rem' }}>Sri Lanka</div>
          <div>Wonder of Asia</div>
          <div style={{ marginTop: '0.5rem' }}>© 2025 Travel Planner</div>
        </div>
      </aside>

      {/* MAIN WRAPPER */}
      <div className="main-wrapper">
        {/* TOP HEADER */}
        <header className="top-header">
          <span className="menu-icon">☰</span>
          <div className="user-area">
            <div className="user-greeting-badge">
              <span>🍃</span> Plan Smart, Travel Better
            </div>
            <div className="avatar-circle">👤</div>
          </div>
        </header>

        {/* CONTENT CONTAINER */}
        <main className="content-container">
          {/* TITLE HEADER ROW */}
          <div className="title-header-row">
            <div>
              <h1>Intelligent Resource Allocation</h1>
              <p>
                Get the best travel resource plan within your budget, time and capacity
                <span className="module-badge">Module 2</span>
              </p>
            </div>

            <button type="button" className="btn-outline-comp" onClick={() => setShowComparison(!showComparison)}>
              📊 Compare Algorithms
            </button>
          </div>

          {/* ALGORITHM COMPARISON DRAWER */}
          {showComparison && (
            <div className="comp-container" style={{ marginBottom: '1.5rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <h4 style={{ fontSize: '0.9rem', color: '#0f172a', fontWeight: 700 }}>Algorithm Experimental Benchmark Comparison</h4>
                <button type="button" onClick={handleRunComparison} disabled={comparing} style={{ background: '#16a34a', color: '#fff', border: 'none', padding: '0.4rem 0.75rem', borderRadius: '0.4rem', fontSize: '0.78rem', cursor: 'pointer' }}>
                  {comparing ? 'Executing...' : '⚡ Run API Benchmark'}
                </button>
              </div>

              {comparisonResults ? (
                <table className="comp-table-ref">
                  <thead>
                    <tr>
                      <th>Algorithm Strategy</th>
                      <th>Feasible</th>
                      <th>Score (pts)</th>
                      <th>Total Cost (LKR)</th>
                      <th>Time Used</th>
                      <th>Weight Used</th>
                      <th>Latency</th>
                    </tr>
                  </thead>
                  <tbody>
                    {comparisonResults.map((item, idx) => (
                      <tr key={idx}>
                        <td><strong>{item.name}</strong></td>
                        <td>{item.data.feasible ? '✅ Feasible' : '❌ Infeasible'}</td>
                        <td><strong style={{ color: '#16a34a' }}>{item.data.overallScore ? item.data.overallScore.toFixed(1) : 0}</strong></td>
                        <td>LKR {item.data.totalCost ? item.data.totalCost.toLocaleString() : 0}</td>
                        <td>{item.data.totalTimeUsed ? item.data.totalTimeUsed.toFixed(1) : 0} h</td>
                        <td>{item.data.totalWeight ? item.data.totalWeight.toFixed(1) : 0} kg</td>
                        <td>{item.data.executionTimeMs} ms</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <p style={{ fontSize: '0.8rem', color: '#64748b' }}>Click "Run API Benchmark" to execute Greedy, Dynamic Programming, and Genetic Algorithm in real-time.</p>
              )}
            </div>
          )}

          {/* TWO COLUMN MAIN LAYOUT */}
          <div className="two-col-grid">
            {/* LEFT COLUMN — Allocation Request */}
            <div className="panel-card">
              <h2 className="panel-header-title">Allocation Request</h2>

              <form onSubmit={handleSubmit}>
                {/* 1. Trip Information */}
                <div className="form-num-section">
                  <div className="form-num-title">
                    <span className="num-circle">1</span> Trip Information
                  </div>

                  <div style={{ marginBottom: '0.65rem' }}>
                    <label style={{ fontSize: '0.78rem', color: '#475569', display: 'block', marginBottom: '0.3rem', fontWeight: 500 }}>Destination</label>
                    <div className="dest-row">
                      <select name="destination" value={formData.destination} onChange={handleChange} className="ref-input no-icon" style={{ flex: 1 }}>
                        <option value="Ella">📍 Ella</option>
                        <option value="Kandy">📍 Kandy</option>
                        <option value="Nuwara Eliya">📍 Nuwara Eliya</option>
                        <option value="Sigiriya">📍 Sigiriya</option>
                        <option value="Galle">📍 Galle</option>
                        <option value="Mirissa">📍 Mirissa</option>
                        <option value="Colombo">📍 Colombo</option>
                      </select>
                      <img 
                        src="https://images.unsplash.com/photo-1546708973-b339540b5162?auto=format&fit=crop&w=200&q=80" 
                        alt="Sri Lanka Destination" 
                        className="dest-thumb" 
                      />
                    </div>
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.65rem' }}>
                    <div>
                      <label style={{ fontSize: '0.78rem', color: '#475569', display: 'block', marginBottom: '0.3rem', fontWeight: 500 }}>Trip Duration</label>
                      <div className="input-box-wrapper">
                        <span className="input-icon">📅</span>
                        <input type="number" name="tripDurationDays" value={formData.tripDurationDays} onChange={handleChange} min="1" max="14" className="ref-input" required />
                        <span className="input-suffix">Days</span>
                      </div>
                    </div>

                    <div>
                      <label style={{ fontSize: '0.78rem', color: '#475569', display: 'block', marginBottom: '0.3rem', fontWeight: 500 }}>Travellers</label>
                      <div className="input-box-wrapper">
                        <span className="input-icon">👥</span>
                        <input type="number" name="travellerCount" value={formData.travellerCount} onChange={handleChange} min="1" className="ref-input" required />
                        <span className="input-suffix">People</span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* 2. Financial Resources */}
                <div className="form-num-section">
                  <div className="form-num-title">
                    <span className="num-circle">2</span> Financial Resources
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.65rem' }}>
                    <div>
                      <label style={{ fontSize: '0.78rem', color: '#475569', display: 'block', marginBottom: '0.3rem', fontWeight: 500 }}>Total Budget (LKR)</label>
                      <input type="number" name="totalBudget" value={formData.totalBudget} onChange={handleChange} min="0" step="1000" className="ref-input no-icon" required />
                    </div>

                    <div>
                      <label style={{ fontSize: '0.78rem', color: '#475569', display: 'block', marginBottom: '0.3rem', fontWeight: 500 }}>Emergency Reserve (LKR)</label>
                      <input type="number" name="emergencyReserve" value={formData.emergencyReserve} onChange={handleChange} min="0" step="500" className="ref-input no-icon" required />
                    </div>
                  </div>

                  <div className="spendable-banner">
                    <span>💵</span> Available for Allocation: LKR {availableForAllocation.toLocaleString()}
                  </div>
                </div>

                {/* 3. Time Resources */}
                <div className="form-num-section">
                  <div className="form-num-title">
                    <span className="num-circle">3</span> Time Resources
                  </div>
                  <div>
                    <label style={{ fontSize: '0.78rem', color: '#475569', display: 'block', marginBottom: '0.3rem', fontWeight: 500 }}>Available Travel Time</label>
                    <div className="input-box-wrapper">
                      <span className="input-icon">🕒</span>
                      <input type="number" name="availableHours" value={formData.availableHours} onChange={handleChange} min="0.5" step="0.5" className="ref-input" required />
                      <span className="input-suffix">Hours</span>
                    </div>
                  </div>
                </div>

                {/* 4. Physical Resources */}
                <div className="form-num-section">
                  <div className="form-num-title">
                    <span className="num-circle">4</span> Physical Resources
                  </div>
                  <div>
                    <label style={{ fontSize: '0.78rem', color: '#475569', display: 'block', marginBottom: '0.3rem', fontWeight: 500 }}>Luggage / Carrying Capacity</label>
                    <div className="input-box-wrapper">
                      <span className="input-icon">🎒</span>
                      <input type="number" name="luggageCapacity" value={formData.luggageCapacity} onChange={handleChange} min="0" step="0.5" className="ref-input" required />
                      <span className="input-suffix">kg</span>
                    </div>
                  </div>
                </div>

                {/* 5. Transportation Preferences (Optional) */}
                <div className="form-num-section">
                  <div className="form-num-title">
                    <span className="num-circle">5</span> Transportation Preferences (Optional)
                  </div>
                  <div style={{ fontSize: '0.76rem', color: '#64748b', marginBottom: '0.3rem' }}>Select preferred transportation modes</div>
                  <div className="checkbox-grid">
                    <label className="checkbox-label">
                      <input type="checkbox" name="transportTrain" checked={formData.transportTrain} onChange={handleChange} />
                      Train
                    </label>
                    <label className="checkbox-label">
                      <input type="checkbox" name="transportBus" checked={formData.transportBus} onChange={handleChange} />
                      Bus
                    </label>
                    <label className="checkbox-label">
                      <input type="checkbox" name="transportTukTuk" checked={formData.transportTukTuk} onChange={handleChange} />
                      Tuk Tuk
                    </label>
                    <label className="checkbox-label">
                      <input type="checkbox" name="transportTaxi" checked={formData.transportTaxi} onChange={handleChange} />
                      Private Vehicle
                    </label>
                  </div>
                </div>

                {/* Advanced Algorithm Option */}
                <div>
                  <button type="button" onClick={() => setShowAdvanced(!showAdvanced)} style={{ background: 'none', border: 'none', color: '#16a34a', fontSize: '0.78rem', fontWeight: 600, cursor: 'pointer' }}>
                    {showAdvanced ? '▲ Hide Advanced Algorithm Settings' : '⚙️ Advanced Algorithm Selection'}
                  </button>
                </div>

                {showAdvanced && (
                  <div style={{ background: '#f8fafc', padding: '0.65rem', borderRadius: '0.4rem', border: '1px solid #e2e8f0', marginTop: '0.35rem' }}>
                    <label style={{ fontSize: '0.75rem', fontWeight: 600, color: '#334155' }}>Optimization Strategy</label>
                    <select name="selectedAlgorithm" value={formData.selectedAlgorithm} onChange={handleChange} className="ref-input no-icon" style={{ marginTop: '0.2rem', padding: '0.45rem' }}>
                      <option value="PIPELINE">Multi-Stage Pipeline (Greedy + DP + Genetic) - Recommended</option>
                      <option value="DYNAMIC_PROGRAMMING">Dynamic Programming Algorithm (Exact Optimal)</option>
                      <option value="GREEDY">Greedy Algorithm (Fast Heuristic Ratio)</option>
                      <option value="GENETIC">Genetic Algorithm (Evolutionary Optimization)</option>
                    </select>
                  </div>
                )}

                {/* Errors */}
                {validationErrors.length > 0 && (
                  <div className="alert-danger-box" style={{ marginTop: '0.75rem' }}>
                    <strong>Input Validation Errors:</strong>
                    <ul>{validationErrors.map((err, i) => <li key={i}>{err}</li>)}</ul>
                  </div>
                )}

                {error && (
                  <div className="alert-danger-box" style={{ marginTop: '0.75rem' }}>
                    {error}
                  </div>
                )}

                <button type="submit" className="btn-green-submit" disabled={loading}>
                  <span>🎯</span> {loading ? 'Generating Travel Plan...' : 'Find My Travel Resource Plan'}
                </button>
              </form>
            </div>

            {/* RIGHT COLUMN — Allocation Results */}
            <div className="panel-card">
              {/* RESULT HEADER */}
              <div className="result-header-row">
                <div>
                  <h2>🛡️ Your Travel Resource Plan</h2>
                  <p>
                    Strategy: <strong style={{ color: '#16a34a' }}>
                      {response?.algorithmUsed === 'PIPELINE' || (!response && formData.selectedAlgorithm === 'PIPELINE')
                        ? 'Multi-Stage Pipeline (Greedy + DP + GA)'
                        : (response?.algorithmUsed || formData.selectedAlgorithm)}
                    </strong>
                    {response?.overallScore ? ` • Utility Score: ${response.overallScore.toFixed(1)} pts` : ''}
                  </p>
                </div>
                {response && (
                  <span className={response.feasible ? 'feasible-badge' : 'alert-danger-box'} style={{ margin: 0, padding: '0.35rem 0.75rem', borderRadius: '2rem' }}>
                    {response.feasible ? '✅ Feasible Plan Found' : '❌ Infeasible Plan'}
                  </span>
                )}
              </div>

              {!response && !error && (
                <div style={{ textAlign: 'center', color: '#94a3b8', padding: '4rem 1rem' }}>
                  <div style={{ fontSize: '3rem', marginBottom: '0.5rem' }}>🌴</div>
                  <h3 style={{ color: '#334155', fontSize: '1.05rem', marginBottom: '0.4rem' }}>No Plan Generated Yet</h3>
                  <p style={{ fontSize: '0.85rem' }}>Configure your travel request on the left and click <strong>"Find My Travel Resource Plan"</strong> to generate your resource recommendations.</p>
                </div>
              )}

              {error && !response && (
                <div style={{ textAlign: 'center', color: '#991b1b', padding: '3rem 1rem' }}>
                  <div style={{ fontSize: '2.5rem', marginBottom: '0.4rem' }}>⚠️</div>
                  <h3 style={{ fontSize: '1rem', marginBottom: '0.3rem' }}>Connection Error</h3>
                  <p style={{ fontSize: '0.82rem' }}>{error}</p>
                </div>
              )}

              {response && (
                <div>
                  {/* 1. TRIP INFORMATION BANNER */}
                  <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: '0.5rem', padding: '0.65rem 0.85rem', marginBottom: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem', fontSize: '0.82rem', color: '#166534' }}>
                    <div>
                      <strong>📍 Destination:</strong> {response.tripInformation?.destination || formData.destination}
                    </div>
                    <div>
                      <strong>📅 Duration:</strong> {response.tripInformation?.tripDurationDays || formData.tripDurationDays} Days ({response.tripInformation?.durationHours || formData.availableHours}h)
                    </div>
                    <div>
                      <strong>👥 Travellers:</strong> {response.tripInformation?.travellerCount || formData.travellerCount} Persons
                    </div>
                  </div>

                  {!response.feasible && (
                    <div className="alert-danger-box" style={{ marginBottom: '1rem' }}>
                      <strong>Constraint Adjustment Guidance:</strong>
                      <ul>{getInfeasibleGuidance().map((note, idx) => <li key={idx}>{note}</li>)}</ul>
                    </div>
                  )}

                  {/* 2. FINANCIAL SUMMARY CARDS */}
                  <div className="metrics-4-grid">
                    <div className="metric-mini-card">
                      <div className="card-icon">💳</div>
                      <div>
                        <label>Total Budget</label>
                        <div className="val">LKR {(response.financialSummary?.totalBudget || formData.totalBudget).toLocaleString()}</div>
                      </div>
                    </div>

                    <div className="metric-mini-card">
                      <div className="card-icon">🛡️</div>
                      <div>
                        <label>Emergency Reserve</label>
                        <div className="val">LKR {(response.financialSummary?.emergencyReserve || formData.emergencyReserve).toLocaleString()}</div>
                      </div>
                    </div>

                    <div className="metric-mini-card">
                      <div className="card-icon">🧮</div>
                      <div>
                        <label>Allocated Cost</label>
                        <div className="val" style={{ color: '#0f172a' }}>LKR {response.totalCost.toLocaleString()}</div>
                      </div>
                    </div>

                    <div className="metric-mini-card">
                      <div className="card-icon">💰</div>
                      <div>
                        <label>Remaining Budget</label>
                        <div className="val" style={{ color: '#16a34a' }}>LKR {response.remainingBudget.toLocaleString()}</div>
                      </div>
                    </div>
                  </div>

                  {/* 3. TIME & PHYSICAL RESOURCE PILLS */}
                  <div className="bottom-pills-grid" style={{ marginBottom: '1.25rem' }}>
                    <div className={`pill-stat-box ${response.totalTimeUsed > formData.availableHours ? 'pill-orange' : 'pill-blue'}`}>
                      <span>{response.totalTimeUsed > formData.availableHours ? '⚠️' : '🕒'}</span>
                      <div>
                        <strong>Time Used:</strong> {response.totalTimeUsed.toFixed(1)}h / {formData.availableHours}h
                        {response.timeSummary && (
                          <span style={{ fontSize: '0.72rem', display: 'block', color: '#475569' }}>
                            Transit: {response.timeSummary.transportationTime.toFixed(1)}h • Activities: {response.timeSummary.activityTime.toFixed(1)}h • Rem: {response.remainingTime.toFixed(1)}h
                          </span>
                        )}
                      </div>
                    </div>

                    <div className="pill-stat-box pill-blue">
                      <span>🎒</span>
                      <div>
                        <strong>Luggage Used:</strong> {response.totalWeight.toFixed(1)} kg / {formData.luggageCapacity} kg
                        {response.physicalResourceSummary && (
                          <span style={{ fontSize: '0.72rem', display: 'block', color: '#475569' }}>
                            Rem. Capacity: {response.remainingCapacity.toFixed(1)} kg
                          </span>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* 4. SELECTED RESOURCES HEADER & CATEGORY TABS */}
                  <div className="tabs-header-row">
                    <h3>Selected Resources ({response.selectedResources?.length || 0})</h3>
                    <div className="tab-pills">
                      <button type="button" className={`tab-pill ${activeTab === 'ALL' ? 'active' : ''}`} onClick={() => setActiveTab('ALL')}>All ({countCategory('ALL')})</button>
                      <button type="button" className={`tab-pill ${activeTab === 'TRANSPORTATION' ? 'active' : ''}`} onClick={() => setActiveTab('TRANSPORTATION')}>Transportation ({countCategory('TRANSPORTATION')})</button>
                      <button type="button" className={`tab-pill ${activeTab === 'ACCOMMODATION' ? 'active' : ''}`} onClick={() => setActiveTab('ACCOMMODATION')}>Accommodation ({countCategory('ACCOMMODATION')})</button>
                      <button type="button" className={`tab-pill ${activeTab === 'ACTIVITIES' ? 'active' : ''}`} onClick={() => setActiveTab('ACTIVITIES')}>Activities ({countCategory('ACTIVITIES')})</button>
                      <button type="button" className={`tab-pill ${activeTab === 'EQUIPMENT' ? 'active' : ''}`} onClick={() => setActiveTab('EQUIPMENT')}>Equipment ({countCategory('EQUIPMENT')})</button>
                    </div>
                  </div>

                  {/* RESOURCE LIST TABLE */}
                  <div className="res-table-header">
                    <span>ITEM & DETAILS</span>
                    <span style={{ textAlign: 'right' }}>COST (LKR)</span>
                    <span style={{ textAlign: 'right' }}>TIME</span>
                    <span style={{ textAlign: 'right' }}>WEIGHT</span>
                  </div>

                  {filteredResources.length > 0 ? (
                    <div>
                      {filteredResources.map((res) => {
                        const cat = getDisplayCategory(res);
                        return (
                          <div key={res.id} className="res-item-row">
                            <div className="res-info">
                              <div className="res-icon-thumb">
                                {cat === 'TRANSPORTATION' ? (res.name.includes('Tuk') ? '🛺' : res.name.includes('Bus') || res.name.includes('Coach') ? '🚌' : '🚆') : cat === 'ACCOMMODATION' ? '🏨' : cat === 'ACTIVITY' ? '⛰️' : '🎒'}
                              </div>
                              <div>
                                <span className="res-cat-tag">{cat === 'PHYSICAL_ITEM' ? 'EQUIPMENT' : cat}</span>
                                <div className="res-title-text">{res.name}</div>
                                <div className="res-sub-text">{getSubLabel(res)}</div>
                              </div>
                            </div>

                            <div className="res-val-col" style={{ textAlign: 'right' }}>{res.cost.toLocaleString()}</div>
                            <div className="res-val-col" style={{ textAlign: 'right' }}>{res.durationHours > 0 ? `${res.durationHours.toFixed(1)} h` : '-'}</div>
                            <div className="res-val-col" style={{ textAlign: 'right' }}>{res.weightKg > 0 ? `${res.weightKg.toFixed(1)} kg` : '0.0 kg'}</div>
                          </div>
                        );
                      })}
                    </div>
                  ) : (
                    <div style={{ textAlign: 'center', padding: '1.5rem', color: '#64748b', fontSize: '0.85rem' }}>
                      No resources selected in this category filter.
                    </div>
                  )}

                  {/* BOTTOM GREEN NOTE */}
                  <div className="pill-green-note" style={{ marginTop: '1rem' }}>
                    🍃 {response.statusMessage || 'This plan gives you the best experience within your constraints. Happy travels!'} 🍃
                  </div>
                </div>
              )}
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
