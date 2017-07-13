package com.imc.ocisv3.pojos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by faizal on 3/25/14.
 */
public class ReportGeneratorPOJO {

    private String title;
    private String status;
    private String content;
    private List<RGParameterPOJO> parameters = new ArrayList<RGParameterPOJO>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void addParameter(RGParameterPOJO parameter) {
        parameters.add(parameter);
    }

    public List<RGParameterPOJO> getParameters() {
        return parameters;
    }

    public void setParameters(List<RGParameterPOJO> parameters) {
        this.parameters = parameters;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
