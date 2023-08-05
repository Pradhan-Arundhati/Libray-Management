// Import required libraries
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LibraryManagementSystem extends JFrame {
    private JTextField bookNameField, authorField, borrowerField, searchField;
    private JButton addBookButton, deleteBookButton, borrowBookButton, returnBookButton, searchButton;
    private JTable table;
    private DefaultTableModel tableModel;

    // SQLite database connection
    private static final String DB_URL = "jdbc:sqlite:library.db";
    private Connection connection;

    public LibraryManagementSystem() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);

        // Initialize components
        bookNameField = new JTextField(20);
        authorField = new JTextField(20);
        borrowerField = new JTextField(20);
        searchField = new JTextField(20);
        addBookButton = new JButton("Add Book");
        deleteBookButton = new JButton("Delete Book");
        borrowBookButton = new JButton("Borrow Book");
        returnBookButton = new JButton("Return Book");
        searchButton = new JButton("Search");

        // Add action listeners to buttons
        addBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBook();
            }
        });

        deleteBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteBook();
            }
        });

        borrowBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                borrowBook();
            }
        });

        returnBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnBook();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchBooks();
            }
        });

        // Create a table with sample data (you can populate it from the database)
        String[] columnNames = {"Book Name", "Author", "Borrower"};
        Object[][] data = {
                {"Book 1", "Author 1", "Borrower 1"},
                {"Book 2", "Author 2", "Borrower 2"},
                {"Book 3", "Author 3", "Borrower 3"},
                // Add more rows here
        };
        tableModel = new DefaultTableModel(data, columnNames);
        table = new JTable(tableModel);

        // Create panels and add components
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Book Name:"));
        inputPanel.add(bookNameField);
        inputPanel.add(new JLabel("Author:"));
        inputPanel.add(authorField);
        inputPanel.add(new JLabel("Borrower:"));
        inputPanel.add(borrowerField);
        inputPanel.add(addBookButton);
        inputPanel.add(deleteBookButton);
        inputPanel.add(borrowBookButton);
        inputPanel.add(returnBookButton);
        inputPanel.add(new JLabel("Search:"));
        inputPanel.add(searchField);
        inputPanel.add(searchButton);

        JScrollPane scrollPane = new JScrollPane(table);

        // Set layout for the main panel
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Initialize the database connection
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTable();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Display the GUI
        setVisible(true);
    }

    // CRUD operations

    private void createTable() {
        try (Statement stmt = connection.createStatement()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY, bookName TEXT, author TEXT, borrower TEXT)";
            stmt.execute(createTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating the table.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addBook() {
        String bookName = bookNameField.getText();
        String author = authorField.getText();
        String borrower = borrowerField.getText();

        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO books (bookName, author, borrower) VALUES (?, ?, ?)")) {
            pstmt.setString(1, bookName);
            pstmt.setString(2, author);
            pstmt.setString(3, borrower);
            pstmt.executeUpdate();

            // Add the new book to the table
            tableModel.addRow(new String[]{bookName, author, borrower});

            // Clear the input fields
            bookNameField.setText("");
            authorField.setText("");
            borrowerField.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding the book.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String bookName = (String) table.getValueAt(selectedRow, 0);
            try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM books WHERE bookName = ?")) {
                pstmt.setString(1, bookName);
                pstmt.executeUpdate();
                tableModel.removeRow(selectedRow);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting the book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a book to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void borrowBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String bookName = (String) table.getValueAt(selectedRow, 0);
            String borrower = borrowerField.getText();
            try (PreparedStatement pstmt = connection.prepareStatement("UPDATE books SET borrower = ? WHERE bookName = ?")) {
                pstmt.setString(1, borrower);
                pstmt.setString(2, bookName);
                pstmt.executeUpdate();
                table.setValueAt(borrower, selectedRow, 2);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating the book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a book to borrow.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void returnBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String bookName = (String) table.getValueAt(selectedRow, 0);
            try (PreparedStatement pstmt = connection.prepareStatement("UPDATE books SET borrower = NULL WHERE bookName = ?")) {
                pstmt.setString(1, bookName);
                pstmt.executeUpdate();
                table.setValueAt("", selectedRow, 2);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating the book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a book to return.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void searchBooks() {
        String searchText = searchField.getText().trim().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        if (searchText.length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LibraryManagementSystem();
            }
        });
    }
}
