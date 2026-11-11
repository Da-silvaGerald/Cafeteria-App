package cafeteriaapp.resources.controllers;

import cafeteriaapp.SceneManager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class HomeTabController {

    @FXML
    private void navigateToMenuPage(MouseEvent event) {
        try {
            SceneManager.switchTo("resources/views/menu.fxml", true);
        } catch (IOException ex) {
            Logger.getLogger(HomeTabController.class.getName()).log(Level.SEVERE, "Failed to load Menu page", ex);
        }
    }

    @FXML
    private void navigateToWalletPage(MouseEvent event) {
        try {
            SceneManager.switchTo("resources/views/wallet.fxml", true);
        } catch (IOException ex) {
            Logger.getLogger(HomeTabController.class.getName()).log(Level.SEVERE, "Failed to load Wallet page", ex);
        }
    }

    @FXML
    private void navigateToOrderHistoryPage(MouseEvent event) {
        try {
            SceneManager.switchTo("resources/views/orderhistory.fxml", true);
        } catch (IOException ex) {
            Logger.getLogger(HomeTabController.class.getName()).log(Level.SEVERE, "Failed to load Order History page", ex);
        }
    }

    @FXML
    private void navigateToCartPage(MouseEvent event) {
        try {
            SceneManager.switchTo("resources/views/cart.fxml", true);
        } catch (IOException ex) {
            Logger.getLogger(HomeTabController.class.getName()).log(Level.SEVERE, "Failed to load Cart page", ex);
        }
    }
}