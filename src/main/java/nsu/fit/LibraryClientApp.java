package nsu.fit;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryClientApp {
    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }
}