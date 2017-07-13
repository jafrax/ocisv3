package com.imc.ocisv3.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by faizal on 10/30/13.
 */
public class MemberPOJO implements Serializable {

    private PolicyPOJO policy;
    private String name;
    private String card_number;
    private String dob;
    private String starting_date;
    private String mature_date;
    private String client_policy_number;
    private String client_id_number;
    private String ip;
    private String op;
    private String maternity;
    private String dental;
    private String glasses;
    private String sex;
    private String marital_status;
    private String idx;
    private String seq;
    private String employee_id;
    private List<String> plan_entry_date = new ArrayList<String>();
    private List<String> plan_exit_date = new ArrayList<String>();

    public PolicyPOJO getPolicy() {
        return policy;
    }

    public void setPolicy(PolicyPOJO policy) {
        this.policy = policy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getStarting_date() {
        return starting_date;
    }

    public void setStarting_date(String starting_date) {
        this.starting_date = starting_date;
    }

    public String getMature_date() {
        return mature_date;
    }

    public void setMature_date(String mature_date) {
        this.mature_date = mature_date;
    }

    public String getClient_policy_number() {
        return client_policy_number;
    }

    public void setClient_policy_number(String client_policy_number) {
        this.client_policy_number = client_policy_number;
    }

    public String getClient_id_number() {
        return client_id_number;
    }

    public void setClient_id_number(String client_id_number) {
        this.client_id_number = client_id_number;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getMaternity() {
        return maternity;
    }

    public void setMaternity(String maternity) {
        this.maternity = maternity;
    }

    public String getDental() {
        return dental;
    }

    public void setDental(String dental) {
        this.dental = dental;
    }

    public String getGlasses() {
        return glasses;
    }

    public void setGlasses(String glasses) {
        this.glasses = glasses;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMarital_status() {
        return marital_status;
    }

    public void setMarital_status(String marital_status) {
        this.marital_status = marital_status;
    }

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public List<String> getPlan_entry_date() {
        return plan_entry_date;
    }

    public void setPlan_entry_date(List<String> plan_entry_date) {
        this.plan_entry_date = plan_entry_date;
    }

    public List<String> getPlan_exit_date() {
        return plan_exit_date;
    }

    public void setPlan_exit_date(List<String> plan_exit_date) {
        this.plan_exit_date = plan_exit_date;
    }

    public String getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }
}
