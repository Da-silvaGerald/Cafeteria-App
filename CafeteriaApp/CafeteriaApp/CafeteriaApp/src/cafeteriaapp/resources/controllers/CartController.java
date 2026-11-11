/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cafeteriaapp.resources.controllers;

import cafeteriaapp.Cart;
import cafeteriaapp.DatabaseConnection;
import cafeteriaapp.SceneManager;
import cafeteriaapp.Session;
import cafeteriaapp.User;
import cafeteriaapp.resources.controllers.MenuController.MenuItem;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CartController implements Initializable {

    @FXML private VBox cartItemsContainer;
    @FXML private Label totalLabel;
    @FXML private Button checkoutBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCartUI();
    }

    @FXML
    private void handleBackToHome(MouseEvent event) {
        try {
            SceneManager.switchTo("resources/views/dashboard.fxml", true);
        } catch (IOException ex) {
            Logger.getLogger(CartController.class.getName()).log(Level.SEVERE, "Failed to navigate back", ex);
        }
    }

private void loadCartUI() {
        cartItemsContainer.getChildren().clear();
        Cart cart = Cart.getInstance();

        // TERMINAL TRACKER: Prints to your NetBeans output when the Cart page opens
        System.out.println(">> OPENING CART PAGE | FOUND CART SIZE: " + cart.getItems().size() + " | CART ID: " + cart.hashCode());

        if (cart.getItems().isEmpty()) {
            Label emptyLbl = new Label("Your cart is completely empty.");
            emptyLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #A52A2A; -fx-font-weight: bold;");
            cartItemsContainer.setAlignment(Pos.CENTER);
            cartItemsContainer.getChildren().add(emptyLbl);
            checkoutBtn.setDisable(true);
            totalLabel.setText("₦0.00");
            return;
        }

        checkoutBtn.setDisable(false);
        cartItemsContainer.setAlignment(Pos.TOP_CENTER);

        for (Map.Entry<MenuItem, Integer> entry : cart.getItems().entrySet()) {
            cartItemsContainer.getChildren().add(createCartRow(entry.getKey(), entry.getValue()));
        }

        totalLabel.setText("₦" + String.format("%,.2f", cart.getTotalCost()));
    }

    private HBox createCartRow(MenuItem item, int quantity) {
        HBox row = new HBox(15);
        // White card background to contrast against the cream FXML background
        row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-radius: 8;");
        row.setAlignment(Pos.CENTER_LEFT);

        VBox infoBox = new VBox(8);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        Label nameLbl = new Label(item.getName());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1A1A2E;");
        
        Label priceLbl = new Label("(@ ₦" + String.format("%,.2f", item.getPrice()) + " each)");
        priceLbl.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");

        // --- Interactive Quantity Controls ---
        HBox qtyBox = new HBox(10);
        qtyBox.setAlignment(Pos.CENTER_LEFT);

        Button minusBtn = new Button("-");
        // Styled with Brand Brown
        minusBtn.setStyle("-fx-background-color: #A52A2A; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-font-weight: bold;");
        minusBtn.setOnAction(e -> {
            Cart.getInstance().setQuantity(item, quantity - 1);
            loadCartUI(); // Refresh UI to update totals instantly
        });

        Label qtyLabel = new Label(String.valueOf(quantity));
        qtyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button plusBtn = new Button("+");
        // Styled with Brand Brown
        plusBtn.setStyle("-fx-background-color: #A52A2A; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-font-weight: bold;");
        plusBtn.setOnAction(e -> {
            if (quantity >= item.getQuantity()) {
                showAlert(Alert.AlertType.WARNING, "Stock Limit", "We only have " + item.getQuantity() + " of " + item.getName() + " left in stock.");
            } else {
                Cart.getInstance().setQuantity(item, quantity + 1);
                loadCartUI();
            }
        });

        qtyBox.getChildren().addAll(minusBtn, qtyLabel, plusBtn);

        infoBox.getChildren().addAll(nameLbl, priceLbl, qtyBox);

        Label itemTotal = new Label("₦" + String.format("%,.2f", item.getPrice() * quantity));
        itemTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #A52A2A;");

        Button removeBtn = new Button("Remove");
        // Styled with an outline to look clean
        removeBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #A52A2A; -fx-text-fill: #A52A2A; -fx-border-radius: 15; -fx-background-radius: 15; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            Cart.getInstance().getItems().remove(item);
            loadCartUI(); 
        });

        row.getChildren().addAll(infoBox, itemTotal, removeBtn);
        return row;
    }

    @FXML
    private void processCheckout(ActionEvent event) {
        Cart cart = Cart.getInstance();
        User currentUser = Session.getInstance().getCurrentUser();

        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Checkout Error", "You must be logged in to checkout.");
            return;
        }

        if (currentUser.getAccountBalance() < cart.getTotalCost()) {
            showAlert(Alert.AlertType.ERROR, "Insufficient Funds", "Your wallet balance (₦" + String.format("%,.2f", currentUser.getAccountBalance()) + ") is too low.");
            return;
        }

        String insertOrder = "INSERT INTO orders (user_id, total_amount) VALUES (?, ?) RETURNING id";
        String insertOrderItem = "INSERT INTO order_items (order_id, menu_item_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
        String updateStock = "UPDATE menu_items SET quantity = quantity - ? WHERE id = ?";
        String deductBalance = "UPDATE users SET account_balance = account_balance - ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnect()) {
            conn.setAutoCommit(false); 

            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrder);
                 PreparedStatement itemStmt = conn.prepareStatement(insertOrderItem);
                 PreparedStatement stockStmt = conn.prepareStatement(updateStock);
                 PreparedStatement userStmt = conn.prepareStatement(deductBalance)) {

                orderStmt.setInt(1, currentUser.getId());
                orderStmt.setDouble(2, cart.getTotalCost());
                ResultSet rs = orderStmt.executeQuery();
                rs.next();
                int newOrderId = rs.getInt("id");

                for (Map.Entry<MenuItem, Integer> entry : cart.getItems().entrySet()) {
                    MenuItem item = entry.getKey();
                    int qtyPurchased = entry.getValue();

                    itemStmt.setInt(1, newOrderId);
                    itemStmt.setInt(2, item.getId());
                    itemStmt.setInt(3, qtyPurchased);
                    itemStmt.setDouble(4, item.getPrice());
                    itemStmt.addBatch();

                    stockStmt.setInt(1, qtyPurchased);
                    stockStmt.setInt(2, item.getId());
                    stockStmt.addBatch();
                }

                itemStmt.executeBatch();
                stockStmt.executeBatch();

                userStmt.setDouble(1, cart.getTotalCost());
                userStmt.setInt(2, currentUser.getId());
                userStmt.executeUpdate();

                conn.commit();
                
                StringBuilder receipt = new StringBuilder();
                receipt.append("==================================\n");
                receipt.append("          CAFETERIA RECEIPT       \n");
                receipt.append("==================================\n\n");
                
                for (Map.Entry<MenuItem, Integer> entry : cart.getItems().entrySet()) {
                    String itemName = entry.getKey().getName();
                    int qty = entry.getValue();
                    double price = entry.getKey().getPrice() * qty;
                    receipt.append(String.format("%-2d x %-20s ₦%,.2f\n", qty, itemName, price));
                }
                
                receipt.append("\n----------------------------------\n");
                receipt.append(String.format("%-25s ₦%,.2f\n", "TOTAL COST:", cart.getTotalCost()));
                
                double newBalance = currentUser.getAccountBalance() - cart.getTotalCost();
                receipt.append(String.format("%-25s ₦%,.2f\n", "REMAINING BALANCE:", newBalance));
                receipt.append("==================================\n");
                receipt.append("        Thank you for eating!     \n");

                currentUser.setAccountBalance((float) newBalance);
                cart.clear();
                
                showReceipt(receipt.toString());
                handleBackToHome(null);

            } catch (SQLException ex) {
                conn.rollback(); 
                showAlert(Alert.AlertType.ERROR, "Transaction Failed", "An error occurred. No money was deducted.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(CartController.class.getName()).log(Level.SEVERE, "Database connection failed.", ex);
        }
    }

    private void showReceipt(String receiptText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Checkout Successful");
        alert.setHeaderText("Order Saved");

        TextArea textArea = new TextArea(receiptText);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 14px;"); 
        
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}