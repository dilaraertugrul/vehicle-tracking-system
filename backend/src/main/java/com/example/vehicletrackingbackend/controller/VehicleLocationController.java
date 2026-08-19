package com.example.vehicletrackingbackend.controller;

import com.example.vehicletrackingbackend.entity.VehicleLocation;
import com.example.vehicletrackingbackend.repository.VehicleLocationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController // HTTP isteklerini karşılar

@RequestMapping("/api/vehicles/") // ortak başlangıç adresleri
public class VehicleLocationController {

    private final VehicleLocationRepository vehicleLocationRepository;

    public VehicleLocationController(VehicleLocationRepository vehicleLocationRepository) {
        this.vehicleLocationRepository = vehicleLocationRepository;
    }
    @GetMapping("/{vehicleId}/latest")
    public Optional<VehicleLocation> getLatest(@PathVariable String vehicleId) {
        return vehicleLocationRepository.findTopByVehicleIdOrderByTimestampDesc(vehicleId); // en yenisini getir
    }

    @GetMapping("/{vehicleId}/history")
    public List<VehicleLocation> getHistory(@PathVariable String vehicleId) {
        return vehicleLocationRepository
                .findByVehicleIdOrderByTimestampAsc(vehicleId);
    }
}
