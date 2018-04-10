package org.springframework.samples.petclinic.vet;

import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.Repository;
import java.util.Collection;

public interface NewVetRepository extends Repository<NewVet, Integer>{

    Collection<NewVet> findAll() throws DataAccessException;

    void addNewVet(NewVet newVet);
}
