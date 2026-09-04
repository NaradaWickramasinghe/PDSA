import React, { useState } from 'react';

const API_BASE_URL = 'http://localhost:8080/api/resource-allocation';

const destImages = {
  'Ella': { src: '/images/destinations/ella.jpg', title: 'Nine Arch Bridge, Ella' },
  'Kandy': { src: '/images/destinations/kandy.jpg', title: 'Temple of the Tooth (Sri Dalada Maligawa), Kandy' },
  'Nuwara Eliya': { src: '/images/destinations/nuwara-eliya.jpg', title: 'Historic Post Office & Tea Country, Nuwara Eliya' },
  'Sigiriya': { src: '/images/destinations/sigiriya.jpg', title: 'Sigiriya Ancient Rock Fortress' },
  'Galle': { src: '/images/destinations/galle.jpg', title: 'Galle Fort Lighthouse & Ramparts' },
  'Mirissa': { src: '/images/destinations/mirissa.jpg', title: 'Coconut Tree Hill, Mirissa' },
  'Colombo': { src: '/images/destinations/colombo.jpg', title: 'Lotus Tower, Colombo' }
};

const strategyNames = {
  'PIPELINE': 'Smart Multi-Tier Optimization',
  'DYNAMIC_PROGRAMMING': 'Maximum Value Optimization',
  'GREEDY': 'Budget-First Optimization',
  'GENETIC': 'Flexible Discovery Optimization'
};

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
        { name: 'Budget-First Strategy', data: greedyData },
        { name: 'Optimal Value Strategy', data: dpData },
        { name: 'Balanced Discovery Strategy', data: geneticData }
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
  };
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
          flex-direction: column;
          min-height: 100vh;
          background-color: #f8fafc;
          color: #1e293b;
        }

        .header-brand {
          display: flex;
          align-items: center;
          gap: 0.75rem;
        }
        .header-brand .logo-icon {
          width: 36px;
          height: 36px;
          background: #16a34a;
          color: #ffffff;
          border-radius: 0.5rem;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 700;
          font-size: 1.2rem;
          box-shadow: 0 2px 4px rgba(22, 163, 74, 0.2);
        }
        .header-brand .brand-title {
          font-size: 1.12rem;
          font-weight: 700;
          color: #0f172a;
          line-height: 1.2;
        }
        .header-brand .brand-sub {
          font-size: 0.75rem;
          color: #64748b;
          font-weight: 500;
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

        /* CATEGORY CARDS STYLING */
        .res-cards-list {
          display: flex;
          flex-direction: column;
          gap: 0.75rem;
          margin-top: 0.5rem;
        }
        .res-card-item {
          background: #ffffff;
          border: 1px solid #e2e8f0;
          border-radius: 0.65rem;
          padding: 0.9rem 1rem;
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          gap: 1rem;
          transition: all 0.15s ease;
          box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
        }
        .res-card-item:hover {
          border-color: #cbd5e1;
          box-shadow: 0 3px 6px rgba(0, 0, 0, 0.04);
          background: #fbfcfd;
        }
        .res-card-left {
          display: flex;
          gap: 0.85rem;
          align-items: flex-start;
          flex: 1;
          min-width: 0;
        }
        .res-cat-icon-box {
          width: 42px;
          height: 42px;
          border-radius: 0.55rem;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 1.25rem;
          flex-shrink: 0;
        }
        .icon-trans { background: #eff6ff; border: 1px solid #bfdbfe; color: #1d4ed8; }
        .icon-stay { background: #fdf2f8; border: 1px solid #fbcfe8; color: #be185d; }
        .icon-act { background: #f0fdf4; border: 1px solid #bbf7d0; color: #15803d; }
        .icon-gear { background: #fff7ed; border: 1px solid #fed7aa; color: #c2410c; }

        .res-card-details {
          flex: 1;
          min-width: 0;
        }
        .res-tag-row {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          margin-bottom: 0.25rem;
          flex-wrap: wrap;
        }
        .badge-cat-tag {
          font-size: 0.68rem;
          font-weight: 700;
          padding: 0.15rem 0.5rem;
          border-radius: 0.3rem;
          text-transform: uppercase;
          letter-spacing: 0.03em;
        }
        .tag-trans { background: #dbeafe; color: #1e40af; }
        .tag-stay { background: #fce7f3; color: #9d174d; }
        .tag-act { background: #dcfce7; color: #166534; }
        .tag-gear { background: #ffedd5; color: #9a3412; }

        .res-card-title {
          font-size: 0.95rem;
          font-weight: 700;
          color: #0f172a;
          line-height: 1.3;
          margin-bottom: 0.25rem;
        }
        .res-card-desc {
          font-size: 0.78rem;
          color: #475569;
          line-height: 1.4;
          margin-bottom: 0.45rem;
        }
        .res-attributes-chips {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          flex-wrap: wrap;
        }
        .attr-chip {
          background: #f1f5f9;
          border: 1px solid #e2e8f0;
          color: #334155;
          font-size: 0.75rem;
          font-weight: 500;
          padding: 0.2rem 0.55rem;
          border-radius: 0.35rem;
          display: inline-flex;
          align-items: center;
          gap: 0.3rem;
        }
        .res-card-right {
          text-align: right;
          flex-shrink: 0;
          padding-top: 0.1rem;
        }
        .res-price-main {
          font-size: 1.05rem;
          font-weight: 700;
          color: #0f172a;
        }
        .res-price-sub {
          font-size: 0.72rem;
          color: #64748b;
          margin-top: 0.15rem;
        }

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

      {/* MAIN WRAPPER */}
      <div className="main-wrapper">
        {/* TOP HEADER NAVBAR */}
        <header className="top-header">
          <div className="header-brand">
            <div className="logo-icon">🌴</div>
            <div>
              <div className="brand-title">Travel Planner</div>
              <div className="brand-sub">Sri Lanka</div>
            </div>
          </div>
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
              📊 Compare Planning Options
            </button>
          </div>

          {/* PLANNING STRATEGY COMPARISON DRAWER */}
          {showComparison && (
            <div className="comp-container" style={{ marginBottom: '1.5rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <h4 style={{ fontSize: '0.92rem', color: '#0f172a', fontWeight: 700 }}>Planning Strategy Comparison</h4>
                <button type="button" onClick={handleRunComparison} disabled={comparing} style={{ background: '#16a34a', color: '#fff', border: 'none', padding: '0.45rem 0.85rem', borderRadius: '0.45rem', fontSize: '0.8rem', fontWeight: 600, cursor: 'pointer' }}>
                  {comparing ? 'Evaluating...' : '⚡ Compare Options'}
                </button>
              </div>

              {comparisonResults ? (
                <table className="comp-table-ref">
                  <thead>
                    <tr>
                      <th>Strategy</th>
                      <th>Feasibility</th>
                      <th>Experience Score</th>
                      <th>Total Cost (LKR)</th>
                      <th>Time Used</th>
                      <th>Weight Used</th>
                      <th>Response Speed</th>
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
                <p style={{ fontSize: '0.8rem', color: '#64748b' }}>Click "Compare Options" to evaluate different planning strategies in real-time.</p>
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
                        src={(destImages[formData.destination] || destImages['Ella']).src} 
                        alt={(destImages[formData.destination] || destImages['Ella']).title} 
                        title={(destImages[formData.destination] || destImages['Ella']).title}
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

                {/* Advanced Preferences */}
                <div>
                  <button type="button" onClick={() => setShowAdvanced(!showAdvanced)} style={{ background: 'none', border: 'none', color: '#16a34a', fontSize: '0.78rem', fontWeight: 600, cursor: 'pointer' }}>
                    {showAdvanced ? '▲ Hide Advanced Preferences' : '⚙️ Advanced Planning Preferences'}
                  </button>
                </div>

                {showAdvanced && (
                  <div style={{ background: '#f8fafc', padding: '0.65rem', borderRadius: '0.4rem', border: '1px solid #e2e8f0', marginTop: '0.35rem' }}>
                    <label style={{ fontSize: '0.75rem', fontWeight: 600, color: '#334155' }}>Planning Optimization Mode</label>
                    <select name="selectedAlgorithm" value={formData.selectedAlgorithm} onChange={handleChange} className="ref-input no-icon" style={{ marginTop: '0.2rem', padding: '0.45rem' }}>
                      <option value="PIPELINE">Smart Multi-Tier Optimizer (Recommended - Best Balance)</option>
                      <option value="DYNAMIC_PROGRAMMING">Maximum Value Focus (Best Resource Utilization)</option>
                      <option value="GREEDY">Budget-First Focus (High Value-to-Cost Priority)</option>
                      <option value="GENETIC">Flexible Discovery Focus (Diverse Experience Mix)</option>
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
                  <div style={{ marginTop: '0.35rem', display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                    <span style={{ background: '#e0f2fe', color: '#0369a1', fontSize: '0.74rem', fontWeight: 700, padding: '0.2rem 0.55rem', borderRadius: '0.3rem' }}>Allocation Pipeline</span>
                  </div>
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
                        <strong>Equipment Weight:</strong> {response.totalWeight.toFixed(1)} kg / {formData.luggageCapacity} kg
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

                  {/* RESOURCE LIST CARDS */}
                  {filteredResources.length > 0 ? (
                    <div className="res-cards-list">
                      {filteredResources.map((res) => {
                        const cat = getDisplayCategory(res);
                        const nights = response.tripInformation?.tripDurationDays || formData.tripDurationDays || 3;
                        const travellers = response.tripInformation?.travellerCount || formData.travellerCount || 2;

                        let icon = '🎒';
                        let iconClass = 'icon-gear';
                        let tagClass = 'tag-gear';
                        let catLabel = 'Equipment';
                        let chips = null;
                        let priceMain = `LKR ${res.cost.toLocaleString()}`;
                        let priceSub = '';

                        if (cat === 'TRANSPORTATION') {
                          catLabel = 'Transportation';
                          tagClass = 'tag-trans';
                          iconClass = 'icon-trans';
                          icon = res.name.toLowerCase().includes('tuk') ? '🛺' 
                               : res.name.toLowerCase().includes('bus') ? '🚌' 
                               : res.name.toLowerCase().includes('train') ? '🚆' : '🚗';
                          chips = (
                            <>
                              <span className="attr-chip">⏱️ {res.durationHours > 0 ? `${res.durationHours.toFixed(1)} hours` : 'Direct'}</span>
                              <span className="attr-chip">👥 Capacity: {res.capacity ? `${res.capacity} people` : 'Standard'}</span>
                              <span className="attr-chip">🚗 {res.transportType || 'Transit'}</span>
                            </>
                          );
                          priceSub = 'Allocated transit fare';
                        } else if (cat === 'ACCOMMODATION') {
                          catLabel = 'Accommodation';
                          tagClass = 'tag-stay';
                          iconClass = 'icon-stay';
                          icon = '🏨';
                          const ratePerNight = Math.round(res.cost / Math.max(1, nights));
                          chips = (
                            <>
                              <span className="attr-chip">📅 {nights} nights</span>
                              <span className="attr-chip">👥 Capacity: {res.capacity ? `${res.capacity} guests` : 'Room'}</span>
                              <span className="attr-chip">💵 LKR {ratePerNight.toLocaleString()} / night</span>
                            </>
                          );
                          priceMain = `LKR ${res.cost.toLocaleString()} total`;
                          priceSub = `Total for ${nights} nights`;
                        } else if (cat === 'ACTIVITY') {
                          catLabel = 'Activity';
                          tagClass = 'tag-act';
                          iconClass = 'icon-act';
                          icon = res.name.toLowerCase().includes('water') || res.name.toLowerCase().includes('falls') || res.name.toLowerCase().includes('beach') || res.name.toLowerCase().includes('surf') ? '🌊'
                               : res.name.toLowerCase().includes('temple') || res.name.toLowerCase().includes('heritage') || res.name.toLowerCase().includes('fort') ? '🏛️' : '⛰️';
                          chips = (
                            <>
                              <span className="attr-chip">⏱️ {res.durationHours.toFixed(1)} hours</span>
                              <span className="attr-chip">👥 {travellers > 1 ? `For ${travellers} travellers` : 'Per ticket'}</span>
                            </>
                          );
                          priceMain = `LKR ${res.cost.toLocaleString()}`;
                          priceSub = travellers > 1 ? 'Total group cost' : 'Activity fee';
                        } else if (cat === 'PHYSICAL_ITEM') {
                          catLabel = 'Equipment';
                          tagClass = 'tag-gear';
                          iconClass = 'icon-gear';
                          icon = res.name.toLowerCase().includes('shoe') || res.name.toLowerCase().includes('pole') ? '🥾'
                               : res.name.toLowerCase().includes('medical') || res.name.toLowerCase().includes('first aid') ? '🩹'
                               : res.name.toLowerCase().includes('power') || res.name.toLowerCase().includes('torch') || res.name.toLowerCase().includes('lamp') ? '🔦' : '🎒';
                          const qualityBadge = (res.usefulness && res.usefulness >= 90) ? '⭐ Highly Recommended' : 'Essential Gear';
                          chips = (
                            <>
                              <span className="attr-chip">🎒 {res.weightKg > 0 ? `${res.weightKg.toFixed(1)} kg` : '0.2 kg'}</span>
                              <span className="attr-chip">{qualityBadge}</span>
                            </>
                          );
                          priceSub = 'Item cost / rental';
                        }

                        const isHighlight = (res.usefulness && res.usefulness >= 92);

                        return (
                          <div key={res.id} className="res-card-item">
                            <div className="res-card-left">
                              <div className={`res-cat-icon-box ${iconClass}`}>{icon}</div>
                              <div className="res-card-details">
                                <div className="res-tag-row">
                                  <span className={`badge-cat-tag ${tagClass}`}>{catLabel}</span>
                                  {isHighlight && (
                                    <span style={{ background: '#fef3c7', color: '#92400e', fontSize: '0.68rem', fontWeight: 700, padding: '1px 6px', borderRadius: '4px' }}>⭐ Top Pick</span>
                                  )}
                                </div>
                                <div className="res-card-title">{res.name}</div>
                                <div className="res-card-desc">{getSubLabel(res)}</div>
                                <div className="res-attributes-chips">
                                  {chips}
                                </div>
                              </div>
                            </div>

                            <div className="res-card-right">
                              <div className="res-price-main">{priceMain}</div>
                              <div className="res-price-sub">{priceSub}</div>
                            </div>
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
