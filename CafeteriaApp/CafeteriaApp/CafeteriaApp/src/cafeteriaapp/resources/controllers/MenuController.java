/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cafeteriaapp.resources.controllers;

import cafeteriaapp.Cart;
import cafeteriaapp.DatabaseConnection;
import cafeteriaapp.SceneManager; 
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.layout.Priority;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;
import javafx.scene.input.MouseEvent;

public class MenuController implements Initializable {

    @FXML private VBox menuListContainer;
    @FXML private HBox categoryContainer;

    private final List<MenuItem> allItems = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadMenuFromDatabase();
        setupCategories();
        renderItems(allItems); 
    }

    @FXML
    private void handleBackToHome(MouseEvent event) {
        try {
            // "false" forces the dashboard to reload fresh
            SceneManager.switchTo("resources/views/dashboard.fxml", false);
        } catch (IOException ex) {
            Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, "Failed to navigate back", ex);
        }
    }

    private void loadMenuFromDatabase() {
        allItems.clear();
        try (Connection conn = DatabaseConnection.getConnect()) {
            String query = "SELECT * FROM menu_items WHERE quantity > 0"; 
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                allItems.add(new MenuItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"), 
                    rs.getInt("quantity"),
                    rs.getDouble("price"),
                    rs.getString("image_path")
                ));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MenuController.class.getName()).log(Level.SEVERE, "Database error", ex);
        }
    }

    private void setupCategories() {
        categoryContainer.getChildren().clear();
        String[] categories = {"All", "Drinks", "Main Meal", "Swallow", "Protein", "Snacks"};
        
        for (String cat : categories) {
            Button btn = new Button(cat);
            // Uses the CSS class for hover colors and no blue focus box!
            btn.getStyleClass().add("category-btn");
            btn.setOnAction(e -> filterByCategory(cat));
            categoryContainer.getChildren().add(btn);
        }
    }

    private void filterByCategory(String category) {
        if (category.equals("All")) {
            renderItems(allItems);
        } else {
            List<MenuItem> filteredItems = allItems.stream()
                .filter(item -> category.equalsIgnoreCase(item.getCategory()))
                .collect(Collectors.toList());
            renderItems(filteredItems);
        }
    }

    private void renderItems(List<MenuItem> items) {
        menuListContainer.getChildren().clear();
        if (items.isEmpty()) {
            Label empty = new Label("No items found in this category.");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setAlignment(Pos.CENTER);
            menuListContainer.getChildren().add(empty);
            return;
        }

        for (MenuItem item : items) {
            menuListContainer.getChildren().add(buildCard(item));
        }
    }

    private VBox buildCard(MenuItem item) {
        VBox wrapper = new VBox();
        wrapper.setStyle("-fx-padding: 0 0 10 0;");

        HBox card = new HBox(14);
        card.setStyle("-fx-background-color: #A52A2A; -fx-padding: 10; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 5, 0, 0, 2);");
        card.setAlignment(Pos.CENTER_LEFT);

        StackPane imageBox = new StackPane();
        imageBox.setMinSize(72, 72);
        imageBox.setMaxSize(72, 72);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(72);
        imageView.setFitHeight(72);
        imageView.setPreserveRatio(true);

        try {
            if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
                URL imageUrl = getClass().getResource(item.getImagePath());
                if (imageUrl != null) {
                    imageView.setImage(new Image(imageUrl.toExternalForm(), true));
                }
            }
        } catch (Exception ignored) { }
        
        imageBox.getChildren().add(imageView);

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(item.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        name.setWrapText(true);

        Label qty = new Label("Stock: " + item.getQuantity());
        qty.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 11px;");

        info.getChildren().addAll(name, qty);

        VBox rightCol = new VBox(6);
        rightCol.setAlignment(Pos.CENTER_RIGHT);

        Label price = new Label("₦" + String.format("%,.2f", item.getPrice()));
        price.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        Button addBtn = new Button("Add +");
        addBtn.setStyle("-fx-background-color: #F5F5DC; -fx-text-fill: #A52A2A; -fx-font-weight: bold; -fx-border-radius: 15; -fx-background-radius: 15; -fx-cursor: hand;");
        
        addBtn.setOnAction(e -> handleAddToCart(item));

        rightCol.getChildren().addAll(price, addBtn);
        card.getChildren().addAll(imageBox, info, rightCol);
        wrapper.getChildren().add(card);
        return wrapper;
    }

    private void handleAddToCart(MenuItem item) {
        Cart.getInstance().addItem(item);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cart Updated");
        alert.setHeaderText(null);
        alert.setContentText(item.getName() + " was added to your cart!");
        alert.show();
    }

    public static class MenuItem {
        private final int id;
        private final String name;
        private final String category; 
        private final int quantity;
        private final double price;
        private final String imagePath;

        public MenuItem(int id, String name, String category, int quantity, double price, String imagePath) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.price = price;
            this.imagePath = imagePath;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public String getImagePath() { return imagePath; }

        // --- CRITICAL FIX: Tells the Cart to match items by their Database ID! ---
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            MenuItem menuItem = (MenuItem) obj;
            return id == menuItem.id; 
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(id);
        }
    }
}