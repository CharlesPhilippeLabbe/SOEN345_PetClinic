package org.springframework.samples.petclinic.model;

public interface ViolationRepository {

    void add(String expected, String Actual);
}
