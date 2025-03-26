Class Flights {
    private String flight_date;
    private String flight_status;
    private String departure_airport;
    private String arrival_airport;
    private String airline;
    private String flight_number;


    public Flights(String flight_date, String flight_status, String departure_airport, String arrival_airport, String airline, String flight_number) {
        this.flight_date = flight_date;
        this.flight_status = flight_status;
        this.departure_airport = departure_airport;
        this.arrival_airport = arrival_airport;
        this.airline = airline;
        this.flight_number = flight_number;
    }

  
    public String getInfo() {
        return "Flight Date: " + flight_date + "\n" +
               "Flight Status: " + flight_status + "\n" +
               "Departure Airport: " + departure_airport + "\n" +
               "Arrival Airport: " + arrival_airport + "\n" +
               "Airline: " + airline + "\n" +
               "Flight Number: " + flight_number;
    }

    public String getFlight_date() {
        return flight_date;
    }
    

    

}

