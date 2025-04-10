package nsu.fit.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import nsu.fit.controllers.ResultViewController;
import nsu.fit.data.access.Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    @Autowired
    private FxWeaver fxWeaver;
    public void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(null);
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

    public void showResults(List<Map<String, Object>> result) {
        Stage stage = new Stage();
        Parent root = fxWeaver.loadView(ResultViewController.class);
        stage.setScene(new Scene(root));
        stage.setTitle("Результаты запроса");
        stage.setResizable(false);

        ResultViewController controller = fxWeaver.getBean(ResultViewController.class);
        controller.loadData(result);

        stage.show();
    }

    public void showReaderInfo(Reader reader) {
        Stage stage = new Stage();
        Parent root = fxWeaver.loadView(ResultViewController.class);
        stage.setScene(new Scene(root));
        stage.setTitle("Читательский билет");
        stage.setResizable(false);

        VBox vbox = new VBox(5);
        vbox.setPadding(new Insets(15));
        vbox.setAlignment(Pos.CENTER_LEFT);

        vbox.getChildren().addAll(
                createInfoRow("Номер читательского билета:", String.valueOf(reader.getId())),
                createInfoRow("Фамилия:", reader.getSurname()),
                createInfoRow("Имя:", reader.getName()),
                createInfoRow("Отчество:", reader.getPatronymic()),
                createInfoRow("Дата рождения:", reader.getBirthDay())
        );

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