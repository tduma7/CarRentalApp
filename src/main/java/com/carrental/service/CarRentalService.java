package com.carrental.service;

import com.carrental.domain.CarType;
import com.carrental.domain.DateRange;
import com.carrental.domain.Reservation;
import com.carrental.exception.CarUnavailableException;
import com.carrental.repository.FleetRepository;
import com.carrental.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class CarRentalService {

    private final FleetRepository fleetRepository;
    private final ReservationRepository reservationRepository;
    private final Map<CarType, ReentrantLock> locks = new EnumMap<>(CarType.class);

    public CarRentalService(
            FleetRepository fleetRepository,
            ReservationRepository reservationRepository
    ) {
        this.fleetRepository = Objects.requireNonNull(fleetRepository);
        this.reservationRepository = Objects.requireNonNull(reservationRepository);

        for (CarType type : CarType.values()) {
            locks.put(type, new ReentrantLock());
        }
    }

    public Reservation reserveCar(
            String customerId,
            CarType carType,
            LocalDateTime startDateTime,
            int durationDays
    ) {
        DateRange requestedRange = DateRange.of(startDateTime, durationDays);
        ReentrantLock lock = locks.get(carType);

        lock.lock();
        try {
            int totalCapacity = fleetRepository.getCapacity(carType);
            List<Reservation> overlappingReservations =
                    reservationRepository.findOverlappingReservations(carType, requestedRange);

            if (overlappingReservations.size() >= totalCapacity) {
                throw new CarUnavailableException(String.format(
                        "No available %s for period %s to %s",
                        carType,
                        requestedRange.start(),
                        requestedRange.end()
                ));
            }

            Reservation reservation = new Reservation(customerId, carType, requestedRange);
            reservationRepository.save(reservation);
            return reservation;
        } finally {
            lock.unlock();
        }
    }
}
