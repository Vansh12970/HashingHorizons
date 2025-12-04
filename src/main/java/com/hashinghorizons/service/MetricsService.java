package com.hashinghorizons.service;

import com.hashinghorizons.model.HashTable;

public class MetricsService {
    public static double computeLoadFactor(HashTable table) {
        return table.size() == 0 ? 0.0 : (double) table.getElementCount() / table.size();
    }

    public static String formatDouble(double val) {
        return String.format("%.3f", val);
    }
}
