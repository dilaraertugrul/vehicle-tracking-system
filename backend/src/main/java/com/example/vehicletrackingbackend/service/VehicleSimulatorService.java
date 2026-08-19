package com.example.vehicletrackingbackend.service;

import com.example.vehicletrackingbackend.event.LocationEvent;
import com.example.vehicletrackingbackend.kafka.LocationProducer;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class VehicleSimulatorService {

    private final RouteService routeService;

    private final LocationProducer locationProducer;

    private List<List<Double>> routeCoordinates;

    private int currentIndex = 0;
    private double currentSpeed = 70;
    private final Random random = new Random();

    public VehicleSimulatorService(
            RouteService routeService,
            LocationProducer locationProducer
    ) {

        this.routeService = routeService;
        this.locationProducer = locationProducer;
    }


    @Scheduled(fixedDelay = 2000)
    public void simulateVehicleMovement() {

        if (routeCoordinates == null) {

            routeCoordinates = routeService.getRouteCoordinates();

            System.out.println(
                    "Rota alındı. Toplam Koordinat: "
                            + routeCoordinates.size()
            );
        }

        if (currentIndex >= routeCoordinates.size()) {

            System.out.println(
                    "Araç varış noktasına ulaştı."
            );

            return;
        }

        List<Double> coordinate =
                routeCoordinates.get(currentIndex);

        double longitude = coordinate.get(0);
        double latitude = coordinate.get(1);


        LocationEvent event = new LocationEvent(

                "CAR-101",

                latitude,

                longitude,

                currentSpeed,

                LocalDateTime.now()
        );


        // Oluşturulan konum eventini Kafka'ya gönderiyoruz.
        locationProducer.sendLocation(event);


        System.out.println(
                "Araç hareket etti -> "
                        + "Index: " + currentIndex
                        + " | Lat: " + latitude
                        + " | Lon: " + longitude
                        + " | Hız: " + currentSpeed + " km/h"
        );

        currentIndex++;
    }


    @Scheduled(
            fixedRate = 15000,
            initialDelay = 15000
    )

    public void changeVehicleSpeed() {

        if (routeCoordinates != null
                && currentIndex >= routeCoordinates.size()) {

            return;
        }
        int speedChange =
                random.nextInt(17) - 8;


        currentSpeed = currentSpeed + speedChange;
        if (currentSpeed < 50) {
            currentSpeed = 50;
        }


        // Hız 100 km/h üzerine çıkmasın.
        if (currentSpeed > 100) {
            currentSpeed = 100;
        }


        System.out.println(
                "Araç hızı değişti -> "
                        + currentSpeed
                        + " km/h"
        );
    }
}