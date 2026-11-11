package cafeteriaapp;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage primaryStage;
    
    // We completely removed the HashMap cache that was freezing your pages!

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchTo(String fxmlPath, boolean resizable) throws IOException {
        // Now, it loads a fresh, brand-new version of the page EVERY time.
        // This guarantees your cart will always read the most up-to-date data.
        Parent root = FXMLLoader.load(SceneManager.class.getResource(fxmlPath));
        
        primaryStage.setResizable(resizable);
        primaryStage.getScene().setRoot(root);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen(); // re-center after resize so it doesn't jump to a corner
    }
}