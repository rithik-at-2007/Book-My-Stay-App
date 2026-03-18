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

class BookingService {
    private final Queue<BookingRequest> queue = new LinkedList<>();
    private final InventoryService inventoryService;
    private final Map<String, Set<String>> allocatedRooms = new HashMap<>();
    private final Set<String> allRoomIds = new HashSet<>();

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

        inventoryService.decrement(request.roomType);

        confirmReservation(request.requestId, roomId);
    }

    private String generateUniqueRoomId() {
        String id;
        do {
            id = UUID.randomUUID().toString();
        } while (allRoomIds.contains(id));
        return id;
    }

    private void confirmReservation(String requestId, String roomId) {
        System.out.println("Confirmed: " + requestId + " -> Room " + roomId);
    }
}

public class UC6{
    public static void main(String[] args) {
        Map<String, Integer> initialInventory = new HashMap<>();
        initialInventory.put("DELUXE", 2);
        initialInventory.put("STANDARD", 3);

        InventoryService inventoryService = new InventoryService(initialInventory);
        BookingService bookingService = new BookingService(inventoryService);

        bookingService.addRequest(new BookingRequest("REQ1", "DELUXE"));
        bookingService.addRequest(new BookingRequest("REQ2", "DELUXE"));
        bookingService.addRequest(new BookingRequest("REQ3", "DELUXE"));
        bookingService.addRequest(new BookingRequest("REQ4", "STANDARD"));

        bookingService.processRequests();
    }
}