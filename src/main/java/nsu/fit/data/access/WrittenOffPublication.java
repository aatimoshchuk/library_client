package nsu.fit.data.access;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class WrittenOffPublication extends AbstractEntity {
    private Integer publicationNomenclatureNumber;
    private String writeOffDate;

    public WrittenOffPublication(int id, Integer publicationNomenclatureNumber, String writeOffDate) {
        this.id = id;
        this.publicationNomenclatureNumber = publicationNomenclatureNumber;
        this.writeOffDate = writeOffDate;
    }

    @Override
    public boolean checkEmptyFields() {
        return publicationNomenclatureNumber > 0 && writeOffDate != null && !writeOffDate.isEmpty();
    }
}
