package com.rentify.core.scheduler;

import com.rentify.core.enums.BookingStatus;
import com.rentify.core.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingStatusScheduler {

    private final BookingRepository bookingRepository;
    private final Logger logger = LoggerFactory.getLogger(BookingStatusScheduler.class);

    @Scheduled(cron = "0 0 12 * * ?")
    @Transactional
    public void updateBookingStatuses() {
        LocalDate today = LocalDate.now();
        logger.info("Running daily booking status update job for date: {}", today);
        int inProgressCount = bookingRepository.updateStatusToInProgress(
                BookingStatus.CONFIRMED,
                BookingStatus.IN_PROGRESS,
                today
        );
        if (inProgressCount > 0) {
            logger.info("Updated {} bookings to IN_PROGRESS", inProgressCount);
        }
        int completedCount = bookingRepository.updateStatusToCompleted(
                List.of(BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS),
                BookingStatus.COMPLETED,
                today
        );
        if (completedCount > 0) {
            logger.info("Updated {} bookings to COMPLETED", completedCount);
        }
    }
}