package com.imc.ocisv3.controllers;

import com.imc.ocisv3.pojos.MemberPOJO;
import com.imc.ocisv3.pojos.PolicyPOJO;
import com.imc.ocisv3.tools.Libs;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import org.zkoss.zul.event.PagingEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by faizal on 10/24/13.
 */
public class PolicyDetailController extends Window {

    private Logger log = LoggerFactory.getLogger(PolicyDetailController.class);
    private Label lPolicy;
    private PolicyPOJO policy;
    private Listbox lbMembers;
    private Listbox lbPlans;
    private Listbox lbPlanItems;
    private Paging pgMembers;
    private String where;
    private Label lPlanTitle;
    private Map<String,String> clientPlanMap;
    
    private Object[] policyNew;

    public void onCreate() {
        if (!Libs.checkSession()) {
//            policy = (PolicyPOJO) getAttribute("policy");
        	policyNew = (Object[]) getAttribute("policy");
            initComponents();
            populatePlansNew();
            populateMemberNewOcis(0, pgMembers.getPageSize());
//            populatePlans();
//            populateMembers(0, pgMembers.getPageSize());
        }
    }

    private void initComponents() {
        lPolicy = (Label) getFellow("lPolicy");
        lbMembers = (Listbox) getFellow("lbMembers");
        lbPlans = (Listbox) getFellow("lbPlans");
        lbPlanItems = (Listbox) getFellow("lbPlanItems");
        pgMembers = (Paging) getFellow("pgMembers");
        lPlanTitle = (Label)getFellow("lPlanTitle");

        pgMembers.addEventListener("onPaging", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                PagingEvent evt = (PagingEvent) event;
//                populateMembers(evt.getActivePage() * pgMembers.getPageSize(), pgMembers.getPageSize());
                populateMemberNewOcis(evt.getActivePage() * pgMembers.getPageSize(), pgMembers.getPageSize());
            }
        });

//        String policyName = policy.getName();
        String policyName = (String)policyNew[1];
        if (Libs.config.get("demo_mode").equals("true") && Libs.getInsuranceId().equals("00051")) policyName = Libs.nn(Libs.config.get("demo_name"));
