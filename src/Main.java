package src;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main {
    public record ReservationResult(
    String customerName, 
    boolean success, 
    String reservationId, 
    String errorMessage
    ) {}    

    public static void main(String[] args) {
        try{
            CarRentalService service = new CarRentalService(1, 2, CarRentalService.MAX_CAR_COUNT);

            Reservation r = service.reserve("Jay", CarType.VAN, LocalDateTime.of(2025, 6, 1, 10, 0), 2);
            Optional<Reservation> found = service.findById(r.getReservationId());
            int newDays = 5;

            //Reschedule reservation from GET
            found.get().reschedule(LocalDateTime.of(2025, 6, 1, 10, 0), newDays);

            System.out.println(found.get());
            System.out.println(r);
            System.out.println();
        } catch(IllegalArgumentException e){
            System.out.println(e.getMessage());
        } catch(NoAvailabilityException e){
            System.out.println(e.getMessage());
        }

        CarRentalService service = new CarRentalService();
        List<ReservationResult> result = new ArrayList<>();
        
        for(int i = 0; i<6; i++){
            try{
                Reservation reservation= service.reserve("John", CarType.SEDAN, LocalDateTime.of(2026, 4, 6, 16, 30), 2);
                result.add(new ReservationResult("John", true, reservation.getReservationId(), null));
            } catch(IllegalArgumentException e){
                result.add(new ReservationResult("John", false, null, e.getMessage()));
            } catch(NoAvailabilityException e){
                result.add(new ReservationResult("John", false, null, e.getMessage()));
            } catch(Exception e){
                result.add(new ReservationResult("John", false, null, e.getMessage()));
            }
        }


        try {
            Reservation reservation = service.reserve("Paul", CarType.VAN, LocalDateTime.of(2026, 5, 6, 16, 30), 2);
            System.out.println(reservation);
            reservation.reschedule(LocalDateTime.of(2026, 5, 6, 16, 30), 5);
            System.out.println(reservation);
            result.add(new ReservationResult("John", true, reservation.getReservationId(), null));
            System.out.println();
        } catch (IllegalArgumentException e) {
            result.add(new ReservationResult("John", false, null, e.getMessage()));
        } catch (NoAvailabilityException e) {
            result.add(new ReservationResult("John", false, null, e.getMessage()));
        } catch (Exception e) {
            result.add(new ReservationResult("John", false, null, e.getMessage()));
        }


        result.forEach(System.out::println);
        System.out.println();
        service.getAllReservations().forEach(System.out::println);
    }
}
