package com.hashinghorizons.model;

public class DoubleHashing extends HashTable {

    public DoubleHashing(int size) {
        super(size);
    }

    private int hash1(int key) {
        int idx = key % table.length;
        if (idx < 0) idx += table.length;
        return idx;
    }

    private int hash2(int key) {
        int h = 1 + (key % (table.length - 1));
        if (h < 0) h += table.length - 1;
        return h;
    }

    @Override
    public void insert(int key) {
        int idx = hash1(key);
        int step = hash2(key);
        int start = idx;

        while (table[idx] != -1) {
            totalCollisions++;
            idx = (idx + step) % table.length;
            if (idx == start) return; // table full
        }

        table[idx] = key;
        elementCount++;
        lastIndex = idx; // ✅ for animation visualization
    }

    @Override
    public boolean search(int key) {
        int idx = hash1(key);
        int step = hash2(key);
        int start = idx;

        while (table[idx] != -1) {
            if (table[idx] == key) return true;
            idx = (idx + step) % table.length;
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
