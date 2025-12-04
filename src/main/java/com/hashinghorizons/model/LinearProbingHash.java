package com.hashinghorizons.model;

public class LinearProbingHash extends HashTable {

    public LinearProbingHash(int size) {
        super(size);
    }

    private int hash(int key) {
        int idx = key % table.length;
        if (idx < 0) idx += table.length;
        return idx;
    }

    @Override
    public void insert(int key) {
        int idx = hash(key);
        int start = idx;
        int localCollisions = 0;

        // handle collisions via linear probing
        while (table[idx] != -1) {
            localCollisions++;
            idx = (idx + 1) % table.length;
            if (idx == start) return; 
        }

        table[idx] = key;
        elementCount++;
        totalCollisions += localCollisions;
        lastIndex = idx; 
    }

    @Override
    public boolean search(int key) {
        int idx = hash(key);
        int start = idx;
        while (table[idx] != -1) {
            if (table[idx] == key) return true;
            idx = (idx + 1) % table.length;
            if (idx == start) break;
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
