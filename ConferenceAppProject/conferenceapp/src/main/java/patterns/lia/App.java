package patterns.lia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import patterns.lia.db.DatabaseInitializer;
import patterns.lia.db.DatabaseManager;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseManager db = DatabaseManager.getDefault();
        DatabaseInitializer.initializeIfMissing(db);

        FXMLLoader loader = new FXMLLoader(App.class.getResource("/main.fxml"));
        Scene scene = new Scene(loader.load(), 450, 450);
        stage.setTitle("Hello World");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
