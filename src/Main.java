package src;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public record ReservationResult(
    String customerName, 
    boolean success, 
    String reservationId, 
    String errorMessage
    ) {}    

    public static void main(String[] args) {
        CarRentalService service = new CarRentalService();
        List<ReservationResult> result = new ArrayList<>();
        
        for(int i = 0; i<6; i++){
            try{
                Reservation reservation= service.reserve("John", CarType.SEDAN, LocalDateTime.of(2026, 4, 6, 16, 30), 2);
                result.add(new ReservationResult("John", true, reservation.getReservationId(), null));
            } catch(IllegalArgumentException e){
                result.add(new ReservationResult("John", false, null, e.getMessage()));
            } catch(NoAvailibilityException e){
                result.add(new ReservationResult("John", false, null, e.getMessage()));
            } catch(Exception e){
                result.add(new ReservationResult("John", false, null, e.getMessage()));
            }
        }


        try {
            Reservation reservation = service.reserve("John", CarType.VAN, LocalDateTime.of(2026, 5, 6, 16, 30), 2);
            System.out.println(reservation);
            reservation.reschedule(LocalDateTime.of(2026, 5, 6, 16, 30), 5);
            System.out.println(reservation);
            result.add(new ReservationResult("John", true, reservation.getReservationId(), null));
            System.out.println();
        } catch (IllegalArgumentException e) {
            result.add(new ReservationResult("John", false, null, e.getMessage()));
        } catch (NoAvailibilityException e) {
            result.add(new ReservationResult("John", false, null, e.getMessage()));
        } catch (Exception e) {
            result.add(new ReservationResult("John", false, null, e.getMessage()));
        }


        result.forEach(System.out::println);
        System.out.println();
        service.getAllReservations().forEach(System.out::println);
    }
}
