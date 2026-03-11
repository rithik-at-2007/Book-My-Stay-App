import java.util.LinkedList;
import java.util.Queue;

class Reservation {
    private String guestName;
    private String roomType;
    private int requestedRooms;

    public Reservation(String guestName, String roomType, int requestedRooms) {
        this.guestName = guestName;
        this.roomType = roomType;
        this.requestedRooms = requestedRooms;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    public int getRequestedRooms() {
        return requestedRooms;
    }
}

class BookingRequestQueue {
    private Queue<Reservation> queue;

    public BookingRequestQueue() {
        this.queue = new LinkedList<>();
    }

    public void addBookingRequest(Reservation reservation) {
        queue.add(reservation);
    }

    public Reservation processNextRequest() {
        return queue.poll();
    }

    public boolean hasRequests() {
        return !queue.isEmpty();
    }
}

class BookingSystem {
    private BookingRequestQueue requestQueue;

    public BookingSystem() {
        this.requestQueue = new BookingRequestQueue();
    }

    public void submitBookingRequest(Reservation reservation) {
        requestQueue.addBookingRequest(reservation);
        System.out.println("Booking request submitted by " + reservation.getGuestName() + " for " + reservation.getRoomType() + " room.");
    }

    public void processBookingRequests() {
        while (requestQueue.hasRequests()) {
            Reservation reservation = requestQueue.processNextRequest();
            System.out.println("Processing booking request for " + reservation.getGuestName() + " to book " + reservation.getRequestedRooms() + " " + reservation.getRoomType() + " room(s).");
        }
    }
}

public class uc5 {
    public static void main(String[] args) {
        BookingSystem bookingSystem = new BookingSystem();

        Reservation reservation1 = new Reservation("John Doe", "Deluxe", 1);
        Reservation reservation2 = new Reservation("Jane Smith", "Suite", 2);
        Reservation reservation3 = new Reservation("Tom Brown", "Standard", 1);

        bookingSystem.submitBookingRequest(reservation1);
        bookingSystem.submitBookingRequest(reservation2);
        bookingSystem.submitBookingRequest(reservation3);

        bookingSystem.processBookingRequests();
    }
}