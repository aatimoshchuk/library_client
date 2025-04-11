package nsu.fit.data.access;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class PublicationPermission {
    private int publicationNomenclatureNumber;
    private int readerCategoryID;
    private String readerCategoryName;
}
