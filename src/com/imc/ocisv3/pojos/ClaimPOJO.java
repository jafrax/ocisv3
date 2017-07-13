package com.imc.ocisv3.pojos;

import java.io.Serializable;

/**
 * Created by faizal on 10/29/13.
 */
public class ClaimPOJO implements Serializable {

    private String claim_number;
    private String policy_number;
    private String index;
    private int claim_count;
    private PolicyPOJO policy;
    private MemberPOJO member;

    public String getClaim_number() {
        return claim_number;
    }

    public void setClaim_number(String claim_number) {
        this.claim_number = claim_number;
    }

    public String getPolicy_number() {
        return policy_number;
    }

    public void setPolicy_number(String policy_number) {
        this.policy_number = policy_number;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public int getClaim_count() {
        return claim_count;
    }

    public void setClaim_count(int claim_count) {
        this.claim_count = claim_count;
    }

    public PolicyPOJO getPolicy() {
        return policy;
    }

    public void setPolicy(PolicyPOJO policy) {
        this.policy = policy;
    }

    public MemberPOJO getMember() {
        return member;
    }

    public void setMember(MemberPOJO member) {
        this.member = member;
    }

}
