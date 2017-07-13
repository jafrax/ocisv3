package com.imc.ocisv3.pojos;

import java.io.Serializable;

/**
 * Created by faizal on 10/30/13.
 */
public class PolicyPOJO implements Serializable {

    private int year;
    private int br;
    private int dist;
    private int policy_number;
    private String name;

 
	public String getPolicy_string() {
        return getYear() + "-" + getBr() + "-" + getDist() + "-" + getPolicy_number();
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getBr() {
        return br;
    }

    public void setBr(int br) {
        this.br = br;
    }

    public int getDist() {
        return dist;
    }

    public void setDist(int dist) {
        this.dist = dist;
    }

    public int getPolicy_number() {
        return policy_number;
    }

    public void setPolicy_number(int policy_number) {
        this.policy_number = policy_number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
