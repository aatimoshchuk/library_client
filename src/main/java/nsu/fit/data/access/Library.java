package nsu.fit.data.access;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Library extends AbstractEntity {

    private String name;
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

