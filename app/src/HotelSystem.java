import java.util.List;
import java.util.stream.Collectors;

class Room {
    private String type;
    private double price;
    private List<String> amenities;
    private int availability;

    public Room(String type, double price, List<String> amenities, int availability) {
        this.type = type;
        this.price = price;
        this.amenities = amenities;
        this.availability = availability;
    }

    public String getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public int getAvailability() {
        return availability;
    }
}

class Inventory {
    private List<Room> rooms;

    public Inventory(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Room> getRooms() {
        return rooms;
    }
}

class SearchService {
    private Inventory inventory;

    public SearchService(Inventory inventory) {
        this.inventory = inventory;
    }

    public List<Room> searchAvailableRooms() {
        return inventory.getRooms().stream()
                .filter(room -> room.getAvailability() > 0)
                .collect(Collectors.toList());
    }

    public Room getRoomDetails(String roomType) {
        return inventory.getRooms().stream()
                .filter(room -> room.getType().equals(roomType) && room.getAvailability() > 0)
                .findFirst()
                .orElse(null);
    }
}

class Guest {
    private SearchService searchService;

    public Guest(SearchService searchService) {
        this.searchService = searchService;
    }

    public void viewAvailableRooms() {
        List<Room> availableRooms = searchService.searchAvailableRooms();
        availableRooms.forEach(room -> System.out.println("Room: " + room.getType() + ", Price: " + room.getPrice() + ", Availability: " + room.getAvailability()));
    }

    public void viewRoomDetails(String roomType) {
        Room room = searchService.getRoomDetails(roomType);
        if (room != null) {
            System.out.println("Room: " + room.getType() + ", Price: " + room.getPrice() + ", Amenities: " + room.getAmenities() + ", Availability: " + room.getAvailability());
        } else {
            System.out.println("Room not available.");
        }
    }
}

public class HotelSystem {
    public static void main(String[] args) {
        Room room1 = new Room("Deluxe", 200.00, List.of("WiFi", "TV", "AC"), 5);
        Room room2 = new Room("Standard", 150.00, List.of("WiFi", "TV"), 0);
        Room room3 = new Room("Suite", 300.00, List.of("WiFi", "TV", "AC", "Mini-Bar"), 3);

        Inventory inventory = new Inventory(List.of(room1, room2, room3));
        SearchService searchService = new SearchService(inventory);
        Guest guest = new Guest(searchService);

        guest.viewAvailableRooms();
        guest.viewRoomDetails("Deluxe");
    }
}