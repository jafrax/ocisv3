package com.imc.ocisv3.controllers;

import com.imc.ocisv3.tools.Libs;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Created by faizal on 10/24/13.
 */
public class IndexController extends Window {

    private Logger log = LoggerFactory.getLogger(IndexController.class);
    private Textbox tUsername;
    private Textbox tPassword;

    public void onCreate() {
        initComponents();
    }

    private void initComponents() {
        tUsername = (Textbox) getFellow("tUsername");
        tPassword = (Textbox) getFellow("tPassword");
    }

    public void login() {
        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + "fullname, ins_id, userlv "
                    + "from ocis.dbo.cis_user "
                    + "where "
                    + "user_id=:uId and "
                    + "pass=:pwd";
            
            //System.out.println(qry);
            SQLQuery q = s.createSQLQuery(qry);
            q.setString("uId", tUsername.getText());
            q.setString("pwd", tPassword.getText());
            
            List<Object[]> l = q.list();
            if (l.size()==1) {
                Object[] o = l.get(0);
                
                if(Executions.getCurrent().getSession().getAttribute("u") != null)
                	Executions.getCurrent().getSession().removeAttribute("u");
                
                Executions.getCurrent().getSession().setAttribute("u", tUsername.getText());
                Executions.getCurrent().getSession().setAttribute("userLevel", Integer.valueOf(Libs.nn(o[2])));
                Libs.userLevel = Integer.valueOf(Libs.nn(o[2]));

                Libs.log_login(tUsername.getText(), new Timestamp(new Date().getTime()));
                
                
                if(Executions.getCurrent().getSession().getAttribute("insuranceId") != null)
                	Executions.getCurrent().getSession().removeAttribute("insuranceId");
                
                if (Libs.userLevel==1) {
                    Executions.sendRedirect("views/ClientSelection.zul");
                } else {
                	Integer clientId = Libs.getNewClientId(Libs.nn(o[1]));
//                    Libs.getSession().setAttribute("insuranceId", Libs.nn(o[1]));
                	Libs.getSession().setAttribute("insuranceId", clientId);
                    Executions.sendRedirect("main.zul");
                }
            } else {
                Messagebox.show("Invalid Username/Password combination. This incident is reported.", "Error", Messagebox.OK, Messagebox.ERROR);
            }
        } catch (Exception ex) {
            log.error("login", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

}
