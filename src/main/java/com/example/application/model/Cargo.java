package com.example.application.model;


public class Cargo extends AbstractCargo {

    public Cargo() {}

    public Cargo(int id, int weight, String weightUnit, int pieces) {
        super(id, weight, weightUnit, pieces);
    }
}
