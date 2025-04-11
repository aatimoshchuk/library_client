package nsu.fit.controllers.category_controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.Reader;
import nsu.fit.data.access.category.Pensioner;
import nsu.fit.repository.category_repository.PensionerRepository;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.service.UserService;
import nsu.fit.util.TableColumnConfigurator;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@FxmlView("pensioner_category.fxml")
public class PensionersController extends AbstractCategoryController<Pensioner, PensionerRepository> {

    @FXML
    private TableColumn<Pensioner, String> idColumn;
    @FXML
    private TableColumn<Pensioner, String> libraryCardNumberColumn;
    @FXML
    private TableColumn<Pensioner, String> pensionCertificateNumberColumn;

    public PensionersController(FxWeaver fxWeaver, PensionerRepository entityRepository, UserService userService,
                                NotificationService notificationService, ReaderRepository readerRepository,
                                TableColumnConfigurator tableColumnConfigurator) {
        super(fxWeaver, entityRepository, userService, notificationService, readerRepository, tableColumnConfigurator);
    }

    @FXML
    public void initialize() {
        tableColumnConfigurator.configureNotEditableTextColumn(idColumn, "id");
        tableColumnConfigurator.configureEditableNumberColumn(libraryCardNumberColumn,
                "libraryCardNumber", Pensioner.class);
        tableColumnConfigurator.configureEditableTextColumn(pensionCertificateNumberColumn,
                "pensionCertificateNumber", Pensioner.class);

        initializeBase();
        initializeNavigateButtons();
    }

    @Override
    protected List<Pensioner> getEntities() {
        return null;
    }

    @Override
    protected Pensioner createEntity() {
        return new Pensioner();
    }

    @Override
    protected boolean confirmDeletion(Pensioner entity) {
        Reader reader = readerRepository.findOne(entity.getLibraryCardNumber());

        return notificationService.showConfirmationWindow("Вы действительно хотите удалить " + reader.getSurname() +
                " " + reader.getName() + " " + reader.getPatronymic() + " из числа пенсионеров?");
    }
}
