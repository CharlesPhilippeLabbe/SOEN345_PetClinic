package org.springframework.samples.petclinic.model;
import org.springframework.data.repository.Repository;
import org.springframework.samples.petclinic.owner.Owner;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;

public abstract class HashConsistencyChecker<T extends BaseEntity> implements ConsistencyChecker<T>{

    private int inconsistencies = 0;
    private int checks = 0;

    private HashMap<Integer, String> checksum = new HashMap<>();
    private MessageDigest md;
    private ViolationRepository violations;


    public HashConsistencyChecker(ViolationRepository violations){
        this.violations = violations;
        try{
            md = MessageDigest.getInstance("MD5");
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }

    @Override
    public int getReadInconsistencies(){
        return this.inconsistencies;
    }
    @Override
    public double getInconsistencyRatio(){
        if(checks>0){
            return (double)inconsistencies/checks;
        }
        else{
            return 0;
        }
    }

    @Override
    public void update(T ob1) {

        this.checksum.put(ob1.getId(), this.getChecksum(ob1));
    }

    @Override
    public boolean check(T expected) {
        return this.check(expected.getId(), expected);
    }

    @Override
    public boolean check(int id, T actual){
        String actualHash = this.getChecksum(actual);
        String expectedHash = this.checksum.get(id);
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
    public int check(Collection<T> owners){

        for(T owner : owners){
            check(owner);//checking each owner in list
        }

        return this.inconsistencies;
    }


    @Override
    public boolean check(T actual, T expected){
        String actualHash = this.getChecksum(actual);
        String expectedHash = this.getChecksum(expected);
        return actualHash.equals(expectedHash);
    }

    @Deprecated
    @Override
    public int check(Collection<T> results, Collection<T> expectedResults){return 0;}

    public String getChecksum(T ob1){
        md.reset();//making sure the md is empty beforehand
        md.update(ob1.getBytes());

        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }



    //void initiateAsync(Repository<?,?> repository);

}
