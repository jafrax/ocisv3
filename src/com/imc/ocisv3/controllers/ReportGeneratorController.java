package com.imc.ocisv3.controllers;

import bsh.Interpreter;
import com.imc.ocisv3.pojos.PolicyPOJO;
import com.imc.ocisv3.pojos.ProviderPOJO;
import com.imc.ocisv3.pojos.RGParameterPOJO;
import com.imc.ocisv3.pojos.ReportGeneratorPOJO;
import com.imc.ocisv3.tools.Libs;
import org.apache.commons.digester.Digester;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.*;
import org.zkoss.zul.event.PagingEvent;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Created by faizal on 3/25/14.
 */
public class ReportGeneratorController extends Window {

    private Logger log = LoggerFactory.getLogger(ReportGeneratorController.class);
    private Listbox lb;
    private List<ReportGeneratorPOJO> rgs = new ArrayList<ReportGeneratorPOJO>();
    private Map<String,ReportGeneratorPOJO> rgMap = new HashMap<String,ReportGeneratorPOJO>();
    private Rows rows;
    private Grid grid;

    public void onCreate() {
        initComponents();
        populate();
    }

    private void initComponents() {
        lb = (Listbox) getFellow("lb");
        rows = (Rows) getFellow("rows");
        grid = (Grid) getFellow("grid");
    }

    private void populate() {
        lb.getItems().clear();
        rgs.clear();
        ((Toolbarbutton) getFellow("tbnGenerateReport")).setDisabled(true);

        File reportDir = new File(Executions.getCurrent().getSession().getWebApp().getRealPath("/resources/reports"));
        if (reportDir.exists()) {
            for (File f : reportDir.listFiles()) {
                try {
                    Digester d = new Digester();
                    d.addObjectCreate("reportgenerator", ReportGeneratorPOJO.class);
                    d.addBeanPropertySetter("reportgenerator/properties/title", "title");
                    d.addBeanPropertySetter("reportgenerator/properties/status", "status");
                    d.addBeanPropertySetter("reportgenerator/content", "content");
                    d.addObjectCreate("reportgenerator/properties/parameters/parameter", RGParameterPOJO.class);
                    d.addSetProperties("reportgenerator/properties/parameters/parameter");
                    d.addSetNext("reportgenerator/properties/parameters/parameter", "addParameter");

                    ReportGeneratorPOJO rgPOJO = (ReportGeneratorPOJO) d.parse(new FileInputStream(f));
                    rgs.add(rgPOJO);
                    rgMap.put(rgPOJO.getTitle(), rgPOJO);

                    if (rgPOJO.getStatus().toLowerCase().equals("production")) {
                        Listitem li = new Listitem();
                        li.setValue(rgPOJO.getTitle());
                        li.appendChild(new Listcell(rgPOJO.getTitle()));
                        lb.appendChild(li);
                    }
                } catch (Exception ex) {
                    log.error("populate", ex);
                }
            }
        }
    }

    public void reportSelected() {
        if (lb.getSelectedCount()==1) {
            populateParameters();
            ((Toolbarbutton) getFellow("tbnGenerateReport")).setDisabled(false);
        }
    }

