package com.example.vehicletrackingbackend.controller;

import com.example.vehicletrackingbackend.dto.RouteEstimate;
// Kalan mesafe ve kalan süre bilgisini frontend'e göndermek için kullandığımız DTO.

import com.example.vehicletrackingbackend.service.RouteService;
// OSRM ile iletişim kuran RouteService sınıfını burada kullanabilmek için import ediyoruz.

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
// Controller'ın ana URL adresini belirlemek için kullanılır.

import org.springframework.web.bind.annotation.RequestParam;
// URL üzerinden latitude ve longitude değerlerini alabilmek için kullanılır.

import org.springframework.web.bind.annotation.RestController;
// Bu sınıfın REST API Controller olduğunu Spring'e belirtir.

import java.util.List;


@RestController

@RequestMapping("/api/routes")
// Bütün endpoint'ler /api/routes ile başlayacak.
public class RouteController {


    private final RouteService routeService;
    // OSRM'ye istek gönderebilmemiz için gerekli service nesnesi.


    // Constructor Injection:
    // Spring bize RouteService nesnesini dışarıdan verir.
    public RouteController(RouteService routeService) {

        this.routeService = routeService;
    }


    // =====================================================
    // İSTANBUL → MALATYA PLANLANAN ROTASI
    // =====================================================

    @GetMapping("/test")
    public List<List<Double>> getTestRoute() {

        // OSRM'den İstanbul → Malatya rota koordinatlarını alır.
        return routeService.getRouteCoordinates();
    }


    // =====================================================
    // GÜNCEL KONUMDAN MALATYA'YA
    // KALAN MESAFE VE SÜRE
    // =====================================================

    @GetMapping("/remaining")
    public RouteEstimate getRemainingRouteEstimate(

            // URL'den latitude değerini alıyoruz.
            @RequestParam double latitude,

            // URL'den longitude değerini alıyoruz.
            @RequestParam double longitude

    ) {

        // Gelen güncel araç koordinatlarını RouteService'e gönderiyoruz.
        //
        // RouteService de OSRM'ye soruyor:
        //
        // "Bu konumdan Malatya'ya ne kadar mesafe
        // ve tahmini ne kadar süre kaldı?"
        return routeService.getRemainingRouteEstimate(
                latitude,
                longitude
        );
    }
}


// LocationController:
// Kafka tarafına veri gönderiyor.
//
// RouteController:
// OSRM'den rota, kalan mesafe ve kalan süre bilgisi istiyor.
//
// DTO:
// API'den gelen/giden verinin Java'daki düzenli karşılığı.