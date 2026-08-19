package com.example.vehicletrackingbackend.controller;

import com.example.vehicletrackingbackend.dto.RouteEstimate;
import com.example.vehicletrackingbackend.service.RouteService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/routes")
public class RouteController {

    // Rota işlemlerini yapan service sınıfı.
    private final RouteService routeService;


    // Constructor Injection:
    // Spring, RouteService nesnesini Controller'a verir.
    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }


    // =====================================================
    // BAŞLANGIÇ → VARIŞ ROTASINI GETİR
    // =====================================================

    @GetMapping
    public List<List<Double>> getRoute(

            @RequestParam double startLatitude,
            @RequestParam double startLongitude,

            @RequestParam double destinationLatitude,
            @RequestParam double destinationLongitude

    ) {

        // Gelen başlangıç ve varış koordinatlarını
        // RouteService'e gönderiyoruz.
        return routeService.getRouteCoordinates(
                startLatitude,
                startLongitude,
                destinationLatitude,
                destinationLongitude
        );
    }


    // =====================================================
    // GÜNCEL KONUMDAN VARIŞ NOKTASINA
    // KALAN MESAFE VE SÜRE
    // =====================================================

    @GetMapping("/remaining")
    public RouteEstimate getRemainingRouteEstimate(

            // Aracın güncel konumu.
            @RequestParam double latitude,
            @RequestParam double longitude,

            // Aracın varış noktası.
            @RequestParam double destinationLatitude,
            @RequestParam double destinationLongitude

    ) {

        return routeService.getRemainingRouteEstimate(
                latitude,
                longitude,
                destinationLatitude,
                destinationLongitude
        );
    }
}