package com.carrental.repository;

import com.carrental.domain.CarType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryFleetRepository implements FleetRepository {

    private final Map<CarType, Integer> capacities = new ConcurrentHashMap<>();

    @Override
    public int getCapacity(CarType carType) {
        return capacities.getOrDefault(carType, 0);
    }

    @Override
    public void setCapacity(CarType carType, int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative");
        }
        capacities.put(carType, capacity);
    }
}
