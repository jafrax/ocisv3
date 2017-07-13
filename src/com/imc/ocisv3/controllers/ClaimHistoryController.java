package com.imc.ocisv3.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
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
 * Created by faizal on 10/25/13.
 */
public class ClaimHistoryController extends Window {

    private Logger log = LoggerFactory.getLogger(ClaimHistoryController.class);
    private Listbox lb;
    private Paging pg;
    private Combobox cbPolicy;
    private Combobox cbFilter;
    private Combobox cbClaimType;
    private Textbox tQuickSearch;
    private Datebox startDate;
    private Datebox endDate;
    private Label lblDate;
//    private Checkbox claimVoucherCb;
    private String where;
    private String queryString;
    private String userProductViewrestriction;
    
    String insid="";
    List products = null;
    
    private String polis ="";
    private List polisList;
    
    private boolean filterByPaymentDate = false;

    public void onCreate() {
        if (!Libs.checkSession()) {
            userProductViewrestriction = Libs.restrictUserProductView.get(Libs.getUser());
            initComponents();
            
            polisList = Libs.getPolisByUserId(Libs.getUser());
            for(int i=0; i < polisList.size(); i++){
        		polis=polis+"'"+(String)polisList.get(i)+"'"+",";
        	}
            if(polis.length() > 1)polis = polis.substring(0, polis.length()-1);
            
//            populateCountForQuickSearch(); the real before modification
//            populateCount();
            
            
            
//            populate(0, pg.getPageSize());
            
            populateNewClaimHistory(0, pg.getPageSize());
        }
    }

    

	private void initComponents() {
        lb = (Listbox) getFellow("lb");
        pg = (Paging) getFellow("pg");
        cbPolicy = (Combobox) getFellow("cbPolicy");
        cbFilter = (Combobox) getFellow("cbFilter");
        cbClaimType = (Combobox)getFellow("cbClaimType");
        tQuickSearch = (Textbox)getFellow("tQuickSearch");
        startDate = (Datebox)getFellow("startDate");
        endDate = (Datebox)getFellow("endDate");
        lblDate = (Label)getFellow("lblDate");
        
//        claimVoucherCb = (Checkbox)getFellow("claimVoucherCb");
        
        
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
        
        tQuickSearch.addEventListener("onOK", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
            	viewClaimHistory();
            }
        });
//        claimVoucherCb.setChecked(true);
        cbPolicy.appendItem("All Products");
        cbPolicy.setSelectedIndex(0);
        cbFilter.setSelectedIndex(0);
        cbClaimType.setSelectedIndex(0);
        
        cbClaimType.setVisible(false);
        showPolicy();
        
//        for (String s : Libs.policyMap.keySet()) {
  /*      for (String s : Libs.getPolicyMap().keySet()) {
//            String policyName = Libs.policyMap.get(s);
        	String policyName = Libs.getPolicyMap().get(s);
            if (Libs.config.get("demo_mode").equals("true") && Libs.getInsuranceId().equals("00051")) policyName = Libs.nn(Libs.config.get("demo_name"));

            if (!Libs.nn(userProductViewrestriction).isEmpty()) {
                if (!userProductViewrestriction.contains(s.split("\\-")[3])) show=false;
                else show=true;
            }

            if (show) cbPolicy.appendItem(policyName + " (" + s + ")");
        }*/

