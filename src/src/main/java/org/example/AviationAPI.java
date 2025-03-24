package org.example;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AviationAPI {
    private static final String API_KEY = "9d9efea6c0c3f692ff8545bd0d610f13";  // Reemplaza con tu clave real
    private static final String urlstring = "http://api.aviationstack.com/v1/flights?access_key=" + API_KEY + "&arr_icao=GCLP&limit=20";
    private static final OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(AviationAPI::fetchFlightData, 0, 7, TimeUnit.DAYS);
    }

    public static void fetchFlightData() {
        System.out.println("Consultando datos de vuelos...");

        Request request = new Request.Builder()
                .url(urlstring)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                JSONObject jsonResponse = new JSONObject(response.body().string());
                System.out.println(jsonResponse.toString(2));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
