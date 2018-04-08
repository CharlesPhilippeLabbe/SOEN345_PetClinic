package org.springframework.samples.petclinic.model;
import org.springframework.data.repository.Repository;

public interface HashConsistencyChecker <T extends BaseEntity> {

    void update(T ob1);

    boolean check(T ob1);

    //void initiateAsync(Repository<?,?> repository);

}
