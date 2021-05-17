package com.example.application.data;

import com.example.application.SearchTerm;
import com.example.application.model.Flight;
import com.example.application.model.FlightDetails;
import com.example.application.utils.FlightNotFoundException;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class Service {

    private final double LB_CONVERT_TO_KG_VALUE = 0.45359237;
    private final Repository repository;

    public Service(Repository repository) {
        this.repository = repository;
    }

    public List<Flight> findAllFlights() {
        return repository.findAllFlights();
    }

    public FlightDetails findFlightDetails(Flight flight, SearchTerm searchTerm) throws FlightNotFoundException {
        if (flight != null && flight.getId() != null) {
            return repository.getFlightDetails(flight).orElseThrow(FlightNotFoundException::new);
        } else {
            if (searchTerm.equals(SearchTerm.FLIGHT_NUMBER)) {
                return repository.getFlightDetailsByFlightNumber(flight.getFlightNumber())
                        .orElseThrow(FlightNotFoundException::new);
            } else if (searchTerm.equals(SearchTerm.DATE)) {
                return repository.getFlightDetailsByFlightDate(flight.getDepartureDate())
                        .orElseThrow(FlightNotFoundException::new);
            }
            throw new FlightNotFoundException();
        }
    }

    public Map<String, Double> getWeights(FlightDetails flightDetails) {
        Map<String, Double> weights = new HashMap<>();
        double cargoWeight = flightDetails.getCargo().stream().mapToDouble(cargo ->
            cargo.getWeightUnit().equals("kg") ? cargo.getWeight() : cargo.getWeight() * LB_CONVERT_TO_KG_VALUE)
                .sum();

        double baggageWeight = flightDetails.getBaggage().stream().mapToDouble(baggage ->
            baggage.getWeightUnit().equals("kg") ? baggage.getWeight() : baggage.getWeight() * LB_CONVERT_TO_KG_VALUE)
                .sum();

        weights.put("cargo", Math.round(cargoWeight * 100.0) / 100.0);
        weights.put("baggage", Math.round(baggageWeight * 100.0) / 100.0);
        weights.put("total", Math.round((cargoWeight + baggageWeight) * 100.0) / 100.0);
        return weights;
    }

    public Flight findFlightById(int flightId) throws FlightNotFoundException {
        Optional<Flight> flight = repository.findFlightById(flightId);
        if (flight.isPresent())
            return flight.get();
        throw new FlightNotFoundException() ;
    }

    public Set<String> getAirportCodes(){
        return repository.getAirportCodes();
    }

    public Set<OffsetDateTime> getDepartureDates(){ return repository.getDepartureDates(); }

    public Map<String, Integer> getFlightsStatistics(String code) {
        Map<String, Integer> flightsStatistics = new HashMap<>();
        List<Flight> flights = repository.getFlights();
        int departures = getDepartures(code, flights);
        int arrivals = getArrivals(code, flights);
        int baggageArriving = getBaggageArriving(code, flights);
        flightsStatistics.put("departures", departures);
        flightsStatistics.put("arrivals", arrivals);

        return flightsStatistics;
    }

    private int getDepartures(String code, List<Flight> flights) {
        List<Flight> departures = flights.stream()
                .filter(flight -> flight.getDepartureAirportIATACode().equals(code)).collect(Collectors.toList());
        return departures.size();
    }

    private int getArrivals(String code, List<Flight> flights) {
        List<Flight> departures = flights.stream()
                .filter(flight -> flight.getArrivalAirportIATACode().equals(code)).collect(Collectors.toList());
        return departures.size();
    }
}
