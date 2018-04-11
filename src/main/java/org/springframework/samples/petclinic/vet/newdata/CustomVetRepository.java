package org.springframework.samples.petclinic.vet.newdata;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.vet.Vet;

import java.util.Collection;

public interface CustomVetRepository {
    Collection<Vet> findAll() throws DataAccessException;

    void addNewVet(Vet vet);
}
