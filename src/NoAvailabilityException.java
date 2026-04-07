package src;

/**
 * Throws exception to be handled when a reservation based request cannot be fulfilled.
 */
public class NoAvailabilityException extends Exception {

    public NoAvailabilityException(String message){
        super(message);
    }
}