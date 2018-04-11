package org.springframework.samples.petclinic.model;

import org.springframework.samples.petclinic.owner.Owner;

import java.util.Collection;
import java.util.Iterator;

public abstract class DatabaseChecker<T extends BaseEntity> implements ConsistencyChecker<T> {

    private int inconsistencies = 0;
    private int checks = 0;

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
    public boolean check(T actual, T expected){
        this.checks++;
        if (!expected.equals(actual)) {
            this.inconsistencies++;
            System.out.println("MIGRATION ERROR: " +
                "found: \n" + actual.toString() +
                "\nbut was supposed to be: \n" + expected.toString());
            this.update(expected);
            return false;
        }
        return true;
    }

    @Override
    public int check(Collection<T> actualResults, Collection<T> expectedResults){

        int initial = this.inconsistencies;//get initial count
        Iterator<T> actualSet = actualResults.iterator();
        for (T expected : expectedResults) {
            if(!actualSet.hasNext()){
                this.checks++;//number of owners == number of reads
                inconsistencies++;
                System.out.println("MIGRATION ERROR: " +
                    "found: nothing \n" +
                    "\nbut was supposed to be: \n" + expected.toString());
                this.update(expected);
                continue;//check if more than one missing
            }
            T actual = actualSet.next();
            check(actual, expected);
        }

        while(actualSet.hasNext()){//there is additional data from new request
            inconsistencies++;
            this.checks++;
            System.out.println("MIGRATION ERROR: " +
                "found: \n" + actualSet.next() +
                "\nbut was supposed to be: nothing");
            //don't know what to do about this
        }

        return this.inconsistencies - initial;//return new inconsistencies

    }

}
