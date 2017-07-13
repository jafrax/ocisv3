package com.imc.ocisv3.controllers;

import com.imc.ocisv3.pojos.ClaimPOJO;
import com.imc.ocisv3.pojos.PolicyPOJO;
import com.imc.ocisv3.tools.Libs;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.*;
import org.zkoss.zul.event.PagingEvent;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by faizal on 10/31/13.
 */
public class ClaimListController extends Window {

    private Logger log = LoggerFactory.getLogger(ClaimListController.class);
    private String policy;
    private String key;
    private Listbox lb;
    private Paging pg;
    private String where;
    private Listfooter ftr;

    public void onCreate() {
        if (!Libs.checkSession()) {
            policy = (String) getAttribute("policy");
            key = (String) getAttribute("key");

            initComponents();
            populateCount();
            populate(0, pg.getPageSize());
        }
    }

    private void initComponents() {
        lb = (Listbox) getFellow("lb");
        pg = (Paging) getFellow("pg");
        ftr = (Listfooter) getFellow("ftr");

        pg.addEventListener("onPaging", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                PagingEvent evt = (PagingEvent) event;
                populate(evt.getActivePage()*pg.getPageSize(), pg.getPageSize());
            }
        });

        String title = "Claim History [";
        if (policy.isEmpty()) title += "All Products - "; else title += policy;
        if (!key.isEmpty()) {
            String[] keySeg = key.split("\\-");
            title += " " + keySeg[0] + " " + Libs.shortMonths[Integer.valueOf(keySeg[1])] + " ";
            if (keySeg[2].equals("I")) title += "INPATIENT";
            if (keySeg[2].equals("O")) title += "OUTPATIENT";
            if (keySeg[2].equals("R")) title += "MATERNITY";
            if (keySeg[2].equals("D")) title += "DENTAL";
            if (keySeg[2].equals("G")) title += "GLASSES";
        }
        title += "]";

        getCaption().setLabel(title);
    }

    private void populate(int offset, int limit) {
        lb.getItems().clear();
        Session s = Libs.sfDB.openSession();
        try {
            String selectTotalApproved = "select "
                    + "sum(" + Libs.createAddFieldString("a.hclmaamt") + ") as approved ";

            String select = "select "
                    + "a.hclmcno, a.hclmyy, a.hclmbr, a.hclmdist, a.hclmpono, "
                    + "b.hhdrname, a.hclmidxno, a.hclmseqno, "
                    + "c.hdt1name, a.hclmtclaim, "
                    + "(" + Libs.createAddFieldString("a.hclmcamt") + ") as proposed, "
                    + "(" + Libs.createAddFieldString("a.hclmaamt") + ") as approved, "
                    + "d.hproname, "
                    + "a.hclmcount, "
                    + "e.hempcnpol, e.hempcnid, "
                    + "f.hmem2data1 "; //16

            String qry = "from idnhltpf.dbo.hltclm a "
                    + "inner join idnhltpf.dbo.hlthdr b on b.hhdryy=a.hclmyy and b.hhdrpono=a.hclmpono "
                    + "inner join idnhltpf.dbo.hltdt1 c on c.hdt1yy=a.hclmyy and c.hdt1pono=a.hclmpono and c.hdt1idxno=a.hclmidxno and c.hdt1seqno=a.hclmseqno and c.hdt1ctr=0 "
                    + "inner join idnhltpf.dbo.hltpro d on d.hpronomor=a.hclmnhoscd "
                    + "inner join idnhltpf.dbo.hltemp e on e.hempyy=a.hclmyy and e.hemppono=a.hclmpono and e.hempidxno=a.hclmidxno and e.hempseqno=a.hclmseqno and e.hempctr=0 "
                    + "left outer join idnhltpf.dbo.hltmemo2 f on f.hmem2yy=a.hclmyy and f.hmem2pono=a.hclmpono and f.hmem2idxno=a.hclmidxno and f.hmem2seqno=a.hclmseqno and f.hmem2claim=a.hclmtclaim and f.hmem2count=a.hclmcount "
                    + "where "
                    + "b.hhdrinsid='" + Libs.getInsuranceId() + "' "
                    + "and a.hclmrecid<>'C' and a.hclmidxno < 99989 and a.hclmpono <> 99999 ";

            if (getAttribute("excess")!=null && ((Boolean) getAttribute("excess"))) {
                qry += "and ((" + Libs.createAddFieldString("a.hclmcamt") + ")- "
                        + "(" + Libs.createAddFieldString("a.hclmaamt") + ")>0) ";
            }

            if (where!=null) qry += "and (" + where + ") ";

            if (!policy.isEmpty()) {
                String[] policySeg = policy.split("\\-");
                qry += "and a.hclmyy=" + policySeg[0] + " and "
                        + "a.hclmpono=" + policySeg[3] + " ";
            }

            if (!key.isEmpty()) {
                String[] keySeg = key.split("\\-");
                qry += "and a.hclmcdatey=" + keySeg[0] + " and "
                        + "a.hclmcdatem=" + (Integer.valueOf(keySeg[1])+1) + " and "
                        + "a.hclmtclaim='" + keySeg[2] + "' ";
            }

            String order = "order by convert(date,convert(varchar,a.hclmcdated)+'-'+convert(varchar,a.hclmcdatem)+'-'+convert(varchar,a.hclmcdatey),105) desc ";
            
           

            List<Object[]> l = s.createSQLQuery(select + qry + order).setFirstResult(offset).setMaxResults(limit).list();
            for (Object[] o : l) {
                String policyName = Libs.nn(o[5]);
                if (Libs.config.get("demo_mode").equals("true") && Libs.getInsuranceId().equals("00051")) policyName = Libs.nn(Libs.config.get("demo_name"));

                String remarks = Libs.nn(o[16]).trim();
                String provider = Libs.nn(o[12]).trim();
                if (remarks.indexOf("[")>-1 && remarks.indexOf("]")>-1) {
                    provider = remarks.substring(remarks.indexOf("[")+1, remarks.indexOf("]"));
                }

                Listitem li = new Listitem();

                li.appendChild(new Listcell(Libs.nn(o[0])));
                li.appendChild(new Listcell(Libs.nn(o[14]).trim()));
                li.appendChild(new Listcell(policyName));
                li.appendChild(new Listcell(o[6] + "-" + o[7]));
                li.appendChild(new Listcell(Libs.nn(o[8])));
                li.appendChild(new Listcell(""));
                li.appendChild(new Listcell(Libs.nn(o[9])));
                li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[10])), "#,###.##"));
                li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[11])), "#,###.##"));
                li.appendChild(new Listcell(provider));

                lb.appendChild(li);
                
                PolicyPOJO policyPOJO = new PolicyPOJO();
                policyPOJO.setYear(Integer.valueOf(Libs.nn(o[1])));
                policyPOJO.setBr(Integer.valueOf(Libs.nn(o[2])));
                policyPOJO.setDist(Integer.valueOf(Libs.nn(o[3])));
                policyPOJO.setPolicy_number(Integer.valueOf(Libs.nn(o[4])));
                policyPOJO.setName(Libs.nn(o[5]));

                ClaimPOJO claimPOJO = new ClaimPOJO();
                claimPOJO.setPolicy(policyPOJO);
                claimPOJO.setClaim_number(Libs.nn(o[0]).trim());
                claimPOJO.setPolicy_number(Libs.nn(o[1]) + '-' + Libs.nn(o[2]) + '-' + Libs.nn(o[3]) + '-' + Libs.nn(o[4]));
                claimPOJO.setIndex(Libs.nn(o[6]) + '-' + Libs.nn(o[7]).trim());
                claimPOJO.setClaim_count(Integer.valueOf(Libs.nn(o[13])));

                li.setValue(claimPOJO);
                
              
            }

            BigDecimal totalApproved = (BigDecimal) s.createSQLQuery(selectTotalApproved + qry).uniqueResult();
            ftr.setLabel("Total approved by analyst: " + new DecimalFormat("#,###.##").format(totalApproved));
        } catch (Exception ex) {
            log.error("populate", ex);
            
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    private void populateCount() {
        Session s = Libs.sfDB.openSession();
        try {
            String countSelect = "select count(*) ";

            String qry = "from idnhltpf.dbo.hltclm a "
                    + "inner join idnhltpf.dbo.hlthdr b on b.hhdryy=a.hclmyy and b.hhdrpono=a.hclmpono "
                    + "where "
                    + "b.hhdrinsid='" + Libs.getInsuranceId() + "' "
                    + "and a.hclmrecid<>'C' and a.hclmidxno < 99989 and a.hclmpono <> 99999 ";

            if (getAttribute("excess")!=null && ((Boolean) getAttribute("excess"))) {
                qry += "and ((" + Libs.createAddFieldString("a.hclmcamt") + ")- "
                        + "(" + Libs.createAddFieldString("a.hclmaamt") + ")>0) ";
            }

            if (where!=null) qry += "and (" + where + ") ";

            if (!policy.isEmpty()) {
                String[] policySeg = policy.split("\\-");
                qry += "and a.hclmyy=" + policySeg[0] + " and "
                        + "a.hclmpono=" + policySeg[3] + " ";
            }

            if (!key.isEmpty()) {
                String[] keySeg = key.split("\\-");
                qry += "and a.hclmcdatey=" + keySeg[0] + " and "
                        + "a.hclmcdatem=" + (Integer.valueOf(keySeg[1])+1) + " and "
                        + "a.hclmtclaim='" + keySeg[2] + "' ";
            }

            Integer count = (Integer) s.createSQLQuery(countSelect + qry).uniqueResult();
            pg.setTotalSize(count);
        } catch (Exception ex) {
            log.error("populateCount", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    public void showClaimDetail() {
        Window w = (Window) Executions.createComponents("views/ClaimDetail.zul", this, null);
        w.setAttribute("claim", lb.getSelectedItem().getValue());
        w.doModal();
    }

    public void refresh() {
        where = null;
        populateCount();
        populate(0, pg.getPageSize());
    }

    public void quickSearch() {
        String val = ((Textbox) getFellow("tQuickSearch")).getText();
        if (!val.isEmpty()) {
            where = "convert(varchar,c.hdt1ncard) like '%" + val + "%' or "
                    + "c.hdt1name like '%" + val + "%' or "
                    + "e.hempcnpol like '%" + val + "%' or "
                    + "e.hempcnid like '%" + val + "%' or "
                    + "a.hclmcno like '%" + val + "%' ";
            populate(0, pg.getPageSize());
        } else refresh();
    }

    public void export() {
        Libs.showDeveloping();
    }

}
