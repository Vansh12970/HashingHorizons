package com.hashinghorizons.model;

import java.util.LinkedList;

@SuppressWarnings("unchecked")
public class ChainingHash extends HashTable {

    private final LinkedList<Integer>[] table;

    public ChainingHash(int size) {
        super(size);
        table = new LinkedList[size];
        for (int i = 0; i < size; i++) {
            table[i] = new LinkedList<>();
        }
    }

    private int hash(int key) {
        int idx = key % table.length;
        if (idx < 0) idx += table.length;
        return idx;
    }

    @Override
    public void insert(int key) {
        int idx = hash(key);
        if (!table[idx].isEmpty()) totalCollisions++;
        table[idx].add(key);
        elementCount++;
        lastIndex = idx; 
    }

    @Override
    public boolean search(int key) {
        int idx = hash(key);
        return table[idx].contains(key);
    }

    @Override
    public void clear() {
        for (LinkedList<Integer> list : table) list.clear();
        elementCount = 0;
        totalCollisions = 0;
        lastIndex = -1;
    }
}
