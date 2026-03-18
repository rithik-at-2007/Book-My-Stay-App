import java.util.*;

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

    synchronized void decrement(String roomType) {
        inventory.put(roomType, inventory.get(roomType) - 1);
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
            processSingleRequest(request);
        }
    }

    private synchronized void processSingleRequest(BookingRequest request) {
        if (!inventoryService.isAvailable(request.roomType)) {
            return;
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

    void printSummary() {
        List<Booking> history = bookingService.getBookingHistory();
        Map<String, Integer> roomTypeCount = new HashMap<>();
        for (Booking b : history) {
            roomTypeCount.put(b.roomType, roomTypeCount.getOrDefault(b.roomType, 0) + 1);
        }
        System.out.println("Booking Summary:");
        for (String type : roomTypeCount.keySet()) {
            System.out.println(type + ": " + roomTypeCount.get(type));
        }
    }
}

public class UC8 {
    public static void main(String[] args) {
        Map<String, Integer> initialInventory = new HashMap<>();
        initialInventory.put("DELUXE", 2);
        initialInventory.put("STANDARD", 3);

        InventoryService inventoryService = new InventoryService(initialInventory);
        BookingService bookingService = new BookingService(inventoryService);

        bookingService.addRequest(new BookingRequest("REQ1", "DELUXE"));
        bookingService.addRequest(new BookingRequest("REQ2", "STANDARD"));
        bookingService.addRequest(new BookingRequest("REQ3", "DELUXE"));

        bookingService.processRequests();

        BookingReportService reportService = new BookingReportService(bookingService);

        reportService.printAllBookings();
        reportService.printSummary();
    }
}