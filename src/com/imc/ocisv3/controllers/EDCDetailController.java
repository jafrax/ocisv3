package com.imc.ocisv3.controllers;

import com.imc.ocisv3.pojos.BenefitPOJO;
import com.imc.ocisv3.pojos.EDCTransactionPOJO;
import com.imc.ocisv3.pojos.MemberPOJO;
import com.imc.ocisv3.tools.Libs;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by faizal on 11/11/13.
 */
public class EDCDetailController extends Window {

    private Logger log = LoggerFactory.getLogger(EDCDetailController.class);
    private EDCTransactionPOJO edcTransaction;
    private Listbox lb;

    public void onCreate() {
        if (!Libs.checkSession()) {
            edcTransaction = (EDCTransactionPOJO) getAttribute("edc");
            initComponents();
            populate();
        }
    }

    private void initComponents() {
        lb = (Listbox) getFellow("lb");

        getCaption().setLabel("EDC Transaction Detail [" + edcTransaction.getTrans_id() + " - " + edcTransaction.getName() + "]");
    }

    private void populate() {
        populateInformation();
        populatePlanItems();
    }

    private void populateInformation() {
        MemberPOJO memberPOJO = Libs.getMember(edcTransaction.getYear() + "-1-0-" + edcTransaction.getPolicy_number(), edcTransaction.getIdx() + "-" + edcTransaction.getSeq());

        int ageDays = 0;
        try {
            ageDays = Libs.getDiffDays(new SimpleDateFormat("yyyy-MM-dd").parse(memberPOJO.getDob()), new Date());
        } catch (Exception ex) {
            log.error("populateInformation", ex);
        }

        ((Label) getFellow("lName")).setValue(edcTransaction.getName());
        ((Label) getFellow("lSex")).setValue(memberPOJO.getSex());
        ((Label) getFellow("lReferenceNumber")).setValue(edcTransaction.getTrans_id());
        ((Label) getFellow("lCardNumber")).setValue(edcTransaction.getCard_number());
        ((Label) getFellow("lPlan")).setValue(edcTransaction.getPlan());
        ((Label) getFellow("lDiagnosis")).setValue(edcTransaction.getIcd());
        ((Label) getFellow("lRequestTime")).setValue(edcTransaction.getDate());

        ((Label) getFellow("lDOB")).setValue(memberPOJO.getDob());

        Label lAge = (Label) getFellow("lAge");
        lAge.setStyle("text-align:right;");
        lAge.setValue("(" + (ageDays / 365) + ")");

        ((Label) getFellow("lClientPolicyNumber")).setValue(memberPOJO.getClient_policy_number());
        ((Label) getFellow("lClientIDNumber")).setValue(memberPOJO.getClient_id_number());
        ((Label) getFellow("lStartingDate")).setValue(memberPOJO.getStarting_date());
        ((Label) getFellow("lMatureDate")).setValue(memberPOJO.getMature_date());
        ((Label) getFellow("lMaritalStatus")).setValue(memberPOJO.getMarital_status());
    }

    private void populatePlanItems() {
        lb.getItems().clear();
        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + Libs.createListFieldString("a.hbftbcd") + ", "
                    + Libs.createListFieldString("a.hbftbpln") + " "
                    + "from idnhltpf.dbo.hltbft a "
                    + "where "
                    + "a.hbftyy=" + edcTransaction.getYear() + " and "
                    + "a.hbftpono=" + edcTransaction.getPolicy_number() + " and "
                    + "a.hbftcode='" + edcTransaction.getPlan() + "' ";

            List<Object[]> l = s.createSQLQuery(qry).list();
            if (l.size()==1) {
                Object[] o = l.get(0);

                for (int i=0; i<30; i++) {
                    if (!Libs.nn(o[i]).trim().isEmpty()) {
                        Listitem li = new Listitem();
                        li.setValue(o);

                        li.appendChild(new Listcell(Libs.nn(o[i]).trim()));
                        li.appendChild(new Listcell(Libs.getBenefitItemDescription(Libs.nn(o[i]).trim())));
                        li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[i+30])), "#,###.##"));
                        li.appendChild(Libs.createNumericListcell(edcTransaction.getProposed()[i], "#,###.##"));
                        li.appendChild(Libs.createNumericListcell(edcTransaction.getApproved()[i], "#,###.##"));

                        lb.appendChild(li);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("populatePlanItems", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

}
