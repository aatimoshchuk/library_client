package nsu.fit.data.access.category;

import lombok.Getter;
import lombok.Setter;
import nsu.fit.data.access.AbstractEntity;

@Getter
@Setter
public abstract class AbstractCategoryEntity extends AbstractEntity {
    protected int libraryCardNumber;
}
