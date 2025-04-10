package nsu.fit.data.access.category;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ScientificWorker extends AbstractCategoryEntity {

    private String organizationName;
    private String scientificTopic;

    public ScientificWorker(int id, int libraryCardNumber, String organizationName, String scientificTopic) {
        this.id = id;
        this.libraryCardNumber = libraryCardNumber;
        this.organizationName = organizationName;
        this.scientificTopic = scientificTopic;
    }

    @Override
    public boolean checkEmptyFields() {
        return libraryCardNumber > 0 && organizationName != null && !organizationName.trim().isEmpty() &&
                scientificTopic != null && !scientificTopic.trim().isEmpty();
    }
}
