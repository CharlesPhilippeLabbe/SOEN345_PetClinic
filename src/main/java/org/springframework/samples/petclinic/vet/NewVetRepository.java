package org.springframework.samples.petclinic.vet;

import org.springframework.dao.DataAccessException;

import java.util.Collection;

public interface NewVetRepository {

    Collection<Vet> findAll() throws DataAccessException;

    void addNewVet(Vet vet);
}
