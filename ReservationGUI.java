import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

class Room {
    int roomNumber;
    boolean occupied;
    String guestName;
    int roomType; // 1 for Single, 2 for Double, 3 for Suite

    public Room(int roomNumber, int roomType) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.occupied = false;
        this.guestName = "";
    }
}

class AdminGUI {
    private static final String ADMIN_USERNAME = "sarmad";
    private static final String ADMIN_PASSWORD = "321";

    public static void showAdminLogin() {
        JFrame frame = new JFrame("Admin Login");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(3, 2));

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        frame.add(usernameLabel);
        frame.add(usernameField);
        frame.add(passwordLabel);
        frame.add(passwordField);
        frame.add(new JLabel()); // Placeholder
        frame.add(loginButton);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
                JOptionPane.showMessageDialog(frame, "Login successful!");
                frame.dispose();
                ReservationGUI.showAdminMenu();
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }
}

class UserGUI {
    private static JTable userTable;
    private static DefaultTableModel userTableModel;

    public static void showUserMenu() {
        JFrame frame = new JFrame("User Menu");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        userTableModel = new DefaultTableModel(new String[]{"Room Number", "Room Type", "Occupied"}, 0);
        userTable = new JTable(userTableModel);
        JScrollPane tableScrollPane = new JScrollPane(userTable);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton viewRoomsButton = new JButton("View Available Rooms");
        JButton bookRoomButton = new JButton("Book Room");
        JButton logoutButton = new JButton("Logout");

        buttonPanel.add(viewRoomsButton);
        buttonPanel.add(bookRoomButton);
        buttonPanel.add(logoutButton);

        frame.add(tableScrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        viewRoomsButton.addActionListener(e -> updateUserTable());
        bookRoomButton.addActionListener(e -> bookRoom());
        logoutButton.addActionListener(e -> {
            frame.dispose();
            ReservationGUI.showMainMenu();
        });

        updateUserTable();
        frame.setVisible(true);
    }

    private static void updateUserTable() {
        userTableModel.setRowCount(0);
        synchronized (ReservationGUI.hotelRooms) {
            for (Room room : ReservationGUI.hotelRooms) {
                if (room != null && !room.occupied) {
                    userTableModel.addRow(new Object[]{
                            room.roomNumber,
                            room.roomType == 1 ? "Single" : room.roomType == 2 ? "Double" : "Suite",
                            "No"
                    });
                }
            }
        }
    }

    private static void bookRoom() {
        JFrame frame = new JFrame("Book Room");
        frame.setSize(400, 200);
        frame.setLayout(new GridLayout(3, 2));

        JLabel roomNumberLabel = new JLabel("Room Number:");
        JTextField roomNumberField = new JTextField();
        JLabel guestNameLabel = new JLabel("Your Name:");
        JTextField guestNameField = new JTextField();
        JButton bookButton = new JButton("Book");

        frame.add(roomNumberLabel);
        frame.add(roomNumberField);
        frame.add(guestNameLabel);
        frame.add(guestNameField);
        frame.add(new JLabel()); // Placeholder
        frame.add(bookButton);

        bookButton.addActionListener(e -> {
            try {
                int roomNumber = Integer.parseInt(roomNumberField.getText());
                String guestName = guestNameField.getText().trim();

                synchronized (ReservationGUI.hotelRooms) {
                    for (Room room : ReservationGUI.hotelRooms) {
                        if (room != null && room.roomNumber == roomNumber) {
                            if (room.occupied) {
                                JOptionPane.showMessageDialog(frame, "Room is already occupied.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            room.guestName = guestName;
                            room.occupied = true;
                            JOptionPane.showMessageDialog(frame, "Room booked successfully!");
                            updateUserTable();
                            frame.dispose();
                            return;
                        }
                    }
                }
                JOptionPane.showMessageDialog(frame, "Room not found.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input. Please enter a valid room number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }
}

public class ReservationGUI {
    protected static final int MAX_ROOMS = 100;
    protected static final Room[] hotelRooms = new Room[MAX_ROOMS];
    protected static int numRooms = 0;
    private static JTable adminTable;
    private static DefaultTableModel adminTableModel;

    public static void showAdminMenu() {
        JFrame frame = new JFrame("Admin Panel");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        adminTableModel = new DefaultTableModel(new String[]{"Room Number", "Room Type", "Occupied", "Guest Name"}, 0);
        adminTable = new JTable(adminTableModel);
        JScrollPane tableScrollPane = new JScrollPane(adminTable);

        JPanel buttonPanel = new JPanel();
        JButton addRoomButton = new JButton("Add Room");
        JButton checkoutButton = new JButton("Check Out Guest");
        JButton logoutButton = new JButton("Logout");

        buttonPanel.add(addRoomButton);
        buttonPanel.add(checkoutButton);
        buttonPanel.add(logoutButton);

        frame.add(tableScrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        addRoomButton.addActionListener(e -> addRoom());
        checkoutButton.addActionListener(e -> adminCheckOut());
        logoutButton.addActionListener(e -> {
            frame.dispose();
            showMainMenu();
        });

        updateAdminTable();
        frame.setVisible(true);
    }

    private static void addRoom() {
        synchronized (hotelRooms) {
            if (numRooms < MAX_ROOMS) {
                Room room = new Room(numRooms + 1, (numRooms % 3) + 1); // Cycles through room types
                hotelRooms[numRooms++] = room;
                updateAdminTable();
            } else {
                JOptionPane.showMessageDialog(null, "Maximum room capacity reached.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void adminCheckOut() {
        JFrame frame = new JFrame("Admin Check Out Guest");
        frame.setSize(400, 200);
        frame.setLayout(new GridLayout(3, 2));

        JLabel roomNumberLabel = new JLabel("Room Number:");
        JTextField roomNumberField = new JTextField();
        JButton checkOutButton = new JButton("Check Out");

        frame.add(roomNumberLabel);
        frame.add(roomNumberField);
        frame.add(new JLabel()); // Placeholder
        frame.add(checkOutButton);

        checkOutButton.addActionListener(e -> {
            try {
                int roomNumber = Integer.parseInt(roomNumberField.getText());
                synchronized (hotelRooms) {
                    for (Room room : hotelRooms) {
                        if (room != null && room.roomNumber == roomNumber) {
                            if (!room.occupied) {
                                JOptionPane.showMessageDialog(frame, "Room is already vacant.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            room.occupied = false;
                            room.guestName = "";
                            JOptionPane.showMessageDialog(frame, "Check-out successful!");
                            updateAdminTable();
                            frame.dispose();
                            return;
                        }
                    }
                }
                JOptionPane.showMessageDialog(frame, "Room not found.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    private static void updateAdminTable() {
        adminTableModel.setRowCount(0);
        synchronized (hotelRooms) {
            for (Room room : hotelRooms) {
                if (room != null) {
                    adminTableModel.addRow(new Object[]{
                            room.roomNumber,
                            room.roomType == 1 ? "Single" : room.roomType == 2 ? "Double" : "Suite",
                            room.occupied ? "Yes" : "No",
                            room.guestName
                    });
                }
            }
        }
    }

    public static void showMainMenu() {
        JFrame frame = new JFrame("Main Menu");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(2, 1));

        JButton adminButton = new JButton("Admin Panel");
        JButton userButton = new JButton("User Panel");

        frame.add(adminButton);
        frame.add(userButton);

        adminButton.addActionListener(e -> {
            frame.dispose();
            AdminGUI.showAdminLogin();
        });

        userButton.addActionListener(e -> {
            frame.dispose();
            UserGUI.showUserMenu();
        });

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ReservationGUI::showMainMenu);
    }
}
