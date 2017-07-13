package com.imc.ocisv3.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.imc.ocisv3.pojos.BenefitPOJO;
import com.imc.ocisv3.pojos.MemberPOJO;
import com.imc.ocisv3.pojos.PolicyPOJO;
import com.imc.ocisv3.tools.Libs;

/**
 * Created by faizal on 10/30/13.
 */
public class MemberListController extends Window {

    private Logger log = LoggerFactory.getLogger(MemberListController.class);
    private Listbox lb;
    private Paging pg;
    private String where;
    private Combobox cbPolicy;
    private Combobox cbStatus;
    private Label lastLbl;
    private Label ttlMember;
    private Label idxLbl;
    private String userProductViewrestriction;
    
    private String polis ="";
    private List polisList;
    private String insid="";
    private List products;
    private boolean isInternal = false;

    public void onCreate() {
        if (!Libs.checkSession()) {
            userProductViewrestriction = Libs.restrictUserProductView.get(Libs.getUser());
            Integer userlevel = (Integer)Executions.getCurrent().getSession().getAttribute("userLevel");
            if(userlevel.intValue() == 1) isInternal = true;
           
            
            initComponents();
            /*
//            populateCount();
            
        	products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
            
            polisList = Libs.getPolisByUserId(Libs.getUser());
            for(int i=0; i < polisList.size(); i++){
        		polis=polis+"'"+(String)polisList.get(i)+"'"+",";
        	}
            if(polis.length() > 1)polis = polis.substring(0, polis.length()-1);
            
            populateCountForQuickSearch();
            populate(0, pg.getPageSize());*/
            
            populateMember(0, pg.getPageSize());
        }
    }

