package com.example.application.model;

import lombok.Data;

import java.util.List;

@Data
public class FlightDetails {

    private int flightId;
    private List<Baggage> baggage;
    private List<Cargo> cargo;

    public FlightDetails(){}

    public FlightDetails(int flightId, List<Baggage> baggage, List<Cargo> cargo) {
        this.flightId = flightId;
        this.baggage = baggage;
        this.cargo = cargo;
    }
}
