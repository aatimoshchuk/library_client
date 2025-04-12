package nsu.fit.controllers.category_controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.Reader;
import nsu.fit.data.access.category.ScientificWorker;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.repository.category_repository.ScientificWorkerRepository;
import nsu.fit.service.UserService;
import nsu.fit.utils.ObjectToMapConverter;
import nsu.fit.utils.TableColumnConfigurator;
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
                                       ReaderRepository readerRepository, TableColumnConfigurator tableColumnConfigurator,
                                       ObjectToMapConverter objectToMapConverter) {
        super(fxWeaver, entityRepository, userService, notificationService, readerRepository, tableColumnConfigurator,
                objectToMapConverter);
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

    @Override
    protected boolean confirmDeletion(ScientificWorker entity) {
        Reader reader = readerRepository.findOne(entity.getLibraryCardNumber());

        return notificationService.showConfirmationWindow("Вы действительно хотите удалить " + reader.getSurname() +
                " " + reader.getName() + " " + reader.getPatronymic() + " из числа научных работников?");
    }
}
