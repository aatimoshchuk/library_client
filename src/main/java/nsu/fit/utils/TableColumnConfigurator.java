package nsu.fit.utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.fit.utils.warning.Warning;
import nsu.fit.utils.warning.WarningType;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TableColumnConfigurator {
    private static final String METHOD_NOT_EXIST = "Method with name {} doesn't exist";
    private static final String NUMBER_FORMAT_EXCEPTION = "The value entered in the field {} is of the wrong type";

    private final NotificationService notificationService;

    public <S> void configureEditableTextColumn(
            TableColumn<S, String> column,
            String propertyName,
            Class<S> rowClass
    ) {
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setCellFactory(TextFieldTableCell.forTableColumn());

        column.setOnEditCommit(event -> {
            S rowValue = event.getRowValue();
            String newValue = event.getNewValue();

            try {
                rowClass.getMethod(getSetterName(propertyName), String.class).invoke(rowValue, newValue);
            } catch (Exception e) {
                log.error(METHOD_NOT_EXIST, getSetterName(propertyName));
            }
        });
    }

    public <S> void configureEditableNumberColumn (
            TableColumn<S, String> column,
            String propertyName,
            Class<S> rowClass
    ) {

        column.setCellValueFactory(cellData -> {
            try {
                S row = cellData.getValue();
                Method getter = row.getClass().getMethod(getGetterName(propertyName));
                Object value = getter.invoke(row);
                return new SimpleStringProperty(value != null ? String.valueOf(value) : "");
            } catch (Exception e) {
                log.error(METHOD_NOT_EXIST, getGetterName(propertyName));
                return new SimpleStringProperty("");
            }
        });

        column.setCellFactory(TextFieldTableCell.forTableColumn());

        column.setOnEditCommit(event -> {
            try {
                Method setter = rowClass.getMethod(getSetterName(propertyName), Integer.class);
                setter.invoke(event.getRowValue(), Integer.parseInt(event.getNewValue()));
            } catch (NumberFormatException e) {
                log.error(NUMBER_FORMAT_EXCEPTION, propertyName);

                if (!event.getOldValue().isEmpty()) {
                    notificationService.showWarning(new Warning(WarningType.SAVING_ERROR,
                            "Значение данного поля должно представлять из себя число!"));
                }
            } catch (Exception e) {
                log.error(METHOD_NOT_EXIST, getSetterName(propertyName));
            }
        });
    }

    public <S> void configureDropDownColumn(TableColumn<S, String> column,
                                            String propertyName,
                                            Class<S> rowClass,
                                            List<String> list) {
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(list)));

        column.setOnEditCommit(event -> {
            S rowValue = event.getRowValue();
            String newValue = event.getNewValue();

            try {
                rowClass.getMethod(getSetterName(propertyName), String.class).invoke(rowValue, newValue);
            } catch (Exception e) {
                log.error(METHOD_NOT_EXIST, getSetterName(propertyName));
            }
        });
    }

    public <S> void configureCheckBoxColumn(TableColumn<S, Boolean> column,
                                            String propertyName,
                                            Class<S> rowClass) {
        column.setCellValueFactory(cellData -> {
            try {
                Method getter = rowClass.getMethod(getGetterName(propertyName));
                return (BooleanProperty) getter.invoke(cellData.getValue());
            } catch (Exception e) {
                log.error(METHOD_NOT_EXIST, getGetterName(propertyName));
                return new SimpleBooleanProperty(false);
            }
        });

        column.setCellFactory(CheckBoxTableCell.forTableColumn(index -> {
            S rowItem = column.getTableView().getItems().get(index);
            try {
                Method getter = rowClass.getMethod(getGetterName(propertyName));
                return (BooleanProperty) getter.invoke(rowItem);
            } catch (Exception e) {
                log.error(METHOD_NOT_EXIST, getGetterName(propertyName));
                return new SimpleBooleanProperty(false);
            }
        }));
    }
    public <S> void configureNotEditableTextColumn(TableColumn<S, String> column,
                                                   String propertyName) {
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
    }

    public <S> void configureNotEditableEnumColumn(
            TableColumn<S, String> column,
            String propertyName,
            Class<S> rowClass
    ) {
        column.setCellValueFactory(cellData -> {
            S rowItem = cellData.getValue();
            try {
                Method getter = rowClass.getMethod(getGetterName(propertyName));
                Object enumValue = getter.invoke(rowItem);

                String displayValue = (enumValue != null) ? enumValue.toString() : "";
                return new ReadOnlyStringWrapper(displayValue);
            } catch (Exception e) {
                log.error(METHOD_NOT_EXIST, getGetterName(propertyName));
                return new ReadOnlyStringWrapper("");
            }
        });
    }

    private String getSetterName(String propertyName) {
        return "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    }

    private String getGetterName(String propertyName) {
        return "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    }
}
