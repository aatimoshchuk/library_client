package nsu.fit.repository;

import nsu.fit.utils.Warning;

import java.util.List;

public abstract class AbstractEntityRepository<T> {
    protected static final String IMPOSSIBLE_TO_SAVE = "Невозможно выполнить сохранение";

    public abstract List<T> findAll();
    public abstract Warning saveEntity(T entity);
    public abstract void deleteEntity(T entity);
}