//        Listheader lhEmployeeId = (Listheader) getFellow("lhEmployeeId");
        /*
        if (Libs.getInsuranceId().equals("00078") || Libs.getInsuranceId().equals("00088")) {
            lhEmployeeId.setVisible(true);
        } else {
            lhEmployeeId.setVisible(false);
        }*/
        
        endDate.setValue(new Date());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        startDate.setValue(cal.getTime());
        
      /*  claimVoucherCb.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				populate(0, pg.getPageSize());
			}
		}); */
    }
    
    private void showPolicy(){
    	boolean show = true;
    	for (String s : Libs.getOcisPolicyMap().keySet()) {
      	String policyName = Libs.getOcisPolicyMap().get(s);
          if (Libs.config.get("demo_mode").equals("true") && Libs.getInsuranceId().equals("00051")) policyName = Libs.nn(Libs.config.get("demo_name"));

          if (!Libs.nn(userProductViewrestriction).isEmpty()) {
              if (!userProductViewrestriction.contains(s.split("\\-")[3])) show=false;
              else show=true;
          }

          if (show) cbPolicy.appendItem(policyName + " (" + s + ")");
      }
    }
    
    private void populateNewClaimHistory(int offset, int limit) {
    	lb.getItems().clear();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String startDate = sdf.format(this.startDate.getValue());
		String endDate = sdf.format(this.endDate.getValue());
		
		String countqry = "Select count(1) ";
		String qry = "Select * ";
		
		String from = "from "+Libs.getDbName()+".dbo.F_OCISClaimHistory('"+Libs.getInsuranceId()+"', '"+startDate+"', '"+endDate+"') ";
		
		
		Session s = Libs.sfOCIS.openSession();
		try{
			 Integer count = (Integer) s.createSQLQuery(countqry + from).uniqueResult();
	         pg.setTotalSize(count);
	         
	         List<Object[]> l = s.createSQLQuery(qry + from).setFirstResult(offset).setMaxResults(limit).list();
	         for(Object[] o : l){
	        	 
	        	 Listitem item = new Listitem();
	        	 final String hid = Libs.nn(o[0]);
	        	 A hidNumber = new A(hid);
	             hidNumber.setStyle("color:#00bbee;text-decoration:none");
	             Listcell cell = new Listcell();
	             cell.appendChild(hidNumber);
	             item.appendChild(cell);
	        	 
	        	 cell = new Listcell(Libs.nn(o[1]));
	        	 item.appendChild(cell);
	        	 
	        	 cell = new Listcell(Libs.nn(o[2]));
	        	 item.appendChild(cell);
	        	 
	        	 cell = new Listcell(Libs.nn(o[3]));
	        	 item.appendChild(cell);
	        	 
	        	 cell = new Listcell(Libs.nn(o[4]));
	        	 item.appendChild(cell);
	        	 
	        	 cell = new Listcell(Libs.nn(o[5]));
	        	 item.appendChild(cell);
	        	 
	        	 cell = new Listcell(Libs.nn(o[6]));
	        	 item.appendChild(cell);
	        	 
	        	 
	        	 cell = new Listcell(Libs.nn(o[7]));
	        	 item.appendChild(cell);
	        	 
	        	 A memberName = new A(Libs.nn(o[8]));
	        	 memberName.setStyle("color:#00bbee;text-decoration:none");
	        	 cell = new Listcell();
	        	 cell.appendChild(memberName);
	        	 item.appendChild(cell);
	        	 
	        	 cell = new Listcell(Libs.nn(o[9]));
	        	 item.appendChild(cell);
	        	 
//	        	 cell = new Listcell(Libs.nn(o[10]));
	        	 item.appendChild(Libs.createNumericListcell(((BigDecimal)o[10]).doubleValue(), "#,###.##"));
	        	 
//	        	 cell = new Listcell(Libs.nn(o[11]));
	        	 item.appendChild(Libs.createNumericListcell(((BigDecimal)o[11]).doubleValue(), "#,###.##"));
	        	 
	        	 cell = new Listcell(Libs.nn(o[12])); //provider name
	        	 item.appendChild(cell);
	        	 
//	        	 cell = new Listcell(Libs.nn(o[13]));
	        	 cell = new Listcell(Libs.formatDate((Date)o[13]));
	        	 item.appendChild(cell);
	        	 
	        	 cell = new Listcell(Libs.formatDate((Date)o[14]));
	        	 item.appendChild(cell);
	        	 
	        	 
//	        	 cell = new Listcell(Libs.nn(o[17]));
	        	 cell = new Listcell(Libs.formatDate((Date)o[15]));
	        	 item.appendChild(cell);
	        	 
//	        	 cell = new Listcell(Libs.nn(o[18]));
	        	 cell = new Listcell(Libs.formatDate((Date)o[16]));
	        	 item.appendChild(cell);
	        	 
//	        	 cell = new Listcell(Libs.nn(o[19]));
	        	 cell = new Listcell(Libs.formatDate((Date)o[17]));
	        	 item.appendChild(cell);
	        	 
	        	 final BigInteger claimNo = (BigInteger)o[20];
//	        	 cell = new Listcell(Libs.nn(o[20]));
	        	 cell = new Listcell(Libs.formatDate((Date)o[18]));
	        	 item.appendChild(cell);
	        	 
	        	 final BigInteger memberNo = (BigInteger)o[22];
	        	 
	        	 
	        	 hidNumber.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						showClaimDetailNew(new Object[]{hid,claimNo});
					}
				});
	        	 
	        	memberName.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						showMemberDetailNew(memberNo);
					}
				});
	        	 
	        	
	        	 
	        	 lb.appendChild(item);
	        	 
	        	 
	         }
	         
	         
		}catch(Exception e){
			
		}finally{
			 if (s!=null && s.isOpen()) s.close();
		}
		
	}
    
  
    
    public void filterBy(){
    	
    	lblDate.setValue("Claim Date From : ");
    	filterByPaymentDate = false;
    	
    	if(cbFilter.getSelectedIndex() == 4){
    		cbClaimType.setVisible(true);
    		tQuickSearch.setVisible(false);
    		cbClaimType.setSelectedIndex(0);
    	}
    	else if(cbFilter.getSelectedIndex() == 6){
    		tQuickSearch.setVisible(false);
    		lblDate.setValue("Payment Date From : ");
    		filterByPaymentDate = true;
    	}
    	else{
    		cbClaimType.setVisible(false);
    		tQuickSearch.setValue(null);
    		tQuickSearch.setVisible(true);
    	}
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



            
//            System.out.println(countSelect + qry + "\n");

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

//            String qry = "from idnhltpf.dbo.hlthdr b "
//                    + "inner join idnhltpf2.dbo.tclaim_header a on b.hhdryy=a.ThnPolis and b.HHDRBR=a.BrPolis and b.HHDRDIST=a.DistPolis and b.hhdrpono=a.NoPolis "
//                    + "inner join idnhltpf.dbo.hltdt1 c  "
//                    + "on a.ThnPolis=c.hdt1yy and a.BrPolis=c.HDT1BR and a.DistPolis=c.HDT1DIST and a.NoPolis=c.hdt1pono and a.Idx=c.hdt1idxno and a.Seq=c.hdt1seqno and c.hdt1ctr=0  "
//                    + "inner join idnhltpf.dbo.hltemp e  " 
//                    + "on c.HDT1YY=e.hempyy and c.HDT1BR=e.HEMPBR and c.HDT1DIST=e.HEMPDIST and c.HDT1PONO=e.hemppono and c.HDT1IDXNO=e.hempidxno and c.HDT1SEQNO=e.hempseqno and c.HDT1CTR=e.hempctr "
//                    + "where "
//                    + "b.hhdrinsid";
            
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
    				
    				if(startDate.getValue() != null && endDate.getValue() != null && !filterByPaymentDate){
    					qry += "and cdate BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
    				}
    				
    				if(startDate.getValue() != null && endDate.getValue() != null && filterByPaymentDate){
    					qry += "and pdate BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
    				}

                    qry=qry + "and a.recid<>'C' AND hdt1PONO<>99999 AND hdt1IDXNO < 99989";

            if (!Libs.nn(userProductViewrestriction).isEmpty()) qry += "and b.hhdrpono in (" + userProductViewrestriction + ") ";

            if (where!=null) qry +=  where;

//            System.out.println(countSelect + qry);

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
        	
        	
//        	String countQry = "select count(*)  ";
        	
            String select = "select "
//                    + "a.HID2 , a.ThnPolis, a.BrPolis, a.DistPolis, a.NoPolis, "
            		+ "a.hclmcno, a.hclmyy, a.hclmbr, a.hclmdist, a.hclmpono, "
//                    + "b.hhdrname, a.Idx, a.seq, "
            		+ "b.hhdrname, a.hclmidxno, a.hclmseqno, "
                    + "c.hdt1name, a.hclmtclaim, "
//                    + "a.diAjukan as proposed, " //10
                    + "(" + Libs.getProposed() + ") as proposed, "
//                    + "a.diBayarkan as approved, "
                    + "(" + Libs.getApproved() + ") as approved, "
                    + "d.hproname, a.hclmcount, e.hempcnpol, e.hempcnid, '' as blank1, c.hdt1ncard, c.hdt1bdtyy, c.hdt1bdtmm, c.hdt1bdtdd, c.hdt1sex, " //21
                    + "f.hdt2plan1, f.hdt2plan2, f.hdt2plan3, f.hdt2plan4, f.hdt2plan5, f.hdt2plan6, "
                    + "f.hdt2sdtyy, f.hdt2sdtmm, f.hdt2sdtdd, "
                    + "f.hdt2mdtyy, f.hdt2mdtmm, f.hdt2mdtdd, "
                    + "(convert(varchar,f.hdt2pedty1)+'-'+convert(varchar,f.hdt2pedtm1)+'-'+convert(varchar,f.hdt2pedtd1)) as hdt2pedt1,"
                    + "(convert(varchar,f.hdt2pedty2)+'-'+convert(varchar,f.hdt2pedtm2)+'-'+convert(varchar,f.hdt2pedtd2)) as hdt2pedt2,"
                    + "(convert(varchar,f.hdt2pedty3)+'-'+convert(varchar,f.hdt2pedtm3)+'-'+convert(varchar,f.hdt2pedtd3)) as hdt2pedt3,"
                    + "(convert(varchar,f.hdt2pedty4)+'-'+convert(varchar,f.hdt2pedtm4)+'-'+convert(varchar,f.hdt2pedtd4)) as hdt2pedt4,"
                    + "(convert(varchar,f.hdt2pedty5)+'-'+convert(varchar,f.hdt2pedtm5)+'-'+convert(varchar,f.hdt2pedtd5)) as hdt2pedt5,"
                    + "(convert(varchar,f.hdt2pedty6)+'-'+convert(varchar,f.hdt2pedtm6)+'-'+convert(varchar,f.hdt2pedtd6)) as hdt2pedt6,"
                    + "(convert(varchar,f.hdt2pxdty1)+'-'+convert(varchar,f.hdt2pxdtm1)+'-'+convert(varchar,f.hdt2pxdtd1)) as hdt2pxdt1,"
                    + "(convert(varchar,f.hdt2pxdty2)+'-'+convert(varchar,f.hdt2pxdtm2)+'-'+convert(varchar,f.hdt2pxdtd2)) as hdt2pxdt2,"
                    + "(convert(varchar,f.hdt2pxdty3)+'-'+convert(varchar,f.hdt2pxdtm3)+'-'+convert(varchar,f.hdt2pxdtd3)) as hdt2pxdt3,"
                    + "(convert(varchar,f.hdt2pxdty4)+'-'+convert(varchar,f.hdt2pxdtm4)+'-'+convert(varchar,f.hdt2pxdtd4)) as hdt2pxdt4,"
                    + "(convert(varchar,f.hdt2pxdty5)+'-'+convert(varchar,f.hdt2pxdtm5)+'-'+convert(varchar,f.hdt2pxdtd5)) as hdt2pxdt5,"
                    + "(convert(varchar,f.hdt2pxdty6)+'-'+convert(varchar,f.hdt2pxdtm6)+'-'+convert(varchar,f.hdt2pxdtd6)) as hdt2pxdt6, "
                    + "c.hdt1mstat, " // 46
                    + "g.hmem2data1, g.hmem2data2, g.hmem2data3, g.hmem2data4, "
                    + "a.hclmcdatey, a.hclmcdatem, a.hclmcdated, " //"a.cdate, " //"a.hclmcdatey, a.hclmcdatem, a.hclmcdated, "
                    + "a.hclmsinyy, a.hclmsinmm, a.hclmsindd, " //"a.sindate, " //"a.hclmsinyy, a.hclmsinmm, a.hclmsindd, "
                    + "a.hclmsoutyy, a.hclmsoutmm, a.hclmsoutdd, " //"a.soutdate, " //"a.hclmsoutyy, a.hclmsoutmm, a.hclmsoutdd, "
                    + "a.hclmrdatey, a.hclmrdatem, a.hclmrdated, " //"a.rdate, " //"a.hclmrdatey, a.hclmrdatem, a.hclmrdated, "
                    + "a.hclmpdatey, a.hclmpdatem, a.hclmpdated, " //"a.pdate, " //"a.hclmpdatey, a.hclmpdatem, a.hclmpdated, "
                    + "a.hclmdiscd1, a.hclmdiscd2, a.hclmdiscd3, " //"a.icd1, a.icd2, a.icd3, "
                    + "a.hclmrecid, " // 69 "a.recid, " // 59
                    + "e.hempmemo3, " //70 // 60
            		+ "v.hovcoutno, "
            		+ "a.hclmnhoscd "; //a.procode ";

            String qry = "from idnhltpf.dbo.hlthdr b "
//                    + "inner join idnhltpf2.dbo.tclaim_header a "
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
//                    + "LEFT JOIN IDNHLTPF.dbo.v_kwitansi k ON a.hid2=k.hid "
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
    				
    				if(startDate.getValue() != null && endDate.getValue() != null && !filterByPaymentDate){
    					//qry += "and cdate BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
    					
    					qry += "and convert(datetime, convert(varchar,a.HCLMCDATEM)+'-'+convert(varchar,a.HCLMCDATED)+'-'+convert(varchar,a.HCLMCDATEY), 110) BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
    				}
    				
    				if(startDate.getValue() != null && endDate.getValue() != null && filterByPaymentDate){
    					qry += "and a.HCLMPDATEM > 0  and a.HCLMPDATED > 0 and a.HCLMPDATEY > 1900 ";
    					
    					qry += "and convert(datetime, convert(varchar,a.HCLMPDATEM)+'-'+convert(varchar,a.HCLMPDATED)+'-'+convert(varchar,a.HCLMPDATEY), 110) BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
    				}
    				
    				
    				
    				

                    qry = qry + "and a.hclmrecid <>'C' AND hdt1PONO<>99999 AND hdt1IDXNO < 99989 ";

            if (!Libs.nn(userProductViewrestriction).isEmpty()) qry += "and b.hhdrpono in (" + userProductViewrestriction + ") ";

            if (where!=null) qry +=  where;

            

            //convert(date,convert(varchar,a.hclmcdated)+'-'+convert(varchar,a.hclmcdatem)+'-'+convert(varchar,a.hclmcdatey),105) desc ";
                  
            //String order = "order by cdate desc ";
            
            String order = "order by convert(datetime, convert(varchar,a.HCLMCDATEM)+'-'+convert(varchar,a.HCLMCDATED)+'-'+convert(varchar,a.HCLMCDATEY), 110) desc ";
            
            

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            

            List<Object[]> l = s.createSQLQuery(select + qry + order).setFirstResult(offset).setMaxResults(limit).list();
//            List<Object[]> l = s.createSQLQuery(queryString).setFirstResult(offset).setMaxResults(limit).list();
            for (Object[] o : l) {
                String policyName = Libs.nn(o[5]);
                if (Libs.config.get("demo_mode").equals("true") && Libs.getInsuranceId().equals("00051")) policyName = Libs.nn(Libs.config.get("demo_name"));

                String remarks = Libs.nn(o[47]).trim();
                String provider = Libs.nn(o[12]).trim();
                if (remarks.indexOf("[")>-1 && remarks.indexOf("]")>-1) {
                    provider = remarks.substring(remarks.indexOf("[")+1, remarks.indexOf("]"));
                }
                
                String claimDate = o[51] + "-" + o[52] + "-" + o[53];

                String receiptDate = o[60] + "-" + o[61] + "-" + o[62]; //sdf.format((Date)o[54]); //Libs.nn(o[54]); //sdf.format((Date)o[55]); //o[60] + "-" + o[61] + "-" + o[62];
                String paymentDate = o[63] + "-" + o[64] + "-" + o[65]; //sdf.format((Date)o[55]); //Libs.nn(o[55]); //sdf.format((Date)o[56]); //o[63] + "-" + o[64] + "-" + o[65];
                String serviceIn = o[54] + "-" + o[55] + "-" + o[56]; //sdf.format((Date)o[52]); //Libs.nn(o[52]);//sdf.format((Date)o[53]); //o[54] + "-" + o[55] + "-" + o[56];
                String serviceOut = o[57] + "-" + o[58] + "-" + o[59]; //sdf.format((Date)o[53]); //Libs.nn(o[53]); //sdf.format((Date)o[54]); //o[57] + "-" + o[58] + "-" + o[59];

                Listitem li = new Listitem();

                Listcell cell = new Listcell();
                A hidNumber = new A(Libs.nn(o[0]));
                hidNumber.setStyle("color:#00bbee;text-decoration:none");
                cell.appendChild(hidNumber);
                li.appendChild(cell);
                
                //new field (voucher no)
//                li.appendChild(new Listcell(Libs.nn(o[61]).trim()));
                li.appendChild(new Listcell(Libs.nn(o[71]).trim()));
                
                //new field (hospital invoice no)
                //li.appendChild(new Listcell(getHospitalInvoice(hidNumber.getLabel().substring(4, hidNumber.getLabel().length()), o[62])));
                String[] result = getHospitalInvoice(hidNumber.getLabel().substring(4, hidNumber.getLabel().length()), o[72]);
                if(result != null) li.appendChild(new Listcell(result[0]));
                else li.appendChild(new Listcell(""));
                
//                li.appendChild(new Listcell(Libs.nn(o[0])));
                li.appendChild(new Listcell(Libs.nn(o[14]).trim()));
                li.appendChild(new Listcell(policyName));
                li.appendChild(new Listcell(o[6] + "-" + o[7]));
                li.appendChild(new Listcell(Libs.nn(o[15]).trim()));
                if (Libs.getInsuranceId().equals("00078") || Libs.getInsuranceId().equals("00088")) {
                    li.appendChild(new Listcell(Libs.nn(o[60]).trim()));
                } else {
                    li.appendChild(new Listcell(""));
                }
                
                cell = new Listcell();
                A memberName = new A(Libs.nn(o[8]));
                memberName.setStyle("color:#00bbee;text-decoration:none");
                cell.appendChild(memberName);
                li.appendChild(cell);
//                li.appendChild(new Listcell(Libs.nn(o[8])));
                
                /*
                 *  Author : Heri Siswanto BN
                 *  Date : 14 August 2014
                 *  Update : Change Wrap on field Status
                 */
                cell = new Listcell();
//                Label lblStatus = new Label(Libs.getStatus(Libs.nn(o[59])));
                Label lblStatus = new Label(Libs.getStatus(Libs.nn(o[69])));
                lblStatus.setPre(true);
                lblStatus.setMultiline(true);
                lblStatus.setParent(cell);
                
                li.appendChild(cell);
                li.appendChild(new Listcell(Libs.getClaimType(Libs.nn(o[9]))));
                li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[10])), "#,###.##"));
                li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[11])), "#,###.##"));
                li.appendChild(new Listcell(provider));

                if (Libs.nn(o[9]).equals("I") || Libs.nn(o[9]).equals("R")) {
                    li.appendChild(new Listcell(""));
                    li.appendChild(new Listcell(serviceIn));
                    li.appendChild(new Listcell(serviceOut));
                } else {
                  li.appendChild(new Listcell(receiptDate));
                    li.appendChild(new Listcell(""));
                    li.appendChild(new Listcell(""));
                }
                
                /*if(paymentDate.equalsIgnoreCase("01-01-1900")) li.appendChild(new Listcell("-"));
                else li.appendChild(new Listcell(paymentDate)); */
                
                if (!Libs.nn(o[63]).equals("0")) {
                    li.appendChild(new Listcell(Libs.fixDate(paymentDate)));
                }else li.appendChild(new Listcell("-"));
                
                if(result != null) li.appendChild(new Listcell(result[1]));
                else li.appendChild(new Listcell(""));
                
                if(!Libs.nn(o[53]).equals("0"))
                	li.appendChild(new Listcell(Libs.fixDate(claimDate)));
                
                
                
                	
                lb.appendChild(li);

                PolicyPOJO policyPOJO = new PolicyPOJO();
                policyPOJO.setYear(Integer.valueOf(Libs.nn(o[1])));
                policyPOJO.setBr(Integer.valueOf(Libs.nn(o[2])));
                policyPOJO.setDist(Integer.valueOf(Libs.nn(o[3])));
                policyPOJO.setPolicy_number(Integer.valueOf(Libs.nn(o[4])));
