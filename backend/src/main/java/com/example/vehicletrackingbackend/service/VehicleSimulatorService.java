package com.example.vehicletrackingbackend.service;

import com.example.vehicletrackingbackend.event.LocationEvent;
import com.example.vehicletrackingbackend.kafka.LocationProducer;

import org.springframework.scheduling.annotation.Scheduled;
// nesneyi belirli zaman aralıklarında otomatik çalışmasını sağlar.
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VehicleSimulatorService {
    private final RouteService routeService;
    // Rota koordinatlarını OSRM'den almak için kullanılır

    private final LocationProducer locationProducer;
    // Location eventleri kafkaya göndermek için

    private List<List<Double>> routeCoordinates;

    private int currentIndex= 0;

    public VehicleSimulatorService(
            RouteService routeService,
            LocationProducer locationProducer
    ){
        this.routeService = routeService;
        this.locationProducer = locationProducer;
    }

    @Scheduled(fixedDelay = 3000)
    public void simulateVehicleMovement(){
        if(routeCoordinates == null){
            routeCoordinates = routeService.getRouteCoordinates();
            System.out.println("Rota alındı. Toplam Koordinat: " + routeCoordinates.size());
        }
        if (currentIndex >= routeCoordinates.size()){
            System.out.println("Araç varış noktasına ulaştı.");
            return;
        }
        List<Double> coordinate = routeCoordinates.get(currentIndex);
        double longitude = coordinate.get(0);
        double latitude = coordinate.get(1);

        LocationEvent event = new LocationEvent(
                "CAR-101",
                latitude,
                longitude,
                70,
                LocalDateTime.now()
        );
        locationProducer.sendLocation(event); // kafkaya gönderiyor

        System.out.println(
                "Araç hareket etti -> " + "Index: " + currentIndex + " | Lat: " + latitude + " | Lon: " + longitude
        );

        currentIndex++; // sonraki koordinata geç.
    }
}
