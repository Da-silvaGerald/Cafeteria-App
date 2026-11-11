/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cafeteriaapp.resources.controllers;

import cafeteriaapp.DatabaseConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
/**
 *
 * @author dasil
 */
public class ManagerDashboardController implements Initializable {

    // --- Table UI Components ---
    @FXML private TableView<ManagerMenuItem> menuTable;
    @FXML private TableColumn<ManagerMenuItem, Integer> colId;
    @FXML private TableColumn<ManagerMenuItem, String> colName;
    @FXML private TableColumn<ManagerMenuItem, String> colCategory;
    @FXML private TableColumn<ManagerMenuItem, Integer> colQty;
    @FXML private TableColumn<ManagerMenuItem, Double> colPrice;

    // --- Form Input Components ---
    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField qtyField;
    @FXML private TextField priceField;
    @FXML private TextArea descArea;

    private ObservableList<ManagerMenuItem> menuItemsList = FXCollections.observableArrayList();
    private ManagerMenuItem selectedItem = null; // Tracks the item currently being edited

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Map TableColumns to the properties of ManagerMenuItem class
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        // 2. Load data from the database
        loadMenuData();

        // 3. Listen for row selection in the table to populate the text fields for editing
        menuTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedItem = newSelection;
                nameField.setText(selectedItem.getName());
                categoryField.setText(selectedItem.getCategory());
                qtyField.setText(String.valueOf(selectedItem.getQuantity()));
                priceField.setText(String.valueOf(selectedItem.getPrice()));
                descArea.setText(selectedItem.getDescription());
            }
        });
    }

    /**
     * READ: Fetches all items from the database and loads them into the TableView
     */
    private void loadMenuData() {
        menuItemsList.clear();
        String query = "SELECT id, name, category, quantity, price, description FROM menu_items ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.getConnect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                menuItemsList.add(new ManagerMenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("description")
                ));
            }
            menuTable.setItems(menuItemsList);

        } catch (SQLException ex) {
            Logger.getLogger(ManagerDashboardController.class.getName()).log(Level.SEVERE, "Failed to load menu data", ex);
        }
    }

    /**
     * CREATE: Adds a new item to the database
     */
    @FXML
    private void handleAddItem() {
        if (!validateInputs()) return;

        // 1. ADDED 'image_path' to the columns and a 6th '?' to the VALUES
        String query = "INSERT INTO menu_items (name, category, quantity, price, description, image_path) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, nameField.getText());
            stmt.setString(2, categoryField.getText());
            stmt.setInt(3, Integer.parseInt(qtyField.getText()));
            stmt.setDouble(4, Double.parseDouble(priceField.getText()));
            stmt.setString(5, descArea.getText());
            
            // 2. Pass an empty string (or a default image path) to satisfy the database
            stmt.setString(6, ""); 
            
            stmt.executeUpdate();
            
            showSuccessAlert("Item added successfully!");
            clearFields();
            loadMenuData(); // Refresh table
            
        } catch (SQLException ex) {
            // This prints the exact reason the database failed to your NetBeans output terminal!
            System.out.println("SQL ERROR DETAILS: " + ex.getMessage()); 
            
            Logger.getLogger(ManagerDashboardController.class.getName()).log(Level.SEVERE, "Failed to add item", ex);
            showErrorAlert("Database Error: Could not add item.");
        }
    }

    /**
     * UPDATE: Modifies the currently selected item
     */
    @FXML
    private void handleUpdateItem() {
        if (selectedItem == null) {
            showErrorAlert("Please select an item from the table to update.");
            return;
        }
        if (!validateInputs()) return;

        String query = "UPDATE menu_items SET name=?, category=?, quantity=?, price=?, description=? WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getConnect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, nameField.getText());
            stmt.setString(2, categoryField.getText());
            stmt.setInt(3, Integer.parseInt(qtyField.getText()));
            stmt.setDouble(4, Double.parseDouble(priceField.getText()));
            stmt.setString(5, descArea.getText());
            stmt.setInt(6, selectedItem.getId());
            
            stmt.executeUpdate();
            
            showSuccessAlert("Item updated successfully!");
            clearFields();
            loadMenuData(); // Refresh table
            
        } catch (SQLException ex) {
            Logger.getLogger(ManagerDashboardController.class.getName()).log(Level.SEVERE, "Failed to update item", ex);
            showErrorAlert("Database Error: Could not update item.");
        }
    }

    /**
     * DELETE: Removes the currently selected item
     */
    @FXML
    private void handleDeleteItem() {
        if (selectedItem == null) {
            showErrorAlert("Please select an item from the table to delete.");
            return;
        }

        String query = "DELETE FROM menu_items WHERE id=?";
        
        try (Connection conn = DatabaseConnection.getConnect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, selectedItem.getId());
            stmt.executeUpdate();
            
            showSuccessAlert("Item deleted successfully!");
            clearFields();
            loadMenuData(); // Refresh table
            
        } catch (SQLException ex) {
            Logger.getLogger(ManagerDashboardController.class.getName()).log(Level.SEVERE, "Failed to delete item", ex);
            showErrorAlert("Database Error: Could not delete item.");
        }
    }

    /**
     * Clears the input form
     */
    @FXML
    private void clearFields() {
        nameField.clear();
        categoryField.clear();
        qtyField.clear();
        priceField.clear();
        descArea.clear();
        selectedItem = null;
        menuTable.getSelectionModel().clearSelection();
    }

    /**
     * Validates that inputs are not empty and numbers are properly formatted.
     */
    private boolean validateInputs() {
        if (nameField.getText().isEmpty() || categoryField.getText().isEmpty() || 
            qtyField.getText().isEmpty() || priceField.getText().isEmpty()) {
            showErrorAlert("Please fill in all required fields.");
            return false;
        }
        try {
            Integer.parseInt(qtyField.getText());
            Double.parseDouble(priceField.getText());
        } catch (NumberFormatException e) {
            showErrorAlert("Quantity must be an integer, and Price must be a valid number.");
            return false;
        }
        return true;
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }

    /**
     * Internal data model used exclusively for the Manager's TableView
     */
    public static class ManagerMenuItem {
        private final int id;
        private final String name;
        private final String category;
        private final int quantity;
        private final double price;
        private final String description;

        public ManagerMenuItem(int id, String name, String category, int quantity, double price, String description) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.price = price;
            this.description = description;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public String getDescription() { return description; }
    }
}