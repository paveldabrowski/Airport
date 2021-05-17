package com.example.application.data;

import com.example.application.model.Flight;
import com.example.application.model.FlightDetails;
import com.example.application.utils.FlightNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.*;

@org.springframework.stereotype.Repository
class Repository {

    private final String FLIGHTS_URL = "http://localhost:3000/flight";
    private final String DETAILS_URL = "http://localhost:3000/details";
    private final RestTemplate restTemplate;
    private List<Flight> flights;
    private final Set<String> airPortCodes = new HashSet<>();
    private final Set<OffsetDateTime> departureDates = new HashSet<>();

    Repository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    List<Flight> findAllFlights() {
        ResponseEntity<Flight[]> response =
                restTemplate.getForEntity(FLIGHTS_URL, Flight[].class);
        Flight[] flights = response.getBody();
        if (flights != null && flights.length > 0) {
            this.flights = Arrays.asList(flights);
            this.flights.forEach(flight -> {
                airPortCodes.add(flight.getArrivalAirportIATACode());
                airPortCodes.add(flight.getDepartureAirportIATACode());
                departureDates.add(flight.getDepartureDate());
            });
            return this.flights;
        } else {
            this.flights = new ArrayList<>();
            return new ArrayList<>();
        }
    }

    Optional<Flight> findFlightById(int id){
        return Optional.ofNullable(restTemplate.getForObject(FLIGHTS_URL + "/" + id, Flight.class));
    }

    Optional<FlightDetails> getFlightDetails(Flight flight) {
        if (flight.getId() != null) {
            var responseEntity = restTemplate.getForEntity(DETAILS_URL + "?flightId="
                    + flight.getId(), FlightDetails[].class);
            FlightDetails[] flightDetails = responseEntity.getBody();
            return checkIfValueIsPresent(flightDetails);
        } else {
            return Optional.empty();
        }
    }

    Optional<FlightDetails> getFlightDetailsByFlightNumber(Integer flightNumber) throws FlightNotFoundException {
        if (flightNumber == null)
            throw new FlightNotFoundException();
        Optional<Flight> flightEntity = getFlightByFlightNumber(flightNumber);
        if (flightEntity.isPresent()){
            return getFlightDetails(flightEntity.get());
        }
        return Optional.empty();
    }

    Optional<FlightDetails> getFlightDetailsByFlightDate(OffsetDateTime departureDate) {
        Optional<Flight> flightEntity = getFlightByDate(departureDate);
        if (flightEntity.isPresent()){
            return getFlightDetails(flightEntity.get());
        }
        return Optional.empty();
    }

    private Optional<Flight> getFlightByDate(OffsetDateTime date){
        return flights.stream().filter(flight1 -> flight1.getDepartureDate().equals(date)).findFirst();
    }

    private Optional<Flight> getFlightByFlightNumber(Integer flightNumber){
        ResponseEntity<Flight[]> flightsResponse = restTemplate.getForEntity(FLIGHTS_URL + "?flightNumber="
                        + flightNumber, Flight[].class);
        Flight[] flights = flightsResponse.getBody();
        return checkIfValueIsPresent(flights);
    }

    private <T> Optional<T> checkIfValueIsPresent(T[] array){
        if (array != null && array.length > 0) {
            return Optional.of(array[0]);
        }
        return Optional.empty();
    }

    public Set<String> getAirportCodes() { return airPortCodes; }

    public Set<OffsetDateTime> getDepartureDates() {
        return departureDates;
    }

    public List<Flight> getFlights() {
        return flights;
    }
}
