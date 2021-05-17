package com.example.application.model;


public class Baggage extends AbstractCargo {

    public Baggage() {}

    public Baggage(int id, int weight, String weightUnit, int pieces) {
        super(id, weight, weightUnit, pieces);
    }
}
