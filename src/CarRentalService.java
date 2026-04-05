package src;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Service for manage car rental reservations.
 * 
 * The three car types are Sedans, SUVs, and Vans. 
 * The service tracks the number of each and accepts reservations if there is availability.
 */
public class CarRentalService {

    //keep track of car count and reservations
    //car count is type to number, car types are a hard requirement
    private final Map<CarType, Integer> capacityByCarType = Collections.synchronizedMap(new EnumMap<>(CarType.class));
    //reservation could be String(id) to Reservation. Consider if we want to seperate by cartype
    //map id to cartype, so individual reservation ids only interact with others of the same type
    //size considerations-> numTypes * numCars * numReservations(lets say 1 per day, for the next year-> 365)
    private final Map<CarType, Map<String, Reservation>> reservationsByCarType;
    private final Map<String, CarType> carTypeById;

    public CarRentalService(){
        //defaults
    }

    public CarRentalService(int sedanCount, int suvCount, int vanCount){
        //explicit
    }



    //reserve method, needs to validate if the booking is valid-> if there are enough cars at that time slot.
    //we can either count the amount of bookings that overlap this time, or we can

    //get method. Get all, get by type, by id, by date/time, by customer?

    //cancel reservation method

    //update reservation method

    //consider fulfilled reservations(if customer showed up)-> would tie into payment. However, not an explcit requirement
}
