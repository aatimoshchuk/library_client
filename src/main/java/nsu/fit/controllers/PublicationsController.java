package nsu.fit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;;
import nsu.fit.data.access.Publication;
import nsu.fit.data.access.PublicationState;
import nsu.fit.data.access.StorageLocation;
import nsu.fit.repository.HistoryEntryRepository;
import nsu.fit.repository.LiteraryWorkRepository;
import nsu.fit.repository.PublicationRepository;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.repository.StorageLocationRepository;
import nsu.fit.repository.WrittenOffPublicationRepository;
import nsu.fit.service.UserRole;
import nsu.fit.service.UserService;
import nsu.fit.utils.ObjectToMapConverter;
import nsu.fit.utils.TableColumnConfigurator;
import nsu.fit.utils.Warning;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@FxmlView("publications.fxml")
public class PublicationsController extends AbstractEntityController<Publication, PublicationRepository> {
    private final HistoryEntryRepository historyEntryRepository;
    private final WrittenOffPublicationRepository writtenOffPublicationRepository;
    private final ReaderRepository readerRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final LiteraryWorkRepository literaryWorkRepository;
    private final ObjectToMapConverter objectToMapConverter;

    @FXML
    private TableColumn<Publication, String> nomenclatureNumberColumn;
    @FXML
    private TableColumn<Publication, String> titleColumn;
    @FXML
    private TableColumn<Publication, String> publisherColumn;
    @FXML
    private TableColumn<Publication, String> receiptDateColumn;
    @FXML
    private TableColumn<Publication, String> yearOfPrintingColumn;
    @FXML
    private TableColumn<Publication, String> categoryColumn;
    @FXML
    private TableColumn<Publication, String> ageRestrictionColumn;
    @FXML
    private TableColumn<Publication, String> storageLocationIdColumn;
    @FXML
    private TableColumn<Publication, String> stateColumn;
    @FXML
    private TableColumn<Publication, Boolean> permissionToIssueColumn;
    @FXML
    private TableColumn<Publication, String> daysForReturnColumn;

    @FXML
    private Button markPublicationAsReturnedButton;
    @FXML
    private Button markPublicationAsWrittenOffButton;
    @FXML
    private Button getReaderWithPublicationButton;
    @FXML
    private Button getStorageLocationInfoButton;
    @FXML
    private Button getAllowedCategoriesButton;
    @FXML
    private Button getLiteraryWorksButton;

    @FXML
    private TextField startDateField;
    @FXML
    private TextField endDateField;

    public PublicationsController(FxWeaver fxWeaver, PublicationRepository entityRepository, UserService userService, NotificationService notificationService, TableColumnConfigurator tableColumnConfigurator, HistoryEntryRepository historyEntryRepository, WrittenOffPublicationRepository writtenOffPublicationRepository, ReaderRepository readerRepository, StorageLocationRepository storageLocationRepository, LiteraryWorkRepository literaryWorkRepository, ObjectToMapConverter objectToMapConverter) {
        super(fxWeaver, entityRepository, userService, notificationService, tableColumnConfigurator);
        this.historyEntryRepository = historyEntryRepository;
        this.writtenOffPublicationRepository = writtenOffPublicationRepository;
        this.readerRepository = readerRepository;
        this.storageLocationRepository = storageLocationRepository;
        this.literaryWorkRepository = literaryWorkRepository;
        this.objectToMapConverter = objectToMapConverter;
    }

    public void markPublicationAsReturned(Publication publication) {
        int numberOfDaysOverdue = historyEntryRepository.getNumberOfDaysOverdue(publication);
        historyEntryRepository.markPublicationAsReturned(publication);

        if (numberOfDaysOverdue > 0) {
            notificationService.showWarning(new Warning(
                    "Срок возврата издания истек " + numberOfDaysOverdue + " дней назад!",
                    "Издание было успешно возвращено."));
        } else {
            notificationService.showNotification("Издание было успешно возвращено.");
        }

        loadData();
        actionPanel.setVisible(false);
    }

    public void markPublicationAsWrittenOff(Publication publication) {
        boolean confirmation = notificationService.showConfirmationWindow("Вы действительно хотите списать издание \"" +
                publication.getTitle() + "\"?");

        if (confirmation) {
            writtenOffPublicationRepository.markPublicationAsWrittenOff(publication);
            notificationService.showNotification("Издание было успешно списано.");
            loadData();
            actionPanel.setVisible(false);
        }
    }

    public void getReaderWithPublication(Publication publication) {
        List<Map<String, Object>> result =
                readerRepository.getReadersWithPublication(publication);

        if (result.isEmpty()) {
            notificationService.showNotification("Читатель не найден. Издание либо находится в библиотеке, либо " +
                    "списано.");
        } else if (result.size() > 1) {
            notificationService.showWarning(new Warning("Ошибка сервера", "У издания больше одного хозяина"));
        } else {
            notificationService.showResultInStringView(result.get(0));
        }
    }

    public void getStorageLocationInfo(Publication publication) {
        StorageLocation storageLocation = storageLocationRepository.findOne(publication.getStorageLocationID());

        if (storageLocation != null) {
            notificationService.showResultInStringView(objectToMapConverter.convert(storageLocation));
        } else {
            notificationService.showNotification("Место хранения с таким ID не найдено.");
        }
    }

