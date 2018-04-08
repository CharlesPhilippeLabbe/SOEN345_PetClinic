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

public class OwnerHashChecker implements HashConsistencyChecker<Owner> {

    private HashMap<Integer, String> checksum = new HashMap<>();
    private MessageDigest md;
    private int inconsistencies = 0;
    private int checks = 0;
    private ViolationRepository violations;

    public OwnerHashChecker(ViolationRepository violations){
        this.violations = violations;
        try{
            md = MessageDigest.getInstance("MD5");
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }


    public int getInconsistencies(){
        return inconsistencies;
    }

    public int getNumberOfChecks(){
        return checks;
    }


    @Override
    public void update(Owner ob1) {
        this.checksum.put(ob1.getId(), this.getChecksum(ob1));
    }

    @Override
    public boolean check(Owner ob1) {
        String actualHash = this.getChecksum(ob1);
        String expectedHash = this.checksum.get(ob1.getId());
        this.checks++;
        if(!actualHash.equals(expectedHash)){
            System.out.println("HASH CONTENT Inconsistency found");
            this.inconsistencies++;
            this.violations.add(expectedHash,actualHash);
            return false;
        }

        return true;
    }

    @Override
    public int check(Collection<Owner> owners){

        for(Owner owner : owners){
            check(owner);//checking each owner in list
        }

        return this.inconsistencies;
    }


    protected String getChecksum(Owner ob1){
        md.reset();//making sure the md is empty beforehand
        md.update(ob1.getBytes());

        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }



    protected void initiateAsync(NewOwnerRepository repository){

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
