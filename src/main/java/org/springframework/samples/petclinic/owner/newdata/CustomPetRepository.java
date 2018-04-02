package org.springframework.samples.petclinic.owner.newdata;

import org.springframework.data.jpa.repository.Query;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CustomPetRepository {

    List<PetType> findPetTypes();

    Pet findById(Integer id);

    void save(Pet pet);
}