    public void getAllowedCategories(Publication publication) {
        FxControllerAndView<PublicationPermissionDialogController, Node> controllerAndView =
                fxWeaver.load(PublicationPermissionDialogController.class);
        PublicationPermissionDialogController controller = controllerAndView.getController();
        controller.setPublication(publication);
        controller.loadData();

        Stage stage = new Stage();
        Parent root = (Parent) controllerAndView.getView().orElse(null);
        stage.setScene(new Scene(root));

        stage.setTitle("Разрешенные категории для издания");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public void getPublicationsThatWereReceiptDuringThePeriod(ActionEvent actionEvent) {
        if (!validateDate(startDateField.getText()) || !validateDate(endDateField.getText())) {
            return;
        }

        List<Map<String, Object>> result = entityRepository.getPublicationsThatWereReceiptDuringThePeriod(
                startDateField.getText(),
                endDateField.getText());

        if (result.isEmpty()) {
            notificationService.showNotification("Список изданий пуст.");
        } else {
            notificationService.showResultsInTableView(result);
        }
    }

    public void getPublicationsThatWereWrittenOffDuringThePeriod(ActionEvent actionEvent) {
        if (!validateDate(startDateField.getText()) || !validateDate(endDateField.getText())) {
            return;
        }

        List<Map<String, Object>> result = entityRepository.getPublicationsThatWereWrittenOffDuringThePeriod(
                startDateField.getText(),
                endDateField.getText());

        if (result.isEmpty()) {
            notificationService.showNotification("Список изданий пуст.");
        } else {
            notificationService.showResultsInTableView(result);
        }
    }

    public void getLiteraryWorks(Publication publication) {
        List<Map<String, Object>> result =
                literaryWorkRepository.getLiteraryWorksIncludedInThePublication(publication);

        if (result.isEmpty()) {
            notificationService.showNotification("Литературные произведения, включенные в данное издание, не найдены.");
        } else {
            notificationService.showResultsInTableView(result);
        }
    }

    @Override
    public void initialize() {
        List<String> categories = entityRepository.loadPublicationCategories();

        tableColumnConfigurator.configureNotEditableTextColumn(nomenclatureNumberColumn, "id");
        tableColumnConfigurator.configureEditableTextColumn(titleColumn, "title", Publication.class);
        tableColumnConfigurator.configureEditableTextColumn(publisherColumn, "publisher", Publication.class);
        tableColumnConfigurator.configureEditableTextColumn(receiptDateColumn, "receiptDate", Publication.class);
        tableColumnConfigurator.configureEditableNumberColumn(yearOfPrintingColumn, "yearOfPrinting", Publication.class);
        tableColumnConfigurator.configureDropDownColumn(categoryColumn, "category", Publication.class, categories);
        tableColumnConfigurator.configureEditableNumberColumn(ageRestrictionColumn, "ageRestriction", Publication.class);
        tableColumnConfigurator.configureEditableNumberColumn(storageLocationIdColumn, "storageLocationID", Publication.class);
        tableColumnConfigurator.configureNotEditableEnumColumn(stateColumn, "state", Publication.class);
        tableColumnConfigurator.configureCheckBoxColumn(permissionToIssueColumn, "permissionToIssue", Publication.class);
        tableColumnConfigurator.configureEditableNumberColumn(daysForReturnColumn, "daysForReturn", Publication.class);

        initializeBase();
    }

    @Override
    protected void applyUserPermissions() {
        UserRole userRole = userService.getUserRole();

        if (userRole.equals(UserRole.ADMIN_FOND) || userRole.equals(UserRole.ADMIN_LIBRARY)) {
            entitiesTable.setEditable(true);

            addButton.setVisible(true);
            saveButton.setVisible(true);
        }

        if (userRole.equals(UserRole.ADMIN_FOND)) {
            deleteButton.setVisible(true);
        }
    }

    @Override
    protected Publication createEntity() {
        return new Publication();
    }

    @Override
    protected boolean confirmDeletion(Publication entity) {
        return notificationService.showConfirmationWindow("Вы действительно хотите удалить \"" + entity.getTitle() +
                "\" из числа изданий?");
    }

    @Override
    protected void setCustomButtonActions() {
        if (selectedEntity.getState().equals(PublicationState.ISSUED)) {
            markPublicationAsReturnedButton.setVisible(true);
            markPublicationAsWrittenOffButton.setVisible(false);
            markPublicationAsReturnedButton.setOnAction(e -> markPublicationAsReturned(selectedEntity));

            getStorageLocationInfoButton.setVisible(true);
            getStorageLocationInfoButton.setOnAction(e -> getStorageLocationInfo(selectedEntity));

            getReaderWithPublicationButton.setVisible(true);
            getReaderWithPublicationButton.setOnAction(e -> getReaderWithPublication(selectedEntity));
        } else if (selectedEntity.getState().equals(PublicationState.AVAILABLE)) {
            markPublicationAsWrittenOffButton.setVisible(true);
            markPublicationAsReturnedButton.setVisible(false);
            markPublicationAsWrittenOffButton.setOnAction(e -> markPublicationAsWrittenOff(selectedEntity));

            getStorageLocationInfoButton.setVisible(true);
            getStorageLocationInfoButton.setOnAction(e -> getStorageLocationInfo(selectedEntity));

            getReaderWithPublicationButton.setVisible(true);
            getReaderWithPublicationButton.setOnAction(e -> getReaderWithPublication(selectedEntity));
        } else {
            markPublicationAsWrittenOffButton.setVisible(false);
            markPublicationAsReturnedButton.setVisible(false);

            getStorageLocationInfoButton.setVisible(false);
            getReaderWithPublicationButton.setVisible(false);
        }

        if (!selectedEntity.getPermissionToIssue().get()) {
            getAllowedCategoriesButton.setVisible(true);
            getAllowedCategoriesButton.setOnAction(e -> getAllowedCategories(selectedEntity));
        } else {
            getAllowedCategoriesButton.setVisible(false);
        }

        getLiteraryWorksButton.setOnAction(e -> getLiteraryWorks(selectedEntity));
    }
}
