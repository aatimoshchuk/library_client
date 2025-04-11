package nsu.fit.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.Librarian;
import nsu.fit.data.access.Reader;
import nsu.fit.repository.LibrarianRepository;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.service.UserRole;
import nsu.fit.service.UserService;
import nsu.fit.util.TableColumnConfigurator;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@FxmlView("librarians.fxml")
public class LibrariansController extends AbstractEntityController<Librarian, LibrarianRepository>{
    private final ReaderRepository readerRepository;

    @FXML
    private TableColumn<Librarian, String> idColumn;
    @FXML
    private TableColumn<Librarian, String> surnameColumn;
    @FXML
    private TableColumn<Librarian, String> nameColumn;
    @FXML
    private TableColumn<Librarian, String> patronymicColumn;
    @FXML
    private TableColumn<Librarian, String> birthDayColumn;
    @FXML
    private TableColumn<Librarian, String> phoneNumberColumn;
    @FXML
    private TableColumn<Librarian, String> libraryColumn;
    @FXML
    private TableColumn<Librarian, String> roomNumberColumn;

    @FXML
    private TextField startDateField;
    @FXML
    private TextField endDateField;

    @FXML
    private Button getDataOnLibrarianProductivityButton;
    @FXML
    private Button getReadersWhoWereServedByTheLibrarianButton;

    public LibrariansController(FxWeaver fxWeaver, LibrarianRepository entityRepository, UserService userService,
                                NotificationService notificationService, ReaderRepository readerRepository,
                                TableColumnConfigurator tableColumnConfigurator) {
        super(fxWeaver, entityRepository, userService, notificationService, tableColumnConfigurator);
        this.readerRepository = readerRepository;
    }

    @FXML
    public void initialize() {
        tableColumnConfigurator.configureNotEditableTextColumn(idColumn, "id");
        tableColumnConfigurator.configureEditableTextColumn(surnameColumn, "surname", Librarian.class);
        tableColumnConfigurator.configureEditableTextColumn(nameColumn, "name", Librarian.class);
        tableColumnConfigurator.configureEditableTextColumn(patronymicColumn, "patronymic", Librarian.class);
        tableColumnConfigurator.configureEditableTextColumn(birthDayColumn, "birthDay", Librarian.class);
        tableColumnConfigurator.configureEditableTextColumn(phoneNumberColumn, "phoneNumber", Librarian.class);
        tableColumnConfigurator.configureEditableNumberColumn(libraryColumn, "library", Librarian.class);
        tableColumnConfigurator.configureEditableNumberColumn(roomNumberColumn, "roomNumber", Librarian.class);

        initializeBase();
    }

    public void getDataOnLibrarianProductivity(Librarian librarian) {
        if (!validateDate(startDateField.getText()) || !validateDate(endDateField.getText())) {
            return;
        }

        int productivity = entityRepository.getDataOnLibrarianProductivityDuringThePeriod(librarian, startDateField.getText(),
                endDateField.getText());
        notificationService.showNotification("Число обслуженных читателей за заданный период: " + productivity);
    }

    public void getReadersWhoWereServedByTheLibrarian(Librarian librarian) {
        if (!validateDate(startDateField.getText()) || !validateDate(endDateField.getText())) {
            return;
        }

        List<Map<String, Object>> result =
                readerRepository.getReadersWhoWereServedByTheLibrarianDuringThePeriod(librarian, startDateField.getText(),
                        endDateField.getText());

        if (result.isEmpty()) {
            notificationService.showNotification("Список обслуженных читателей пуст.");
        } else {
            notificationService.showResults(result);
        }
    }

    @Override
    protected void applyUserPermissions() {
        UserRole currUserRole =  userService.getUserRole();

        if (currUserRole.equals(UserRole.ADMIN_FOND) || currUserRole.equals(UserRole.ADMIN_LIBRARY)) {
            entitiesTable.setEditable(true);

            saveButton.setVisible(true);
            deleteButton.setVisible(true);
            addButton.setVisible(true);
        }
    }

    @Override
    protected void setCustomButtonActions() {
        getDataOnLibrarianProductivityButton.setOnAction(e -> getDataOnLibrarianProductivity(selectedEntity));
        getReadersWhoWereServedByTheLibrarianButton.setOnAction(e -> getReadersWhoWereServedByTheLibrarian(selectedEntity));
    }

    @Override
    protected Librarian createEntity() {
        return new Librarian();
    }

    @Override
    protected boolean confirmDeletion(Librarian entity) {
        return notificationService.showConfirmationWindow("Вы действительно хотите удалить " + entity.getSurname() +
                " " + entity.getName() + " " + entity.getPatronymic() + " из числа библиотекарей?");
    }
}
