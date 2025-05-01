package nsu.fit.repository;

import nsu.fit.utils.warning.Warning;

import java.util.List;

public abstract class AbstractEntityRepository<T> {

    public abstract List<T> findAll();
    public abstract Warning saveEntity(T entity);
    public abstract void deleteEntity(T entity);

}
