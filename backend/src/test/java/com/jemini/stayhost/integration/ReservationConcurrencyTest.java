package com.jemini.stayhost.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.jemini.stayhost.support.IntegrationTestBase;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReservationConcurrencyTest extends IntegrationTestBase {

    private TestPropertyData propertyData;
    private String userToken;

    @BeforeAll
    void setUp() {
        propertyData = setupPropertyWithInventory("concurrency", 10);
        registerUser("concurrent@test.com", "password1234");
        userToken = loginUser("concurrent@test.com", "password1234");
    }

    @Test
    @Order(1)
    @DisplayName("재고 1개에 동시 예약시 1명만 성공")
    void 재고_1개에_동시_예약시_1명만_성공() throws InterruptedException {
        final LocalDate checkIn = LocalDate.now().plusDays(1);
        final LocalDate checkOut = checkIn.plusDays(1);

        // 재고를 1로 재설정
        setInventory(propertyData.partnerToken(), propertyData.roomTypeId(), checkIn, checkOut, 1);

        final int threadCount = 50;
        final CountDownLatch readyLatch = new CountDownLatch(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failCount = new AtomicInteger(0);

        final List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                final ResponseEntity<String> response = requestCreateReservation(
                        userToken, propertyData.propertyId(), propertyData.roomTypeId(), checkIn, checkOut, 2);

                if (response.getStatusCode().is2xxSuccessful()) {
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        for (final Future<?> future : futures) {
            try {
                future.get(30, TimeUnit.SECONDS);
            } catch (final ExecutionException | TimeoutException e) {
                failCount.incrementAndGet();
            }
        }
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
    }

    @Test
    @Order(2)
    @DisplayName("재고 10개에 동시 예약시 10명만 성공")
    void 재고_10개에_동시_예약시_10명만_성공() throws InterruptedException {
        final LocalDate checkIn = LocalDate.now().plusDays(3);
        final LocalDate checkOut = checkIn.plusDays(1);

        // 재고를 10으로 재설정
        setInventory(propertyData.partnerToken(), propertyData.roomTypeId(), checkIn, checkOut, 10);

        final int threadCount = 50;
        final CountDownLatch readyLatch = new CountDownLatch(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failCount = new AtomicInteger(0);

        final List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                final ResponseEntity<String> response = requestCreateReservation(
                        userToken, propertyData.propertyId(), propertyData.roomTypeId(), checkIn, checkOut, 2);

                if (response.getStatusCode().is2xxSuccessful()) {
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        for (final Future<?> future : futures) {
            try {
                future.get(30, TimeUnit.SECONDS);
            } catch (final ExecutionException | TimeoutException e) {
                failCount.incrementAndGet();
            }
        }
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failCount.get()).isEqualTo(threadCount - 10);
    }

    @Test
    @Order(3)
    @DisplayName("동일 예약 동시 취소시 1번만 성공")
    void 동일_예약_동시_취소시_1번만_성공() throws InterruptedException {
        final LocalDate checkIn = LocalDate.now().plusDays(5);
        final LocalDate checkOut = checkIn.plusDays(1);

        // 재고 설정 및 예약 생성
        setInventory(propertyData.partnerToken(), propertyData.roomTypeId(), checkIn, checkOut, 1);
        final ResponseEntity<String> created = requestCreateReservation(
                userToken, propertyData.propertyId(), propertyData.roomTypeId(), checkIn, checkOut, 2);
        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        final Long reservationId = getData(created).get("id").asLong();

        final int threadCount = 20;
        final CountDownLatch readyLatch = new CountDownLatch(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failCount = new AtomicInteger(0);

        final List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                final ResponseEntity<String> response = requestCancelReservation(userToken, reservationId, "동시 취소");

                if (response.getStatusCode().is2xxSuccessful()) {
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        for (final Future<?> future : futures) {
            try {
                future.get(30, TimeUnit.SECONDS);
            } catch (final ExecutionException | TimeoutException e) {
                failCount.incrementAndGet();
            }
        }
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
    }
}
