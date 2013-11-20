package com.imc.ocisv3.controllers;

import com.imc.ocisv3.pojos.EDCTransactionPOJO;
import com.imc.ocisv3.tools.Libs;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.*;
import org.zkoss.zul.event.PagingEvent;

import java.util.List;
import java.util.Map;

/**
 * Created by faizal on 10/25/13.
 */
public class EDCTransactionsController extends Window {

    private Logger log = LoggerFactory.getLogger(EDCTransactionsController.class);
    private Listbox lbActiveTransactions;
    private Listbox lbClosedTransactions;
    private Listbox lbManualTransactions;
    private Paging pgActiveTransactions;
    private Paging pgClosedTransactions;
    private Paging pgManualTransactions;
    private String whereActive;
    private String whereClosed;
    private String whereManual;

    public void onCreate() {
        initComponents();
        populateActiveTransactions(0, pgActiveTransactions.getPageSize());
        populateClosedTransactions(0, pgClosedTransactions.getPageSize());
        populateManualTransactions(0, pgManualTransactions.getPageSize());
    }

    private void initComponents() {
        lbActiveTransactions = (Listbox) getFellow("lbActiveTransactions");
        lbClosedTransactions = (Listbox) getFellow("lbClosedTransactions");
        lbManualTransactions = (Listbox) getFellow("lbManualTransactions");
        pgActiveTransactions = (Paging) getFellow("pgActiveTransactions");
        pgClosedTransactions = (Paging) getFellow("pgClosedTransactions");
        pgManualTransactions = (Paging) getFellow("pgManualTransactions");
        pgActiveTransactions.addEventListener("onPaging", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                PagingEvent evt = (PagingEvent) event;
                populateActiveTransactions(evt.getActivePage()*pgActiveTransactions.getPageSize(), pgActiveTransactions.getPageSize());
            }
        });

        pgClosedTransactions.addEventListener("onPaging", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                PagingEvent evt = (PagingEvent) event;
                populateClosedTransactions(evt.getActivePage()*pgClosedTransactions.getPageSize(), pgClosedTransactions.getPageSize());
            }
        });
        pgManualTransactions.addEventListener("onPaging", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                PagingEvent evt = (PagingEvent) event;
                populateClosedTransactions(evt.getActivePage()*pgManualTransactions.getPageSize(), pgManualTransactions.getPageSize());
            }
        });
    }

    private void populateActiveTransactions(int offset, int limit) {
        lbActiveTransactions.getItems().clear();
        Session s = Libs.sfEDC.openSession();
        try {
//            Create policy numbers
            String policies = "";
            for (String policy : Libs.policyMap.keySet()) policies += "'" + policy.substring(policy.lastIndexOf("-")+1) + "', ";
            if (policies.endsWith(", ")) policies = policies.substring(0, policies.length()-2);

            String count = "select count(*) ";

            String select = "select "
                    + "a.transaction_id, a.no_kartu, a.tid, a.request_date, "
                    + "a.type, a.icd, a.reply_variable, c.pro_code ";

            String qry = "from edc_prj.dbo.ms_log_transaction a "
                    + "left outer join edc_prj.dbo.edc_transclm b on b.trans_id=a.transaction_id "
                    + "inner join edc_prj.dbo.edc_terminal c on c.tid=a.tid "
                    + "where substring(a.no_kartu, 6, 5) in (" + policies + ") "
                    + "and a.request_function='Validation' and a.icd<>'' and len(a.icd)>2 "
                    + "and b.trans_id is null "
                    + "and a.status='true' ";


            if (whereActive!=null) qry += "and (" + whereActive + ") ";

            String order = "order by request_date desc ";
            Integer recordsCount = (Integer) s.createSQLQuery(count + qry).uniqueResult();
            pgActiveTransactions.setTotalSize(recordsCount);

            List<Object[]> l = s.createSQLQuery(select + qry + order).setFirstResult(offset).setMaxResults(limit).list();
            for (Object[] o : l) {
                String replyVariable = Libs.nn(o[6]).trim();
                String name = replyVariable.split("\\;")[5];

                EDCTransactionPOJO edcTransactionPOJO = new EDCTransactionPOJO();
                edcTransactionPOJO.setTrans_id(Libs.nn(o[0]));
                edcTransactionPOJO.setCard_number(Libs.nn(o[1]));
                edcTransactionPOJO.setType(Libs.getClaimType(Libs.nn(o[4])));

                Listitem li = new Listitem();
                li.setValue(edcTransactionPOJO);

                li.appendChild(new Listcell(edcTransactionPOJO.getTrans_id()));
                li.appendChild(new Listcell(edcTransactionPOJO.getCard_number()));
                li.appendChild(new Listcell(edcTransactionPOJO.getType()));
                li.appendChild(new Listcell(name));
                li.appendChild(new Listcell(Libs.nn(o[5]).trim()));
                li.appendChild(new Listcell(Libs.getICDByCode(Libs.nn(o[5]).trim())));
                li.appendChild(new Listcell(Libs.getHospitalById(Libs.nn(o[7]).trim())));
                li.appendChild(new Listcell(Libs.nn(o[3])));

                lbActiveTransactions.appendChild(li);
            }
        } catch (Exception ex) {
            log.error("populateActiveTransactions", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    private void populateClosedTransactions(int offset, int limit) {
        lbClosedTransactions.getItems().clear();
        Session s = Libs.sfEDC.openSession();
        try {
//            Create policy numbers
            String policies = "";
            for (String policy : Libs.policyMap.keySet()) policies += "'" + policy.substring(policy.lastIndexOf("-")+1) + "', ";
            if (policies.endsWith(", ")) policies = policies.substring(0, policies.length()-2);

            String count = "select count(*) ";

            String select = "select "
                    + "a.trans_id, a.no_kartu, a.hclmtclaim, "
                    + "a.hclmdiscd1, a.hclmdiscd2, a.hclmdiscd3, "
                    + "a.hclmnhoscd, " //6
                    + "a.hclmrdatey, a.hclmrdatem, a.hclmrdated, "
                    + "(a.hclmpcode1 + a.hclmpcode2) as plan_code, "
                    + "a.hclmyy, a.hclmpono, a.hclmidxno, a.hclmseqno, "
                    + Libs.createListFieldString("a.hclmcamt") + ", "
                    + Libs.createListFieldString("a.hclmaamt") + " ";

            String qry = "from edc_prj.dbo.edc_transclm a "
                    + "where "
                    + "substring(no_kartu, 6, 5) in (" + policies + ") "
                    + "and a.hclmrecid<>'C' ";

            if (whereClosed!=null) qry += "and (" + whereClosed + ") ";

            String order = "order by a.hclmrdatey desc, a.hclmrdatem desc, a.hclmrdated desc ";
            Integer recordsCount = (Integer) s.createSQLQuery(count + qry).uniqueResult();
            pgClosedTransactions.setTotalSize(recordsCount);

            List<Object[]> l = s.createSQLQuery(select + qry + order).setFirstResult(offset).setMaxResults(limit).list();
            for (Object[] o : l) {
                String icd = Libs.nn(o[3]).trim();
                if (!Libs.nn(o[4]).trim().isEmpty()) icd += ", " + Libs.nn(o[4]).trim();
                if (!Libs.nn(o[5]).trim().isEmpty()) icd += ", " + Libs.nn(o[5]).trim();

                String cardNumber = Libs.nn(o[1]);
                String memberName = Libs.getMemberByCardNumber(cardNumber);

                EDCTransactionPOJO edcTransactionPOJO = new EDCTransactionPOJO();
                edcTransactionPOJO.setTrans_id(Libs.nn(o[0]));
                edcTransactionPOJO.setCard_number(Libs.nn(o[1]));
                edcTransactionPOJO.setType(Libs.getClaimType(Libs.nn(o[2])));
                edcTransactionPOJO.setIcd(icd);
                edcTransactionPOJO.setDate(Libs.nn(o[7]) + "-" + Libs.nn(o[8]) + "-" + Libs.nn(o[9]));
                edcTransactionPOJO.setPlan(Libs.nn(o[10]).trim());
                edcTransactionPOJO.setYear(Integer.valueOf(Libs.nn(o[11])));
                edcTransactionPOJO.setPolicy_number(Integer.valueOf(Libs.nn(o[12])));
                edcTransactionPOJO.setIdx(Integer.valueOf(Libs.nn(o[13])));
                edcTransactionPOJO.setSeq(Libs.nn(o[14]));
                edcTransactionPOJO.setName(memberName);

                Double[] approved = new Double[30];
                Double[] proposed = new Double[30];

                for (int i=0; i<30; i++) {
                    proposed[i] = Double.valueOf(Libs.nn(o[15 + i]));
                    approved[i] = Double.valueOf(Libs.nn(o[45 + i]));
                }

                edcTransactionPOJO.setApproved(approved);
                edcTransactionPOJO.setProposed(proposed);

                Listitem li = new Listitem();
                li.setValue(edcTransactionPOJO);

                li.appendChild(new Listcell(edcTransactionPOJO.getTrans_id()));
                li.appendChild(new Listcell(edcTransactionPOJO.getCard_number()));
                li.appendChild(new Listcell(edcTransactionPOJO.getType()));
                li.appendChild(new Listcell(memberName));
                li.appendChild(new Listcell(edcTransactionPOJO.getPlan()));
                li.appendChild(new Listcell(edcTransactionPOJO.getIcd()));
                li.appendChild(new Listcell(Libs.getICDByCode(edcTransactionPOJO.getIcd())));
                li.appendChild(new Listcell(Libs.getHospitalById(Libs.nn(o[6]).trim())));
                li.appendChild(new Listcell(edcTransactionPOJO.getDate()));

                lbClosedTransactions.appendChild(li);
            }
        } catch (Exception ex) {
            log.error("populateClosedTransactions", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    private void populateManualTransactions(int offset, int limit) {
        lbManualTransactions.getItems().clear();
        Session s = Libs.sfEDC.openSession();
        try {
//            Create policy numbers
            String policies = "";
            for (String policy : Libs.policyMap.keySet()) policies += "'" + policy.substring(policy.lastIndexOf("-")+1) + "', ";
            if (policies.endsWith(", ")) policies = policies.substring(0, policies.length()-2);

            String count = "select count(*) ";

            String select = "select "
                    + "a.transaction_id, a.no_kartu, a.tid, a.request_date, "
                    + "a.type, a.icd, a.reply_variable, c.pro_code ";

            String qry = "from edc_prj.dbo.ms_log_transaction a "
                    + "inner join edc_prj.dbo.edc_terminal c on c.tid=a.tid "
                    + "where substring(a.no_kartu, 6, 5) in (" + policies + ") "
                    + "and a.request_function like 'Verification%' and a.icd<>'' and len(a.icd)>2 "
                    + "and a.flg<>'C' "
                    + "and a.description is not null ";

            if (whereManual!=null) qry += "and (" + whereManual + ") ";

            String order = "order by request_date desc ";
            Integer recordsCount = (Integer) s.createSQLQuery(count + qry).uniqueResult();
            pgManualTransactions.setTotalSize(recordsCount);

            List<Object[]> l = s.createSQLQuery(select + qry + order).setFirstResult(offset).setMaxResults(limit).list();
            for (Object[] o : l) {
                String replyVariable = Libs.nn(o[6]).trim();
                String name = replyVariable.split("\\;")[5];

                EDCTransactionPOJO edcTransactionPOJO = new EDCTransactionPOJO();
                edcTransactionPOJO.setTrans_id(Libs.nn(o[0]));
                edcTransactionPOJO.setCard_number(Libs.nn(o[1]));
                edcTransactionPOJO.setType(Libs.nn(o[4]));

                Listitem li = new Listitem();
                li.setValue(edcTransactionPOJO);

                li.appendChild(new Listcell(edcTransactionPOJO.getTrans_id()));
                li.appendChild(new Listcell(edcTransactionPOJO.getCard_number()));
                li.appendChild(new Listcell(Libs.getClaimType(edcTransactionPOJO.getType())));
                li.appendChild(new Listcell(name));
                li.appendChild(new Listcell(Libs.nn(o[5]).trim()));
                li.appendChild(new Listcell(Libs.getICDByCode(Libs.nn(o[5]).trim())));
                li.appendChild(new Listcell(Libs.getHospitalById(Libs.nn(o[7]).trim())));
                li.appendChild(new Listcell(Libs.nn(o[3])));

                lbManualTransactions.appendChild(li);
            }
        } catch (Exception ex) {
            log.error("populateManualTransactions", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    public void refreshActiveTransactions() {
        whereActive = null;
        populateActiveTransactions(0, pgActiveTransactions.getPageSize());
    }

    public void refreshClosedTransactions() {
        whereClosed = null;
        populateClosedTransactions(0, pgClosedTransactions.getPageSize());
    }

    public void refreshManualTransactions() {
        whereManual = null;
        populateManualTransactions(0, pgManualTransactions.getPageSize());
    }

    public void quickSearchActiveTransactions() {
        String val = ((Textbox) getFellow("tQuickSearchActiveTransactions")).getText();
        if (!val.isEmpty()) {
            whereActive = "a.no_kartu like '%" + val + "%' ";
            populateActiveTransactions(0, pgActiveTransactions.getPageSize());
        } else refreshActiveTransactions();
    }

    public void quickSearchClosedTransactions() {
        String val = ((Textbox) getFellow("tQuickSearchClosedTransactions")).getText();
        if (!val.isEmpty()) {
            whereClosed = "a.no_kartu like '%" + val + "%' ";
            populateClosedTransactions(0, pgClosedTransactions.getPageSize());
        } else refreshClosedTransactions();
    }

    public void quickSearchManualTransactions() {
        String val = ((Textbox) getFellow("tQuickSearchManualTransactions")).getText();
        if (!val.isEmpty()) {
            whereManual = "a.no_kartu like '%" + val + "%' ";
            populateManualTransactions(0, pgManualTransactions.getPageSize());
        } else refreshManualTransactions();
    }

    public void exportActiveTransactions() {
        Libs.showDeveloping();
    }

    public void exportClosedTransactions() {
        Libs.showDeveloping();
    }

    public void exportManualTransactions() {
        Libs.showDeveloping();
    }

    public void showClosedTransactionDetail() {
        Window w = (Window) Executions.createComponents("views/EDCDetail.zul", this, null);
        w.setAttribute("edc", lbClosedTransactions.getSelectedItem().getValue());
        w.doModal();
    }

    public void showManualTransactionDetail() {
        Session s = Libs.sfEDC.openSession();
        try {
            EDCTransactionPOJO edc = lbManualTransactions.getSelectedItem().getValue();

            String select = "select "
                    + "a.trans_id, a.no_kartu, a.hclmtclaim, "
                    + "a.hclmdiscd1, a.hclmdiscd2, a.hclmdiscd3, "
                    + "a.hclmnhoscd, " //6
                    + "a.hclmrdatey, a.hclmrdatem, a.hclmrdated, "
                    + "(a.hclmpcode1 + a.hclmpcode2) as plan_code, "
                    + "a.hclmyy, a.hclmpono, a.hclmidxno, a.hclmseqno, "
                    + Libs.createListFieldString("a.hclmcamt") + ", "
                    + Libs.createListFieldString("a.hclmaamt") + " ";

            String qry = "from edc_prj.dbo.edc_transclm a "
                    + "where trans_id=" + edc.getTrans_id();

            List<Object[]> l = s.createSQLQuery(select + qry).list();
            if (l.size()==1) {
                Object[] o = l.get(0);

                String icd = Libs.nn(o[3]).trim();
                if (!Libs.nn(o[4]).trim().isEmpty()) icd += ", " + Libs.nn(o[4]).trim();
                if (!Libs.nn(o[5]).trim().isEmpty()) icd += ", " + Libs.nn(o[5]).trim();

                String cardNumber = Libs.nn(o[1]);
                String memberName = Libs.getMemberByCardNumber(cardNumber);

                edc = new EDCTransactionPOJO();
                edc.setTrans_id(Libs.nn(o[0]));
                edc.setCard_number(Libs.nn(o[1]));
                edc.setType(Libs.nn(o[2]));
                edc.setIcd(icd);
                edc.setDate(Libs.nn(o[7]) + "-" + Libs.nn(o[8]) + "-" + Libs.nn(o[9]));
                edc.setPlan(Libs.nn(o[10]).trim());
                edc.setYear(Integer.valueOf(Libs.nn(o[11])));
                edc.setPolicy_number(Integer.valueOf(Libs.nn(o[12])));
                edc.setIdx(Integer.valueOf(Libs.nn(o[13])));
                edc.setSeq(Libs.nn(o[14]));
                edc.setName(memberName);

                Double[] approved = new Double[30];
                Double[] proposed = new Double[30];

                for (int i=0; i<30; i++) {
                    proposed[i] = Double.valueOf(Libs.nn(o[15 + i]));
                    approved[i] = Double.valueOf(Libs.nn(o[45 + i]));
                }

                edc.setApproved(approved);
                edc.setProposed(proposed);

                Window w = (Window) Executions.createComponents("views/EDCDetail.zul", this, null);
                w.setAttribute("edc", edc);
                w.doModal();
            }
        } catch (Exception ex) {
            log.error("showManualTransactionDetail", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

}
