package com.imc.ocisv3.controllers;

import com.imc.ocisv3.tools.Libs;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.*;
import org.zkoss.zul.Calendar;

import java.util.*;

/**
 * Created by faizal on 10/29/13.
 */
public class DashboardController extends Window {

    private Logger log = LoggerFactory.getLogger(DashboardController.class);
    private Flashchart chartFrequency;
    private String userProductViewrestriction;

    public void onCreate() {
        if (!Libs.checkSession()) {
            userProductViewrestriction = Libs.restrictUserProductView.get(Libs.getUser());
            initComponents();
            populateChartFrequency();
        }
    }

    private void initComponents() {
        chartFrequency = (Flashchart) getFellow("chartFrequency");
    }

    private void populateChartFrequency() {
        Map<String,Integer> claimMap = new HashMap<String,Integer>();
        CategoryModel cm = new SimpleCategoryModel();

        Session s = Libs.sfDB.openSession();
        try {
        	
        	String insid="";
        	List products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
        	
            String qry = "select "
                    + "a.hclmcdatey, a.hclmcdatem, a.hclmtclaim, count(*) "
                    + "from idnhltpf.dbo.hltclm a "
                    + "inner join idnhltpf.dbo.hlthdr b on b.hhdryy=a.hclmyy and b.hhdrpono=a.hclmpono "
                    + "where "
                    + "b.hhdrinsid";
            		if(products.size() > 0) qry = qry + " in  ("+insid+")";
            		else qry = qry + "='" + Libs.getInsuranceId() + "' ";  
            		
            		qry = qry  + " and a.hclmrecid<>'C' ";

            if (!Libs.nn(userProductViewrestriction).isEmpty()) qry += "and b.hhdrpono in (" + userProductViewrestriction + ") ";

            qry += "group by a.hclmcdatey, a.hclmcdatem, a.hclmtclaim ";
            
//            Messagebox.show(qry);

            List<Object[]> l = s.createSQLQuery(qry).list();

            for (Object[] o : l) {
                String key = o[0] + "-" + (Integer.valueOf(Libs.nn(o[1]))-1) + "-" + o[2];
                claimMap.put(key, Integer.valueOf(Libs.nn(o[3])));
            }

            for (int i=-11; i<1; i++) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.MONTH, i);
                String key = cal.get(java.util.Calendar.YEAR) + "-" + cal.get(java.util.Calendar.MONTH) + "-I";
                cm.setValue("Inpatient", Libs.shortMonths[cal.get(java.util.Calendar.MONTH)] + " " + String.valueOf(cal.get(java.util.Calendar.YEAR)).substring(2), claimMap.get(key));
            }

            for (int i=-11; i<1; i++) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.MONTH, i);
                String key = cal.get(java.util.Calendar.YEAR) + "-" + cal.get(java.util.Calendar.MONTH) + "-O";
                cm.setValue("Outpatient", Libs.shortMonths[cal.get(java.util.Calendar.MONTH)] + " " + String.valueOf(cal.get(java.util.Calendar.YEAR)).substring(2), claimMap.get(key));
            }

            for (int i=-11; i<1; i++) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.MONTH, i);
                String key = cal.get(java.util.Calendar.YEAR) + "-" + cal.get(java.util.Calendar.MONTH) + "-R";
                cm.setValue("Maternity", Libs.shortMonths[cal.get(java.util.Calendar.MONTH)] + " " + String.valueOf(cal.get(java.util.Calendar.YEAR)).substring(2), claimMap.get(key));
            }

            for (int i=-11; i<1; i++) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.MONTH, i);
                String key = cal.get(java.util.Calendar.YEAR) + "-" + cal.get(java.util.Calendar.MONTH) + "-D";
                cm.setValue("Dental", Libs.shortMonths[cal.get(java.util.Calendar.MONTH)] + " " + String.valueOf(cal.get(java.util.Calendar.YEAR)).substring(2), claimMap.get(key));
            }

            for (int i=-11; i<1; i++) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.MONTH, i);
                String key = cal.get(java.util.Calendar.YEAR) + "-" + cal.get(java.util.Calendar.MONTH) + "-G";
                cm.setValue("Glasses", Libs.shortMonths[cal.get(java.util.Calendar.MONTH)] + " " + String.valueOf(cal.get(java.util.Calendar.YEAR)).substring(2), claimMap.get(key));
            }

            chartFrequency.setModel(cm);
        } catch (Exception ex) {
            log.error("populateChartFrequency", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

}
