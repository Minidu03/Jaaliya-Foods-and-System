package com.slginventory.model;

import java.time.LocalDateTime;

public class Transport {
    private int id;
    private int vehicleId;
    private String vehicleNumber;
    private String sourceCenter;
    private String destinationCenter;
    private int goodsId;
    private String goodsName;
    private int quantity;
    private String status;
    private LocalDateTime scheduledDate;
    private LocalDateTime departureDate;
    private LocalDateTime arrivalDate;
    private String notes;

    public Transport(int id, int vehicleId, String vehicleNumber, String sourceCenter, 
                     String destinationCenter, int goodsId, String goodsName, int quantity,
                     String status, LocalDateTime scheduledDate, LocalDateTime departureDate,
                     LocalDateTime arrivalDate, String notes) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.sourceCenter = sourceCenter;
        this.destinationCenter = destinationCenter;
        this.goodsId = goodsId;
        this.goodsName = goodsName;
        this.quantity = quantity;
        this.status = status;
        this.scheduledDate = scheduledDate;
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
        this.notes = notes;
    }

    // Getters
    public int getId() { return id; }
    public int getVehicleId() { return vehicleId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getSourceCenter() { return sourceCenter; }
    public String getDestinationCenter() { return destinationCenter; }
    public int getGoodsId() { return goodsId; }
    public String getGoodsName() { return goodsName; }
    public int getQuantity() { return quantity; }
    public String getStatus() { return status; }
    public LocalDateTime getScheduledDate() { return scheduledDate; }
    public LocalDateTime getDepartureDate() { return departureDate; }
    public LocalDateTime getArrivalDate() { return arrivalDate; }
    public String getNotes() { return notes; }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setDepartureDate(LocalDateTime departureDate) { this.departureDate = departureDate; }
    public void setArrivalDate(LocalDateTime arrivalDate) { this.arrivalDate = arrivalDate; }
    public void setNotes(String notes) { this.notes = notes; }
}

