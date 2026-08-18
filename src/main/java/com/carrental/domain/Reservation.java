package com.carrental.domain;

import java.util.Objects;
import java.util.UUID;

public class Reservation {

    private final UUID id;
    private final String customerId;
    private final CarType carType;
    private final DateRange dateRange;

    public Reservation(String customerId, CarType carType, DateRange dateRange) {
        this.id = UUID.randomUUID();
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.carType = Objects.requireNonNull(carType, "Car type cannot be null");
        this.dateRange = Objects.requireNonNull(dateRange, "Date range cannot be null");
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public CarType getCarType() {
        return carType;
    }

    public DateRange getDateRange() {
        return dateRange;
    }
}
