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

    private final Random random = new Random();


    // Simülasyonda takip edeceğimiz araçlar.
    private final List<VehicleSimulation> vehicles = List.of(

            // CAR-101: İstanbul → Bolu
            new VehicleSimulation(
                    "CAR-101",
                    41.0082,   // İstanbul latitude
                    28.9784,   // İstanbul longitude
                    40.7350,   // Bolu latitude
                    31.6066,   // Bolu longitude
                    70
            ),

            // CAR-102: Ankara → Konya
            new VehicleSimulation(
                    "CAR-102",
                    39.9334,
                    32.8597,
                    37.8746,
                    32.4932,
                    65
            ),

            // CAR-103: İzmir → Antalya
            new VehicleSimulation(
                    "CAR-103",
                    38.4237,
                    27.1428,
                    36.8969,
                    30.7133,
                    75
            )
    );


    public VehicleSimulatorService(
            RouteService routeService,
            LocationProducer locationProducer
    ) {

        this.routeService = routeService;
        this.locationProducer = locationProducer;
    }


    @Scheduled(fixedDelay = 1000)
    // Demo için her 1 saniyede bir araçların konumu güncellenir.
    public void simulateVehicleMovement() {
        for (VehicleSimulation vehicle : vehicles) {

            moveVehicle(vehicle);
        }
    }

    private void moveVehicle(VehicleSimulation vehicle) {
        if (vehicle.routeCoordinates == null) {

            vehicle.routeCoordinates =
                    routeService.getRouteCoordinates(

                            vehicle.startLatitude,
                            vehicle.startLongitude,

                            vehicle.destinationLatitude,
                            vehicle.destinationLongitude
                    );


            System.out.println(
                    vehicle.vehicleId
                            + " rotası alındı. Toplam koordinat: "
                            + vehicle.routeCoordinates.size()
            );
        }

        if (vehicle.currentIndex >= vehicle.routeCoordinates.size()) {

            return;
        }


        // Aracın o an bulunduğu rota noktasını alıyoruz.
        List<Double> coordinate =
                vehicle.routeCoordinates.get(
                        vehicle.currentIndex
                );


        // OSRM koordinat sırası:
        // longitude, latitude
        double longitude =
                coordinate.get(0);

        double latitude =
                coordinate.get(1);


        // Aracın güncel konum eventi.
        LocationEvent event =
                new LocationEvent(

                        vehicle.vehicleId,

                        latitude,

                        longitude,

                        vehicle.currentSpeed,

                        LocalDateTime.now()
                );


        // Konum eventini Kafka'ya gönderiyoruz.
        locationProducer.sendLocation(event);


        System.out.println(
                vehicle.vehicleId
                        + " hareket etti"
                        + " | Index: " + vehicle.currentIndex
                        + " | Lat: " + latitude
                        + " | Lon: " + longitude
                        + " | Hız: " + vehicle.currentSpeed
                        + " km/h"
        );


        // Eğer araç son koordinata geldiyse
        // varış noktasına ulaşmıştır.
        if (
                vehicle.currentIndex
                        ==
                        vehicle.routeCoordinates.size() - 1
        ) {

            System.out.println(
                    " "
                            + vehicle.vehicleId
                            + " VARIŞ NOKTASINA ULAŞTI."
            );


            // Bir sonraki çalışmada tekrar event göndermemesi için
            // index'i listenin dışına çıkarıyoruz.
            vehicle.currentIndex =
                    vehicle.routeCoordinates.size();

            return;
        }


        // =================================================
        // DEMO HIZLANDIRMA
        // =================================================
        //
        // Normalde:
        //
        // currentIndex++;
        //
        // diyerek her saniye sadece 1 rota noktası ilerliyorduk.
        //
        // Demo sırasında rotayı yaklaşık 30 saniyede
        // tamamlamak için her seferinde rotanın
        // yaklaşık 1/30'u kadar ilerliyoruz.

        int stepSize =
                Math.max(
                        1,
                        (int) Math.ceil(
                                vehicle.routeCoordinates.size()
                                        / 30.0
                        )
                );


        // Son koordinatı geçmemesi için
        // Math.min kullanıyoruz.
        vehicle.currentIndex =
                Math.min(

                        vehicle.currentIndex
                                +
                                stepSize,

                        vehicle.routeCoordinates.size() - 1
                );
    }

    // Sayfa yenilendiğinde bütün araçları
// başlangıç noktasına döndürür.
    public void resetSimulation() {

        for (VehicleSimulation vehicle : vehicles) {

            // Aracı rotanın ilk noktasına döndür.
            vehicle.currentIndex = 0;

            // Hızı tekrar başlangıç değerine getir.
            if (vehicle.vehicleId.equals("CAR-101")) {
                vehicle.currentSpeed = 70;
            }

            if (vehicle.vehicleId.equals("CAR-102")) {
                vehicle.currentSpeed = 65;
            }

            if (vehicle.vehicleId.equals("CAR-103")) {
                vehicle.currentSpeed = 75;
            }
        }

        System.out.println(
                " Simülasyon baştan başlatıldı."
        );
    }

    @Scheduled(
            fixedRate = 15000,
            initialDelay = 15000
    )
    public void changeVehicleSpeeds() {

        for (VehicleSimulation vehicle : vehicles) {

            changeSpeed(vehicle);
        }
    }


    // =====================================================
    // TEK BİR ARACIN HIZINI DEĞİŞTİR
    // =====================================================

    private void changeSpeed(VehicleSimulation vehicle) {

        // Araç rotayı bitirdiyse
        // artık hızını değiştirmiyoruz.
        if (
                vehicle.routeCoordinates != null
                        &&
                        vehicle.currentIndex
                                >=
                                vehicle.routeCoordinates.size()
        ) {

            return;
        }


        // -8 ile +8 km/h arasında
        // rastgele değişim oluşturur.
        int speedChange =
                random.nextInt(17) - 8;


        vehicle.currentSpeed =
                vehicle.currentSpeed
                        +
                        speedChange;


        // Minimum 50 km/h.
        if (vehicle.currentSpeed < 50) {

            vehicle.currentSpeed = 50;
        }


        // Maksimum 100 km/h.
        if (vehicle.currentSpeed > 100) {

            vehicle.currentSpeed = 100;
        }


        System.out.println(
                vehicle.vehicleId
                        + " hızı değişti -> "
                        + vehicle.currentSpeed
                        + " km/h"
        );
    }


    // =====================================================
    // ARAÇ SİMÜLASYON BİLGİLERİ
    // =====================================================

    private static class VehicleSimulation {

        // Aracın sistemdeki benzersiz kimliği.
        private final String vehicleId;


        // Başlangıç noktası.
        private final double startLatitude;
        private final double startLongitude;


        // Varış noktası.
        private final double destinationLatitude;
        private final double destinationLongitude;


        // OSRM'den gelen rota koordinatları.
        private List<List<Double>> routeCoordinates;


        // Aracın rota üzerindeki güncel noktası.
        private int currentIndex = 0;


        // Aracın ekranda gösterilen anlık hızı.
        private double currentSpeed;


        public VehicleSimulation(
                String vehicleId,
                double startLatitude,
                double startLongitude,
                double destinationLatitude,
                double destinationLongitude,
                double currentSpeed
        ) {

            this.vehicleId =
                    vehicleId;

            this.startLatitude =
                    startLatitude;

            this.startLongitude =
                    startLongitude;

            this.destinationLatitude =
                    destinationLatitude;

            this.destinationLongitude =
                    destinationLongitude;

            this.currentSpeed =
                    currentSpeed;
        }
    }
}