    private void populateParameters() {
        grid.getRows().getChildren().clear();
        ReportGeneratorPOJO rgPOJO = rgMap.get(Libs.nn(lb.getSelectedItem().getValue()));

        for (RGParameterPOJO parameter : rgPOJO.getParameters()) {
            StringBuilder layout = new StringBuilder();

            layout.append("<row>")
                    .append("<label value=\"" + parameter.getTitle() + "\"/>");

            if (parameter.getType().equals("DATE_RANGE")) {
                layout.append("<hlayout>")
                        .append("<datebox id=\"" + parameter.getId() + ".date.start\" width=\"100px\" format=\"yyyy-MM-dd\"/>-")
                        .append("<datebox id=\"" + parameter.getId() + ".date.end\" width=\"100px\" format=\"yyyy-MM-dd\"/>")
                        .append("</hlayout>");
            }

            if (parameter.getType().equals("PROVIDER_SELECTOR")) {
                layout.append("<bandbox id=\"" + parameter.getId() + "\" hflex=\"true\" readonly=\"true\" onOpen='spaceOwner.providerSelectorOpened(\"" + parameter.getId() + "\", event);'>")
                        .append("<bandpopup width=\"500px\" height=\"350px\">")
                        .append("<panel width=\"100%\" height=\"100%\">")
                        .append("<panelchildren>")
                        .append("<hbox pack=\"stretch\" sclass=\"hboxRemoveWhiteStrips\" width=\"100%\">")
                        .append("<toolbar height=\"21px\">")
                        .append("<toolbarbutton image=\"resources/icons/accept.png\" tooltiptext=\"OK\" onClick='spaceOwner.providerSelectorSelected(\"" + parameter.getId() + "\");'/>")
                        .append("</toolbar>")
                        .append("<toolbar align=\"end\" height=\"21px\">")
                        .append("<image src=\"resources/icons/browse.png\"/>")
                        .append("<textbox id=\"" + parameter.getId() + ".t.quicksearch\" onOK='spaceOwner.quickSearchProviderSelector(\"" + parameter.getId() + "\");'/>")
                        .append("</toolbar>")
                        .append("</hbox>")
                        .append("<listbox id=\"" + parameter.getId() + ".lb\" width=\"100%\" height=\"100%\" vflex=\"true\" style=\"white-space:nowrap; border-top:0px;\" onDoubleClick='spaceOwner.providerSelectorSelected(\"" + parameter.getId() + "\");'>")
                        .append("<listhead sizable=\"true\">")
                        .append("<listheader label=\"Provider ID\" width=\"80px\"/>")
                        .append("<listheader label=\"Name\"/>")
                        .append("</listhead>")
                        .append("</listbox>")
                        .append("<paging id=\"" + parameter.getId() + ".pg\" pageSize=\"25\"/>")
                        .append("</panelchildren>")
                        .append("</panel>")
                        .append("</bandpopup>")
                        .append("</bandbox>");
            }

            if (parameter.getType().equals("PRODUCT_SELECTOR")) {
                layout.append("<bandbox id=\"" + parameter.getId() + "\" hflex=\"true\" readonly=\"true\" onOpen='spaceOwner.productSelectorOpened(\"" + parameter.getId() + "\", event);'>")
                        .append("<bandpopup width=\"500px\" height=\"350px\">")
                        .append("<panel width=\"100%\" height=\"100%\">")
                        .append("<panelchildren>")
                        .append("<hbox pack=\"stretch\" sclass=\"hboxRemoveWhiteStrips\" width=\"100%\">")
                        .append("<toolbar height=\"21px\">")
                        .append("<toolbarbutton image=\"resources/icons/accept.png\" tooltiptext=\"OK\" onClick='spaceOwner.productSelectorSelected(\"" + parameter.getId() + "\");'/>")
                        .append("</toolbar>")
                        .append("<toolbar align=\"end\" height=\"21px\">")
                        .append("<image src=\"resources/icons/browse.png\"/>")
                        .append("<textbox id=\"" + parameter.getId() + ".t.quicksearch\" onOK='spaceOwner.quickSearchProductSelector(\"" + parameter.getId() + "\");'/>")
                        .append("</toolbar>")
                        .append("</hbox>")
                        .append("<listbox id=\"" + parameter.getId() + ".lb\" width=\"100%\" height=\"100%\" vflex=\"true\" style=\"white-space:nowrap; border-top:0px;\" onDoubleClick='spaceOwner.productSelectorSelected(\"" + parameter.getId() + "\");'>")
                        .append("<listhead sizable=\"true\">")
                        .append("<listheader label=\"Year\" width=\"70px\"/>")
                        .append("<listheader label=\"Product Number\" width=\"110px\"/>")
                        .append("<listheader label=\"Name\"/>")
                        .append("</listhead>")
                        .append("</listbox>")
                        .append("</panelchildren>")
                        .append("</panel>")
                        .append("</bandpopup>")
                        .append("</bandbox>");
            }

            layout.append("</row>");

            rows.appendChild(Executions.createComponentsDirectly(layout.toString(), "zul", null, null));
        }
    }

    public void generateReport() {
        String uuid = UUID.randomUUID().toString();
        File fuuid = new File(Libs.nn(Libs.config.get("temp_dir")) + File.separator + uuid);

        ReportGeneratorPOJO rgPOJO = rgMap.get(Libs.nn(lb.getSelectedItem().getValue()));
        Interpreter interpreter = new Interpreter();
        String reportName = "";
        String content = "import com.imc.ocisv3.pojos;"
                + "import java.util.*;"
                + "import org.hibernate.*;"
                + "import java.io.*;"
                + "import org.apache.poi.hssf.usermodel.HSSFWorkbook;"
                + "import org.apache.poi.ss.usermodel.Sheet;"
                + "import org.apache.poi.ss.usermodel.Workbook;"
                + "import java.text.SimpleDateFormat;"
                + "import org.zkoss.zul.*;"
                + "import com.imc.ocisv3.tools.Libs;"
                + rgPOJO.getContent();

        try {
            interpreter.set("fuuid", fuuid);
            interpreter.set("report_name", reportName);
            interpreter.set("client_id", Libs.getInsuranceId());

            for (RGParameterPOJO parameter : rgPOJO.getParameters()) {
                if (parameter.getType().equals("PRODUCT_SELECTOR")) {
                    interpreter.set(parameter.getId(), getFellow(parameter.getId()).getAttribute("e"));
                }

                if (parameter.getType().equals("DATE_RANGE")) {
                    interpreter.set(parameter.getId() + "_date_start", ((Datebox) getFellow(parameter.getId() + ".date.start")).getValue());
                    interpreter.set(parameter.getId() + "_date_end", ((Datebox) getFellow(parameter.getId() + ".date.end")).getValue());
                }
            }

            interpreter.eval(content);
        } catch (Exception ex) {
            log.error("generateReport", ex);
        }
    }

//    PROVIDER_SELECTOR
    public void providerSelectorOpened(final String id, OpenEvent evt) {
        if (evt.isOpen()) {
            final Paging pgProvider = (Paging) getFellow(id + ".pg");

            if (!pgProvider.getEventListeners("onPaging").iterator().hasNext()) {
                pgProvider.addEventListener("onPaging", new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        PagingEvent pagingEvent = (PagingEvent) event;
                        populateProviderSelector(id, pagingEvent.getActivePage()*pgProvider.getPageSize(), pgProvider.getPageSize());
                    }
                });
            } else {
                System.out.println("have!");
            }

