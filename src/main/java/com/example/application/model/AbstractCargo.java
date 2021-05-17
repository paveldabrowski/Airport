package com.example.application.model;

import lombok.Data;

@Data
abstract class AbstractCargo {

    private int id;
    private int weight;
    private String weightUnit;
    private int pieces;

    public AbstractCargo(){}

    public AbstractCargo(int id, int weight, String weightUnit, int pieces) {
        this.id = id;
        this.weight = weight;
        this.weightUnit = weightUnit;
        this.pieces = pieces;
    }

}
