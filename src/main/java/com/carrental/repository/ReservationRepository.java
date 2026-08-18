package com.carrental.repository;

import com.carrental.domain.CarType;
import com.carrental.domain.DateRange;
import com.carrental.domain.Reservation;

import java.util.List;

public interface ReservationRepository {

    void save(Reservation reservation);

    List<Reservation> findOverlappingReservations(CarType carType, DateRange dateRange);
}
