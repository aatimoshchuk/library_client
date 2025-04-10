package nsu.fit.repository;

import java.util.List;

public abstract class AbstractEntityRepository<T> {
    public abstract List<T> findAll();
    public abstract String saveEntity(T entity);
    public abstract void deleteEntity(T entity);
}
