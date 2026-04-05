package src;
/**
 * Represents types of cars. Includes all types available for reservation.
 */
public enum CarType {
    SEDAN, SUV, VAN;

    @Override
    public String toString(){
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
