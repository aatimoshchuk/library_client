package nsu.fit.data.access.category;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Schoolchild extends AbstractCategoryEntity {

    private String educationalInstitutionName;
    private int grade;
    private String extensionDate;

    public Schoolchild(int id, int libraryCardNumber, String educationalInstitutionName, int grade, String extensionDate) {
        this.id = id;
        this.libraryCardNumber = libraryCardNumber;
        this.educationalInstitutionName = educationalInstitutionName;
        this.grade = grade;
        this.extensionDate = extensionDate;
    }

    @Override
    public boolean checkEmptyFields() {
        return libraryCardNumber > 0 && grade > 0 && educationalInstitutionName != null &&
                !educationalInstitutionName.trim().isEmpty() && extensionDate != null && !extensionDate.trim().isEmpty();
    }
}
