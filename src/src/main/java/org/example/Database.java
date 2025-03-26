package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.sql.*;

public class Database {
    private static final String insertFlightSql = "INSERT INTO flights(flight_date, flight_status, departure_airport, arrival_airport, airline, flight_number) VALUES(?, ?, ?, ?, ?, ?)";
    private final File file;

    public Database(File file) throws SQLException {
        this.file = file;
        this.createDatabase();
    }

    public Database insert(JSONArray flights) {
        if (flights.isEmpty()) {
            System.out.println("No hay vuelos disponibles en la API.");
            return this;
        } 
    
        saveFlightsToDatabase(flights);
        return null;
    }

    public void createDatabase() throws SQLException { // hacer reutilizable con los parametros de la base de datos
        try (Connection conn = DriverManager.getConnection(dbUrl())) {
            if (conn != null) {
                String createTableSQL = " flights (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "flight_date TEXT," +
                        "flight_status TEXT," +
                        "departure_airport TEXT," +
                        "arrival_airport TEXT," +
                        "airline TEXT," +
                        "flight_number TEXT" +
                        
                        ");";
                conn.createStatement().execute(createTableSQL);
                System.out.println("Base de datos creada correctamente.");
            }
        }
    }



    obj = {
        name: "_id",
        type: "INTEGER",
        primaryKey: true,
        autoIncrement: true
    }

    columns = [
        obj,
        obj,
        obj
    ]

    createTable(name, columns) 



    private static void insertFlightData(PreparedStatement pstmt, JSONObject flight) throws SQLException {
        String flightNumber = flight.optString("flight_number", "N/A");
        try {

            const columns = [
                {
                    name: "flight_date",
                    type: "sting",
                    isObject: false
                },
                {
                    name: "flight_status",
                    type: "sting",
                    isObject: false
                },
                {
                    name: "departure",
                    type: "sting",
                    isObject: true,
                    childrenObject: "airport"
                },
                {
                    name: "arrival",
                    type: "sting",
                    isObject: true,
                    childrenObject: "airport"
                }
            ]
                
            

            getValueFlight(flight , columns)
            pstmt.setString(1, flight.optString("flight_date", "N/A"));
            pstmt.setString(2, flight.optString("flight_status", "N/A"));
            pstmt.setString(3, flight.getJSONObject("departure").optString("airport", "N/A"));
            pstmt.setString(4, flight.getJSONObject("arrival").optString("airport", "N/A"));
            pstmt.setString(5, flight.getJSONObject("airline").optString("name", "N/A"));
            pstmt.setString(6, flightNumber);
            pstmt.addBatch();
        } catch (Exception e) {
            System.out.println("Error al procesar un vuelo: " + e.getMessage());
        }
    }

    private void saveFlightsToDatabase(JSONArray flights) {
        try (Connection conn = DriverManager.getConnection(dbUrl());
             PreparedStatement pstmt = conn.prepareStatement(insertFlightSql)) {

            for (int i = 0; i < flights.length(); i++) {
                JSONObject flight = flights.getJSONObject(i);
                insertFlightData(pstmt, flight);
            }

            pstmt.executeBatch();
            System.out.println("Vuelos guardados en la base de datos.");
        } catch (SQLException e) {
            handleSQLException(e, "Error al guardar vuelos en la base de datos.");
        }
    }

    private static void handleSQLException(SQLException e, String message) {
        System.out.println(message);
        e.printStackTrace();
    }

    private String dbUrl() {
        return "jdbc:sqlite:" + file.getAbsolutePath();
    }
}
