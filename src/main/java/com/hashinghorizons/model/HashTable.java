package com.hashinghorizons.model;

public abstract class HashTable {

    protected int[] table;
    protected int elementCount;
    protected int totalCollisions;
    protected int lastIndex = -1; 

    public HashTable(int size) {
        table = new int[size];
        for (int i = 0; i < size; i++) {
            table[i] = -1;
        }
        elementCount = 0;
        totalCollisions = 0;
    }

   
    public abstract void insert(int key);
    public abstract boolean search(int key);
    public abstract void clear();

    
    public int getCollisions() {
        return totalCollisions;
    }

    public int getElementCount() {
        return elementCount;
    }

    public int size() {
        return table.length;
    }

    public int getLastIndex() {
        return lastIndex;
    }
}
