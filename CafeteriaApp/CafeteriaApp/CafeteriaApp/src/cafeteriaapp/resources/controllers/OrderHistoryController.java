/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cafeteriaapp.resources.controllers;

import cafeteriaapp.DatabaseConnection;
import cafeteriaapp.SceneManager;
import cafeteriaapp.Session;
import cafeteriaapp.User;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class OrderHistoryController implements Initializable {

    @FXML
    private VBox ordersContainer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadOrderHistory();
    }

    @FXML
    private void handleBackToHome(MouseEvent event) {
        try {
            SceneManager.switchTo("resources/views/dashboard.fxml", true);
        } catch (IOException ex) {
            Logger.getLogger(OrderHistoryController.class.getName()).log(Level.SEVERE, "Failed to navigate back", ex);
        }
    }

    private void loadOrderHistory() {
        ordersContainer.getChildren().clear();
        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showEmptyState("You must be logged in to view order history.");
            return;
        }

        String fetchOrdersQuery = "SELECT id, total_amount, order_date FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        String fetchItemsQuery = "SELECT oi.quantity, oi.price_at_purchase, mi.name "
                               + "FROM order_items oi "
                               + "JOIN menu_items mi ON oi.menu_item_id = mi.id "
                               + "WHERE oi.order_id = ?";

        try (Connection conn = DatabaseConnection.getConnect();
             PreparedStatement orderStmt = conn.prepareStatement(fetchOrdersQuery)) {

            orderStmt.setInt(1, currentUser.getId());
            ResultSet orderRs = orderStmt.executeQuery();

            boolean hasOrders = false;
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a");

            while (orderRs.next()) {
                hasOrders = true;
                int orderId = orderRs.getInt("id");
                double total = orderRs.getDouble("total_amount");
                java.sql.Timestamp date = orderRs.getTimestamp("order_date");
                String formattedDate = dateFormat.format(date);

                // Create the Card UI for this specific order
                VBox orderCard = createOrderCard(orderId, formattedDate, total);
                VBox itemsList = new VBox(5);
                itemsList.setStyle("-fx-padding: 10 0 10 0;");

                // Fetch the items inside this specific order
                try (PreparedStatement itemStmt = conn.prepareStatement(fetchItemsQuery)) {
                    itemStmt.setInt(1, orderId);
                    ResultSet itemRs = itemStmt.executeQuery();

                    while (itemRs.next()) {
                        String itemName = itemRs.getString("name");
                        int qty = itemRs.getInt("quantity");
                        double price = itemRs.getDouble("price_at_purchase");
                        
                        itemsList.getChildren().add(createItemRow(itemName, qty, price));
                    }
                }

                orderCard.getChildren().add(1, itemsList); // Insert items list into the middle of the card
                ordersContainer.getChildren().add(orderCard);
            }

            if (!hasOrders) {
                showEmptyState("You haven't placed any orders yet.");
            }

        } catch (SQLException ex) {
            Logger.getLogger(OrderHistoryController.class.getName()).log(Level.SEVERE, "Error loading history", ex);
            showEmptyState("Error loading your order history.");
        }
    }

    private VBox createOrderCard(int orderId, String date, double total) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        VBox.setMargin(card, new Insets(0, 0, 15, 0));

        // Header: Order ID and Date
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT); // Changed from CENTER_BETWEEN
        header.setStyle("-fx-border-color: transparent transparent #eee transparent; -fx-border-width: 1; -fx-padding: 0 0 10 0;");
        
        Label idLabel = new Label("Order #" + orderId);
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1a1a2e;");
        
        // --- NEW: Add an expanding spacer to push the date to the right ---
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
        
        // --- Added the spacer into the middle of the HBox ---
        header.getChildren().addAll(idLabel, spacer, dateLabel);

        // Footer: Total Amount
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-border-color: #eee transparent transparent transparent; -fx-border-width: 1; -fx-padding: 10 0 0 0;");
        
        Label totalText = new Label("Total: ");
        totalText.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
        
        Label totalAmount = new Label("₦" + String.format("%,.2f", total));
        totalAmount.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2ecc71;");
        
        footer.getChildren().addAll(totalText, totalAmount);

        card.getChildren().addAll(header, footer);
        return card;
    }

    private HBox createItemRow(String name, int qty, double price) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label qtyLabel = new Label(qty + "x  ");
        qtyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
        
        Label nameLabel = new Label(name);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        
        Label priceLabel = new Label("₦" + String.format("%,.2f", price * qty));
        priceLabel.setStyle("-fx-text-fill: #555;");
        
        row.getChildren().addAll(qtyLabel, nameLabel, priceLabel);
        return row;
    }

    private void showEmptyState(String message) {
        Label empty = new Label(message);
        empty.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-padding: 50 0 0 0;");
        ordersContainer.setAlignment(Pos.TOP_CENTER);
        ordersContainer.getChildren().add(empty);
    }
}