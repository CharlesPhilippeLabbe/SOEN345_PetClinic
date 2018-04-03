package org.springframework.samples.petclinic.owner.newdata;

import java.util.HashMap;
import java.util.Set;

import org.springframework.samples.petclinic.owner.Owner;

public class HashStorage {
	private HashMap<Integer, Integer> owners;
	
	public void add(Owner owner)
	{
		owners.put(owner.getId(), owner.hashCode());
	}
	
	public Set<Integer> getIds() 
	{
		return owners.keySet();
	}
	
	public int get(int id)
	{
		return owners.get(id);
	}
}
