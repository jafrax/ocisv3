package com.imc.ocisv3.controllers;

import bsh.Interpreter;

import com.imc.ocisv3.pojos.BenefitPOJO;
import com.imc.ocisv3.pojos.ClaimPOJO;
import com.imc.ocisv3.tools.Libs;





import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.sql.Clob;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.serial.SerialClob;

/**
 * Created by faizal on 10/29/13.
 */
public class ClaimDetailController extends Window {

    private Logger log = LoggerFactory.getLogger(ClaimDetailController.class);
    private ClaimPOJO claimPOJO;
    private String hidNumber;
    private BigInteger claimNumber;
    private Listbox lb;
    private A lembarAnalisa;
    private Listheader lhDaysLeft;
    
    int counter = 0;

    public void onCreate() {
        if (!Libs.checkSession()) {
//            claimPOJO = (ClaimPOJO) getAttribute("claim");
        	Object[] obj = (Object[])getAttribute("claim");
        	hidNumber = (String) obj[0];
        	claimNumber = (BigInteger)obj[1];
        	
            initComponents();
            populate();
        }
    }

    private void initComponents() {
        lb = (Listbox) getFellow("lb");
        lembarAnalisa = (A)getFellow("lembarAnalisa");
//        lhDaysLeft = (Listheader) getFellow("lhDaysLeft");

//        getCaption().setLabel("Claim Detail [" + claimPOJO.getClaim_number() + "]");
        getCaption().setLabel("Claim Detail [" + hidNumber + "]");
        
        lembarAnalisa.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				// TODO Auto-generated method stub
				downloadLembarAnalisa();
			}
		});
    }

    private void populate() {
//        populateInformation();
//        populatePlanItems();
    	
    	populateInformationNew();
    	populatePlanItemsNew();
    }
    
    private void populatePlanItemsNew(){
    	lb.getItems().clear();
    	Session s = Libs.sfOCIS.openSession();
    	try{
    		double totalProposed = 0;
            double totalApproved = 0;

    		String qry = "select * from IMCare.dbo.F_OCISClaimDetailDt ("+claimNumber.intValue()+") ";
    		List<Object[]> l = s.createSQLQuery(qry).list();
    		for(Object[] o : l){
               Listitem li = new Listitem();
    	       li.appendChild(new Listcell(Libs.nn(o[0])));
    	       li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[1])), "#"));
               li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[2])), "#,###.##"));
    	       li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[3])), "#,###.##"));
    	       li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[4])), "#,###.##"));
    	       li.appendChild(Libs.createRemarksListcell(Libs.nn(5), this));
    	       lb.appendChild(li);
    	       
    	       totalProposed = totalProposed + Double.valueOf(Libs.nn(o[2])).doubleValue();
    	       totalApproved = totalApproved + Double.valueOf(Libs.nn(o[3])).doubleValue();
    		}
    		
    		((Listfooter) getFellow("ftrTotalProposed")).setLabel(new DecimalFormat("#,###.##").format(totalProposed));
            ((Listfooter) getFellow("ftrTotalApproved")).setLabel(new DecimalFormat("#,###.##").format(totalApproved));
            ((Listfooter) getFellow("ftrTotalExcess")).setLabel(new DecimalFormat("#,###.##").format(totalProposed-totalApproved));
    		
    	}catch(Exception e){
    		log.error("populatePlanItemsNew", e);
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
    }
    
    private void populateInformationNew(){
    	Session s = Libs.sfOCIS.openSession();
    	try{
    		String qry = "Select * from IMCare.dbo.F_OCISClaimDetailHd("+claimNumber.intValue()+") ";    
    		
    		List<Object[]> l = s.createSQLQuery(qry).list();
    		if(l.size() > 0) {
    			Object[] o = l.get(0);
    			  String diag = Libs.nn(o[10]).replace("(", "-");
    			  
    			  
    			  String[] diagnosis = diag.split("-");
    			  
    			 ((Label) getFellow("lProvider")).setValue(Libs.nn(o[7]));
                 ((Label) getFellow("lDiagnosis")).setValue(diagnosis[1]);
                 ((Label) getFellow("lDescription")).setValue(diagnosis[0]);
                 ((Label) getFellow("lName")).setValue(Libs.nn(o[0]).trim());
                 ((Label) getFellow("lDOB")).setValue(Libs.formatDate((Date)o[2]));
                 ((Label) getFellow("lClaimType")).setValue(Libs.nn(o[9]).trim());
                 if(!Libs.nn(o[9]).trim().equals("InPatient")){
                	 ((Label) getFellow("lTitleReceiptDate")).setVisible(true);
                	 ((Label) getFellow("lTitleServiceIn")).setVisible(false);
                	 ((Label) getFellow("lReceiptDate")).setValue(Libs.formatDate((Date)o[8]));
                	 ((Label) getFellow("lServiceOut")).setValue(Libs.formatDate((Date)o[8]));
                	 
                	
                 }
                 else{
                	 ((Label) getFellow("lTitleReceiptDate")).setVisible(false);
                	 ((Label) getFellow("lTitleServiceIn")).setVisible(true);
                	 ((Label) getFellow("lServiceIn")).setValue(Libs.formatDate((Date)o[8]));
//                	 ((Label) getFellow("lServiceOut")).setValue(Libs.formatDate((Date)o[8])); //need new field
                 }
               
                 ((Label) getFellow("lSex")).setValue(Libs.nn(o[1]).trim());
                 ((Label) getFellow("lCardNumber")).setValue(Libs.nn(o[4]).trim());
                 ((Label) getFellow("lMaritalStatus")).setValue(Libs.nn(o[3]).trim());
                 ((Label) getFellow("lCompanyName")).setValue(Libs.nn(o[5]).trim());
//                 ((Label) getFellow("lServiceDays")).setValue(String.valueOf(Libs.getDiffDays(new SimpleDateFormat("yyyy-MM-dd").parse(serviceIn), new SimpleDateFormat("yyyy-MM-dd").parse(serviceOut))+1));
//                 ((Label) getFellow("tRemarks")).setValue(remarks);
    		}
    	}catch(Exception e){
    		log.error("populateInformationNew", e);
    	}finally{
    		 if (s!=null && s.isOpen()) s.close();
    	}
    }

    private void populateInformation() {
        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + "a.hclmsinyy, a.hclmsinmm, a.hclmsindd, "
                    + "a.hclmsoutyy, a.hclmsoutmm, a.hclmsoutdd, "
                    + "a.hclmdiscd1, a.hclmdiscd2, a.hclmdiscd3, "
                    + "b.hproname, a.hclmtclaim, c.hdt1name, "
                    + "c.hdt1bdtyy, c.hdt1bdtmm, c.hdt1bdtdd, "
                    + "c.hdt1sex, c.hdt1ncard, c.hdt1mstat, d.hhdrname, "
                    + "e.hmem2data1, e.hmem2data2, e.hmem2data3, e.hmem2data4, "
                    + "a.hclmrdatey, a.hclmrdatem, a.hclmrdated "
                    + "from idnhltpf.dbo.hltclm a "
                    + "inner join idnhltpf.dbo.hltpro b on b.hpronomor=a.hclmnhoscd "
                    + "inner join idnhltpf.dbo.hltdt1 c on c.hdt1yy=a.hclmyy and c.hdt1pono=a.hclmpono and c.hdt1idxno=a.hclmidxno and c.hdt1seqno=a.hclmseqno and c.hdt1ctr=0 "
                    + "inner join idnhltpf.dbo.hlthdr d on d.hhdryy=a.hclmyy and d.hhdrpono=a.hclmpono "
                    + "left outer join idnhltpf.dbo.hltmemo2 e on e.hmem2yy=a.hclmyy and e.hmem2pono=a.hclmpono and e.hmem2idxno=a.hclmidxno and e.hmem2seqno=a.hclmseqno and e.hmem2claim=a.hclmtclaim and e.hmem2count=a.hclmcount "
                    + "where "
                    + "a.hclmcno='" + claimPOJO.getClaim_number() + "' and "
                    + "(convert(varchar,a.hclmyy)+'-'+convert(varchar,a.hclmbr)+'-'+convert(varchar,a.hclmdist)+'-'+convert(varchar,a.hclmpono))='" + claimPOJO.getPolicy_number() + "' and "
                    + "(convert(varchar,a.hclmidxno)+'-'+a.hclmseqno)='" + claimPOJO.getIndex() + "' and "
                    + "a.hclmcount=" + claimPOJO.getClaim_count();
            
            System.out.println(qry);

            List<Object[]> l = s.createSQLQuery(qry).list();
            if (l.size()==1) {
                Object[] o = l.get(0);

                if ("I,R".contains(Libs.nn(o[10]))) {
                    getFellow("lTitleServiceIn").setVisible(true);
                    getFellow("lTitleServiceOut").setVisible(true);
                    getFellow("lTitleServiceDays").setVisible(true);
                    getFellow("lServiceIn").setVisible(true);
                    getFellow("lServiceOut").setVisible(true);
                    getFellow("lServiceDays").setVisible(true);
                    getFellow("lTitleReceiptDate").setVisible(false);
                    getFellow("lReceiptDate").setVisible(false);
                } else {
                    getFellow("lTitleServiceIn").setVisible(false);
                    getFellow("lTitleServiceOut").setVisible(false);
                    getFellow("lTitleServiceDays").setVisible(false);
                    getFellow("lServiceIn").setVisible(false);
                    getFellow("lServiceOut").setVisible(false);
                    getFellow("lServiceDays").setVisible(false);
                    getFellow("lTitleReceiptDate").setVisible(true);
                    getFellow("lReceiptDate").setVisible(true);
                }

                String diagnosis = Libs.nn(o[6]).trim();
                if (!Libs.nn(o[7]).trim().isEmpty()) diagnosis += ", " + Libs.nn(o[7]).trim();
                if (!Libs.nn(o[8]).trim().isEmpty()) diagnosis += ", " + Libs.nn(o[8]).trim();

                String[] segShowRemainingDays = Libs.nn(Libs.config.get("show_remaining_days")).split("\\,");
                for (String seg : segShowRemainingDays) {
                    String[] segDisease = seg.split("\\:");
                    if (segDisease[0].equals(Libs.nn(claimPOJO.getPolicy().getPolicy_number()))) {
                        boolean show = false;
                        if (segDisease.length==1) {
                            show = true;
                        } else if (diagnosis.contains(segDisease[1])) {
                            show = true;
                        }

                        if (show) {
                            lhDaysLeft.setVisible(true);
                        }
                    }
                }

                String dob = Libs.nn(o[12]) + "-" + Libs.nn(o[13]) + "-" + Libs.nn(o[14]);
                String receiptDate = Libs.nn(o[23]) + "-" + Libs.nn(o[24]) + "-" + Libs.nn(o[25]);
                int ageDays = 0;
                try {
                    ageDays = Libs.getDiffDays(new SimpleDateFormat("yyyy-MM-dd").parse(dob), new Date());
                } catch (Exception ex) {
                    log.error("populateInformation", ex);
                }

                Label lAge = (Label) getFellow("lAge");
                lAge.setStyle("text-align:right;");
                lAge.setValue("(" + (ageDays / 365) + ")");

                String serviceIn = o[0] + "-" + o[1] + "-" + o[2];
                String serviceOut = o[3] + "-" + o[4] + "-" + o[5];
                String remarks = Libs.nn(o[19]).trim() + Libs.nn(o[20]).trim() + "\n" + Libs.nn(o[21]).trim() + Libs.nn(o[22]).trim();
                String provider = Libs.nn(o[9]).trim();

                if (remarks.indexOf("[")>-1 && remarks.indexOf("]")>-1) {
                    provider = remarks.substring(remarks.indexOf("[")+1, remarks.indexOf("]"));
                    remarks = remarks.substring(remarks.indexOf("]")+1);
                }

                String companyName = Libs.nn(o[18]).trim();
                if (Libs.config.get("demo_mode").equals("true") && Libs.getInsuranceId().equals("00051")) companyName = Libs.nn(Libs.config.get("demo_name"));

                ((Label) getFellow("lProvider")).setValue(provider);
                ((Label) getFellow("lDiagnosis")).setValue(diagnosis.toUpperCase());
                ((Label) getFellow("lDescription")).setValue(Libs.getICDByCode(diagnosis));
                ((Label) getFellow("lServiceIn")).setValue(Libs.fixDate(serviceIn));
                ((Label) getFellow("lServiceOut")).setValue(Libs.fixDate(serviceOut));
                ((Label) getFellow("lReceiptDate")).setValue(Libs.fixDate(receiptDate));
                ((Label) getFellow("lClaimType")).setValue(Libs.getClaimType(Libs.nn(o[10]).trim()));
                ((Label) getFellow("lName")).setValue(Libs.nn(o[11]).trim());
                ((Label) getFellow("lDOB")).setValue(Libs.fixDate(dob));
                ((Label) getFellow("lSex")).setValue(Libs.nn(o[15]).trim());
                ((Label) getFellow("lCardNumber")).setValue(Libs.nn(o[16]).trim());
                ((Label) getFellow("lMaritalStatus")).setValue(Libs.nn(o[17]).trim());
                ((Label) getFellow("lCompanyName")).setValue(companyName);
                ((Label) getFellow("lServiceDays")).setValue(String.valueOf(Libs.getDiffDays(new SimpleDateFormat("yyyy-MM-dd").parse(serviceIn), new SimpleDateFormat("yyyy-MM-dd").parse(serviceOut))+1));
                ((Label) getFellow("tRemarks")).setValue(remarks);
            }
        } catch (Exception ex) {
            log.error("populateInformation", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    private void populatePlanItems() {
        lb.getItems().clear();
        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + "(a.hclmpcode1 + a.hclmpcode2) as plan_code, "
                    + Libs.createListFieldString("a.hclmcamt") + ", "
                    + Libs.createListFieldString("a.hclmaamt") + ", "
                    + Libs.createListFieldString("a.hclmaday") + ", "
                    + "a.hclmtclaim "
                    + "from idnhltpf.dbo.hltclm a "
                    + "where "
                    + "a.hclmcno='" + claimPOJO.getClaim_number() + "' and "
                    + "(convert(varchar,a.hclmyy)+'-'+convert(varchar,a.hclmbr)+'-'+convert(varchar,a.hclmdist)+'-'+convert(varchar,a.hclmpono))='" + claimPOJO.getPolicy_number() + "' and "
                    + "(convert(varchar,a.hclmidxno)+'-'+a.hclmseqno)='" + claimPOJO.getIndex() + "' and "
                    + "a.hclmcount=" + claimPOJO.getClaim_count();

            double totalProposed = 0;
            double totalApproved = 0;

            List<Object[]> l = s.createSQLQuery(qry).list();
            if (l.size()==1) {
                Object[] o = l.get(0);

                BenefitPOJO benefitPOJO = Libs.getBenefit(claimPOJO.getPolicy_number(), Libs.nn(o[0]));

                int i=0;
                for (String planItem : benefitPOJO.getPlan_items()) {
                    if (!planItem.trim().isEmpty() && Double.valueOf(Libs.nn(o[i+1]))>0) {
                        totalProposed += Double.valueOf(Libs.nn(o[i+1]));
                        totalApproved += Double.valueOf(Libs.nn(o[i+31]));

                        String remarks = Libs.loadAdvancedMemo(claimPOJO.getPolicy_number(), claimPOJO.getIndex(), claimPOJO.getClaim_count(), Libs.nn(o[91]), claimPOJO.getClaim_number(), planItem);

                        Listitem li = new Listitem();
                        li.appendChild(new Listcell(Libs.getBenefitItemDescription(planItem)));
                        li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[i+61])), "#"));
//                        li.appendChild(Libs.createNumericListcell(0, "#"));
                        li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[i+1])), "#,###.##"));
                        li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[i+31])), "#,###.##"));
                        li.appendChild(Libs.createNumericListcell(Double.valueOf(Libs.nn(o[i+1]))-Double.valueOf(Libs.nn(o[i+31])), "#,###.##"));
                        li.appendChild(Libs.createRemarksListcell(remarks, this));
                        lb.appendChild(li);

                        try {
                            File f = new File(Executions.getCurrent().getSession().getWebApp().getRealPath("/rules/" + claimPOJO.getPolicy().getPolicy_number() + "_Claim_Detail_Row.rule"));
                            if (f.exists()) {
                                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                                String rule = "";
                                while (br.ready()) {
                                    rule += br.readLine();
                                }
                                br.close();

                                Interpreter interpreter = new Interpreter();
                                interpreter.set("config", Libs.config);
                                interpreter.set("claim", claimPOJO);
                                interpreter.set("li", li);
                                interpreter.set("benefitCode", planItem);
                                interpreter.set("diagnosis", ((Label) getFellow("lDiagnosis")).getValue());
                                interpreter.eval(rule);
                            }
                        } catch (Exception ex) {
                            log.error("populatePlanItems", ex);
                        }
                    }
                    i++;
                }
            }

            ((Listfooter) getFellow("ftrTotalProposed")).setLabel(new DecimalFormat("#,###.##").format(totalProposed));
            ((Listfooter) getFellow("ftrTotalApproved")).setLabel(new DecimalFormat("#,###.##").format(totalApproved));
            ((Listfooter) getFellow("ftrTotalExcess")).setLabel(new DecimalFormat("#,###.##").format(totalProposed-totalApproved));

        } catch (Exception ex) {
            log.error("populatePlanItems", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }
    
    public void exportToXls(){
    	String[] columnsHeaderInap = new String[] {
                "NAME", "DATE OF BIRTH (AGE)", "SEX", "MARITAL STATUS", "CARD NUMBER", "COMPANY NAME",  "CLAIM TYPE", "PROVIDER",
                "DIAGNOSIS", "DIAGNOSIS DESCRIPTION", "REMARKS", "SERVICE IN", "SERVICE OUT", "SERVICES DAY"};
    	String[] detailHeaderInap = new String[]{
    			((Label) getFellow("lName")).getValue(), ((Label) getFellow("lDOB")).getValue() + " "+ ((Label) getFellow("lAge")).getValue(),
    			((Label) getFellow("lSex")).getValue(), ((Label) getFellow("lMaritalStatus")).getValue(),  ((Label) getFellow("lCardNumber")).getValue(),
    			((Label) getFellow("lCompanyName")).getValue(), ((Label) getFellow("lClaimType")).getValue(),  ((Label) getFellow("lProvider")).getValue(),
    			((Label) getFellow("lDiagnosis")).getValue(), ((Label) getFellow("lDescription")).getValue(),  ((Label) getFellow("tRemarks")).getValue(),
    			((Label) getFellow("lServiceIn")).getValue(),  ((Label) getFellow("lServiceOut")).getValue(),  ((Label) getFellow("lServiceDays")).getValue()
    	};
    	
    	String[] detailHeaderJalan = new String[]{
    			((Label) getFellow("lName")).getValue(), ((Label) getFellow("lDOB")).getValue() + " "+ ((Label) getFellow("lAge")).getValue(),
    			((Label) getFellow("lSex")).getValue(), ((Label) getFellow("lMaritalStatus")).getValue(),  ((Label) getFellow("lCardNumber")).getValue(),
    			((Label) getFellow("lCompanyName")).getValue(), ((Label) getFellow("lClaimType")).getValue(),  ((Label) getFellow("lProvider")).getValue(),
    			((Label) getFellow("lDiagnosis")).getValue(), ((Label) getFellow("lDescription")).getValue(),  ((Label) getFellow("tRemarks")).getValue(),
    			((Label) getFellow("lReceiptDate")).getValue()
    	};
    	
    	String[] columnsHeaderJalan = new String[] {
                "NAME", "DATE OF BIRTH (AGE)", "SEX", "MARITAL STATUS", "CARD NUMBER", "COMPANY NAME",  "CLAIM TYPE", "PROVIDER",
                "DIAGNOSIS", "DIAGNOSIS DESCRIPTION", "REMARKS", "RECEIPT DATE"};
    	
    	String[] columnsDetail = new String[] {
                "BENEFIT", "DAYS", "PROPOSE", "APPROVE", "EXCESS", "REMARKS"};
    	
    	try{
    		
    		 Workbook wb = new HSSFWorkbook();
             Sheet sheet = wb.createSheet("Claim History Detail");
             int cnt = 0;

    		
    		org.apache.poi.ss.usermodel.Row row = sheet.createRow(cnt);
    		if(getFellow("lTitleServiceIn").isVisible()){
    			for (int i=0; i<columnsHeaderInap.length; i++) {
                    Libs.createCell(row, i, columnsHeaderInap[i]);
                }
    			cnt++;
    			row = sheet.createRow(cnt);
    			for(int i=0; i < columnsHeaderInap.length; i++){
    				Libs.createCell(row, i, detailHeaderInap[i]);
    			}
    		}else{
    			for (int i=0; i<columnsHeaderJalan.length; i++) {
                    Libs.createCell(row, i, columnsHeaderJalan[i]);
                }
    			cnt++;
    			row = sheet.createRow(cnt);
    			for(int i=0; i < columnsHeaderJalan.length; i++){
    				Libs.createCell(row, i, detailHeaderJalan[i]);
    			}
    			
    		}
    		
    		cnt = cnt + 2;
    		row = sheet.createRow(cnt);
    		for(int i=0; i < columnsDetail.length; i++){
    			Libs.createCell(row, i, columnsDetail[i]);
    		}
    		
    		Doublebox db = new Doublebox();
    		db.setFormat("#,###.##");
    		for(Listitem item : lb.getItems()){
    			cnt++;
    			row = sheet.createRow(cnt);
    			for(int i=0; i < columnsDetail.length; i++){
        			Libs.createCell(row, i, ((Listcell)item.getChildren().get(i)).getLabel());
        		}
    		}
    		
    		String fn = "ClaimDetail-"+((Label) getFellow("lName")).getValue() + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";

            FileOutputStream out = new FileOutputStream(Libs.config.get("temp_dir").toString() + File.separator + fn);
            wb.write(out);
            out.close();

            Thread.sleep(5000);

            File f = new File(Libs.config.get("temp_dir").toString() + File.separator + fn);
            InputStream is = new FileInputStream(f);
            Filedownload.save(is, "application/vnd.ms-excel", fn);
            f.delete();
    		
    	}catch(Exception ex){
    		
    	}
    	
    	
    }
    
    
    private void downloadLembarAnalisa() throws Exception{
    	
    	try{
    		Workbook wb = new HSSFWorkbook();
       	 Map<String, CellStyle> styles = Libs.createStyles(wb);
       	 
       	 String[] titlesRj = {
       	            "No",	"Index", "Nama Pasien", "Nama\nKaryawan", "Starting\nDate", "Usia", "Plan", "Klaim\nKe", "Kode\nPenyakit",
       	            "Tanggal\nKwitansi", "Manfaat", "Diajukan", "Dibayar", "Kelebihan", "Keterangan"
       	    };
       	 
       	 String[] titlesRi = {"Manfaat", "Qty", "Diajukan", "Pagu", "Diganti", "Kelebihan", "Keterangan"};
       	 
       	 
       	 Sheet sheet = wb.createSheet("Lembar Analisa");
            PrintSetup printSetup = sheet.getPrintSetup();
            printSetup.setLandscape(true);
            sheet.setFitToPage(true);
            sheet.setHorizontallyCenter(true);
            
            counter = 0;
            
          //title row
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(counter);
            titleRow.setHeightInPoints(45);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            if(claimPOJO.getClaim_number().contains("OP")){
           	 titleCell.setCellValue("LEMBAR ANALISA RAWAT JALAN");
           	 sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:O$1"));
            }
           	 
            else if(claimPOJO.getClaim_number().contains("IP")){
           	 titleCell.setCellValue("LEMBAR ANALISA RAWAT INAP");
           	 sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:G$1"));
            }
           	 
            titleCell.setCellStyle(styles.get("title"));
            
            counter = counter + 1;
            
           
            
            if(claimPOJO.getClaim_number().contains("OP")){
            	 org.apache.poi.ss.usermodel.Row claimHeaderRow = sheet.createRow(counter);
                 org.apache.poi.ss.usermodel.Cell cellClaimHeader = claimHeaderRow.createCell(3);
                 sheet.addMergedRegion(CellRangeAddress.valueOf("$D$"+(counter+1)+":E$"+(counter+1)+""));
            	 String head = "Pemegang Polis ";//  :" + claimPOJO.getPolicy_number() +"\t Mulai Pertanggungan\t : "+"2014-01-01";
            	 cellClaimHeader.setCellValue(head);
              	 cellClaimHeader.setCellStyle(styles.get("header2"));
              	 
              	 cellClaimHeader = claimHeaderRow.createCell(5);
              	 cellClaimHeader.setCellValue(":");
             	 cellClaimHeader.setCellStyle(styles.get("header3"));
             	 
             	 cellClaimHeader = claimHeaderRow.createCell(6);
             	 sheet.addMergedRegion(CellRangeAddress.valueOf("$G$"+(counter+1)+":O$"+(counter+1)+""));
             	 cellClaimHeader.setCellValue(claimPOJO.getPolicy_number()+" ("+((Label) getFellow("lCompanyName")).getValue()+")");
            	 cellClaimHeader.setCellStyle(styles.get("header2"));
            	 
            	 counter = counter + 1;
            	 
            	 
            	 claimHeaderRow = sheet.createRow(counter);
            	 cellClaimHeader = claimHeaderRow.createCell(3);
            	 sheet.addMergedRegion(CellRangeAddress.valueOf("$D$"+(counter+1)+":E$"+(counter+1)+""));
            	 head = "Nomor Klaim ";
            	 cellClaimHeader.setCellValue(head);
              	 cellClaimHeader.setCellStyle(styles.get("header2"));
              	 
              	 cellClaimHeader = claimHeaderRow.createCell(5);
              	 cellClaimHeader.setCellValue(":");
             	 cellClaimHeader.setCellStyle(styles.get("header3"));
             	 
             	 cellClaimHeader = claimHeaderRow.createCell(6);
             	 sheet.addMergedRegion(CellRangeAddress.valueOf("$G$"+(counter+1)+":O$"+(counter+1)+""));
             	 cellClaimHeader.setCellValue(claimPOJO.getClaim_number());
            	 cellClaimHeader.setCellStyle(styles.get("header2"));
            	 counter = counter + 1;
            	 
            	 claimHeaderRow = sheet.createRow(counter);
            	 cellClaimHeader = claimHeaderRow.createCell(3);
            	 sheet.addMergedRegion(CellRangeAddress.valueOf("$D$"+(counter+1)+":E$"+(counter+1)+""));
            	 head = "Rumah Sakit ";
            	 cellClaimHeader.setCellValue(head);
              	 cellClaimHeader.setCellStyle(styles.get("header2"));
              	 
              	 cellClaimHeader = claimHeaderRow.createCell(5);
              	 cellClaimHeader.setCellValue(":");
             	 cellClaimHeader.setCellStyle(styles.get("header3"));
             	 
             	 cellClaimHeader = claimHeaderRow.createCell(6);
             	 sheet.addMergedRegion(CellRangeAddress.valueOf("$G$"+(counter+1)+":O$"+(counter+1)+""));
             	 cellClaimHeader.setCellValue(((Label) getFellow("lProvider")).getValue());
            	 cellClaimHeader.setCellStyle(styles.get("header2"));
            	 
            	 counter = counter + 1;
            	 
            	 
            	 
            }
              	 
            else if(claimPOJO.getClaim_number().contains("IP")){
            	 org.apache.poi.ss.usermodel.Row claimHeaderRow = sheet.createRow(counter);
                 org.apache.poi.ss.usermodel.Cell cellClaimHeader = claimHeaderRow.createCell(0);
            	 String head = "Pemegang Polis ";//  :" + claimPOJO.getPolicy_number() +"\t Mulai Pertanggungan\t : "+"2014-01-01";
            	 cellClaimHeader.setCellValue(head);
              	 cellClaimHeader.setCellStyle(styles.get("header2"));
              	 
              	 cellClaimHeader = claimHeaderRow.createCell(1);
              	 cellClaimHeader.setCellValue(":");
             	 cellClaimHeader.setCellStyle(styles.get("header3"));
             	 
             	 cellClaimHeader = claimHeaderRow.createCell(2);
             	 sheet.addMergedRegion(CellRangeAddress.valueOf("$C$"+(counter+1)+":G$"+(counter+1)+""));
             	 cellClaimHeader.setCellValue(claimPOJO.getPolicy_number()+" ("+((Label) getFellow("lCompanyName")).getValue()+")");
            	 cellClaimHeader.setCellStyle(styles.get("header2"));
            	 
            	 counter = counter + 1;
            	 
            	 claimHeaderRow = sheet.createRow(counter);
            	 cellClaimHeader = claimHeaderRow.createCell(0);
            	 head = "Nama Peserta ";
            	 cellClaimHeader.setCellValue(head);
              	 cellClaimHeader.setCellStyle(styles.get("header2"));
              	 
              	 cellClaimHeader = claimHeaderRow.createCell(1);
              	 cellClaimHeader.setCellValue(":");
             	 cellClaimHeader.setCellStyle(styles.get("header3"));
             	 
             	 cellClaimHeader = claimHeaderRow.createCell(2);
             	 sheet.addMergedRegion(CellRangeAddress.valueOf("$C$"+(counter+1)+":G$"+(counter+1)+""));
             	 cellClaimHeader.setCellValue(((Label) getFellow("lName")).getValue());
            	 cellClaimHeader.setCellStyle(styles.get("header2"));
            	 
            	 counter = counter + 1;
            	 
            	 claimHeaderRow = sheet.createRow(counter);
            	 cellClaimHeader = claimHeaderRow.createCell(0);
            	 head = "Nomor Klaim ";
            	 cellClaimHeader.setCellValue(head);
              	 cellClaimHeader.setCellStyle(styles.get("header2"));
              	 
              	 cellClaimHeader = claimHeaderRow.createCell(1);
              	 cellClaimHeader.setCellValue(":");
             	 cellClaimHeader.setCellStyle(styles.get("header3"));
             	 
             	 cellClaimHeader = claimHeaderRow.createCell(2);
             	 sheet.addMergedRegion(CellRangeAddress.valueOf("$C$"+(counter+1)+":G$"+(counter+1)+""));
             	 cellClaimHeader.setCellValue(claimPOJO.getClaim_number());
            	 cellClaimHeader.setCellStyle(styles.get("header2"));
            	 
            	 counter = counter + 1;
            	 claimHeaderRow = sheet.createRow(counter);
            	 cellClaimHeader = claimHeaderRow.createCell(0);
            	 head = "Tgl Perawatan ";
            	 cellClaimHeader.setCellValue(head);
              	 cellClaimHeader.setCellStyle(styles.get("header2"));
              	 
              	 cellClaimHeader = claimHeaderRow.createCell(1);
              	 cellClaimHeader.setCellValue(":");
             	 cellClaimHeader.setCellStyle(styles.get("header3"));
             	 
             	 cellClaimHeader = claimHeaderRow.createCell(2);
             	 sheet.addMergedRegion(CellRangeAddress.valueOf("$C$"+(counter+1)+":G$"+(counter+1)+""));
             	 cellClaimHeader.setCellValue(((Label) getFellow("lServiceIn")).getValue()+" s.d " + ((Label) getFellow("lServiceOut")).getValue());
            	 cellClaimHeader.setCellStyle(styles.get("header2"));
            	 counter = counter + 1;
            	 
            	 claimHeaderRow = sheet.createRow(counter);
            	 cellClaimHeader = claimHeaderRow.createCell(0);
            	 head = "Rumah Sakit ";
            	 cellClaimHeader.setCellValue(head);
              	 cellClaimHeader.setCellStyle(styles.get("header2"));
              	 
              	 cellClaimHeader = claimHeaderRow.createCell(1);
              	 cellClaimHeader.setCellValue(":");
             	 cellClaimHeader.setCellStyle(styles.get("header3"));
             	 
             	 cellClaimHeader = claimHeaderRow.createCell(2);
             	 sheet.addMergedRegion(CellRangeAddress.valueOf("$C$"+(counter+1)+":G$"+(counter+1)+""));
             	 cellClaimHeader.setCellValue(((Label) getFellow("lProvider")).getValue());
            	 cellClaimHeader.setCellStyle(styles.get("header2"));
            	 
            	 counter = counter + 1;
            	 
            	 claimHeaderRow = sheet.createRow(counter);
            	 cellClaimHeader = claimHeaderRow.createCell(0);
            	 head = "Kode Penyakit ";
            	 cellClaimHeader.setCellValue(head);
              	 cellClaimHeader.setCellStyle(styles.get("header2"));
              	 
              	 cellClaimHeader = claimHeaderRow.createCell(1);
              	 cellClaimHeader.setCellValue(":");
             	 cellClaimHeader.setCellStyle(styles.get("header3"));
             	 
             	 cellClaimHeader = claimHeaderRow.createCell(2);
             	 sheet.addMergedRegion(CellRangeAddress.valueOf("$C$"+(counter+1)+":G$"+(counter+1)+""));
             	 cellClaimHeader.setCellValue(((Label) getFellow("lDiagnosis")).getValue());
            	 cellClaimHeader.setCellStyle(styles.get("header2"));
            	 
            	 counter = counter + 1;
            	 
             	 
              	 
              	 
            }
            
            counter = counter + 1;
            
            //header row
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(counter);
            headerRow.setHeightInPoints(40);
            org.apache.poi.ss.usermodel.Cell headerCell;
            
            if(claimPOJO.getClaim_number().contains("OP")){
           	 for (int i = 0; i < titlesRj.length; i++) {
                    headerCell = headerRow.createCell(i);
                    headerCell.setCellValue(titlesRj[i]);
                    headerCell.setCellStyle(styles.get("header"));
                }
            }
            else{
           	 for (int i = 0; i < titlesRi.length; i++) {
                    headerCell = headerRow.createCell(i);
                    headerCell.setCellValue(titlesRi[i]);
                    headerCell.setCellStyle(styles.get("header"));
                }
            }
            
            
            counter = counter + 1;
            
            fillDetail(sheet, styles);
            
            //add remarks if inpatient claim
            if(claimPOJO.getClaim_number().contains("IP")){
            	counter = counter + 1;
            	org.apache.poi.ss.usermodel.Row row = sheet.createRow(counter);
            	org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
            	sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(counter+1)+":G$"+(counter+1)+""));
            	cell.setCellValue("Remarks : ");
            	cell.setCellStyle(styles.get("cell2"));
            	
            	counter = counter + 1;
            	
            	row = sheet.createRow(counter);
            	row.setHeightInPoints(50);
            	cell = row.createCell(0);
            	cell.setCellStyle(styles.get("cell2"));
            	sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(counter+1)+":G$"+(counter+1)+""));
            	cell.setCellValue(((Label)getFellow("tRemarks")).getValue()+"\r");
            	
            	
            	counter = counter + 1;

            }
            
            counter = counter + 1;
            
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(counter);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
            if(claimPOJO.getClaim_number().contains("OP"))
            	sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(counter+1)+":O$"+(counter+1)+""));
            else
            	sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(counter+1)+":G$"+(counter+1)+""));
            cell.setCellValue("Catatan :");
            cell.setCellStyle(styles.get("cell2"));
            
            counter = counter + 1;
            
            row = sheet.createRow(counter);
            cell = row.createCell(0);
            if(claimPOJO.getClaim_number().contains("OP"))
            	sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(counter+1)+":O$"+(counter+1)+""));
            else
            	sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(counter+1)+":G$"+(counter+1)+""));
            cell.setCellValue("-Lembar Analisa Klaim ini resmi dikeluarkan oleh komputer dan sah tanpa tanda tangan");
            cell.setCellStyle(styles.get("cell2"));
            
            counter = counter + 1;
            
            row = sheet.createRow(counter);
            cell = row.createCell(0);
            if(claimPOJO.getClaim_number().contains("OP"))
            	sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(counter+1)+":O$"+(counter+1)+""));
            else 
            	sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(counter+1)+":I$"+(counter+1)+""));
            cell.setCellValue("-IMCare setiap saat berhak mengadakan koreksi apabila ada kesalahan pada Lembar Analisa Klaim ini");
            cell.setCellStyle(styles.get("cell2"));
            
            
            
            if(claimPOJO.getClaim_number().contains("OP")){
            	for(int i=0; i < titlesRj.length; i++){
            		sheet.autoSizeColumn(i);
            	}
            }else{
            	for(int i=0; i < titlesRi.length; i++){
            		sheet.autoSizeColumn(i);
            	}
            }
            
            
            String fn = "lembarAnalisa-"+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";

            FileOutputStream out = new FileOutputStream(Libs.config.get("temp_dir").toString() + File.separator + fn);
            wb.write(out);
            out.close();

            Thread.sleep(5000);

            File f = new File(Libs.config.get("temp_dir").toString() + File.separator + fn);
            InputStream is = new FileInputStream(f);
            Filedownload.save(is, "application/vnd.ms-excel", fn);
            f.delete();

    	}catch(Exception e){
    		
    	}
    	 
    }

	private void fillDetail(Sheet sheet, Map<String, CellStyle> styles) {
		 Session s = Libs.sfDB.openSession();
		 try{
			 int nomor = 1;
			 org.apache.poi.ss.usermodel.Cell mycell;
			 
			String qry = "select  "
					   + Libs.createListFieldString("a.hclmcamt") + ", "
					   + Libs.createListFieldString("a.hclmaamt") + ", "
					   + Libs.createListFieldString("a.hclmaday") + ", "
					   + "convert(varchar,a.HCLMIDXNO)+ a.HCLMSEQNO as idx, c.hdt1name as patientName, " //91
					   + "(select HDT1NAME from idnhltpf.dbo.hltdt1 g where g.HDT1YY=a.HCLMYY and g.HDT1BR=a.HCLMBR "
					   + "and g.HDT1DIST=a.HCLMDIST and g.HDT1PONO=a.HCLMPONO and g.HDT1IDXNO=a.HCLMIDXNO and g.HDT1SEQNO='A' and g.HDT1CTR=0) as employeeName, " 
					   + "convert(varchar,a.hclmsinyy)+'-'+CONVERT(varchar,a.hclmsinmm)+'-' +CONVERT(varchar,a.hclmsindd) as sindate, " 
					   + "convert(varchar,a.hclmsoutyy)+'-'+CONVERT(varchar,a.hclmsoutmm)+'-'+convert(varchar,a.hclmsoutdd) as soutdate, " 
					   + "convert(varchar,a.hclmrdatey)+'-'+CONVERT(varchar,a.hclmrdatem)+'-'+convert(varchar,a.hclmrdated) as rdate, "
					   + "convert(varchar,f.HDT2SDTYY)+'-'+CONVERT(varchar,f.HDT2SDTMM)+'-'+convert(varchar,f.HDT2SDTDD) as startingDate, "
					   + "(a.hclmpcode1 + a.hclmpcode2) as plan_code, "  //97
					   + "datediff(year,convert(datetime,convert(varchar,c.hdt1bdtyy)+'-'+CONVERT(varchar,c.hdt1bdtmm)+'-'+CONVERT(varchar,c.hdt1bdtdd),110), GETDATE()) as age, "
					   + "a.hclmdiscd1, a.hclmdiscd2, a.hclmdiscd3, b.hproname, a.hclmtclaim, " //103
					   + "c.hdt1sex, c.hdt1ncard, a.hclmcount, "//106
					   + "c.hdt1mstat, d.hhdrname, e.hmem2data1, e.hmem2data2, e.hmem2data3, e.hmem2data4 "
					   + "from idnhltpf.dbo.hltclm a "
					   + "inner join idnhltpf.dbo.hltpro b on b.hpronomor=a.hclmnhoscd "
					   + "inner join idnhltpf.dbo.hltdt1 c on c.hdt1yy=a.hclmyy and c.HDT1BR=a.HCLMBR and c.HDT1DIST=a.HCLMDIST and c.hdt1pono=a.hclmpono "
					   + "and c.hdt1idxno=a.hclmidxno and c.hdt1seqno=a.hclmseqno and c.hdt1ctr=0 "
					   + "inner join IDNHLTPF.dbo.hltdt2 f on c.HDT1YY=f.HDT2YY and c.HDT1BR=f.HDT2BR and c.HDT1DIST=f.HDT2DIST and c.HDT1PONO=f.HDT2PONO "
					   + "and c.HDT1IDXNO=f.HDT2IDXNO and c.HDT1SEQNO=f.HDT2SEQNO and c.HDT1CTR=f.HDT2CTR "
					   + "inner join idnhltpf.dbo.hlthdr d on d.hhdryy=a.hclmyy and d.HHDRBR=a.HCLMBR and d.HHDRDIST=a.HCLMDIST and d.hhdrpono=a.hclmpono "
					   + "left outer join idnhltpf.dbo.hltmemo2 e on e.hmem2yy=a.hclmyy and e.HMEM2BR=a.HCLMBR and e.HMEM2DIST=a.HCLMDIST and e.hmem2pono=a.hclmpono "
					   + "and e.hmem2idxno=a.hclmidxno and e.hmem2seqno=a.hclmseqno and e.hmem2claim=a.hclmtclaim and e.hmem2count=a.hclmcount "
					   + "where a.hclmcno='"+claimPOJO.getClaim_number()+"' ";
			
			
			List<Object[]> l = s.createSQLQuery(qry).list();
			String plan = null;
			double totalProposed = 0;
			double totalApproved = 0;
			double totalExcess = 0;
			
			for (Object[] o : l){
				for(int i=0; i < 30; i++){
					if(Double.valueOf(Libs.nn(o[i]))>0){
						  org.apache.poi.ss.usermodel.Row row = sheet.createRow(counter);
						  
						  if(claimPOJO.getClaim_number().contains("OP")){
							  mycell = row.createCell(0);
							  mycell.setCellValue(nomor+"");
							  mycell.setCellStyle(styles.get("cell"));
							  
							  mycell = row.createCell(1); //idx
							  mycell.setCellValue(Libs.nn(o[90]));
							  mycell.setCellStyle(styles.get("cell"));
							  
							  mycell = row.createCell(2); //patient name
							  mycell.setCellValue(Libs.nn(o[91]));
							  mycell.setCellStyle(styles.get("cell"));
							  
							  mycell = row.createCell(3); //employee name
							  mycell.setCellValue(Libs.nn(o[92]));
							  mycell.setCellStyle(styles.get("cell"));  
							  
							  mycell = row.createCell(4); //starting date
							  mycell.setCellValue(Libs.nn(o[96]));
							  mycell.setCellStyle(styles.get("cell"));  
							  
							  mycell = row.createCell(5); //age
							  mycell.setCellValue(Libs.nn(o[98]));
							  mycell.setCellStyle(styles.get("cell"));  
							  
							  mycell = row.createCell(6); //plan
							  plan = Libs.nn(o[97]);
							  mycell.setCellValue(plan);
							  mycell.setCellStyle(styles.get("cell"));
							  
							  mycell = row.createCell(8); //icd code
							  String icd = Libs.nn(o[99]).trim(); if(!Libs.nn(o[100]).trim().equalsIgnoreCase("")) icd = icd + ","+Libs.nn(o[100]).trim();if(!Libs.nn(o[101]).trim().equalsIgnoreCase("")) icd = icd + ","+Libs.nn(o[101]).trim();
							  mycell.setCellValue(icd);
							  mycell.setCellStyle(styles.get("cell"));  
							  
							  mycell = row.createCell(7); //claim counter
							  mycell.setCellValue(Libs.nn(o[106]));
							  mycell.setCellStyle(styles.get("cell"));
							  
							  mycell = row.createCell(9); //bill date
							  mycell.setCellValue(Libs.nn(o[95]));
							  mycell.setCellStyle(styles.get("cell"));
							  
							  mycell = row.createCell(10); //Benefit
							  Object[] obj = Libs.getBenefit(claimPOJO.getPolicy_number(), plan, i+1);
							  mycell.setCellValue(obj[2].toString());
							  mycell.setCellStyle(styles.get("cell"));
							  
							  totalProposed = totalProposed + Double.valueOf(Libs.nn(o[i])).doubleValue();
							  mycell = row.createCell(11); //proposed
							  mycell.setCellValue(Double.valueOf(Libs.nn(o[i])).doubleValue());
							  mycell.setCellStyle(styles.get("cell_angka"));
							  
							  totalApproved = totalApproved + Double.valueOf(Libs.nn(o[i+30])).doubleValue();
							  mycell = row.createCell(12); //approved
							  mycell.setCellValue(Double.valueOf(Libs.nn(o[i+30])).doubleValue());
							  mycell.setCellStyle(styles.get("cell_angka"));
							  
							  totalExcess = totalExcess + (Double.valueOf(Libs.nn(o[i])).doubleValue()-Double.valueOf(Libs.nn(o[i+30])).doubleValue());
							  mycell = row.createCell(13); //excess
							  mycell.setCellValue(Double.valueOf(Libs.nn(o[i])).doubleValue()-Double.valueOf(Libs.nn(o[i+30])).doubleValue());
							  mycell.setCellStyle(styles.get("cell_angka"));
							  
							  mycell = row.createCell(14); //ket
							  mycell.setCellValue(Libs.nn(o[109]).trim()+Libs.nn(o[110]).trim()+Libs.nn(o[111]).trim()+Libs.nn(o[112]).trim());
							  mycell.setCellStyle(styles.get("cell"));
							  
						  }
						  else{
							  
							  plan = Libs.nn(o[97]);
							  
							  mycell = row.createCell(0); //Benefit
							  Object[] obj = Libs.getBenefit(claimPOJO.getPolicy_number(), plan, i+1);
							  mycell.setCellValue(obj[2].toString());
							  mycell.setCellStyle(styles.get("cell"));  
							  
							  mycell = row.createCell(1); //days
							  mycell.setCellValue(Double.valueOf(Libs.nn(o[i+60])).doubleValue());
							  mycell.setCellStyle(styles.get("cell_angka"));
							  
							  totalProposed = totalProposed + Double.valueOf(Libs.nn(o[i])).doubleValue();
							  mycell = row.createCell(2); //proposed
							  mycell.setCellValue(Double.valueOf(Libs.nn(o[i])).doubleValue());
							  mycell.setCellStyle(styles.get("cell_angka"));
							  
							  mycell = row.createCell(3); //pagu
							  mycell.setCellValue(Double.valueOf(obj[1].toString()).doubleValue());
							  mycell.setCellStyle(styles.get("cell_angka"));
							  
							  totalApproved = totalApproved + Double.valueOf(Libs.nn(o[i+30])).doubleValue();
							  mycell = row.createCell(4); //approved
							  mycell.setCellValue(Double.valueOf(Libs.nn(o[i+30])).doubleValue());
							  mycell.setCellStyle(styles.get("cell_angka"));
							  
							  totalExcess = totalExcess +(Double.valueOf(Libs.nn(o[i])).doubleValue()-Double.valueOf(Libs.nn(o[i+30])).doubleValue());
							  mycell = row.createCell(5); //excess
							  mycell.setCellValue(Double.valueOf(Libs.nn(o[i])).doubleValue()-Double.valueOf(Libs.nn(o[i+30])).doubleValue());
							  mycell.setCellStyle(styles.get("cell_angka"));
							  
							  String remarks = Libs.loadAdvancedMemo(claimPOJO.getPolicy_number(), Libs.nn(o[90]), Integer.parseInt(Libs.nn(o[106])), Libs.nn(o[91]), claimPOJO.getClaim_number(), obj[0].toString());
							  
							  mycell = row.createCell(6); //ket
							  mycell.setCellValue(remarks);
							  mycell.setCellStyle(styles.get("cell"));
							  
							  
						  }
						  
						  counter = counter + 1;
						  nomor = nomor + 1;
					}
				}
			}
			
			counter = counter + 1;
			
			org.apache.poi.ss.usermodel.Row row = sheet.createRow(counter);
			
			if(claimPOJO.getClaim_number().contains("OP")){
				mycell = row.createCell(10);
				mycell.setCellValue("TOTAL ");
				mycell.setCellStyle(styles.get("header2"));
				
				mycell = row.createCell(11); //proposed
				mycell.setCellValue(totalProposed);
				mycell.setCellStyle(styles.get("cell_angka_bold"));
				  
				
				mycell = row.createCell(12); //approved
				mycell.setCellValue(totalApproved);
				mycell.setCellStyle(styles.get("cell_angka_bold"));
				  
				mycell = row.createCell(13); //excess
				mycell.setCellValue(totalExcess);
				mycell.setCellStyle(styles.get("cell_angka_bold"));
				
				counter = counter + 1;
			}else{
				mycell = row.createCell(0);
				mycell.setCellValue("TOTAL ");
				mycell.setCellStyle(styles.get("header2"));
				
				mycell = row.createCell(2); //proposed
				mycell.setCellValue(totalProposed);
				mycell.setCellStyle(styles.get("cell_angka_bold"));
				  
				
				mycell = row.createCell(4); //approved
				mycell.setCellValue(totalApproved);
				mycell.setCellStyle(styles.get("cell_angka_bold"));
				  
				mycell = row.createCell(5); //excess
				mycell.setCellValue(totalExcess);
				mycell.setCellStyle(styles.get("cell_angka_bold"));
				
				counter = counter + 1;
			}
			
		 }catch(Exception e){
			 log.error("fillDetail", e); 
		 }finally{
			 if (s!=null && s.isOpen()) s.close();
		 }
			
		
	}


}
