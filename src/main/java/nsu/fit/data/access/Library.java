package nsu.fit.data.access;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nsu.fit.annotations.DisplayName;
import nsu.fit.annotations.FieldOrder;

@Setter
@Getter
@NoArgsConstructor
public class Library extends AbstractEntity {

    @DisplayName("ID")
    @FieldOrder(1)
    private int id;

    @DisplayName("Название")
    @FieldOrder(2)
    private String name;

    @DisplayName("Адрес")
    @FieldOrder(3)
    private String address;

    public Library(int id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    @Override
    public boolean checkEmptyFields() {
        return name != null && !name.isEmpty() && address != null && !address.isEmpty();
    }

}

