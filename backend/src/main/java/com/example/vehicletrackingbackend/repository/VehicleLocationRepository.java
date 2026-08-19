package com.example.vehicletrackingbackend.repository;

import com.example.vehicletrackingbackend.entity.VehicleLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleLocationRepository extends JpaRepository<VehicleLocation, Long> {
    Optional<VehicleLocation>
    findTopByVehicleIdOrderByTimestampDesc(String vehicleId);
    // yeniden eskiye göre sırala yani aracın son konumunu getir.

    List<VehicleLocation>
    findByVehicleIdOrderByTimestampAsc(String vehicleId);
    // bütün konumlarını eskiden yeniye doğru sırala
}

