package nsu.fit.controllers.category_controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.Reader;
import nsu.fit.data.access.category.Schoolchild;
import nsu.fit.data.access.category.Student;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.repository.category_repository.StudentRepository;
import nsu.fit.service.UserService;
import nsu.fit.util.TableColumnConfigurator;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@FxmlView("student_category.fxml")
public class StudentsController extends AbstractCategoryController<Student, StudentRepository> {

    @FXML
    private TableColumn<Student, String> idColumn;
    @FXML
    private TableColumn<Student, String> libraryCardNumberColumn;
    @FXML
    private TableColumn<Student, String> educationalInstitutionNameColumn;
    @FXML
    private TableColumn<Student, String> facultyColumn;
    @FXML
    private TableColumn<Student, String> courseColumn;
    @FXML
    private TableColumn<Student, String> groupNumberColumn;
    @FXML
    private TableColumn<Student, String> studentCardNumberColumn;
    @FXML
    private TableColumn<Student, String> extensionDateColumn;

    @FXML
    private TextField educationalInstitutionNameField;
    @FXML
    private TextField facultyField;
    @FXML
    private TextField courseField;

    public StudentsController(FxWeaver fxWeaver, StudentRepository entityRepository, UserService userService,
                              NotificationService notificationService, ReaderRepository readerRepository,
                              TableColumnConfigurator tableColumnConfigurator) {
        super(fxWeaver, entityRepository, userService, notificationService, readerRepository, tableColumnConfigurator);
    }

    @FXML
    public void initialize() {
        tableColumnConfigurator.configureNotEditableTextColumn(idColumn, "id");
        tableColumnConfigurator.configureEditableNumberColumn(libraryCardNumberColumn, "libraryCardNumber",
                Student.class);
        tableColumnConfigurator.configureEditableTextColumn(educationalInstitutionNameColumn,
                "educationalInstitutionName", Student.class);
        tableColumnConfigurator.configureEditableTextColumn(facultyColumn, "faculty", Student.class);
        tableColumnConfigurator.configureEditableNumberColumn(groupNumberColumn, "groupNumber", Student.class);
        tableColumnConfigurator.configureEditableNumberColumn(studentCardNumberColumn, "studentCardNumber",
                Student.class);
        tableColumnConfigurator.configureEditableTextColumn(extensionDateColumn, "extensionDate",
                Student.class);

        initializeBase();
        initializeNavigateButtons();
    }

    @Override
    protected List<Student> getEntities() {
        return entityRepository.findEntities(educationalInstitutionNameField.getText().trim(),
                facultyField.getText().trim(), courseField.getText().trim());
    }

    @Override
    protected Student createEntity() {
        return new Student();
    }

    @Override
    protected boolean confirmDeletion(Student entity) {
        Reader reader = readerRepository.findOne(entity.getLibraryCardNumber());

        return notificationService.showConfirmationWindow("Вы действительно хотите удалить " + reader.getSurname() +
                " " + reader.getName() + " " + reader.getPatronymic() + " из числа студентов?");
    }
}
