package com.carrental.repository;

import com.carrental.domain.CarType;
import com.carrental.domain.DateRange;
import com.carrental.domain.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations = new CopyOnWriteArrayList<>();

    @Override
    public void save(Reservation reservation) {
        reservations.add(reservation);
    }

    @Override
    public List<Reservation> findOverlappingReservations(CarType carType, DateRange dateRange) {
        List<Reservation> result = new ArrayList<>();

        for (Reservation reservation : reservations) {
            boolean hasMatchingCarType = reservation.getCarType() == carType;
            boolean overlaps = reservation.getDateRange().overlapsWith(dateRange);

            if (hasMatchingCarType && overlaps) {
                result.add(reservation);
            }
        }

        return result;
    }
}
