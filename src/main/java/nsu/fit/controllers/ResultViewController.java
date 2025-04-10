package nsu.fit.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@FxmlView("result_view.fxml")
public class ResultViewController {
    @FXML
    private TableView<Map<String, Object>> tableView;

    public void loadData(List<Map<String, Object>> data) {
        tableView.getColumns().clear();
        tableView.getItems().clear();

        if (data.isEmpty()) return;

        Map<String, Object> firstRow = data.get(0);

        for (String columnName : firstRow.keySet()) {
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnName);
            column.setCellValueFactory(cellData -> {
                Object value = cellData.getValue().get(columnName);
                return new SimpleStringProperty(value != null ? value.toString() : "");
            });

            column.setPrefWidth(getColumnWidth(columnName, data));
            tableView.getColumns().add(column);
        }

        tableView.getItems().addAll(data);
    }

    private double getColumnWidth(String columnName, List<Map<String, Object>> data) {
        double maxWidth = computeTextWidth(columnName) + 20;

        for (Map<String, Object> row : data) {
            Object value = row.get(columnName);
            if (value != null) {
                double textWidth = computeTextWidth(value.toString());
                maxWidth = Math.max(maxWidth, textWidth + 20);
            }
        }

        return maxWidth;
    }

    private double computeTextWidth(String text) {
        javafx.scene.text.Text tempText = new javafx.scene.text.Text(text);
        tempText.setFont(javafx.scene.text.Font.font(18));
        return tempText.getLayoutBounds().getWidth();
    }
}
