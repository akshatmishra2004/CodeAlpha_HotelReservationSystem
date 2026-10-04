import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    static final String DATA_FILE = "hotel_data.dat";

    enum RoomType { STANDARD, DELUXE, SUITE }

    static class Room implements Serializable {
        int roomNumber;
        RoomType type;
        double pricePerNight;

        Room(int roomNumber, RoomType type, double pricePerNight) {
            this.roomNumber = roomNumber;
            this.type = type;
            this.pricePerNight = pricePerNight;
        }
    }

    static class Reservation implements Serializable {
        int bookingId, roomNumber;
        String guestName, phone;
        LocalDate checkIn, checkOut;
        double amount;
        String paymentStatus;

        Reservation(int bookingId, int roomNumber, String guestName, String phone,
                    LocalDate checkIn, LocalDate checkOut, double amount, String paymentStatus) {
            this.bookingId = bookingId;
            this.roomNumber = roomNumber;
            this.guestName = guestName;
            this.phone = phone;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.amount = amount;
            this.paymentStatus = paymentStatus;
        }
    }

    static List<Room> rooms = new ArrayList<>();
    static List<Reservation> reservations = new ArrayList<>();
    static int nextBookingId = 1001;

    public static void main(String[] args) {
        loadData();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n========== HOTEL RESERVATION SYSTEM ==========");
            System.out.println("1. Display All Rooms");
            System.out.println("2. Search Available Rooms");
            System.out.println("3. Book Room");
            System.out.println("4. View Booking Details");
            System.out.println("5. Cancel Reservation");
            System.out.println("6. View All Reservations");
            System.out.println("7. Save & Exit");
            System.out.print("Enter choice: ");

            int choice = readInt(sc);

            switch (choice) {
                case 1 -> displayRooms();
                case 2 -> searchRooms(sc);
                case 3 -> bookRoom(sc);
                case 4 -> viewBooking(sc);
                case 5 -> cancelBooking(sc);
                case 6 -> displayReservations();
                case 7 -> {
                    saveData();
                    System.out.println("Data saved. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    static void initializeRooms() {
        rooms.clear();
        rooms.add(new Room(101, RoomType.STANDARD, 2000));
        rooms.add(new Room(102, RoomType.STANDARD, 2000));
        rooms.add(new Room(201, RoomType.DELUXE, 3500));
        rooms.add(new Room(202, RoomType.DELUXE, 3500));
        rooms.add(new Room(301, RoomType.SUITE, 6000));
        rooms.add(new Room(302, RoomType.SUITE, 6000));
    }

    static void displayRooms() {
        System.out.println("\nRoom\tType\t\tPrice/Night\tStatus");
        System.out.println("-----------------------------------------------");
        for (Room room : rooms) {
            System.out.printf("%d\t%-10s\tRs. %.2f\t%s%n",
                    room.roomNumber, room.type, room.pricePerNight,
                    isCurrentlyBooked(room.roomNumber) ? "Booked" : "Available");
        }
    }

    static void searchRooms(Scanner sc) {
        System.out.print("Enter check-in date (yyyy-MM-dd): ");
        LocalDate checkIn = readDate(sc);
        System.out.print("Enter check-out date (yyyy-MM-dd): ");
        LocalDate checkOut = readDate(sc);

        if (!validDates(checkIn, checkOut)) return;

        System.out.println("\nAvailable rooms:");
        boolean found = false;

        for (Room room : rooms) {
            if (isAvailable(room.roomNumber, checkIn, checkOut)) {
                System.out.printf("Room %d | %-8s | Rs. %.2f/night%n",
                        room.roomNumber, room.type, room.pricePerNight);
                found = true;
            }
        }

        if (!found) System.out.println("No rooms available for these dates.");
    }

    static void bookRoom(Scanner sc) {
        System.out.print("Enter check-in date (yyyy-MM-dd): ");
        LocalDate checkIn = readDate(sc);
        System.out.print("Enter check-out date (yyyy-MM-dd): ");
        LocalDate checkOut = readDate(sc);

        if (!validDates(checkIn, checkOut)) return;

        searchRoomsForDates(checkIn, checkOut);

        System.out.print("Enter room number: ");
        int roomNumber = readInt(sc);
        Room room = findRoom(roomNumber);

        if (room == null || !isAvailable(roomNumber, checkIn, checkOut)) {
            System.out.println("Room is unavailable or does not exist.");
            return;
        }

        sc.nextLine();
        System.out.print("Enter guest name: ");
        String name = sc.nextLine();
        System.out.print("Enter phone number: ");
        String phone = sc.nextLine();

        long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
        double amount = nights * room.pricePerNight;

        System.out.printf("Total amount: Rs. %.2f%n", amount);
        System.out.print("Proceed with simulated payment? (yes/no): ");
        String payment = sc.nextLine();

        if (!payment.equalsIgnoreCase("yes")) {
            System.out.println("Booking cancelled before payment.");
            return;
        }

        Reservation r = new Reservation(
                nextBookingId++, roomNumber, name, phone,
                checkIn, checkOut, amount, "PAID"
        );

        reservations.add(r);
        saveData();

        System.out.println("\nBooking successful!");
        System.out.println("Booking ID: " + r.bookingId);
        System.out.println("Room: " + r.roomNumber);
        System.out.println("Amount Paid: Rs. " + String.format("%.2f", r.amount));
    }

    static void viewBooking(Scanner sc) {
        System.out.print("Enter booking ID: ");
        int id = readInt(sc);

        for (Reservation r : reservations) {
            if (r.bookingId == id) {
                printReservation(r);
                return;
            }
        }
        System.out.println("Booking not found.");
    }

    static void cancelBooking(Scanner sc) {
        System.out.print("Enter booking ID to cancel: ");
        int id = readInt(sc);

        Iterator<Reservation> iterator = reservations.iterator();
        while (iterator.hasNext()) {
            Reservation r = iterator.next();
            if (r.bookingId == id) {
                iterator.remove();
                saveData();
                System.out.println("Reservation " + id + " cancelled successfully.");
                return;
            }
        }
        System.out.println("Booking not found.");
    }

    static void displayReservations() {
        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }

        System.out.println("\n================ RESERVATIONS ================");
        for (Reservation r : reservations) printReservation(r);
    }

    static void printReservation(Reservation r) {
        System.out.println("-----------------------------------------------");
        System.out.println("Booking ID   : " + r.bookingId);
        System.out.println("Guest Name   : " + r.guestName);
        System.out.println("Phone        : " + r.phone);
        System.out.println("Room Number  : " + r.roomNumber);
        System.out.println("Check-in     : " + r.checkIn);
        System.out.println("Check-out    : " + r.checkOut);
        System.out.println("Amount       : Rs. " + String.format("%.2f", r.amount));
        System.out.println("Payment      : " + r.paymentStatus);
    }

    static void searchRoomsForDates(LocalDate checkIn, LocalDate checkOut) {
        System.out.println("\nAvailable rooms:");
        for (Room room : rooms)
            if (isAvailable(room.roomNumber, checkIn, checkOut))
                System.out.printf("Room %d | %-8s | Rs. %.2f/night%n",
                        room.roomNumber, room.type, room.pricePerNight);
    }

    static boolean isAvailable(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        for (Reservation r : reservations) {
            if (r.roomNumber == roomNumber &&
                checkIn.isBefore(r.checkOut) && checkOut.isAfter(r.checkIn)) {
                return false;
            }
        }
        return true;
    }

    static boolean isCurrentlyBooked(int roomNumber) {
        LocalDate today = LocalDate.now();
        for (Reservation r : reservations) {
            if (r.roomNumber == roomNumber &&
                !today.isBefore(r.checkIn) && today.isBefore(r.checkOut)) {
                return true;
            }
        }
        return false;
    }

    static Room findRoom(int number) {
        for (Room r : rooms)
            if (r.roomNumber == number) return r;
        return null;
    }

    static boolean validDates(LocalDate checkIn, LocalDate checkOut) {
        if (!checkIn.isBefore(checkOut)) {
            System.out.println("Check-out must be after check-in.");
            return false;
        }
        if (checkIn.isBefore(LocalDate.now())) {
            System.out.println("Check-in date cannot be in the past.");
            return false;
        }
        return true;
    }

    static LocalDate readDate(Scanner sc) {
        while (true) {
            try {
                return LocalDate.parse(sc.next(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                System.out.print("Invalid date. Use yyyy-MM-dd: ");
            }
        }
    }

    static int readInt(Scanner sc) {
        while (!sc.hasNextInt()) {
            System.out.print("Enter a valid number: ");
            sc.next();
        }
        return sc.nextInt();
    }

    static void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(rooms);
            out.writeObject(reservations);
            out.writeInt(nextBookingId);
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    static void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            initializeRooms();
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            rooms = (List<Room>) in.readObject();
            reservations = (List<Reservation>) in.readObject();
            nextBookingId = in.readInt();
        } catch (Exception e) {
            initializeRooms();
            reservations.clear();
            nextBookingId = 1001;
            System.out.println("Starting with fresh hotel data.");
        }
    }
}
