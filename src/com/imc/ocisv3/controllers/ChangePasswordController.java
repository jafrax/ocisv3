package com.imc.ocisv3.controllers;

import com.imc.ocisv3.tools.Libs;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Created by faizal on 2/12/14.
 */
public class ChangePasswordController extends Window {

    private Logger log = LoggerFactory.getLogger(ChangePasswordController.class);
    private Textbox tCurrentPassword;
    private Textbox tNewPassword;
    private Textbox tRepeatPassword;
    private String username;

    public void onCreate() {
        if (!Libs.checkSession()) {
            initComponents();
            username = Libs.nn(Libs.getSession().getAttribute("u"));
        }
    }

    private void initComponents() {
        tCurrentPassword = (Textbox) getFellow("tCurrentPassword");
        tNewPassword = (Textbox) getFellow("tNewPassword");
        tRepeatPassword = (Textbox) getFellow("tRepeatPassword");
    }

    private boolean validate() {
        boolean valid = true;
        boolean validPass = false;

        Session s3 = Libs.sfDB.openSession();
        try {
            String q = "select count(*) "
                    + "from ocis.dbo.cis_user "
                    + "where "
                    + "user_id='" + username + "' "
                    + "and pass='" + tCurrentPassword.getText() + "' ";

            int rc = (Integer) s3.createSQLQuery(q).uniqueResult();
            if (rc==1) validPass = true;
        } catch (Exception ex) {
            log.error("validate", ex);
        } finally {
            s3.close();
        }

        if (tCurrentPassword.getText().isEmpty() || tNewPassword.getText().isEmpty() || tRepeatPassword.getText().isEmpty()) {
            Messagebox.show("Please enter value in all fields", "Error", Messagebox.OK, Messagebox.ERROR);
            valid = false;
        } else if (!validPass) {
            Messagebox.show("Invalid current password", "Error", Messagebox.OK, Messagebox.ERROR);
            valid = false;
        } else if (!tNewPassword.getText().equals(tRepeatPassword.getText())) {
            Messagebox.show("New and Repeat Password do not match", "Error", Messagebox.OK, Messagebox.ERROR);
            valid = false;
        }

        return valid;
    }

    public void changePassword() {
        if (validate()) {
            if (Messagebox.show("Are you sure you want to change the password?", "Confirmation", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, Messagebox.CANCEL)==Messagebox.OK) {
                Session s3 = Libs.sfDB.openSession();
                try {
                    String q = "update ocis.dbo.cis_user "
                            + "set pass='" + tNewPassword.getText() + "' "
                            + "where "
                            + "user_id='" + username + "' "
                            + "and pass='" + tCurrentPassword.getText() + "' ";

                    s3.createSQLQuery(q).executeUpdate();
                    s3.beginTransaction().commit();
                    Messagebox.show("Password has been changed", "Information", Messagebox.OK, Messagebox.INFORMATION);
                    detach();
                } catch (Exception ex) {
                    log.error("changePassword", ex);
                } finally {
                    s3.close();
                }
            }
        }
    }

}
