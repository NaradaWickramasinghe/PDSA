// src/utils/constants.js
// Application-wide constants for Travel-IDSS

// API Endpoints
export const API_ENDPOINTS = {
  ROUTES: '/routes',
  RESOURCES: '/resources',
  NETWORK: '/network',
  DECISIONS: '/decisions',
  OPTIMIZATION: '/optimization',
};

// Algorithm Types
export const ALGORITHMS = {
  ROUTE: {
    DIJKSTRA: 'dijkstra',
    A_STAR: 'a_star',
    BELLMAN_FORD: 'bellman_ford',
    FLOYD_WARSHALL: 'floyd_warshall',
  },
  OPTIMIZATION: {
    GREEDY: 'greedy',
    DYNAMIC_PROGRAMMING: 'dynamic_programming',
    BRANCH_AND_BOUND: 'branch_and_bound',
    GENETIC_ALGORITHM: 'genetic_algorithm',
    SIMULATED_ANNEALING: 'simulated_annealing',
  },
  NETWORK: {
    BFS: 'bfs',
    DFS: 'dfs',
    KRUSKAL: 'kruskal',
    PRIM: 'prim',
  },
};

// Navigation Items
export const NAV_ITEMS = [
  { path: '/', label: 'Dashboard', icon: 'dashboard' },
  { path: '/route-optimization', label: 'Route Optimization', icon: 'route' },
  { path: '/resource-allocation', label: 'Resource Allocation', icon: 'resources' },
  { path: '/network-analysis', label: 'Network Analysis', icon: 'network' },
  { path: '/decision-support', label: 'Decision Support', icon: 'decision' },
  { path: '/optimization', label: 'Optimization', icon: 'optimization' },
];

// Chart Colors
export const CHART_COLORS = {
  primary: '#6366f1',
  secondary: '#8b5cf6',
  success: '#10b981',
  warning: '#f59e0b',
  danger: '#ef4444',
  info: '#3b82f6',
  palette: ['#6366f1', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444', '#3b82f6', '#ec4899', '#14b8a6'],
};

// Status Types
export const STATUS = {
  IDLE: 'idle',
  LOADING: 'loading',
  SUCCESS: 'success',
  ERROR: 'error',
};

// Pagination Defaults
export const PAGINATION = {
  DEFAULT_PAGE: 1,
  DEFAULT_PAGE_SIZE: 10,
  PAGE_SIZE_OPTIONS: [5, 10, 20, 50],
};
