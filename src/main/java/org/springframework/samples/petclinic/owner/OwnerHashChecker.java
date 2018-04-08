package org.springframework.samples.petclinic.owner;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.samples.petclinic.model.HashConsistencyChecker;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class OwnerHashChecker implements HashConsistencyChecker<Owner> {

    private HashMap<Integer, String> checksum = new HashMap<>();
    private MessageDigest md;
    private int inconsistencies = 0;
    private int checks = 0;

    public OwnerHashChecker(){
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
            return false;
        }

        return true;
    }

    private String getChecksum(Owner ob1){
        md.reset();//making sure the md is empty beforehand
        md.update(ob1.getBytes());

        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }


}
