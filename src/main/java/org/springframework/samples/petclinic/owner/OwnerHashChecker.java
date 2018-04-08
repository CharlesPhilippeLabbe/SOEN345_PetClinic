package org.springframework.samples.petclinic.owner;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.samples.petclinic.model.HashConsistencyChecker;
import org.springframework.samples.petclinic.model.ViolationRepository;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class OwnerHashChecker extends HashConsistencyChecker<Owner> {

    private NewOwnerRepository repository;

    public OwnerHashChecker(NewOwnerRepository repository, ViolationRepository violations){
        super(violations);
        this.repository = repository;
    }

    public OwnerHashChecker(ViolationRepository violations){
        super(violations);
    }


    public void setRepository(NewOwnerRepository repo){
        this.repository = repo;
    }

    protected void initiateAsync(NewOwnerRepository repo){
        this.repository = repo;
        this.initiateAsync();
    }

    protected void initiateAsync(){

        if(this.repository == null){
            return;
        }
        CompletableFuture.supplyAsync(()->{
           while(OwnerToggles.hashChecker){
               Collection<Owner> owners = repository.findByLastName("");//getting all the owners

               for(Owner owner: owners){
                   this.check(owner);//checking every instances of owner in database for inconsistencies
               }

                try {
                    Thread.sleep(10*60000);//wait 10 mins until next check
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
           }
            return false;
        });

    }

}
