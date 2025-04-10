package nsu.fit.controllers.category_controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.category.Schoolchild;
import nsu.fit.data.access.category.ScientificWorker;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.repository.category_repository.ScientificWorkerRepository;
import nsu.fit.service.UserService;
import nsu.fit.util.TableColumnConfigurator;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@FxmlView("scientific_worker_category.fxml")
public class ScientificWorkersController extends AbstractCategoryController<ScientificWorker,
        ScientificWorkerRepository> {

    @FXML
    private TableColumn<ScientificWorker, String> idColumn;
    @FXML
    private TableColumn<ScientificWorker, String> libraryCardNumberColumn;
    @FXML
    private TableColumn<ScientificWorker, String> organizationNameColumn;
    @FXML
    private TableColumn<ScientificWorker, String> scientificTopicColumn;

    @FXML
    private TextField organizationNameField;
    @FXML
    private TextField scientificTopicField;

    public ScientificWorkersController(FxWeaver fxWeaver, ScientificWorkerRepository entityRepository,
                                       UserService userService, NotificationService notificationService,
                                       ReaderRepository readerRepository, TableColumnConfigurator tableColumnConfigurator) {
        super(fxWeaver, entityRepository, userService, notificationService, readerRepository, tableColumnConfigurator);
    }

    @FXML
    public void initialize() {
        tableColumnConfigurator.configureNotEditableTextColumn(idColumn, "id");
        tableColumnConfigurator.configureEditableNumberColumn(libraryCardNumberColumn, "libraryCardNumber",
                ScientificWorker.class);
        tableColumnConfigurator.configureEditableTextColumn(organizationNameColumn, "organizationName",
                ScientificWorker.class);
        tableColumnConfigurator.configureEditableTextColumn(scientificTopicColumn, "scientificTopic",
                ScientificWorker.class);

        initializeBase();
        initializeNavigateButtons();
    }

    @Override
    public List<ScientificWorker> getEntities() {
        return entityRepository.findEntities(organizationNameField.getText().trim(),
                scientificTopicField.getText().trim());
    }

    @Override
    protected ScientificWorker createEntity() {
        return new ScientificWorker();
    }
}
