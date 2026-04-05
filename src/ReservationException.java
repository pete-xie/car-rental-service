package src;

/**
 * Throws exception to be handled when a reservation cannot be fulfilled.
 */
public class ReservationException extends Exception {

    public ReservationException(String message){
        super(message);
    }
}