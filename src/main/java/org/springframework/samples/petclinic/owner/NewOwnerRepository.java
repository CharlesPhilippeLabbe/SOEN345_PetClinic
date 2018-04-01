package org.springframework.samples.petclinic.owner;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public interface NewOwnerRepository extends Repository<NewOwner, Integer> {


    /**
     * Retrieve {@link NewOwner}s from the data store by last name, returning all owners
     * whose last name <i>starts</i> with the given name.
     * @param lastName Value to search for
     * @return a Collection of matching {@link NewOwner}s (or an empty Collection if none
     * found)
     */
    @Query("SELECT DISTINCT owner FROM NewOwner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
    @Transactional(readOnly = true)
    Collection<NewOwner> findByLastName(@Param("lastName") String lastName);

    /**
     * Retrieve an {@link NewOwner} from the data store by id.
     * @param id the id to search for
     * @return the {@link NewOwner} if found
     */
    @Query("SELECT owner FROM NewOwner owner left join fetch owner.pets WHERE owner.id =:id")
    @Transactional(readOnly = true)
    NewOwner findById(@Param("id") Integer id);

    /**
     * Save an {@link NewOwner} to the data store, either inserting or updating it.
     * @param owner the {@link NewOwner} to save
     */
    void save(NewOwner owner);
}
