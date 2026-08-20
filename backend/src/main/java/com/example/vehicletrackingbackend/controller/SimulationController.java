package com.example.vehicletrackingbackend.controller;

import com.example.vehicletrackingbackend.dto.VehicleRouteInfo;
import com.example.vehicletrackingbackend.service.VehicleSimulatorService;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final VehicleSimulatorService vehicleSimulatorService;


    public SimulationController(
            VehicleSimulatorService vehicleSimulatorService
    ) {

        this.vehicleSimulatorService =
                vehicleSimulatorService;
    }


    // Sayfa yenilendiğinde:
    // 3 yeni rastgele rota oluştur
    // ve yeni rotaları frontend'e gönder.
    @PostMapping("/reset")
    public List<VehicleRouteInfo> resetSimulation() {

        vehicleSimulatorService.resetSimulation();

        return vehicleSimulatorService.getCurrentRoutes();
    }


    // Mevcut rotaları sadece okumak istersek.
    @GetMapping("/routes")
    public List<VehicleRouteInfo> getCurrentRoutes() {

        return vehicleSimulatorService.getCurrentRoutes();
    }
}