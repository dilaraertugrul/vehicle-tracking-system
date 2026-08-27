package com.example.vehicletrackingbackend.service;

import com.example.vehicletrackingbackend.dto.RandomRoutePoint;
import com.example.vehicletrackingbackend.dto.VehicleRouteInfo;
import com.example.vehicletrackingbackend.event.LocationEvent;
import com.example.vehicletrackingbackend.kafka.LocationProducer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Service
public class VehicleSimulatorService {

    private static final int MIN_INITIAL_SPEED = 60;
    private static final int MAX_INITIAL_SPEED = 80;

    private static final int MIN_SPEED = 50;
    private static final int MAX_SPEED = 100;

    private static final int SPEED_CHANGE_LIMIT = 8;
    private static final int ROUTE_STEP_COUNT = 30;

    private final RouteService routeService;
    private final RandomRouteService randomRouteService;
    private final LocationProducer locationProducer;

    private final Random random = new Random();

    private final List<VehicleSimulation> vehicles = new ArrayList<>();

    public VehicleSimulatorService(
            RouteService routeService,
            RandomRouteService randomRouteService,
            LocationProducer locationProducer
    ) {
        this.routeService = routeService;
        this.randomRouteService = randomRouteService;
        this.locationProducer = locationProducer;
    }


    // Yeni simülasyon oluşturur.
    public synchronized void resetSimulation() {

        vehicles.clear();

        vehicles.add(createVehicle("CAR-101", RandomRouteService.Region.WEST));

        vehicles.add(createVehicle("CAR-102", RandomRouteService.Region.CENTRAL));

        vehicles.add(createVehicle("CAR-103", RandomRouteService.Region.EAST));

        System.out.println("Yeni simülasyon oluşturuldu.");

        for (VehicleSimulation vehicle : vehicles) {
            System.out.println(
                    vehicle.vehicleId
                            + " | "
                            + vehicle.startPoint.getLocationName()
                            + " → "
                            + vehicle.destinationPoint.getLocationName()
                            + " | "
                            + Math.round(vehicle.currentSpeed)
                            + " km/h"
            );
        }
    }


    // Araç için rastgele başlangıç, varış ve hız oluşturur.
    private VehicleSimulation createVehicle(String vehicleId, RandomRouteService.Region region) {

        RandomRoutePoint startPoint = randomRouteService.getRandomPoint(region);
        RandomRoutePoint destinationPoint;


        do {
            destinationPoint = randomRouteService.getRandomPoint(region);

        } while (startPoint.getLocationName().equals(destinationPoint.getLocationName())
        );


        List<List<Double>> routeCoordinates =
                routeService.getRouteCoordinates(
                        startPoint.getLatitude(),
                        startPoint.getLongitude(),
                        destinationPoint.getLatitude(),
                        destinationPoint.getLongitude()
                );

        return new VehicleSimulation(
                vehicleId,
                startPoint,
                destinationPoint,
                routeCoordinates,
                randomInitialSpeed()
        );
    }

    // Başlangıç hızını 60-80 km/h arasında üretir.
    private double randomInitialSpeed() {

        return MIN_INITIAL_SPEED + random.nextInt(MAX_INITIAL_SPEED - MIN_INITIAL_SPEED + 1);
    }

    // Araçları her saniye rota üzerinde ilerletir.
    @Scheduled(fixedDelay = 1000)
    public synchronized void simulateVehicleMovement() {

        for (VehicleSimulation vehicle : vehicles) {
            moveVehicle(vehicle);
        }
    }


    private void moveVehicle(VehicleSimulation vehicle) {

        if (vehicle.currentIndex >= vehicle.routeCoordinates.size()) {
            return;
        }

        List<Double> coordinate = vehicle.routeCoordinates.get(vehicle.currentIndex);

        // OSRM koordinat sırası: longitude, latitude
        double longitude = coordinate.get(0);
        double latitude = coordinate.get(1);


        LocationEvent event =
                new LocationEvent(
                        vehicle.vehicleId,
                        latitude,
                        longitude,
                        vehicle.currentSpeed,
                        LocalDateTime.now()
                );


        // LocationEvent Kafka topic'ine publish edilir.
        locationProducer.sendLocation(event);

        if (vehicle.currentIndex == vehicle.routeCoordinates.size() - 1) {

            System.out.println(
                    vehicle.vehicleId
                            + " varış noktasına ulaştı: "
                            + vehicle.destinationPoint.getLocationName()
            );

            vehicle.currentIndex = vehicle.routeCoordinates.size();
            return;
        }

        int stepSize = Math.max(1, (int) Math.ceil(vehicle.routeCoordinates.size() / (double) ROUTE_STEP_COUNT));

        vehicle.currentIndex = Math.min(vehicle.currentIndex + stepSize, vehicle.routeCoordinates.size() - 1);
    }


    // Frontend'e araçların başlangıç ve varış bilgilerini verir.
    public synchronized List<VehicleRouteInfo> getCurrentRoutes() {

        List<VehicleRouteInfo> routes = new ArrayList<>();

        for (VehicleSimulation vehicle : vehicles) {

            routes.add(
                    new VehicleRouteInfo(
                            vehicle.vehicleId,

                            vehicle.startPoint.getLocationName(),
                            vehicle.startPoint.getLatitude(),
                            vehicle.startPoint.getLongitude(),

                            vehicle.destinationPoint.getLocationName(),
                            vehicle.destinationPoint.getLatitude(),
                            vehicle.destinationPoint.getLongitude()
                    )
            );
        }
        return routes;
    }


    // Araç hızlarını 15 saniyede bir rastgele değiştirir.
    @Scheduled(fixedRate = 15000, initialDelay = 15000)
    public synchronized void changeVehicleSpeeds() {

        for (VehicleSimulation vehicle : vehicles) {

            if (vehicle.currentIndex >= vehicle.routeCoordinates.size()) {
                continue;
            }
            int speedChange = random.nextInt(SPEED_CHANGE_LIMIT * 2 + 1) - SPEED_CHANGE_LIMIT;

            vehicle.currentSpeed += speedChange;

            vehicle.currentSpeed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, vehicle.currentSpeed));
        }
    }


    // Her aracın simülasyon sırasında ihtiyaç duyduğu bilgileri tutar.
    private static class VehicleSimulation {

        private final String vehicleId;

        private final RandomRoutePoint startPoint;
        private final RandomRoutePoint destinationPoint;

        private final List<List<Double>> routeCoordinates;

        private int currentIndex = 0;
        private double currentSpeed;


        public VehicleSimulation(
                String vehicleId,
                RandomRoutePoint startPoint,
                RandomRoutePoint destinationPoint,
                List<List<Double>> routeCoordinates,
                double currentSpeed
        ) {
            this.vehicleId = vehicleId;
            this.startPoint = startPoint;
            this.destinationPoint = destinationPoint;
            this.routeCoordinates = routeCoordinates;
            this.currentSpeed = currentSpeed;
        }
    }
}