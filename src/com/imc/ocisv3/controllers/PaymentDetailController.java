package com.imc.ocisv3.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.imc.ocisv3.tools.Libs;

/**
 * Created by Arifullah Ibn Rusyd on 06 Nov 2014.
 */
public class PaymentDetailController extends Window {

    private Logger log = LoggerFactory.getLogger(PaymentDetailController.class);

    private Rows paymentRows;
    private Rows costRows;
    private Label totalClaim;
    private Label totalCost;
    private Label total;
    boolean isProvider = false;
    
    String insid="";
    List products = null;
    
    Object[] payment;
    int counter = 0;
    String checqueNo;

    public void onCreate() {
        if (!Libs.checkSession()) {
            payment = (Object[])getAttribute("payment");
          
            
            products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
            
            
            checqueNo = Libs.nn(payment[2]);
            if(Libs.nn(payment[6]).equalsIgnoreCase("Y")) isProvider = true;
            initComponents();
            populate();
        }
    }

    private void initComponents() {
        paymentRows = (Rows) getFellow("paymentRows");
        costRows = (Rows)getFellow("costRows");
        totalClaim = (Label)getFellow("totalClaim");
        totalCost = (Label)getFellow("totalCost");
        total = (Label)getFellow("total");
        getCaption().setLabel("Payment Detail [" + Libs.nn(payment[2]) + "]");
        
     }

    private void populate() {
        populateClaim();
        populateCost();
        
        Doublebox db = new Doublebox();
		db.setFormat("#,###.##");
		
		db.setText(totalClaim.getValue());
        double totalClaim = db.getValue();
        
        db.setText(totalCost.getValue());
        double totalCost = db.getValue();
        
        double total = totalClaim + totalCost;
        
        db.setValue(total);
        
        String sign = "";
        if(totalCost >= 0) sign = " + ";
        
        this.total.setValue("Total : "+this.totalClaim.getValue() + sign + this.totalCost.getValue() + " = "+db.getText()); 
    }