//                policyPOJO.setName(Libs.nn(o[16]).trim());
                policyPOJO.setName(Libs.nn(o[5]));

                MemberPOJO memberPOJO = new MemberPOJO();
                memberPOJO.setPolicy(policyPOJO);
                memberPOJO.setEmployee_id(Libs.nn(o[60]).trim());
                memberPOJO.setName(Libs.nn(o[8]).trim());
                memberPOJO.setCard_number(Libs.nn(o[17]).trim());
                memberPOJO.setDob(Libs.nn(o[18]) + "-" + Libs.nn(o[19]) + "-" + Libs.nn(o[20]));
                memberPOJO.setStarting_date(Libs.nn(o[28]) + "-" + Libs.nn(o[29]) + "-" + Libs.nn(o[30]));
                memberPOJO.setMature_date(Libs.nn(o[31]) + "-" + Libs.nn(o[32]) + "-" + Libs.nn(o[33]));
                memberPOJO.setSex(Libs.nn(o[21]));
                memberPOJO.setIp(Libs.nn(o[22]).trim());
                memberPOJO.setOp(Libs.nn(o[23]).trim());
                memberPOJO.setMaternity(Libs.nn(o[24]).trim());
                memberPOJO.setDental(Libs.nn(o[25]).trim());
                memberPOJO.setGlasses(Libs.nn(o[26]).trim());
                memberPOJO.setMarital_status(Libs.nn(o[46]));
                memberPOJO.setIdx(Libs.nn(o[6]));
                memberPOJO.setSeq(Libs.nn(o[7]));
                memberPOJO.setClient_policy_number(Libs.nn(o[14]).trim());
                memberPOJO.setClient_id_number(Libs.nn(o[15]).trim());
                memberPOJO.getPlan_entry_date().add(Libs.nn(o[34]));
                memberPOJO.getPlan_entry_date().add(Libs.nn(o[35]));
                memberPOJO.getPlan_entry_date().add(Libs.nn(o[36]));
                memberPOJO.getPlan_entry_date().add(Libs.nn(o[37]));
                memberPOJO.getPlan_entry_date().add(Libs.nn(o[38]));
                memberPOJO.getPlan_entry_date().add(Libs.nn(o[39]));
                memberPOJO.getPlan_exit_date().add(Libs.nn(o[40]));
                memberPOJO.getPlan_exit_date().add(Libs.nn(o[41]));
                memberPOJO.getPlan_exit_date().add(Libs.nn(o[42]));
                memberPOJO.getPlan_exit_date().add(Libs.nn(o[43]));
                memberPOJO.getPlan_exit_date().add(Libs.nn(o[44]));
                memberPOJO.getPlan_exit_date().add(Libs.nn(o[45]));

               final  ClaimPOJO claimPOJO = new ClaimPOJO();
                claimPOJO.setPolicy(policyPOJO);
                claimPOJO.setMember(memberPOJO);
                claimPOJO.setClaim_number(Libs.nn(o[0]).trim());
                claimPOJO.setPolicy_number(Libs.nn(o[1]) + '-' + Libs.nn(o[2]) + '-' + Libs.nn(o[3]) + '-' + Libs.nn(o[4]));
                claimPOJO.setIndex(Libs.nn(o[6]) + '-' + Libs.nn(o[7]).trim());
                claimPOJO.setClaim_count(Integer.valueOf(Libs.nn(o[13])));

                li.setValue(claimPOJO);
                
                memberName.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						showMemberDetail(claimPOJO);
					}
				});
                
                hidNumber.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						showClaimDetail(claimPOJO);
					}
				});

