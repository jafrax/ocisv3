package com.imc.ocisv3.controllers;

import com.imc.ocisv3.tools.Libs;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import org.zkoss.zul.event.PagingEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by faizal on 10/25/13.
 */
public class InHospitalMonitoringController extends Window {

    private Logger log = LoggerFactory.getLogger(InHospitalMonitoringController.class);
    private Listbox lb;
    private Combobox cbStatus;
    private Paging pg;
    private String where;
    private String userProductViewrestriction;
    
    private String polis ="";
    private List polisList;


    public void onCreate() {
        if (!Libs.checkSession()) {
            userProductViewrestriction = Libs.restrictUserProductView.get(Libs.getUser());
            initComponents();
            
            polisList = Libs.getPolisByUserId(Libs.getUser());
            for(int i=0; i < polisList.size(); i++){
        		polis=polis+"'"+(String)polisList.get(i)+"'"+",";
        	}
            if(polis.length() > 1)polis = polis.substring(0, polis.length()-1);
            
            populate(0, pg.getPageSize());
        }
    }

    private void initComponents() {
        lb = (Listbox) getFellow("lb");
        pg = (Paging) getFellow("pg");
        cbStatus = (Combobox)getFellow("cbStatus");

        pg.addEventListener("onPaging", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                PagingEvent evt = (PagingEvent) event;
                populate(evt.getActivePage()*pg.getPageSize(), pg.getPageSize());
            }
        });
        
        cbStatus.addEventListener(Events.ON_SELECT, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				quickSearch();
			}
		});
        
        cbStatus.setSelectedIndex(0);
        
        
    }

    private void populate(int offset, int limit) {
        lb.getItems().clear();

        Session s = Libs.sfDB.openSession();
        try {
        	String insid="";
        	List products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
        	
            /*String countQry = "select count(*) from idnhltpf.dbo.hlthdr b "
                    + "inner join surjam_new.dbo.ms_surjam a on b.hhdryy=a.thn_polis and b.hhdrpono=a.no_polis "
                    + "inner join idnhltpf.dbo.hltdt1 c on c.hdt1yy=a.thn_polis and c.HDT1BR=a.Br_Polis and c.HDT1DIST=a.Dist_Polis and c.hdt1pono=a.no_polis and c.hdt1idxno=a.idx and c.hdt1seqno=a.seq and c.hdt1ctr=0 "
                    + "where "
                    + "b.hhdrinsid";
            		if(products.size() > 0) countQry = countQry + " in  ("+insid+") ";
            		else countQry = countQry + "='" + Libs.getInsuranceId() + "' ";  
            		
            		if(polisList.size() > 0){
            			countQry = countQry + "and convert(varchar,b.hhdryy)+'-'+convert(varchar,b.hhdrbr)+'-'+convert(varchar,b.hhdrdist)+'-'+convert(varchar,b.hhdrpono) "
            					  + "in ("+polis+") ";
            		} */
        	
        	String countQry = "select count(1) ";
            		
            String qry = "select "
                    + "a.nosurat, "
                    + "a.thn_polis, a.br_polis, a.dist_polis, a.no_polis, "
                    + "a.idx, a.seq, a.nokartu, "
                    + "a.nmpeserta, a.namars, "
                    + "a.kelas, a.kettrans, "
                    + "c.hdt1bdtyy, c.hdt1bdtmm, c.hdt1bdtdd, " // 12
                    + "c.hdt1sex, "
                    + "a.klskamar, a.hrgkamar, " // 16
                    + "a.tg_rawat, "
                    + "b.hhdrname, "
                    + "a.diagnosa, a.icd, " // 20
                    + "a.user_prv, tgl_print, "
                    + "a.tg_keluar, a.estimasi, a.notepenting, "
                    + "c.hdt1mstat, " // 27
                    + "(convert(varchar,hclmsinyy)+'-'+convert(varchar,hclmsinmm)+'-'+convert(varchar,hclmsindd)) as sin, "
                    + "(convert(varchar,hclmsoutyy)+'-'+convert(varchar,hclmsoutmm)+'-'+convert(varchar,hclmsoutdd)) as sout ";
            
                    
               String from = "from idnhltpf.dbo.hlthdr b "
                    + "inner join surjam_new.dbo.ms_surjam a on b.hhdryy=a.thn_polis and b.hhdrpono=a.no_polis "
                    + "inner join idnhltpf.dbo.hltdt1 c on c.hdt1yy=a.thn_polis and c.HDT1BR=a.Br_Polis and c.HDT1DIST=a.Dist_Polis and c.hdt1pono=a.no_polis and c.hdt1idxno=a.idx and c.hdt1seqno=a.seq and c.hdt1ctr=0 "
                    + "left outer join idnhltpf.dbo.hltclm d on d.hclmcno='IDN/' + a.no_hid "
                    + "where "
                    + "b.hhdrinsid";
            		if(products.size() > 0) from = from + " in  ("+insid+") ";
            		else from = from + "='" + Libs.getInsuranceId() + "' ";  
            		
            		
            		if(polisList.size() > 0){
            			from = from + "and convert(varchar,b.hhdryy)+'-'+convert(varchar,b.hhdrbr)+'-'+convert(varchar,b.hhdrdist)+'-'+convert(varchar,b.hhdrpono) "
            					  + "in ("+polis+") ";
            		} 
            		
            		if(cbStatus.getSelectedIndex() == 0){
//            			countQry = countQry + " and a.kettrans='1' ";
            			from = from + " and flg='1' and a.kettrans <> '0' and a.Tg_Keluar = convert(datetime, '01-01-1900', 110) ";
//            			qry = qry + " and a.kettrans='1' and (convert(varchar,hclmsoutyy)+'-'+convert(varchar,hclmsoutmm)+'-'+convert(varchar,hclmsoutdd)) <> '0-0-0'";
//            			from = from + " and a.kettrans <> '0' and (convert(varchar,hclmsoutyy)+'-'+convert(varchar,hclmsoutmm)+'-'+convert(varchar,hclmsoutdd)) = '0-0-0'";
            		}
            		else if(cbStatus.getSelectedIndex() == 1){
//            			countQry = countQry + " and a.kettrans='2' ";
            			from = from + "and flg='1' and a.kettrans <> '0' and a.Tg_Keluar > convert(datetime, '01-01-1900', 110) ";
//            			qry = qry + " and a.kettrans='2' ";
//            			from = from + "and a.kettrans <> '0' and (convert(varchar,hclmsoutyy)+'-'+convert(varchar,hclmsoutmm)+'-'+convert(varchar,hclmsoutdd)) <> '0-0-0'";
            		}
            		else if(cbStatus.getSelectedIndex() == 2){
            			from = from + " and a.kettrans='0' ";
//            			from = from + " and a.kettrans='0' ";
            		}
            
            if (!Libs.nn(userProductViewrestriction).isEmpty()) qry += "and b.hhdrpono in (" + userProductViewrestriction + ") ";

            if (where!=null) {
                from += "and (" + where + ") ";
//                from += "and (" + where + ") ";
            }

            String order= " order by nosurat desc;";
//            
            System.out.println(countQry + from);
//            System.out.println(qry);

            Integer count = (Integer) s.createSQLQuery(countQry+from).uniqueResult();
            pg.setTotalSize(count);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String tglKeluar = null;

            List<Object[]> l = s.createSQLQuery(qry+from+order).setFirstResult(offset).setMaxResults(limit).list();
            for (Object[] o : l) {
                Listcell lcStatus = new Listcell();
                Label lStatus = new Label();
                
               tglKeluar = sdf.format((Date)o[24]);
                
                if (Libs.nn(o[11]).trim().equals("0")) {
                    lStatus.setValue("CANCELED");
                } else if (tglKeluar.equalsIgnoreCase("01/01/1900")) {
                    lStatus.setValue("ACTIVE");
                    lStatus.setStyle("color:#00FF00");
                } else {
                    lStatus.setValue("CLOSED");
                    lStatus.setStyle("color:#FF0000;");
                }
                lcStatus.appendChild(lStatus);

                Listitem li = new Listitem();
                li.setValue(o);

                li.appendChild(new Listcell(Libs.nn(o[0])));
                li.appendChild(new Listcell(Libs.nn(o[23]).substring(0, 10)));
                li.appendChild(new Listcell(Libs.nn(o[1]) + "-" + Libs.nn(o[2]) + "-" + Libs.nn(o[3]) + "-" + Libs.nn(o[4])));
                li.appendChild(new Listcell(Libs.nn(o[7])));
                li.appendChild(new Listcell(Libs.nn(o[5]) + "-" + Libs.nn(o[6])));
                li.appendChild(new Listcell(Libs.nn(o[8]).trim()));
                li.appendChild(lcStatus);
                li.appendChild(new Listcell(Libs.nn(o[10]).trim()));
                li.appendChild(new Listcell(Libs.nn(o[9]).trim()));
                lb.appendChild(li);
            }
        } catch (Exception ex) {
            log.error("populate", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    public void refresh() {
        where = null;
        populate(0, pg.getPageSize());
    }

    public void lbSelected() {}

    public void showInHospitalMonitoringDetail() {
        Window w = (Window) Executions.createComponents("views/InHospitalMonitoringDetail.zul", Libs.getRootWindow(), null);
        w.setAttribute("ihm", lb.getSelectedItem().getValue());
        w.doModal();
    }

    public void quickSearch() {
        String val = ((Textbox) getFellow("tQuickSearch")).getText();
        if (!val.isEmpty()) {
            where = "convert(varchar, a.nokartu) like '%" + val + "%' or "
                    + "a.nmpeserta like '%" + val + "%' or "
                    + "a.namars like '%" + val + "%' or "
                    + "(convert(varchar, a.idx) + '-' + a.seq)='" + val + "' ";

            populate(0, pg.getPageSize());
        } else refresh();
    }

    public void export() {
        Libs.showDeveloping();
    }

}
