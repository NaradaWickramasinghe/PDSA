package com.nibm.intelligenttravelmanagementsystem.resourceallocation.service;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationProblem;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.AllocationResult;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceCategory;
import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@Getter
@Setter
public class GeneticAllocationService implements AllocationAlgorithm {

    private static final String ALGORITHM_NAME = "GENETIC";
    private static final double EPSILON = 1e-6;

    private int populationSize = 50;
    private int generations = 100;
    private double crossoverRate = 0.8;
    private double mutationRate = 0.05;
    private Long randomSeed = null;

    public GeneticAllocationService() {
    }

    public GeneticAllocationService(int populationSize, int generations, double mutationRate, Long randomSeed) {
        this.populationSize = populationSize;
        this.generations = generations;
        this.mutationRate = mutationRate;
        this.randomSeed = randomSeed;
    }

    @Override
    public AllocationResult allocate(AllocationProblem problem) {
        long startTime = System.currentTimeMillis();

        if (problem == null || problem.getCandidateOptions() == null || problem.getCandidateOptions().isEmpty()) {
            return AllocationResult.infeasible(ALGORITHM_NAME, "No candidate resources provided for Genetic Algorithm.");
        }

        double effectiveBudget = problem.getEffectiveBudget();
        double maxTime = problem.getMaxAvailableHours();
        double maxCapacity = problem.getMaxCarryingCapacityKg();

        if (effectiveBudget <= 0 && maxTime <= 0 && maxCapacity <= 0) {
            return AllocationResult.infeasible(ALGORITHM_NAME, "Constraints permit no available allocation capacity.");
        }

        List<ResourceOption> candidates = new ArrayList<>();
        for (ResourceOption option : problem.getCandidateOptions()) {
            if (option != null && option.isAvailable()) {
                candidates.add(option);
            }
        }

        if (candidates.isEmpty()) {
            return AllocationResult.infeasible(ALGORITHM_NAME, "No available candidate resources found.");
        }

        Random random = (randomSeed != null) ? new Random(randomSeed) : new Random();
        int numGenes = candidates.size();

        List<Chromosome> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            Chromosome chromosome = new Chromosome(numGenes);
            for (int g = 0; g < numGenes; g++) {
                chromosome.genes[g] = random.nextDouble() < 0.3;
            }
            evaluateFitness(chromosome, candidates, problem);
            population.add(chromosome);
        }

        Chromosome bestOverall = null;

        for (int gen = 0; gen < generations; gen++) {
            population.sort((c1, c2) -> Double.compare(c2.fitness, c1.fitness));

            if (bestOverall == null || population.get(0).fitness > bestOverall.fitness) {
                bestOverall = population.get(0).clone();
            }

            List<Chromosome> nextGeneration = new ArrayList<>();
            nextGeneration.add(population.get(0).clone());
            if (populationSize > 1) {
                nextGeneration.add(population.get(1).clone());
            }

            while (nextGeneration.size() < populationSize) {
                Chromosome parent1 = tournamentSelect(population, random);
                Chromosome parent2 = tournamentSelect(population, random);

                Chromosome child1;
                Chromosome child2;
                if (random.nextDouble() < crossoverRate) {
                    Chromosome[] offspring = singlePointCrossover(parent1, parent2, random);
                    child1 = offspring[0];
                    child2 = offspring[1];
                } else {
                    child1 = parent1.clone();
                    child2 = parent2.clone();
                }

                mutate(child1, mutationRate, random);
                mutate(child2, mutationRate, random);

                evaluateFitness(child1, candidates, problem);
                evaluateFitness(child2, candidates, problem);

                nextGeneration.add(child1);
                if (nextGeneration.size() < populationSize) {
                    nextGeneration.add(child2);
                }
            }

            population = nextGeneration;
        }

        if (bestOverall != null) {
            evaluateFitness(bestOverall, candidates, problem);
        }

        boolean isFeasible = bestOverall != null && bestOverall.valid && bestOverall.totalCost > 0;
        List<ResourceOption> selectedResources = new ArrayList<>();

