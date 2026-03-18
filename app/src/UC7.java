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
    private final Set<String> confirmedReservations = new HashSet<>();

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

    boolean isReservationValid(String requestId) {
        return confirmedReservations.contains(requestId);
    }
}

class AddOnService {
    String name;
    double cost;

    AddOnService(String name, double cost) {
        this.name = name;
        this.cost = cost;
    }
}

class AddOnServiceManager {
    private final Map<String, List<AddOnService>> reservationServices = new HashMap<>();
    private final BookingService bookingService;

    AddOnServiceManager(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    void addService(String reservationId, AddOnService service) {
        if (!bookingService.isReservationValid(reservationId)) {
            return;
        }

        reservationServices
                .computeIfAbsent(reservationId, k -> new ArrayList<>())
                .add(service);
    }

    double calculateTotalCost(String reservationId) {
        List<AddOnService> services = reservationServices.getOrDefault(reservationId, new ArrayList<>());
        double total = 0;
        for (AddOnService s : services) {
            total += s.cost;
        }
        return total;
    }

    void printServices(String reservationId) {
        List<AddOnService> services = reservationServices.getOrDefault(reservationId, new ArrayList<>());
        for (AddOnService s : services) {
            System.out.println(s.name + " - " + s.cost);
        }
    }
}

public class UC7 {
    public static void main(String[] args) {
        Map<String, Integer> initialInventory = new HashMap<>();
        initialInventory.put("DELUXE", 2);
        initialInventory.put("STANDARD", 3);

        InventoryService inventoryService = new InventoryService(initialInventory);
        BookingService bookingService = new BookingService(inventoryService);

        bookingService.addRequest(new BookingRequest("REQ1", "DELUXE"));
        bookingService.addRequest(new BookingRequest("REQ2", "STANDARD"));

        bookingService.processRequests();


        AddOnServiceManager manager = new AddOnServiceManager(bookingService);

        manager.addService("REQ1", new AddOnService("Breakfast", 500));
        manager.addService("REQ1", new AddOnService("Airport Pickup", 1200));
        manager.addService("REQ2", new AddOnService("Extra Bed", 800));

        System.out.println("Services for REQ1:");
        manager.printServices("REQ1");
        System.out.println("Total Cost: " + manager.calculateTotalCost("REQ1"));

        System.out.println("Services for REQ2:");
        manager.printServices("REQ2");
        System.out.println("Total Cost: " + manager.calculateTotalCost("REQ2"));
    }
}