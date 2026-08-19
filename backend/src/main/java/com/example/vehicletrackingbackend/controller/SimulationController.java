package com.example.vehicletrackingbackend.controller;

import com.example.vehicletrackingbackend.service.VehicleSimulatorService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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


    // Frontend sayfası yeniden açıldığında
    // araç simülasyonunu baştan başlatır.
    @PostMapping("/reset")
    public void resetSimulation() {

        vehicleSimulatorService.resetSimulation();
    }
}