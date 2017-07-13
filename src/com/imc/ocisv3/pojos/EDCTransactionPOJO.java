package com.imc.ocisv3.pojos;

import java.io.Serializable;

/**
 * Created by faizal on 10/30/13.
 */
public class EDCTransactionPOJO implements Serializable {

    private String trans_id;
    private String card_number;
    private int policy_number;
    private int year;
    private int idx;
    private String seq;
    private String type;
    private String icd;
    private String date;
    private String plan;
    private Double[] proposed;
    private Double[] approved;
    private String name;

    public String getTrans_id() {
        return trans_id;
    }

    public void setTrans_id(String trans_id) {
        this.trans_id = trans_id;
    }

    public int getPolicy_number() {
        return policy_number;
    }

    public void setPolicy_number(int policy_number) {
        this.policy_number = policy_number;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcd() {
        return icd;
    }

    public void setIcd(String icd) {
        this.icd = icd;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Double[] getProposed() {
        return proposed;
    }

    public void setProposed(Double[] proposed) {
        this.proposed = proposed;
    }

    public Double[] getApproved() {
        return approved;
    }

    public void setApproved(Double[] approved) {
        this.approved = approved;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
