package com.example.vehicletrackingbackend.service;

import com.example.vehicletrackingbackend.dto.RandomRoutePoint;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Random;


@Service
public class RandomRouteService {

    private static final int MAX_ATTEMPTS = 6;
    private static final long NOMINATIM_INTERVAL_MS = 1100;

    private static final List<String> LOCATION_FIELDS =
            List.of(
                    "city",
                    "town",
                    "municipality",
                    "village",
                    "county",
                    "state_district",
                    "state"
            );


    private final Random random = new Random();

    private final RouteService routeService;
    private final RestClient nominatimClient;

    private long lastNominatimRequestTime = 0;


    public RandomRouteService(
            RouteService routeService
    ) {

        this.routeService = routeService;

        this.nominatimClient =
                RestClient.builder()
                        .baseUrl(
                                "https://nominatim.openstreetmap.org"
                        )
                        .defaultHeader(
                                "User-Agent",
                                "vehicle-tracking-learning-project/1.0"
                        )
                        .defaultHeader(
                                "Accept-Language",
                                "tr"
                        )
                        .build();
    }


    public RandomRoutePoint getRandomPoint(
            Region region
    ) {

        RuntimeException lastError = null;


        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {

            try {

                double latitude =
                        randomBetween(
                                region.minLatitude,
                                region.maxLatitude
                        );

                double longitude =
                        randomBetween(
                                region.minLongitude,
                                region.maxLongitude
                        );


                // Rastgele koordinatı en yakın sürülebilir yola oturtur.
                RouteService.RoadPoint roadPoint =
                        routeService.snapToNearestRoad(
                                latitude,
                                longitude
                        );


                String locationName =
                        reverseGeocode(
                                roadPoint.latitude(),
                                roadPoint.longitude()
                        );


                if (locationName != null) {

                    return new RandomRoutePoint(
                            locationName,
                            roadPoint.latitude(),
                            roadPoint.longitude()
                    );
                }


            } catch (RuntimeException exception) {

                lastError = exception;
            }
        }


        throw new IllegalStateException(
                region
                        + " bölgesinde geçerli rastgele konum oluşturulamadı.",
                lastError
        );
    }


    private double randomBetween(
            double min,
            double max
    ) {

        return min
                + random.nextDouble()
                * (max - min);
    }


    private String reverseGeocode(
            double latitude,
            double longitude
    ) {

        waitForNominatim();


        JsonNode response =
                nominatimClient
                        .get()
                        .uri(
                                uriBuilder ->
                                        uriBuilder
                                                .path("/reverse")
                                                .queryParam(
                                                        "format",
                                                        "jsonv2"
                                                )
                                                .queryParam(
                                                        "lat",
                                                        latitude
                                                )
                                                .queryParam(
                                                        "lon",
                                                        longitude
                                                )
                                                .queryParam(
                                                        "zoom",
                                                        12
                                                )
                                                .queryParam(
                                                        "addressdetails",
                                                        1
                                                )
                                                .build()
                        )
                        .retrieve()
                        .body(JsonNode.class);


        if (response == null) {
            return null;
        }


        JsonNode address =
                response.path("address");


        // Türkiye dışındaki noktaları kabul etme.
        String countryCode =
                address
                        .path("country_code")
                        .asText();


        if (!"tr".equalsIgnoreCase(countryCode)) {
            return null;
        }


        for (String field : LOCATION_FIELDS) {

            String value =
                    address
                            .path(field)
                            .asText("");


            if (!value.isBlank()) {
                return value;
            }
        }


        return null;
    }


    // Nominatim'e çok hızlı arka arkaya istek gönderilmesini engeller.
    private synchronized void waitForNominatim() {

        long elapsed =
                System.currentTimeMillis()
                        - lastNominatimRequestTime;


        long waitTime =
                NOMINATIM_INTERVAL_MS
                        - elapsed;


        if (waitTime > 0) {

            try {

                Thread.sleep(waitTime);

            } catch (InterruptedException exception) {

                Thread.currentThread().interrupt();

                throw new IllegalStateException(
                        "Konum servisi bekleme işlemi kesildi.",
                        exception
                );
            }
        }


        lastNominatimRequestTime =
                System.currentTimeMillis();
    }


    public enum Region {

        WEST(
                36.5,
                42.0,
                26.0,
                31.5
        ),

        CENTRAL(
                36.5,
                42.0,
                31.5,
                38.0
        ),

        EAST(
                36.5,
                42.0,
                38.0,
                44.5
        );


        private final double minLatitude;
        private final double maxLatitude;

        private final double minLongitude;
        private final double maxLongitude;


        Region(
                double minLatitude,
                double maxLatitude,
                double minLongitude,
                double maxLongitude
        ) {

            this.minLatitude = minLatitude;
            this.maxLatitude = maxLatitude;

            this.minLongitude = minLongitude;
            this.maxLongitude = maxLongitude;
        }
    }
}