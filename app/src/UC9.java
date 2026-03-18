import java.util.*;

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
            throw new InvalidBookingException("Inventory cannot be decremented below zero for room type: " + roomType);
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

    void addRequest(BookingRequest request) {
        queue.offer(request);
    }

    void processRequests() {
        while (!queue.isEmpty()) {
            BookingRequest request = queue.poll();
            try {
                processSingleRequest(request);
            } catch (InvalidBookingException e) {
                System.out.println("Booking failed for " + request.requestId + ": " + e.getMessage());
            }
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

    boolean isReservationValid(String requestId) {
        return confirmedReservations.contains(requestId);
    }

    List<Booking> getBookingHistory() {
        return Collections.unmodifiableList(bookingHistory);
    }
}

class BookingReportService {
    private final BookingService bookingService;

    BookingReportService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    void printAllBookings() {
        List<Booking> history = bookingService.getBookingHistory();
        System.out.println("Booking History:");
        for (Booking b : history) {
            System.out.println("Reservation: " + b.reservationId + ", Room Type: " + b.roomType + ", Room ID: " + b.roomId);
        }
    }
}

public class UC9 {
    public static void main(String[] args) {
        Map<String, Integer> initialInventory = new HashMap<>();
        initialInventory.put("DELUXE", 2);
        initialInventory.put("STANDARD", 1);

        InventoryService inventoryService = new InventoryService(initialInventory);
        BookingService bookingService = new BookingService(inventoryService);

        bookingService.addRequest(new BookingRequest("REQ1", "DELUXE"));
        bookingService.addRequest(new BookingRequest("REQ2", "STANDARD"));
        bookingService.addRequest(new BookingRequest("REQ3", "INVALID_TYPE"));
        bookingService.addRequest(new BookingRequest("REQ4", "STANDARD"));

        bookingService.processRequests();

        BookingReportService reportService = new BookingReportService(bookingService);
        reportService.printAllBookings();
    }
}