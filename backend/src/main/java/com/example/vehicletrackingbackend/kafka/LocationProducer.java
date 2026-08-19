package com.example.vehicletrackingbackend.kafka;

import com.example.vehicletrackingbackend.event.LocationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
// Spring bu sınıfın nesnesini oluşturur ve yönetir.
public class LocationProducer {

    // Kafka'ya mesaj göndermek için Spring'in sağladığı araç.
    private final KafkaTemplate<String, LocationEvent> kafkaTemplate;


    public LocationProducer(
            KafkaTemplate<String, LocationEvent> kafkaTemplate
    ) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendLocation(LocationEvent event) {

        kafkaTemplate.send(
                // Mesajın gönderileceği topic.
                "vehicle-location-events",
                event.getVehicleId(),
                event
        );
    }
}

