package com.imc.ocisv3.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import org.zkoss.zul.A;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.imc.ocisv3.pojos.PolicyPOJO;
import com.imc.ocisv3.tools.Libs;

/**
 * Created by faizal on 10/24/13.
 */
public class PoliciesController extends Window {

    private Logger log = LoggerFactory.getLogger(PoliciesController.class);
    private Listbox lb;
    private Combobox cbProduct;
    private Paging pg;
    private String where;
    private String userProductViewrestriction;
    
    String insid="";
    List products = null;
    
    private String polis ="";
    private List polisList;

    public void onCreate() {
        if (!Libs.checkSession()) {
            userProductViewrestriction = Libs.restrictUserProductView.get(Libs.getUser());
            initComponents();
            
            /*
            polisList = Libs.getPolisByUserId(Libs.getUser());
            for(int i=0; i < polisList.size(); i++){
        		polis=polis+"'"+(String)polisList.get(i)+"'"+",";
        	}
            if(polis.length() > 1)polis = polis.substring(0, polis.length()-1);
            
            populate(0, pg.getPageSize());
            */
            populateNewOcis(0, pg.getPageSize());
        }
    }

    private void initComponents() {
        lb = (Listbox) getFellow("lb");
        pg = (Paging) getFellow("pg");
        cbProduct = (Combobox) getFellow("cbProduct");

        pg.addEventListener("onPaging", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                PagingEvent evt = (PagingEvent) event;
//                populate(evt.getActivePage()*pg.getPageSize(), pg.getPageSize());
                populateNewOcis(evt.getActivePage()*pg.getPageSize(), pg.getPageSize());
            }
        });
        
        cbProduct.setSelectedIndex(0);
        
       
    	products = Libs.getProductByUserId(Libs.getUser());
    	for(int i=0; i < products.size(); i++){
    		insid=insid+"'"+(String)products.get(i)+"'"+",";
    	}
    	
    	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
    }
    
    private void populateNewOcis(int offset, int limit){
    	lb.getItems().clear();
    	
    	Session s = Libs.sfOCIS.openSession();
    	try{
    		
    		String qryCount = "Select count(1) ";
    		String select = "Select * ";
    		String sql = " from "+ Libs.getDbName()+".dbo.F_OCISProduct(:id) ";
    		if(cbProduct.getSelectedIndex() == 0) {
    			sql = sql + "where PolisStatus='Active'";
    			if(where != null) sql = sql + " and "+where;
    		}
    		else if(cbProduct.getSelectedIndex() == 1) {
    			sql = sql + "where PolisStatus='Close'";
    			if(where != null) sql = sql + " and "+where;
    		}else{
    			if(where != null) sql = sql + " where "+where;
    		}
    		
    		String order = " order by PolisName";
    		
    		SQLQuery countQry = s.createSQLQuery(qryCount+sql);
    		countQry.setInteger("id", Libs.getNewInsuranceId());
    		
    		 Integer count = (Integer) countQry.uniqueResult();
             pg.setTotalSize(count);
    		
    		
    		SQLQuery q = s.createSQLQuery(select+sql+order);
    		q.setInteger("id", Libs.getNewInsuranceId());
    		
    		List<Object[]> l = q.setFirstResult(offset).setMaxResults(limit).list();
    		
    		for(final Object[] o : l){
    			Listitem item = new Listitem();
    			
    			item.appendChild(new Listcell(Libs.nn(o[0])));
    			
    			A policyName = new A(Libs.nn(o[1]).trim());
                policyName.setStyle("color:#00bbee;text-decoration:none");
                
                Listcell cell = new Listcell();
                cell.appendChild(policyName);
                
                
    			item.appendChild(cell);
    			item.appendChild(new Listcell(Libs.nn(o[2])));
    			item.appendChild(Libs.createNumericListcell((Integer)o[3], "#,###"));
    			item.appendChild(new Listcell(Libs.formatDate((Date)o[4])));
    			item.appendChild(new Listcell(Libs.formatDate((Date)o[5])));
    			
    			final Integer policyId = (Integer)o[6];
    			
    			lb.appendChild(item);
    			
    			policyName.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						showPolicyDetailNew(o);
						
					}
				});
    			
    		}
    		
    	}catch(Exception e){
    		log.error("populateNewOcis",e);
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
    	
    }

    private void populate(int offset, int limit) {
        lb.getItems().clear();

        Session s = Libs.sfDB.openSession();
        
        try {
        	
        	
        	
        	
            String countQry = "select count(*) "
                    + "from idnhltpf.dbo.hlthdr a "
                    + "where "
                    + "a.hhdrinsid";
            
            
                    if(products.size() > 0){
                    	countQry = countQry + " in ("+insid+") ";
                    }
                    else{
                    	countQry = countQry + " = '" + Libs.getInsuranceId() + "' ";
                    }
                    
                    if(polisList.size() > 0){
                    	countQry = countQry + "and convert(varchar,a.hhdryy)+'-'+convert(varchar,a.hhdrbr)+'-'+convert(varchar,a.hhdrdist)+'-'+convert(varchar,a.hhdrpono) "
            					  + "in ("+polis+") ";
            		} 
                    
                    if(cbProduct.getSelectedIndex() == 0)
                    	countQry = countQry + " and convert(datetime,convert(varchar,hhdrmdtmm)+'-'+convert(varchar, hhdrmdtdd)+'-'+convert(varchar,hhdrmdtyy), 110) >= GETDATE() ";
                    else if(cbProduct.getSelectedIndex() == 1)
                    	countQry = countQry + " and convert(datetime,convert(varchar,hhdrmdtmm)+'-'+convert(varchar, hhdrmdtdd)+'-'+convert(varchar,hhdrmdtyy), 110) < GETDATE() ";
                  

            String qry = "select "
                    + "a.hhdryy, a.hhdrbr, a.hhdrdist, a.hhdrpono, "
                    + "a.hhdrname, "
                    + "a.hhdrsdtyy, a.hhdrsdtmm, a.hhdrsdtdd, "
                    + "a.hhdrmdtyy, a.hhdrmdtmm, a.hhdrmdtdd "
                    + "from idnhltpf.dbo.hlthdr a "
                    + "where "
                    + "a.hhdrinsid";
            	if(products.size() > 0) qry = qry + " in  ("+insid+") ";
            	else qry = qry + " = '" + Libs.getInsuranceId() + "' ";
            	
            	if(polisList.size() > 0){
        			qry = qry + "and convert(varchar,a.hhdryy)+'-'+convert(varchar,a.hhdrbr)+'-'+convert(varchar,a.hhdrdist)+'-'+convert(varchar,a.hhdrpono) "
        					  + "in ("+polis+") ";
        		} 
            
            	if(cbProduct.getSelectedIndex() == 0)
                	qry = qry + " and convert(datetime,convert(varchar,hhdrmdtmm)+'-'+convert(varchar, hhdrmdtdd)+'-'+convert(varchar,hhdrmdtyy), 110) >= GETDATE() ";
                else if(cbProduct.getSelectedIndex() == 1)
                	qry = qry + " and convert(datetime,convert(varchar,hhdrmdtmm)+'-'+convert(varchar, hhdrmdtdd)+'-'+convert(varchar,hhdrmdtyy), 110) < GETDATE() ";


            if (where!=null) {
                countQry += "and (" + where + ") ";
                qry += "and (" + where + ") ";
            }

            SQLQuery query = s.createSQLQuery(countQry);
                      
            Integer count = (Integer) query.uniqueResult();
            pg.setTotalSize(count);
            
            query = s.createSQLQuery(qry);
           
  
            boolean show = true;
//            List<Object[]> l = s.createSQLQuery(qry).setFirstResult(offset).setMaxResults(limit).list();
            List<Object[]> l = query.setFirstResult(offset).setMaxResults(limit).list();
            
            for (Object[] o : l) {
                String policyNumber = Libs.nn(o[0]) + "-" + Libs.nn(o[1]) + "-" + Libs.nn(o[2]) + "-" + Libs.nn(o[3]);
                String startingDate = Libs.nn(o[5]) + "-" + Libs.nn(o[6]) + "-" + Libs.nn(o[7]);
                String matureDate = Libs.nn(o[8]) + "-" + Libs.nn(o[9]) + "-" + Libs.nn(o[10]);
                int matureDays = Libs.getDiffDays(new Date(), new SimpleDateFormat("yyyy-MM-dd").parse(matureDate));

                Listcell lcStatus = new Listcell();
                Label lStatus = new Label();
                if (matureDays>0) {
                    lStatus.setValue("ACTIVE");
                    lStatus.setStyle("color:#00FF00");
                } else {
                    lStatus.setValue("MATURE");
                    lStatus.setStyle("color:#FF0000;");
                }
                lcStatus.appendChild(lStatus);

                Listitem li = new Listitem();

               final PolicyPOJO policyPOJO = new PolicyPOJO();
                policyPOJO.setYear(Integer.valueOf(Libs.nn(o[0])));
                policyPOJO.setBr(Integer.valueOf(Libs.nn(o[1])));
                policyPOJO.setDist(Integer.valueOf(Libs.nn(o[2])));
                policyPOJO.setPolicy_number(Integer.valueOf(Libs.nn(o[3])));
                policyPOJO.setName(Libs.nn(o[4]).trim());

                li.setValue(policyPOJO);

                A policyName = new A(Libs.nn(o[4]).trim());
                policyName.setStyle("color:#00bbee;text-decoration:none");
                
                li.appendChild(new Listcell(policyNumber));
//                li.appendChild(new Listcell(((Libs.config.get("demo_mode")).equals("true") && Libs.getInsuranceId().equals("00051")) ? Libs.nn(Libs.config.get("demo_name")) : Libs.nn(o[4]).trim()));
                Listcell lc = new Listcell();
                lc.appendChild(policyName);
                li.appendChild(lc);
                li.appendChild(lcStatus);
                li.appendChild(Libs.createNumericListcell(getMemberCount(Libs.nn(o[0]), Libs.nn(o[3])), "#,###"));
                li.appendChild(new Listcell(startingDate));
                li.appendChild(new Listcell(matureDate));

                if (!Libs.nn(userProductViewrestriction).isEmpty()) {
                    if (!userProductViewrestriction.contains(Libs.nn(o[3]))) show=false;
                    else show=true;
                }
                
                policyName.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						showPolicyDetail(policyPOJO);
						
					}
				});

                if (show) lb.appendChild(li);
                

                
            }
        } catch (Exception ex) {

            log.error("populate", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
            
        }
    }

    private int getMemberCount(String hdt1yy, String hdt1pono) {
        int result = 0;
        Session s = Libs.sfDB.openSession();
        try {
        	
        	String insid="";
        	List products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1) insid = insid.substring(0, insid.length()-1);
        	
        	String policyNo = hdt1yy+"-1"+"-0-"+hdt1pono;
            String qry = "select count(*) from idnhltpf.dbo.hlthdr b "
                    + "inner join idnhltpf.dbo.hltdt1 a "
            		+ "on b.hhdryy=a.hdt1yy and b.hhdrbr=a.hdt1br and b.hhdrdist=a.hdt1dist and b.hhdrpono=a.hdt1pono "
            		+ "inner join idnhltpf.dbo.hltdt2 d "
                    + "on a.hdt1yy=d.hdt2yy and a.hdt1br=d.hdt2br and a.hdt1dist=d.hdt2dist and a.hdt1pono=d.hdt2pono and a.hdt1idxno=d.hdt2idxno and a.hdt1seqno=d.hdt2seqno and a.hdt1ctr=d.hdt2ctr "
            		+ "inner join idnhltpf.dbo.hltemp c on a.hdt1yy=c.hempyy and  a.HDT1BR=c.HEMPBR and a.HDT1DIST=c.HEMPDIST and a.hdt1pono=c.hemppono and a.hdt1idxno=c.hempidxno and a.hdt1seqno=c.hempseqno and a.hdt1ctr=c.hempctr "
                    + "where "
            		+ "b.hhdrinsid";
            		if(products.size() > 0) qry = qry + " in  ("+insid+") ";
            		else qry = qry + "='" + Libs.getInsuranceId() + "' "; 
            		
            		qry += "and (convert(varchar,b.hhdryy)+'-'+convert(varchar,b.hhdrbr)+'-'+convert(varchar,b.hhdrdist)+'-'+convert(varchar,b.hhdrpono)='" + policyNo + "') "
                    + "and a.hdt1ctr=0 and a.hdt1idxno < 99989 and a.hdt1pono <> 99999 "
            		+ "and d.hdt2moe not in('M','U') ";
            
//            System.out.println(qry);

            result = (Integer) s.createSQLQuery(qry).uniqueResult();
        } catch (Exception ex) {
            log.error("getMemberCount", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
        return result;
    }

    public void refresh() {
        where = null;
//        populate(0, pg.getPageSize());
        populateNewOcis(0, pg.getPageSize());
    }

    public void quickSearch() {
        String val = ((Textbox) getFellow("tQuickSearch")).getText();
        if (!val.isEmpty()) {
            /*where = "convert(varchar,a.hhdrpono) like '%" + val + "%' or "
                    + "a.hhdrname like '%" + val + "%' ";
                    */
        	where = "PolisName like '%" + val + "%'";
//            populate(0, pg.getPageSize());
        	populateNewOcis(0, pg.getPageSize());
        } else refresh();
    }

    public void productSelected() {
    	quickSearch();
    }
    
    public void showPolicyDetailNew(Object[] pojo) {
        if (Libs.getCenter().getChildren().size()>0) Libs.getCenter().removeChild(Libs.getCenter().getFirstChild());
        Window w = (Window) Executions.createComponents("views/PolicyDetail.zul", Libs.getCenter(), null);
//        w.setAttribute("policy", lb.getSelectedItem().getValue());
        w.setAttribute("policy", pojo);
    }

    public void showPolicyDetail(PolicyPOJO pojo) {
        if (Libs.getCenter().getChildren().size()>0) Libs.getCenter().removeChild(Libs.getCenter().getFirstChild());
        Window w = (Window) Executions.createComponents("views/PolicyDetail.zul", Libs.getCenter(), null);
//        w.setAttribute("policy", lb.getSelectedItem().getValue());
        w.setAttribute("policy", pojo);
    }

    public void export() {
        Session s = Libs.sfDB.openSession();
        try {
        	
        	String insid="";
        	List products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1) insid = insid.substring(0, insid.length()-1);
        	
        	
        	
            String qry = "select "
                    + "a.hhdryy, a.hhdrbr, a.hhdrdist, a.hhdrpono, "
                    + "a.hhdrname, "
                    + "a.hhdrsdtyy, a.hhdrsdtmm, a.hhdrsdtdd, "
                    + "a.hhdrmdtyy, a.hhdrmdtmm, a.hhdrmdtdd "
                    + "from idnhltpf.dbo.hlthdr a "
                    + "where "
                    + "a.hhdrinsid";
            
            		if(products.size() > 0) qry = qry + " in  ("+insid+") ";
            		else qry = qry + "='" + Libs.getInsuranceId() + "' ";  //='" + Libs.getInsuranceId() + "' ";
            		

                	if(cbProduct.getSelectedIndex() == 0)
                    	qry = qry + " and convert(datetime,convert(varchar,hhdrmdtmm)+'-'+convert(varchar, hhdrmdtdd)+'-'+convert(varchar,hhdrmdtyy), 110) >= GETDATE() ";
                    else if(cbProduct.getSelectedIndex() == 1)
                    	qry = qry + " and convert(datetime,convert(varchar,hhdrmdtmm)+'-'+convert(varchar, hhdrmdtdd)+'-'+convert(varchar,hhdrmdtyy), 110) < GETDATE() ";

            if (where!=null) qry += "and (" + where + ") ";

            String uuid = UUID.randomUUID().toString();
            File f = new File(Libs.config.get("temp_dir").toString() + File.separator + uuid);
            FileOutputStream fos = new FileOutputStream(f);

            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("Policies");
            
            
           
            
            Libs.createXLSRow(sheet, 0, new Object[] {
                    "Policy Number",
                    "Name",
                    "Status",
                    "Members",
                    "Starting Date",
                    "Mature Date",
            });

            int row = 1;
            List<Object[]> l = s.createSQLQuery(qry).list();
            for (Object[] o : l) {
                String policyNumber = Libs.nn(o[0]) + "-" + Libs.nn(o[1]) + "-" + Libs.nn(o[2]) + "-" + Libs.nn(o[3]);
                String startingDate = Libs.nn(o[5]) + "-" + Libs.nn(o[6]) + "-" + Libs.nn(o[7]);
                String matureDate = Libs.nn(o[8]) + "-" + Libs.nn(o[9]) + "-" + Libs.nn(o[10]);
                int matureDays = Libs.getDiffDays(new Date(), new SimpleDateFormat("yyyy-MM-dd").parse(matureDate));

                String status = (matureDays>0) ? "ACTIVE" : "MATURE";

                Libs.createXLSRow(sheet, row, new Object[] {
                        policyNumber,
                        Libs.nn(o[4]).trim(),
                        status,
                        getMemberCount(Libs.nn(o[0]), Libs.nn(o[3])),
                        startingDate,
                        matureDate
                });
                row++;
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            sheet.autoSizeColumn(3);
            sheet.autoSizeColumn(4);
            sheet.autoSizeColumn(5);
            
            wb.write(fos);
            fos.close();

            FileInputStream fis = new FileInputStream(f);
            Filedownload.save(fis, "Application/Excel", Libs.getInsuranceId() + "-Policies-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xls");
        } catch (Exception ex) {
            log.error("export", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

}