            populateProviderSelector(id, 0, pgProvider.getPageSize());
        }
    }

    public void providerSelectorSelected(String id) {
        Bandbox bb = (Bandbox) getFellow(id);
        Listbox lbProvider = (Listbox) getFellow(id + ".lb");
        if (lbProvider.getSelectedCount()==1) {
            ProviderPOJO providerPOJO = lbProvider.getSelectedItem().getValue();
            bb.setText("[" + providerPOJO.getProvider_id() + "] " + providerPOJO.getName());
            bb.setAttribute("e", providerPOJO);
            bb.close();
        }
    }

    private void populateProviderSelector(String id, int offset, int limit) {
        Listbox lbProvider = (Listbox) getFellow(id + ".lb");
        Paging pgProvider = (Paging) getFellow(id + ".pg");
        lbProvider.getItems().clear();

        Session s = Libs.sfDB.openSession();
        String q0 = "select count(*) ";
        String q1 = "select "
                + "hpronomor, hproname ";
        String q2 = "from idnhltpf.dbo.hltpro ";
        String q3 = "";

        try {
            Integer rc = (Integer) s.createSQLQuery(q0 + q2 + q3).uniqueResult();
            pgProvider.setTotalSize(rc);

            List<Object[]> l = s.createSQLQuery(q1 + q2 + q3).setFirstResult(offset).setMaxResults(limit).list();
            for (Object[] o : l) {
                ProviderPOJO providerPOJO = new ProviderPOJO();
                providerPOJO.setProvider_id(Integer.valueOf(Libs.nn(o[0])));
                providerPOJO.setName(Libs.nn(o[1]).trim());

                Listitem li = new Listitem();
                li.setValue(providerPOJO);
                li.appendChild(new Listcell(String.valueOf(providerPOJO.getProvider_id())));
                li.appendChild(new Listcell(String.valueOf(providerPOJO.getName())));

                lbProvider.appendChild(li);
            }
        } catch (Exception ex) {
            log.error("populateProviderSelector", ex);
        } finally {
            s.close();
        }
    }

    //    PRODUCT_SELECTOR
    public void productSelectorOpened(String id, OpenEvent evt) {
        if (evt.isOpen()) {
            populateProductSelector(id);
        }
    }

    public void productSelectorSelected(String id) {
        Bandbox bb = (Bandbox) getFellow(id);
        Listbox lbProduct = (Listbox) getFellow(id + ".lb");
        if (lbProduct.getSelectedCount()==1) {
            PolicyPOJO policyPOJO = lbProduct.getSelectedItem().getValue();
            bb.setText("[" + policyPOJO.getYear() + "-" + policyPOJO.getPolicy_number() + "] " + policyPOJO.getName());
            bb.setAttribute("e", policyPOJO);
            bb.close();
        }
    }

    private void populateProductSelector(String id) {
        Listbox lbProduct = (Listbox) getFellow(id + ".lb");
        lbProduct.getItems().clear();

        Session s = Libs.sfDB.openSession();
        
        String insid="";
    	List products = Libs.getProductByUserId(Libs.getUser());
    	for(int i=0; i < products.size(); i++){
    		insid=insid+"'"+(String)products.get(i)+"'"+",";
    	}
    	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
        
        String q = "select "
                + "hhdryy, hhdrpono, hhdrname "
                + "from idnhltpf.dbo.hlthdr "
                + "where "
                + "hhdrinsid";
        		if(products.size() > 0) q = q + " in  ("+insid+")";
        		else q = q + "='" + Libs.getInsuranceId() + "' ";  
        try {
            List<Object[]> l = s.createSQLQuery(q).list();
            for (Object[] o : l) {
                PolicyPOJO policyPOJO = new PolicyPOJO();
                policyPOJO.setYear(Integer.valueOf(Libs.nn(o[0])));
                policyPOJO.setBr(1);
                policyPOJO.setDist(1);
                policyPOJO.setPolicy_number(Integer.valueOf(Libs.nn(o[1])));
                policyPOJO.setName(Libs.nn(o[2]).trim());

                Listitem li = new Listitem();
                li.setValue(policyPOJO);
                li.appendChild(new Listcell(String.valueOf(policyPOJO.getYear())));
                li.appendChild(new Listcell(String.valueOf(policyPOJO.getPolicy_number())));
                li.appendChild(new Listcell(String.valueOf(policyPOJO.getName())));

                lbProduct.appendChild(li);
            }
        } catch (Exception ex) {
            log.error("populateProductSelector", ex);
        } finally {
            s.close();
        }
    }

}
