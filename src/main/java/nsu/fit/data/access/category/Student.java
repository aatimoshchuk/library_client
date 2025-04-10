package nsu.fit.data.access.category;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Student extends AbstractCategoryEntity {

    private String educationalInstitutionName;
    private String faculty;
    private int course;
    private int groupNumber;
    private int studentCardNumber;
    private String extensionDate;

    public Student(int id, int libraryCardNumber, String educationalInstitutionName, String faculty, int course,
                   int groupNumber, int studentCardNumber, String extensionDate) {
        this.id = id;
        this.libraryCardNumber = libraryCardNumber;
        this.educationalInstitutionName = educationalInstitutionName;
        this.faculty = faculty;
        this.course = course;
        this.groupNumber = groupNumber;
        this.studentCardNumber = studentCardNumber;
        this.extensionDate = extensionDate;
    }

    @Override
    public boolean checkEmptyFields() {
        return libraryCardNumber > 0 && studentCardNumber > 0 && course > 0 && educationalInstitutionName != null &&
                !educationalInstitutionName.trim().isEmpty() && extensionDate != null && !extensionDate.trim().isEmpty();
    }
}
