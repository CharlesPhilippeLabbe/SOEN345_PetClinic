package org.springframework.samples.petclinic.model;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

public class ViolationRepositoryImpl implements ViolationRepository {
    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    @Override
    public void add(String expected, String actual) {
        Query query = entityManager.createNativeQuery("INSERT INTO violations(time, expected, actual)" +
            "VALUES(:time, :expected,:actual)");
        query.setParameter("time", java.sql.Timestamp.valueOf(LocalDateTime.now()));
        query.setParameter("expected", expected);
        query.setParameter("actual", actual);

        query.executeUpdate();

    }
}
