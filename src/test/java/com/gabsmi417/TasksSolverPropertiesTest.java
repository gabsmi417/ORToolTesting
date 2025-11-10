package com.gabsmi417;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.assertj.core.api.Assertions;

import com.google.ortools.Loader;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.lifecycle.BeforeTry;

public class TasksSolverPropertiesTest {

    public static boolean IS_LOADED = false;
    @BeforeTry
    public void loadOrTools() {
        if (!IS_LOADED) {
            System.out.println( "Loading Or-Tools... (this may take a moment)");
            Loader.loadNativeLibraries();
            System.out.println("OR-Tools Loaded");
            IS_LOADED = true;
        }
    }

    @Property(tries = 10)
    void eachWorkerHasAtMostThreeTasks(
            @ForAll("taskInputs") TaskInput input
    ) {
        TasksSolver solver = new TasksSolver(
                input.priorities,
                input.durations,
                input.numWorkers
        );

        Optional<Assignment> maybeAssignment = solver.solve();

        // Property: If a feasible solution exists, no worker exceeds the task limit
        maybeAssignment.ifPresent(assignment -> {
            for (Map.Entry<Integer, Integer[]> e : assignment.assignments.entrySet()) {
                long totalWork = 0;
                for (int task : e.getValue()) {
                    totalWork += input.durations[task];
                }
                Assertions.assertThat(totalWork)
                        .as("Worker " + e.getKey() + " should not exceed max tasks")
                        .isLessThanOrEqualTo(TasksSolver.MAX_TASKS_PER_WORKER);
            }
        });

        System.out.println("Size: " + input.priorities.length + ", Workers: " + input.numWorkers);
        solver.printStatistics();
    }

    // ---- Data generation ----

    @Provide
    Arbitrary<TaskInput> taskInputs() {
        Arbitrary<Integer> numTasks = Arbitraries.integers().between(4000, 10000);
        Arbitrary<Integer> numWorkers = Arbitraries.integers().between(10, 30);

        return Combinators.combine(numTasks, numWorkers).as((t, w) -> {
            long[] priorities = new long[t];
            long[] durations = new long[t];
            Random r = new Random();
            for (int i = 0; i < t; i++) {
                priorities[i] = 1 + r.nextInt(5);
                durations[i] = 1; // keep it simple
            }
            return new TaskInput(priorities, durations, w);
        });
    }

    // ---- Helper record ----
    static class TaskInput {
        final long[] priorities;
        final long[] durations;
        final int numWorkers;

        TaskInput(long[] priorities, long[] durations, int numWorkers) {
            this.priorities = priorities;
            this.durations = durations;
            this.numWorkers = numWorkers;
        }
    }
}
