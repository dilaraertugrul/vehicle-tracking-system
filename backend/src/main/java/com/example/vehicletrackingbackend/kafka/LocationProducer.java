package com.example.vehicletrackingbackend.kafka;

import com.example.vehicletrackingbackend.event.LocationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service // Bunun nesnesini kendisi oluşturup yönetecek.
public class LocationProducer {
    private final KafkaTemplate<String, LocationEvent> kafkaTemplate; // Springin kafkaya mesaj göndermek için verdiği hazır araç

    public LocationProducer(KafkaTemplate<String, LocationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void sendLocation(LocationEvent event) {
        kafkaTemplate.send("vehicle-location-events", event); // hangi topic olduğu ve ne göndereceği bilgisi veriliyor.
    }
}
/*
Oluşturulan LocationEvent nesnesini Kafkadaki topice göndermek.
 */
