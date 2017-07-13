package com.imc.ocisv3.controllers;

import com.imc.ocisv3.tools.Libs;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by faizal on 10/27/13.
 */
public class InHospitalMonitoringDetailController extends Window {

    private Logger log = LoggerFactory.getLogger(InHospitalMonitoringDetailController.class);
    private Object[] ihm;
    private Listbox lb;

    public void onCreate() {
        if (!Libs.checkSession()) {
            ihm = (Object[]) getAttribute("ihm");
            initComponents();
            populate();
        }
    }

    private void initComponents() {
        lb = (Listbox) getFellow("lb");

        getCaption().setLabel("In Hospital Monitoring Detail [" + Libs.nn(ihm[8]) + "]");
    }

    private void populate() {
        populateInformation();
        populateMonitoringRecords();
    }

    private void populateInformation() {
        ((Label) getFellow("lGLNumber")).setValue(Libs.nn(ihm[0]));
        ((Label) getFellow("lCardNumber")).setValue(Libs.nn(ihm[7]));
        ((Label) getFellow("lName")).setValue(Libs.nn(ihm[8]));
        ((Label) getFellow("lProvider")).setValue(Libs.nn(ihm[9]).trim());

        String dob = Libs.nn(ihm[12]) + "-" + Libs.nn(ihm[13]) + "-" + Libs.nn(ihm[14]);
        int ageDays = 0;
        try {
            ageDays = Libs.getDiffDays(new SimpleDateFormat("yyyy-MM-dd").parse(dob), new Date());
        } catch (Exception ex) {
            log.error("populateInformation", ex);
        }

        ((Label) getFellow("lDOB")).setValue(dob);

        Label lAge = (Label) getFellow("lAge");
        lAge.setStyle("text-align:right;");
        lAge.setValue("(" + (ageDays / 365) + ")");

        ((Label) getFellow("lSex")).setValue(Libs.nn(ihm[15]));

        ((Label) getFellow("lRoomClass")).setValue(Libs.nn(ihm[16]));

        Label lRoomPrice = (Label) getFellow("lRoomPrice");
        lRoomPrice.setStyle("text-align:right;");
        lRoomPrice.setValue(new DecimalFormat("#,###.##").format(Double.valueOf(Libs.nn(ihm[17]))));
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tglKeluar = sdf.format((Date)ihm[24]);
        
        System.out.println("ini tanggal keluar dodol : "+tglKeluar);

        Label lStatus = (Label) getFellow("lStatus");
        if (Libs.nn(ihm[11]).trim().equals("0")) {
            lStatus.setValue("CANCELED");
        } else if (tglKeluar.equalsIgnoreCase("1900-01-01")) {
            lStatus.setValue("ACTIVE");
            lStatus.setStyle("color:#00FF00");
        } else {
            lStatus.setValue("CLOSED");
            lStatus.setStyle("color:#FF0000;");
        }

        ((Label) getFellow("lServiceIn")).setValue(ihm[28]==null ? (Libs.nn(ihm[18]).startsWith("1900") ? "" : Libs.nn(ihm[18]).substring(0, 10)) : Libs.nn(ihm[28]));
        ((Label) getFellow("lCompanyName")).setValue(Libs.nn(ihm[19]).trim());
        ((Label) getFellow("lDiagnosis")).setValue(Libs.nn(ihm[20]).trim() + " (" + Libs.nn(ihm[21]).trim() + ")");
        ((Label) getFellow("lPIC")).setValue(Libs.nn(ihm[22]).trim());
        ((Label) getFellow("lGLDate")).setValue(Libs.nn(ihm[23]).substring(0, 10));
        if(tglKeluar.equalsIgnoreCase("1900-01-01"))((Label) getFellow("lServiceOut")).setValue(null);
        else ((Label) getFellow("lServiceOut")).setValue(tglKeluar);

        Label lLastCostEstimation = (Label) getFellow("lLastCostEstimation");
        lLastCostEstimation.setStyle("text-align:right;");
        lLastCostEstimation.setValue(new DecimalFormat("#,###.##").format(Double.valueOf(Libs.nn(ihm[25]))));

        ((Label) getFellow("lRemarks")).setValue(Libs.nn(ihm[26]));
        ((Label) getFellow("lMaritalStatus")).setValue(Libs.nn(ihm[27]));

        populateFinalInformation();
    }

