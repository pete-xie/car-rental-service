package test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import src.CarRentalService;
import src.CarType;
import src.NoAvailabilityException;
import src.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CarRentalService}.
 *
 * Requirements verified:
 *  R1  – Successful reservation. 
 *        \System can be created with a custom fleet size.
 *        \Given a valid type, date, time and number of days, a reservation is created.
 *  R2  – Availability is enforced. 
 *        \Overlapping date windows are detected and enforced. 
 *        \Non-overlapping date windows are allowed. 
 *        \Each car type has an independent limit
 *  R3  – Reservation and Service input validation
 *  R4  – API logic 
 */

@DisplayName("Car Rental System Tests")
class CarRentalServiceTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2025, 6, 1, 10, 0);

    private CarRentalService system;

    @BeforeEach
    void setUp() {
        // Default: 5 sedans, 3 SUVs, 1 vans
        system = new CarRentalService();
    }

    // ================================================================== //
    //  Successful reservation.
    // ================================================================== //

    @Test
    @DisplayName("Successful reservation returns a non-null Reservation")
    void successfulReservationReturnsReservation() throws NoAvailabilityException {
        Reservation r = system.reserve("George", CarType.SEDAN, BASE, 3);

        assertNotNull(r);
        //Sets values
        assertEquals("George", r.getCustomerName());
        assertEquals(CarType.SEDAN, r.getCarType());
        assertEquals(BASE, r.getStartDateTime());
        assertEquals(3, r.getNumberOfDays());
        //Creates these values
        assertEquals(BASE.plusDays(3), r.getEndDateTime());
        assertNotNull(r.getReservationId());
        assertFalse(r.getReservationId().isBlank());
    }

    @Test
    @DisplayName("Successful reservation with a prior reservation")
    void successfulReservationWithPriorReservation() throws NoAvailabilityException {
        Reservation r1 = system.reserve("Will", CarType.SEDAN, BASE, 3);
        Reservation r2 = system.reserve("Will", CarType.SEDAN, BASE, 3);

        //Successful reservation
        assertNotNull(r2);
        //Seperate UUID expected
        assertNotEquals(r1.getReservationId(), r2.getReservationId());
    }

    @ParameterizedTest(name = "Reserve a {0} successfully")
    @EnumSource(CarType.class)
    @DisplayName("R1: All three car types can be reserved")
    void allCarTypesCanBeReserved(CarType type) throws NoAvailabilityException {
        Reservation r = system.reserve("Customer", type, BASE, 2);
        assertEquals(type, r.getCarType());
    }

    
    // ================================================================== //
    //  Inventory cap enforced
    // ================================================================== //

    @Test
    @DisplayName("Booking beyond SUV capacity 3 throws NoAvailabilityException")
    void exceedingSuvCapacityThrows() throws NoAvailabilityException {
        //Fill all SUVs
        system.reserve("C1", CarType.SUV, BASE, 2);
        system.reserve("C2", CarType.SUV, BASE, 2);
        system.reserve("C3", CarType.SUV, BASE, 2);


        //Fourth booking overlapping the same window must fail
        assertThrows(NoAvailabilityException.class, () ->
                system.reserve("C3", CarType.SUV, BASE, 2));
    }

    @Test
    @DisplayName("Booking beyond Van capacity 1 throws NoAvailabilityException")
    void exceedingVanCapacityThrows() throws NoAvailabilityException {
        //Fill all Vans
        system.reserve("V1", CarType.VAN, BASE, 5);

        // Second booking overlapping the same window must fail
        assertThrows(NoAvailabilityException.class, () ->
                system.reserve("V2", CarType.VAN, BASE, 5));
    }

    @Test
    @DisplayName("Booking beyond Sedan capacity 5 throws NoAvailabilityException")
    void exceedingSedanCapacityThrows() throws NoAvailabilityException {
        //Fill all Sedans
        system.reserve("S1", CarType.SEDAN, BASE, 1);
        system.reserve("S2", CarType.SEDAN, BASE, 1);
        system.reserve("S3", CarType.SEDAN, BASE, 1);
        system.reserve("S4", CarType.SEDAN, BASE, 1);
        system.reserve("S5", CarType.SEDAN, BASE, 1);


        //Sixth booking overlapping the same window must fail
        assertThrows(NoAvailabilityException.class, () ->
                system.reserve("S6", CarType.SEDAN, BASE, 1));
    }

    @Test
    @DisplayName("Custom fleet of 1 sedan allows only 1 concurrent booking")
    void customFleetOfOneSedanAllowsOneBooking() throws NoAvailabilityException {
        CarRentalService small = new CarRentalService(1, 1, 1);
        small.reserve("Anna", CarType.SEDAN, BASE, 3);

        assertThrows(NoAvailabilityException.class, () ->
                small.reserve("Bob", CarType.SEDAN, BASE, 3));
    }

    @Test
    @DisplayName("Fleet of size 0 rejects all reservations for that type")
    void fleetOfZeroRejectsAll() {
        CarRentalService noVans = new CarRentalService(3, 2, 0);
        assertThrows(NoAvailabilityException.class, () ->
                noVans.reserve("Billy", CarType.VAN, BASE, 1));
    }

    // ================================================================== //
    //  Overlapping date windows are detected and enforced.
    // ================================================================== //

    @Test
    @DisplayName("Fully overlapping window is rejected when at capacity")
    void fullyOverlappingWindowRejected() throws NoAvailabilityException {
        //Testing Vans at capacity
        system.reserve("V1", CarType.VAN, BASE, 5);

        //Window (BASE+1, BASE+3) is fully inside the existing window (BASE, BASE+5)
        assertThrows(NoAvailabilityException.class, () ->
                system.reserve("V2", CarType.VAN, BASE.plusDays(1), 2));
    }

    @Test
    @DisplayName("Partially overlapping window is rejected when at capacity")
    void partiallyOverlappingWindowRejected() throws NoAvailabilityException {
        //Testing SUVs at capacity
        system.reserve("C1", CarType.SUV, BASE, 4);
        system.reserve("C2", CarType.SUV, BASE, 4);
        system.reserve("C3", CarType.SUV, BASE, 4);

        //Window (BASE+3, BASE+6) is partially inside an existing window (BASE, BASE+4)
        assertThrows(NoAvailabilityException.class, () ->
                system.reserve("C4", CarType.SUV, BASE.plusDays(3), 3));
    }
    

    @Test
    @DisplayName("Adjacent reservations do NOT overlap")
    void adjacentWindowsDoNotOverlap() throws NoAvailabilityException {
        //Testing Sedans at capacity. Window is (BASE+3, BASE+6)
        system.reserve("S1", CarType.SEDAN, BASE.plusDays(3), 3);
        system.reserve("S2", CarType.SEDAN, BASE.plusDays(3), 3);
        system.reserve("S3", CarType.SEDAN, BASE.plusDays(3), 3);
        system.reserve("S4", CarType.SEDAN, BASE.plusDays(3), 3);
        system.reserve("S5", CarType.SEDAN, BASE.plusDays(3), 3);

        //New reservation starts exactly at BASE+6
        assertDoesNotThrow(() ->
                system.reserve("V2", CarType.VAN, BASE.plusDays(6), 3));
        //New reservation ends exactly at BASE+3
        assertDoesNotThrow(() ->
                system.reserve("V2", CarType.VAN, BASE, 3));
    }

    // ================================================================== //
    //  Independent inventories per type
    // ================================================================== //

    @Test
    @DisplayName("Filling Sedan inventory does not affect SUV or Van availability")
    void sedanFullDoesNotBlockSuvOrVan() throws NoAvailabilityException {
        //Fill all 5 Sedans
        system.reserve("S1", CarType.SEDAN, BASE, 3);
        system.reserve("S2", CarType.SEDAN, BASE, 3);
        system.reserve("S3", CarType.SEDAN, BASE, 3);
        system.reserve("S4", CarType.SEDAN, BASE, 3);
        system.reserve("S5", CarType.SEDAN, BASE, 3);

        //SUV should still be bookable
        assertDoesNotThrow(() -> system.reserve("C1", CarType.SUV, BASE, 3));
        //Van should still be bookable
        assertDoesNotThrow(() -> system.reserve("V1", CarType.VAN, BASE, 3));
    }

    @Test
    @DisplayName("Mixed-type reservations all fit within separate capacities")
    void mixedTypeReservationsAllFit() {
        assertDoesNotThrow(() -> {
            //Fill all sedans
            system.reserve("S1", CarType.SEDAN, BASE, 2);
            system.reserve("S2", CarType.SEDAN, BASE, 2);
            system.reserve("S3", CarType.SEDAN, BASE, 2);
            system.reserve("S4", CarType.SEDAN, BASE, 2);
            system.reserve("S5", CarType.SEDAN, BASE, 2);

            //Fill all SUVs
            system.reserve("U1", CarType.SUV, BASE, 2);
            system.reserve("U2", CarType.SUV, BASE, 2);
            system.reserve("U3", CarType.SUV, BASE, 2);

            //Fill all vans
            system.reserve("V1", CarType.VAN, BASE, 2);
        });

        //All reservations should be present and distinct
        assertEquals(CarRentalService.DEFAULT_SEDAN_COUNT, system.getReservationsByCarType(CarType.SEDAN).size());
        assertEquals(CarRentalService.DEFAULT_SUV_COUNT, system.getReservationsByCarType(CarType.SUV).size());
        assertEquals(CarRentalService.DEFAULT_VAN_COUNT, system.getReservationsByCarType(CarType.VAN).size());
    }

    // ================================================================== //
    //  Reservation and Service Input validation
    // ================================================================== //


    @Test
    @DisplayName("Negative fleet size throws IllegalArgumentException")
    void negativeFleetSizeThrows() {
        assertThrows(IllegalArgumentException.class, () -> 
                new CarRentalService(-1, 2, 2));
    }

    @Test
    @DisplayName("Max fleet size is allowed")
    void maxFleetSizeValid() {
        assertDoesNotThrow(() -> 
                new CarRentalService(CarRentalService.MAX_CAR_COUNT, 2, 2));
    }

    @Test
    @DisplayName("Greater than max fleet size throws IllegalArgumentException")
    void greaterThanMaxFleetSizeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new CarRentalService(CarRentalService.MAX_CAR_COUNT+1, 2, 2));
    }

    @Test
    @DisplayName("Blank customer name throws IllegalArgumentException")
    void blankCustomerNameThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                system.reserve("  ", CarType.SEDAN, BASE, 1));
    }

    @Test
    @DisplayName("Null customer name throws IllegalArgumentException")
    void nullCustomerNameThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                system.reserve(null, CarType.SEDAN, BASE, 1));
    }

    @Test
    @DisplayName("Null car type throws IllegalArgumentException")
    void nullCarTypeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                system.reserve("Maya", null, BASE, 1));
    }

    @Test
    @DisplayName("Null start date/time throws IllegalArgumentException")
    void nullStartDateTimeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                system.reserve("Sam", CarType.SEDAN, null, 1));
    }

    @Test
    @DisplayName("Zero days throws IllegalArgumentException")
    void zeroDaysThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                system.reserve("Grace", CarType.SEDAN, BASE, 0));
    }

    @Test
    @DisplayName("Negative days throws IllegalArgumentException")
    void negativeDaysThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                system.reserve("Taylor", CarType.SEDAN, BASE, -3));
    }

    @Test
    @DisplayName("Minimum valid days is 1 day")
    void oneDayReservationIsValid() {
        assertDoesNotThrow(() -> system.reserve("Gene", CarType.VAN, BASE, 1));
    }

    @Test
    @DisplayName("Maximum valid days is allowed")
    void maxDayReservationIsValid() {
        assertDoesNotThrow(() -> system.reserve("Naomi", CarType.VAN, BASE, Reservation.MAX_RESERVATION));
    }

    @Test
    @DisplayName("Greater than maximum valid days is not allowed")
    void greaterThanMaxDayReservationThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                system.reserve("Jessie", CarType.SEDAN, BASE, Reservation.MAX_RESERVATION+1));
    }

    // ================================================================== //
    //  API
    // ================================================================== //


    @Test
    @DisplayName("Null passed to cancel throws NullPointerException")
    void cancelWithNullThrows() {
        assertThrows(NullPointerException.class, () ->
                system.cancel(null));
    }

    @Test
    @DisplayName("Cancelling a reservation frees its slot")
    void cancellationFreesSlot() throws NoAvailabilityException {
        Reservation r = system.reserve("V1", CarType.VAN, BASE, 3);

        //Full capacity
        assertThrows(NoAvailabilityException.class, () ->
                system.reserve("V2", CarType.VAN, BASE, 3));

        //Cancel one
        system.cancel(r.getReservationId());

        //Now should succeed
        assertDoesNotThrow(() -> system.reserve("V3", CarType.VAN, BASE, 3));
    }

    @Test
    @DisplayName("Cancelling a non-existent ID throws NoAvailabilityException")
    void cancelNonExistentIdThrows() {
        assertThrows(NoSuchElementException.class, () ->
                system.cancel("does-not-exist"));
    }

    @Test
    @DisplayName("Cancelled reservation is removed from the system")
    void cancelledReservationRemovedFromList() throws NoAvailabilityException {
        Reservation r = system.reserve("Mario", CarType.SEDAN, BASE, 2);
        system.cancel(r.getReservationId());

        assertTrue(system.getAllReservations().isEmpty());
    }

    @Test
    @DisplayName("getAllReservations returns all active reservations")
    void getAllReservationsReturnsAll() throws NoAvailabilityException {
        system.reserve("Alice", CarType.SEDAN, BASE, 1);
        system.reserve("Bob",   CarType.SUV,   BASE, 2);
        system.reserve("Carol", CarType.VAN,   BASE, 3);

        assertEquals(3, system.getAllReservations().size());
    }

    @Test
    @DisplayName("getReservationsByType throws NullPointerException when type is null")
    void getReservationsByTypeThrows() throws NoAvailabilityException {
        assertThrows(NullPointerException.class, () ->
                system.getReservationsByCarType(null));
    }
    
    @Test
    @DisplayName("getReservationsByType filters correctly")
    void getReservationsByTypeFilters() throws NoAvailabilityException {
        system.reserve("Alice", CarType.SEDAN, BASE, 1);
        system.reserve("Bob",   CarType.SUV,   BASE, 2);
        system.reserve("Carol", CarType.SEDAN, BASE, 1);

        List<Reservation> sedans = system.getReservationsByCarType(CarType.SEDAN);
        assertEquals(2, sedans.size());
        assertTrue(sedans.stream().allMatch(r -> r.getCarType() == CarType.SEDAN));
    }
    
    @Test
    @DisplayName("findById throws NullPointerException when ID is null")
    void findByIdThrows() throws NoAvailabilityException {
        assertThrows(NullPointerException.class, () ->
                system.findById(null));
    }

    @Test
    @DisplayName("findById returns correct reservation and is not the same object")
    void findByIdReturnsCorrectReservation() throws NoAvailabilityException {
        Reservation r = system.reserve("Kevin", CarType.VAN, BASE, 2);
        Optional<Reservation> found = system.findById(r.getReservationId());

        assertTrue(found.isPresent());
        assertEquals(r.getReservationId(), found.get().getReservationId());
        assertFalse(r == found.get());
    }

    @Test
    @DisplayName("findById returns empty for unknown ID")
    void findByIdReturnsEmptyForUnknownId() {
        assertTrue(system.findById("unknown-id").isEmpty());
    }


    @Test
    @DisplayName("Update throws NullPointerException when ID is null")
    void updateWithNullIdThrows() {
        assertThrows(NullPointerException.class, () ->
                system.update(null, BASE, 2));
    }

    @Test
    @DisplayName("Update throws NullPointerException when ID is invalid")
    void updateWithInvalidIdThrows() {
        assertThrows(NoSuchElementException.class, () ->
                system.update("unknown-id", BASE, 2));
    }

    @Test
    @DisplayName("Update throws IllegalArgumentException when Date is null")
    void updateWithNullDateThrows() throws NoAvailabilityException {
        Reservation r = system.reserve("Carl", CarType.VAN, BASE, 2);
        assertThrows(IllegalArgumentException.class, () ->
                system.update(r.getReservationId(), null, 2));
    }

    @Test
    @DisplayName("Update throws IllegalArgumentException when days is invalid")
    void updateWithInvalidDaysThrows() throws NoAvailabilityException {
        Reservation r = system.reserve("Emma", CarType.VAN, BASE, 2);
        assertThrows(IllegalArgumentException.class, () ->
                system.update(r.getReservationId(), BASE, -1));
        assertThrows(IllegalArgumentException.class, () ->
                system.update(r.getReservationId(), BASE, Reservation.MAX_RESERVATION + 1));
    }

    @Test
    @DisplayName("Update correctly updates reservation")
    void updateSuccessfullyChanges() throws NoAvailabilityException {
        Reservation r = system.reserve("Larry", CarType.VAN, BASE, 2);
        int newDays = 5;

        //Prior to reschedule
        assertNotEquals(r.getNumberOfDays(), newDays);
        r.reschedule(BASE, newDays);

        //After reschedule
        assertEquals(r.getNumberOfDays(), newDays);
    }

    @Test
    @DisplayName("Update throws NoAvailabilityException when no cars available")
    void updateThrows() throws NoAvailabilityException {
        system.reserve("V1", CarType.VAN, BASE, 2);

        //Non overlapping reservation
        Reservation r = system.reserve("V2", CarType.VAN, BASE.plusDays(2), 2);

        //Reschedule to overlap with V1
        assertThrows(NoAvailabilityException.class, () ->
                system.update(r.getReservationId(), BASE, 2));
    }

    @Test
    @DisplayName("Can't reschedule using GET methods")
    void originalReservationNotReturned() throws NoAvailabilityException {
        Reservation r = system.reserve("Jay", CarType.VAN, BASE, 2);
        Optional<Reservation> found = system.findById(r.getReservationId());
        int newDays = 5;

        //Reschedule reservation from GET
        found.get().reschedule(BASE, newDays);

        //Check if orignal was updated
        assertNotEquals(r.getNumberOfDays(), found.get().getNumberOfDays());
    }

}