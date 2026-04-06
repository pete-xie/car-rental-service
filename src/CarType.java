package src;
/**
 * Represents types of cars. Includes all types available for reservation.
 */
public enum CarType {
    SEDAN("Sedan"), 
    SUV("SUV"), 
    VAN("Van");

    private final String label;

    private CarType(String label){
        this.label = label;
    }

    public String getLabel(){
        return label;
    }
}
