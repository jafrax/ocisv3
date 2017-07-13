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
 * Created by Arifullah on 5/11/2014.
 */
public class PaymentHistoryController extends Window {

    private Logger log = LoggerFactory.getLogger(PaymentHistoryController.class);
    private Listbox lb;
    private Paging pg;
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
            
            populate(0, pg.getPageSize());
        }
    }

    private void initComponents() {
        lb = (Listbox) getFellow("lb");
        pg = (Paging) getFellow("pg");
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
      
        endDate.setValue(new Date());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        startDate.setValue(cal.getTime());
        
     }
    
  

    private void populate(int offset, int limit) {
        lb.getItems().clear();
        Session s = Libs.sfDB.openSession();
        try {
        	
        	
        	String countQry = "select count(1) ";
            String select   = "select pdate, HINSNAME,chequeNo, jumlahClaim, proposed, approved, provider "; 
            String from     = "from (select COUNT(1) as jumlahClaim, "
            				+ "SUM(c.HCLMCAMT1+c.HCLMCAMT2+c.HCLMCAMT3+c.HCLMCAMT4+c.HCLMCAMT5+c.HCLMCAMT6+c.HCLMCAMT7+c.HCLMCAMT8+ "
            		        + "c.HCLMCAMT9+c.HCLMCAMT10+c.HCLMCAMT11+c.HCLMCAMT12+c.HCLMCAMT13+c.HCLMCAMT14+c.HCLMCAMT15+c.HCLMCAMT16+ "
            				+ "c.HCLMCAMT17+c.HCLMCAMT18+c.HCLMCAMT19+c.HCLMCAMT20+c.HCLMCAMT21+c.HCLMCAMT22+c.HCLMCAMT23+c.HCLMCAMT24+c.HCLMCAMT25+c.HCLMCAMT26+ "
            		        + "c.HCLMCAMT27+c.HCLMCAMT28+c.HCLMCAMT29+c.HCLMCAMT30) as proposed, " 
            				+ "SUM(c.HCLMAAMT1+c.HCLMAAMT2+c.HCLMAAMT3+c.HCLMAAMT4+c.HCLMAAMT5+c.HCLMAAMT6+c.HCLMAAMT7+c.HCLMAAMT8+c.HCLMAAMT9+c.HCLMAAMT10+ "
            		        + "c.HCLMAAMT11+c.HCLMAAMT12+c.HCLMAAMT13+c.HCLMAAMT14+c.HCLMAAMT15+c.HCLMAAMT16+c.HCLMAAMT17+c.HCLMAAMT18+c.HCLMAAMT19+c.HCLMAAMT20+ "
            				+ "c.HCLMAAMT21+c.HCLMAAMT22+c.HCLMAAMT23+c.HCLMAAMT24+c.HCLMAAMT25+c.HCLMAAMT26+c.HCLMAAMT27+ "
            		        + "c.HCLMAAMT28+c.HCLMAAMT29+c.HCLMAAMT30) as approved, i.HINSNAME, (HOVCTYP+' '+convert(varchar,HOVCCQNO)) as chequeNo, v.hovcbank, "
            				+ "convert(datetime,(convert(varchar,c.hclmpdatem)+'-'+convert(varchar,c.hclmpdated)+'-'+convert(varchar,c.hclmpdatey)),110) as pdate, "
            		        + "'Y' as provider from IDNHLTPF.dbo.HLTINS i inner join IDNHLTPF.dbo.HLTHDR h on i.HINSID=h.HHDRINSID inner join IDNHLTPF.dbo.hltclm c " 
            				+ "on c.HCLMYY=h.HHDRYY and c.HCLMBR=h.HHDRBR and "
            				+ "c.HCLMDIST=h.HHDRDIST and c.HCLMPONO=h.HHDRPONO inner join IDNHLTPF.dbo.hltovc v on v.HOVCCNO=c.HCLMNOMOR "
            		        + "where h.HHDRINSID";
            if(products.size() > 0) from = from + " in  ("+insid+") ";
			else from = from + "='" + Libs.getInsuranceId() + "' ";
            
            from = from + "and c.HCLMRECID='P' and c.hclmpdatem > 0 and c.hclmpdated > 0 and c.hclmpdatey > 2012 and c.HCLMNHOSCD <> '0' "
            		    + "and convert(datetime,(convert(varchar,c.hclmpdatem)+'-'+convert(varchar,c.hclmpdated)+'-'+convert(varchar,c.hclmpdatey)),110) "
            		    + "between '"+startDate.getText()+"' AND '"+endDate.getText()+"' "
            		    + "group by i.HINSNAME,(HOVCTYP+' '+convert(varchar,HOVCCQNO)),v.hovcbank, "
            		    + "convert(datetime,(convert(varchar,c.hclmpdatem)+'-'+convert(varchar,c.hclmpdated)+'-'+convert(varchar,c.hclmpdatey)),110) "
            		    + "UNION "
            		    + "select COUNT(1) as jumlahClaim, SUM(c.HCLMCAMT1+c.HCLMCAMT2+c.HCLMCAMT3+c.HCLMCAMT4+c.HCLMCAMT5+c.HCLMCAMT6+c.HCLMCAMT7+c.HCLMCAMT8+ "
            		    + "c.HCLMCAMT9+c.HCLMCAMT10+c.HCLMCAMT11+c.HCLMCAMT12+c.HCLMCAMT13+c.HCLMCAMT14+c.HCLMCAMT15+c.HCLMCAMT16+c.HCLMCAMT17+c.HCLMCAMT18+ "
            		    + "c.HCLMCAMT19+c.HCLMCAMT20+c.HCLMCAMT21+c.HCLMCAMT22+c.HCLMCAMT23+c.HCLMCAMT24+c.HCLMCAMT25+c.HCLMCAMT26+c.HCLMCAMT27+c.HCLMCAMT28+ "
            		    + "c.HCLMCAMT29+c.HCLMCAMT30) as proposed, SUM(c.HCLMAAMT1+c.HCLMAAMT2+c.HCLMAAMT3+c.HCLMAAMT4+c.HCLMAAMT5+ c.HCLMAAMT6+c.HCLMAAMT7+ "
            		    + "c.HCLMAAMT8+c.HCLMAAMT9+c.HCLMAAMT10+c.HCLMAAMT11+c.HCLMAAMT12+c.HCLMAAMT13+c.HCLMAAMT14+c.HCLMAAMT15+c.HCLMAAMT16+c.HCLMAAMT17+ "
            		    + "c.HCLMAAMT18+c.HCLMAAMT19+c.HCLMAAMT20+c.HCLMAAMT21+c.HCLMAAMT22+c.HCLMAAMT23+c.HCLMAAMT24+c.HCLMAAMT25+c.HCLMAAMT26+c.HCLMAAMT27+ "
            		    + "c.HCLMAAMT28+c.HCLMAAMT29+c.HCLMAAMT30) as approved, i.HINSNAME, (HOVCTYP+' '+convert(varchar,HOVCCQNO)) as chequeNo, v.hovcbank, "
            		    + "convert(datetime,(convert(varchar,c.hclmpdatem)+'-'+convert(varchar,c.hclmpdated)+'-'+convert(varchar,c.hclmpdatey)),110) as pdate, "
            		    + "'N' as provider from IDNHLTPF.dbo.HLTINS i inner join IDNHLTPF.dbo.HLTHDR h on i.HINSID=h.HHDRINSID  inner join IDNHLTPF.dbo.hltclm c " 
            		    + "on c.HCLMYY=h.HHDRYY and c.HCLMBR=h.HHDRBR and "
            		    + "c.HCLMDIST=h.HHDRDIST and c.HCLMPONO=h.HHDRPONO inner join IDNHLTPF.dbo.hltovc v on v.HOVCCNO=c.HCLMNOMOR where h.HHDRINSID";
            if(products.size() > 0) from = from + " in  ("+insid+") ";
			else from = from + "='" + Libs.getInsuranceId() + "' ";
            
            from = from + "and c.HCLMRECID='P' and c.hclmpdatem > 0 and c.hclmpdated > 0 and c.hclmpdatey > 2012 and c.HCLMNHOSCD = '0' and "
            		    + "convert(datetime,(convert(varchar,c.hclmpdatem)+'-'+convert(varchar,c.hclmpdated)+'-'+convert(varchar,c.hclmpdatey)),110) "
            		    + "between  '"+startDate.getText()+"' AND '"+endDate.getText()+"' "
            		    + "group by i.HINSNAME,(HOVCTYP+' '+convert(varchar,HOVCCQNO)),v.hovcbank, "
            		    + "convert(datetime,(convert(varchar,c.hclmpdatem)+'-'+convert(varchar,c.hclmpdated)+'-'+convert(varchar,c.hclmpdatey)),110)) q ";
            		    

          
            Integer count = (Integer) s.createSQLQuery(countQry+from).uniqueResult();
            pg.setTotalSize(count);
            
            String order = "order by q.pdate asc";
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
 
            List<Object[]> l = s.createSQLQuery(select + from+order).setFirstResult(offset).setMaxResults(limit).list();
            for (final Object[] o : l) {
            	
            	Listitem li = new Listitem();
            	li.appendChild(new Listcell(sdf.format((Date)o[0]))); //date
            	li.appendChild(new Listcell(Libs.nn(o[1]))); //client name
            	A chequeNo = new A(Libs.nn(o[2]));
            	chequeNo.setStyle("color:#00bbee;text-decoration :none");
            	Listcell cell = new Listcell();
            	cell.appendChild(chequeNo);
            	
            	li.appendChild(cell); //cheque no
            	li.appendChild(new Listcell(Libs.nn(o[3]))); //total claim number
            	
            	Double total = 0.0;
            	// get biaya lain-lain
            	Double biaya = Libs.getPemakaianDana(Libs.nn(o[2]), insid);
            	if(Libs.nn(o[6]).equalsIgnoreCase("Y")){
            		total = biaya + Double.valueOf(Libs.nn(o[4]));
            		li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[4])), "#,###.##"));
            	}
            	else {
            		total = biaya + Double.valueOf(Libs.nn(o[5]));
            		li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[5])), "#,###.##"));
            	}
            	
            	li.appendChild(Libs.createNumericListcell(biaya, "#,###.##"));//
            	
            	li.appendChild(Libs.createNumericListcell(total, "#,###.##"));//
            	
            	chequeNo.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						Window w = (Window) Executions.createComponents("views/PaymentDetail.zul", null, null);
				        w.setAttribute("payment", o);
				        w.doModal();
					}
				});
            	
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
			
			
		}catch(Exception e){
			System.out.println( procode + " - "+ hid+ " *** "+ query +"\n");
			log.error("getHospitalInvoice", e);
		}finally{
			 if (s!=null && s.isOpen()) s.close();
		}
		return hasil;
	}

	public void refresh() {
        populate(0, pg.getPageSize());
    }
    
   
    

    public void lbSelected() {
        if (lb.getSelectedCount()>0) {
            ((Toolbarbutton) getFellow("tbnShowMemberDetail")).setDisabled(false);
        }
    }

   

    public void export() {
    	StringBuffer sb = new StringBuffer();
        Session s = Libs.sfDB.openSession();
        try {
        	
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
    
    public void viewPaymentHistory(){
    	populate(0, pg.getPageSize());
    }

}
