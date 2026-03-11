import java.util.HashMap;
import java.util.Map;

public class uc3 {

    public static void main(String[] args) {

        Room singleRoom = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suiteRoom = new SuiteRoom();

        RoomInventory inventory = new RoomInventory();

        inventory.addRoomType("Single Room", 5);
        inventory.addRoomType("Double Room", 3);
        inventory.addRoomType("Suite Room", 2);

        System.out.println("Hotel Room Inventory\n");

        singleRoom.displayDetails();
        System.out.println("Available: " + inventory.getAvailability("Single Room"));
        System.out.println();

        doubleRoom.displayDetails();
        System.out.println("Available: " + inventory.getAvailability("Double Room"));
        System.out.println();

        suiteRoom.displayDetails();
        System.out.println("Available: " + inventory.getAvailability("Suite Room"));
        System.out.println();

        inventory.updateAvailability("Single Room", -1);

        System.out.println("Updated Inventory\n");
        inventory.displayInventory();
    }
}

abstract class Room {

    protected String roomType;
    protected int beds;
    protected int size;
    protected double price;

    public Room(String roomType, int beds, int size, double price) {
        this.roomType = roomType;
        this.beds = beds;
        this.size = size;
        this.price = price;
    }

    public void displayDetails() {
        System.out.println("Room Type: " + roomType);
        System.out.println("Beds: " + beds);
        System.out.println("Size: " + size + " sq ft");
        System.out.println("Price per Night: $" + price);
    }
}

class SingleRoom extends Room {

    public SingleRoom() {
        super("Single Room", 1, 200, 80);
    }
}

class DoubleRoom extends Room {

    public DoubleRoom() {
        super("Double Room", 2, 350, 120);
    }
}

class SuiteRoom extends Room {

    public SuiteRoom() {
        super("Suite Room", 3, 600, 250);
    }
}

class RoomInventory {

    private Map<String, Integer> availability;

    public RoomInventory() {
        availability = new HashMap<>();
    }

    public void addRoomType(String roomType, int count) {
        availability.put(roomType, count);
    }

    public int getAvailability(String roomType) {
        return availability.getOrDefault(roomType, 0);
    }

    public void updateAvailability(String roomType, int change) {
        int current = availability.getOrDefault(roomType, 0);
        availability.put(roomType, current + change);
    }

    public void displayInventory() {
        for (Map.Entry<String, Integer> entry : availability.entrySet()) {
            System.out.println(entry.getKey() + " Available: " + entry.getValue());
        }
    }
}