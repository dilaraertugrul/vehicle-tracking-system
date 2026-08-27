package com.example.vehicletrackingbackend.service;

import com.example.vehicletrackingbackend.dto.OsrmResponse;
import com.example.vehicletrackingbackend.dto.OsrmRoute;
import com.example.vehicletrackingbackend.dto.RouteEstimate;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Locale;

// OSRM ile gerçek yolu bulur
@Service
public class RouteService {

    private final RestClient restClient; // dış servislere istek atıyr

    public RouteService() {

        this.restClient = RestClient.builder()
                .baseUrl("https://router.project-osrm.org")
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "identity")
                .build();
    }


    // Başlangıç ve varış arasındaki gerçek yol koordinatlarını alır.
    public List<List<Double>> getRouteCoordinates(
            double startLatitude,
            double startLongitude,
            double destinationLatitude,
            double destinationLongitude
    ) {

        String uri = String.format(
                Locale.US,
                "/route/v1/driving/%f,%f;%f,%f"
                        + "?overview=full&geometries=geojson",
                startLongitude,
                startLatitude,
                destinationLongitude,
                destinationLatitude
        );


        OsrmResponse response = restClient
                .get()
                .uri(uri)
                .retrieve()
                .body(OsrmResponse.class);


        if (response == null
                || response.getRoutes() == null
                || response.getRoutes().isEmpty()) {

            throw new RuntimeException(
                    "OSRM'den rota alınamadı."
            );
        }


        return response
                .getRoutes()
                .get(0)
                .getGeometry()
                .getCoordinates();
    }


    // Rastgele koordinatı en yakın sürülebilir yol noktasına taşır.
    public RoadPoint snapToNearestRoad(
            double latitude,
            double longitude
    ) {

        String uri = String.format(
                Locale.US,
                "/nearest/v1/driving/%f,%f?number=1",
                longitude,
                latitude
        );


        JsonNode response = restClient
                .get()
                .uri(uri)
                .retrieve()
                .body(JsonNode.class);


        if (response == null) {

            throw new RuntimeException(
                    "OSRM'den en yakın yol bilgisi alınamadı."
            );
        }


        JsonNode waypoints = response.path("waypoints");


        if (!waypoints.isArray()
                || waypoints.size() == 0) {

            throw new RuntimeException(
                    "Yakında sürülebilir yol bulunamadı."
            );
        }


        JsonNode location =
                waypoints
                        .get(0)
                        .path("location");


        if (!location.isArray()
                || location.size() < 2) {

            throw new RuntimeException(
                    "OSRM geçerli yol koordinatı döndürmedi."
            );
        }


        // OSRM sırası: longitude, latitude
        return new RoadPoint(
                location.get(1).asDouble(),
                location.get(0).asDouble()
        );
    }


    // Aracın mevcut konumundan varışa kalan mesafe ve süreyi alır.
    public RouteEstimate getRemainingRouteEstimate(
            double currentLatitude,
            double currentLongitude,
            double destinationLatitude,
            double destinationLongitude
    ) {

        String uri = String.format(
                Locale.US,
                "/route/v1/driving/%f,%f;%f,%f?overview=false",
                currentLongitude,
                currentLatitude,
                destinationLongitude,
                destinationLatitude
        );


        OsrmResponse response = restClient
                .get()
                .uri(uri)
                .retrieve()
                .body(OsrmResponse.class);


        if (response == null
                || response.getRoutes() == null
                || response.getRoutes().isEmpty()) {

            throw new RuntimeException(
                    "OSRM'den kalan rota bilgisi alınamadı."
            );
        }

        OsrmRoute route = response.getRoutes().get(0);

        return new RouteEstimate(
                route.getDistance(),
                route.getDuration()
        );
    }

    // OSRM nearest sonucundaki yol koordinatını temsil eder.
    public record RoadPoint(double latitude, double longitude) {
    }
}