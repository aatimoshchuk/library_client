package nsu.fit.data.access;

import lombok.Getter;

@Getter
public abstract class AbstractEntity {

    protected int id;

    public abstract boolean checkEmptyFields();

    public boolean validateNumericFields() {
        return true;
    }

}
