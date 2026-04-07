package src;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Service for manage car rental reservations.
 * 
 * The three car types are Sedans, SUVs, and Vans. 
 * The service tracks the number of each and accepts reservations if there is availability.
 */
public class CarRentalService {


    public static final int DEFAULT_SEDAN_COUNT = 5;
    public static final int DEFAULT_SUV_COUNT = 3;
    public static final int DEFAULT_VAN_COUNT = 1;


    private final Map<CarType, Integer> fleetSizeByCarType;
    private final Map<CarType, Map<String, Reservation>> reservationsByCarType;
    private final Map<String, CarType> carTypeById;

    public CarRentalService(){
        this(DEFAULT_SEDAN_COUNT, DEFAULT_SUV_COUNT, DEFAULT_VAN_COUNT);
    }

    public CarRentalService(int sedanCount, int suvCount, int vanCount){
        validateCount(CarType.SEDAN, sedanCount);
        validateCount(CarType.SUV, suvCount);
        validateCount(CarType.VAN, vanCount);
        
        fleetSizeByCarType = new EnumMap<>(CarType.class);
        fleetSizeByCarType.put(CarType.SEDAN, sedanCount);
        fleetSizeByCarType.put(CarType.SUV, suvCount);
        fleetSizeByCarType.put(CarType.VAN, vanCount);

        reservationsByCarType = new EnumMap<>(CarType.class);
        for(CarType name: CarType.values()){
            reservationsByCarType.put(name, new LinkedHashMap<>());
        }

        carTypeById = new HashMap<>();
    }


    /**
     * Reserves a car of the given type for the given customer for the given number of days starting at the given date/time. 
     * 
     * @param customerName
     * @param carType
     * @param startDateTime
     * @param days
     * @return newly created Reservation
     * @throws NoAvailabilityException if there is no car of the requested type available for the given date/time.
     */
    public Reservation reserve(String customerName, CarType carType, LocalDateTime startDateTime, int days) throws NoAvailabilityException{
        //Reservation is created, only proceeds if inputs are valid
        Reservation newReservation = new Reservation(customerName, carType, startDateTime, days);
    
        //Check if there is availability
        int overlappingReservations = countOverlappingReservations(carType, startDateTime, days, "");
        int carCapacity = fleetSizeByCarType.get(carType);
        if(overlappingReservations >= carCapacity){
            throw new NoAvailabilityException(
                "No %s available on %s for %d days. All %d cars are booked".formatted(carType.getLabel(), startDateTime, days, carCapacity)
            );
        }
        
        //Valid reservation, save to maps
        reservationsByCarType.get(carType).put(newReservation.getReservationId(), newReservation);
        carTypeById.put(newReservation.getReservationId(), carType);
        
        return newReservation;
    }
    
    //GET method
    public Optional<Reservation> findById(String reservationId){
        CarType currCarType = carTypeById.get(reservationId);
        if(currCarType == null){
            return Optional.empty();
        }
        
        Reservation currentReservation = reservationsByCarType.get(currCarType).get(reservationId);
        return Optional.of(new Reservation(currentReservation));
    }

    //GET method
    public List<Reservation> getAllReservations(){
        return reservationsByCarType.values().stream()
                .flatMap(m -> m.values().stream())
                .map(Reservation::new)
                .toList();
    }

    //GET method
    public List<Reservation> getReservationsByCarType(CarType carType){
        return reservationsByCarType.get(carType).values().stream()
                .map(Reservation::new)
                .toList();
    }

    //DELETE method
    public void cancel(String reservationId) throws NoSuchElementException{
        CarType currCarType = carTypeById.get(reservationId);
        if(currCarType == null){
            throw new NoSuchElementException("No reservation found with ID: " + reservationId);
        }

        reservationsByCarType.get(currCarType).remove(reservationId);
        carTypeById.remove(reservationId);
    }
    
    //PUT method
    public Reservation update(String reservationId, LocalDateTime newStartDateTime, int newDays) throws NoAvailabilityException, NoSuchElementException{
        CarType currCarType = carTypeById.get(reservationId);
        if(currCarType == null){
            throw new NoSuchElementException("No reservation found with ID: " + reservationId);
        }

        Reservation existingReservation = reservationsByCarType.get(currCarType).get(reservationId);
        int overlappingReservations = countOverlappingReservations(currCarType, newStartDateTime, newDays, existingReservation.getReservationId());

        int carCapacity = fleetSizeByCarType.get(currCarType);
        if(overlappingReservations >= carCapacity){
            throw new NoAvailabilityException(
                "No %s available on %s for %d days. All %d cars booked".formatted(currCarType.getLabel(), newStartDateTime, newDays, carCapacity)
            );
        }
        existingReservation.reschedule(newStartDateTime, newDays);
        return existingReservation;
    }

    //Helper that counts how many reservations overlap with given time, returns an int count.
    private int countOverlappingReservations(CarType carType, LocalDateTime startDateTime, int days, String currentResId){
        int count = 0;
        for(Reservation r: reservationsByCarType.get(carType).values()){
            if(r.getReservationId().equals(currentResId)){
                continue;
            }
            if(r.overlapsWith(startDateTime, days)){
                count++;
            }
        }

        return count;
    }

    //Helper validation method used in constructor. Enforces that fleet size cannot be negative
    private void validateCount(CarType carType, int carCount){
        if(carCount < 0){
            throw new IllegalArgumentException(carType.getLabel() + " fleet size cannot be negative");
        }
    }
}
