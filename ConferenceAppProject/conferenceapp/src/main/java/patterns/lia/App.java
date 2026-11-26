package patterns.lia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import patterns.lia.db.DatabaseInitializer;
import patterns.lia.db.DatabaseManager;
import patterns.lia.utilities.LanguageManager;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseManager db = DatabaseManager.getDefault();
        DatabaseInitializer.initializeIfMissing(db);

        LanguageManager lang = LanguageManager.get();
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/login.fxml"), lang.getBundle());
        Scene scene = new Scene(loader.load(), 450, 520);
        stage.setTitle(lang.get("login.title"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
