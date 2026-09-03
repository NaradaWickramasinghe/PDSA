import { useMemo, useState } from 'react'
import './App.css'

const initialForm = {
  origin: 'A',
  destination: 'F',
  maxTravelTime: 220,
  maxBudget: 140,
  optimizationMethod: 'BRANCH_AND_BOUND',
  objectiveWeights: {
    distanceWeight: 0.45,
    timeWeight: 0.35,
    costWeight: 0.2,
  },
}

const defaultError = 'The optimization request could not be completed.'

function App() {
  const [form, setForm] = useState(initialForm)
  const [validationMessages, setValidationMessages] = useState([])
  const [result, setResult] = useState(null)
  const [benchmark, setBenchmark] = useState(null)
  const [loading, setLoading] = useState({ optimize: false, compare: false })

  const routeSummary = useMemo(() => {
    if (!result || !Array.isArray(result.route) || result.route.length === 0) {
      return 'N/A'
    }
    return result.route.join(' → ')
  }, [result])

  const handleFieldChange = (event) => {
    const { name, value } = event.target

    setForm((current) => {
      if (name in current.objectiveWeights) {
        return {
          ...current,
          objectiveWeights: {
            ...current.objectiveWeights,
            [name]: Number(value),
          },
        }
      }

      return {
        ...current,
        [name]: value,
      }
    })
  }

  const validatePayload = (payload) => {
    const errors = []

    if (!payload.origin) {
      errors.push('Origin is required.')
    }

    if (!payload.destination) {
      errors.push('Destination is required.')
    }

    if (payload.origin && payload.destination && payload.origin === payload.destination) {
      errors.push('Origin and destination must be different.')
    }

    if (!Number.isFinite(payload.maxTravelTime) || payload.maxTravelTime <= 0) {
      errors.push('Maximum travel time must be a positive number.')
    }

    if (!Number.isFinite(payload.maxBudget) || payload.maxBudget <= 0) {
      errors.push('Maximum budget must be a positive number.')
    }

    const weightEntries = Object.values(payload.objectiveWeights)
    if (weightEntries.some((value) => !Number.isFinite(value) || value < 0)) {
      errors.push('Objective weights must be numeric values greater than or equal to 0.')
    }

    const totalWeight = weightEntries.reduce((sum, value) => sum + value, 0)
    if (totalWeight <= 0) {
      errors.push('Objective weights must have a total greater than 0.')
    }

    return { errors, totalWeight }
  }

  const formatMetric = (value, fallback = 'N/A') => {
    if (value === null || value === undefined || Number.isNaN(Number(value))) {
      return fallback
    }
    return Number(value).toFixed(2)
  }

  const renderResult = (data) => {
    if (!data || !data.success) {
      return (
        <div className="error-card">
          <h3>No feasible route found</h3>
          <div>{data?.errorMessage || 'No feasible route was found under the selected constraints.'}</div>
        </div>
      )
    }

    return (
      <>
        <div className="route-badge">Route: {routeSummary}</div>
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-label">Selected Algorithm</div>
            <div className="stat-value">{data.selectedAlgorithm || 'N/A'}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Distance</div>
            <div className="stat-value">{formatMetric(data.totalDistance)} km</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Travel Time</div>
            <div className="stat-value">{formatMetric(data.totalTravelTime)} min</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Cost</div>
            <div className="stat-value">${formatMetric(data.totalCost)}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Objective Score</div>
            <div className="stat-value">{formatMetric(data.objectiveScore)}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Execution Time</div>
            <div className="stat-value">{data.executionTimeMs ?? 'N/A'} ms</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Nodes Explored</div>
            <div className="stat-value">{data.nodesExplored ?? 'N/A'}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Status</div>
            <div className="stat-value">Feasible route found</div>
          </div>
        </div>
        {data.statesExplored !== undefined && data.statesExplored !== null ? (
          <div className="alert success" style={{ marginTop: '16px' }}>
            States explored: {data.statesExplored}
          </div>
        ) : null}
      </>
    )
  }

  const handleOptimize = async (event) => {
    event.preventDefault()
    const payload = { ...form, maxTravelTime: Number(form.maxTravelTime), maxBudget: Number(form.maxBudget) }
    const validation = validatePayload(payload)

    if (validation.errors.length > 0) {
      setValidationMessages(validation.errors)
      setResult({ success: false, errorMessage: validation.errors[0] })
      return
    }

    setValidationMessages(['Optimizing route...'])
    setLoading((current) => ({ ...current, optimize: true }))

    try {
      const response = await fetch('/api/optimization/route', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })

      const data = await response.json()

      if (!response.ok || !data) {
        setResult({ success: false, errorMessage: data?.errorMessage || defaultError })
        return
      }

      setResult(data)
      setBenchmark(null)
      setValidationMessages([])
    } catch (error) {
      setResult({ success: false, errorMessage: 'Unable to connect to the optimization API. Please try again.' })
    } finally {
      setLoading((current) => ({ ...current, optimize: false }))
    }
  }

  const handleBenchmark = async () => {
    const payload = {
      ...form,
      optimizationMethod: 'BENCHMARK',
      maxTravelTime: Number(form.maxTravelTime),
      maxBudget: Number(form.maxBudget),
    }
    const validation = validatePayload(payload)

    if (validation.errors.length > 0) {
      setValidationMessages(validation.errors)
      return
    }

    setValidationMessages(['Comparing all algorithms...'])
    setLoading((current) => ({ ...current, compare: true }))

    try {
      const response = await fetch('/api/optimization/benchmark', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })

      const data = await response.json()

      if (!response.ok || !data || !Array.isArray(data.results)) {
        setBenchmark(null)
        return
      }

      setBenchmark(data)
      setValidationMessages([])
    } catch (error) {
      setBenchmark(null)
    } finally {
      setLoading((current) => ({ ...current, compare: false }))
    }
  }

  return (
    <div className="page-shell">
      <header className="header">
        <p className="eyebrow">PDSA Travel System</p>
        <h1>Intelligent Travel Optimization</h1>
        <p className="subtitle">
          Compare route options, optimize travel plans, and evaluate the best path under time and budget constraints.
        </p>
      </header>

      <main className="content">
        <section className="panel" aria-labelledby="optimization-form-title">
          <div className="panel-header">
            <h2 id="optimization-form-title" className="panel-title">Optimization Input</h2>
            <span className="section-badge">Live API</span>
          </div>

          <form onSubmit={handleOptimize} noValidate>
            <div className="form-grid">
              <div className="field">
                <label htmlFor="origin">Origin</label>
                <select id="origin" name="origin" value={form.origin} onChange={handleFieldChange}>
                  {['A', 'B', 'C', 'D', 'E', 'F'].map((city) => (
                    <option key={city} value={city}>{city}</option>
                  ))}
                </select>
              </div>

              <div className="field">
                <label htmlFor="destination">Destination</label>
                <select id="destination" name="destination" value={form.destination} onChange={handleFieldChange}>
                  {['A', 'B', 'C', 'D', 'E', 'F'].map((city) => (
                    <option key={city} value={city}>{city}</option>
                  ))}
                </select>
              </div>

              <div className="field">
                <label htmlFor="maxTravelTime">Maximum Travel Time</label>
                <input id="maxTravelTime" name="maxTravelTime" type="number" min="1" step="1" value={form.maxTravelTime} onChange={handleFieldChange} />
              </div>

              <div className="field">
                <label htmlFor="maxBudget">Maximum Budget</label>
                <input id="maxBudget" name="maxBudget" type="number" min="1" step="1" value={form.maxBudget} onChange={handleFieldChange} />
              </div>

              <div className="field">
                <label htmlFor="optimizationMethod">Optimization Method</label>
                <select id="optimizationMethod" name="optimizationMethod" value={form.optimizationMethod} onChange={handleFieldChange}>
                  <option value="BRANCH_AND_BOUND">Branch and Bound</option>
                  <option value="GENETIC_ALGORITHM">Genetic Algorithm</option>
                  <option value="PARETO_DYNAMIC_PROGRAMMING">Pareto Dynamic Programming</option>
                </select>
              </div>

              <div className="field">
                <label htmlFor="distanceWeight">Distance Weight</label>
                <input id="distanceWeight" name="distanceWeight" type="number" min="0" step="0.05" value={form.objectiveWeights.distanceWeight} onChange={handleFieldChange} />
              </div>

              <div className="field">
                <label htmlFor="timeWeight">Time Weight</label>
                <input id="timeWeight" name="timeWeight" type="number" min="0" step="0.05" value={form.objectiveWeights.timeWeight} onChange={handleFieldChange} />
              </div>

              <div className="field">
                <label htmlFor="costWeight">Cost Weight</label>
                <input id="costWeight" name="costWeight" type="number" min="0" step="0.05" value={form.objectiveWeights.costWeight} onChange={handleFieldChange} />
              </div>
            </div>

            {validationMessages.length > 0 ? (
              <div className="validation-stack" aria-live="polite">
                {validationMessages.map((message, index) => (
                  <div key={`${message}-${index}`} className={`alert ${index === 0 && validationMessages[0] === 'Optimizing route...' || index === 0 && validationMessages[0] === 'Comparing all algorithms...' ? 'warning' : 'error'}`}>
                    {message}
                  </div>
                ))}
              </div>
            ) : null}

            <div className="button-row">
              <button className="primary-btn" type="submit" disabled={loading.optimize}>
                {loading.optimize ? 'Optimizing route...' : 'Optimize Route'}
              </button>
              <button className="secondary-btn" type="button" onClick={handleBenchmark} disabled={loading.compare}>
                {loading.compare ? 'Comparing...' : 'Compare Algorithms'}
              </button>
            </div>
          </form>
        </section>

        <section className="panel" aria-labelledby="results-title">
          <div className="panel-header">
            <h2 id="results-title" className="panel-title">Optimization Result</h2>
            <span className="section-badge">Result</span>
          </div>
          <div className="result-panel">
            {result ? renderResult(result) : <p className="empty-state">Choose a route and run optimization.</p>}
          </div>
        </section>

        <section className="panel" aria-labelledby="comparison-title">
          <div className="panel-header">
            <h2 id="comparison-title" className="panel-title">Algorithm Comparison</h2>
            <span className="section-badge">Benchmark</span>
          </div>
          <div className="result-panel">
            {benchmark && Array.isArray(benchmark.results) && benchmark.results.length > 0 ? (
              <div className="comparison-grid">
                {benchmark.results.map((item) => (
                  <article key={`${item.selectedAlgorithm}-${item.executionTimeMs}`} className="comparison-card">
                    <h4>{item.selectedAlgorithm || 'Unknown'}</h4>
                    <ul>
                      <li><span>Route</span><strong>{Array.isArray(item.route) && item.route.length > 0 ? item.route.join(' → ') : 'N/A'}</strong></li>
                      <li><span>Distance</span><strong>{formatMetric(item.totalDistance)} km</strong></li>
                      <li><span>Travel Time</span><strong>{formatMetric(item.totalTravelTime)} min</strong></li>
                      <li><span>Cost</span><strong>${formatMetric(item.totalCost)}</strong></li>
                      <li><span>Objective</span><strong>{formatMetric(item.objectiveScore)}</strong></li>
                      <li><span>Execution</span><strong>{item.executionTimeMs ?? 'N/A'} ms</strong></li>
                      <li><span>Nodes</span><strong>{item.nodesExplored ?? 'N/A'}</strong></li>
                      <li><span>States</span><strong>{item.statesExplored ?? 'N/A'}</strong></li>
                      <li><span>Status</span><strong>{item.success ? 'Feasible' : 'No result'}</strong></li>
                    </ul>
                  </article>
                ))}
              </div>
            ) : (
              <p className="empty-state">Run a benchmark to compare all optimization methods.</p>
            )}
          </div>
        </section>

        <section className="panel" aria-labelledby="explanation-title">
          <div className="panel-header">
            <h2 id="explanation-title" className="panel-title">Algorithm Notes</h2>
            <span className="section-badge">Methodology</span>
          </div>
          <div className="legend">
            <div className="legend-item">
              <strong>Branch and Bound</strong>
              Exact optimization approach for constrained route selection.
            </div>
            <div className="legend-item">
              <strong>Genetic Algorithm</strong>
              Heuristic/metaheuristic method for approximate high-quality solutions.
            </div>
            <div className="legend-item">
              <strong>Pareto Dynamic Programming</strong>
              Multi-objective Pareto-based approach for trade-off analysis.
            </div>
          </div>
        </section>
      </main>
    </div>
  )
}

export default App