        if (isFeasible) {
            for (int i = 0; i < numGenes; i++) {
                if (bestOverall.genes[i]) {
                    selectedResources.add(candidates.get(i));
                }
            }
        }

        long executionTimeMs = System.currentTimeMillis() - startTime;
        String message = isFeasible 
                ? "Genetic Algorithm travel plan generated successfully." 
                : "INFEASIBLE: Genetic search could not find a valid travel plan.";

        return AllocationResult.builder()
                .algorithmName(ALGORITHM_NAME)
                .feasible(isFeasible)
                .selectedResources(selectedResources)
                .totalCost(isFeasible ? bestOverall.totalCost : 0.0)
                .remainingBudget(isFeasible ? Math.max(0.0, effectiveBudget - bestOverall.totalCost) : 0.0)
                .totalTime(isFeasible ? bestOverall.totalTime : 0.0)
                .remainingTime(isFeasible ? Math.max(0.0, maxTime - bestOverall.totalTime) : 0.0)
                .totalWeight(isFeasible ? bestOverall.totalWeight : 0.0)
                .remainingCapacity(isFeasible ? Math.max(0.0, maxCapacity - bestOverall.totalWeight) : 0.0)
                .overallScore(isFeasible ? bestOverall.totalUsefulness : 0.0)
                .executionTimeMs(executionTimeMs)
                .statusMessage(message)
                .build();
    }

    @Override
    public String getAlgorithmName() {
        return ALGORITHM_NAME;
    }

    private void evaluateFitness(Chromosome chromosome, List<ResourceOption> candidates, AllocationProblem problem) {
        double totalCost = 0.0;
        double totalTime = 0.0;
        double totalWeight = 0.0;
        double totalUsefulness = 0.0;

        boolean selectedTransport = false;
        boolean selectedAccommodation = false;

        boolean hasTransportInCandidates = candidates.stream().anyMatch(c -> c.getCategory() == ResourceCategory.TRANSPORTATION);
        boolean hasAccommodationInCandidates = candidates.stream().anyMatch(c -> c.getCategory() == ResourceCategory.ACCOMMODATION);

        for (int i = 0; i < chromosome.genes.length; i++) {
            if (chromosome.genes[i]) {
                ResourceOption option = candidates.get(i);
                if (option.getCategory() == ResourceCategory.TRANSPORTATION) selectedTransport = true;
                if (option.getCategory() == ResourceCategory.ACCOMMODATION) selectedAccommodation = true;

                totalCost += option.getCost();
                totalTime += option.getDurationHours();
                totalWeight += option.getWeightKg();
                totalUsefulness += option.getUsefulness();
            }
        }

        // Reward complete plan role coverage
        if (hasTransportInCandidates && selectedTransport) totalUsefulness += 50.0;
        if (hasAccommodationInCandidates && selectedAccommodation) totalUsefulness += 50.0;

        chromosome.totalCost = totalCost;
        chromosome.totalTime = totalTime;
        chromosome.totalWeight = totalWeight;
        chromosome.totalUsefulness = totalUsefulness;

        double budgetViolation = Math.max(0.0, totalCost - problem.getEffectiveBudget());
        double timeViolation = Math.max(0.0, totalTime - problem.getMaxAvailableHours());
        double weightViolation = Math.max(0.0, totalWeight - problem.getMaxCarryingCapacityKg());

        boolean isValid = (budgetViolation <= EPSILON) && (timeViolation <= EPSILON) && (weightViolation <= EPSILON);
        chromosome.valid = isValid;

        if (isValid) {
            chromosome.fitness = totalUsefulness;
        } else {
            double penalty = 1000.0 * (budgetViolation + timeViolation + weightViolation);
            chromosome.fitness = Math.max(0.0, totalUsefulness - penalty);
        }
    }

    private Chromosome tournamentSelect(List<Chromosome> population, Random random) {
        int tournamentSize = Math.min(3, population.size());
        Chromosome best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Chromosome contestant = population.get(random.nextInt(population.size()));
            if (best == null || contestant.fitness > best.fitness) {
                best = contestant;
            }
        }
        return best;
    }

    private Chromosome[] singlePointCrossover(Chromosome p1, Chromosome p2, Random random) {
        int len = p1.genes.length;
        Chromosome c1 = new Chromosome(len);
        Chromosome c2 = new Chromosome(len);

        int point = random.nextInt(len);

        for (int i = 0; i < len; i++) {
            if (i < point) {
                c1.genes[i] = p1.genes[i];
                c2.genes[i] = p2.genes[i];
            } else {
                c1.genes[i] = p2.genes[i];
                c2.genes[i] = p1.genes[i];
            }
        }

        return new Chromosome[]{c1, c2};
    }

    private void mutate(Chromosome chromosome, double rate, Random random) {
        for (int i = 0; i < chromosome.genes.length; i++) {
            if (random.nextDouble() < rate) {
                chromosome.genes[i] = !chromosome.genes[i];
            }
        }
    }

    private static class Chromosome implements Cloneable {
        boolean[] genes;
        double fitness;
        double totalCost;
        double totalTime;
        double totalWeight;
        double totalUsefulness;
        boolean valid;

        Chromosome(int size) {
            this.genes = new boolean[size];
        }

        @Override
        public Chromosome clone() {
            try {
                Chromosome cloned = (Chromosome) super.clone();
                cloned.genes = this.genes.clone();
                return cloned;
            } catch (CloneNotSupportedException e) {
                Chromosome fallback = new Chromosome(this.genes.length);
                System.arraycopy(this.genes, 0, fallback.genes, 0, this.genes.length);
                fallback.fitness = this.fitness;
                fallback.totalCost = this.totalCost;
                fallback.totalTime = this.totalTime;
                fallback.totalWeight = this.totalWeight;
                fallback.totalUsefulness = this.totalUsefulness;
                fallback.valid = this.valid;
                return fallback;
            }
        }
    }

    @lombok.Getter
    public static class GeneticStageResult {
        private final ResourceOption selectedAccommodation;
        private final List<ResourceOption> selectedActivities;
        private final double totalCost;
        private final double totalTime;
        private final double overallScore;

        public GeneticStageResult(ResourceOption selectedAccommodation, List<ResourceOption> selectedActivities, double totalCost, double totalTime, double overallScore) {
            this.selectedAccommodation = selectedAccommodation;
            this.selectedActivities = selectedActivities != null ? selectedActivities : Collections.emptyList();
            this.totalCost = totalCost;
            this.totalTime = totalTime;
            this.overallScore = overallScore;
        }
    }

    /**
     * Pipeline Stage 3: Genetic Algorithm Experience Optimization (Accommodation + Activities).
     * Optimizes multi-objective trade-offs between accommodation standard and activity assortment
     * using population evolution, crossover, mutation, and elitism under remaining budget and time.
     */
    public GeneticStageResult optimizeAccommodationAndActivities(AllocationProblem problem, double remainingBudget, double remainingTimeHours, int travellerCount, int tripDurationDays) {
        if (problem == null || problem.getCandidateOptions() == null) {
            return new GeneticStageResult(null, Collections.emptyList(), 0.0, 0.0, 0.0);
        }

        List<ResourceOption> accommodationCandidates = problem.getCandidateOptions().stream()
                .filter(o -> o != null && o.isAvailable() && o.getCategory() == ResourceCategory.ACCOMMODATION)
                .toList();

        List<ResourceOption> activityCandidates = problem.getCandidateOptions().stream()
                .filter(o -> o != null && o.isAvailable() && o.getCategory() == ResourceCategory.ACTIVITY)
                .toList();

        int numAccommodations = accommodationCandidates.size();
        int numActivities = activityCandidates.size();

        if (numAccommodations == 0 && numActivities == 0) {
            return new GeneticStageResult(null, Collections.emptyList(), 0.0, 0.0, 0.0);
        }

        boolean requiresAccommodation = tripDurationDays > 1 && numAccommodations > 0;
        int nights = tripDurationDays > 1 ? Math.max(1, tripDurationDays - 1) : 0;

        Random random = (randomSeed != null) ? new Random(randomSeed) : new Random();
        int popSize = Math.max(30, populationSize);
        int maxGens = Math.max(40, generations);

        class Stage3Chromosome implements Cloneable {
            int accommIdx;
            boolean[] activityBits;
            double cost;
            double time;
            double usefulness;
            double fitness;
            boolean valid;

            Stage3Chromosome() {
                this.activityBits = new boolean[numActivities];
                this.accommIdx = -1;
            }

            @Override
            public Stage3Chromosome clone() {
                Stage3Chromosome c = new Stage3Chromosome();
                c.accommIdx = this.accommIdx;
                c.activityBits = this.activityBits.clone();
                c.cost = this.cost;
                c.time = this.time;
                c.usefulness = this.usefulness;
                c.fitness = this.fitness;
                c.valid = this.valid;
                return c;
            }
        }

        java.util.function.Consumer<Stage3Chromosome> evaluator = (chrom) -> {
            double cCost = 0.0;
            double cTime = 0.0;
            double cScore = 0.0;

            if (chrom.accommIdx >= 0 && chrom.accommIdx < numAccommodations) {
                ResourceOption acc = accommodationCandidates.get(chrom.accommIdx);
                cCost += acc.getCost() * (nights > 0 ? nights : 1);
                cScore += acc.getUsefulness();
            }

            int selectedActCount = 0;
            for (int i = 0; i < numActivities; i++) {
                if (chrom.activityBits[i]) {
                    ResourceOption act = activityCandidates.get(i);
                    cCost += act.getCost();
                    cTime += act.getDurationHours();
                    cScore += act.getUsefulness();
                    selectedActCount++;
                }
            }

            chrom.cost = cCost;
            chrom.time = cTime;
            chrom.usefulness = cScore;

            double budgetViolation = Math.max(0.0, cCost - remainingBudget);
            double timeViolation = Math.max(0.0, cTime - remainingTimeHours);
            boolean roleViolation = requiresAccommodation && chrom.accommIdx < 0;

            boolean isValid = (budgetViolation <= EPSILON) && (timeViolation <= EPSILON) && !roleViolation;
            chrom.valid = isValid;

            if (isValid) {
                double diversityBonus = selectedActCount * 5.0;
                double budgetSavingsBonus = (remainingBudget > 0) ? ((remainingBudget - cCost) / remainingBudget) * 5.0 : 0.0;
                chrom.fitness = cScore + diversityBonus + budgetSavingsBonus;
            } else {
                double penalty = 1000.0 * (budgetViolation + timeViolation * 500.0) + (roleViolation ? 5000.0 : 0.0);
                chrom.fitness = Math.max(0.0, cScore - penalty);
            }
        };

        List<Stage3Chromosome> pop = new ArrayList<>();
        for (int i = 0; i < popSize; i++) {
            Stage3Chromosome c = new Stage3Chromosome();
            if (requiresAccommodation) {
                c.accommIdx = random.nextInt(numAccommodations);
            }
            for (int a = 0; a < numActivities; a++) {
                c.activityBits[a] = random.nextDouble() < 0.35;
            }
            evaluator.accept(c);
            pop.add(c);
        }

        Stage3Chromosome bestOverall = null;

        for (int g = 0; g < maxGens; g++) {
            pop.sort((a, b) -> Double.compare(b.fitness, a.fitness));

            if (bestOverall == null || pop.get(0).fitness > bestOverall.fitness) {
                bestOverall = pop.get(0).clone();
            }

            List<Stage3Chromosome> nextGen = new ArrayList<>();
            nextGen.add(pop.get(0).clone());
            if (pop.size() > 1) {
                nextGen.add(pop.get(1).clone());
            }

            while (nextGen.size() < popSize) {
                Stage3Chromosome p1 = pop.get(random.nextInt(Math.min(5, pop.size())));
                Stage3Chromosome p2 = pop.get(random.nextInt(pop.size()));

                Stage3Chromosome child1 = p1.clone();
                Stage3Chromosome child2 = p2.clone();

                if (random.nextDouble() < crossoverRate) {
                    if (random.nextBoolean()) {
                        int tmp = child1.accommIdx;
                        child1.accommIdx = child2.accommIdx;
                        child2.accommIdx = tmp;
                    }
                    if (numActivities > 1) {
                        int pt = random.nextInt(numActivities);
                        for (int k = pt; k < numActivities; k++) {
                            boolean bTmp = child1.activityBits[k];
                            child1.activityBits[k] = child2.activityBits[k];
                            child2.activityBits[k] = bTmp;
                        }
                    }
                }

                if (requiresAccommodation && random.nextDouble() < mutationRate) {
                    child1.accommIdx = random.nextInt(numAccommodations);
                }
                if (requiresAccommodation && random.nextDouble() < mutationRate) {
                    child2.accommIdx = random.nextInt(numAccommodations);
                }
                for (int k = 0; k < numActivities; k++) {
                    if (random.nextDouble() < (1.0 / Math.max(1, numActivities))) {
                        child1.activityBits[k] = !child1.activityBits[k];
                    }
                    if (random.nextDouble() < (1.0 / Math.max(1, numActivities))) {
                        child2.activityBits[k] = !child2.activityBits[k];
                    }
                }

                evaluator.accept(child1);
                evaluator.accept(child2);

                nextGen.add(child1);
                if (nextGen.size() < popSize) {
                    nextGen.add(child2);
                }
            }

            pop = nextGen;
        }

        if (bestOverall != null) {
            evaluator.accept(bestOverall);
        }

        ResourceOption selectedAccommodation = null;
        List<ResourceOption> selectedActivities = new ArrayList<>();
        double totalCost = 0.0;
        double totalTime = 0.0;
        double totalScore = 0.0;

        if (bestOverall != null && (bestOverall.valid || bestOverall.cost <= remainingBudget + EPSILON)) {
            if (bestOverall.accommIdx >= 0 && bestOverall.accommIdx < numAccommodations) {
                selectedAccommodation = accommodationCandidates.get(bestOverall.accommIdx);
                totalCost += selectedAccommodation.getCost() * (nights > 0 ? nights : 1);
                totalScore += selectedAccommodation.getUsefulness();
            }

            for (int i = 0; i < numActivities; i++) {
                if (bestOverall.activityBits[i]) {
                    ResourceOption act = activityCandidates.get(i);
                    selectedActivities.add(act);
                    totalCost += act.getCost();
                    totalTime += act.getDurationHours();
                    totalScore += act.getUsefulness();
                }
            }
        } else {
            // Greedy fallback repair if constraints were exceptionally tight
            double curCost = 0.0;
            double curTime = 0.0;
            if (requiresAccommodation) {
                for (ResourceOption acc : accommodationCandidates) {
                    double aCost = acc.getCost() * (nights > 0 ? nights : 1);
                    if (aCost <= remainingBudget) {
                        selectedAccommodation = acc;
                        curCost += aCost;
                        totalScore += acc.getUsefulness();
                        break;
                    }
                }
            }
            for (ResourceOption act : activityCandidates) {
                if (curCost + act.getCost() <= remainingBudget + EPSILON && curTime + act.getDurationHours() <= remainingTimeHours + EPSILON) {
                    selectedActivities.add(act);
                    curCost += act.getCost();
                    curTime += act.getDurationHours();
                    totalScore += act.getUsefulness();
                }
            }
            totalCost = curCost;
            totalTime = curTime;
        }

        return new GeneticStageResult(selectedAccommodation, selectedActivities, totalCost, totalTime, totalScore);
    }
}
