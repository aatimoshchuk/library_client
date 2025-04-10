package nsu.fit.data.access.category;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Lecturer extends AbstractCategoryEntity {

    private String educationalInstitutionName;
    private String jobTitle;

    public Lecturer(int id, int libraryCardNumber, String educationalInstitutionName, String jobTitle) {
        this.id = id;
        this.libraryCardNumber = libraryCardNumber;
        this.educationalInstitutionName = educationalInstitutionName;
        this.jobTitle = jobTitle;
    }

    @Override
    public boolean checkEmptyFields() {
        return libraryCardNumber > 0 && educationalInstitutionName != null &&
                !educationalInstitutionName.trim().isEmpty();
    }
}
