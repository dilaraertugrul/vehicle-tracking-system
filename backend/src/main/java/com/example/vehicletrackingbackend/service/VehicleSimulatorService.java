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

    private final RouteService routeService;
    private final RandomRouteService randomRouteService;
    private final LocationProducer locationProducer;

    private final Random random = new Random();

    private final List<VehicleSimulation> vehicles =
            new ArrayList<>();


    public VehicleSimulatorService(
            RouteService routeService,
            RandomRouteService randomRouteService,
            LocationProducer locationProducer
    ) {

        this.routeService = routeService;
        this.randomRouteService = randomRouteService;
        this.locationProducer = locationProducer;
    }


    // =====================================================
    // YENİ SİMÜLASYON
    // =====================================================

    public synchronized void resetSimulation() {

        vehicles.clear();


        // Her araç kendi bölgesinde çalışır.
        vehicles.add(
                createVehicle(
                        "CAR-101",
                        70,
                        RandomRouteService.Region.WEST
                )
        );


        vehicles.add(
                createVehicle(
                        "CAR-102",
                        65,
                        RandomRouteService.Region.CENTRAL
                )
        );


        vehicles.add(
                createVehicle(
                        "CAR-103",
                        75,
                        RandomRouteService.Region.EAST
                )
        );


        System.out.println(
                "Yeni simülasyon oluşturuldu."
        );


        for (VehicleSimulation vehicle : vehicles) {

            System.out.println(
                    vehicle.vehicleId
                            + " | "
                            + vehicle.startPoint.getLocationName()
                            + " → "
                            + vehicle.destinationPoint.getLocationName()
            );
        }
    }


    // =====================================================
    // TEK ARAÇ OLUŞTUR
    // =====================================================

    private VehicleSimulation createVehicle(
            String vehicleId,
            double speed,
            RandomRouteService.Region region
    ) {

        // Bölgeden rastgele başlangıç şehri.
        RandomRoutePoint startPoint =
                randomRouteService.getRandomPoint(
                        region
                );


        RandomRoutePoint destinationPoint;


        // Başlangıç ve varış aynı şehir olmasın.
        do {

            destinationPoint =
                    randomRouteService.getRandomPoint(
                            region
                    );

        }
        while (
                startPoint.getLocationName()
                        .equals(
                                destinationPoint.getLocationName()
                        )
        );


        // OSRM gerçek yol rotasını oluşturur.
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
                speed
        );
    }


    // =====================================================
    // ARAÇLARI HAREKET ETTİR
    // =====================================================

    @Scheduled(fixedDelay = 1000)
    public synchronized void simulateVehicleMovement() {

        if (vehicles.isEmpty()) {
            return;
        }


        for (VehicleSimulation vehicle : vehicles) {

            moveVehicle(vehicle);
        }
    }


    // =====================================================
    // TEK ARACI HAREKET ETTİR
    // =====================================================

    private void moveVehicle(
            VehicleSimulation vehicle
    ) {

        // Araç rotayı tamamladıysa dur.
        if (
                vehicle.currentIndex
                        >=
                        vehicle.routeCoordinates.size()
        ) {

            return;
        }


        /*
         * OSRM koordinatı:
         * [longitude, latitude]
         */
        List<Double> coordinate =
                vehicle.routeCoordinates.get(
                        vehicle.currentIndex
                );


        double longitude =
                coordinate.get(0);

        double latitude =
                coordinate.get(1);


        // Aracın anlık konum eventi.
        LocationEvent event =
                new LocationEvent(
                        vehicle.vehicleId,
                        latitude,
                        longitude,
                        vehicle.currentSpeed,
                        LocalDateTime.now()
                );


        // Kafka topic'ine publish edilir.
        locationProducer.sendLocation(event);


        // Araç son noktaya ulaştıysa dur.
        if (
                vehicle.currentIndex
                        ==
                        vehicle.routeCoordinates.size() - 1
        ) {

            System.out.println(
                    vehicle.vehicleId
                            + " varış noktasına ulaştı: "
                            + vehicle.destinationPoint.getLocationName()
            );


            vehicle.currentIndex =
                    vehicle.routeCoordinates.size();

            return;
        }


        /*
         * Demo sırasında rota yaklaşık
         * 30 saniyede tamamlansın.
         */
        int stepSize =
                Math.max(
                        1,
                        (int) Math.ceil(
                                vehicle.routeCoordinates.size()
                                        / 30.0
                        )
                );


        vehicle.currentIndex =
                Math.min(
                        vehicle.currentIndex + stepSize,
                        vehicle.routeCoordinates.size() - 1
                );
    }


    // =====================================================
    // FRONTEND'E ROTA BİLGİLERİNİ VER
    // =====================================================

    public synchronized List<VehicleRouteInfo> getCurrentRoutes() {

        List<VehicleRouteInfo> routes =
                new ArrayList<>();


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


    // =====================================================
    // RASTGELE HIZ DEĞİŞİMİ
    // =====================================================

    @Scheduled(
            fixedRate = 15000,
            initialDelay = 15000
    )
    public synchronized void changeVehicleSpeeds() {

        for (VehicleSimulation vehicle : vehicles) {

            if (
                    vehicle.currentIndex
                            >=
                            vehicle.routeCoordinates.size()
            ) {

                continue;
            }


            // -8 ile +8 arasında değişim.
            vehicle.currentSpeed +=
                    random.nextInt(17) - 8;


            // Hız 50 - 100 km/h arasında kalır.
            vehicle.currentSpeed =
                    Math.max(
                            50,
                            Math.min(
                                    100,
                                    vehicle.currentSpeed
                            )
                    );
        }
    }


    // =====================================================
    // ARAÇ SİMÜLASYON DURUMU
    // =====================================================

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