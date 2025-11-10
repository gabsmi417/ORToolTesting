package com.gabsmi417;

import java.util.Map;
import java.util.Optional;

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

    @Property(tries = 100)
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
    }

    @Property(tries = 100)
    void uniqueTasksAssigned(
            @ForAll("taskInputs") TaskInput input
    ) {
        TasksSolver solver = new TasksSolver(
                input.priorities,
                input.durations,
                input.numWorkers
        );

        Optional<Assignment> maybeAssignment = solver.solve();

        // Property: Each task is assigned to at most one worker
        maybeAssignment.ifPresent(assignment -> {
            boolean[] assignedTasks = new boolean[input.priorities.length];
            for (Map.Entry<Integer, Integer[]> e : assignment.assignments.entrySet()) {
                for (int task : e.getValue()) {
                    Assertions.assertThat(assignedTasks[task])
                            .as("Task " + task + " should be assigned to at most one worker")
                            .isFalse();
                    assignedTasks[task] = true;
                }
            }
        });
    }

    @Provide
    Arbitrary<TaskInput> taskInputs() {
        Arbitrary<Integer> numTasks = Arbitraries.integers().between(10, 600);
        Arbitrary<Integer> numWorkers = Arbitraries.integers().between(10, 30);

        return numTasks.flatMap(t -> {
            Arbitrary<long[]> prioritiesArb = Arbitraries.integers()
                    .between(1, 10)
                    .array(long[].class)
                    .ofSize(t);

            Arbitrary<long[]> durationsArb  = Arbitraries.integers()
                    .between(1, 20)
                    .array(long[].class)
                    .ofSize(t);
            
            return Combinators.combine(prioritiesArb, durationsArb, numWorkers)
                    .as(TaskInput::new);
         });
    }

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
