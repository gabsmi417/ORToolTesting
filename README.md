## ORTools Testing

This package is a test packages for testing some ORTools from Google. In paticular it uses ORTools's CP-SAT solver
to solve a basic combinatorial optimization problem for task scheduling. 

### Task Scheduling
We have n tasks each their a priority and time to complete. We have m workers who each have the same total capacity. Our
goal is to assign tasks to workers maximizing the priorities of task completed, while ensuring that any worker does
not exceed their capacity.

This problem easily encodes as a contraint optimization problem. The main files are `TasksSolver.java` which is where the 
CP-SAT encoding is done, and `Assignment.java` where the output is encoded in the case of a valid solution. `App.java` surves as
the entry point of the project. The `test/` directory is not being used and just has a dummy test in it.