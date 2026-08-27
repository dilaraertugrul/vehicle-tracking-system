package com.example.vehicletrackingbackend.kafka;

import com.example.vehicletrackingbackend.event.LocationEvent;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
public class LiveLocationConsumer {

    private final SimpMessagingTemplate messagingTemplate;


    public LiveLocationConsumer(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }


    @KafkaListener(
            topics = "vehicle-location-events",
            groupId = "live-location-group"
    )
    public void consume(LocationEvent event) {

        messagingTemplate.convertAndSend(
                "/topic/vehicle-locations",
                event
        );
    }
}