package examen.fapte_bune.repository;

import examen.fapte_bune.domain.Entity;

import java.io.IOException;
import java.util.Optional;

public interface Repository<ID, E extends Entity<ID>>{
    Optional<E> findOne(ID id) throws IOException;
    Iterable<E> findAll() throws IOException;
    Optional<E> save(E entity) throws IOException;
    Optional<E> delete(ID id) throws IOException;
    Optional<E> update(E entity) throws IOException;
}
