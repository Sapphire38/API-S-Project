package org.example;
import java.io.File;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            AviationAPI api = new AviationAPI(System.getenv("apiKey")).fetch();
            api.startScheduledDataFetch();
            new AviationDB(new File(System.getenv("database"))).insert(api.flights());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}