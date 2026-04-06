package src;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single car reservation.
 */
public class Reservation {
    

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int MAX_RESERVATION = 365;

    //Requirements are: CarType, Date and Time, and Number of Days
    //consider hours each day vs reserved for full days. Rentals are usually full days
    //consider if we want to modify reservations. In memory, it is probably best if we allow it. We don't need to record event history
    private final String reservationId;
    private final String customerName;
    private final CarType carType;
    private LocalDateTime startDateTime;
    private int numberOfDays;
    private LocalDateTime endDateTime;

    public Reservation(String customerName, CarType carType, LocalDateTime startDateTime, int numberOfDays){
        if(customerName == null || customerName.isBlank()){
            throw new IllegalArgumentException("Customer name cannot be blank.");
        }
        if(carType == null){
            throw new IllegalArgumentException("Car type cannot be null.");
        }
        if(startDateTime == null){
            throw new IllegalArgumentException("Start date time object cannot be null.");
        }
        validateDuration(numberOfDays);
        
        this.customerName = customerName;
        this.carType = carType;
        this.numberOfDays = numberOfDays;
        this.startDateTime = startDateTime;
        this.endDateTime = startDateTime.plusDays(numberOfDays);
        this.reservationId = UUID.randomUUID().toString();
    }

    //getters
    public String getReservationId(){ return reservationId; }
    public String getCustomerName(){ return customerName; }
    public CarType getCarType(){ return carType; }
    public int getNumberOfDays(){ return numberOfDays; }
    public LocalDateTime getStartTime(){ return startDateTime; }
    public LocalDateTime getEndTime(){ return endDateTime; }

    /**
     * Handles rescheduling a reservation to ensure endDateTime is set with startDateTime. 
     * Ensures endDateTime can never occur before startDateTime
     * 
     * @param newStartDateTime
     * @param newDays must be valid, between 1 and MAX_RESERVATION
     */
    public void reschedule(LocalDateTime newStartDateTime, int newDays){
        validateDuration(newDays);
        this.startDateTime = newStartDateTime;
        this.numberOfDays = newDays;
        this.endDateTime = newStartDateTime.plusDays(newDays);
    }

    public boolean overlapsWith(LocalDateTime start, int days){
        LocalDateTime end = start.plusDays(days);
        return this.startDateTime.isBefore(end) && this.endDateTime.isAfter(start);
    }

    private void validateDuration(int days){
        if(days < 1 || days > MAX_RESERVATION){
            throw new IllegalArgumentException("Number of days '%d' is outside of valid range (1-%d)".formatted(days, MAX_RESERVATION));
        }
    }

    @Override
    public String toString(){
        return String.format(
            "Reservation: {id=%s, customer=%s, car_type=%s, days=%d, start=%s, end=%s",
            reservationId, customerName, carType, numberOfDays, startDateTime.format(FORMATTER), endDateTime.format(FORMATTER)
        );
    }

    @Override
    public boolean equals(Object o){
        if(this == o){            
            return true;
        }
        if(!(o instanceof Reservation r)){
            return false;
        }
        return Objects.equals(reservationId, r.reservationId);
    }

    @Override 
    public int hashCode(){
        return Objects.hash(reservationId);
    }
}
