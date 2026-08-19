package com.example.vehicletrackingbackend.service;

import com.example.vehicletrackingbackend.dto.OsrmResponse;
import com.example.vehicletrackingbackend.dto.OsrmRoute;
import com.example.vehicletrackingbackend.dto.RouteEstimate;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Locale;


@Service
public class RouteService {

    // OSRM API'ye HTTP isteği göndermek için kullanılır.
    private final RestClient restClient;


    public RouteService() {

        this.restClient = RestClient.builder()

                // OSRM API'nin temel adresi.
                .baseUrl("https://router.project-osrm.org")

                // Sıkıştırma kaynaklı hataları önlemek için.
                .defaultHeader(
                        HttpHeaders.ACCEPT_ENCODING,
                        "identity"
                )

                .build();
    }

    public List<List<Double>> getRouteCoordinates(
            double startLatitude,
            double startLongitude,
            double destinationLatitude,
            double destinationLongitude
    ) {

        String uri = String.format(
                Locale.US,
                "/route/v1/driving/%f,%f;%f,%f" +
                        "?overview=full&geometries=geojson",

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


        // İlk rotanın bütün yol koordinatlarını döndürüyoruz.
        return response
                .getRoutes()
                .get(0)
                .getGeometry()
                .getCoordinates();
    }

    public RouteEstimate getRemainingRouteEstimate(
            double currentLatitude,
            double currentLongitude,
            double destinationLatitude,
            double destinationLongitude
    ) {

        // Aracın mevcut konumu ile
        // varış noktası arasında OSRM isteği oluşturuyoruz.
        String uri = String.format(
                Locale.US,
                "/route/v1/driving/%f,%f;%f,%f?overview=false",

                currentLongitude,
                currentLatitude,
                destinationLongitude,
                destinationLatitude
        );


        // OSRM API'ye isteği gönderiyoruz.
        OsrmResponse response = restClient
                .get()
                .uri(uri)
                .retrieve()
                .body(OsrmResponse.class);


        // Geçerli rota bilgisi gelmediyse
        // hata oluşturuyoruz.
        if (response == null
                || response.getRoutes() == null
                || response.getRoutes().isEmpty()) {

            throw new RuntimeException(
                    "OSRM'den kalan rota bilgisi alınamadı."
            );
        }


        // OSRM'nin döndürdüğü ilk rota bilgisini alıyoruz.
        OsrmRoute route = response
                .getRoutes()
                .get(0);


        // Frontend'e kalan mesafe ve süreyi gönderiyoruz.
        return new RouteEstimate(
                route.getDistance(),
                route.getDuration()
        );
    }
}