    private void initComponents() {
        lb = (Listbox) getFellow("lb");
        pg = (Paging) getFellow("pg");
        cbPolicy = (Combobox) getFellow("cbPolicy");
        cbStatus = (Combobox) getFellow("cbStatus");
        lastLbl = (Label)getFellow("lastLbl");
        idxLbl = (Label)getFellow("idxLbl");
        ttlMember = (Label)getFellow("ttlMember");

        pg.addEventListener("onPaging", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                PagingEvent evt = (PagingEvent) event;
//                populate(evt.getActivePage()*pg.getPageSize(), pg.getPageSize());
                populateMember(evt.getActivePage()*pg.getPageSize(), pg.getPageSize());
            }
        });

        /*cbPolicy.appendItem("All Products");
        
        cbPolicy.setSelectedIndex(0);
        boolean show = true;
//        for (String s : Libs.policyMap.keySet()) {
        for (String s : Libs.getPolicyMap().keySet()) {
//            String policyName = Libs.policyMap.get(s);
        	String policyName = Libs.getPolicyMap().get(s);
            if (Libs.config.get("demo_mode").equals("true") && Libs.getInsuranceId().equals("00051")) policyName = Libs.nn(Libs.config.get("demo_name"));

            String restriction = Libs.restrictUserProductView.get(Libs.getUser());
            if (!Libs.nn(restriction).isEmpty()) {
                if (!restriction.contains(s.split("\\-")[3])) show=false;
                else show=true;
            }

            if (show) cbPolicy.appendItem(policyName + " (" + s + ")");
        }*/
        
        
        Libs.getProduct(cbPolicy);
        
        cbStatus.appendItem("ACTIVE");
        cbStatus.appendItem("INACTIVE");
        cbStatus.appendItem("MATURE");
        cbStatus.setSelectedIndex(0);

       
        /*Listheader lhEmployeeId = (Listheader) getFellow("lhEmployeeId");
        if (Libs.getInsuranceId().equals("00078") || Libs.getInsuranceId().equals("00088")) {
            lhEmployeeId.setVisible(true);
        } else {
            lhEmployeeId.setVisible(false);
        }*/
        
        idxLbl.setVisible(false);
        lastLbl.setVisible(false);
    }

   

	private void populateCount() {
        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select count(*) from idnhltpf.dbo.hlthdr b "
                    + "inner join idnhltpf.dbo.hltdt1 a "
                    + "on b.hhdryy=a.hdt1yy and b.hhdrpono=a.hdt1pono "
                    + "inner join idnhltpf.dbo.hltdt2 d "
                    + "on a.hdt1yy=d.hdt2yy and a.hdt1pono=d.hdt2pono and a.hdt1idxno=d.hdt2idxno and a.hdt1seqno=d.hdt2seqno and a.hdt1ctr=d.hdt2ctr "
                    + "where "
                    + "b.hhdrinsid";
            
            	if(products.size() > 0) qry = qry + " in  ("+insid+")";
            	else qry = qry + "='" + Libs.getInsuranceId() + "' ";  
            
            	qry = qry + " and a.hdt1ctr=0 and a.hdt1idxno < 99989 and a.hdt1pono <> 99999 ";

            if (where!=null) qry += "and (" + where + ") ";
            
            if (cbPolicy.getSelectedIndex()>0) {
                String policy = cbPolicy.getSelectedItem().getLabel();
                policy = policy.substring(policy.indexOf("(")+1, policy.indexOf(")"));
                qry += "and (convert(varchar,a.hdt1yy)+'-'+convert(varchar,a.hdt1br)+'-'+convert(varchar,a.hdt1dist)+'-'+convert(varchar,a.hdt1pono)='" + policy + "') ";
            }
            
            if(cbStatus.getSelectedIndex() == 0){
            	qry += " and d.hdt2moe not in('M','U') and convert(datetime, convert(varchar,HDT2MDTMM)+'-'+convert(varchar,HDT2MDTDD)+'-'+convert(varchar,HDT2MDTYY), 110) > GETDATE() ";
            }else if(cbStatus.getSelectedIndex() == 1){
            	qry += " and d.hdt2moe = 'U'";
            }else qry += " and d.hdt2moe = 'M'";
            
//            System.out.println(qry);

            Integer recordsCount = (Integer) s.createSQLQuery(qry).uniqueResult();
            pg.setTotalSize(recordsCount);
            ttlMember.setValue(recordsCount.toString());
            
        } catch (Exception ex) {
            log.error("populateCount", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    private void populateCountForQuickSearch() {
        Session s = Libs.sfDB.openSession();
        try {
        	
            String qry = "select count(*) from idnhltpf.dbo.hlthdr b "
                    + "inner join idnhltpf.dbo.hltdt1 a "
                    + "on b.hhdryy=a.hdt1yy and b.hhdrbr=a.hdt1br and b.hhdrdist=a.hdt1dist and b.hhdrpono=a.hdt1pono "
                    + "inner join idnhltpf.dbo.hltdt2 d "
                    + "on a.hdt1yy=d.hdt2yy and a.hdt1br=d.hdt2br and a.hdt1dist=d.hdt2dist and a.hdt1pono=d.hdt2pono and a.hdt1idxno=d.hdt2idxno and a.hdt1seqno=d.hdt2seqno and a.hdt1ctr=d.hdt2ctr "
                    + " inner join idnhltpf.dbo.hltemp c on a.hdt1yy=c.hempyy and  a.HDT1BR=c.HEMPBR and a.HDT1DIST=c.HEMPDIST and a.hdt1pono=c.hemppono and a.hdt1idxno=c.hempidxno and a.hdt1seqno=c.hempseqno and a.hdt1ctr=c.hempctr "
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
                        qry += " and (convert(varchar,b.hhdryy)+'-'+convert(varchar,b.hhdrbr)+'-'+convert(varchar,b.hhdrdist)+'-'+convert(varchar,b.hhdrpono)='" + policy + "') ";
                    }
            		
            		qry = qry + " and a.hdt1ctr=0 and a.hdt1idxno < 99989 and a.hdt1pono <> 99999 AND a.HDT1NAME not like '%DUMMY%' ";
                     
            		
            if (where!=null) qry += "and (" + where + ") ";

            
            if(cbStatus.getSelectedIndex() == 0){
            	qry += " and d.hdt2moe not in('M','U') and convert(datetime, convert(varchar,HDT2MDTMM)+'-'+convert(varchar,HDT2MDTDD)+'-'+convert(varchar,HDT2MDTYY), 110) > GETDATE() ";
            }else if(cbStatus.getSelectedIndex() == 1){
            	qry += " and d.hdt2moe = 'U'";
            }else qry += " and d.hdt2moe = 'M'";
            
            System.out.println(qry);

            Integer recordsCount = (Integer) s.createSQLQuery(qry).uniqueResult();
            pg.setTotalSize(recordsCount);
            ttlMember.setValue(recordsCount.toString());
        } catch (Exception ex) {
            log.error("populateCountForQuickSearch", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }
    
    private void populateMember(int offset, int limit){
    	lb.getItems().clear();
    	Session s = Libs.sfOCIS.openSession();
    	try{
    		String count = "Select count(1) ";
    		String select = "Select * ";
    		String sql = " from "+Libs.getDbName()+".dbo.F_OCISMember(:idclient, null) ";
    		if(cbPolicy.getSelectedIndex() > 0){
    			sql = sql + "where PolisNo=:polisNo ";
    			if(where != null) sql = sql + " and " +where;
    		}else{
    			if(where != null) sql = sql + " where "+where;
    		}
    		
    		SQLQuery countQry = s.createSQLQuery(count+sql);
    		countQry.setInteger("idclient", Libs.getNewInsuranceId());
    		
    		SQLQuery query = s.createSQLQuery(select + sql);
    		query.setInteger("idclient", Libs.getNewInsuranceId());
    		if(cbPolicy.getSelectedIndex() > 0){
    			String policy = cbPolicy.getSelectedItem().getLabel();
                policy = policy.substring(policy.indexOf("(")+1, policy.indexOf(")"));
    			query.setString("polisNo", policy);
    			countQry.setString("polisNo", policy);
    		}
    		
    		Integer recordsCount = (Integer) countQry.uniqueResult();
            pg.setTotalSize(recordsCount);
            ttlMember.setValue(recordsCount.toString());
    		
    		
            int startNumber = (((pg.getActivePage()+1) * 20) - 20) + 1; 
    		
    		List<Object[]> l = query.setFirstResult(offset).setMaxResults(limit).list();
    		for(Object[] o : l){
    			Listitem item = new Listitem();
    			
    			item.appendChild(new Listcell(""+startNumber));
    			item.appendChild(new Listcell(Libs.nn(o[1]).toUpperCase()));
    			
    			A memberName = new A(Libs.nn(o[2]));
	        	memberName.setStyle("color:#00bbee;text-decoration:none");
	        	Listcell cell = new Listcell();
	        	cell.appendChild(memberName);
    			item.appendChild(cell);
    			
      			item.appendChild(new Listcell(Libs.nn(o[3])));
      			item.appendChild(new Listcell(Libs.nn(o[4])));
      			item.appendChild(new Listcell(Libs.nn(o[5])));
      			item.appendChild(new Listcell(Libs.nn(o[6])));
      			item.appendChild(new Listcell(Libs.formatDate((Date)o[7])));
      			item.appendChild(new Listcell(Libs.nn(o[8])));
      			item.appendChild(new Listcell(Libs.nn(o[9])));
      			item.appendChild(new Listcell(Libs.formatDate((Date)o[10])));
      			item.appendChild(new Listcell(Libs.formatDate((Date)o[11])));
      			
      			item.appendChild(new Listcell(Libs.nn(o[12])));
      			if(o[13] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[13]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			if(o[14] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[14]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			if(o[15] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[15]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			
      			item.appendChild(new Listcell(Libs.nn(o[16])));
      			if(o[17] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[17]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			if(o[18] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[18]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			if(o[19] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[19]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			
      			item.appendChild(new Listcell(Libs.nn(o[20])));
      			if(o[21] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[21]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			if(o[22] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[22]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			if(o[23] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[23]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			
      			item.appendChild(new Listcell(Libs.nn(o[24])));
      			if(o[25] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[25]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			if(o[26] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[26]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			if(o[27] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[27]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			
      			item.appendChild(new Listcell(Libs.nn(o[28])));
      			if(o[29] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[29]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			if(o[30] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[30]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			if(o[31] != null)item.appendChild(Libs.createNumericListcell(((BigDecimal)o[31]).doubleValue(), "#,###.##"));
      			else item.appendChild(new Listcell("-"));
      			
      			final BigInteger memberNo = (BigInteger)o[32];
      			
      			startNumber = startNumber + 1;
      			
      			memberName.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						showMemberDetailNew(memberNo);
					}
				});
      			
      			lb.appendChild(item);
    		}
    		
    		
    	}catch(Exception e){
    		log.error("populateMember", e);
    	}finally{
    		if(s!=null && s.isOpen()) s.close();
    	}
    	
    }
    
    public void showMemberDetailNew(BigInteger memberNo){
    	Window w = (Window) Executions.createComponents("views/MemberDetail.zul", Libs.getRootWindow(), null);
    	w.setAttribute("memberId", memberNo);
    	w.doModal();
    }

    private void populate(int offset, int limit) {
        lb.getItems().clear();
        Session s = Libs.sfDB.openSession();
        try {
        	
        	
        	
            String select = "select "
                    + "a.hdt1ncard, a.hdt1name, "
                    + "a.hdt1bdtyy, a.hdt1bdtmm, a.hdt1bdtdd, "
                    + "a.hdt1sex, "
                    + "b.hdt2plan1, b.hdt2plan2, b.hdt2plan3, b.hdt2plan4, b.hdt2plan5, b.hdt2plan6, "
                    + "b.hdt2sdtyy, b.hdt2sdtmm, b.hdt2sdtdd, "
                    + "b.hdt2mdtyy, b.hdt2mdtmm, b.hdt2mdtdd, "
                    + "a.hdt1idxno, a.hdt1seqno, "
                    + "c.hempcnid, " //20
                    + "(convert(varchar,b.hdt2pedty1)+'-'+convert(varchar,b.hdt2pedtm1)+'-'+convert(varchar,b.hdt2pedtd1)) as hdt2pedt1,"
                    + "(convert(varchar,b.hdt2pedty2)+'-'+convert(varchar,b.hdt2pedtm2)+'-'+convert(varchar,b.hdt2pedtd2)) as hdt2pedt2,"
                    + "(convert(varchar,b.hdt2pedty3)+'-'+convert(varchar,b.hdt2pedtm3)+'-'+convert(varchar,b.hdt2pedtd3)) as hdt2pedt3,"
                    + "(convert(varchar,b.hdt2pedty4)+'-'+convert(varchar,b.hdt2pedtm4)+'-'+convert(varchar,b.hdt2pedtd4)) as hdt2pedt4,"
                    + "(convert(varchar,b.hdt2pedty5)+'-'+convert(varchar,b.hdt2pedtm5)+'-'+convert(varchar,b.hdt2pedtd5)) as hdt2pedt5,"
                    + "(convert(varchar,b.hdt2pedty6)+'-'+convert(varchar,b.hdt2pedtm6)+'-'+convert(varchar,b.hdt2pedtd6)) as hdt2pedt6,"
                    + "(convert(varchar,b.hdt2pxdty1)+'-'+convert(varchar,b.hdt2pxdtm1)+'-'+convert(varchar,b.hdt2pxdtd1)) as hdt2pxdt1,"
                    + "(convert(varchar,b.hdt2pxdty2)+'-'+convert(varchar,b.hdt2pxdtm2)+'-'+convert(varchar,b.hdt2pxdtd2)) as hdt2pxdt2,"
                    + "(convert(varchar,b.hdt2pxdty3)+'-'+convert(varchar,b.hdt2pxdtm3)+'-'+convert(varchar,b.hdt2pxdtd3)) as hdt2pxdt3,"
                    + "(convert(varchar,b.hdt2pxdty4)+'-'+convert(varchar,b.hdt2pxdtm4)+'-'+convert(varchar,b.hdt2pxdtd4)) as hdt2pxdt4,"
                    + "(convert(varchar,b.hdt2pxdty5)+'-'+convert(varchar,b.hdt2pxdtm5)+'-'+convert(varchar,b.hdt2pxdtd5)) as hdt2pxdt5,"
                    + "(convert(varchar,b.hdt2pxdty6)+'-'+convert(varchar,b.hdt2pxdtm6)+'-'+convert(varchar,b.hdt2pxdtd6)) as hdt2pxdt6, "
                    + "a.hdt1mstat, c.hempcnpol, " //33
                    + "a.hdt1yy, a.hdt1br, a.hdt1dist, a.hdt1pono, "
                    + "d.hhdrname, "
                    + "b.hdt2moe, " //40
                    + "b.hdt2xdtyy, b.hdt2xdtmm, b.hdt2xdtdd, "
                    + "c.hempmemo3 ";


            String qry = "from idnhltpf.dbo.hlthdr d  "
            		+" inner join idnhltpf.dbo.hltdt1 a on d.hhdryy=a.hdt1yy and d.hhdrbr=a.hdt1br and d.hhdrdist=a.hdt1dist and d.hhdrpono=a.hdt1pono " 
                    + "inner join idnhltpf.dbo.hltdt2 b on a.hdt1yy=b.hdt2yy and a.hdt1br=b.hdt2br and a.hdt1dist=b.hdt2dist and a.hdt1pono=b.hdt2pono and a.hdt1idxno=b.hdt2idxno and a.hdt1seqno=b.hdt2seqno and a.hdt1ctr=b.hdt2ctr "
                    + "inner join idnhltpf.dbo.hltemp c on a.hdt1yy=c.hempyy and a.HDT1BR=c.HEMPBR and a.HDT1DIST=c.HEMPDIST and a.hdt1pono=c.hemppono and a.hdt1idxno=c.hempidxno and a.hdt1seqno=c.hempseqno and a.hdt1ctr=c.hempctr "
                    + "where "
                    + "d.hhdrinsid";
            		if(products.size() > 0) qry = qry + " in  ("+insid+")";
            		else qry = qry + "='" + Libs.getInsuranceId() + "' "; 
            		
            		if(polisList.size() > 0){
            			qry = qry + "and convert(varchar,d.hhdryy)+'-'+convert(varchar,d.hhdrbr)+'-'+convert(varchar,d.hhdrdist)+'-'+convert(varchar,d.hhdrpono) "
            					  + "in ("+polis+") ";
            		} 
            		
            		if (cbPolicy.getSelectedIndex()>0) {
                        String policy = cbPolicy.getSelectedItem().getLabel();
                        policy = policy.substring(policy.indexOf("(")+1, policy.indexOf(")"));
                        qry += "and (convert(varchar,d.hhdryy)+'-'+convert(varchar,d.hhdrbr)+'-'+convert(varchar,d.hhdrdist)+'-'+convert(varchar,d.hhdrpono)='" + policy + "') ";
                    }
            		
            		qry = qry + "and a.hdt1ctr=0 and a.hdt1idxno < 99989 and a.hdt1pono <> 99999 and a.HDT1NAME not like '%DUMMY%' ";

            if (!Libs.nn(userProductViewrestriction).isEmpty()) qry += "and d.hhdrpono in (" + userProductViewrestriction + ") ";

            if (where!=null) qry += "and (" + where + ") ";

            
            
            if(cbStatus.getSelectedIndex() == 0){
            	qry += " and b.hdt2moe not in('M','U') and convert(datetime, convert(varchar,HDT2MDTMM)+'-'+convert(varchar,HDT2MDTDD)+'-'+convert(varchar,HDT2MDTYY), 110) > GETDATE() ";
            }else if(cbStatus.getSelectedIndex() == 1){
            	qry += " and b.hdt2moe = 'U' ";
            }else qry += " and b.hdt2moe = 'M' ";
            

            String order = "order by a.hdt1name asc ";
            
//            System.out.println(select + qry + order);

            List<Object[]> l = s.createSQLQuery(select + qry + order).setFirstResult(offset).setMaxResults(limit).list();
            
            int page = pg.getActivePage();
            boolean isFamilyLimit;
            double planUsage;
            
            int startNumber = (((page+1) * 20) - 20) + 1; 

            for (Object[] o : l) {
            	isFamilyLimit = false;
            	
                PolicyPOJO policyPOJO = new PolicyPOJO();
                policyPOJO.setYear(Integer.valueOf(Libs.nn(o[35])));
                policyPOJO.setBr(Integer.valueOf(Libs.nn(o[36])));
                policyPOJO.setDist(Integer.valueOf(Libs.nn(o[37])));
                policyPOJO.setPolicy_number(Integer.valueOf(Libs.nn(o[38])));
                policyPOJO.setName(Libs.nn(o[39]).trim());

                Map<String,String> clientPlanMap = Libs.getClientPlanMap(policyPOJO.getPolicy_string());

                final MemberPOJO memberPOJO = new MemberPOJO();
                memberPOJO.setPolicy(policyPOJO);
                memberPOJO.setName(Libs.nn(o[1]).trim());
                memberPOJO.setCard_number(Libs.nn(o[0]).trim());
                memberPOJO.setDob(Libs.nn(o[2]) + "-" + Libs.nn(o[3]) + "-" + Libs.nn(o[4]));
                memberPOJO.setStarting_date(Libs.nn(o[12]) + "-" + Libs.nn(o[13]) + "-" + Libs.nn(o[14]));
                memberPOJO.setMature_date(Libs.nn(o[15]) + "-" + Libs.nn(o[16]) + "-" + Libs.nn(o[17]));
                memberPOJO.setSex(Libs.nn(o[5]));
                memberPOJO.setIp(Libs.nn(o[6]).trim());
                memberPOJO.setOp(Libs.nn(o[7]).trim());
                memberPOJO.setMaternity(Libs.nn(o[8]).trim());
                memberPOJO.setDental(Libs.nn(o[9]).trim());
                memberPOJO.setGlasses(Libs.nn(o[10]).trim());
                memberPOJO.setMarital_status(Libs.nn(o[33]));
                memberPOJO.setIdx(Libs.nn(o[18]));
                memberPOJO.setSeq(Libs.nn(o[19]));
                memberPOJO.setClient_policy_number(Libs.nn(o[34]).trim());
                memberPOJO.setClient_id_number(Libs.nn(o[20]).trim());

                memberPOJO.getPlan_entry_date().add(Libs.nn(o[21]));
                memberPOJO.getPlan_entry_date().add(Libs.nn(o[22]));
                memberPOJO.getPlan_entry_date().add(Libs.nn(o[23]));
                memberPOJO.getPlan_entry_date().add(Libs.nn(o[24]));
                memberPOJO.getPlan_entry_date().add(Libs.nn(o[25]));
                memberPOJO.getPlan_entry_date().add(Libs.nn(o[26]));
                memberPOJO.getPlan_exit_date().add(Libs.nn(o[27]));
                memberPOJO.getPlan_exit_date().add(Libs.nn(o[28]));
                memberPOJO.getPlan_exit_date().add(Libs.nn(o[29]));
                memberPOJO.getPlan_exit_date().add(Libs.nn(o[30]));
                memberPOJO.getPlan_exit_date().add(Libs.nn(o[31]));
                memberPOJO.getPlan_exit_date().add(Libs.nn(o[32]));

                int ageDays = Libs.getDiffDays(new SimpleDateFormat("yyyy-MM-dd").parse(memberPOJO.getDob()), new Date());
                int matureDays = Libs.getDiffDays(new Date(), new SimpleDateFormat("yyyy-MM-dd").parse(memberPOJO.getMature_date()));

                Listcell lcStatus = new Listcell();
                Label lStatus = new Label();
                if (matureDays>0 && !Libs.nn(o[40]).equals("M") && !Libs.nn(o[40]).equals("U")) {
                    lStatus.setValue("ACTIVE");
                    lStatus.setStyle("color:#00FF00");
                } else {
                	if(Libs.nn(o[40]).equals("M")){
                		lStatus.setValue("MATURE");
                        lStatus.setStyle("color:#FF0000;");
                	}
                	else {
                		lStatus.setValue("INACTIVE");
                        lStatus.setStyle("color:#000000");
                	}
                    
                }
                if (Libs.nn(o[40]).equals("U")) {
                    String effectiveDate = Libs.nn(o[41]) + "-" + Libs.nn(o[42]) + "-" + Libs.nn(o[43]);
                    int effectiveDays = Libs.getDiffDays(new Date(), new SimpleDateFormat("yyyy-MM-dd").parse(effectiveDate));
                    if (effectiveDays<0) {
                        lStatus.setValue("INACTIVE");
                        lStatus.setStyle("color:#000000");
                    }
                }

                

                lcStatus.appendChild(lStatus);

                Listitem li = new Listitem();
                li.setValue(memberPOJO);
                
                li.appendChild(new Listcell(startNumber+""));

                li.appendChild(lcStatus);
                if (Libs.getInsuranceId().equals("00078") || Libs.getInsuranceId().equals("00088")) {
                    li.appendChild(new Listcell(Libs.nn(o[44]).trim()));
                } else {
                    li.appendChild(new Listcell(""));
                }
                
                Listcell cell = new Listcell();
                A memberName = new A(memberPOJO.getName());
                memberName.setStyle("color:#00bbee;text-decoration:none");
                cell.appendChild(memberName);
                li.appendChild(cell);
                li.appendChild(new Listcell(Libs.nn(o[34]).trim()));
                li.appendChild(new Listcell(Libs.nn(o[44]).trim()));
                li.appendChild(new Listcell(Libs.nn(o[20]).trim()));
                li.appendChild(new Listcell(memberPOJO.getCard_number()));
                li.appendChild(new Listcell(memberPOJO.getDob()));
                li.appendChild(Libs.createNumericListcell(ageDays/365, "#"));
                li.appendChild(new Listcell(memberPOJO.getSex()));
                li.appendChild(new Listcell(memberPOJO.getStarting_date()));
                li.appendChild(new Listcell(memberPOJO.getMature_date()));
                
               /* 
                li.appendChild(new Listcell((clientPlanMap.get(memberPOJO.getOp())==null ? memberPOJO.getOp() : clientPlanMap.get(memberPOJO.getOp()))));
                li.appendChild(new Listcell((clientPlanMap.get(memberPOJO.getMaternity())==null ? memberPOJO.getMaternity() : clientPlanMap.get(memberPOJO.getMaternity()))));
                li.appendChild(new Listcell((clientPlanMap.get(memberPOJO.getDental())==null ? memberPOJO.getDental() : clientPlanMap.get(memberPOJO.getDental()))));
                li.appendChild(new Listcell((clientPlanMap.get(memberPOJO.getGlasses())==null ? memberPOJO.getGlasses() : clientPlanMap.get(memberPOJO.getGlasses())))); */
                
                /*li.appendChild(new Listcell(""));
                li.appendChild(new Listcell(""));
                li.appendChild(new Listcell(""));
                li.appendChild(new Listcell(""));
                li.appendChild(new Listcell(""));*/
                
                
                
                if(!memberPOJO.getIp().equals("")){
                	planUsage = 0.0;
                	
                	isFamilyLimit = Libs.isFamilyLimit(policyPOJO, "I");
                	BenefitPOJO benefitPOJO = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), memberPOJO.getIp(), memberPOJO.getIdx(), memberPOJO.getSeq(), "I");
                	if(benefitPOJO.getLimit() > 0){
                		li.appendChild(new Listcell((clientPlanMap.get(memberPOJO.getIp())==null ? memberPOJO.getIp() : clientPlanMap.get(memberPOJO.getIp()))));
                		
                		if(!isInternal)li.appendChild(new Listcell(""));
            			else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit() ,"#,###.##"));
                		
                		/*if(benefitPOJO.isLimitGaji()){
                			if(!isInternal)li.appendChild(new Listcell(""));
                			else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit() ,"#,###.##"));
                		}
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit() ,"#,###.##")); */
                		
                		if(isFamilyLimit){
                			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "I");
                		}else{
                			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getIp(),"I");
                			Integer counter = new Integer(obj[0].toString());
                			if(obj != null && counter > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
                		}
                		
                		li.appendChild(Libs.createNumericListcell(planUsage ,"#,###.##"));
                		
                		if(!isInternal) li.appendChild(new Listcell(""));
            			else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit()-planUsage ,"#,###.##"));
                		
                		/*
                		if(benefitPOJO.isLimitGaji()){
                			if(!isInternal) li.appendChild(new Listcell(""));
                			else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit()-planUsage ,"#,###.##"));
                		}
                		else {
                			if(!isInternal) li.appendChild(new Listcell(""));
                			li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit()-planUsage ,"#,###.##"));
                		} */
                		
                	}
                	else{
                		//check maybe reimbursement only
                		String ipReimbursementOnly = Libs.isReimbursementOnly(policyPOJO.getPolicy_number(), "I", memberPOJO.getIp());
                		BenefitPOJO benefitReim = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), ipReimbursementOnly, memberPOJO.getIdx(), memberPOJO.getSeq(), "I");
                		li.appendChild(new Listcell(ipReimbursementOnly +"- (R)"));
                		
            			if(!isInternal)li.appendChild(new Listcell(""));
            			else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit() ,"#,###.##"));

                		
                		/*if(benefitReim.isLimitGaji()){
                			if(!isInternal)li.appendChild(new Listcell(""));
                			else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit() ,"#,###.##"));
                		}
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit() ,"#,###.##"));*/
                		
                		if(isFamilyLimit){
                			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "I");
                		}else{
                			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getIp(),"I");
                			Integer counter = new Integer(obj[0].toString());
                			if(obj != null && counter > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
                		}
                		
                		li.appendChild(Libs.createNumericListcell(planUsage ,"#,###.##"));
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit()-planUsage ,"#,###.##"));
                		
                		/*if(benefitReim.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit()-planUsage ,"#,###.##"));*/
                	}
                }else{
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                }

                
                if(!memberPOJO.getOp().equals("")){
                	planUsage = 0.0;
                	isFamilyLimit = Libs.isFamilyLimit(policyPOJO, "O");
                	BenefitPOJO benefitPOJO = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), memberPOJO.getOp(), memberPOJO.getIdx(), memberPOJO.getSeq(), "O");
                	if(benefitPOJO.getLimit() > 0){
                		li.appendChild(new Listcell((clientPlanMap.get(memberPOJO.getOp())==null ? memberPOJO.getOp() : clientPlanMap.get(memberPOJO.getOp()))));
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit() ,"#,###.##"));
                		
                		/*if(benefitPOJO.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit() ,"#,###.##")); */
                		
                		if(isFamilyLimit){
                			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "O");
                		}else{
                			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getIp(),"O");
                			Integer counter = new Integer(obj[0].toString());
                			if(obj != null && counter > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
                		}
                		
                		li.appendChild(Libs.createNumericListcell(planUsage ,"#,###.##"));
                		
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit()-planUsage ,"#,###.##"));
                		/*
                		if(benefitPOJO.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit()-planUsage ,"#,###.##")); */
                		
                	}
                	else{
                		//check maybe reimbursement only
                		String ipReimbursementOnly = Libs.isReimbursementOnly(policyPOJO.getPolicy_number(), "O", memberPOJO.getOp());
                		BenefitPOJO benefitReim = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), ipReimbursementOnly, memberPOJO.getIdx(), memberPOJO.getSeq(), "O");
                		li.appendChild(new Listcell(ipReimbursementOnly +"- (R)"));
                		
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit() ,"#,###.##")); 
                		
                		/*
                		if(benefitReim.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit() ,"#,###.##")); */
                		
                		if(isFamilyLimit){
                			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "O");
                		}else{
                			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getOp(),"O");
                			Integer counter = new Integer(obj[0].toString());
                			if(obj != null && counter > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
                		}
                		
                		li.appendChild(Libs.createNumericListcell(planUsage ,"#,###.##"));
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit()-planUsage ,"#,###.##"));
                		/*
                		if(benefitReim.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit()-planUsage ,"#,###.##")); */
                	}
                }else{
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                }

                
                if(!memberPOJO.getMaternity().equals("")){
                	planUsage = 0.0;
                	isFamilyLimit = Libs.isFamilyLimit(policyPOJO, "R");
                	BenefitPOJO benefitPOJO = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), memberPOJO.getMaternity(), memberPOJO.getIdx(), memberPOJO.getSeq(), "R");
                	if(benefitPOJO.getLimit() > 0){
                		li.appendChild(new Listcell((clientPlanMap.get(memberPOJO.getMaternity())==null ? memberPOJO.getMaternity() : clientPlanMap.get(memberPOJO.getMaternity()))));
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit() ,"#,###.##"));
                		/*
                		if(benefitPOJO.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit() ,"#,###.##"));*/
                		
                		if(isFamilyLimit){
                			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "R");
                		}else{
                			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getMaternity(),"R");
                			Integer counter = new Integer(obj[0].toString());
                			if(obj != null && counter > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
                		}
                		
                		li.appendChild(Libs.createNumericListcell(planUsage ,"#,###.##"));
                		
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit()-planUsage ,"#,###.##")); 
                		
                		/*
                		if(benefitPOJO.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit()-planUsage ,"#,###.##")); */
                		
                	}
                	else{
                		//check maybe reimbursement only
                		String ipReimbursementOnly = Libs.isReimbursementOnly(policyPOJO.getPolicy_number(), "R", memberPOJO.getMaternity());
                		BenefitPOJO benefitReim = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), ipReimbursementOnly, memberPOJO.getIdx(), memberPOJO.getSeq(), "R");
                		li.appendChild(new Listcell(ipReimbursementOnly +" - (R)"));
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit() ,"#,###.##"));
                		/*
                		if(benefitReim.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit() ,"#,###.##"));*/
                		
                		if(isFamilyLimit){
                			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "R");
                		}else{
                			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getMaternity(),"R");
                			Integer counter = new Integer(obj[0].toString());
                			if(obj != null && counter > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
                		}
                		
                		li.appendChild(Libs.createNumericListcell(planUsage ,"#,###.##"));
                		
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit()-planUsage ,"#,###.##"));
                		/*
                		if(benefitReim.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit()-planUsage ,"#,###.##"));*/
                	}
                }else{
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                }

                
                if(!memberPOJO.getDental().equals("")){
                	planUsage = 0.0;
                	isFamilyLimit = Libs.isFamilyLimit(policyPOJO, "D");
                	BenefitPOJO benefitPOJO = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), memberPOJO.getDental(), memberPOJO.getIdx(), memberPOJO.getSeq(), "D");
                	if(benefitPOJO.getLimit() > 0){
                		li.appendChild(new Listcell((clientPlanMap.get(memberPOJO.getDental())==null ? memberPOJO.getDental() : clientPlanMap.get(memberPOJO.getDental()))));
                		
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit() ,"#,###.##"));
                		/*
                		if(benefitPOJO.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit() ,"#,###.##"));*/
                		
                		if(isFamilyLimit){
                			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "D");
                		}else{
                			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getDental(),"D");
                			Integer counter = new Integer(obj[0].toString());
                			if(obj != null && counter > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
                		}
                		
                		li.appendChild(Libs.createNumericListcell(planUsage ,"#,###.##"));
                		
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit()-planUsage ,"#,###.##"));
                		/*
                		if(benefitPOJO.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit()-planUsage ,"#,###.##"));*/
                		
                	}
                	else{
                		//check maybe reimbursement only
                		String ipReimbursementOnly = Libs.isReimbursementOnly(policyPOJO.getPolicy_number(), "D", memberPOJO.getDental());
                		BenefitPOJO benefitReim = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), ipReimbursementOnly, memberPOJO.getIdx(), memberPOJO.getSeq(), "D");
                		li.appendChild(new Listcell(ipReimbursementOnly +"- (R)"));
                		
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit() ,"#,###.##"));
                		
                		/*if(benefitReim.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit() ,"#,###.##"));*/
                		
                		if(isFamilyLimit){
                			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "D");
                		}else{
                			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getDental(),"D");
                			Integer counter = new Integer(obj[0].toString());
                			if(obj != null && counter > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
                		}
                		
                		li.appendChild(Libs.createNumericListcell(planUsage ,"#,###.##"));
                		
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit()-planUsage ,"#,###.##"));
                		/*
                		if(benefitReim.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit()-planUsage ,"#,###.##"));*/
                	}
                }else{
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                }

                
                if(!memberPOJO.getGlasses().equals("")){
                	planUsage = 0.0;
                	isFamilyLimit = Libs.isFamilyLimit(policyPOJO, "G");
                	BenefitPOJO benefitPOJO = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), memberPOJO.getGlasses(), memberPOJO.getIdx(), memberPOJO.getSeq(), "G");
                	if(benefitPOJO.getLimit() > 0){
                		li.appendChild(new Listcell((clientPlanMap.get(memberPOJO.getGlasses())==null ? memberPOJO.getGlasses() : clientPlanMap.get(memberPOJO.getGlasses()))));
                		
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit() ,"#,###.##"));
                		/*if(benefitPOJO.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit() ,"#,###.##"));*/
                		
                		if(isFamilyLimit){
                			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "G");
                		}else{
                			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getGlasses(),"G");
                			Integer counter = new Integer(obj[0].toString());
                			if(obj != null && counter > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
                		}
                		
                		li.appendChild(Libs.createNumericListcell(planUsage ,"#,###.##"));
                		
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit()-planUsage ,"#,###.##"));
                		
                		/*if(benefitPOJO.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitPOJO.getLimit()-planUsage ,"#,###.##"));*/
                		
                	}
                	else{
                		//check maybe reimbursement only
                		String ipReimbursementOnly = Libs.isReimbursementOnly(policyPOJO.getPolicy_number(), "G", memberPOJO.getGlasses());
                		BenefitPOJO benefitReim = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), ipReimbursementOnly, memberPOJO.getIdx(), memberPOJO.getSeq(), "G");
                		li.appendChild(new Listcell(ipReimbursementOnly +"- (R)"));
                		
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit() ,"#,###.##"));
                		/*if(benefitReim.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit() ,"#,###.##"));*/
                		
                		if(isFamilyLimit){
                			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "G");
                		}else{
                			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getGlasses(),"G");
                			Integer counter = new Integer(obj[0].toString());
                			if(obj != null && counter > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
                		}
                		
                		li.appendChild(Libs.createNumericListcell(planUsage ,"#,###.##"));
                		if(!isInternal)li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit()-planUsage ,"#,###.##"));
                		
                		/*if(benefitReim.isLimitGaji())li.appendChild(new Listcell(""));
                		else li.appendChild(Libs.createNumericListcell(benefitReim.getLimit()-planUsage ,"#,###.##"));*/
                	}
                }else{
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                	li.appendChild(new Listcell("-"));
                }

                
                lb.appendChild(li);
                
                memberName.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						showMemberDetail(memberPOJO);
					}
				});
                
                startNumber = startNumber + 1;
            }
        } catch (Exception ex) {
            log.error("populate", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    public void refresh() {
        where = null;
//        populateCount();
//        populate(0, pg.getPageSize());
        populateMember(0, pg.getPageSize());
    }
    
    public void quickSearchNew(){
    	String val = ((Textbox) getFellow("tQuickSearch")).getText();
    	if (!val.isEmpty() || (val.isEmpty() && cbPolicy.getSelectedIndex()>0)){
    	where = "(MemberCardNo like '%" + val + "%' or "
    		  + "MemberName like '%" + val + "%' or "
    		  + "PolisClient like '%" + val + "%' or "
    		  + "IDNumber like '%" + val + "%')";
    	 populateMember(0, pg.getPageSize());
    	}else refresh();
    }

    public void quickSearch() {
        String val = ((Textbox) getFellow("tQuickSearch")).getText();
        if (!val.isEmpty() || (val.isEmpty() && cbPolicy.getSelectedIndex()>0)) {
            where = "a.hdt1ncard like '%" + val + "%' or "
                    + "a.hdt1name like '%" + val + "%' or "
                    + "c.hempcnpol like '%" + val + "%' or "
                    + "c.hempmemo3 like '%" + val + "%' or "
                    + "c.hempcnid like '%" + val + "%' ";
            populateCountForQuickSearch();
            populate(0, pg.getPageSize());
        } else refresh();
    }

    public void showMemberDetail(MemberPOJO memberPOJO) {
//        MemberPOJO memberPOJO = lb.getSelectedItem().getValue();
        Window w = (Window) Executions.createComponents("views/MemberDetail.zul", Libs.getRootWindow(), null);
        w.setAttribute("policy", memberPOJO.getPolicy());
        w.setAttribute("member", memberPOJO);
        w.doModal();
    }

    public void policySelected() {
//        quickSearch();
    	quickSearchNew();
        
        if (cbPolicy.getSelectedIndex()>0) {
        	lastLbl.setVisible(false);
        	idxLbl.setVisible(false);
        	/*
            String policy = cbPolicy.getSelectedItem().getLabel();
            policy = policy.substring(policy.indexOf("(")+1, policy.indexOf(")"));
            
            String[] polis = policy.split("-");
            
           int lastIndex = getLastIndex(polis);
           
           idxLbl.setValue(lastIndex+"");*/
        }else{
        	lastLbl.setVisible(false);
        	idxLbl.setVisible(false);
        }
    }

    private int getLastIndex(String[] polis) {
    	Session s = Libs.sfDB.openSession();
    	
    	int result = 0;
    	
    	try{
    		String query = "select MAX(hdt1idxno) from idnhltpf.dbo.hltdt1 where HDT1YY='"+polis[0]+"' and HDT1BR='"+polis[1]+"' and HDT1DIST='"+polis[2]+"' and HDT1PONO='"+polis[3]+"' and hdt1idxno < 90000";
    		BigDecimal hasil = (BigDecimal)s.createSQLQuery(query).uniqueResult();
    		
    		result = hasil.intValue();
    	}catch(Exception e){
    		log.error("getLastIndex", e);
    	}finally{
    		 if (s!=null && s.isOpen()) s.close();
    	}
    	
		return result;
	}

	public void export() {
		if (Messagebox.show("Retrieving this data would take up to more than 1 minutes, continue?", "Confirmation", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION)==Messagebox.OK){
		Session s = Libs.sfDB.openSession();
		String[] titles = new String[]{"Status", "Name", "Policy Number", "ID Number", "NIK", "Card Number", "DOB", "Age", "Sex","Starting Date", "Mature Date", 
									  "IP", "IP Limit", "IP Usage", "IP Balance", "OP", "OP Limit", "OP Usage", "OP Balance","Maternity", "MT Limit", 
									  "MT Usage", "MT Balance", "Dental", "DT Limit", "DT Usage", "DT Balance", "Glasses", "GL Limit", "GL Usage", "GL Balance"}; //AD
		
		String judul = "Member List From "+cbPolicy.getSelectedItem().getLabel() + " Product With "+cbStatus.getSelectedItem().getLabel() + " Status";
        
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
        sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(counter+1)+":AE$"+(counter+1)+""));
        titleCell.setCellValue(judul);
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
        	 String select = "select "
                     + "a.hdt1ncard, a.hdt1name, "
                     + "a.hdt1bdtyy, a.hdt1bdtmm, a.hdt1bdtdd, "
                     + "a.hdt1sex, "
                     + "b.hdt2plan1, b.hdt2plan2, b.hdt2plan3, b.hdt2plan4, b.hdt2plan5, b.hdt2plan6, "
                     + "b.hdt2sdtyy, b.hdt2sdtmm, b.hdt2sdtdd, "
                     + "b.hdt2mdtyy, b.hdt2mdtmm, b.hdt2mdtdd, "
                     + "a.hdt1idxno, a.hdt1seqno, "
                     + "c.hempcnid, " //20
                     + "(convert(varchar,b.hdt2pedty1)+'-'+convert(varchar,b.hdt2pedtm1)+'-'+convert(varchar,b.hdt2pedtd1)) as hdt2pedt1,"
                     + "(convert(varchar,b.hdt2pedty2)+'-'+convert(varchar,b.hdt2pedtm2)+'-'+convert(varchar,b.hdt2pedtd2)) as hdt2pedt2,"
                     + "(convert(varchar,b.hdt2pedty3)+'-'+convert(varchar,b.hdt2pedtm3)+'-'+convert(varchar,b.hdt2pedtd3)) as hdt2pedt3,"
                     + "(convert(varchar,b.hdt2pedty4)+'-'+convert(varchar,b.hdt2pedtm4)+'-'+convert(varchar,b.hdt2pedtd4)) as hdt2pedt4,"
                     + "(convert(varchar,b.hdt2pedty5)+'-'+convert(varchar,b.hdt2pedtm5)+'-'+convert(varchar,b.hdt2pedtd5)) as hdt2pedt5,"
                     + "(convert(varchar,b.hdt2pedty6)+'-'+convert(varchar,b.hdt2pedtm6)+'-'+convert(varchar,b.hdt2pedtd6)) as hdt2pedt6,"
                     + "(convert(varchar,b.hdt2pxdty1)+'-'+convert(varchar,b.hdt2pxdtm1)+'-'+convert(varchar,b.hdt2pxdtd1)) as hdt2pxdt1,"
                     + "(convert(varchar,b.hdt2pxdty2)+'-'+convert(varchar,b.hdt2pxdtm2)+'-'+convert(varchar,b.hdt2pxdtd2)) as hdt2pxdt2,"
                     + "(convert(varchar,b.hdt2pxdty3)+'-'+convert(varchar,b.hdt2pxdtm3)+'-'+convert(varchar,b.hdt2pxdtd3)) as hdt2pxdt3,"
                     + "(convert(varchar,b.hdt2pxdty4)+'-'+convert(varchar,b.hdt2pxdtm4)+'-'+convert(varchar,b.hdt2pxdtd4)) as hdt2pxdt4,"
                     + "(convert(varchar,b.hdt2pxdty5)+'-'+convert(varchar,b.hdt2pxdtm5)+'-'+convert(varchar,b.hdt2pxdtd5)) as hdt2pxdt5,"
                     + "(convert(varchar,b.hdt2pxdty6)+'-'+convert(varchar,b.hdt2pxdtm6)+'-'+convert(varchar,b.hdt2pxdtd6)) as hdt2pxdt6, "
                     + "a.hdt1mstat, c.hempcnpol, " //33
                     + "a.hdt1yy, a.hdt1br, a.hdt1dist, a.hdt1pono, "
                     + "d.hhdrname, "
                     + "b.hdt2moe, " //40
                     + "b.hdt2xdtyy, b.hdt2xdtmm, b.hdt2xdtdd, "
                     + "c.hempmemo3 ";


             String qry = "from idnhltpf.dbo.hlthdr d  "
             		+" inner join idnhltpf.dbo.hltdt1 a on d.hhdryy=a.hdt1yy and d.hhdrbr=a.hdt1br and d.hhdrdist=a.hdt1dist and d.hhdrpono=a.hdt1pono " 
                     + "inner join idnhltpf.dbo.hltdt2 b on a.hdt1yy=b.hdt2yy and a.hdt1br=b.hdt2br and a.hdt1dist=b.hdt2dist and a.hdt1pono=b.hdt2pono and a.hdt1idxno=b.hdt2idxno and a.hdt1seqno=b.hdt2seqno and a.hdt1ctr=b.hdt2ctr "
                     + "inner join idnhltpf.dbo.hltemp c on a.hdt1yy=c.hempyy and a.HDT1BR=c.HEMPBR and a.HDT1DIST=c.HEMPDIST and a.hdt1pono=c.hemppono and a.hdt1idxno=c.hempidxno and a.hdt1seqno=c.hempseqno and a.hdt1ctr=c.hempctr "
                     + "where "
                     + "d.hhdrinsid";
             		if(products.size() > 0) qry = qry + " in  ("+insid+")";
             		else qry = qry + "='" + Libs.getInsuranceId() + "' "; 
             		
             		if(polisList.size() > 0){
             			qry = qry + "and convert(varchar,d.hhdryy)+'-'+convert(varchar,d.hhdrbr)+'-'+convert(varchar,d.hhdrdist)+'-'+convert(varchar,d.hhdrpono) "
             					  + "in ("+polis+") ";
             		} 
             		
             		if (cbPolicy.getSelectedIndex()>0) {
                         String policy = cbPolicy.getSelectedItem().getLabel();
                         policy = policy.substring(policy.indexOf("(")+1, policy.indexOf(")"));
                         qry += "and (convert(varchar,d.hhdryy)+'-'+convert(varchar,d.hhdrbr)+'-'+convert(varchar,d.hhdrdist)+'-'+convert(varchar,d.hhdrpono)='" + policy + "') ";
                     }
             		
             		qry = qry + "and a.hdt1ctr=0 and a.hdt1idxno < 99989 and a.hdt1pono <> 99999 and a.HDT1NAME not like '%DUMMY%' ";

             if (!Libs.nn(userProductViewrestriction).isEmpty()) qry += "and d.hhdrpono in (" + userProductViewrestriction + ") ";

             if (where!=null) qry += "and (" + where + ") ";

             
             
             if(cbStatus.getSelectedIndex() == 0){
             	qry += " and b.hdt2moe not in('M','U') and convert(datetime, convert(varchar,HDT2MDTMM)+'-'+convert(varchar,HDT2MDTDD)+'-'+convert(varchar,HDT2MDTYY), 110) > GETDATE() ";
             }else if(cbStatus.getSelectedIndex() == 1){
             	qry += " and b.hdt2moe = 'U' ";
             }else qry += " and b.hdt2moe = 'M' ";
             

             String order = "order by a.hdt1name asc ";
             
             List<Object[]> l = s.createSQLQuery(select+qry+order).list();
             for(Object[] o : l){
            	 createDetail(sheet, o, counter, styles);
            	 counter = counter + 1;
             }
             
             for(int i=0; i < titles.length; i++){
         		sheet.autoSizeColumn(i);
         	 }
				
				
			String fn = "Member-"+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";

	        FileOutputStream out = new FileOutputStream(Libs.config.get("temp_dir").toString() + File.separator + fn);
	        wb.write(out);
	        out.close();

	        Thread.sleep(5000);

	        File f = new File(Libs.config.get("temp_dir").toString() + File.separator + fn);
	        InputStream is = new FileInputStream(f);
	        Filedownload.save(is, "application/vnd.ms-excel", fn);
	        f.delete();
        }catch(Exception e){
        	 log.error("export", e);
        }finally{
        	 if (s!=null && s.isOpen()) s.close();
        }
	}
