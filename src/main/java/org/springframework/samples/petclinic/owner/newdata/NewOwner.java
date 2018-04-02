package org.springframework.samples.petclinic.owner.newdata;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.petclinic.model.Person;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import java.util.*;

@Entity
@Table(name = "new_owners")
public class NewOwner extends Person {
    @Column(name = "address")
    @NotEmpty
    private String address;

    @Column(name = "city")
    @NotEmpty
    private String city;

    @Column(name = "telephone")
    @NotEmpty
    @Digits(fraction = 0, integer = 10)
    private String telephone;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private Set<NewPet> pets;

    public NewOwner(){
        super();
    }

    public NewOwner(Owner owner){
        super(owner);
        this.address = owner.getAddress();
        this.city = owner.getCity();
        this.telephone = owner.getTelephone();
        this.pets = getNewPetsSet(owner.getPets());
    }


    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    protected Set<NewPet> getPetsInternal() {
        if (this.pets == null) {
            this.pets = new HashSet<>();
        }
        return this.pets;
    }

    protected void setPetsInternal(Set<NewPet> pets) {
        this.pets = pets;
    }

    public List<NewPet> getPets() {
        List<NewPet> sortedPets = new ArrayList<>(getPetsInternal());
        PropertyComparator.sort(sortedPets,
            new MutableSortDefinition("name", true, true));
        return Collections.unmodifiableList(sortedPets);
    }


    public void addPet(NewPet pet) {
        if (pet.isNew()) {
            getPetsInternal().add(pet);
        }
        pet.setOwner(this);
    }

    /**
     * Return the Pet with the given name, or null if none found for this Owner.
     *
     * @param name to test
     * @return true if pet name is already in use
     */
    public NewPet getPet(String name) {
        return getPet(name, false);
    }

    /**
     * Return the Pet with the given name, or null if none found for this Owner.
     *
     * @param name to test
     * @return true if pet name is already in use
     */
    public NewPet getPet(String name, boolean ignoreNew) {
        name = name.toLowerCase();
        for (NewPet pet : getPetsInternal()) {
            if (!ignoreNew || !pet.isNew()) {
                String compName = pet.getName();
                compName = compName.toLowerCase();
                if (compName.equals(name)) {
                    return pet;
                }
            }
        }
        return null;
    }

    private Set<NewPet> getNewPetsSet(Collection<Pet> pets){
        Set<NewPet> newSet = new HashSet<>();

        for(Pet pet: pets){
            newSet.add(new NewPet(pet));
        }

        return newSet;
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)

            .append("id", this.getId()).append("new", this.isNew())
            .append("lastName", this.getLastName())
            .append("firstName", this.getFirstName()).append("address", this.address)
            .append("city", this.city).append("telephone", this.telephone).toString();
    }


}
