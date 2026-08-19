package com.example.vehicletrackingbackend.service;

import org.springframework.http.HttpHeaders;
// HTTP header isimlerini kullanabilmek için gerekli.

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
// Başka bir API'ye istek göndermek için RestClient kullanıyoruz.

import com.example.vehicletrackingbackend.dto.OsrmResponse;
// OSRM'den gelen JSON cevabını Java nesnesine çevirmek için kullanıyoruz.

import com.example.vehicletrackingbackend.dto.OsrmRoute;
// OSRM'den gelen route içindeki distance ve duration bilgilerine ulaşmak için.

import com.example.vehicletrackingbackend.dto.RouteEstimate;
// Kalan mesafe ve kalan süreyi frontend'e göndermek için oluşturduğumuz DTO.

import java.util.List;
// Rota koordinatlarını liste olarak döndürmek için kullanıyoruz.


@Service
public class RouteService {

    // OSRM API'ye HTTP isteği göndermek için kullanacağımız nesne.
    private final RestClient restClient;


    public RouteService() {

        this.restClient = RestClient.builder()

                // Bütün OSRM isteklerimizin başlangıç adresi.
                .baseUrl("https://router.project-osrm.org")

                // Daha önce yaşadığımız sıkıştırma sorununu önlemek için.
                .defaultHeader(
                        HttpHeaders.ACCEPT_ENCODING,
                        "identity"
                )

                // RestClient nesnesini oluşturuyoruz.
                .build();
    }


    // =====================================================
    // İSTANBUL → MALATYA PLANLANAN ROTASINI AL
    // =====================================================

    public List<List<Double>> getRouteCoordinates() {

        // OSRM'den gelen cevabı doğrudan
        // OsrmResponse nesnesine çeviriyoruz.
        OsrmResponse response = restClient.get()

                // OSRM API'ye GET isteği gönderiyoruz.
                .uri(
                        "/route/v1/driving/28.9784,41.0082;38.3095,38.3552" +
                                "?overview=full&geometries=geojson"
                )

                // HTTP isteğini gönderip cevabı alıyoruz.
                .retrieve()

                // Gelen JSON'u OsrmResponse nesnesine çeviriyoruz.
                .body(OsrmResponse.class);


        return response

                .getRoutes()
                // OSRM cevabındaki routes listesini alıyoruz.

                .get(0)
                // İlk rotayı seçiyoruz.

                .getGeometry()
                // Seçilen rotanın geometry bilgisini alıyoruz.

                .getCoordinates();
        // Geometry içindeki bütün yol koordinatlarını döndürüyoruz.
    }


    // =====================================================
    // ARACIN GÜNCEL KONUMUNDAN MALATYA'YA
    // KALAN MESAFE VE SÜREYİ HESAPLA
    // =====================================================

    public RouteEstimate getRemainingRouteEstimate(
            double latitude,
            double longitude
    ) {

        /*
         * latitude ve longitude değerleri
         * CAR-101'in o anki konumunu temsil eder.
         *
         * Örneğin:
         *
         * latitude  = 40.99697
         * longitude = 29.134424
         */


        // Malatya'nın varış koordinatları.
        double destinationLatitude = 38.3552;
        double destinationLongitude = 38.3095;


        /*
         * OSRM koordinatları şu sırada ister:
         *
         * longitude,latitude
         *
         * Bu nedenle URI oluştururken
         * longitude değerini önce yazıyoruz.
         */


        OsrmResponse response = restClient.get()

                .uri(
                        "/route/v1/driving/" +

                                // CAR-101'in güncel konumu.
                                longitude + "," +
                                latitude + ";" +

                                // Malatya varış noktası.
                                destinationLongitude + "," +
                                destinationLatitude +

                                // Burada rota çizgisine ihtiyacımız yok.
                                // Sadece distance ve duration istiyoruz.
                                "?overview=false"
                )

                .retrieve()

                .body(OsrmResponse.class);


        /*
         * OSRM'nin routes listesindeki
         * ilk rota bilgisini alıyoruz.
         */
        OsrmRoute route = response

                .getRoutes()

                .get(0);


        /*
         * route.getDistance()
         * → Malatya'ya kalan mesafe
         * → metre cinsinden
         *
         * route.getDuration()
         * → Malatya'ya kalan tahmini süre
         * → saniye cinsinden
         */


        // Frontend'e gönderebileceğimiz
        // sade RouteEstimate nesnesini oluşturuyoruz.
        return new RouteEstimate(

                route.getDistance(),

                route.getDuration()
        );
    }
}