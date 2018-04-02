package org.springframework.samples.petclinic.vet.newdata;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.vet.Vet;

import java.util.Collection;

public class CustomVetRepositoryImpl implements CustomVetRepository {
    @Override
    public Collection<Vet> findAll() throws DataAccessException {
        return null;
    }

    @Override
    public void addNewVet(Vet vet) {

    }
}
