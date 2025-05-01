package nsu.fit.data.access.category;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Pensioner extends AbstractCategoryEntity {

    private String pensionCertificateNumber;

    public Pensioner(int id, int libraryCardNumber, String pensionCertificateNumber) {
        this.id = id;
        this.libraryCardNumber = libraryCardNumber;
        this.pensionCertificateNumber = pensionCertificateNumber;
    }

    @Override
    public boolean checkEmptyFields() {
        return libraryCardNumber != null && libraryCardNumber > 0 && pensionCertificateNumber != null &&
                !pensionCertificateNumber.trim().isEmpty();
    }

    @Override
    public boolean validateNumericFields() {
        return libraryCardNumber != null;
    }
}