//        Libs.showDeveloping();
    }

	private void createDetail(Sheet sheet, Object[] o, int counter, Map<String, CellStyle> styles) {
		try{
			
		
		
		PolicyPOJO policyPOJO = new PolicyPOJO();
        policyPOJO.setYear(Integer.valueOf(Libs.nn(o[35])));
        policyPOJO.setBr(Integer.valueOf(Libs.nn(o[36])));
        policyPOJO.setDist(Integer.valueOf(Libs.nn(o[37])));
        policyPOJO.setPolicy_number(Integer.valueOf(Libs.nn(o[38])));
        policyPOJO.setName(Libs.nn(o[39]).trim());

        Map<String,String> clientPlanMap = Libs.getClientPlanMap(policyPOJO.getPolicy_string());

        final MemberPOJO memberPOJO = new MemberPOJO();
        memberPOJO.setPolicy(policyPOJO);
        memberPOJO.setName(Libs.nn(o[1]).trim());
        memberPOJO.setCard_number(Libs.nn(o[0]).trim());
        memberPOJO.setDob(Libs.nn(o[2]) + "-" + Libs.nn(o[3]) + "-" + Libs.nn(o[4]));
        memberPOJO.setStarting_date(Libs.nn(o[12]) + "-" + Libs.nn(o[13]) + "-" + Libs.nn(o[14]));
        memberPOJO.setMature_date(Libs.nn(o[15]) + "-" + Libs.nn(o[16]) + "-" + Libs.nn(o[17]));
        memberPOJO.setSex(Libs.nn(o[5]));
        memberPOJO.setIp(Libs.nn(o[6]).trim());
        memberPOJO.setOp(Libs.nn(o[7]).trim());
        memberPOJO.setMaternity(Libs.nn(o[8]).trim());
        memberPOJO.setDental(Libs.nn(o[9]).trim());
        memberPOJO.setGlasses(Libs.nn(o[10]).trim());
        memberPOJO.setMarital_status(Libs.nn(o[33]));
        memberPOJO.setIdx(Libs.nn(o[18]));
        memberPOJO.setSeq(Libs.nn(o[19]));
        memberPOJO.setClient_policy_number(Libs.nn(o[34]).trim());
        memberPOJO.setClient_id_number(Libs.nn(o[20]).trim());

        memberPOJO.getPlan_entry_date().add(Libs.nn(o[21]));
        memberPOJO.getPlan_entry_date().add(Libs.nn(o[22]));
        memberPOJO.getPlan_entry_date().add(Libs.nn(o[23]));
        memberPOJO.getPlan_entry_date().add(Libs.nn(o[24]));
        memberPOJO.getPlan_entry_date().add(Libs.nn(o[25]));
        memberPOJO.getPlan_entry_date().add(Libs.nn(o[26]));
        memberPOJO.getPlan_exit_date().add(Libs.nn(o[27]));
        memberPOJO.getPlan_exit_date().add(Libs.nn(o[28]));
        memberPOJO.getPlan_exit_date().add(Libs.nn(o[29]));
        memberPOJO.getPlan_exit_date().add(Libs.nn(o[30]));
        memberPOJO.getPlan_exit_date().add(Libs.nn(o[31]));
        memberPOJO.getPlan_exit_date().add(Libs.nn(o[32]));

        int ageDays = Libs.getDiffDays(new SimpleDateFormat("yyyy-MM-dd").parse(memberPOJO.getDob()), new Date());
        int matureDays = Libs.getDiffDays(new Date(), new SimpleDateFormat("yyyy-MM-dd").parse(memberPOJO.getMature_date()));
		
		org.apache.poi.ss.usermodel.Cell mycell;
        org.apache.poi.ss.usermodel.Row row;
        
        String status = "";
        
        if (matureDays>0 && !Libs.nn(o[40]).equals("M") && !Libs.nn(o[40]).equals("U")) {
            status = "ACTIVE";
        } 
        else 
        {
        	if(Libs.nn(o[40]).equals("M")){
        		status= "MATURE";
        	}
        	else {
        		status = "INACTIVE";
        	}
            
        }
         
        row = sheet.createRow(counter);
        mycell = row.createCell(0); //status
        mycell.setCellValue(status);
        mycell.setCellStyle(styles.get("cell"));
        
        mycell = row.createCell(1);//member name
        mycell.setCellValue(memberPOJO.getName());
        mycell.setCellStyle(styles.get("cell"));
        
        mycell = row.createCell(2); //client policy number
        mycell.setCellValue(Libs.nn(o[34]).trim());
        mycell.setCellStyle(styles.get("cell"));
        
        mycell = row.createCell(3); //client id number
        mycell.setCellValue(Libs.nn(o[20]).trim());
        mycell.setCellStyle(styles.get("cell"));
        
        mycell = row.createCell(4); //client id number
        mycell.setCellValue(Libs.nn(o[44]).trim());
        mycell.setCellStyle(styles.get("cell"));
        
        mycell = row.createCell(5); //card number
        mycell.setCellValue(memberPOJO.getCard_number());
        mycell.setCellStyle(styles.get("cell"));
        
        mycell = row.createCell(6); //dob
        mycell.setCellValue(memberPOJO.getDob());
        mycell.setCellStyle(styles.get("cell"));
        
        mycell = row.createCell(7); //age
        mycell.setCellValue(ageDays/365);
        mycell.setCellStyle(styles.get("cell_angka"));
        
        mycell = row.createCell(8); //sex
        mycell.setCellValue(memberPOJO.getSex());
        mycell.setCellStyle(styles.get("cell"));
        
        mycell = row.createCell(9); //starting date
        mycell.setCellValue(memberPOJO.getStarting_date());
        mycell.setCellStyle(styles.get("cell"));
        
        mycell = row.createCell(10); //mature date
        mycell.setCellValue(memberPOJO.getMature_date());
        mycell.setCellStyle(styles.get("cell"));
        
        String ipPlan="";
        Double ipLimit=null;
        Double ipUsage=null;
        Double ipBalance=null;
        
        
        if(!memberPOJO.getIp().equals("")){
        	ipPlan = memberPOJO.getIp();
        	double planUsage = 0.0;
        	boolean	isFamilyLimit = Libs.isFamilyLimit(policyPOJO, "I");
        	BenefitPOJO benefitPOJO = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), memberPOJO.getIp(), memberPOJO.getIdx(), memberPOJO.getSeq(), "I");
        	if(benefitPOJO.getLimit() > 0){
        		if(!benefitPOJO.isLimitGaji())
        			ipLimit = benefitPOJO.getLimit();
        		
        		if(isFamilyLimit){
        			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "I");
        		}else{
        			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getIp(),"I");
        			Integer counterClaim = new Integer(obj[0].toString());
        			if(obj != null && counterClaim > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
        		}
        		ipUsage = planUsage;
        		if(!benefitPOJO.isLimitGaji()) ipBalance = benefitPOJO.getLimit()-planUsage;
        	}
        	else{
        		//check maybe reimbursement only
        		String ipReimbursementOnly = Libs.isReimbursementOnly(policyPOJO.getPolicy_number(), "I", memberPOJO.getIp());
        		if(ipReimbursementOnly != null){
        			BenefitPOJO benefitReim = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), ipReimbursementOnly, memberPOJO.getIdx(), memberPOJO.getSeq(), "I");
        			ipPlan = ipReimbursementOnly + "- (R)";
        			if(!benefitReim.isLimitGaji())ipLimit = benefitReim.getLimit();
        			
        			if(isFamilyLimit){
            			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "I");
            		}else{
            			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), ipReimbursementOnly,"I");
            			Integer counterClaim = new Integer(obj[0].toString());
            			if(obj != null && counterClaim > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
            		}
            		
        			ipUsage = planUsage;
            		if(!benefitReim.isLimitGaji())ipBalance = benefitReim.getLimit()-planUsage;
            		
        		}
        	}
        }
        
        
        mycell = row.createCell(11); //ip plan
        mycell.setCellValue(ipPlan);
        mycell.setCellStyle(styles.get("cell"));
        
        mycell = row.createCell(12); //ip limit
        if(ipLimit != null) mycell.setCellValue(ipLimit);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
        
        mycell = row.createCell(13); //ip usage
        if(ipUsage != null) mycell.setCellValue(ipUsage);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
        
        mycell = row.createCell(14); //ip balance
        if(ipBalance != null) mycell.setCellValue(ipBalance);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
        
        
        String opPlan="";
        Double opLimit=null;
        Double opUsage=null;
        Double opBalance=null;
        
        
        if(!memberPOJO.getOp().equals("")){
        	opPlan = memberPOJO.getOp();
        	double planUsage = 0.0;
        	boolean	isFamilyLimit = Libs.isFamilyLimit(policyPOJO, "O");
        	BenefitPOJO benefitPOJO = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), memberPOJO.getOp(), memberPOJO.getIdx(), memberPOJO.getSeq(), "O");
        	if(benefitPOJO.getLimit() > 0){
        		if(!benefitPOJO.isLimitGaji())
        			opLimit = benefitPOJO.getLimit();
        		
        		if(isFamilyLimit){
        			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "O");
        		}else{
        			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getOp(),"O");
        			Integer counterClaim = new Integer(obj[0].toString());
        			if(obj != null && counterClaim > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
        		}
        		opUsage = planUsage;
        		if(!benefitPOJO.isLimitGaji()) opBalance = benefitPOJO.getLimit()-planUsage;
        	}
        	else{
        		//check maybe reimbursement only
        		String ipReimbursementOnly = Libs.isReimbursementOnly(policyPOJO.getPolicy_number(), "O", memberPOJO.getOp());
        		if(ipReimbursementOnly != null){
        			BenefitPOJO benefitReim = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), ipReimbursementOnly, memberPOJO.getIdx(), memberPOJO.getSeq(), "O");
        			opPlan = ipReimbursementOnly + "- (R)";
        			if(!benefitReim.isLimitGaji())opLimit = benefitReim.getLimit();
        			
        			if(isFamilyLimit){
            			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "O");
            		}else{
            			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), ipReimbursementOnly,"O");
            			Integer counterClaim = new Integer(obj[0].toString());
            			if(obj != null && counterClaim > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
            		}
            		
        			opUsage = planUsage;
            		if(!benefitReim.isLimitGaji())opBalance = benefitReim.getLimit()-planUsage;
            		
        		}
        	}
        }
        
        
        mycell = row.createCell(15); //op plan
        mycell.setCellValue(opPlan);
        mycell.setCellStyle(styles.get("cell"));
        
        mycell = row.createCell(16); //op limit
        if(opLimit != null) mycell.setCellValue(opLimit);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
        
        mycell = row.createCell(17); //op usage
        if(opUsage != null)mycell.setCellValue(opUsage);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
        
        mycell = row.createCell(18); //op balance
        if(opBalance != null)mycell.setCellValue(opBalance);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
        
        String maternity="";
        Double mtLimit=null;
        Double mtUsage=null;
        Double mtBalance=null;
        
        
        if(!memberPOJO.getMaternity().equals("")){
        	maternity = memberPOJO.getMaternity();
        	double planUsage = 0.0;
        	boolean	isFamilyLimit = Libs.isFamilyLimit(policyPOJO, "R");
        	BenefitPOJO benefitPOJO = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), memberPOJO.getMaternity(), memberPOJO.getIdx(), memberPOJO.getSeq(), "R");
        	if(benefitPOJO.getLimit() > 0){
        		if(!benefitPOJO.isLimitGaji())
        			mtLimit = benefitPOJO.getLimit();
        		
        		if(isFamilyLimit){
        			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "R");
        		}else{
        			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getMaternity(),"R");
        			Integer counterClaim = new Integer(obj[0].toString());
        			if(obj != null && counterClaim > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
        		}
        		mtUsage = planUsage;
        		if(!benefitPOJO.isLimitGaji()) mtBalance = benefitPOJO.getLimit()-planUsage;
        	}
        	else{
        		//check maybe reimbursement only
        		String ipReimbursementOnly = Libs.isReimbursementOnly(policyPOJO.getPolicy_number(), "R", memberPOJO.getMaternity());
        		if(ipReimbursementOnly != null){
        			BenefitPOJO benefitReim = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), ipReimbursementOnly, memberPOJO.getIdx(), memberPOJO.getSeq(), "R");
        			maternity = ipReimbursementOnly + "- (R)";
        			if(!benefitReim.isLimitGaji())mtLimit = benefitReim.getLimit();
        			
        			if(isFamilyLimit){
            			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "R");
            		}else{
            			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), ipReimbursementOnly,"R");
            			Integer counterClaim = new Integer(obj[0].toString());
            			if(obj != null && counterClaim > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
            		}
            		
        			mtUsage = planUsage;
            		if(!benefitReim.isLimitGaji())mtBalance = benefitReim.getLimit()-planUsage;
            		
        		}
        	}
        }
        
        
        mycell = row.createCell(19); //maternity plan
        mycell.setCellValue(maternity);
        mycell.setCellStyle(styles.get("cell"));
        
        mycell = row.createCell(20); //mt limit
        if(mtLimit != null)mycell.setCellValue(mtLimit);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
        
        mycell = row.createCell(21); //mt usage
        if(mtUsage != null)mycell.setCellValue(mtUsage);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
        
        mycell = row.createCell(22); //mt balance
        if(mtBalance != null)mycell.setCellValue(mtBalance);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
        
        
        String dental="";
        Double dtLimit=null;
        Double dtUsage=null;
        Double dtBalance=null;
        
        
        if(!memberPOJO.getDental().equals("")){
        	dental = memberPOJO.getDental();
        	double planUsage = 0.0;
        	boolean	isFamilyLimit = Libs.isFamilyLimit(policyPOJO, "D");
        	BenefitPOJO benefitPOJO = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), memberPOJO.getDental(), memberPOJO.getIdx(), memberPOJO.getSeq(), "D");
        	if(benefitPOJO.getLimit() > 0){
        		if(!benefitPOJO.isLimitGaji())
        			dtLimit = benefitPOJO.getLimit();
        		
        		if(isFamilyLimit){
        			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "D");
        		}else{
        			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getDental(),"D");
        			Integer counterClaim = new Integer(obj[0].toString());
        			if(obj != null && counterClaim > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
        		}
        		dtUsage = planUsage;
        		if(!benefitPOJO.isLimitGaji()) dtBalance = benefitPOJO.getLimit()-planUsage;
        	}
        	else{
        		//check maybe reimbursement only
        		String ipReimbursementOnly = Libs.isReimbursementOnly(policyPOJO.getPolicy_number(), "D", memberPOJO.getDental());
        		if(ipReimbursementOnly != null){
        			BenefitPOJO benefitReim = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), ipReimbursementOnly, memberPOJO.getIdx(), memberPOJO.getSeq(), "D");
        			dental = ipReimbursementOnly + "- (R)";
        			if(!benefitReim.isLimitGaji())dtLimit = benefitReim.getLimit();
        			
        			if(isFamilyLimit){
            			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "D");
            		}else{
            			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), ipReimbursementOnly,"D");
            			Integer counterClaim = new Integer(obj[0].toString());
            			if(obj != null && counterClaim > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
            		}
            		
        			dtUsage = planUsage;
            		if(!benefitReim.isLimitGaji())dtBalance = benefitReim.getLimit()-planUsage;
            		
        		}
        	}
        }
        
        
        mycell = row.createCell(23); //dt plan
        mycell.setCellValue(dental);
        mycell.setCellStyle(styles.get("cell"));
        
        mycell = row.createCell(24); //dt limit
        if(dtLimit != null) mycell.setCellValue(dtLimit);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
        
        mycell = row.createCell(25); //dt usage
        if(dtUsage != null)mycell.setCellValue(dtUsage);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
        
        mycell = row.createCell(26); //dt balance
        if(dtBalance != null)mycell.setCellValue(dtBalance);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
        
        
        String glPlan="";
        Double glLimit=null;
        Double glUsage=null;
        Double glBalance=null;
        
        
        if(!memberPOJO.getOp().equals("")){
        	glPlan = memberPOJO.getGlasses();
        	double planUsage = 0.0;
        	boolean	isFamilyLimit = Libs.isFamilyLimit(policyPOJO, "G");
        	BenefitPOJO benefitPOJO = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), memberPOJO.getGlasses(), memberPOJO.getIdx(), memberPOJO.getSeq(), "G");
        	if(benefitPOJO.getLimit() > 0){
        		if(!benefitPOJO.isLimitGaji())
        			glLimit = benefitPOJO.getLimit();
        		
        		if(isFamilyLimit){
        			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "G");
        		}else{
        			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), memberPOJO.getGlasses(),"G");
        			Integer counterClaim = new Integer(obj[0].toString());
        			if(obj != null && counterClaim > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
        		}
        		glUsage = planUsage;
        		if(!benefitPOJO.isLimitGaji()) glBalance = benefitPOJO.getLimit()-planUsage;
        	}
        	else{
        		//check maybe reimbursement only
        		String ipReimbursementOnly = Libs.isReimbursementOnly(policyPOJO.getPolicy_number(), "G", memberPOJO.getGlasses());
        		if(ipReimbursementOnly != null){
        			BenefitPOJO benefitReim = Libs.getBenefit(policyPOJO.getYear() + "-" + policyPOJO.getBr() + "-" + policyPOJO.getDist() + "-" + policyPOJO.getPolicy_number(), ipReimbursementOnly, memberPOJO.getIdx(), memberPOJO.getSeq(), "G");
        			glPlan = ipReimbursementOnly + "- (R)";
        			if(!benefitReim.isLimitGaji())glLimit = benefitReim.getLimit();
        			
        			if(isFamilyLimit){
            			planUsage = Libs.getFamilyUsage(policyPOJO, memberPOJO.getIdx(), "G");
            		}else{
            			Object[] obj = Libs.getPlanUsage(policyPOJO, memberPOJO.getIdx(), memberPOJO.getSeq(), ipReimbursementOnly,"G");
            			Integer counterClaim = new Integer(obj[0].toString());
            			if(obj != null && counterClaim > 0)planUsage = ((BigDecimal)obj[1]).doubleValue();
            		}
            		
        			glUsage = planUsage;
            		if(!benefitReim.isLimitGaji())glBalance = benefitReim.getLimit()-planUsage;
            		
        		}
        	}
        }
        
        
        mycell = row.createCell(27); //gl plan
        mycell.setCellValue(glPlan);
        mycell.setCellStyle(styles.get("cell"));
        
        mycell = row.createCell(28); //gl limit
        if(glLimit != null)mycell.setCellValue(glLimit);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
        
        mycell = row.createCell(29); //gl usage
        if(glUsage != null)mycell.setCellValue(glUsage);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
        
        mycell = row.createCell(30); //gl balance
        if(glBalance != null)mycell.setCellValue(glBalance);
        else mycell.setCellValue("");
        mycell.setCellStyle(styles.get("cell_angka"));
      
        
        
		}catch(Exception e){
			log.error("createDetail",e);
		}
        
        
		
	}

}
