package com.slginventory.model;

public class Vehicle {
    private int id;
    private String vehicleNumber;
    private String vehicleType;
    private int capacityKg;
    private String driverName;
    private String driverPhone;
    private String status;

    public Vehicle(int id, String vehicleNumber, String vehicleType, int capacityKg, 
                   String driverName, String driverPhone, String status) {
        this.id = id;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.capacityKg = capacityKg;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getVehicleType() { return vehicleType; }
    public int getCapacityKg() { return capacityKg; }
    public String getDriverName() { return driverName; }
    public String getDriverPhone() { return driverPhone; }
    public String getStatus() { return status; }

    // Setters
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public void setCapacityKg(int capacityKg) { this.capacityKg = capacityKg; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    public void setStatus(String status) { this.status = status; }
}