    private void populateMonitoringRecords() {
        lb.getItems().clear();
        Session s = Libs.sfEDC.openSession();
        try {
            String qry = "select "
                    + "a.mde_date, a.mde_pic, a.mde_followup, a.mde_description, "
                    + "a.mde_estimation, a.mde_excess, a.mde_lastdiagnose, a.mde_adminrs "
                    + "from bd_bdiis.dbo.tr_monitoring_detail a "
                    + "inner join bd_bdiis.dbo.tr_monitoring b on b.mon_id=a.mon_id "
                    + "where "
                    + "b.mon_guarantee='" + Libs.nn(ihm[0]) + "' and "
                    + "b.mon_card_no='" + Libs.nn(ihm[7]) + "' and "
                    + "a.mde_flag='1' "
                    + "order by a.mde_date desc ";

            List<Object[]> l = s.createSQLQuery(qry).list();
            for (Object[] o : l) {
                Listitem li = new Listitem();
                li.setValue(o);

                li.appendChild(new Listcell(Libs.nn(o[0]).substring(0, 19)));
                li.appendChild(new Listcell(Libs.nn(o[3]).trim()));
                li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[4])), "#,###.##"));
                li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[5])), "#,###.##"));
                li.appendChild(new Listcell(Libs.nn(o[6]).trim()));
                li.appendChild(new Listcell(Libs.nn(o[1]).trim()));
                li.appendChild(new Listcell(Libs.nn(o[2]).substring(0, 10)));
                li.appendChild(new Listcell(Libs.nn(o[7]).trim()));

                lb.appendChild(li);
            }
        } catch (Exception ex) {
            log.error("populateMonitoringRecords", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    private void populateFinalInformation() {
        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + "a.hclmdiscd1, a.hclmdiscd2, a.hclmdiscd3, "
                    + "(" + Libs.createAddFieldString("a.hclmaamt") + ") as approved, "
                    + "b.nosuratkwitansi, b.tanggal_perawatan2 "
                    + "from idnhltpf.dbo.hltclm a "
                    + "inner join aso.dbo.pre_ip_provider b on b.thn_polis=a.hclmyy and b.no_polis=a.hclmpono and b.idx=a.hclmidxno and b.seq=a.hclmseqno and a.hclmcno='IDN/'+b.no_hid and b.no_surat_jaminan='" + Libs.nn(ihm[0]) + "' "
                    + "where "
                    + "a.hclmyy=" + Libs.nn(ihm[1]) + " and "
                    + "a.hclmpono=" + Libs.nn(ihm[4]) + " and "
                    + "a.hclmidxno=" + Libs.nn(ihm[5]) + " and "
                    + "a.hclmseqno='" + Libs.nn(ihm[6]) + "' ";

            List<Object[]> l = s.createSQLQuery(qry).list();
            if (l.size()==1) {
                Object[] o = l.get(0);
                String finalDiagnosis = Libs.nn(o[0]).trim();
                if (!Libs.nn(o[1]).trim().isEmpty()) finalDiagnosis += ", " + Libs.nn(o[1]).trim();
                if (!Libs.nn(o[2]).trim().isEmpty()) finalDiagnosis += ", " + Libs.nn(o[2]).trim();

                ((Label) getFellow("lFinalDiagnosis")).setValue(finalDiagnosis);
                ((Label) getFellow("lReceiptNumber")).setValue(Libs.nn(o[4]).trim());
//                ((Label) getFellow("lServiceOut")).setValue(Libs.nn(o[5]).substring(0, 10));

                Label lFinalAmount = (Label) getFellow("lFinalAmount");
                lFinalAmount.setStyle("font-weight:bold;");
                lFinalAmount.setValue(new DecimalFormat("#,###.##").format(Double.valueOf(Libs.nn(o[3]))));
            }
        } catch (Exception ex) {
            log.error("populateFinalInformation", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

}
