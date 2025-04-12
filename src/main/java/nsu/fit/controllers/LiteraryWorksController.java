package nsu.fit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.LiteraryWork;
import nsu.fit.repository.LiteraryWorkRepository;
import nsu.fit.repository.PublicationRepository;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.service.UserRole;
import nsu.fit.service.UserService;
import nsu.fit.utils.TableColumnConfigurator;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@FxmlView("literary_works.fxml")
public class LiteraryWorksController extends AbstractEntityController<LiteraryWork, LiteraryWorkRepository> {
    private final PublicationRepository publicationRepository;
    private final ReaderRepository readerRepository;

    @FXML
    private TableColumn<LiteraryWork, String> idColumn;
    @FXML
    private TableColumn<LiteraryWork, String> titleColumn;
    @FXML
    private TableColumn<LiteraryWork, String> authorColumn;
    @FXML
    private TableColumn<LiteraryWork, String> writingYearColumn;
    @FXML
    private TableColumn<LiteraryWork, String> categoryColumn;

    @FXML
    private TextField startDateField;
    @FXML
    private TextField endDateField;
    @FXML
    private TextField authorNameField;
    @FXML
    private TextField maxCountField;

    @FXML
    private Button getPublicationsWithLiteraryWorkButton;
    @FXML
    private Button getReadersWithLiteraryWorkButton;
    @FXML
    private Button getReadersWhoReceivedLiteraryWorkDuringThePeriodButton;

    public LiteraryWorksController(FxWeaver fxWeaver, LiteraryWorkRepository entityRepository, UserService userService,
                                   NotificationService notificationService, TableColumnConfigurator tableColumnConfigurator, PublicationRepository publicationRepository, ReaderRepository readerRepository) {
        super(fxWeaver, entityRepository, userService, notificationService, tableColumnConfigurator);
        this.publicationRepository = publicationRepository;
        this.readerRepository = readerRepository;
    }

    @FXML
    public void initialize() {
        List<String> categories = entityRepository.loadLiteraryWorkCategories();

        tableColumnConfigurator.configureNotEditableTextColumn(idColumn, "id");
        tableColumnConfigurator.configureEditableTextColumn(titleColumn, "title", LiteraryWork.class);
        tableColumnConfigurator.configureEditableTextColumn(authorColumn, "author", LiteraryWork.class);
        tableColumnConfigurator.configureEditableNumberColumn(writingYearColumn, "writingYear", LiteraryWork.class);
        tableColumnConfigurator.configureDropDownColumn(categoryColumn, "category", LiteraryWork.class, categories);

        initializeBase();
    }

    public void getTheMostPopularLiteraryWorks(ActionEvent actionEvent) {
        if (!validateNumber(maxCountField.getText())) {
            return;
        }
        List<Map<String, Object>> result =
                entityRepository.getTheMostPopularLiteraryWorks(Integer.parseInt(maxCountField.getText()));

        if (result.isEmpty()) {
            notificationService.showNotification("Список произведений пуст.");
        } else {
            notificationService.showResultsInTableView(result);
        }
    }

    public void getPublicationsWithTheAuthorsWorks(ActionEvent actionEvent) {
        List<Map<String, Object>> result =
                publicationRepository.getPublicationsWithTheAuthorsWorks(authorNameField.getText());

        if (result.isEmpty()) {
            notificationService.showNotification("Список изданий пуст.");
        } else {
            notificationService.showResultsInTableView(result);
        }

    }

    public void getPublicationsWithLiteraryWork(LiteraryWork literaryWork) {
        List<Map<String, Object>> result =
                publicationRepository.getPublicationsWithLiteraryWork(literaryWork);

        if (result.isEmpty()) {
            notificationService.showNotification("Список изданий пуст.");
        } else {
            notificationService.showResultsInTableView(result);
        }
    }

    public void getReadersWithLiteraryWork(LiteraryWork literaryWork) {
        List<Map<String, Object>> result =
                readerRepository.getReadersWithLiteraryWork(literaryWork);

        if (result.isEmpty()) {
            notificationService.showNotification("Список читателей пуст.");
        } else {
            notificationService.showResultsInTableView(result);
        }
    }

    public void getReadersWhoReceivedLiteraryWorkDuringThePeriod(LiteraryWork literaryWork) {
        if (!validateDate(startDateField.getText()) || !validateDate(endDateField.getText())) {
            return;
        }

        List<Map<String, Object>> result =
                readerRepository.getReadersWhoReceivedLiteraryWorkDuringThePeriod(literaryWork,
                        startDateField.getText(), endDateField.getText());

        if (result.isEmpty()) {
            notificationService.showNotification("Список читателей пуст.");
        } else {
            notificationService.showResultsInTableView(result);
        }
    }

    @Override
    protected void applyUserPermissions() {
        if (userService.getUserRole().equals(UserRole.ADMIN_FOND)) {
            entitiesTable.setEditable(true);

            addButton.setVisible(true);
            saveButton.setVisible(true);
            deleteButton.setVisible(true);
        }
    }

    @Override
    protected void setCustomButtonActions() {
        getPublicationsWithLiteraryWorkButton.setOnAction(e -> getPublicationsWithLiteraryWork(selectedEntity));
        getReadersWhoReceivedLiteraryWorkDuringThePeriodButton
                .setOnAction(e -> getReadersWhoReceivedLiteraryWorkDuringThePeriod(selectedEntity));
        getReadersWithLiteraryWorkButton.setOnAction(e -> getReadersWithLiteraryWork(selectedEntity));
    }

    @Override
    protected LiteraryWork createEntity() {
        return new LiteraryWork();
    }

    @Override
    protected boolean confirmDeletion(LiteraryWork entity) {
        return notificationService.showConfirmationWindow("Вы действительно хотите удалить \"" + entity.getTitle() +
                "\" из числа произведений?");
    }
}
