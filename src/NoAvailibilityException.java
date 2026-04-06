package src;

/**
 * Throws exception to be handled when a reservation cannot be fulfilled.
 */
public class NoAvailibilityException extends Exception {

    //consider adding more meaningful exceptions
    public NoAvailibilityException(String message){
        super(message);
    }
}