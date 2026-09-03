# Module 5 Optimization Complexity Notes

## Exact method: Branch and Bound

- Best case: O(n) for a very short or direct feasible route with early pruning.
- Average case: O(b^d) in the branching factor and depth of the route graph, with pruning reducing the explored frontier.
- Worst case: O(b^d) when the graph is dense and pruning cannot eliminate many branches.
- Space complexity: O(d + b) for recursion depth, visited nodes, and the current route stack.

The exact method is suitable when the graph is relatively small and correctness is the main priority.

## Heuristic method: Genetic Algorithm

- Best case: O(p _ g _ l) where p is the population size, g the number of generations, and l the route length being evaluated.
- Average case: O(p _ g _ l) with practical runtime depending on mutational pressure and population diversity.
- Worst case: O(p _ g _ l) for a full evaluation cycle, though mutation/crossover costs are limited and bounded by population size.
- Space complexity: O(p \* l) for storing the population and route chromosomes.

This method is useful when the search space is too large for exact enumeration and a good feasible solution is preferred over guaranteed optimality.

## Approximation / multi-objective method: Pareto Frontier

- Best case: O(n \* k) for a narrow frontier with a small number of maintained non-dominated states.
- Average case: O(v _ f _ e) where v is the number of states, f the frontier size, and e the edges considered.
- Worst case: O(v^2) dominated-state checks in dense graphs with a large Pareto frontier.
- Space complexity: O(f \* d) for storing non-dominated route states per node.

This method is appropriate when multiple objectives must be balanced and a trade-off set is desired.

## Observed behavior

The project uses a compact graph for benchmarking, so the experimental results are best interpreted as relative algorithm quality rather than global performance guarantees. The exact method typically gives the strongest objective score under tight constraints, while the genetic method is faster on larger search spaces, and the Pareto frontier is useful for transparent multi-objective trade-offs.
