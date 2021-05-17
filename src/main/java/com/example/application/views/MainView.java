package com.example.application.views;

import com.example.application.SearchTerm;
import com.example.application.data.Service;
import com.example.application.model.Flight;
import com.example.application.model.FlightDetails;
import com.example.application.utils.FlightNotFoundException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "")
@RouteAlias(value = "")
@PageTitle("Airport")
@CssImport("./themes/airport/views/main-view.css")
public class MainView extends VerticalLayout {

    private final Service service;
    private final Paragraph cargoWeightText = new Paragraph("Cargo weight: ");
    private final Paragraph baggageWeightText = new Paragraph("Baggage weight: ");
    private final Paragraph totalWeight = new Paragraph("Total weight: ");
    private final Paragraph departureFlights = new Paragraph("Departure flights: ");
    private final Paragraph arrivalFlights = new Paragraph("Arrival flights: ");
//    private final Paragraph totalWeight = new Paragraph("Total weight: ");
    private final BeanValidationBinder<Flight> flightBinder = new BeanValidationBinder<>(Flight.class);
    private final H4 flightNumberText = new H4("Flight number: ");

    public MainView(Service service) {
        this.service = service;
        addClassName("main-view");
        Grid<Flight> flightGrid = createFlightGrid(service);

        H2 title = new H2("Flights:");


        VerticalLayout leftVertical = createLeftLayout();
        VerticalLayout rightVertical = createRightLayout();

        VerticalLayout leftResultLayout = new VerticalLayout(flightNumberText, cargoWeightText, baggageWeightText,
                totalWeight);
        leftResultLayout.setId("leftResultLayout");

        add(new VerticalLayout(title, flightGrid),
                new HorizontalLayout(leftVertical, rightVertical),
                new HorizontalLayout(leftResultLayout));
    }

    private Grid<Flight> createFlightGrid(Service service) {
        Grid<Flight> flightGrid = new Grid<>(Flight.class);
        List<Flight> allFlights = service.findAllFlights();
        flightGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        flightGrid.setItems(allFlights);
        flightGrid.asSingleSelect().addValueChangeListener(event -> {
            Flight flight = event.getValue();
            if (flight != null)
                flightBinder.setBean(flight);
            else
                flightBinder.setBean(new Flight());
        });
        return flightGrid;
    }

    private VerticalLayout createLeftLayout() {
        IntegerField flightNumberField = new IntegerField("Flight number:");
        Button flightNumberButton = new Button("Details");
        flightNumberButton.addClickListener(buttonClickEvent -> {
            if (flightBinder.isValid())
                getFlightDetails(flightBinder.getBean(), SearchTerm.FLIGHT_NUMBER);
        });
        flightBinder.forField(flightNumberField).bind(Flight::getFlightNumber, Flight::setFlightNumber);

        ComboBox<OffsetDateTime> dateTimePiker = new ComboBox<>("Date:");
        dateTimePiker.setId("date-piker");
        List<OffsetDateTime> departureDates = service.findAllFlights().stream().map(Flight::getDepartureDate)
                .collect(Collectors.toList());
        dateTimePiker.setItems(departureDates);

        Button departureDateButton = new Button("Details");
        departureDateButton.addClickListener(buttonClickEvent -> {
            if (flightBinder.isValid())
                getFlightDetails(dateTimePiker.getValue());
        });

        flightBinder.forField(dateTimePiker).bind("departureDate");

        flightBinder.addValueChangeListener(valueChangeEvent -> {
            if (flightBinder.isValid()){
                flightNumberButton.setEnabled(true);
                departureDateButton.setEnabled(true);
            } else {
                flightNumberButton.setEnabled(false);
                departureDateButton.setEnabled(false);
            }
        });
        flightBinder.setBean(new Flight());

        Paragraph paragraph = new Paragraph("Select flight or type/paste keywords to get more info about weights:");
        HorizontalLayout inputsLayout = new HorizontalLayout(
                flightNumberField, flightNumberButton,
                dateTimePiker, departureDateButton
        );
        VerticalLayout leftVertical = new VerticalLayout(paragraph, inputsLayout);
        return leftVertical;
    }

    private VerticalLayout createRightLayout() {
        ComboBox<String> airportCodeCombobox = new ComboBox<>("AirportCode");
        airportCodeCombobox.setItems(service.getAirportCodes());
        Button airportCodeButton = new Button("Details");
        airportCodeButton.addClickListener(event -> {
            processAirportCode(airportCodeCombobox.getValue());
        });


        ComboBox<OffsetDateTime> dateTimeComboBox = new ComboBox<>("Date");
        dateTimeComboBox.setItems(service.getDepartureDates());
        Button dateTimeButton = new Button("Details");




        HorizontalLayout inputsLayout = new HorizontalLayout(airportCodeCombobox, airportCodeButton, dateTimeComboBox,
                dateTimeButton);

        Paragraph paragraph = new Paragraph("Select airport code or date to get more info about departures and " +
                "arrivals:");
        VerticalLayout rightLayout = new VerticalLayout(paragraph, inputsLayout);
        return rightLayout;
    }

    private void processAirportCode(String code) {
        Map<String, Integer> flightsStatistics = service.getFlightsStatistics(code);
    }

    private void getFlightDetails(OffsetDateTime departureDate) {
        if (departureDate != null)
            getFlightDetails(new Flight(departureDate), SearchTerm.DATE);
        else
            flightNotFoundNotification();
    }

    private void getFlightDetails(Flight flight, SearchTerm searchTerm) {
        try {
            FlightDetails flightDetails = service.findFlightDetails(flight, searchTerm);
            Map<String, Double> cargoWeight = service.getWeights(flightDetails);
            setFlightDetails(cargoWeight, flightDetails);
        } catch (FlightNotFoundException e) {
            flightNotFoundNotification();
        }
    }

    private void setFlightDetails(Map<String, Double> cargoWeight, FlightDetails flightDetails) {
        try {
            Flight flight = service.findFlightById(flightDetails.getFlightId());
            flightNumberText.setText("Flight number: " + flight.getFlightNumber());
            cargoWeightText.setText( "Cargo weight: " + cargoWeight.get("cargo").toString() + " kg");
            baggageWeightText.setText("Baggage weight: " + cargoWeight.get("baggage").toString() + " kg");
            totalWeight.setText("Total weight: " + cargoWeight.get("total").toString() + " kg");
        } catch (FlightNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void flightNotFoundNotification() {
        Notification.show(
                "Flight not found. Choose date, provide flight number or simply select flight from table.",
                5000,
                Notification.Position.MIDDLE);
    }
}
