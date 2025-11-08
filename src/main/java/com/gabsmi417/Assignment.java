package com.gabsmi417;

import java.util.HashMap;
import java.util.Map;

import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;

public class Assignment {
    public final Map<Integer, Integer[]> assignments; // making this a map as we should eventually have a worker object
    private final CpSolver solver;

    public Assignment(TasksSolver tasksSolver) {
        this.solver = tasksSolver.solver;
        this.assignments = new HashMap<>();

        if (tasksSolver.status != CpSolverStatus.OPTIMAL && tasksSolver.status != CpSolverStatus.FEASIBLE) {
            throw new IllegalArgumentException("No solution found.");
        }

        createAssignments(tasksSolver);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Solution:\n");
        for (Map.Entry<Integer, Integer[]> entry : assignments.entrySet()) {
            sb.append("Worker ").append(entry.getKey()).append(": Tasks ").append(java.util.Arrays.toString(entry.getValue())).append("\n");
        }
        return sb.toString();
    }
    
    private void createAssignments(TasksSolver tasksSolver) {
        for (int i = 0; i < tasksSolver.numTasks; i++) {
            for (int j = 0; j < tasksSolver.numWorkers; j++) {
                if (solver.value(tasksSolver.taskToWorkerVars[i][j]) == 1) {
                    if (!assignments.containsKey(j)) {
                        assignments.put(j, new Integer[0]);
                    }
                    Integer[] currentTasks = assignments.get(j);
                    Integer[] updatedTasks = new Integer[currentTasks.length + 1];
                    System.arraycopy(currentTasks, 0, updatedTasks, 0, currentTasks.length);
                    updatedTasks[currentTasks.length] = i;
                    assignments.put(j, updatedTasks);
                }
            }
        }
    }
  
}
