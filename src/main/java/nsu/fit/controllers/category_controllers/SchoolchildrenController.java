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
import nsu.fit.repository.ReaderRepository;
import nsu.fit.repository.category_repository.SchoolchildRepository;
import nsu.fit.service.UserService;
import nsu.fit.util.TableColumnConfigurator;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@FxmlView("schoolchild_category.fxml")
public class SchoolchildrenController extends AbstractCategoryController<Schoolchild, SchoolchildRepository> {

    @FXML
    private TableColumn<Schoolchild, String> idColumn;
    @FXML
    private TableColumn<Schoolchild, String> libraryCardNumberColumn;
    @FXML
    private TableColumn<Schoolchild, String> educationalInstitutionNameColumn;
    @FXML
    private TableColumn<Schoolchild, String> gradeColumn;
    @FXML
    private TableColumn<Schoolchild, String> extensionDateColumn;

    @FXML
    private TextField educationalInstitutionNameField;
    @FXML
    private TextField gradeField;

    public SchoolchildrenController(FxWeaver fxWeaver, SchoolchildRepository entityRepository, UserService userService,
                                    NotificationService notificationService, ReaderRepository readerRepository,
                                    TableColumnConfigurator tableColumnConfigurator) {
        super(fxWeaver, entityRepository, userService, notificationService, readerRepository, tableColumnConfigurator);
    }

    @FXML
    public void initialize() {
        tableColumnConfigurator.configureNotEditableTextColumn(idColumn, "id");
        tableColumnConfigurator.configureEditableNumberColumn(libraryCardNumberColumn, "libraryCardNumber",
                Schoolchild.class);
        tableColumnConfigurator.configureEditableTextColumn(educationalInstitutionNameColumn,
                "educationalInstitutionName", Schoolchild.class);
        tableColumnConfigurator.configureEditableNumberColumn(gradeColumn, "grade", Schoolchild.class);
        tableColumnConfigurator.configureEditableTextColumn(extensionDateColumn, "extensionDate", Schoolchild.class);

        initializeBase();
        initializeNavigateButtons();
    }

    @Override
    protected List<Schoolchild> getEntities() {
        return entityRepository.findEntities(educationalInstitutionNameField.getText().trim(),
                gradeField.getText().trim());
    }

    @Override
    protected Schoolchild createEntity() {
        return new Schoolchild();
    }
}
