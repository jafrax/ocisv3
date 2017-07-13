package com.imc.ocisv3.controllers;

import com.imc.ocisv3.tools.Libs;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.*;

import java.io.File;
import java.util.List;

/**
 * Created by faizal on 10/24/13.
 */
public class MainController extends Window {

    private Logger log = LoggerFactory.getLogger(MainController.class);
    private Tree tree;
    private Treeitem finDA;
    private Image imgCompanyLogo;
    
    private String polis ="";
    private List polisList;

    public void onCreate() {
        if (!Libs.checkSession()) {
            initComponents();
            
            polisList = Libs.getPolisByUserId(Libs.getUser());
            for(int i=0; i < polisList.size(); i++){
        		polis=polis+"'"+(String)polisList.get(i)+"'"+",";
        	}
            if(polis.length() > 1)polis = polis.substring(0, polis.length()-1);
            
//            getPolicies();
            open("Dashboard");
            setVisible(true);
        }
    }

    private void initComponents() {
        tree = (Tree) getFellow("tree");
        imgCompanyLogo = (Image) getFellow("imgCompanyLogo");
        finDA = (Treeitem)getFellow("finDA");

        Libs.getDesktop().setAttribute("rootWindow", this);
        Libs.getDesktop().setAttribute("center", getFellow("center"));

        ((Label) getFellow("lUsername")).setValue("User: " + Libs.nn(Executions.getCurrent().getSession().getAttribute("u")));

        String imageFile = "";

        if (Libs.config.get("demo_mode").equals("true") && Libs.getInsuranceId().equals("00051")) {
            imageFile = "/resources/companies/99999.jpg";
        } else {
            imageFile = "/resources/companies/" + Libs.getInsuranceId() + ".jpg";
            File f = new File(Executions.getCurrent().getSession().getWebApp().getRealPath(imageFile));
            if (!f.exists()) imageFile = "resources/companies/00000.jpg";
        }

        imgCompanyLogo.setSrc(imageFile);
        
        Integer userlevel = (Integer)Executions.getCurrent().getSession().getAttribute("userLevel");
        if(userlevel.intValue() == 1){
        	getFellow("tiClientSelection").setVisible(true);
        	finDA.setVisible(true);
        }else{
        	getFellow("tiClientSelection").setVisible(false);
        	finDA.setVisible(false);
        }
        
    }

    private void getPolicies() {
        Libs.policyMap.clear();
        Session s = Libs.sfDB.openSession();
        try {
        	
        	String insid="";
        	List products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
        	
            String qry = "select "
                    + "(convert(varchar,a.hhdryy)+'-'+convert(varchar,a.hhdrbr)+'-'+convert(varchar,a.hhdrdist)+'-'+convert(varchar,a.hhdrpono)) as policy, "
                    + "a.hhdrname "
                    + "from idnhltpf.dbo.hlthdr a "
                    + "where a.hhdrinsid";
            
            		if(products.size() > 0) qry = qry + " in  ("+insid+") ";
            		else qry = qry + "='" + Libs.getInsuranceId() + "' ";
            		
            		if(polisList.size() > 0){
            			qry = qry + "and convert(varchar,a.hhdryy)+'-'+convert(varchar,a.hhdrbr)+'-'+convert(varchar,a.hhdrdist)+'-'+convert(varchar,a.hhdrpono) "
            					  + "in ("+polis+") ";
            		} 
                    
            		/*
            		 * Author : Heri Siswanto BN
            		 * Date : 14 August 2014
            		 * Updae : Ordered by date (Reguest Mr Jame)
            		 */
            		
            		if(Executions.getCurrent().getSession().getAttribute("insuranceId") != "99999"){
            			//qry = qry + " and ='"+Executions.getCurrent().getSession().getAttribute("insuranceId")+"'";
            		}
            		//qry= qry + "order by a.hhdrname desc ";
            		
                    qry= qry + "order by a.hhdradtyy desc, a.hhdradtmm desc, a.hhdradtdd desc";
                    
            List<Object[]> l = s.createSQLQuery(qry).list();
            for (Object[] o : l) {
                Libs.policyMap.put(Libs.nn(o[0]), Libs.nn(o[1]).trim());
            }
        } catch (Exception ex) {
            log.error("getPolicies", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    public void open(String view) {
        if (Libs.getCenter().getChildren().size()>0) Libs.getCenter().removeChild(Libs.getCenter().getFirstChild());
        Executions.createComponents("/views/" + view + ".zul", Libs.getCenter(), null);
    }

    public void logout() {
        if (Messagebox.show("Are you sure you want to logout?", "Confirmation", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION)==Messagebox.OK) {
            Executions.getCurrent().getSession().setMaxInactiveInterval(0);
            Executions.getCurrent().getSession().invalidate();
            Executions.sendRedirect("/index.zul");
        }
    }

    public void clientSelection() {
        Executions.sendRedirect("views/ClientSelection.zul");
    }

    public void changePassword() {
        Window w = (Window) Executions.createComponents("/views/ChangePassword.zul", this, null);
        w.doModal();
    }

    public void openReportGenerator() {
//        Window w = (Window) Executions.createComponents("/views/ReportGenerator.zul", this, null);
    	 Window w = (Window) Executions.createComponents("/views/ReportList.zul", this, null);
         w.doModal();
    }

}
