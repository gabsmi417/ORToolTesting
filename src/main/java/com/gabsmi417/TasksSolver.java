package com.gabsmi417;

import java.util.Optional;

import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;

public class TasksSolver {
    protected final int numTasks;
    protected final int numWorkers;
    protected final int maxTasksPerWorker;
    protected final long[] priorities;
    protected final long[] taskDurations;

    protected IntVar[] taskVars;
    protected IntVar[][] taskToWorkerVars;
    protected final CpModel model;
    protected final CpSolver solver;
    protected CpSolverStatus status;

    public TasksSolver(
                long[] priorities,
                long[] taskDurations,
                int numWorkers,
                int maxTasksPerWorker
    ) {
        this.numTasks = priorities.length;
        if (taskDurations.length != this.numTasks) {
            throw new IllegalArgumentException("The number of task durations must match the number of priorities.");
        }
        this.priorities = priorities;
        this.taskDurations = taskDurations;
        this.numWorkers = numWorkers;
        this.maxTasksPerWorker = maxTasksPerWorker;
        this.model = new CpModel();
        this.solver = new CpSolver();
        initializeVariables();
        initializeConstraints();
        initializeObjective();
    }

    public Optional<Assignment> solve() {
        this.status = solver.solve(this.model);

        if (this.status != CpSolverStatus.OPTIMAL && this.status != CpSolverStatus.FEASIBLE) {
            return Optional.empty();
        } else {
            return Optional.of(new Assignment(this));
        }
    }

    public void printStatistics() {
        System.out.println("Statistics");
        System.out.printf("  conflicts: %d%n", solver.numConflicts());
        System.out.printf("  branches : %d%n", solver.numBranches());
        System.out.printf("  wall time: %f s%n", solver.wallTime());
    }

    private void initializeVariables() {
        this.taskVars = new IntVar[this.numTasks];

        // Create n binary variables: task_0, task_1, ..., task_(n-1)
        for (int i = 0; i < this.numTasks; i++) {
            taskVars[i] = model.newBoolVar("task_" + i);
        }

        this.taskToWorkerVars = new IntVar[this.numTasks][this.numWorkers];

        for (int i = 0; i < this.numTasks; i++) {
            for (int j = 0 ; j < this.numWorkers; j++) {
                taskToWorkerVars[i][j] = model.newBoolVar("task_" + i + "_worker_" + j);
            }
        }
    }

    private void initializeConstraints() {
        // check that each worker is assigned to at most maxTasksPerWorker tasks
        for (int j = 0; j < this.numWorkers; j++) {
            IntVar[] workerTasks = new IntVar[this.numTasks];
            for (int i = 0; i < this.numTasks; i++) {
                workerTasks[i] = taskToWorkerVars[i][j];
            }
            model.addLessOrEqual(LinearExpr.weightedSum(workerTasks, taskDurations), maxTasksPerWorker);
        }

        // check that each task is assigned to at most one workers if done
        for (int i = 0; i < this.numTasks; i++) {
            IntVar t = taskVars[i];
            model.addEquality(LinearExpr.sum(taskToWorkerVars[i]), t);
        }
    }

    private void initializeObjective() {
        model.maximize(LinearExpr.weightedSum(taskVars, this.priorities));
    }
  
}
