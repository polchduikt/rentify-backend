package com.rentify.core.scheduler;

import com.rentify.core.enums.BookingStatus;
import com.rentify.core.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingStatusScheduler {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Kiev");

    private final BookingRepository bookingRepository;
    private final Logger logger = LoggerFactory.getLogger(BookingStatusScheduler.class);

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncBookingStatusesOnStartup() {
        int normalizedVersions = bookingRepository.normalizeNullVersions();
        if (normalizedVersions > 0) {
            logger.info("Normalized NULL booking versions to 0 for {} records", normalizedVersions);
        }
        updateStatuses("startup");
    }

    @Scheduled(cron = "0 0 * * * *", zone = "Europe/Kiev")
    @Transactional
    public void updateBookingStatuses() {
        updateStatuses("schedule");
    }

    private void updateStatuses(String trigger) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        logger.info("Running booking status update job [{}] for date: {}", trigger, today);
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
