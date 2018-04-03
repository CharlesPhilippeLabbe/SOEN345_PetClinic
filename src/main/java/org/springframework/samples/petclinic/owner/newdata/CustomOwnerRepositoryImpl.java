package org.springframework.samples.petclinic.owner.newdata;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

public class CustomOwnerRepositoryImpl implements CustomOwnerRepository{

    @PersistenceContext
    EntityManager entityManager;


    /**
     * @Query("SELECT DISTINCT owner FROM new_owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
     * @param lastName
     * @return
     */
    @Transactional
    @Override
    public Collection<Owner> findByLastName(String lastName) {
        Query query = entityManager.createNativeQuery("SELECT DISTINCT * FROM new_owners LEFT JOIN pets using (id) WHERE last_name LIKE :last_name", Owner.class);
        query.setParameter("last_name", lastName + "%");

        Collection<Owner> owners = query.getResultList();
        System.out.println(owners.size());
        return owners;
    }

    /**
     * @Query("SELECT owner FROM new_owner left join fetch owner.pets WHERE owner.id =:id")
     * @param id
     * @return
     */
    @Transactional
    @Override
    public Owner findById(Integer id) {
        Query query = entityManager.createNativeQuery("SELECT *  FROM new_owners LEFT JOIN pets using (id) WHERE id = :id", Owner.class);
        query.setParameter("id", id);
        Owner owner = (Owner)query.getSingleResult();
        return owner;
    }

    /**
     * INSERT IGNORE INTO owners VALUES (1, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023')
     * @param owner
     */
    @Transactional
    @Override
    public void save(Owner owner) {


        Query query = entityManager.createNativeQuery("INSERT INTO new_owners(id, first_name, last_name, address, city, telephone)" +
            " VALUES(:id,:first_name,:last_name,:address,:city,:telephone)" +
            "ON DUPLICATE KEY UPDATE first_name =:first_name, last_name = :last_name, address= :address, city = :city, telephone = :telephone");
        query.setParameter("id", owner.getId());
        query.setParameter("first_name", owner.getFirstName());
        query.setParameter("last_name", owner.getLastName());
        query.setParameter("address", owner.getAddress());
        query.setParameter("city", owner.getCity());
        query.setParameter("telephone", owner.getTelephone());

        query.executeUpdate();


    }
}
