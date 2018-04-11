package org.springframework.samples.petclinic.model;

import java.util.Collection;

public interface ConsistencyChecker<T extends BaseEntity> {

    int getReadInconsistencies();
    double getInconsistencyRatio();
    boolean check(int id, T expected);
    boolean check(T actual, T expected);
    boolean check(T expected);
    int check(String name, Collection<T> expected);
    int check(Collection<T> results);
    int check(Collection<T> results, Collection<T> expectedResults);
    void update(T expected);
}
