package org.springframework.samples.petclinic.vet;

import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.Repository;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Param;

import java.util.Collection;

public interface NewVetRepository extends Repository<NewVet, Integer>{

    Collection<NewVet> findAll() throws DataAccessException;
    
   Vet findById(@Param("id") Integer id);

    void addNewVet(NewVet newVet);
}
