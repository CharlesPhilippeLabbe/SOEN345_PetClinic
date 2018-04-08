package org.springframework.samples.petclinic.model;
import org.springframework.data.repository.Repository;

import java.util.Collection;

public interface HashConsistencyChecker <T extends BaseEntity> {

    void update(T ob1);

    boolean check(T ob1);

    int check(Collection<T> list);

    //void initiateAsync(Repository<?,?> repository);

}