    private void populateClaim() {
    	paymentRows.getChildren().clear();
    	Session s = Libs.sfDB.openSession();
    	String[] cqNo = checqueNo.split(" ");
    	try{
    		
    		String qry = "select  " 
    				   + "SUM(c.HCLMCAMT1+c.HCLMCAMT2+c.HCLMCAMT3+c.HCLMCAMT4+c.HCLMCAMT5+ "
    				   + "c.HCLMCAMT6+c.HCLMCAMT7+c.HCLMCAMT8+c.HCLMCAMT9+c.HCLMCAMT10+c.HCLMCAMT11+c.HCLMCAMT12+ "
    				   + "c.HCLMCAMT13+c.HCLMCAMT14+c.HCLMCAMT15+c.HCLMCAMT16+c.HCLMCAMT17+c.HCLMCAMT18+c.HCLMCAMT19+ "
    				   + "c.HCLMCAMT20+c.HCLMCAMT21+c.HCLMCAMT22+c.HCLMCAMT23+c.HCLMCAMT24+c.HCLMCAMT25+c.HCLMCAMT26+ "
    				   + "c.HCLMCAMT27+c.HCLMCAMT28+c.HCLMCAMT29+c.HCLMCAMT30) as proposed, "
    				   + "SUM(c.HCLMAAMT1+c.HCLMAAMT2+c.HCLMAAMT3+c.HCLMAAMT4+c.HCLMAAMT5+ "
    				   + "c.HCLMAAMT6+c.HCLMAAMT7+c.HCLMAAMT8+c.HCLMAAMT9+c.HCLMAAMT10+c.HCLMAAMT11+c.HCLMAAMT12+ "
    				   + "c.HCLMAAMT13+c.HCLMAAMT14+c.HCLMAAMT15+c.HCLMAAMT16+c.HCLMAAMT17+c.HCLMAAMT18+c.HCLMAAMT19+ "
    				   + "c.HCLMAAMT20+c.HCLMAAMT21+c.HCLMAAMT22+c.HCLMAAMT23+c.HCLMAAMT24+c.HCLMAAMT25+c.HCLMAAMT26+ "
    				   + "c.HCLMAAMT27+c.HCLMAAMT28+c.HCLMAAMT29+c.HCLMAAMT30) as approved, dt.HDT1NAME, dt.HDT1NCARD, "
    				   + "(convert(varchar,c.HCLMYY)+'-'+CONVERT(varchar,c.HCLMBR)+'-'+CONVERT(varchar,c.HCLMDIST)+'-'+ CONVERT(varchar,c.HCLMPONO)) as polis, "
    				   + "(convert(varchar,c.HCLMIDXNO) + c.HCLMSEQNO) as idx, p.HPRONAME, h.HHDRNAME, c.HCLMTCLAIM, c.HCLMCOUNT "
    				   + "from IDNHLTPF.dbo.hltovc v "
    				   + "inner join IDNHLTPF.dbo.hltclm c on v.HOVCCNO=c.HCLMNOMOR "
    				   + "inner join IDNHLTPF.dbo.HLTPRO p on c.HCLMNHOSCD=p.HPRONOMOR "
    				   + "inner join IDNHLTPF.dbo.HLTHDR h on c.HCLMYY=h.HHDRYY and c.HCLMBR=h.HHDRBR and c.HCLMDIST=h.HHDRDIST and c.HCLMPONO=h.HHDRPONO "
    				   + "inner join IDNHLTPF.dbo.hltdt1 dt on c.HCLMYY=dt.HDT1YY and c.HCLMBR=dt.HDT1BR and c.HCLMDIST=dt.HDT1DIST "
    				   + "and c.HCLMPONO=dt.HDT1PONO and c.HCLMIDXNO=dt.HDT1IDXNO and c.HCLMSEQNO=dt.HDT1SEQNO and dt.HDT1CTR=0 "
    				   + "where v.HOVCCQNO='"+cqNo[1]+"' and v.HOVCTYP='"+cqNo[0]+"' and c.HCLMRECID='P' "
    				   + "group by dt.HDT1NAME, dt.HDT1NCARD, "
    				   + "(convert(varchar,c.HCLMYY)+'-'+CONVERT(varchar,c.HCLMBR)+'-'+CONVERT(varchar,c.HCLMDIST)+'-'+ CONVERT(varchar,c.HCLMPONO)), "
    				   + "(convert(varchar,c.HCLMIDXNO) + c.HCLMSEQNO), p.HPRONAME, p.HPRONAME, h.HHDRNAME, c.HCLMTCLAIM, c.HCLMCOUNT";
    		
    		Doublebox db = new Doublebox();
    		db.setFormat("#,###.##");
    		
    		Double total = 0.0;
    		
    		 List<Object[]> l = s.createSQLQuery(qry).list();
    		 int number = 1;
    		 for(Object[] o : l){
    			 Row row = new Row();
    			 
    			 Cell cell = new Cell();
    			 Label lbl = new Label(number+"");
    			 cell.appendChild(lbl);
    			 row.appendChild(cell);
    			 
    			 BigDecimal proposed = (BigDecimal)o[0];
    			 BigDecimal approved = (BigDecimal)o[1];
    			 
    			 if(isProvider) {
    				 db.setValue(proposed.doubleValue());
    				 total = total + proposed.doubleValue();
    			 }
    			 else {
    				 db.setValue(approved.doubleValue());
    				 total = total + approved.doubleValue();
    			 }
    			 
    			 cell = new Cell();
    			 cell.setAlign("right");
    			 lbl = new Label(db.getText()); //total claim
    			 cell.appendChild(lbl);
    			 row.appendChild(cell);
    			 
    			 cell = new Cell();
    			 lbl = new Label(Libs.nn(o[2])); //name
    			 cell.appendChild(lbl);
    			 row.appendChild(cell);
    			 
    			 cell = new Cell();
    			 lbl = new Label(Libs.nn(o[3])); //card
    			 cell.appendChild(lbl);
    			 row.appendChild(cell);
    			 
    			 cell = new Cell();
    			 lbl = new Label(Libs.nn(o[7])); //polis name
    			 cell.appendChild(lbl);
    			 row.appendChild(cell);
    			 
    			 cell = new Cell();
    			 lbl = new Label(Libs.nn(o[4])); //polis
    			 cell.appendChild(lbl);
    			 row.appendChild(cell);
    			 
    			 cell = new Cell();
    			 lbl = new Label(Libs.nn(o[5])); //index
    			 cell.appendChild(lbl);
    			 row.appendChild(cell);
    			 
    			 cell = new Cell();
    			 lbl = new Label(Libs.getClaimType(Libs.nn(o[8]))); //claim type
    			 cell.appendChild(lbl);
    			 row.appendChild(cell);
    			 
    			 cell = new Cell();
    			 lbl = new Label(Libs.nn(o[9])); //counter
    			 cell.appendChild(lbl);
    			 row.appendChild(cell);
    			 
    			 cell = new Cell();
    			 lbl = new Label(Libs.nn(o[6])); //hospital
    			 cell.appendChild(lbl);
    			 row.appendChild(cell);
    			 
    			 number = number + 1;
    			 
    			 paymentRows.appendChild(row);
    		 }
    		 
    		 db.setValue(total);
    		 totalClaim.setValue(db.getText());
			
		}catch(Exception e){
			log.error("populateClaim",e);
		}finally{
			if (s!=null && s.isOpen()) s.close();
		}
		
	}

	private void populateCost() {
		costRows.getChildren().clear();
		Session s = Libs.sfDB.openSession();
		
		Doublebox db = new Doublebox();
		db.setFormat("#,###.##");
		
		double totalCost = 0.0;
		
		try{
			String qry = "select ltrim(rtrim(Cat)) as cat, Uraian, Nominal from idnhltpf.dbo.FinPemakaianDanaByTrans where CheckNo='"+checqueNo+"' and Flg='1' and InsId ";
			if(!insid.equals("")) qry = qry + " in  ("+insid+") ";
    		else qry = qry + "='" + Libs.getInsuranceId() + "' "; 
			List<Object[]> l = s.createSQLQuery(qry).list();
			for(Object[] o : l){
				 Row row = new Row();
				Cell cell = new Cell();
    			Label lbl = new Label(Libs.nn(o[0])); //cat
    			 cell.appendChild(lbl);
    			 row.appendChild(cell);
    			 
    			 cell = new Cell();
    			 lbl = new Label(Libs.nn(o[1])); //description
    			 cell.appendChild(lbl);
    			 row.appendChild(cell);
    			 
    			 db.setValue(((BigDecimal)o[2]).doubleValue());
    			 cell = new Cell();
    			 lbl = new Label(db.getText()); //amount 
    			 cell.appendChild(lbl);
    			 row.appendChild(cell);
    			 if(Libs.nn(o[0]).equalsIgnoreCase("Biaya Meterai"))totalCost = totalCost - db.getValue().doubleValue();
    			 else totalCost = totalCost + db.getValue().doubleValue();
    			 
    			 costRows.appendChild(row);
			}
			
			db.setValue(totalCost);
			this.totalCost.setValue(db.getText());
			
		}catch(Exception e){
			log.error("populateCost",e);
		}finally{
			if (s!=null && s.isOpen()) s.close();
		}
		
	}


}
