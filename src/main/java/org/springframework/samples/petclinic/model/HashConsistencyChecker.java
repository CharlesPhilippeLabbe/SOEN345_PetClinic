package org.springframework.samples.petclinic.model;

public interface HashConsistencyChecker <T extends BaseEntity> {

    void update(T ob1);

    boolean check(T ob1);

}