//                ((Toolbarbutton) getFellow("tbnShowMemberDetail")).setDisabled(true);
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
//		String claimType = hid.substring(hid.length()-3, hid.length()).trim();
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
    	if(cbFilter.getSelectedIndex() == 0){
    		//filter by Name
    		where = " AND hdt1name like '%" + tQuickSearch.getText() + "%' ";
    	}else if(cbFilter.getSelectedIndex() == 1){
    		//filter by claim no (HID)
    		where = " and hclmcno like '%" + tQuickSearch.getText() + "%' ";
    	}else if(cbFilter.getSelectedIndex() == 2){
    		//filter by client policy number
    		where = " and hempcnpol like '%" + tQuickSearch.getText() + "%' "; 
    	}else if(cbFilter.getSelectedIndex() == 3){
    		//filter by product (policy name)
    		where = " and hhdrname like '%" + tQuickSearch.getText() + "%' ";
    	}else if(cbFilter.getSelectedIndex() == 4){
    		//filter by claim type
    		if(cbClaimType.getSelectedIndex() == 0)
    			where = " and tclaim='I'";
    		else where = " and tclaim<>'I'";
    	}
    	
    	else if(cbFilter.getSelectedIndex() == 5){
    		//filter by product (policy name)
    		where = " and (CONVERT(varchar,hdt1idxno)+HDT1SEQNO) like '" + tQuickSearch.getText() + "%' ";
    	}
    	
    	/*
    	if(claimVoucherCb.isChecked()){
    		where = " and hovcoutno is not null ";
    	}
    	else {
    		where = " and hovcoutno is null ";
    	}*/
    	
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
//            populateCount();
            populate(0, pg.getPageSize());
        } else refresh();
    }

    public void lbSelected() {
        if (lb.getSelectedCount()>0) {
            ((Toolbarbutton) getFellow("tbnShowMemberDetail")).setDisabled(false);
        }
    }
    
    public void showClaimDetailNew(Object[] objects){
    	Window w = (Window) Executions.createComponents("views/ClaimDetail.zul", this, null);
        w.setAttribute("claim", objects);
        w.doModal();
    }

    public void showClaimDetail(ClaimPOJO claimPojo) {
        Window w = (Window) Executions.createComponents("views/ClaimDetail.zul", this, null);
        w.setAttribute("claim", claimPojo);
        w.doModal();
    }

    public void policySelected() {
        quickSearch();
    }
    
    public void showMemberDetailNew(BigInteger memberNo){
    	Window w = (Window) Executions.createComponents("views/MemberDetail.zul", Libs.getRootWindow(), null);
    	w.setAttribute("memberId", memberNo);
    	w.doModal();
    }

    public void showMemberDetail(ClaimPOJO claimPOJO) {
    	/*if(lb.getSelectedItem() == null){
    		Messagebox.show("Select Claim History Data First!", "Information", Messagebox.OK, Messagebox.INFORMATION);
    		return;
    	}*/
//        ClaimPOJO claimPOJO = lb.getSelectedItem().getValue();
        Window w = (Window) Executions.createComponents("views/MemberDetail.zul", Libs.getRootWindow(), null);
        w.setAttribute("policy", claimPOJO.getPolicy());
        w.setAttribute("member", claimPOJO.getMember());
        w.doModal();
    }
    
    public void exportToXls(){
    	Session s = Libs.sfDB.openSession();
    	try{
    		
        	
        	 String qry = "select "
                     + "a.hclmcno, a.hclmyy, a.hclmbr, a.hclmdist, a.hclmpono, b.hhdrname, a.hclmidxno, a.hclmseqno, c.hdt1name, a.hclmtclaim, "
                     + "(" + Libs.getProposed() + ") as proposed, "
                     + "(" + Libs.getApproved() + ") as approved, "
                     + "d.hproname, a.hclmcount, e.hempcnpol, e.hempcnid, '' as blank1, c.hdt1ncard, c.hdt1bdtyy, c.hdt1bdtmm, c.hdt1bdtdd, c.hdt1sex,  " //21
                     + "f.hdt2plan1, f.hdt2plan2, f.hdt2plan3, f.hdt2plan4, f.hdt2plan5, f.hdt2plan6, "
                     + "f.hdt2sdtyy, f.hdt2sdtmm, f.hdt2sdtdd, "
                     + "f.hdt2mdtyy, f.hdt2mdtmm, f.hdt2mdtdd, "
                     + "(convert(varchar,f.hdt2pedty1)+'-'+convert(varchar,f.hdt2pedtm1)+'-'+convert(varchar,f.hdt2pedtd1)) as hdt2pedt1,"
                     + "(convert(varchar,f.hdt2pedty2)+'-'+convert(varchar,f.hdt2pedtm2)+'-'+convert(varchar,f.hdt2pedtd2)) as hdt2pedt2,"
                     + "(convert(varchar,f.hdt2pedty3)+'-'+convert(varchar,f.hdt2pedtm3)+'-'+convert(varchar,f.hdt2pedtd3)) as hdt2pedt3,"
                     + "(convert(varchar,f.hdt2pedty4)+'-'+convert(varchar,f.hdt2pedtm4)+'-'+convert(varchar,f.hdt2pedtd4)) as hdt2pedt4,"
                     + "(convert(varchar,f.hdt2pedty5)+'-'+convert(varchar,f.hdt2pedtm5)+'-'+convert(varchar,f.hdt2pedtd5)) as hdt2pedt5,"
                     + "(convert(varchar,f.hdt2pedty6)+'-'+convert(varchar,f.hdt2pedtm6)+'-'+convert(varchar,f.hdt2pedtd6)) as hdt2pedt6,"
                     + "(convert(varchar,f.hdt2pxdty1)+'-'+convert(varchar,f.hdt2pxdtm1)+'-'+convert(varchar,f.hdt2pxdtd1)) as hdt2pxdt1,"
                     + "(convert(varchar,f.hdt2pxdty2)+'-'+convert(varchar,f.hdt2pxdtm2)+'-'+convert(varchar,f.hdt2pxdtd2)) as hdt2pxdt2,"
                     + "(convert(varchar,f.hdt2pxdty3)+'-'+convert(varchar,f.hdt2pxdtm3)+'-'+convert(varchar,f.hdt2pxdtd3)) as hdt2pxdt3,"
                     + "(convert(varchar,f.hdt2pxdty4)+'-'+convert(varchar,f.hdt2pxdtm4)+'-'+convert(varchar,f.hdt2pxdtd4)) as hdt2pxdt4,"
                     + "(convert(varchar,f.hdt2pxdty5)+'-'+convert(varchar,f.hdt2pxdtm5)+'-'+convert(varchar,f.hdt2pxdtd5)) as hdt2pxdt5,"
                     + "(convert(varchar,f.hdt2pxdty6)+'-'+convert(varchar,f.hdt2pxdtm6)+'-'+convert(varchar,f.hdt2pxdtd6)) as hdt2pxdt6, "
                     + "c.hdt1mstat, "
                     + "g.hmem2data1, g.hmem2data2, g.hmem2data3, g.hmem2data4, "
                     + "a.hclmcdatey, a.hclmcdatem, a.hclmcdated, " //"a.cdate,"  //"a.hclmcdatey, a.hclmcdatem, a.hclmcdated, "
                     + "a.hclmsinyy, a.hclmsinmm, a.hclmsindd, " //"a.sindate, " //"a.hclmsinyy, a.hclmsinmm, a.hclmsindd, "
                     + "a.hclmsoutyy, a.hclmsoutmm, a.hclmsoutdd, " //"a.soutdate, " //"a.hclmsoutyy, a.hclmsoutmm, a.hclmsoutdd, "
                     + "a.hclmrdatey, a.hclmrdatem, a.hclmrdated, " //"a.rdate, " //"a.hclmrdatey, a.hclmrdatem, a.hclmrdated, "
                     + "a.hclmpdatey, a.hclmpdatem, a.hclmpdated, "//"a.pdate, "//"a.hclmpdatey, a.hclmpdatem, a.hclmpdated, "
                     + "a.hclmdiscd1, a.hclmdiscd2, a.hclmdiscd3, " //"a.icd1, a.icd2, a.icd3, "
                     + "a.hclmrecid, " //a.recid, "
                     + "hovcoutno, a.hclmnhoscd  "
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
    				
    				if(startDate.getValue() != null && endDate.getValue() != null && !filterByPaymentDate){
    					//qry += "and cdate BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
    					qry += "and convert(datetime, convert(varchar,a.HCLMCDATEM)+'-'+convert(varchar,a.HCLMCDATED)+'-'+convert(varchar,a.HCLMCDATEY), 110) BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
    				}
    				
    				if(startDate.getValue() != null && endDate.getValue() != null && filterByPaymentDate){
    					qry += "and a.HCLMPDATEM > 0  and a.HCLMPDATED > 0 and a.HCLMPDATEY > 1900 ";
    					
    					qry += "and convert(datetime, convert(varchar,a.HCLMPDATEM)+'-'+convert(varchar,a.HCLMPDATED)+'-'+convert(varchar,a.HCLMPDATEY), 110) BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
    				}

                    qry = qry + "and a.hclmrecid <>'C' AND hdt1PONO<>99999 AND hdt1IDXNO < 99989";
                    
                    if (where!=null) qry +=  where;
                    
                    qry = qry + " order by convert(datetime, convert(varchar,a.HCLMCDATEM)+'-'+convert(varchar,a.HCLMCDATED)+'-'+convert(varchar,a.HCLMCDATEY), 110) desc ";
                    
                    List<Object[]> l = s.createSQLQuery(qry).list();
                    createReport(l);
        	
        	
    	}catch(Exception ex){
    		log.error("exportToXls", ex);
    	}finally {
            if (s!=null && s.isOpen()) s.close();
        }
    	
    	
        
    }
    
    public void exportWithDetail(){
    	
    	if (Messagebox.show("Retrieving this data would take up to 1 minutes, continue?", "Confirmation", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION)==Messagebox.OK){
    		Session s = Libs.sfDB.openSession();
        	String[] titles = new String[]{"Policy No", "Index", "Name", "Card Number", "Company Name", "Client Policy No", "Client Id", "Claim No", "Voucher No", 
        			"Claim Type", "Hospital Invoice No", "Provider Name", "Diagnosis", "Benefit", "Days", "Proposed", "Approved", "Excess", 
        			"Note", "Service In", "Service Out", "Claim Date", "Payment Date"};
        	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        	
        	StringBuffer sb = new StringBuffer();
            String judul = "Claim History ";
            if(filterByPaymentDate) judul = judul + "Payment Periode "+sdf.format(startDate.getValue()) + " To "+sdf.format(endDate.getValue());
            else judul = judul + "Claim Periode "+sdf.format(startDate.getValue()) + " To "+sdf.format(endDate.getValue());
             
            sb.append(judul);
             if(cbFilter.getSelectedIndex() != 4 && !tQuickSearch.getText().equalsIgnoreCase("")){
             	sb.append(" Filter By " +cbFilter.getSelectedItem().getLabel() + " : "+ tQuickSearch.getText());
             }else if(cbFilter.getSelectedIndex() == 4){
             	sb.append(" Filter By " +cbFilter.getSelectedItem().getLabel() + " : "+ cbClaimType.getSelectedItem().getLabel());
             }
             
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
            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(counter+1)+":W$"+(counter+1)+""));
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
    					   + Libs.createListFieldString("a.hclmaamt") + ", "
    					   + Libs.createListFieldString("a.hclmaday") + ", "
    					   + Libs.createListFieldString("a.hclmref")  + ", "
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
    			
        				if(startDate.getValue() != null && endDate.getValue() != null && !filterByPaymentDate){
        					//qry += "and cdate BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
        					qry += "and convert(datetime, convert(varchar,a.HCLMCDATEM)+'-'+convert(varchar,a.HCLMCDATED)+'-'+convert(varchar,a.HCLMCDATEY), 110) BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
        				}
        				
        				if(startDate.getValue() != null && endDate.getValue() != null && filterByPaymentDate){
        					qry += "and a.HCLMPDATEM > 0  and a.HCLMPDATED > 0 and a.HCLMPDATEY > 1900 ";
        					
        					qry += "and convert(datetime, convert(varchar,a.HCLMPDATEM)+'-'+convert(varchar,a.HCLMPDATED)+'-'+convert(varchar,a.HCLMPDATEY), 110) BETWEEN '"+startDate.getText()+"' AND '"+endDate.getText()+"' ";
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
       							  	mycell.setCellValue(Libs.nn(o[120]));
       							  	mycell.setCellStyle(styles.get("cell"));
       							  	
       							  	mycell = row.createCell(1); //Index
    							  	mycell.setCellValue(Libs.nn(o[122])+ " "+Libs.nn(o[123]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(2); //member name
       							  	mycell.setCellValue(Libs.nn(o[124]));
       							  	mycell.setCellStyle(styles.get("cell"));
       							  	
       							  	mycell = row.createCell(3); //card Number
    							  	mycell.setCellValue(Libs.nn(o[131]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(4); //company name
       							  	mycell.setCellValue(Libs.nn(o[121]));
       							  	mycell.setCellStyle(styles.get("cell"));
       							  	
       							  	mycell = row.createCell(5); //client policy no
    							  	mycell.setCellValue(Libs.nn(o[128]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(6); //client id
       							  	mycell.setCellValue(Libs.nn(o[129]));
       							  	mycell.setCellStyle(styles.get("cell"));
       							  	
       							  	hid = Libs.nn(o[137]);
       							  	mycell = row.createCell(7); //claim no
    							  	mycell.setCellValue(hid);
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(8); //voucher no
    							  	mycell.setCellValue(Libs.nn(o[148]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(9); //claim type
    							  	mycell.setCellValue(Libs.getClaimType(Libs.nn(o[125])));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	String[] result = getHospitalInvoice(hid.substring(4, hid.length()), o[149]);
    							  	mycell = row.createCell(10); //hospital invoice no
    							  	if(result != null) mycell.setCellValue(result[0]); else mycell.setCellValue(""); 
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    								mycell = row.createCell(11); //provider name
    							  	mycell.setCellValue(Libs.nn(o[126]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	String icd = Libs.nn(o[150]).trim(); if(!Libs.nn(o[151]).trim().equalsIgnoreCase("")) icd = icd + ","+Libs.nn(o[151]).trim();if(!Libs.nn(o[152]).trim().equalsIgnoreCase("")) icd = icd + ","+Libs.nn(o[152]).trim();
    							  	
    							  	mycell = row.createCell(12); //diagnosis
    							  	mycell.setCellValue(icd);
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	String plan = Libs.nn(o[130]);
    							  	Object[] obj = Libs.getBenefit(Libs.nn(o[120]), plan, i+1);
    							  	
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
    							  	
    							  	mycell = row.createCell(18);//keterangan
    							  	if(Libs.nn(o[i+90]).equals(""))mycell.setCellValue(Libs.nn(o[i+90]));
    							  	else {
    							  		
    							  		mycell.setCellValue(Libs.getRefDescription(Libs.nn(o[i+90])));
    							  	
    							  	}
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(19); //service in
    							  	if(hid.contains("OP"))
    							  		mycell.setCellValue(Libs.nn(o[145]));
    							  	else mycell.setCellValue(Libs.nn(o[143]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(20); //service out
    							  	if(hid.contains("OP"))
    							  		mycell.setCellValue(Libs.nn(o[145]));
    							  	else mycell.setCellValue(Libs.nn(o[144]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	mycell = row.createCell(21); //claim date
    							  	mycell.setCellValue(Libs.nn(o[142]));
    							  	mycell.setCellStyle(styles.get("cell"));
    							  	
    							  	
    							  	mycell = row.createCell(22); //payment date
    							  	if(Libs.nn(o[146]).equalsIgnoreCase("0-0-0"))
    							  		mycell.setCellValue("-");
    							  	else mycell.setCellValue(Libs.nn(o[146]));
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
        Window w = (Window) Executions.createComponents("views/ReportDialog.zul", this, null);
        w.setAttribute("title", "Claim History");
        w.setAttribute("product", cbPolicy.getSelectedItem().getLabel());
        w.doModal();

        if (w.getAttribute("export")!=null && w.getAttribute("export").equals(true)) {
            int scope = (Integer) w.getAttribute("scope");

            if (scope==0) {
                Session s = Libs.sfDB.openSession();
                try {
                    List<Object[]> l = s.createSQLQuery(queryString).list();
                    createReport(l);
                } catch (Exception ex) {
                    log.error("export", ex);
                } finally {
                    if (s!=null && s.isOpen()) s.close();
                }
            } else {
                Session s = Libs.sfDB.openSession();
                try {
                	
                    String productName = String.valueOf(w.getAttribute("product"));
                    int period = (Integer) w.getAttribute("period");

                    String qry = "select "
                    		+ "a.HID2 , a.ThnPolis, a.BrPolis, a.DistPolis, a.NoPolis, b.hhdrname, a.Idx, a.seq, c.hdt1name, a.tclaim, "
                            + "a.diAjukan as proposed, " //10
                            + "a.diBayarkan as approved, "
                            + "d.hproname, a.Counter, e.hempcnpol, e.hempcnid, '' as blank1, c.hdt1ncard, c.hdt1bdtyy, c.hdt1bdtmm, c.hdt1bdtdd, c.hdt1sex,  " //21
                            + "f.hdt2plan1, f.hdt2plan2, f.hdt2plan3, f.hdt2plan4, f.hdt2plan5, f.hdt2plan6, "
                            + "f.hdt2sdtyy, f.hdt2sdtmm, f.hdt2sdtdd, "
                            + "f.hdt2mdtyy, f.hdt2mdtmm, f.hdt2mdtdd, "
                            + "(convert(varchar,f.hdt2pedty1)+'-'+convert(varchar,f.hdt2pedtm1)+'-'+convert(varchar,f.hdt2pedtd1)) as hdt2pedt1,"
                            + "(convert(varchar,f.hdt2pedty2)+'-'+convert(varchar,f.hdt2pedtm2)+'-'+convert(varchar,f.hdt2pedtd2)) as hdt2pedt2,"
                            + "(convert(varchar,f.hdt2pedty3)+'-'+convert(varchar,f.hdt2pedtm3)+'-'+convert(varchar,f.hdt2pedtd3)) as hdt2pedt3,"
                            + "(convert(varchar,f.hdt2pedty4)+'-'+convert(varchar,f.hdt2pedtm4)+'-'+convert(varchar,f.hdt2pedtd4)) as hdt2pedt4,"
                            + "(convert(varchar,f.hdt2pedty5)+'-'+convert(varchar,f.hdt2pedtm5)+'-'+convert(varchar,f.hdt2pedtd5)) as hdt2pedt5,"
                            + "(convert(varchar,f.hdt2pedty6)+'-'+convert(varchar,f.hdt2pedtm6)+'-'+convert(varchar,f.hdt2pedtd6)) as hdt2pedt6,"
                            + "(convert(varchar,f.hdt2pxdty1)+'-'+convert(varchar,f.hdt2pxdtm1)+'-'+convert(varchar,f.hdt2pxdtd1)) as hdt2pxdt1,"
                            + "(convert(varchar,f.hdt2pxdty2)+'-'+convert(varchar,f.hdt2pxdtm2)+'-'+convert(varchar,f.hdt2pxdtd2)) as hdt2pxdt2,"
                            + "(convert(varchar,f.hdt2pxdty3)+'-'+convert(varchar,f.hdt2pxdtm3)+'-'+convert(varchar,f.hdt2pxdtd3)) as hdt2pxdt3,"
                            + "(convert(varchar,f.hdt2pxdty4)+'-'+convert(varchar,f.hdt2pxdtm4)+'-'+convert(varchar,f.hdt2pxdtd4)) as hdt2pxdt4,"
                            + "(convert(varchar,f.hdt2pxdty5)+'-'+convert(varchar,f.hdt2pxdtm5)+'-'+convert(varchar,f.hdt2pxdtd5)) as hdt2pxdt5,"
                            + "(convert(varchar,f.hdt2pxdty6)+'-'+convert(varchar,f.hdt2pxdtm6)+'-'+convert(varchar,f.hdt2pxdtd6)) as hdt2pxdt6, "
                            + "c.hdt1mstat, "
                            + "g.hmem2data1, g.hmem2data2, g.hmem2data3, g.hmem2data4, "
                            + "a.cdate,"  //"a.hclmcdatey, a.hclmcdatem, a.hclmcdated, "
                            + "a.sindate, " //"a.hclmsinyy, a.hclmsinmm, a.hclmsindd, "
                            + "a.soutdate, " //"a.hclmsoutyy, a.hclmsoutmm, a.hclmsoutdd, "
                            + "a.rdate, " //"a.hclmrdatey, a.hclmrdatem, a.hclmrdated, "
                            + "a.pdate, "//"a.hclmpdatey, a.hclmpdatem, a.hclmpdated, "
                            + "a.icd1, a.icd2, a.icd3, "
                            + "a.recid "
                            + "from idnhltpf.dbo.hlthdr b "
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
                            + "where "
                            + "b.hhdrinsid";                    
            				if(products.size() > 0) qry = qry + " in  ("+insid+") ";
            				else qry = qry + "='" + Libs.getInsuranceId() + "' ";  
            				
            				if(polisList.size() > 0){
                    			qry = qry + "and convert(varchar,b.hhdryy)+'-'+convert(varchar,b.hhdrbr)+'-'+convert(varchar,b.hhdrdist)+'-'+convert(varchar,b.hhdrpono) "
                    					  + "in ("+polis+") ";
                    		} 

                            qry = qry + "and a.recid<>'C' AND hdt1PONO<>99999 AND hdt1IDXNO < 99989";

                    if (!Libs.nn(userProductViewrestriction).isEmpty()) qry += "and b.hhdrpono in (" + userProductViewrestriction + ") ";

                    if (!productName.toLowerCase().equals("all products")) {
                        String policy = productName.substring(productName.indexOf("(")+1, productName.indexOf(")"));
                        String policyNo[] = policy.split("-");
    	                qry += "and b.hhdryy='"+policyNo[0]+"' and b.hhdrbr='"+policyNo[1]+"' and b.hhdrdist='"+policyNo[2]+"' and b.hhdrpono='" + policyNo[3] + "' ";
    	                
//                        qry += "and (convert(varchar,a.hclmyy)+'-'+convert(varchar,a.hclmbr)+'-'+convert(varchar,a.hclmdist)+'-'+convert(varchar,a.hclmpono)='" + policy + "') ";
                    }
                    
                    Date dateStart = (Date) w.getAttribute("dateStart");
                    Date dateEnd = (Date) w.getAttribute("dateEnd");
                    
                    qry += "and a.cdate between '" + new SimpleDateFormat("yyyy-MM-dd").format(dateStart) + "' and '" + new SimpleDateFormat("yyyy-MM-dd").format(dateEnd) + "'";
                    
                    /*

                    switch (period) {
                        case 0:
                            Date date = (Date) w.getAttribute("date");
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            cal.set(Calendar.DAY_OF_MONTH, 1);
                            cal.set(Calendar.MONTH, 0);
                            qry += "and convert(datetime,convert(varchar,hclmcdated)+'-'+convert(varchar,hclmcdatem)+'-'+convert(varchar,hclmcdatey),105) between '" + new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()) + "' and '" + new SimpleDateFormat("yyyy-MM-dd").format(date) + "'";
                            break;
                        case 1:
                            break;
                        case 2:
                            Date dateStart = (Date) w.getAttribute("dateStart");
                            Date dateEnd = (Date) w.getAttribute("dateEnd");
                            qry += "and convert(datetime,convert(varchar,hclmcdated)+'-'+convert(varchar,hclmcdatem)+'-'+convert(varchar,hclmcdatey),105) between '" + new SimpleDateFormat("yyyy-MM-dd").format(dateStart) + "' and '" + new SimpleDateFormat("yyyy-MM-dd").format(dateEnd) + "'";
                            break;
                        case 3:
                            int monthStart = (Integer) w.getAttribute("monthStart");
                            int monthEnd = (Integer) w.getAttribute("monthEnd");
                            int monthYearStart = (Integer) w.getAttribute("monthYearStart");
                            int monthYearEnd = (Integer) w.getAttribute("monthYearEnd");
                            int maxDay = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
                            Calendar calStart = Calendar.getInstance();
                            Calendar calEnd = Calendar.getInstance();
                            calStart.set(monthYearStart, monthStart, 1);
                            calEnd.set(monthYearEnd, monthEnd, maxDay);
                            qry += "and convert(datetime,convert(varchar,hclmcdated)+'-'+convert(varchar,hclmcdatem)+'-'+convert(varchar,hclmcdatey),105) between '" + new SimpleDateFormat("yyyy-MM-dd").format(calStart.getTime()) + "' and '" + new SimpleDateFormat("yyyy-MM-dd").format(calEnd.getTime()) + "'";
                            break;
                        case 4:
                            int yearStart = (Integer) w.getAttribute("yearStart");
                            int yearEnd = (Integer) w.getAttribute("yearEnd");
                            Calendar calStart1 = Calendar.getInstance();
                            Calendar calEnd1 = Calendar.getInstance();
                            calStart1.set(yearStart, 0, 1);
                            calEnd1.set(yearEnd, 11, 31);
                            qry += "and convert(datetime,convert(varchar,hclmcdated)+'-'+convert(varchar,hclmcdatem)+'-'+convert(varchar,hclmcdatey),105) between '" + new SimpleDateFormat("yyyy-MM-dd").format(calStart1.getTime()) + "' and '" + new SimpleDateFormat("yyyy-MM-dd").format(calEnd1.getTime()) + "'";
                            break;
                    }
                    */
                    
//                    System.out.println("\n" +qry);

                    List<Object[]> l = s.createSQLQuery(qry).list();
                    createReport(l);
                } catch (Exception ex) {
                    log.error("export", ex);
                } finally {
                    if (s!=null && s.isOpen()) s.close();
                }
            }
        }
    }

    private void createReport(List<Object[]> list) {
        /*String[] columnsMemberWise = new String[] {
                "POLICY YEAR", "BR", "DIST", "POLICY NUMBER", "COMPANY NAME", "INDEX", "SEQ", "CARD NUMBER",
                "NAME", "COUNT", "TYPE", "CLAIM-YEAR", "CLAIM-MONTH", "CLAIM-DAY", "SIN-YEAR", "SIN-MONTH",
                "SIN-DAY", "SOUT-YEAR", "SOUT-MONTH", "SOUT-DAY", "RECEIPT-YEAR", "RECEIPT-MONTH",
                "RECEIPT-DAY", "PAYMENT-YEAR", "PAYMENT-MONTH", "PAYMENT-DAY", "HID NUMBER", "PROVIDER NAME",
                "ICD1", "ICD2", "ICD3", "PROPOSED", "APPROVED", "STATUS", "MEMO" };*/
    	
    	String[] columnsMemberWise = new String[] {
                "POLICY NUMBER", "COMPANY NAME", "INDEX", "SEQ", "CARD NUMBER",
                "NAME", "AGE", "SEX", "COUNT", "TYPE", "EMPLOYEE NAME", "EMP INDEX", "EMP SEQ","REGISTER DATE","CLAIM-DATE", "SIN-DATE", "SOUT-DATE", "RECEIPT-DATE", "PAYMENT-DATE", 
                "HID NUMBER", "VOUCHER NO", "HOSPITAL INVOICE NO", "PROVIDER NAME", "ICD1", "ICD2", "ICD3", 
                "PROPOSED", "APPROVED", "STATUS", "MEMO"};
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        try {
            Workbook wb = new HSSFWorkbook();
            Sheet sheet = wb.createSheet("Claim History");
            
            int cnt = 0;
            
          
            StringBuffer sb = new StringBuffer();
            String judul = "Claim History ";
            if(filterByPaymentDate) judul = judul + "Payment Periode "+sdf.format(startDate.getValue()) + " To "+sdf.format(endDate.getValue());
            else judul = judul + "Claim Periode "+sdf.format(startDate.getValue()) + " To "+sdf.format(endDate.getValue());
            
            sb.append(judul);
            if(cbFilter.getSelectedIndex() != 4 && !tQuickSearch.getText().equalsIgnoreCase("")){
            	sb.append(" Filter By " +cbFilter.getSelectedItem().getLabel() + " : "+ tQuickSearch.getText());
            }else if(cbFilter.getSelectedIndex() == 4){
            	sb.append(" Filter By " +cbFilter.getSelectedItem().getLabel() + " : "+ cbClaimType.getSelectedItem().getLabel());
            }
            
            sb.append(" Product : " + cbPolicy.getSelectedItem().getLabel());
            
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(cnt);
            org.apache.poi.ss.usermodel.Cell titleCell = row.createCell(0);
            sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(cnt+1)+":AB$"+(cnt+1)+""));
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
                
                int ageDays = Libs.getDiffDays(new SimpleDateFormat("yyyy-MM-dd").parse(Libs.nn(o[18]) + "-" + Libs.nn(o[19]) + "-" + Libs.nn(o[20])), new Date());
                
                //age and sex
                Libs.createCell(row, 6, ageDays/365);
                Libs.createCell(row, 7, Libs.nn(o[21]));
                
                Libs.createCell(row, 8, Libs.nn(o[13]));
                Libs.createCell(row, 9, Libs.nn(o[9]));
               
                String empName = getEmployeeName(Libs.nn(o[1])+"-"+Libs.nn(o[2])+"-"+Libs.nn(o[3])+"-"+Libs.nn(o[4]), Libs.nn(o[6]));
                if(empName != null){
                	Libs.createCell(row, 10, empName);
                	Libs.createCell(row, 11, Libs.nn(o[6]));
                	Libs.createCell(row, 12, "A");
                }else{
                	Libs.createCell(row, 10, "");
                	Libs.createCell(row, 11, "");
                	Libs.createCell(row, 12, "");
                }
                
                
                String receiptDate = o[60] + "-" + o[61] + "-" + o[62];if(receiptDate.equalsIgnoreCase("0-0-0"))receiptDate="-";
                String paymentDate = o[63] + "-" + o[64] + "-" + o[65];if(paymentDate.equalsIgnoreCase("0-0-0"))paymentDate="-";
                String serviceIn = o[54] + "-" + o[55] + "-" + o[56]; if(serviceIn.equalsIgnoreCase("0-0-0"))serviceIn="-";
                String serviceOut = o[57] + "-" + o[58] + "-" + o[59];if(serviceOut.equalsIgnoreCase("0-0-0"))serviceOut="-";
                String claimDate = o[51] + "-" + o[52] + "-" + o[53]; 
                
                String hidNumber =Libs.nn(o[0]);
                String[] result = getHospitalInvoice(hidNumber.substring(4, hidNumber.length()), o[71]);
                
                
                
                if(result != null)
                	Libs.createCell(row, 13, result[1]);
                else
                	Libs.createCell(row, 13, "");
                
                Libs.createCell(row, 14, claimDate);
                Libs.createCell(row, 15, serviceIn);
                Libs.createCell(row, 16, serviceOut);
                
                Libs.createCell(row, 17, receiptDate);  //Libs.createCell(row, 11, Libs.nn(o[51]));
                Libs.createCell(row, 18, paymentDate);  //Libs.createCell(row, 12, Libs.nn(o[52]));
                Libs.createCell(row, 19, Libs.nn(o[0]));  //Libs.createCell(row, 13, Libs.nn(o[53]));
                Libs.createCell(row, 20, Libs.nn(o[70])); //Libs.createCell(row, 14, Libs.nn(o[54]));
                
                
                
               
                if(result != null)
                	Libs.createCell(row, 21, result[0]);
                else Libs.createCell(row, 21, "");
                
                Libs.createCell(row, 22, Libs.nn(o[12]));
                Libs.createCell(row, 23, Libs.nn(o[66]));
                Libs.createCell(row, 24, Libs.nn(o[67]));
                Libs.createCell(row, 25, Libs.nn(o[68]));
                
                Double d =(Double)o[10];
                Cell cell = row.createCell(26); 
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(d.doubleValue());
                
                d = (Double)o[11];
                cell = row.createCell(27); 
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(d.doubleValue());
                
                
                Libs.createCell(row, 28, Libs.getStatus(Libs.nn(o[69])));
                Libs.createCell(row, 29, Libs.nn(o[47]).trim() + Libs.nn(o[48]).trim() + Libs.nn(o[49]).trim() + Libs.nn(o[50]).trim());
                
               
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
