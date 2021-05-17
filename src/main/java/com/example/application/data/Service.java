package com.example.application.data;

import com.example.application.SearchTerm;
import com.example.application.model.Baggage;
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

    public Map<String, Integer> getFlightsStatistics(String code, OffsetDateTime dateTime) {
        List<Flight> flights = repository.getFlights();
        List<Flight> departures = getDepartures(code, flights, dateTime);
        List<Flight> arrivals = getArrivals(code, flights, dateTime);
        return buildStatisticsMap(departures, arrivals);
    }

    private Map<String, Integer> buildStatisticsMap(List<Flight> departures, List<Flight> arrivals) {
        Map<String, Integer> flightsStatistics = new HashMap<>();
        int baggageArriving = getBaggagePieces(arrivals);
        int baggageDeparting = getBaggagePieces(departures);
        flightsStatistics.put("departures", departures.size());
        flightsStatistics.put("arrivals", arrivals.size());
        flightsStatistics.put("baggageArriving", baggageArriving);
        flightsStatistics.put("baggageDeparting", baggageDeparting);
        return flightsStatistics;
    }

    private int getBaggagePieces(List<Flight> flights) {
        List<Baggage> baggage = flights.stream().map(flight -> {
            var flightOptional = repository.getFlightDetails(flight);
            if (flightOptional.isPresent())
                return flightOptional.get().getBaggage();
            return new ArrayList<Baggage>();
        }).flatMap(List::stream).collect(Collectors.toList());

        return baggage.stream().mapToInt(value -> value.getPieces()).sum();
    }

    private List<Flight> getDepartures(String code, List<Flight> flights, OffsetDateTime dateTime) {
        if (dateTime != null) {
            return flights.stream().filter(flight ->
                    flight.getDepartureAirportIATACode().equals(code) && flight.getDepartureDate().equals(dateTime))
                    .collect(Collectors.toList());
        }
        return flights.stream().filter(flight ->
                flight.getDepartureAirportIATACode().equals(code)).collect(Collectors.toList());
    }

    private List<Flight> getArrivals(String code, List<Flight> flights, OffsetDateTime dateTime) {
        if (dateTime != null){
            return flights.stream().filter(flight ->
                    flight.getArrivalAirportIATACode().equals(code) && flight.getDepartureDate().equals(dateTime))
                    .collect(Collectors.toList());
        }
        return flights.stream()
                .filter(flight -> flight.getArrivalAirportIATACode().equals(code)).collect(Collectors.toList());
    }
}
