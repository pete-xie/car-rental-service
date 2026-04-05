package src;

import java.time.LocalDateTime;

/**
 * Represents a single car reservation.
 */
public class Reservation {
    
    //Requirements are: CarType, Date and Time, and Number of Days
    //consider hours each day vs reserved for full days. Rentals are usually full days
    //consider if we want to modify reservations. In memory, it is probably best if we allow it. We don't need to record event history
    private String reservationID;
    private String customerName;
    private String carType;
    private int numberOfDays;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
