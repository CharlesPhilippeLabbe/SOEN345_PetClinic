package org.springframework.samples.petclinic.owner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.DatabaseChecker;

import java.util.Collection;

public class OwnerDatabaseChecker extends DatabaseChecker<Owner> {


    private NewOwnerRepository newOwners;

    public OwnerDatabaseChecker(NewOwnerRepository newDB){
        this.newOwners = newDB;
    }


    @Override
    public boolean check(int id, Owner expected) {
        if(OwnerToggles.newDB && OwnerToggles.oldDB && OwnerToggles.forklifted) {
            Owner actual = this.newOwners.findById(id);
            return this.check(actual, expected);
        }
        return false;
    }

    @Override
    public boolean check(Owner expected) {
        return check(expected.getId(), expected);
    }

    @Override
    public int check(Collection<Owner> results) {
        return this.check("", results);
    }

    @Override
    public int check(String lastname, Collection<Owner> results){
        if(OwnerToggles.newDB && OwnerToggles.oldDB && OwnerToggles.forklifted) {
            return this.check(this.newOwners.findByLastName(lastname), results);
        }
        return 0;
    }

    @Override
    public void update(Owner expected) {
        if(!OwnerToggles.testing){
            this.newOwners.save(expected);
        }

    }
}
