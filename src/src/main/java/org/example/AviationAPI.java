package org.example;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AviationAPI {
    private static final String apiKey = "9d9efea6c0c3f692ff8545bd0d610f13";
    private static final String urlLink = "http://api.aviationstack.com/v1/flights?access_key=" + apiKey + "&arr_icao=GCLP&flight_status=landed";
    private static final String dbUrl = "jdbc:sqlite:flights.db";
    private static final String insertFlightSql = "INSERT INTO flights(flight_date, flight_status, departure_airport, arrival_airport, airline, flight_number) VALUES(?, ?, ?, ?, ?, ?)";

    private static final OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) {
        createDatabase();
        listTables();
        startScheduledDataFetch();
    }

    private static void startScheduledDataFetch() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(AviationAPI::fetchFlightData, 0, 7, TimeUnit.DAYS);
    }

    private static void createDatabase() {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
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
        } catch (SQLException e) {
            handleSQLException(e, "Error al crear la base de datos.");
        }
    }

    private static void listTables() {
        String sql = "SELECT name FROM sqlite_master WHERE type='table'";
        try (Connection conn = DriverManager.getConnection(dbUrl);
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

    public static void fetchFlightData() {
        System.out.println("Consultando datos de vuelos...");
        String responseBody = fetchFlightDataFromAPI();

        if (responseBody != null) {
            processAndSaveFlights(responseBody);
        }
    }

    private static String fetchFlightDataFromAPI() {
        Request request = new Request.Builder()
                .url(urlLink)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            } else {
                System.out.println("Respuesta vacía de la API.");
                return null;
            }
        } catch (IOException e) {
            System.out.println("Error al obtener datos de la API: " + e.getMessage());
            return null;
        }
    }

    private static void processAndSaveFlights(String responseBody) {
        JSONObject jsonResponse = new JSONObject(responseBody);

        if (!jsonResponse.has("data")) {
            System.out.println("No se encontraron datos en la respuesta.");
            return;
        }

        JSONArray flights = jsonResponse.getJSONArray("data");

        if (flights.isEmpty()) {
            System.out.println("No hay vuelos disponibles en la API.");
            return;
        }

        saveFlightsToDatabase(flights);
    }

    private static void saveFlightsToDatabase(JSONArray flights) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
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

    private static void handleSQLException(SQLException e, String message) {
        System.out.println(message);
        e.printStackTrace();
    }
}
