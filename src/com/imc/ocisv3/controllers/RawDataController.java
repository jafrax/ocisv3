package com.imc.ocisv3.controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.imc.ocisv3.pojos.ClaimPOJO;
import com.imc.ocisv3.pojos.MemberPOJO;
import com.imc.ocisv3.pojos.PolicyPOJO;
import com.imc.ocisv3.tools.Libs;

/**
 * Created by Arifullah on 31/10/2014.
 */
public class RawDataController extends Window {

    private Logger log = LoggerFactory.getLogger(RawDataController.class);
    private Listbox lb;
    private Paging pg;
    private Combobox cbPolicy;
    private Datebox startDate;
    private Datebox endDate;
    private String where;
    private String queryString;
    private String userProductViewrestriction;
    
    String insid="";
    List products = null;
    
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
            
            populateCountForQuickSearch();
            populate(0, pg.getPageSize());
        }
    }

    private void initComponents() {
        lb = (Listbox) getFellow("lb");
        pg = (Paging) getFellow("pg");
        cbPolicy = (Combobox) getFellow("cbPolicy");
        startDate = (Datebox)getFellow("startDate");
        endDate = (Datebox)getFellow("endDate");
        
    	products = Libs.getProductByUserId(Libs.getUser());
    	for(int i=0; i < products.size(); i++){
    		insid=insid+"'"+(String)products.get(i)+"'"+",";
    	}
    	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);

        pg.addEventListener("onPaging", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                PagingEvent evt = (PagingEvent) event;
                populate(evt.getActivePage()*pg.getPageSize(), pg.getPageSize());
            }
        });
        
        cbPolicy.appendItem("All Products");
        cbPolicy.setSelectedIndex(0);
        boolean show = true;
        
        for (String s : Libs.getPolicyMap().keySet()) {
        	String policyName = Libs.getPolicyMap().get(s);
            if (Libs.config.get("demo_mode").equals("true") && Libs.getInsuranceId().equals("00051")) policyName = Libs.nn(Libs.config.get("demo_name"));

            if (!Libs.nn(userProductViewrestriction).isEmpty()) {
                if (!userProductViewrestriction.contains(s.split("\\-")[3])) show=false;
                else show=true;
            }

            if (show) cbPolicy.appendItem(policyName + " (" + s + ")");
        }

      
        endDate.setValue(new Date());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        startDate.setValue(cal.getTime());
        
     }
    
  
    
  

    private void populateCount() {
        Session s = Libs.sfDB.openSession();
        try {
        	
        	
            String countSelect = "select count(*) ";

            String qry = "from idnhltpf.dbo.hlthdr b  "
                    + "inner join idnhltpf2.dbo.tclaim_header a on b.hhdryy=a.ThnPolis and b.HHDRBR=a.BrPolis and b.HHDRDIST=a.DistPolis and b.hhdrpono=a.NoPolis "
                    + "where "
                    + "b.hhdrinsid";
    				if(products.size() > 0) qry = qry + " in  ("+insid+") ";
    				else qry = qry + "='" + Libs.getInsuranceId() + "' ";  
    				
    				
    				if(polisList.size() > 0){
            			qry = qry + "and convert(varchar,b.hhdryy)+'-'+convert(varchar,b.hhdrbr)+'-'+convert(varchar,b.hhdrdist)+'-'+convert(varchar,b.hhdrpono) "
            					  + "in ("+polis+") ";
            		} 
    				
    				if (cbPolicy.getSelectedIndex()>0) {
    	                String policy = cbPolicy.getSelectedItem().getLabel();
    	                policy = policy.substring(policy.indexOf("(")+1, policy.indexOf(")"));
    	                String policyNo[] = policy.split("-");
    	                qry += "and b.hhdryy='"+policyNo[0]+"' and b.hhdrbr='"+policyNo[1]+"' and b.hhdrdist='"+policyNo[2]+"' and b.hhdrpono='" + policyNo[3] + "' ";
    	            }
    				
    				if(startDate.getValue() != null && endDate.getValue() != null){
    					qry += "and cdate BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
    				}

                    qry = qry + "and a.recid<>'C' AND hdt1PONO<>99999 AND hdt1IDXNO < 99989 ";

            if (!Libs.nn(userProductViewrestriction).isEmpty()) qry += "and b.hhdrpono in (" + userProductViewrestriction + ") ";

            if (where!=null) qry += "and (" + where + ") ";


            Integer count = (Integer) s.createSQLQuery(countSelect + qry).uniqueResult();
            pg.setTotalSize(count);
        } catch (Exception ex) {
            log.error("populateCount", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    private void populateCountForQuickSearch() {
        lb.getItems().clear();
        Session s = Libs.sfDB.openSession();
        try {
        		
            String countSelect = "select count(*) ";

            
            String qry = "from idnhltpf.dbo.hlthdr b "
                    + "inner join idnhltpf2.dbo.tclaim_header a "
                    + "on b.hhdryy=a.ThnPolis and b.HHDRBR=a.BrPolis and b.HHDRDIST=a.DistPolis and b.hhdrpono=a.NoPolis "
                    + "inner join idnhltpf.dbo.hltpro d on d.hpronomor=a.procode "
                    + "inner join idnhltpf.dbo.hltdt1 c "
                    + "on a.ThnPolis=c.hdt1yy and a.BrPolis=c.HDT1BR and a.DistPolis=c.HDT1DIST and a.NoPolis=c.hdt1pono and a.Idx=c.hdt1idxno and a.Seq=c.hdt1seqno and c.hdt1ctr=0 "
                    + "inner join idnhltpf.dbo.hltdt2 f  "
                    + "on c.HDT1YY=f.hdt2yy and c.HDT1BR=f.HDT2BR and c.HDT1DIST=f.HDT2DIST and c.HDT1PONO=f.hdt2pono and c.HDT1IDXNO=f.hdt2idxno and c.HDT1SEQNO=f.hdt2seqno and c.HDT1CTR=f.hdt2ctr "
                    + "inner join idnhltpf.dbo.hltemp e "
                    + "on f.HDT2YY=e.hempyy and f.HDT2BR=e.HEMPBR and f.HDT2DIST=e.HEMPDIST and f.HDT2PONO=e.hemppono and f.HDT2IDXNO=e.hempidxno and f.HDT2SEQNO=e.hempseqno and f.HDT2CTR=e.hempctr "
                    + "left outer join idnhltpf.dbo.hltmemo2 g  "
                    + "on a.ThnPolis=g.hmem2yy and a.BrPolis=g.HMEM2BR and a.DistPolis=g.HMEM2DIST and a.NoPolis=g.hmem2pono and a.Idx=g.hmem2idxno and a.Seq=g.hmem2seqno and a.tclaim=g.hmem2claim and a.Counter=g.hmem2count "
                    + "INNER JOIN idnhltpf.dbo.hltovc on a.hid=hovccno "
                    + "where "
                    + "b.hhdrinsid";
            
    				if(products.size() > 0) qry = qry + " in  ("+insid+") ";
    				else qry = qry + "='" + Libs.getInsuranceId() + "' ";  
    				
    				if(polisList.size() > 0){
            			qry = qry + "and convert(varchar,b.hhdryy)+'-'+convert(varchar,b.hhdrbr)+'-'+convert(varchar,b.hhdrdist)+'-'+convert(varchar,b.hhdrpono) "
            					  + "in ("+polis+") ";
            		} 
    				
    				if (cbPolicy.getSelectedIndex()>0) {
    	                String policy = cbPolicy.getSelectedItem().getLabel();
    	                policy = policy.substring(policy.indexOf("(")+1, policy.indexOf(")"));
    	                String policyNo[] = policy.split("-");
    	                qry += "and b.hhdryy='"+policyNo[0]+"' and b.hhdrbr='"+policyNo[1]+"' and b.hhdrdist='"+policyNo[2]+"' and b.hhdrpono='" + policyNo[3] + "' ";
    	            }
    				
    				if(startDate.getValue() != null && endDate.getValue() != null){
    					qry += "and cdate BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
    				}

                    qry=qry + "and a.recid<>'C' AND hdt1PONO<>99999 AND hdt1IDXNO < 99989";

            if (!Libs.nn(userProductViewrestriction).isEmpty()) qry += "and b.hhdrpono in (" + userProductViewrestriction + ") ";

            if (where!=null) qry +=  where;


            Integer count = (Integer) s.createSQLQuery(countSelect + qry).uniqueResult();
            pg.setTotalSize(count);
        } catch (Exception ex) {
            log.error("populateCountForQuickSearch", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    private void populate(int offset, int limit) {
        lb.getItems().clear();
        Session s = Libs.sfDB.openSession();
        try {
        	
        	
        	
            String select = "select "
            		+ "convert(varchar,a.hclmyy)+convert(varchar,a.hclmbr)+convert(varchar,a.hclmdist)+convert(varchar,a.hclmpono)+convert(varchar,a.hclmidxno)+"
            		+ "a.hclmseqno+a.hclmtclaim+convert(varchar,a.hclmcount) as transId, e.hempcnpol, e.hempcnid, c.hdt1name, "
                    + "(select hdt1name from idnhltpf.dbo.hltdt1 where hdt1yy=a.hclmyy and hdt1br=a.hclmbr and hdt1dist=a.hclmdist and hdt1pono=a.hclmpono "
            		+ "and hdt1idxno=a.hclmidxno and hdt1seqno='A' and hdt1ctr=0) as employeeName, (a.HCLMPCODE1+a.HCLMPCODE2) as bplan, "
                    + "a.hclmtclaim, convert(varchar,a.hclmcdatey)+'-'+convert(varchar,a.hclmcdatem)+'-'+convert(varchar,a.hclmcdated) as claimDate, "
                    + "convert(varchar,a.hclmsinyy)+'-'+convert(varchar,a.hclmsinmm)+'-'+convert(varchar,a.hclmsindd) as sindate, "
                    + "convert(varchar,a.hclmsoutyy)+'-'+convert(varchar,a.hclmsoutmm)+'-'+convert(varchar,a.hclmsoutdd) as soutdate, "
                    + "convert(varchar,a.hclmrdatey)+'-'+convert(varchar,a.hclmrdatem)+'-'+convert(varchar,a.hclmrdated) as rdate, "
                    + "a.hclmcno,a.hclmnhoscd, d.hproname, a.hclmdiscd1, a.hclmdiscd2, a.hclmdiscd3, "
                    + "(" + Libs.getProposed() + ") as proposed, "
                    + "(" + Libs.getApproved() + ") as approved, "
                    + "convert(varchar,a.hclmpdatey)+'-'+convert(varchar,a.hclmpdatem)+'-'+convert(varchar,a.hclmpdated) as pdate, "
            		+ "(ltrim(rtrim(g.hmem2data1))+ltrim(rtrim(g.hmem2data2))+ltrim(rtrim(g.hmem2data3))+ltrim(rtrim(g.hmem2data4))) as memo, "
            		+ "v.hovcoutno, (v.hovctyp+' ' +convert(varchar,v.hovccqno)) as chequeNo, '1' as Flag ";

            String qry = "from idnhltpf.dbo.hlthdr b "
            		+ "inner join idnhltpf.dbo.hltclm a "
                    + "on b.hhdryy=a.hclmyy and b.HHDRBR=a.hclmbr and b.HHDRDIST=a.hclmdist and b.hhdrpono=a.hclmpono "
                    + "inner join idnhltpf.dbo.hltpro d on a.hclmnhoscd=d.hpronomor "
                    + "inner join idnhltpf.dbo.hltdt1 c "
                    + "on a.hclmyy=c.hdt1yy and a.hclmbr=c.HDT1BR and a.hclmdist=c.HDT1DIST and a.hclmpono=c.hdt1pono and a.hclmidxno=c.hdt1idxno and a.hclmseqno=c.hdt1seqno and c.hdt1ctr=0 "
                    + "inner join idnhltpf.dbo.hltdt2 f  "
                    + "on c.HDT1YY=f.hdt2yy and c.HDT1BR=f.HDT2BR and c.HDT1DIST=f.HDT2DIST and c.HDT1PONO=f.hdt2pono and c.HDT1IDXNO=f.hdt2idxno and c.HDT1SEQNO=f.hdt2seqno and c.HDT1CTR=f.hdt2ctr "
                    + "inner join idnhltpf.dbo.hltemp e "
                    + "on f.HDT2YY=e.hempyy and f.HDT2BR=e.HEMPBR and f.HDT2DIST=e.HEMPDIST and f.HDT2PONO=e.hemppono and f.HDT2IDXNO=e.hempidxno and f.HDT2SEQNO=e.hempseqno and f.HDT2CTR=e.hempctr "
                    + "left outer join idnhltpf.dbo.hltmemo2 g  "
                    + "on a.hclmyy=g.hmem2yy and a.hclmbr=g.HMEM2BR and a.hclmdist=g.HMEM2DIST and a.hclmpono=g.hmem2pono and a.hclmidxno=g.hmem2idxno and a.hclmseqno=g.hmem2seqno and a.hclmtclaim=g.hmem2claim and a.hclmcount=g.hmem2count "
                    + "INNER JOIN idnhltpf.dbo.hltovc v on a.hclmnomor=v.hovccno "
                    + "where "
                    + "b.hhdrinsid";
            
    				if(products.size() > 0) qry = qry + " in  ("+insid+") ";
    				else qry = qry + "='" + Libs.getInsuranceId() + "' ";  
    				
    				if(polisList.size() > 0){
            			qry = qry + "and convert(varchar,b.hhdryy)+'-'+convert(varchar,b.hhdrbr)+'-'+convert(varchar,b.hhdrdist)+'-'+convert(varchar,b.hhdrpono) "
            					  + "in ("+polis+") ";
            		} 
    				
    				if (cbPolicy.getSelectedIndex()>0) {
    	                String policy = cbPolicy.getSelectedItem().getLabel();
    	                policy = policy.substring(policy.indexOf("(")+1, policy.indexOf(")"));
    	                String policyNo[] = policy.split("-");
    	                qry += "and b.hhdryy='"+policyNo[0]+"' and b.hhdrbr='"+policyNo[1]+"' and b.hhdrdist='"+policyNo[2]+"' and b.hhdrpono='" + policyNo[3] + "' ";
    	            }
    				
    				if(startDate.getValue() != null && endDate.getValue() != null){    					
    					qry += "and convert(datetime, convert(varchar,a.HCLMPDATEM)+'-'+convert(varchar,a.HCLMPDATED)+'-'+convert(varchar,a.HCLMPDATEY), 110) BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
    				}
    				
    				

                    qry = qry + "and a.hclmrecid='P' AND hdt1PONO<>99999 AND hdt1IDXNO < 99989 ";

            if (!Libs.nn(userProductViewrestriction).isEmpty()) qry += "and b.hhdrpono in (" + userProductViewrestriction + ") ";

            if (where!=null) qry +=  where;

            
            String order = "order by convert(datetime, convert(varchar,a.HCLMCDATEM)+'-'+convert(varchar,a.HCLMCDATED)+'-'+convert(varchar,a.HCLMCDATEY), 110) desc ";
            
 
            List<Object[]> l = s.createSQLQuery(select + qry+order).setFirstResult(offset).setMaxResults(limit).list();
            for (Object[] o : l) {
            	
            	Listitem li = new Listitem();
            	li.appendChild(new Listcell(Libs.nn(o[0])));
            	li.appendChild(new Listcell(Libs.nn(o[1])));
            	li.appendChild(new Listcell(Libs.nn(o[2])));
            	li.appendChild(new Listcell(Libs.nn(o[3])));
            	li.appendChild(new Listcell(Libs.nn(o[4])));
            	li.appendChild(new Listcell(Libs.nn(o[5])));
            	li.appendChild(new Listcell(Libs.nn(o[6])));
            	li.appendChild(new Listcell(Libs.nn(o[7])));
            	if(Libs.nn(o[6]).equalsIgnoreCase("I") || Libs.nn(o[6]).equalsIgnoreCase("R")){
            		li.appendChild(new Listcell(Libs.nn(o[8])));
            		li.appendChild(new Listcell(Libs.nn(o[9])));
            	}else{
            		li.appendChild(new Listcell(Libs.nn(o[10])));
            		li.appendChild(new Listcell(Libs.nn(o[10])));
            	}
            	li.appendChild(new Listcell(Libs.nn(o[11])));
            	li.appendChild(new Listcell(Libs.nn(o[12])));
            	li.appendChild(new Listcell(Libs.nn(o[13])));
            	li.appendChild(new Listcell(Libs.nn(o[14])));
            	li.appendChild(new Listcell(Libs.nn(o[15])));
            	li.appendChild(new Listcell(Libs.nn(o[16])));
            	li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[17])), "#,###.##")) ;//proposed
            	li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[17]))-Double.valueOf(Libs.nn(o[18])), "#,###.##"));
            	li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[18])), "#,###.##"));//approved
            	li.appendChild(new Listcell(Libs.nn(o[19])));
            	li.appendChild(new Listcell(Libs.nn(o[20])));
            	li.appendChild(new Listcell(Libs.nn(o[21])));
            	li.appendChild(new Listcell(Libs.nn(o[22])));
            	li.appendChild(new Listcell(Libs.nn(o[23])));
            	li.appendChild(new Listcell(Libs.nn(o[0])));
            	
            	lb.appendChild(li);
            	
            }
        } catch (Exception ex) {
            log.error("populate", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }
    
    private String getEmployeeName(String policyNo, String index){
    	String hasil = null;
    	Session s = Libs.sfDB.openSession();
    	
    	String[] polis = policyNo.split("-");
    	try{
    		String query = "select HDT1NAME from idnhltpf.dbo.hltdt1 where HDT1YY='"+polis[0]+"' and HDT1BR='"+polis[1]+"' and HDT1DIST='"+polis[2]+"' and " 
    				     + "HDT1PONO='"+polis[3]+"' and HDT1IDXNO='"+index+"' and HDT1SEQNO='A' AND HDT1CTR = 0";
    		hasil = (String) s.createSQLQuery(query).uniqueResult();
    		
    	}catch(Exception e){
    		log.error("getEmployeeName", e);
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
    	return hasil;
    }

    private String[] getHospitalInvoice(String hid, Object object) {
		String[] hasil = null;
		int procode = ((BigDecimal)object).intValue();
		String query = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Session s = Libs.sfDB.openSession();
		try{
			
			if(procode != 0){
				if(hid.contains("OP")){
					query = "SELECT No_Surat, Tgl_Terima_Adm FROM ASO.dbo.pre_op_provider where No_HID='"+hid.trim()+"' AND Flg='1'";
					List<Object[]> l = s.createSQLQuery(query).list();
					if(l.size() > 0){
						Object[] o = l.get(0);
						hasil = new String[]{Libs.nn(o[0]), sdf.format((Date)o[1])};
					} 
				}
					
				else if(hid.contains("IP")){
					query = "SELECT NoSuratKwitansi, Tgl_Terima_Adm FROM ASO.dbo.pre_ip_provider where No_HID='"+hid.trim()+"' AND Flg='1'";
					
					List<Object[]> l = s.createSQLQuery(query).list();
					if(l.size() > 0){
						Object[] o = l.get(0);
						hasil = new String[]{Libs.nn(o[0]), sdf.format((Date)o[1])};
					}
				}
					
			}else{
				if(hid.contains("OP")){
					query = "SELECT NoRef, Tgl_Terima_Adm FROM ASO.dbo.Pre_OP_Reimburse where No_HID='"+hid.trim()+"' AND Flg='1'";
					List<Object[]> l = s.createSQLQuery(query).list();
					if(l.size() > 0){
						Object[] o = l.get(0);
						hasil = new String[]{Libs.nn(o[0]), sdf.format((Date)o[1])};
					}
				}
					
				else if(hid.contains("IP")){
					query = "SELECT NoRef, Tgl_Terima_Adm FROM ASO.dbo.Pre_IP_Reimburse where No_HID='"+hid.trim()+"' AND Flg='1'";
					List<Object[]> l = s.createSQLQuery(query).list();
					if(l.size() > 0){
						Object[] o = l.get(0);
						hasil = new String[]{Libs.nn(o[0]), sdf.format((Date)o[1])};
					}
					
				}
					
			}
			
//			System.out.println(claimType+" - "+ procode + " - "+ hid+ " *** "+ query +"\n");
			
			
		}catch(Exception e){
			System.out.println( procode + " - "+ hid+ " *** "+ query +"\n");
			log.error("getHospitalInvoice", e);
		}finally{
			 if (s!=null && s.isOpen()) s.close();
		}
		return hasil;
	}

	public void refresh() {
        where = null;
        populateCount();
        populate(0, pg.getPageSize());
    }
    
    public void viewClaimHistory(){
    	
    	
    	populateCountForQuickSearch();
    	populate(0, pg.getPageSize());
    }

    public void quickSearch() {
        String val = "";// ((Textbox) getFellow("tQuickSearch")).getText();
        if (!val.isEmpty()) {
            where = "(convert(varchar,c.hdt1ncard) like '%" + val + "%' or "
                    + "c.hdt1name like '%" + val + "%' or "
                    + "e.hempcnpol like '%" + val + "%' or "
                    + "e.hempcnid like '%" + val + "%' or "
                    + "e.hempmemo3 like '%" + val + "%' or "
                    + "a.hclmcno like '%" + val + "%') ";

            populateCountForQuickSearch();
            populate(0, pg.getPageSize());
        } else refresh();
    }

    public void lbSelected() {
        if (lb.getSelectedCount()>0) {
            ((Toolbarbutton) getFellow("tbnShowMemberDetail")).setDisabled(false);
        }
    }

    public void showClaimDetail(ClaimPOJO claimPojo) {
        Window w = (Window) Executions.createComponents("views/ClaimDetail.zul", this, null);
        w.setAttribute("claim", claimPojo);
        w.doModal();
    }

    public void policySelected() {
        quickSearch();
    }

    public void showMemberDetail(ClaimPOJO claimPOJO) {
        Window w = (Window) Executions.createComponents("views/MemberDetail.zul", Libs.getRootWindow(), null);
        w.setAttribute("policy", claimPOJO.getPolicy());
        w.setAttribute("member", claimPOJO.getMember());
        w.doModal();
    }
    
    
    	
    	
        
    
    
    
    public void exportDetail(){
    	Session s = Libs.sfDB.openSession();
    	StringBuffer sb = new StringBuffer();
      try{
    	String select = "select  " 
    			   + "convert(varchar,a.hclmyy)+convert(varchar,a.hclmbr)+convert(varchar,a.hclmdist)+convert(varchar,a.hclmpono)+convert(varchar,a.hclmidxno)+"
        		   + "a.hclmseqno+a.hclmtclaim+convert(varchar,a.hclmcount) as transId, "
				   + Libs.createListFieldString("a.hclmcamt") + ", "
				   + Libs.createListFieldString("a.hclmcday") + ", "
				   + Libs.createListFieldString("a.hclmaamt") + ", "
				   + Libs.createListFieldString("a.hclmaday") + ", "
    			   + "(a.hclmpcode1 + a.hclmpcode2) as plan_code, "
    		       + "convert(varchar,a.hclmyy)+'-'+convert(varchar,a.hclmbr)+'-'+convert(varchar, a.hclmdist) +'-'+convert(varchar,a.hclmpono) as polisNo ";
    	
    	  String qry = "from idnhltpf.dbo.hlthdr b "
          		  + "inner join idnhltpf.dbo.hltclm a "
                  + "on b.hhdryy=a.hclmyy and b.HHDRBR=a.hclmbr and b.HHDRDIST=a.hclmdist and b.hhdrpono=a.hclmpono "
                  + "inner join idnhltpf.dbo.hltpro d on a.hclmnhoscd=d.hpronomor "
                  + "inner join idnhltpf.dbo.hltdt1 c "
                  + "on a.hclmyy=c.hdt1yy and a.hclmbr=c.HDT1BR and a.hclmdist=c.HDT1DIST and a.hclmpono=c.hdt1pono and a.hclmidxno=c.hdt1idxno and a.hclmseqno=c.hdt1seqno and c.hdt1ctr=0 "
                  + "inner join idnhltpf.dbo.hltdt2 f  "
                  + "on c.HDT1YY=f.hdt2yy and c.HDT1BR=f.HDT2BR and c.HDT1DIST=f.HDT2DIST and c.HDT1PONO=f.hdt2pono and c.HDT1IDXNO=f.hdt2idxno and c.HDT1SEQNO=f.hdt2seqno and c.HDT1CTR=f.hdt2ctr "
                  + "inner join idnhltpf.dbo.hltemp e "
                  + "on f.HDT2YY=e.hempyy and f.HDT2BR=e.HEMPBR and f.HDT2DIST=e.HEMPDIST and f.HDT2PONO=e.hemppono and f.HDT2IDXNO=e.hempidxno and f.HDT2SEQNO=e.hempseqno and f.HDT2CTR=e.hempctr "
                  + "left outer join idnhltpf.dbo.hltmemo2 g  "
                  + "on a.hclmyy=g.hmem2yy and a.hclmbr=g.HMEM2BR and a.hclmdist=g.HMEM2DIST and a.hclmpono=g.hmem2pono and a.hclmidxno=g.hmem2idxno and a.hclmseqno=g.hmem2seqno and a.hclmtclaim=g.hmem2claim and a.hclmcount=g.hmem2count "
                  + "INNER JOIN idnhltpf.dbo.hltovc v on a.hclmnomor=v.hovccno "
                  + "where "
                  + "b.hhdrinsid";
          
  				if(products.size() > 0) qry = qry + " in  ("+insid+") ";
  				else qry = qry + "='" + Libs.getInsuranceId() + "' ";  
  				
  				if(polisList.size() > 0){
          			qry = qry + "and convert(varchar,b.hhdryy)+'-'+convert(varchar,b.hhdrbr)+'-'+convert(varchar,b.hhdrdist)+'-'+convert(varchar,b.hhdrpono) "
          					  + "in ("+polis+") ";
          		} 
  				
  				if (cbPolicy.getSelectedIndex()>0) {
  	                String policy = cbPolicy.getSelectedItem().getLabel();
  	                policy = policy.substring(policy.indexOf("(")+1, policy.indexOf(")"));
  	                String policyNo[] = policy.split("-");
  	                qry += "and b.hhdryy='"+policyNo[0]+"' and b.hhdrbr='"+policyNo[1]+"' and b.hhdrdist='"+policyNo[2]+"' and b.hhdrpono='" + policyNo[3] + "' ";
  	            }
  				
  				if(startDate.getValue() != null && endDate.getValue() != null){    					
  					qry += "and convert(datetime, convert(varchar,a.HCLMPDATEM)+'-'+convert(varchar,a.HCLMPDATED)+'-'+convert(varchar,a.HCLMPDATEY), 110) BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
  				}
  				
  				

                  qry = qry + "and a.hclmrecid='P' AND hdt1PONO<>99999 AND hdt1IDXNO < 99989 ";

          if (!Libs.nn(userProductViewrestriction).isEmpty()) qry += "and b.hhdrpono in (" + userProductViewrestriction + ") ";

          if (where!=null) qry +=  where;

          
          String order = "order by convert(datetime, convert(varchar,a.HCLMCDATEM)+'-'+convert(varchar,a.HCLMCDATED)+'-'+convert(varchar,a.HCLMCDATEY), 110) desc ";
          
          List<Object[]> l = s.createSQLQuery(select + qry+order).list();
          for (Object[] o : l){
        	  for(int i=1; i < 31; i++){
        		  if(Double.valueOf(Libs.nn(o[i]))>0){
        			  sb.append(Libs.nn(o[0])+",");
        			  String plan = Libs.nn(o[121]);
        			  String polis = Libs.nn(o[122]);
        			  sb.append(plan+",");
        			  Object[] obj = Libs.getBenefit(polis, plan, i);
        			  sb.append(obj[2].toString()+","); //description
        			  sb.append(Double.valueOf(Libs.nn(o[i])).intValue()+",");
        			  sb.append(Double.valueOf(Libs.nn(o[i+30])).intValue()+",");
        			  sb.append(Double.valueOf(Libs.nn(o[i+60])).intValue()+",");
        			  sb.append(Double.valueOf(Libs.nn(o[i+90])).intValue()+",");
        			  sb.append(""+",");
        			  sb.append("\r\n");
        		  } 
        	  }
        	  
          }
          
          String fn = "rawDataDetail-" + new SimpleDateFormat("yyyyMMdd").format(startDate.getValue()) + "_"+new SimpleDateFormat("yyyyMMdd").format(endDate.getValue())+".txt";
          FileWriter fw = new FileWriter(Libs.config.get("temp_dir").toString()+ File.separator + fn);
          BufferedWriter bw = new BufferedWriter(fw);
          bw.write(sb.toString());
          bw.close();

          Thread.sleep(5000);

          File f = new File(Libs.config.get("temp_dir").toString() + File.separator + fn);
          InputStream is = new FileInputStream(f);
          Filedownload.save(is, "application/txt", fn);
          f.delete();
      }catch(Exception e){
    	  log.error("exportDetail", e);
      }finally{
    	  if (s!=null && s.isOpen()) s.close(); 
      }
    	
    }
    
    public void exportWithDetail(){
    	
    	if (Messagebox.show("Retrieving this data would take up to 1 minutes, continue?", "Confirmation", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION)==Messagebox.OK){
    		Session s = Libs.sfDB.openSession();
        	String[] titles = new String[]{"Policy No", "Index", "Name", "Card Number", "Company Name", "Client Policy No", "Client Id", "Claim No", "Voucher No", 
        			"Claim Type", "Hospital Invoice No", "Provider Name", "Diagnosis", "Benefit", "Days", "Proposed", "Approved", "Excess", 
        			"Service In", "Service Out", "Claim Date", "Payment Date"};
        	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        	
        	StringBuffer sb = new StringBuffer();
            String judul = "Claim History Periode "+sdf.format(startDate.getValue()) + " To "+sdf.format(endDate.getValue());
             
            sb.append(judul);
         
             
            sb.append(" Product : " + cbPolicy.getSelectedItem().getLabel());
        	
        	Workbook wb = new HSSFWorkbook();
          	Map<String, CellStyle> styles = Libs.createStyles(wb);
          	
          	Sheet sheet = wb.createSheet("Claim Detail");
            PrintSetup printSetup = sheet.getPrintSetup();
            printSetup.setLandscape(true);
            sheet.setFitToPage(true);
            sheet.setHorizontallyCenter(true);
            
            int counter = 0;
            
            org.apache.poi.ss.usermodel.Cell mycell;
            org.apache.poi.ss.usermodel.Row row;
            
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(counter);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(counter+1)+":V$"+(counter+1)+""));
            titleCell.setCellValue(sb.toString());
            titleCell.setCellStyle(styles.get("header2"));
            
            counter = counter + 2;
            
            titleRow = sheet.createRow(counter);
            for(int i=0; i < titles.length; i++){
            	titleCell = titleRow.createCell(i);
            	titleCell.setCellValue(titles[i]);
            	titleCell.setCellStyle(styles.get("header2"));
            }
            
            counter = counter + 1;
          	 
        	try{
        		String qry = "select  " 
        				   + Libs.createListFieldString("a.hclmcamt") + ", "
        				   + Libs.createListFieldString("a.hclmcday") + ", "
    					   + Libs.createListFieldString("a.hclmaamt") + ", "
    					   + Libs.createListFieldString("a.hclmaday") + ", "
    					   + "convert(varchar,a.hclmyy)+'-'+convert(varchar,a.hclmbr)+'-'+convert(varchar, a.hclmdist) +'-'+convert(varchar,a.hclmpono) as polisNo, " //90
    					   + "b.hhdrname, a.hclmidxno, a.hclmseqno, c.hdt1name, a.hclmtclaim, d.hproname, a.hclmcount, e.hempcnpol, e.hempcnid, " //99
    					   + "(a.hclmpcode1 + a.hclmpcode2) as plan_code, c.hdt1ncard, c.hdt1bdtyy, c.hdt1bdtmm, c.hdt1bdtdd, c.hdt1sex, c.hdt1mstat, a.hclmcno, " //107
    					   + "g.hmem2data1, g.hmem2data2, g.hmem2data3, g.hmem2data4, " //111
    					   + "convert(varchar,a.hclmcdatey)+'-'+convert(varchar,a.hclmcdatem)+'-'+convert(varchar,a.hclmcdated) as cdate, "
    					   + "convert(varchar,a.hclmsinyy)+'-'+convert(varchar,a.hclmsinmm)+'-'+convert(varchar,a.hclmsindd) as sindate, "
    					   + "convert(varchar, a.hclmsoutyy)+'-'+convert(varchar,a.hclmsoutmm)+'-'+ convert(varchar,a.hclmsoutdd) as soutdate, "
    					   + "convert(varchar,a.hclmrdatey)+'-'+convert(varchar,a.hclmrdatem)+'-'+convert(varchar,a.hclmrdated) as rdate, "
    					   + "convert(varchar,a.hclmpdatey)+'-'+convert(varchar,a.hclmpdatem)+'-'+convert(varchar,a.hclmpdated) as pdate, "
    					   + "a.hclmrecid, hovcoutno, a.hclmnhoscd, " //119
    					   + "a.hclmdiscd1, a.hclmdiscd2, a.hclmdiscd3 "
    					   + "from idnhltpf.dbo.hlthdr b "
    	                   + "inner join idnhltpf.dbo.hltclm a "
    	                   + "on b.hhdryy=a.hclmyy and b.HHDRBR=a.hclmbr and b.HHDRDIST=a.hclmdist and b.hhdrpono=a.hclmpono "
    	                   + "inner join idnhltpf.dbo.hltpro d on d.hpronomor=a.hclmnhoscd "
    	                   + "inner join idnhltpf.dbo.hltdt1 c "
    	                   + "on a.hclmyy=c.hdt1yy and a.hclmbr=c.HDT1BR and a.hclmdist=c.HDT1DIST and b.hhdrpono=c.hdt1pono and a.hclmidxno=c.hdt1idxno and a.hclmseqno=c.hdt1seqno and c.hdt1ctr=0 "
    	                   + "inner join idnhltpf.dbo.hltdt2 f  "
    	                   + "on c.HDT1YY=f.hdt2yy and c.HDT1BR=f.HDT2BR and c.HDT1DIST=f.HDT2DIST and c.HDT1PONO=f.hdt2pono and c.HDT1IDXNO=f.hdt2idxno and c.HDT1SEQNO=f.hdt2seqno and c.HDT1CTR=f.hdt2ctr "
    	                   + "inner join idnhltpf.dbo.hltemp e "
    	                   + "on f.HDT2YY=e.hempyy and f.HDT2BR=e.HEMPBR and f.HDT2DIST=e.HEMPDIST and f.HDT2PONO=e.hemppono and f.HDT2IDXNO=e.hempidxno and f.HDT2SEQNO=e.hempseqno and f.HDT2CTR=e.hempctr "
    	                   + "left outer join idnhltpf.dbo.hltmemo2 g  "
    	                   + "on a.hclmyy=g.hmem2yy and a.hclmbr=g.HMEM2BR and a.hclmdist=g.HMEM2DIST and a.hclmpono=g.hmem2pono and a.hclmidxno=g.hmem2idxno and a.hclmseqno=g.hmem2seqno and a.hclmtclaim=g.hmem2claim and a.hclmcount=g.hmem2count "
    	                   + "INNER JOIN idnhltpf.dbo.hltovc on a.hclmnomor=hovccno "
    	                   + "where "
    	                   + "b.hhdrinsid";  
        		
        				if(products.size() > 0) qry = qry + " in  ("+insid+") ";
        				else qry = qry + "='" + Libs.getInsuranceId() + "' ";  
        				
        				if(polisList.size() > 0){
                			qry = qry + "and convert(varchar,b.hhdryy)+'-'+convert(varchar,b.hhdrbr)+'-'+convert(varchar,b.hhdrdist)+'-'+convert(varchar,b.hhdrpono) "
                					  + "in ("+polis+") ";
                		} 
    				
        				if (cbPolicy.getSelectedIndex()>0) {
        					String policy = cbPolicy.getSelectedItem().getLabel();
        					policy = policy.substring(policy.indexOf("(")+1, policy.indexOf(")"));
        					String policyNo[] = policy.split("-");
        					qry += "and b.hhdryy='"+policyNo[0]+"' and b.hhdrbr='"+policyNo[1]+"' and b.hhdrdist='"+policyNo[2]+"' and b.hhdrpono='" + policyNo[3] + "' ";
        				}
    			
        				if(startDate.getValue() != null && endDate.getValue() != null){
        					//qry += "and cdate BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
        					qry += "and convert(datetime, convert(varchar,a.HCLMCDATEM)+'-'+convert(varchar,a.HCLMCDATED)+'-'+convert(varchar,a.HCLMCDATEY), 110) BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
        				}

        				qry = qry + "and a.hclmrecid <>'C' AND hdt1PONO<>99999 AND hdt1IDXNO < 99989";
                
        				if (where!=null) qry +=  where;
                
        				qry = qry + " order by convert(datetime, convert(varchar,a.HCLMCDATEM)+'-'+convert(varchar,a.HCLMCDATED)+'-'+convert(varchar,a.HCLMCDATEY), 110) desc ";
        				String hid = null;
        				List<Object[]> l = s.createSQLQuery(qry).list();
        				for (Object[] o : l){
        					for(int i=0; i < 30; i++){
        						if(Double.valueOf(Libs.nn(o[i]))>0){
        							row = sheet.createRow(counter);
        							
        							mycell = row.createCell(0); //policy no
       							  	mycell.setCellValue(Libs.nn(o[90]));
       							  	mycell.setCellStyle(styles.get("cell"));
       							  	
       							  	mycell = row.createCell(1); //Index
    							  	mycell.setCellValue(Libs.nn(o[92])+ " "+Libs.nn(o[93]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(2); //member name
       							  	mycell.setCellValue(Libs.nn(o[94]));
       							  	mycell.setCellStyle(styles.get("cell"));
       							  	
       							  	mycell = row.createCell(3); //card Number
    							  	mycell.setCellValue(Libs.nn(o[101]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(4); //company name
       							  	mycell.setCellValue(Libs.nn(o[91]));
       							  	mycell.setCellStyle(styles.get("cell"));
       							  	
       							  	mycell = row.createCell(5); //client policy no
    							  	mycell.setCellValue(Libs.nn(o[98]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(6); //client id
       							  	mycell.setCellValue(Libs.nn(o[99]));
       							  	mycell.setCellStyle(styles.get("cell"));
       							  	
       							  	hid = Libs.nn(o[107]);
       							  	mycell = row.createCell(7); //claim no
    							  	mycell.setCellValue(hid);
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(8); //voucher no
    							  	mycell.setCellValue(Libs.nn(o[118]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(9); //claim type
    							  	mycell.setCellValue(Libs.getClaimType(Libs.nn(o[95])));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	String[] result = getHospitalInvoice(hid.substring(4, hid.length()), o[119]);
    							  	mycell = row.createCell(10); //hospital invoice no
    							  	if(result != null) mycell.setCellValue(result[0]); else mycell.setCellValue(""); 
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    								mycell = row.createCell(11); //provider name
    							  	mycell.setCellValue(Libs.nn(o[96]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	String icd = Libs.nn(o[120]).trim(); if(!Libs.nn(o[121]).trim().equalsIgnoreCase("")) icd = icd + ","+Libs.nn(o[121]).trim();if(!Libs.nn(o[122]).trim().equalsIgnoreCase("")) icd = icd + ","+Libs.nn(o[122]).trim();
    							  	
    							  	mycell = row.createCell(12); //diagnosis
    							  	mycell.setCellValue(icd);
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	String plan = Libs.nn(o[100]);
    							  	Object[] obj = Libs.getBenefit(Libs.nn(o[90]), plan, i+1);
    							  	
    							  	mycell = row.createCell(13); //Benefit
    							  	mycell.setCellValue(obj[2].toString());
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(14); //Days
    							  	mycell.setCellValue(Libs.nn(o[i+60]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(15); //proposed
    							  	mycell.setCellValue(Double.valueOf(Libs.nn(o[i])));
    							  	mycell.setCellStyle(styles.get("cell_angka"));
    							  	
    							  	mycell = row.createCell(16); //approved
    							  	mycell.setCellValue(Double.valueOf(Libs.nn(o[i+30])));
    							  	mycell.setCellStyle(styles.get("cell_angka"));
    							  	
    							  	mycell = row.createCell(17); //excess
    							  	mycell.setCellValue(Double.valueOf(Libs.nn(o[i])) - Double.valueOf(Libs.nn(o[i+30])));
    							  	mycell.setCellStyle(styles.get("cell_angka"));
    							  	
    							  	mycell = row.createCell(18); //service in
    							  	if(hid.contains("OP"))
    							  		mycell.setCellValue(Libs.nn(o[115]));
    							  	else mycell.setCellValue(Libs.nn(o[113]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(19); //service out
    							  	if(hid.contains("OP"))
    							  		mycell.setCellValue(Libs.nn(o[115]));
    							  	else mycell.setCellValue(Libs.nn(o[114]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(20); //claim date
    							  	mycell.setCellValue(Libs.nn(o[112]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	
    							  	mycell = row.createCell(21); //payment date
    							  	if(Libs.nn(o[116]).equalsIgnoreCase("0-0-0"))
    							  		mycell.setCellValue("-");
    							  	else mycell.setCellValue(Libs.nn(o[116]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	counter = counter + 1;
    							  	
        						}
        					}
        				}
        				
        				
        				for(int i=0; i < titles.length; i++){
                    		sheet.autoSizeColumn(i);
                    	}
        				
        				
        				String fn = "Claim_Detail-"+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";

        	            FileOutputStream out = new FileOutputStream(Libs.config.get("temp_dir").toString() + File.separator + fn);
        	            wb.write(out);
        	            out.close();

        	            Thread.sleep(5000);

        	            File f = new File(Libs.config.get("temp_dir").toString() + File.separator + fn);
        	            InputStream is = new FileInputStream(f);
        	            Filedownload.save(is, "application/vnd.ms-excel", fn);
        	            f.delete();

        				
        	}catch(Exception e){
        		log.error("exportWithDetail", e);
        	}finally{
        		if (s!=null && s.isOpen()) s.close();
        	}
    	}
    	
    	
    }

    public void export() {
    	StringBuffer sb = new StringBuffer();
        Session s = Libs.sfDB.openSession();
        try {
        	
        	
        	
            String select = "select "
            		+ "convert(varchar,a.hclmyy)+convert(varchar,a.hclmbr)+convert(varchar,a.hclmdist)+convert(varchar,a.hclmpono)+convert(varchar,a.hclmidxno)+"
            		+ "a.hclmseqno+a.hclmtclaim+convert(varchar,a.hclmcount) as transId, e.hempcnpol, e.hempcnid, c.hdt1name, "
                    + "(select hdt1name from idnhltpf.dbo.hltdt1 where hdt1yy=a.hclmyy and hdt1br=a.hclmbr and hdt1dist=a.hclmdist and hdt1pono=a.hclmpono "
            		+ "and hdt1idxno=a.hclmidxno and hdt1seqno='A' and hdt1ctr=0) as employeeName, (a.HCLMPCODE1+a.HCLMPCODE2) as bplan, "
                    + "a.hclmtclaim, convert(varchar,a.hclmcdatey)+'-'+convert(varchar,a.hclmcdatem)+'-'+convert(varchar,a.hclmcdated) as claimDate, "
                    + "convert(varchar,a.hclmsinyy)+'-'+convert(varchar,a.hclmsinmm)+'-'+convert(varchar,a.hclmsindd) as sindate, "
                    + "convert(varchar,a.hclmsoutyy)+'-'+convert(varchar,a.hclmsoutmm)+'-'+convert(varchar,a.hclmsoutdd) as soutdate, "
                    + "convert(varchar,a.hclmrdatey)+'-'+convert(varchar,a.hclmrdatem)+'-'+convert(varchar,a.hclmrdated) as rdate, "
                    + "a.hclmcno,a.hclmnhoscd, d.hproname, a.hclmdiscd1, a.hclmdiscd2, a.hclmdiscd3, "
                    + "(" + Libs.getProposed() + ") as proposed, "
                    + "(" + Libs.getApproved() + ") as approved, "
                    + "convert(varchar,a.hclmpdatey)+'-'+convert(varchar,a.hclmpdatem)+'-'+convert(varchar,a.hclmpdated) as pdate, "
            		+ "(ltrim(rtrim(g.hmem2data1))+ltrim(rtrim(g.hmem2data2))+ltrim(rtrim(g.hmem2data3))+ltrim(rtrim(g.hmem2data4))) as memo, "
            		+ "v.hovcoutno, (v.hovctyp+' ' +convert(varchar,v.hovccqno)) as chequeNo, '1' as Flag ";

            String qry = "from idnhltpf.dbo.hlthdr b "
            		+ "inner join idnhltpf.dbo.hltclm a "
                    + "on b.hhdryy=a.hclmyy and b.HHDRBR=a.hclmbr and b.HHDRDIST=a.hclmdist and b.hhdrpono=a.hclmpono "
                    + "inner join idnhltpf.dbo.hltpro d on a.hclmnhoscd=d.hpronomor "
                    + "inner join idnhltpf.dbo.hltdt1 c "
                    + "on a.hclmyy=c.hdt1yy and a.hclmbr=c.HDT1BR and a.hclmdist=c.HDT1DIST and a.hclmpono=c.hdt1pono and a.hclmidxno=c.hdt1idxno and a.hclmseqno=c.hdt1seqno and c.hdt1ctr=0 "
                    + "inner join idnhltpf.dbo.hltdt2 f  "
                    + "on c.HDT1YY=f.hdt2yy and c.HDT1BR=f.HDT2BR and c.HDT1DIST=f.HDT2DIST and c.HDT1PONO=f.hdt2pono and c.HDT1IDXNO=f.hdt2idxno and c.HDT1SEQNO=f.hdt2seqno and c.HDT1CTR=f.hdt2ctr "
                    + "inner join idnhltpf.dbo.hltemp e "
                    + "on f.HDT2YY=e.hempyy and f.HDT2BR=e.HEMPBR and f.HDT2DIST=e.HEMPDIST and f.HDT2PONO=e.hemppono and f.HDT2IDXNO=e.hempidxno and f.HDT2SEQNO=e.hempseqno and f.HDT2CTR=e.hempctr "
                    + "left outer join idnhltpf.dbo.hltmemo2 g  "
                    + "on a.hclmyy=g.hmem2yy and a.hclmbr=g.HMEM2BR and a.hclmdist=g.HMEM2DIST and a.hclmpono=g.hmem2pono and a.hclmidxno=g.hmem2idxno and a.hclmseqno=g.hmem2seqno and a.hclmtclaim=g.hmem2claim and a.hclmcount=g.hmem2count "
                    + "INNER JOIN idnhltpf.dbo.hltovc v on a.hclmnomor=v.hovccno "
                    + "where "
                    + "b.hhdrinsid";
            
    				if(products.size() > 0) qry = qry + " in  ("+insid+") ";
    				else qry = qry + "='" + Libs.getInsuranceId() + "' ";  
    				
    				if(polisList.size() > 0){
            			qry = qry + "and convert(varchar,b.hhdryy)+'-'+convert(varchar,b.hhdrbr)+'-'+convert(varchar,b.hhdrdist)+'-'+convert(varchar,b.hhdrpono) "
            					  + "in ("+polis+") ";
            		} 
    				
    				if (cbPolicy.getSelectedIndex()>0) {
    	                String policy = cbPolicy.getSelectedItem().getLabel();
    	                policy = policy.substring(policy.indexOf("(")+1, policy.indexOf(")"));
    	                String policyNo[] = policy.split("-");
    	                qry += "and b.hhdryy='"+policyNo[0]+"' and b.hhdrbr='"+policyNo[1]+"' and b.hhdrdist='"+policyNo[2]+"' and b.hhdrpono='" + policyNo[3] + "' ";
    	            }
    				
    				if(startDate.getValue() != null && endDate.getValue() != null){    					
    					qry += "and convert(datetime, convert(varchar,a.HCLMPDATEM)+'-'+convert(varchar,a.HCLMPDATED)+'-'+convert(varchar,a.HCLMPDATEY), 110) BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
    				}
    				
    				

                    qry = qry + "and a.hclmrecid='P' AND hdt1PONO<>99999 AND hdt1IDXNO < 99989 ";

            if (!Libs.nn(userProductViewrestriction).isEmpty()) qry += "and b.hhdrpono in (" + userProductViewrestriction + ") ";

            if (where!=null) qry +=  where;

            
            String order = "order by convert(datetime, convert(varchar,a.HCLMCDATEM)+'-'+convert(varchar,a.HCLMCDATED)+'-'+convert(varchar,a.HCLMCDATEY), 110) desc ";
            
            String memo = null;
            
            SimpleDateFormat origin = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat tpiFormat = new SimpleDateFormat("ddMMyyyy");
            
            List<Object[]> l = s.createSQLQuery(select + qry + order).list();
            for (Object[] o : l) {
            	
            	memo = "";
            	sb.append(Libs.nn(o[0]).trim()+",");
            	sb.append(Libs.nn(o[1]).trim()+",");
            	sb.append(Libs.nn(o[2]).trim()+",");
            	sb.append(Libs.nn(o[3]).trim()+",");
            	sb.append(Libs.nn(o[4]).trim()+",");
            	sb.append(Libs.nn(o[5]).trim()+",");
            	sb.append(Libs.nn(o[6]).trim().toLowerCase()+",");
            	
            	Date tgl = origin.parse(Libs.nn(o[7]).trim());
            	sb.append(tpiFormat.format(tgl)+",");
            	if(Libs.nn(o[6]).equalsIgnoreCase("I") || Libs.nn(o[6]).equalsIgnoreCase("R")){
            		tgl = origin.parse(Libs.nn(o[8]).trim());
            		sb.append(tpiFormat.format(tgl)+",");
            		
            		tgl = origin.parse(Libs.nn(o[9]).trim());
            		sb.append(tpiFormat.format(tgl)+",");
            	}else{
            		tgl = origin.parse(Libs.nn(o[10]).trim());
            		sb.append(tpiFormat.format(tgl)+",");
            		sb.append(tpiFormat.format(tgl)+",");
            	}
            	sb.append(Libs.nn(o[11]).trim()+",");
            	sb.append(Libs.nn(o[12]).trim()+",");
            	sb.append(Libs.nn(o[13]).trim()+",");
            	sb.append(Libs.nn(o[14]).trim()+",");
            	sb.append(Libs.nn(o[15]).trim()+",");
            	sb.append(Libs.nn(o[16]).trim()+",");
            	sb.append(Double.valueOf(Libs.nn(o[17])).intValue()+",") ;//proposed
            	sb.append((Double.valueOf(Libs.nn(o[17])).intValue()-Double.valueOf(Libs.nn(o[18])).intValue())+",");
            	sb.append(Double.valueOf(Libs.nn(o[18])).intValue()+",");//approved
            	
            	tgl = origin.parse(Libs.nn(o[19]).trim());
            	
            	sb.append(tpiFormat.format(tgl)+",");
            
            	memo = Libs.nn(o[20]).replace(",", " ").trim();
            	if(memo.length() > 200) memo = memo.substring(0, 199);
            	
            	sb.append(memo+",");
            	sb.append(Libs.nn(o[21]).trim()+",");
            	sb.append(Libs.nn(o[22]).trim()+",");
            	sb.append(Libs.nn(o[23]).trim()+",");
            	sb.append(Libs.nn(o[0]).trim());
            	sb.append("\r\n");
            	
            }
            
            String fn = "rawData-" + new SimpleDateFormat("yyyyMMdd").format(startDate.getValue()) + "_"+new SimpleDateFormat("yyyyMMdd").format(endDate.getValue())+".txt";
            FileWriter fw = new FileWriter(Libs.config.get("temp_dir").toString()+ File.separator + fn);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(sb.toString());
            bw.close();

            Thread.sleep(5000);

            File f = new File(Libs.config.get("temp_dir").toString() + File.separator + fn);
            InputStream is = new FileInputStream(f);
            Filedownload.save(is, "application/txt", fn);
            f.delete();
        } catch (Exception ex) {
            log.error("export", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    
    	
    }

    private void createReport(List<Object[]> list) {
       
    	String[] columnsMemberWise = new String[] {
                "POLICY NUMBER", "COMPANY NAME", "INDEX", "SEQ", "CARD NUMBER",
                "NAME", "COUNT", "TYPE", "EMPLOYEE NAME", "EMP INDEX","REGISTER DATE","CLAIM-DATE", "SIN-DATE", "SOUT-DATE", "RECEIPT-DATE", "PAYMENT-DATE", 
                "HID NUMBER", "VOUCHER NO", "HOSPITAL INVOICE NO", "PROVIDER NAME", "ICD1", "ICD2", "ICD3", 
                "PROPOSED", "APPROVED", "STATUS", "MEMO"};
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        try {
            Workbook wb = new HSSFWorkbook();
            Sheet sheet = wb.createSheet("Claim History");
            
            int cnt = 0;
            
          
            StringBuffer sb = new StringBuffer();
            String judul = "Claim History Periode "+sdf.format(startDate.getValue()) + " To "+sdf.format(endDate.getValue());
            
            sb.append(judul);
            
            sb.append(" Product : " + cbPolicy.getSelectedItem().getLabel());
            
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(cnt);
            org.apache.poi.ss.usermodel.Cell titleCell = row.createCell(0);
            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(cnt+1)+":AA$"+(cnt+1)+""));
            titleCell.setCellValue(sb.toString());

            
//            Libs.createCell(row, 0, sb.toString());
            
            cnt++;
            
            row = sheet.createRow(cnt);
            
            cnt++;
            row = sheet.createRow(cnt);
            
            cnt++;
            row = sheet.createRow(cnt);

            for (int i=0; i<columnsMemberWise.length; i++) {
                Libs.createCell(row, i, columnsMemberWise[i]);
            }

            cnt++;

            for (Object[] o : list) {
                row = sheet.createRow(cnt);

                Libs.createCell(row, 0, Libs.nn(o[1])+"-"+Libs.nn(o[2])+"-"+Libs.nn(o[3])+"-"+Libs.nn(o[4]));
                Libs.createCell(row, 1, Libs.nn(o[5]));
                Libs.createCell(row, 2, Libs.nn(o[6]));
                Libs.createCell(row, 3, Libs.nn(o[7]));
                Libs.createCell(row, 4, Libs.nn(o[17]));
                Libs.createCell(row, 5, Libs.nn(o[8]));
                Libs.createCell(row, 6, Libs.nn(o[13]));
                Libs.createCell(row, 7, Libs.nn(o[9]));
               
                String empName = getEmployeeName(Libs.nn(o[1])+"-"+Libs.nn(o[2])+"-"+Libs.nn(o[3])+"-"+Libs.nn(o[4]), Libs.nn(o[6]));
                if(empName != null){
                	Libs.createCell(row, 8, empName);
                	Libs.createCell(row, 9, Libs.nn(o[6])+" A");
                }else{
                	Libs.createCell(row, 8, "");
                	Libs.createCell(row, 9, "");
                }
                
                
                String receiptDate = o[60] + "-" + o[61] + "-" + o[62];if(receiptDate.equalsIgnoreCase("0-0-0"))receiptDate="-";
                String paymentDate = o[63] + "-" + o[64] + "-" + o[65];if(paymentDate.equalsIgnoreCase("0-0-0"))paymentDate="-";
                String serviceIn = o[54] + "-" + o[55] + "-" + o[56]; if(serviceIn.equalsIgnoreCase("0-0-0"))serviceIn="-";
                String serviceOut = o[57] + "-" + o[58] + "-" + o[59];if(serviceOut.equalsIgnoreCase("0-0-0"))serviceOut="-";
                String claimDate = o[51] + "-" + o[52] + "-" + o[53]; 
                
                String hidNumber =Libs.nn(o[0]);
                String[] result = getHospitalInvoice(hidNumber.substring(4, hidNumber.length()), o[71]);
                
                
                
                if(result != null)
                	Libs.createCell(row, 10, result[1]);
                else
                	Libs.createCell(row, 10, "");
                
                Libs.createCell(row, 11, claimDate);
                Libs.createCell(row, 12, serviceIn);
                Libs.createCell(row, 13, serviceOut);
                
                Libs.createCell(row, 14, receiptDate);  //Libs.createCell(row, 11, Libs.nn(o[51]));
                Libs.createCell(row, 15, paymentDate);  //Libs.createCell(row, 12, Libs.nn(o[52]));
                Libs.createCell(row, 16, Libs.nn(o[0]));  //Libs.createCell(row, 13, Libs.nn(o[53]));
                Libs.createCell(row, 17, Libs.nn(o[70])); //Libs.createCell(row, 14, Libs.nn(o[54]));
                
                
                
               
                if(result != null)
                	Libs.createCell(row, 18, result[0]);
                else Libs.createCell(row, 18, "");
                
                Libs.createCell(row, 19, Libs.nn(o[12]));
                Libs.createCell(row, 20, Libs.nn(o[66]));
                Libs.createCell(row, 21, Libs.nn(o[67]));
                Libs.createCell(row, 22, Libs.nn(o[68]));
                
                Double d =(Double)o[10];
                Cell cell = row.createCell(23); 
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(d.doubleValue());
                
                d = (Double)o[11];
                cell = row.createCell(24); 
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(d.doubleValue());
                
                
                Libs.createCell(row, 25, Libs.getStatus(Libs.nn(o[69])));
                Libs.createCell(row, 26, Libs.nn(o[47]).trim() + Libs.nn(o[48]).trim() + Libs.nn(o[49]).trim() + Libs.nn(o[50]).trim());
                
               
                cnt++;
            }
            
            for(int i=0; i < columnsMemberWise.length; i++){
        		sheet.autoSizeColumn(i);
        	}

            String fn = "ClaimHistory-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";

            FileOutputStream out = new FileOutputStream(Libs.config.get("temp_dir").toString() + File.separator + fn);
            wb.write(out);
            out.close();

            Thread.sleep(5000);

            File f = new File(Libs.config.get("temp_dir").toString() + File.separator + fn);
            InputStream is = new FileInputStream(f);
            Filedownload.save(is, "application/vnd.ms-excel", fn);
            f.delete();
        } catch (Exception ex) {
            log.error("createReport", ex);
        }
    }

}
