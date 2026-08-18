package com.carrental.repository;

import com.carrental.domain.CarType;

public interface FleetRepository {

    int getCapacity(CarType carType);

    void setCapacity(CarType carType, int capacity);
}
