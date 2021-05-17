package com.example.application.utils;

public class FlightNotFoundException extends Exception {

    public FlightNotFoundException(){
        super("Flight not found");
    }
}
