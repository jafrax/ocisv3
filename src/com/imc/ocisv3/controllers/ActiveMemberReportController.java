package com.imc.ocisv3.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
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
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

import com.imc.ocisv3.tools.Libs;

@SuppressWarnings("serial")
public class ActiveMemberReportController extends SelectorComposer<Window>{
	
	private Logger log = LoggerFactory.getLogger(ActiveMemberReportController.class);
	
	@Wire private Listbox lb;
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		populateActiveMember();
	}
	
	private int getTotalPolicy(String hinsid){
		Session s = Libs.sfDB.openSession();
		int hasil = 0;
		try{
			String sql = "select COUNT(1) from IDNHLTPF.dbo.HLTHDR HLTHDR where hlthdr.HHDRINSID=:insuranceId "
					   + "and convert(datetime, convert(varchar,HHDRMDTMM)+'-'+convert(varchar,HHDRMDTDD)+'-'+convert(varchar,HHDRMDTYY), 110) >= GETDATE() ";
			
			SQLQuery q = s.createSQLQuery(sql);
			q.setString("insuranceId", hinsid);
			
			Integer result  = (Integer)q.uniqueResult();
			hasil = result.intValue();

		}catch(Exception e){
			log.error("getTotalPolicy", e);
		}finally{
			if (s!=null && s.isOpen()) s.close();
		}
		
		return hasil;
	}
	
	private void populateActiveMember() {
		lb.getItems().clear();
		Session s = Libs.sfDB.openSession();
		
		try{
			String query = "SELECT HLTINS.HINSNAME, HLTINS.HINSID, COUNT(1) as member "
					     + "FROM IDNHLTPF.dbo.HLTDT2 HLTDT2 , IDNHLTPF.dbo.HLTHDR HLTHDR, IDNHLTPF.dbo.HLTINS HLTINS, "
					     + "IDNHLTPF.dbo.HLTDT1 HLTDT1, IDNHLTPF.dbo.HLTEMP HLTEMP "
					     + "WHERE "
					     + "convert(datetime, convert(varchar,HLTDT2.HDT2ADTMM)+'-'+convert(varchar,HLTDT2.HDT2ADTDD)+'-'+convert(varchar,HLTDT2.HDT2ADTYY), 110) <= GETDATE() "
					     + "and convert(datetime, convert(varchar,HLTDT2.HDT2MDTMM)+'-'+convert(varchar,HLTDT2.HDT2MDTDD)+'-'+convert(varchar,HLTDT2.HDT2MDTYY), 110) >= GETDATE() "
					     + "and HLTDT2.hdt2ctr=0 AND HLTDT2.hdt2PONO<>99999 AND HLTDT2.hdt2IDXNO < 99989 and HLTDT2.HDT2YY=HLTHDR.HHDRYY AND HLTDT2.HDT2BR=HLTHDR.HHDRBR "
					     + "AND HLTDT2.HDT2DIST=HLTHDR.HHDRDIST AND HLTDT2.HDT2PONO=HLTHDR.HHDRPONO AND HLTHDR.HHDRINSID = HLTINS.HINSID AND HLTDT2.HDT2YY=HLTDT1.HDT1YY "
					     + "AND HLTDT2.HDT2BR=HLTDT1.HDT1BR AND HLTDT2.HDT2DIST=HLTDT1.HDT1DIST AND HLTDT2.HDT2PONO=HLTDT1.HDT1PONO AND HLTDT2.HDT2IDXNO=HLTDT1.HDT1IDXNO "
					     + "AND HLTDT2.HDT2SEQNO=HLTDT1.HDT1SEQNO AND HLTDT2.HDT2CTR=HLTDT1.HDT1CTR AND HLTDT2.HDT2YY=HLTEMP.HEMPYY AND HLTDT2.HDT2BR=HLTEMP.HEMPBR AND "
					     + "HLTDT2.HDT2DIST=HLTEMP.HEMPDIST AND HLTDT2.HDT2PONO=HLTEMP.HEMPPONO AND HLTDT2.HDT2IDXNO=HLTEMP.HEMPIDXNO AND HLTDT2.HDT2SEQNO=HLTEMP.HEMPSEQNO "
					     + "AND HLTDT2.HDT2CTR=HLTEMP.HEMPCTR AND HLTDT2.hdt2moe NOT in('M','U','C') "
					     + "GROUP BY HLTINS.HINSNAME, HLTINS.HINSID ORDER BY HLTINS.HINSNAME";


			int total = 0;
			
			
			
			
			
			Integer jumlah = null;
			
			 List<Object[]> l = s.createSQLQuery(query).list();
			 int nomor = 1;
			 Listitem li;
			 for(Object[] o : l){
				 li = new Listitem();
				 li.appendChild(new Listcell(nomor+""));
				 li.appendChild(new Listcell(Libs.nn(o[0])));
//				 li.appendChild(new Listcell(getTotalPolicy(Libs.nn(o[1]))+""));
				 
				 jumlah = (Integer)o[2];
				 total = total + jumlah.intValue();
				 li.appendChild(new Listcell(jumlah.toString()));
				 
				 nomor = nomor + 1;
				 lb.appendChild(li);
			 }
			 
			 li = new Listitem();
			 li.appendChild(new Listcell(""));
			 li.appendChild(new Listcell("TOTAL"));
//			 li.appendChild(new Listcell());
			 li.appendChild(new Listcell(total+""));
			 
			 lb.appendChild(li);

			
		}catch(Exception e){
			log.error("populateActiveMember",e);
		}finally{
			if (s!=null && s.isOpen()) s.close();
		}
		
	}
	
	@Listen("onClick=#export")
	public void export(){
		try{
			String[] titles = new String[]{"No", "Client Name", "Active Member"};
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			String judul = "Active Member Per "+sdf.format(new Date());
			
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
	        sheet.addMergedRegion(CellRangeAddress.valueOf("$A$"+(counter+1)+":C$"+(counter+1)+""));
	        titleCell.setCellValue(judul);
	        titleCell.setCellStyle(styles.get("header3"));
	        
	        counter = counter + 2;
	        
	        titleRow = sheet.createRow(counter);
	        for(int i=0; i < titles.length; i++){
	        	titleCell = titleRow.createCell(i);
	        	titleCell.setCellValue(titles[i]);
	        	titleCell.setCellStyle(styles.get("header"));
	        }
	        
	        counter = counter + 1;
	        
	        for(Listitem item : lb.getItems()){
	        	row = sheet.createRow(counter);
				
	        	Listcell cell = (Listcell)item.getChildren().get(0);
				mycell = row.createCell(0); //no
				mycell.setCellValue(cell.getLabel());
				mycell.setCellStyle(styles.get("cell"));
				  	
				cell = (Listcell) item.getChildren().get(1);
				
				mycell = row.createCell(1); //Client Name
			  	mycell.setCellValue(cell.getLabel());
			  	mycell.setCellStyle(styles.get("cell"));
			  	
			  	cell = (Listcell) item.getChildren().get(2);
			  	mycell = row.createCell(2); //member active
				mycell.setCellValue(Double.valueOf(cell.getLabel()));
				mycell.setCellStyle(styles.get("cell_angka"));
				
				counter = counter + 1;
	        }
	        
	        for(int i=0; i < titles.length; i++){
	    		sheet.autoSizeColumn(i);
	    	}
			
			
			String fn = "Active_Member-"+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";

	        FileOutputStream out = new FileOutputStream(Libs.config.get("temp_dir").toString() + File.separator + fn);
	        wb.write(out);
	        out.close();

	        Thread.sleep(5000);

	        File f = new File(Libs.config.get("temp_dir").toString() + File.separator + fn);
	        InputStream is = new FileInputStream(f);
	        Filedownload.save(is, "application/vnd.ms-excel", fn);
	        f.delete();

			
		}catch(Exception e){
			log.error("export",e);
		}
		
	}

}
