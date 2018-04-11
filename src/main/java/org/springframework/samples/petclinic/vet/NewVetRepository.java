package org.springframework.samples.petclinic.vet;

import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.Repository;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Param;

import java.util.Collection;

public interface NewVetRepository extends Repository<NewVet, Integer>{

	 @Transactional(readOnly = true)
	 @Cacheable("vets")
	 Collection<Vet> findAll() throws DataAccessException;

    void addNewVet(Vet vet);
}
