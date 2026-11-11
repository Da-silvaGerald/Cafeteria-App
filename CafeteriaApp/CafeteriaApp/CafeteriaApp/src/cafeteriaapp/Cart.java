package cafeteriaapp;

import cafeteriaapp.resources.controllers.MenuController.MenuItem;
import java.util.HashMap;
import java.util.Map;

public class Cart {
    
    // Singleton instance so every page shares the exact same cart
    private static Cart instance;
    private final Map<MenuItem, Integer> items = new HashMap<>();

    private Cart() {}

    public static Cart getInstance() {
        if (instance == null) {
            instance = new Cart();
        }
        return instance;
    }

    public void addItem(MenuItem item) {
        items.put(item, items.getOrDefault(item, 0) + 1);
        // TERMINAL TRACKER: Prints to your NetBeans output when an item is added
        System.out.println(">> ITEM ADDED: " + item.getName() + " | CURRENT CART SIZE: " + items.size() + " | CART ID: " + this.hashCode());
    }

    public void setQuantity(MenuItem item, int newQuantity) {
        if (newQuantity <= 0) {
            items.remove(item); 
        } else {
            items.put(item, newQuantity);
        }
    }

    public Map<MenuItem, Integer> getItems() {
        return items;
    }

    public double getTotalCost() {
        double total = 0.0;
        for (Map.Entry<MenuItem, Integer> entry : items.entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
        }
        return total;
    }

    public void clear() {
        items.clear();
    }
}