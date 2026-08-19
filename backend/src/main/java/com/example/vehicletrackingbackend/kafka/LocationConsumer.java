package com.example.vehicletrackingbackend.kafka;
import com.example.vehicletrackingbackend.event.LocationEvent;
import com.example.vehicletrackingbackend.entity.VehicleLocation;
import com.example.vehicletrackingbackend.repository.VehicleLocationRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class LocationConsumer {
    private final VehicleLocationRepository vehicleLocationRepository;

    public LocationConsumer(VehicleLocationRepository vehicleLocationRepository) {
        this.vehicleLocationRepository = vehicleLocationRepository;
    }

    @KafkaListener( // Bu metot kafkadan mesaj dinler

            // Bu metodun kafkadaki hangi topici dinleyeceini gösteriyo
            topics = "vehicle-location-events",

            // hangi consumeer grupa ait olduğunu belitiyor
            groupId = "spring-location-group"
    )
    public void consume(LocationEvent event) {

        VehicleLocation vehicleLocation = new VehicleLocation(
                event.getVehicleId(),
                event.getLatitude(),
                event.getLongitude(),
                event.getSpeed(),
                event.getTimestamp()
        );
        vehicleLocationRepository.save(vehicleLocation);

        System.out.println("Kafka'dan konum eventi alındı ve PostgreSQL'e kaydedildi: ");
        System.out.println("Araç ID: " + event.getVehicleId());
        System.out.println("Latitude: " + event.getLatitude());
        System.out.println("Longitude: " + event.getLongitude());
        System.out.println("Hız: " + event.getSpeed());
        System.out.println("Zaman: " + event.getTimestamp());
        System.out.println("-----------------------------");
    }
}
