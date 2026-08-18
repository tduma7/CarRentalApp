package com.carrental.service;

import com.carrental.domain.CarType;
import com.carrental.domain.Reservation;
import com.carrental.exception.CarUnavailableException;
import com.carrental.repository.InMemoryFleetRepository;
import com.carrental.repository.InMemoryReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarRentalServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 1, 10, 0);

    private InMemoryFleetRepository fleetRepository;
    private CarRentalService rentalService;

    @BeforeEach
    void setUp() {
        fleetRepository = new InMemoryFleetRepository();
        InMemoryReservationRepository reservationRepository = new InMemoryReservationRepository();
        rentalService = new CarRentalService(fleetRepository, reservationRepository);

        fleetRepository.setCapacity(CarType.SEDAN, 2);
        fleetRepository.setCapacity(CarType.SUV, 1);
    }

    @Test
    @DisplayName("Should successfully create a reservation when car is available")
    void shouldCreateReservationSuccessfully() {
        Reservation reservation = rentalService.reserveCar("cust-1", CarType.SEDAN, NOW, 3);

        assertThat(reservation).isNotNull();
        assertThat(reservation.getCarType()).isEqualTo(CarType.SEDAN);
        assertThat(reservation.getCustomerId()).isEqualTo("cust-1");
    }

    @Test
    @DisplayName("Should throw exception when capacity limit is reached for overlapping period")
    void shouldThrowExceptionWhenCapacityExceeded() {
        rentalService.reserveCar("cust-1", CarType.SUV, NOW, 3);

        assertThatThrownBy(() -> rentalService.reserveCar("cust-2", CarType.SUV, NOW.plusDays(1), 2))
                .isInstanceOf(CarUnavailableException.class);
    }

    @Test
    @DisplayName("Should allow consecutive non-overlapping reservations")
    void shouldAllowConsecutiveReservations() {
        rentalService.reserveCar("cust-1", CarType.SUV, NOW, 2);

        Reservation secondReservation = rentalService.reserveCar("cust-2", CarType.SUV, NOW.plusDays(2), 2);

        assertThat(secondReservation).isNotNull();
    }

    @Test
    @DisplayName("Should safely handle concurrent reservations for limited capacity")
    void shouldHandleConcurrentReservationsSafely() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();

        try {
            for (int i = 0; i < threadCount; i++) {
                String customerId = "cust-" + i;
                executor.submit(() -> reserveWhenAvailable(customerId, startLatch, successCount));
            }

            startLatch.countDown();
            executor.shutdown();

            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
            assertThat(successCount.get()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private void reserveWhenAvailable(
            String customerId,
            CountDownLatch startLatch,
            AtomicInteger successCount
    ) {
        try {
            startLatch.await();
            rentalService.reserveCar(customerId, CarType.SUV, NOW, 2);
            successCount.incrementAndGet();
        } catch (CarUnavailableException ignored) {
            // Only one SUV can be reserved for this period.
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
