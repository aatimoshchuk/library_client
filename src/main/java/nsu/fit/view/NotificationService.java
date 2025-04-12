package nsu.fit.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxWeaver;
import nsu.fit.controllers.ResultViewController;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {
    public static final String DEFAULT_WARNING_HEADER = "Невозможно выполнить запрос";
    private final FxWeaver fxWeaver;
    public void showWarning(String message, String header) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информационное сообщение");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showConfirmationWindow(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle("Подтверждение");
        alert.setHeaderText(null);

        return alert.showAndWait().orElse(ButtonType.CANCEL).equals(ButtonType.YES);
    }

    public void showResultsInTableView(List<Map<String, Object>> result) {
        Stage stage = new Stage();
        Parent root = fxWeaver.loadView(ResultViewController.class);
        stage.setScene(new Scene(root));
        stage.setTitle("Результаты запроса");
        stage.setResizable(false);

        ResultViewController controller = fxWeaver.getBean(ResultViewController.class);
        controller.loadData(result);

        stage.show();
    }

    public void showResultInStringView(Map<String, Object> result) {
        Stage stage = new Stage();
        Parent root = fxWeaver.loadView(ResultViewController.class);
        stage.setScene(new Scene(root));
        stage.setTitle("Результат запроса");
        stage.setResizable(false);

        VBox vbox = new VBox(result.size());
        vbox.setPadding(new Insets(15));
        vbox.setAlignment(Pos.CENTER_LEFT);

        for (String fieldName : result.keySet()) {
            vbox.getChildren().add(createInfoRow(fieldName + ":", String.valueOf(result.get(fieldName))));
        }

        Scene scene = new Scene(vbox, 350, 150);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private HBox createInfoRow(String labelText, String value) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-font-family: 'Hlebozavod Medium'");
        label.setMinWidth(200);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 14px; -fx-font-family: 'Advent Pro'");

        HBox hbox = new HBox(10, label, valueLabel);
        hbox.setAlignment(Pos.CENTER_LEFT);
        return hbox;
    }
}