//        String subTitle = "[" + policy.getYear() + "-" + policy.getBr() + "-" + policy.getDist() + "-" + policy.getPolicy_number() + "] " + policyName;
        String subTitle = "[" + (String)policyNew[0] + "] " + policyName;
        lPolicy.setValue(subTitle);

        /*
        Listheader lhEmployeeId = (Listheader) getFellow("lhEmployeeId");
        if (Libs.getInsuranceId().equals("00078") || Libs.getInsuranceId().equals("00088")) {
            lhEmployeeId.setVisible(true);
        } else {
            lhEmployeeId.setVisible(false);
        }*/
    }
    
    private void populatePlansNew(){
    	lbPlans.getItems().clear();
    	Session s = Libs.sfOCIS.openSession();
    	try{
    		String qry = "Exec "+Libs.getDbName()+".dbo.S_OCISProductPlanList :polisId";
    		SQLQuery query = s.createSQLQuery(qry);
    		query.setInteger("polisId", (Integer)policyNew[6]);
    		
    		int panjangArray = 0;
    		List<Object[]> l = query.list();
    		if(l.size() > 0){
    			Object[] arrobj = l.get(0);
    			panjangArray = arrobj.length;
    		} 
    		
    		for(int i=1; i < panjangArray; i++){
    			Listheader lhdr = new Listheader();
                lhdr.setWidth("120px");
                lbPlans.getListhead().appendChild(lhdr);
    		}
    		
    		for(Object[] o : l){
    			Listitem item = new Listitem();
    			for(int i=1; i < o.length; i++){
    				final String planCode = (String)o[i];
    				Listcell cell = new Listcell(planCode);
    				item.appendChild(cell);
    				
    				cell.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event arg0) throws Exception {
							lPlanTitle.setValue("Plan Items [" + planCode + "]");
							showPlanDetail(planCode);
							
						}
					});
    			}
    			lbPlans.appendChild(item);
    		}
    		
    	}catch(Exception e){
    		log.error("populatePlansNew", e);
    	}finally{
    		if(s != null && s.isOpen())s.close();
    	}
    }
    
    private void showPlanDetail(String planCode){
    	lbPlanItems.getItems().clear();
    	Session s = Libs.sfOCIS.openSession();
    	try{
    		
    		String sql = "Select * from "+Libs.getDbName()+".dbo.F_OCISProductPlanBenefitList(:plan)";
    		SQLQuery query = s.createSQLQuery(sql);
    		query.setString("plan", planCode);
    		
    		List<Object[]> l = query.list();
    		for(Object[] o : l){
    			Listitem item = new Listitem();
    			item.appendChild(new Listcell(Libs.nn(o[0])));
    			
    			Boolean bool = (Boolean)o[2];
    			if(bool.booleanValue()){
    				item.appendChild(new Listcell(" AS CHARGE"));
    			}else{
    				item.appendChild(Libs.createNumericListcell(((BigDecimal)o[1]).doubleValue(), "#,###.##"));
    			}
    			
    			lbPlanItems.appendChild(item);
    		}
    		
    	}catch(Exception e){
    		log.error("showPlanDetail",e);
    	}finally{
    		if(s != null && s.isOpen())s.close();
    	}
    	
    }

    private void populatePlans() {
        lbPlans.getItems().clear();

        clientPlanMap = Libs.getClientPlanMap(policy.getPolicy_string());

        Map<String,ArrayList<String>> plansMap = new HashMap<String,ArrayList<String>>();
        plansMap.put("inpatient", new ArrayList<String>());
        plansMap.put("outpatient",  new ArrayList<String>());
        plansMap.put("maternity",  new ArrayList<String>());
        plansMap.put("dental",  new ArrayList<String>());
        plansMap.put("glasses",  new ArrayList<String>());

        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + "hbftcode "
                    + "from idnhltpf.dbo.hltbft "
                    + "where "
                    + "hbftyy=" + policy.getYear() + " and hbftpono=" + policy.getPolicy_number() + " "
                    + "order by hbftcode asc ";

            List<String> l = s.createSQLQuery(qry).list();

            for (String plan : l) {
                if (plan.startsWith("C")) plansMap.get("inpatient").add(plan);
                if (plan.startsWith("A")) plansMap.get("outpatient").add(plan);
                if (plan.startsWith("R")) plansMap.get("maternity").add(plan);
                if (plan.startsWith("D")) plansMap.get("dental").add(plan);
                if (plan.startsWith("G")) plansMap.get("glasses").add(plan);
            }

            int maxColumn = 0;
            for (String k : plansMap.keySet()) {
                if (maxColumn<plansMap.get(k).size()) maxColumn = plansMap.get(k).size();
            }

            for (int i=0; i<maxColumn; i++) {
                Listheader lhdr = new Listheader();
                lhdr.setWidth("60px");
                lbPlans.getListhead().appendChild(lhdr);
            }

            if (!plansMap.get("inpatient").isEmpty()) {
                Listitem li = new Listitem();
                li.appendChild(new Listcell("INPATIENT"));
                for (String plan : plansMap.get("inpatient")) li.appendChild(new PlanListcell(this, plan));
                lbPlans.appendChild(li);
            }

            if (!plansMap.get("outpatient").isEmpty()) {
                Listitem li = new Listitem();
                li.appendChild(new Listcell("OUTPATIENT"));
                for (String plan : plansMap.get("outpatient")) li.appendChild(new PlanListcell(this, plan));
                lbPlans.appendChild(li);
            }

            if (!plansMap.get("maternity").isEmpty()) {
                Listitem li = new Listitem();
                li.appendChild(new Listcell("MATERNITY"));
                for (String plan : plansMap.get("maternity")) li.appendChild(new PlanListcell(this, plan));
                lbPlans.appendChild(li);
            }

            if (!plansMap.get("dental").isEmpty()) {
                Listitem li = new Listitem();
                li.appendChild(new Listcell("DENTAL"));
                for (String plan : plansMap.get("dental")) li.appendChild(new PlanListcell(this, plan));
                lbPlans.appendChild(li);
            }

            if (!plansMap.get("glasses").isEmpty()) {
                Listitem li = new Listitem();
                li.appendChild(new Listcell("GLASSES"));
                for (String plan : plansMap.get("glasses")) li.appendChild(new PlanListcell(this, plan));
                lbPlans.appendChild(li);
            }
        } catch (Exception ex) {
            log.error("populatePlans", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }
    
    private void populateMemberNewOcis(int offset, int limit){
    	lbMembers.getItems().clear();
    	Session s = Libs.sfOCIS.openSession();
    	try{
    		String count = "Select count(1) ";
    		String select= "Select * ";
    		String from = "from "+Libs.getDbName()+".dbo.F_OCISProductMember(:idPolis)";
    		
    		SQLQuery countQry = s.createSQLQuery(count+from);
    		countQry.setInteger("idPolis",(Integer)policyNew[6]);
    		
    		Integer recordsCount = (Integer) countQry.uniqueResult();
            pgMembers.setTotalSize(recordsCount);
            
            int startNumber = (((pgMembers.getActivePage()+1) * 20) - 20) + 1;
            
            SQLQuery query = s.createSQLQuery(select+from);
            query.setInteger("idPolis", (Integer)policyNew[6]);
            
            List<Object[]> l = query.setFirstResult(offset).setMaxResults(limit).list();
            for(Object[] o : l){
            	Listitem item = new Listitem();
            	item.appendChild(Libs.createNumericListcell(startNumber, "#,###.##"));
            	item.appendChild(new Listcell(Libs.nn(o[1])));
            	item.appendChild(new Listcell(Libs.nn(o[2])));
            	item.appendChild(new Listcell(Libs.nn(o[3])));
            	item.appendChild(new Listcell(Libs.nn(o[4])));
            	item.appendChild(new Listcell(Libs.nn(o[5])));
            	item.appendChild(new Listcell(Libs.formatDate((Date)o[6])));
            	item.appendChild(Libs.createNumericListcell((Integer)o[7], "#,###"));
            	
            	item.appendChild(new Listcell(Libs.nn(o[8])));
            	item.appendChild(new Listcell(Libs.nn(o[9])));
            	item.appendChild(new Listcell(Libs.nn(o[10])));
            	item.appendChild(new Listcell(Libs.nn(o[11])));
            	item.appendChild(new Listcell(Libs.nn(o[12])));
            	item.appendChild(new Listcell(Libs.nn(o[13])));
            	
            	item.appendChild(new Listcell(Libs.formatDate((Date)o[14])));
            	item.appendChild(new Listcell(Libs.formatDate((Date)o[15])));
            	item.appendChild(new Listcell(Libs.formatDate((Date)o[16])));
            	
            	lbMembers.appendChild(item);
            	
            	startNumber = startNumber + 1;
            	
            }
    		
    		
    	}catch(Exception e){
    		log.error("populateMemberNewOcis",e);
    	}finally{
    		if(s != null && s.isOpen()) s.close();
    	}
    }

    private void populateMembers(int offset, int limit) {
        Map<String,String> clientPlanMap = Libs.getClientPlanMap(policy.getPolicy_string());

        lbMembers.getItems().clear();
        Session s = Libs.sfDB.openSession();
        try {
        	
        	String insid="";
        	List products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
        	
        	String policyNo = policy.getYear()+"-1-0-"+policy.getPolicy_number();
        	
            String count = "select count(*) ";

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
                    + "b.hdt2moe, "
                    + "b.hdt2xdtyy, b.hdt2xdtmm, b.hdt2xdtdd, "
                    + "c.hempmemo3 "; // 39

            String qry = "from idnhltpf.dbo.hlthdr d "
            		+ "inner join idnhltpf.dbo.hltdt1 a on d.hhdryy=a.hdt1yy and d.hhdrbr=a.hdt1br and d.hhdrdist=a.hdt1dist and d.hhdrpono=a.hdt1pono "
                    + "inner join idnhltpf.dbo.hltdt2 b on a.hdt1yy=b.hdt2yy and a.hdt1br=b.hdt2br and a.hdt1dist=b.hdt2dist and a.hdt1pono=b.hdt2pono and a.hdt1idxno=b.hdt2idxno and a.hdt1seqno=b.hdt2seqno and a.hdt1ctr=b.hdt2ctr "
                    + "inner join idnhltpf.dbo.hltemp c on a.hdt1yy=c.hempyy and a.HDT1BR=c.HEMPBR and a.HDT1DIST=c.HEMPDIST and a.hdt1pono=c.hemppono and a.hdt1idxno=c.hempidxno and a.hdt1seqno=c.hempseqno and a.hdt1ctr=c.hempctr "
                    + "where "
                    + "d.hhdrinsid";
                    
            		if(products.size() > 0) qry = qry + " in  ("+insid+")";
            		else qry = qry + "='" + Libs.getInsuranceId() + "' "; 
                    
            		qry +="and (convert(varchar,d.hhdryy)+'-'+convert(varchar,d.hhdrbr)+'-'+convert(varchar,d.hhdrdist)+'-'+convert(varchar,d.hhdrpono)='" + policyNo + "') "
                    + "and a.hdt1ctr=0 and a.hdt1idxno < 99989 and a.hdt1pono <> 99999 "
                    + "and a.hdt1seqno='A' and b.hdt2moe not in('M','U') ";

            if (where!=null) qry += "and (" + where + ") ";

            String order = "order by a.hdt1name asc ";

            Integer recordsCount = (Integer) s.createSQLQuery(count + qry).uniqueResult();
            pgMembers.setTotalSize(recordsCount);
            
            int page = pgMembers.getActivePage();
            
            int startNumber = (((page+1) * 20) - 20) + 1; 

            List<Object[]> l = s.createSQLQuery(select + qry + order).setFirstResult(offset).setMaxResults(limit).list();
            for (Object[] o : l) {
                MemberPOJO memberPOJO = new MemberPOJO();
                memberPOJO.setPolicy(policy);
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
                if (matureDays>0) {
                    lStatus.setValue("ACTIVE");
                    lStatus.setStyle("color:#00FF00");
                } else {
                    lStatus.setValue("MATURE");
                    lStatus.setStyle("color:#FF0000;");
                }
                if (Libs.nn(o[35]).equals("U")) {
                    String effectiveDate = Libs.nn(o[36]) + "-" + Libs.nn(o[37]) + "-" + Libs.nn(o[38]);
                    int effectiveDays = Libs.getDiffDays(new Date(), new SimpleDateFormat("yyyy-MM-dd").parse(effectiveDate));
                    if (effectiveDays<0) {
                        lStatus.setValue("INACTIVE");
                        lStatus.setStyle("color:#000000;");
                    }
                };

                lcStatus.appendChild(lStatus);

                Listitem li = new Listitem();
                li.setValue(memberPOJO);
                
                li.appendChild(new Listcell(startNumber+""));
                li.appendChild(new Listcell(memberPOJO.getCard_number()));
                li.appendChild(lcStatus);
                li.appendChild(new Listcell(Libs.nn(o[34]).trim()));
                li.appendChild(new Listcell(Libs.nn(o[20]).trim()));
                if (Libs.getInsuranceId().equals("00078") || Libs.getInsuranceId().equals("00088")) {
                    li.appendChild(new Listcell(Libs.nn(o[39]).trim()));
                } else {
                    li.appendChild(new Listcell(""));
                }
                li.appendChild(new Listcell(memberPOJO.getName()));
                li.appendChild(new Listcell(memberPOJO.getDob()));
                li.appendChild(Libs.createNumericListcell(ageDays/365, "#"));
                li.appendChild(new Listcell(memberPOJO.getSex()));
                li.appendChild(new Listcell((clientPlanMap.get(memberPOJO.getIp())==null ? memberPOJO.getIp() : clientPlanMap.get(memberPOJO.getIp()))));
                li.appendChild(new Listcell((clientPlanMap.get(memberPOJO.getOp())==null ? memberPOJO.getOp() : clientPlanMap.get(memberPOJO.getOp()))));
                li.appendChild(new Listcell((clientPlanMap.get(memberPOJO.getMaternity())==null ? memberPOJO.getMaternity() : clientPlanMap.get(memberPOJO.getMaternity()))));
                li.appendChild(new Listcell((clientPlanMap.get(memberPOJO.getDental())==null ? memberPOJO.getDental() : clientPlanMap.get(memberPOJO.getDental()))));
                li.appendChild(new Listcell((clientPlanMap.get(memberPOJO.getGlasses())==null ? memberPOJO.getGlasses() : clientPlanMap.get(memberPOJO.getGlasses()))));
                li.appendChild(new Listcell(memberPOJO.getStarting_date()));
                li.appendChild(new Listcell(memberPOJO.getMature_date()));
                lbMembers.appendChild(li);
                
                startNumber = startNumber + 1;
            }
        } catch (Exception ex) {
            log.error("populateMembers", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    public void lbSelected() {

    }

    public void refreshMembers() {
        where = null;
        populateMembers(0, pgMembers.getPageSize());
    }

    public void exportMembers() {
//        try {
//            String uuid = UUID.randomUUID().toString();
//            File f = new File(Libs.config.get("temp_dir").toString() + File.separator + uuid);
//            FileOutputStream fos = new FileOutputStream(f);
//
//            HSSFWorkbook wb = new HSSFWorkbook();
//            HSSFSheet sheet = wb.createSheet("Members");
//            Libs.createXLSRow(sheet, 0, new Object[] {
//                    "Card Number",
//                    "Status",
//                    "Name",
//                    "DOB",
//                    "Age",
//                    "Sex",
//                    "IP",
//                    "OP",
//                    "Maternity",
//                    "Dental",
//                    "Glasses",
//                    "Other",
//                    "Starting Date",
//                    "Mature Date"
//            });
//
//            wb.write(fos);
//            fos.close();
//
//            FileInputStream fis = new FileInputStream(f);
//            Filedownload.save(fis, "Application/Excel", Libs.insuranceId + "-" + (policy.getYear() + "-" + policy.getBr() + "-" + policy.getDist() + "-" + policy.getPolicy_number()) + "-Members-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xls");
//        } catch (Exception ex) {
//            log.error("exportMembers", ex);
//        } finally {
//
//        }
        Libs.showDeveloping();
    }

    public void quickSearch() {
        String val = ((Textbox) getFellow("tQuickSearch")).getText();
        if (!val.isEmpty()) {
            where = "convert(varchar,a.hdt1ncard) like '%" + val + "%' or "
                    + "a.hdt1name like '%" + val + "%' or "
                    + "c.hempcnpol like '%" + val + "%' or "
                    + "c.hempmemo3 like '%" + val + "%' or "
                    + "c.hempcnid like '%" + val + "%' ";
            populateMembers(0, pgMembers.getPageSize());
        } else refreshMembers();
    }

    public void showMemberDetail() {
        Window w = (Window) Executions.createComponents("views/MemberDetail.zul", Libs.getRootWindow(), null);
        w.setAttribute("policy", policy);
        w.setAttribute("member", lbMembers.getSelectedItem().getValue());
        w.doModal();
    }
    
    public void back(){
    	 if (Libs.getCenter().getChildren().size()>0) Libs.getCenter().removeChild(Libs.getCenter().getFirstChild());
         Window w = (Window) Executions.createComponents("views/Policies.zul", Libs.getCenter(), null);
    }

    private void displayPlanItems(String plan) {
        lbPlanItems.getItems().clear();
        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + Libs.createListFieldString("a.hbftbcd") + ", "
                    + Libs.createListFieldString("a.hbftbpln") + " "
                    + "from idnhltpf.dbo.hltbft a "
                    + "where "
                    + "a.hbftyy=" + policy.getYear() + " and "
                    + "a.hbftpono=" + policy.getPolicy_number() + " and "
                    + "a.hbftcode='" + plan + "' ";

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
                        li.appendChild(new Listcell(""));

                        lbPlanItems.appendChild(li);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("displayPlanItems", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    class PlanListcell extends Listcell {

        public PlanListcell(final Window w, final String s) {
            final String label = clientPlanMap.get(s);
            setLabel((label==null) ? s : label);
            addEventListener("onClick", new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    ((Label) w.getFellow("lPlanTitle")).setValue("Plan Items [" + ((label==null) ? s : label) + "]");
                    displayPlanItems(s);
                }
            });
        }

    }

}
