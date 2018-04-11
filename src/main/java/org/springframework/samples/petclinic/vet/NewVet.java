package org.springframework.samples.petclinic.vet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;

import org.springframework.samples.petclinic.model.Person;
import org.springframework.beans.support.*;
import org.springframework.core.style.ToStringCreator;



@Entity
@Table(name = "new_vets")
public class NewVet extends Person {

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "vet_specialties", joinColumns = @JoinColumn(name = "vet_id"), inverseJoinColumns = @JoinColumn(name = "specialty_id"))
    private Set<Specialty> specialties;
    
    public NewVet(){
    	super();
    }
    
    public NewVet(Vet vet){
    	this.specialties = vet.getSpecialtiesInternal();
    }

    protected Set<Specialty> getSpecialtiesInternal() {
        if (this.specialties == null) {
            this.specialties = new HashSet<>();
        }
        return this.specialties;
    }

    protected void setSpecialtiesInternal(Set<Specialty> specialties) {
        this.specialties = specialties;
    }
    
   

    public List<Specialty> getSpecialties() {
        List<Specialty> sortedSpecs = new  ArrayList<>(getSpecialtiesInternal());
        PropertyComparator.sort(sortedSpecs,
                new MutableSortDefinition("name", true, true));
        return Collections.unmodifiableList(sortedSpecs);
    }

    public int getNrOfSpecialties() {
        return getSpecialtiesInternal().size();
    }

    public void addSpecialty(Specialty specialty) {
        getSpecialtiesInternal().add(specialty);
    }
    
    @Override 
    public String toString(){
    	return new ToStringCreator(this)
    			.append("id", this.getId()).append("new",this.isNew())
    			.append("lastName", this.getLastName())
    			.append("firstName", this.getFirstName())
    			.append("numberOfSpecialties", this.getNrOfSpecialties())
    			.append("listOfSpecialties", this.getSpecialties()).toString();
    }

}