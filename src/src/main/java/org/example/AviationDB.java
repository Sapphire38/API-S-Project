package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.sql.*;

public class AviationDB {
    private static final String insertFlightSql = "INSERT INTO flights(flight_date, flight_status, departure_airport, arrival_airport, airline, flight_number) VALUES(?, ?, ?, ?, ?, ?)";
    private final File file;

    public AviationDB(File file) throws SQLException {
        this.file = file;
        this.createDatabase();
    }

    public AviationDB insert(JSONArray flights) {
        if (flights.isEmpty()) {
            System.out.println("No hay vuelos disponibles en la API.");
            return this;
        }

        saveFlightsToDatabase(flights);
        return null;
    }

    private void createDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbUrl())) {
            if (conn != null) {
                String createTableSQL = "CREATE TABLE IF NOT EXISTS flights (" +
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

    private static void insertFlightData(PreparedStatement pstmt, JSONObject flight) throws SQLException {
        String flightNumber = flight.optString("flight_number", "N/A");
        try {
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

    private static void saveFlightsToDatabase(JSONArray flights) {
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
    private static void listTables() {
        String sql = "SELECT name FROM sqlite_master WHERE type='table'";
        try (Connection conn = DriverManager.getConnection(dbUrl());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("Tablas en la base de datos:");
            while (rs.next()) {
                System.out.println(" - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error al listar las tablas.");
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
