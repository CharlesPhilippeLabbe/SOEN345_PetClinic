package org.springframework.samples.petclinic.owner.newdata;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface CustomPetRepository {

    List<PetType> findPetTypes();
    
    Collection<Pet> findByName(@Param("name") String name);

    Pet findById(@Param("id") Integer id);

    void save(Pet pet);
}
