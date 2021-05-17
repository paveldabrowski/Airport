package com.example.application.model;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import java.time.OffsetDateTime;

@Data
public class Flight {

    private Integer id;
    @NumberFormat
    private Integer flightNumber;
    private String departureAirportIATACode;
    private String arrivalAirportIATACode;
    @DateTimeFormat
    private OffsetDateTime departureDate;

    public Flight(){}

    public Flight(Integer id, Integer flightNumber, String departureAirportIATACode, String arrivalAirportIATACode,
                  OffsetDateTime departureDate) {

        this.id = id;
        this.flightNumber = flightNumber;
        this.departureAirportIATACode = departureAirportIATACode;
        this.arrivalAirportIATACode = arrivalAirportIATACode;
        this.departureDate = departureDate;
    }

    public Flight(OffsetDateTime departureDate) {
        this.departureDate = departureDate;
    }
}
