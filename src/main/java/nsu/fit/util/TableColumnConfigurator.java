package nsu.fit.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;

@Service
public class TableColumnConfigurator {
    private static Logger logger = LoggerFactory.getLogger(TableColumnConfigurator.class);

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
                logger.error("Method with name " + getSetterName(propertyName) + " doesn't exist");
            }
        });
    }

    public <S> void configureEditableNumberColumn(
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
                logger.error("Method with name " + getGetterName(propertyName) + " doesn't exist");
                return new SimpleStringProperty("");
            }
        });

        column.setCellFactory(TextFieldTableCell.forTableColumn());

        column.setOnEditCommit(event -> {
            try {
                Method setter = rowClass.getMethod(getSetterName(propertyName), Integer.class);
                setter.invoke(event.getRowValue(), Integer.parseInt(event.getNewValue()));
            } catch (Exception e) {
                logger.error("Method with name " + getSetterName(propertyName) + " doesn't exist");
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
                logger.error("Method with name " + getSetterName(propertyName) + " doesn't exist");
            }
        });
    }

    public <S> void configureCheckBoxColumn(TableColumn<S, Boolean> column,
                                            String propertyName,
                                            Class<S> rowClass) {
        column.setCellValueFactory(cellData -> {
            try {
                S row = cellData.getValue();
                Method getter = row.getClass().getMethod("is" + Character.toUpperCase(propertyName.charAt(0)) +
                        propertyName.substring(1));
                Object value = getter.invoke(row);
                if (value instanceof ObservableValue) {
                    return (ObservableValue<Boolean>) value;
                } else if (value instanceof Boolean) {
                    return new SimpleBooleanProperty((Boolean) value);
                } else {
                    return new SimpleBooleanProperty(false);
                }
            } catch (Exception e) {
                logger.error("Method with name " + getGetterName(propertyName) + " doesn't exist");
                return new SimpleBooleanProperty(false);
            }
        });

        column.setCellFactory(tc -> new CheckBoxTableCell<>(index -> {
            S row = column.getTableView().getItems().get(index);
            try {
                Method getter = row.getClass().getMethod("is" + Character.toUpperCase(propertyName.charAt(0)) +
                        propertyName.substring(1));
                Object value = getter.invoke(row);

                if (value instanceof BooleanProperty) {
                    return (BooleanProperty) value;
                } else if (value instanceof Boolean) {
                    return new SimpleBooleanProperty((Boolean) value);
                }
            } catch (Exception e) {
                logger.error("Failed to access property: " + propertyName, e);
            }

            return new SimpleBooleanProperty(false);
        }));

        column.setEditable(true);
    }
    public <S> void configureNotEditableTextColumn(TableColumn<S, String> column,
                                                   String propertyName) {
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
    }

    private String getSetterName(String propertyName) {
        return "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    }

    private String getGetterName(String propertyName) {
        return "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    }
}
