package src;

/**
 * Throws exception to be handled when a reservation cannot be fulfilled, due to no cars being available.
 */
public class NoAvailabilityException extends Exception {

    public NoAvailabilityException(String message){
        super(message);
    }
}