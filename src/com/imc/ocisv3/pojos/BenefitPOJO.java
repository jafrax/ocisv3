package com.imc.ocisv3.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by faizal on 10/29/13.
 */
public class BenefitPOJO implements Serializable {

    private String plan_code;
    private double limit;
    boolean isLimitGaji = false;
    
    public boolean isLimitGaji() {
		return isLimitGaji;
	}

	public void setLimitGaji(boolean isLimitGaji) {
		this.isLimitGaji = isLimitGaji;
	}

	private List<String> plan_items = new ArrayList<String>();

    public String getPlan_code() {
        return plan_code;
    }

    public void setPlan_code(String plan_code) {
        this.plan_code = plan_code;
    }

    public List<String> getPlan_items() {
        return plan_items;
    }

    public void setPlan_items(List<String> plan_items) {
        this.plan_items = plan_items;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

}
