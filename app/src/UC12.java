import java.io.*;
import java.util.*;

class BookingRequest implements Serializable {
    String requestId;
    String roomType;

    BookingRequest(String requestId, String roomType) {
        this.requestId = requestId;
        this.roomType = roomType;
    }
}

class InventoryService implements Serializable {
    private final Map<String, Integer> inventory = new HashMap<>();

    InventoryService(Map<String, Integer> initialInventory) {
        inventory.putAll(initialInventory);
    }

    synchronized boolean isAvailable(String roomType) {
        return inventory.getOrDefault(roomType, 0) > 0;
    }

    synchronized void decrement(String roomType) throws Exception {
        int count = inventory.getOrDefault(roomType, 0);
        if (count <= 0) throw new Exception("No inventory for room type: " + roomType);
        inventory.put(roomType, count - 1);
    }

    synchronized void increment(String roomType) {
        inventory.put(roomType, inventory.getOrDefault(roomType, 0) + 1);
    }

    synchronized void printInventory() {
        System.out.println("Current Inventory: " + inventory);
    }
}

class Booking implements Serializable {
    String reservationId;
    String roomId;
    String roomType;

    Booking(String reservationId, String roomId, String roomType) {
        this.reservationId = reservationId;
        this.roomId = roomId;
        this.roomType = roomType;
    }
}

class BookingService implements Serializable {
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
            } catch (Exception e) {
                System.out.println("Booking failed for " + request.requestId + ": " + e.getMessage());
            }
        }
    }

    private synchronized void processSingleRequest(BookingRequest request) throws Exception {
        if (!inventoryService.isAvailable(request.roomType)) {
            throw new Exception("No available rooms for type: " + request.roomType);
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

    void printBookings() {
        for (Booking b : bookingHistory) {
            System.out.println("Reservation: " + b.reservationId + ", Room Type: " + b.roomType + ", Room ID: " + b.roomId);
        }
    }
}

class PersistenceService {
    private final String fileName;

    PersistenceService(String fileName) {
        this.fileName = fileName;
    }

    void saveState(BookingService bookingService, InventoryService inventoryService) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(bookingService);
            out.writeObject(inventoryService);
            System.out.println("State saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save state: " + e.getMessage());
        }
    }

    Object[] restoreState() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            BookingService bookingService = (BookingService) in.readObject();
            InventoryService inventoryService = (InventoryService) in.readObject();
            System.out.println("State restored successfully.");
            return new Object[]{bookingService, inventoryService};
        } catch (FileNotFoundException e) {
            System.out.println("No saved state found. Starting fresh.");
        } catch (Exception e) {
            System.out.println("Failed to restore state: " + e.getMessage());
        }
        return null;
    }
}

public class UC12 {
    public static void main(String[] args) {
        String persistenceFile = "booking_state.dat";
        PersistenceService persistenceService = new PersistenceService(persistenceFile);

        Object[] restored = persistenceService.restoreState();
        InventoryService inventoryService;
        BookingService bookingService;

        if (restored != null) {
            bookingService = (BookingService) restored[0];
            inventoryService = (InventoryService) restored[1];
        } else {
            Map<String, Integer> initialInventory = new HashMap<>();
            initialInventory.put("DELUXE", 2);
            initialInventory.put("STANDARD", 3);

            inventoryService = new InventoryService(initialInventory);
            bookingService = new BookingService(inventoryService);

            bookingService.addRequest(new BookingRequest("REQ1", "DELUXE"));
            bookingService.addRequest(new BookingRequest("REQ2", "STANDARD"));
        }

        bookingService.processRequests();

        System.out.println("\nCurrent Bookings After Processing:");
        bookingService.printBookings();
        inventoryService.printInventory();

        persistenceService.saveState(bookingService, inventoryService);
    }
}