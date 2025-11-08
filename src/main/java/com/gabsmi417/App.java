package com.gabsmi417;

import java.util.Optional;

import com.google.ortools.Loader;

public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Loading Or-Tools... (this may take a moment)");
        Loader.loadNativeLibraries();
        System.out.println("OR-Tools Loaded");

        TasksSolver ts = new TasksSolver(
                new long[]{3, 2, 2, 4, 4, 4, 4, 4}, 
                new long[]{2, 3, 1, 3, 3, 3, 3, 3},
                2
        );
        Optional<Assignment> a = ts.solve();

        if (a.isPresent()) {
            System.out.println(a.get());
            ts.printStatistics();
        } else {
            System.out.println("No solution found.");
        }
    }
}
