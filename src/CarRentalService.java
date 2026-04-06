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


    //default number of cars for each type
    public static final int DEFAULT_SEDAN_COUNT = 5;
    public static final int DEFAULT_SUV_COUNT = 3;
    public static final int DEFAULT_VAN_COUNT = 1;

    //keep track of car count and reservations
    //car count is type to number, car types are a hard requirement
    private final Map<CarType, Integer> fleetSizeByCarType;
    //reservation could be String(id) to Reservation. Consider if we want to seperate by cartype
    //map id to cartype, so individual reservation ids only interact with others of the same type
    //size considerations-> numTypes * numCars * numReservations(lets say 1 per day, for the next year-> 365)
    private final Map<CarType, Map<String, Reservation>> reservationsByCarType;
    private final Map<String, CarType> carTypeById;

    public CarRentalService(){
        //defaults
        this(DEFAULT_SEDAN_COUNT, DEFAULT_SUV_COUNT, DEFAULT_VAN_COUNT);
    }

    public CarRentalService(int sedanCount, int suvCount, int vanCount){
        //explicit
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



    //reserve method, needs to validate if the booking is valid-> if there are enough cars at that time slot.
    //we can either count the amount of bookings that overlap this time, or we can
    public Reservation reserve(String customerName, CarType carType, LocalDateTime startDateTime, int days) throws NoAvailibilityException{
        //validate input
        int overlappingReservations = countOverlappingReservations(carType, startDateTime, days);

        int carCapacity = fleetSizeByCarType.get(carType);
        if(overlappingReservations >= carCapacity){
            throw new NoAvailibilityException(
                "No %s available on %s for %d. All %d cars are booked".formatted(carType.getLabel(), startDateTime, days, carCapacity)
            );
            //could perhaps give the earliest available
        }
        
        Reservation reservation = new Reservation(customerName, carType, startDateTime, days);
        
        reservationsByCarType.get(carType).put(reservation.getReservationId(), reservation);
        carTypeById.put(reservation.getReservationId(), carType);
        
        return reservation;
    }
    //get method. Get all, get by type, by id, by date/time, by customer?
    public Optional<Reservation> findById(String reservationId){
        CarType currCarType = carTypeById.get(reservationId);
        if(currCarType == null){
            return Optional.empty();
        }
        return Optional.of(reservationsByCarType.get(currCarType).get(reservationId));
    }

    public List<Reservation> getAllReservations(){
        return reservationsByCarType.values().stream()
                .flatMap(m -> m.values().stream())
                .toList();
    }

    //cancel/delete reservation method
    public void cancel(String reservationId) throws NoSuchElementException{
        CarType currCarType = carTypeById.get(reservationId);
        if(currCarType == null){
            throw new NoSuchElementException("No reservation found with ID: " + reservationId);
        }

        reservationsByCarType.get(currCarType).remove(reservationId);
        carTypeById.remove(reservationId);
    }
    
    //update reservation method
    public Reservation update(String reservationId, LocalDateTime newStartDateTime, int newDays) throws NoAvailibilityException, NoSuchElementException{
        CarType currCarType = carTypeById.get(reservationId);
        if(currCarType == null){
            throw new NoSuchElementException("No reservation found with ID: " + reservationId);
        }

        Reservation existingReservation = reservationsByCarType.get(currCarType).get(reservationId);
        int overlappingReservations = countOverlappingReservations(currCarType, newStartDateTime, newDays);

        int carCapacity = fleetSizeByCarType.get(currCarType);
        if(overlappingReservations >= carCapacity){
            throw new NoAvailibilityException(
                "No %s available on %s. All %d cars booked".formatted(currCarType.getLabel(), newStartDateTime, carCapacity)
            );
        }
        existingReservation.reschedule(newStartDateTime, newDays);
        return existingReservation;
    }

    private int countOverlappingReservations(CarType carType, LocalDateTime startDateTime, int days){
        int count = 0;
        for(Reservation r: reservationsByCarType.get(carType).values()){
            if(r.overlapsWith(startDateTime, days)){
                count++;
            }
        }

        return count;
    }

    private void validateCount(CarType carType, int carCount){
        if(carCount < 0){
            throw new IllegalArgumentException(carType.getLabel() + " fleet size cannot be negative");
        }
    }
}
