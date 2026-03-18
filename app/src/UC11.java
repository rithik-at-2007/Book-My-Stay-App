import java.util.*;
import java.util.concurrent.*;

class InvalidBookingException extends Exception {
    InvalidBookingException(String message) {
        super(message);
    }
}

class BookingRequest {
    String requestId;
    String roomType;

    BookingRequest(String requestId, String roomType) {
        this.requestId = requestId;
        this.roomType = roomType;
    }
}

class InventoryService {
    private final Map<String, Integer> inventory = new HashMap<>();

    InventoryService(Map<String, Integer> initialInventory) {
        inventory.putAll(initialInventory);
    }

    synchronized boolean isAvailable(String roomType) {
        return inventory.getOrDefault(roomType, 0) > 0;
    }

    synchronized void decrement(String roomType) throws InvalidBookingException {
        int count = inventory.getOrDefault(roomType, 0);
        if (count <= 0) {
            throw new InvalidBookingException("No available rooms for type: " + roomType);
        }
        inventory.put(roomType, count - 1);
    }

    synchronized void validateRoomType(String roomType) throws InvalidBookingException {
        if (!inventory.containsKey(roomType)) {
            throw new InvalidBookingException("Invalid room type: " + roomType);
        }
    }
}

class Booking {
    String reservationId;
    String roomId;
    String roomType;

    Booking(String reservationId, String roomId, String roomType) {
        this.reservationId = reservationId;
        this.roomId = roomId;
        this.roomType = roomType;
    }
}

class BookingService {
    private final Queue<BookingRequest> queue = new LinkedList<>();
    private final InventoryService inventoryService;
    private final Map<String, Set<String>> allocatedRooms = new HashMap<>();
    private final Set<String> allRoomIds = new HashSet<>();
    private final Set<String> confirmedReservations = new HashSet<>();
    private final List<Booking> bookingHistory = new ArrayList<>();

    BookingService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    synchronized void addRequest(BookingRequest request) {
        queue.offer(request);
    }

    void processRequestsConcurrently(int numberOfThreads) {
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                while (true) {
                    BookingRequest request;
                    synchronized (this) {
                        request = queue.poll();
                    }
                    if (request == null) break;

                    try {
                        processSingleRequest(request);
                    } catch (InvalidBookingException e) {
                        System.out.println("Booking failed for " + request.requestId + ": " + e.getMessage());
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.out.println("Booking threads interrupted");
        }
    }

    private synchronized void processSingleRequest(BookingRequest request) throws InvalidBookingException {
        inventoryService.validateRoomType(request.roomType);

        if (!inventoryService.isAvailable(request.roomType)) {
            throw new InvalidBookingException("No available rooms for type: " + request.roomType);
        }

        String roomId = generateUniqueRoomId();

        allocatedRooms
                .computeIfAbsent(request.roomType, k -> new HashSet<>())
                .add(roomId);

        allRoomIds.add(roomId);
        confirmedReservations.add(request.requestId);

        inventoryService.decrement(request.roomType);

        Booking booking = new Booking(request.requestId, roomId, request.roomType);
        bookingHistory.add(booking);

        confirmReservation(booking);
    }

    private String generateUniqueRoomId() {
        String id;
        do {
            id = UUID.randomUUID().toString();
        } while (allRoomIds.contains(id));
        return id;
    }

    private void confirmReservation(Booking booking) {
        System.out.println("Confirmed: " + booking.reservationId + " -> Room " + booking.roomId);
    }

    List<Booking> getBookingHistory() {
        return Collections.unmodifiableList(bookingHistory);
    }
}

public class UC11 {
    public static void main(String[] args) {
        Map<String, Integer> initialInventory = new HashMap<>();
        initialInventory.put("DELUXE", 2);
        initialInventory.put("STANDARD", 3);

        InventoryService inventoryService = new InventoryService(initialInventory);
        BookingService bookingService = new BookingService(inventoryService);

        // Simulate concurrent booking requests
        List<BookingRequest> requests = List.of(
                new BookingRequest("REQ1", "DELUXE"),
                new BookingRequest("REQ2", "STANDARD"),
                new BookingRequest("REQ3", "DELUXE"),
                new BookingRequest("REQ4", "STANDARD"),
                new BookingRequest("REQ5", "STANDARD")
        );

        for (BookingRequest r : requests) {
            bookingService.addRequest(r);
        }

        bookingService.processRequestsConcurrently(3);

        System.out.println("\nFinal Booking History:");
        for (Booking b : bookingService.getBookingHistory()) {
            System.out.println("Reservation: " + b.reservationId + ", Room Type: " + b.roomType + ", Room ID: " + b.roomId);
        }
    }
}