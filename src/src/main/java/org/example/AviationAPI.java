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
    private final String apiKey ;

    private static final OkHttpClient client = new OkHttpClient();
    private JSONArray flights;

    public AviationAPI(String apiKey) {
        this.apiKey = apiKey;
    }

    public AviationAPI fetch() {
        System.out.println("Consultando datos de vuelos...");
        process(fetchFlightDataFromAPI());
        return this;
    }

    public JSONArray flights() {
        return flights;
    }

    private void process(String responseBody) {
        if (responseBody == null) return;
        process(new JSONObject(responseBody));
    }

    private void process(JSONObject jsonResponse) {
        if (!jsonResponse.has("data")) {
            System.out.println("No se encontraron datos en la respuesta.");
            return;
        }
        this.flights = jsonResponse.getJSONArray("data");
    }

    public static void main(String[] args) {
        createDatabase();
        listTables();
        startScheduledDataFetch();
    }

    private static void startScheduledDataFetch() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Consultando datos de vuelos...");
            String responseBody = fetchFlightDataFromAPI();

            if (responseBody != null) {
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
        }, 0, 7, TimeUnit.DAYS);
    }

    private String fetchFlightDataFromAPI() {
        Request request = new Request.Builder()
                .url(urlLink())
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

    private String urlLink() {
        return "http://api.aviationstack.com/v1/flights?access_key=" + apiKey + "&arr_icao=GCLP&flight_status=landed";
    }
}
