package com.hashinghorizons.service;

import com.hashinghorizons.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulationService {

    // Used to store the last simulation result (for export)
    private SimulationResult lastResult;

    /** Holds results of a single simulation */
    public static class SimulationResult {
        public final String technique;
        public final int tableSize;
        public final int elements;
        public final int collisions;
        public final double loadFactor;
        public final long insertTimeNanos;

        public SimulationResult(String technique, int tableSize, int elements,
                                int collisions, double loadFactor, long insertTimeNanos) {
            this.technique = technique;
            this.tableSize = tableSize;
            this.elements = elements;
            this.collisions = collisions;
            this.loadFactor = loadFactor;
            this.insertTimeNanos = insertTimeNanos;
        }
    }

    /**
     * Runs the hashing simulation for a given technique, table size, and keys.
     */
    public SimulationResult runSimulation(String technique, int tableSize, int[] keys) {
        HashTable ht;

        switch (technique) {
            case "Linear Probing":
                ht = new LinearProbingHash(tableSize);
                break;
            case "Quadratic Probing":
                ht = new QuadraticProbingHash(tableSize);
                break;
            case "Double Hashing":
                ht = new DoubleHashing(tableSize);
                break;
            default:
                ht = new ChainingHash(tableSize);
        }

        long start = System.nanoTime();
        for (int key : keys) ht.insert(key);
        long end = System.nanoTime();

        int elements = ht.getElementCount();
        int collisions = ht.getCollisions();
        double loadFactor = tableSize == 0 ? 0.0 : ((double) elements / tableSize);

        SimulationResult result = new SimulationResult(
                technique, tableSize, elements, collisions, loadFactor, end - start
        );

        // Store for CSV export
        lastResult = result;

        return result;
    }

    /** Returns the last simulation result (for Export CSV feature) */
    public SimulationResult getLastResult() {
        return lastResult;
    }

    /** Generates random keys */
    public int[] generateRandomKeys(int count, int maxVal, long seed) {
        Random rnd = (seed == 0) ? new Random() : new Random(seed);
        int[] arr = new int[count];
        for (int i = 0; i < count; i++) arr[i] = rnd.nextInt(maxVal) + 1;
        return arr;
    }

    /** Parses comma/space-separated integers into an int array */
    public int[] parseKeys(String csv) {
        String[] parts = csv.trim().split("[,\\s]+");
        List<Integer> list = new ArrayList<>();
        for (String p : parts) {
            if (p.isBlank()) continue;
            try {
                list.add(Integer.parseInt(p.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return list.stream().mapToInt(i -> i).toArray();
    }
}
