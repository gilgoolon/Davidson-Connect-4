package com.gil.connect_four.logic;
public enum Color {
    Red(1),
    Yellow(-1),
    Empty(0);
    private final int value;

    Color(int val){
        value = val;
    }
}
