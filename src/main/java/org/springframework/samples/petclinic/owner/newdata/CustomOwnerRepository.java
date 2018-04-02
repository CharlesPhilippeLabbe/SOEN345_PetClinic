package org.springframework.samples.petclinic.owner.newdata;

import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.owner.Owner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;

public interface CustomOwnerRepository {
     Collection<Owner> findByLastName(@Param("lastName") String lastName);
     Owner findById(@Param("id") Integer id);
     void save(Owner owner);
}


