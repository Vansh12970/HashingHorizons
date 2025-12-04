package com.hashinghorizons.model;

public class QuadraticProbingHash extends HashTable {

    public QuadraticProbingHash(int size) {
        super(size);
    }

    private int hash(int key) {
        int idx = key % table.length;
        if (idx < 0) idx += table.length;
        return idx;
    }

    @Override
    public void insert(int key) {
        int base = hash(key);
        int i = 0;
        int idx;

        while (i < table.length) {
            idx = (base + i * i) % table.length;
            if (table[idx] == -1) {
                table[idx] = key;
                elementCount++;
                lastIndex = idx;  
                return;
            } else {
                totalCollisions++;
                i++;
            }
        }
    }

    @Override
    public boolean search(int key) {
        int base = hash(key);
        int i = 0;
        int idx;

        while (i < table.length) {
            idx = (base + i * i) % table.length;
            if (table[idx] == -1) return false;
            if (table[idx] == key) return true;
            i++;
        }
        return false;
    }

    @Override
    public void clear() {
        for (int i = 0; i < table.length; i++) {
            table[i] = -1;
        }
        elementCount = 0;
        totalCollisions = 0;
        lastIndex = -1;
    }
}
