package nsu.fit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.Publication;
import nsu.fit.data.access.ReaderCategoryPermission;
import nsu.fit.repository.PublicationPermissionRepository;
import nsu.fit.service.UserRole;
import nsu.fit.service.UserService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@FxmlView("permission-dialog.fxml")
@RequiredArgsConstructor
public class PublicationPermissionDialogController {
    private final PublicationPermissionRepository publicationPermissionRepository;
    private final List<CheckBox> checkBoxes = new ArrayList<>();
    private final UserService userService;

    @Setter
    private Publication publication;

    @FXML
    private VBox checkboxContainer;
    @FXML
    private Button saveButton;

    public void loadData() {
        List<ReaderCategoryPermission> categories = publicationPermissionRepository.findAllForPublication(publication);
        UserRole userRole = userService.getUserRole();

        checkboxContainer.getChildren().clear();
        checkBoxes.clear();

        for (ReaderCategoryPermission category : categories) {
            CheckBox checkBox = new CheckBox(category.getReaderCategoryName());
            checkBox.setUserData(category.getReaderCategoryID());
            checkBox.setSelected(category.isPermitted());
            checkBox.setStyle("-fx-font-size: 14px; -fx-font-family: 'Hlebozavod Medium'; -fx-text-fill: #7b6f6f" );

            if (userRole.equals(UserRole.LIBRARIAN)) {
                checkBox.setDisable(true);
            }

            checkBoxes.add(checkBox);
            checkboxContainer.getChildren().add(checkBox);
        }

        if (userRole.equals(UserRole.LIBRARIAN)) {
            saveButton.setDisable(true);

        }
    }

    @FXML
    private void onSaveClick() {
        List<Integer> permittedIDs = checkBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> (Integer) cb.getUserData())
                .collect(Collectors.toList());

        publicationPermissionRepository.savePermissions(publication, permittedIDs);

        ((Stage) checkboxContainer.getScene().getWindow()).close();
    }

    @FXML
    private void onCancelClick() {
        ((Stage) checkboxContainer.getScene().getWindow()).close();
    }
}