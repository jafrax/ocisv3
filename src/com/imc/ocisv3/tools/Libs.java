package com.imc.ocisv3.tools;

import com.imc.ocisv3.pojos.BenefitPOJO;
import com.imc.ocisv3.pojos.ClaimPOJO;
import com.imc.ocisv3.pojos.MemberPOJO;
import com.imc.ocisv3.pojos.PolicyPOJO;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;

/**
 * Created by faizal on 10/24/13.
 */
public class Libs {

    private static Logger log = LoggerFactory.getLogger(Libs.class);
    public static Properties config;
    public static SessionFactory sfDB;
    public static SessionFactory sfEDC;
    public static SessionFactory sfOCIS;
    
    
    public static String[] months = new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
    public static String[] shortMonths = new String[] { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };
    public static int userLevel = 1;
    public static Map<String,String> restrictUserProductView = new HashMap<String,String>();
    public static Map<String,String> policyMap = new HashMap<String,String>();
    
    public static String getDbName(){
    	return String.valueOf(Libs.config.get("ocis_db"));
    }
    
    public static void getProduct(Combobox cb){
    	cb.getItems().clear();
    	cb.appendItem("All Products");
    	 
    	  Session s = Libs.sfOCIS.openSession();
    	  try{
    	    	String sql = "Select * from " +Libs.getDbName()+".dbo.F_OCISProduct(:idClient) where PolisStatus=:status order by PolisName";
    	    	SQLQuery query = s.createSQLQuery(sql);
    	    	query.setInteger("idClient", Libs.getNewInsuranceId());
    	    	query.setString("status", "Active");
    	    		
    	    	List<Object[]> l = query.list();
    	    	for(Object[] o : l){
    	    		cb.appendItem(((String)o[1]).trim() + "("+ ((String) o[0]).trim() + ")");
    	    	}
    	    cb.setSelectedIndex(0);	
    	    	
    	  }catch(Exception e){
    	    		log.error("getProduct", e);
    	  }finally{
    	  	if(s != null && s.isOpen()) s.close();
    	  }
    			
    		
    }
    
    public static Integer getNewClientId(String oldIdClient){
    	Integer newClientId = null;
    	String sql = "select ClientId from "+Libs.getDbName()+".dbo.V_MsClientGroup Where ClientIdOld=:oldId";
    	Session s = Libs.sfOCIS.openSession();
    	try{
    		
    		SQLQuery query = s.createSQLQuery(sql);
    		query.setString("oldId", oldIdClient);
    		
    		newClientId =(Integer) query.uniqueResult();
    		
    	}catch(Exception e){
    		log.error("getNewClientId",e);
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
    	return newClientId;
    }
    
    public static String[] getHospitalInvoice(String hid, Object prcode) {
		String[] hasil = null;
		int procode = ((BigDecimal)prcode).intValue();
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
			log.error("getHospitalInvoice", e);
		}finally{
			 if (s!=null && s.isOpen()) s.close();
		}
		return hasil;
	}
    
    public static Double getPemakaianDana(String chequeNo, String insid){
    	Session s = Libs.sfDB.openSession();
    	Double result = 0.0;
    	try{
    		String query = "select ltrim(rtrim(Cat)) as cat, Nominal from idnhltpf.dbo.FinPemakaianDanaByTrans where CheckNo='"+ chequeNo+"' and Flg=1 and InsId";
    		if(!insid.equals("")) query = query + " in  ("+insid+") ";
    		else query = query + "='" + Libs.getInsuranceId() + "' "; 
    		List<Object[]> l = s.createSQLQuery(query).list();
    		for(Object[] o : l){
    			if(Libs.nn(o[0]).equalsIgnoreCase("Biaya Meterai")) result = result - Double.valueOf(Libs.nn(o[1])).doubleValue();
    			else result = result + Double.valueOf(Libs.nn(o[1])).doubleValue();
    		}
    	}catch(Exception e){
    		log.error("getPemakaianDana", e);
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
    	return result;
    }
    
    public static Map<String, String> getOcisPolicyMap(){
    	Map<String,String> policyMap = new HashMap<String,String>();
    	Session s = Libs.sfOCIS.openSession();
        try {
        	
        	String polis ="";
            List polisList;
            
        	 polisList = Libs.getPolisByUserId(Libs.getUser());
             for(int i=0; i < polisList.size(); i++){
         		polis=polis+"'"+(String)polisList.get(i)+"'"+",";
         	}
             if(polis.length() > 1)polis = polis.substring(0, polis.length()-1);
        	
        	String insid="";
        	List products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
        	
            String qry = "select polisNo, clientName from IMCare.dbo.V_ClientProduct where clientIdOld ";
            if(products.size() > 0) qry = qry + " in  ("+insid+") ";
            else qry = qry + "='" + Libs.getInsuranceId() + "' ";
            		
            		
                    
            List<Object[]> l = s.createSQLQuery(qry).list();
            for (Object[] o : l) {
                policyMap.put(Libs.nn(o[0]), Libs.nn(o[1]).trim());
            }
        } catch (Exception ex) {
            log.error("getOcisPolicyMap", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
        
        return policyMap;
    }
    
    
    public static Map<String, String> getPolicyMap(){
    	Map<String,String> policyMap = new HashMap<String,String>();
    	Session s = Libs.sfDB.openSession();
        try {
        	
        	String polis ="";
            List polisList;
            
        	 polisList = Libs.getPolisByUserId(Libs.getUser());
             for(int i=0; i < polisList.size(); i++){
         		polis=polis+"'"+(String)polisList.get(i)+"'"+",";
         	}
             if(polis.length() > 1)polis = polis.substring(0, polis.length()-1);
        	
        	String insid="";
        	List products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
        	
            String qry = "select "
                    + "(convert(varchar,a.hhdryy)+'-'+convert(varchar,a.hhdrbr)+'-'+convert(varchar,a.hhdrdist)+'-'+convert(varchar,a.hhdrpono)) as policy, "
                    + "a.hhdrname "
                    + "from idnhltpf.dbo.hlthdr a "
                    + "where a.hhdrinsid";
            
            		if(products.size() > 0) qry = qry + " in  ("+insid+") ";
            		else qry = qry + "='" + Libs.getInsuranceId() + "' ";
            		
            		if(polisList.size() > 0){
            			qry = qry + "and convert(varchar,a.hhdryy)+'-'+convert(varchar,a.hhdrbr)+'-'+convert(varchar,a.hhdrdist)+'-'+convert(varchar,a.hhdrpono) "
            					  + "in ("+polis+") ";
            		} 
                    
            		/*
            		 * Author : Heri Siswanto BN
            		 * Date : 14 August 2014
            		 * Updae : Ordered by date (Reguest Mr Jame)
            		 */
            		
            		if(Executions.getCurrent().getSession().getAttribute("insuranceId") != "99999"){
            			//qry = qry + " and ='"+Executions.getCurrent().getSession().getAttribute("insuranceId")+"'";
            		}
            		//qry= qry + "order by a.hhdrname desc ";
            		
                    qry= qry + "order by a.hhdradtyy desc, a.hhdradtmm desc, a.hhdradtdd desc";
                    
            List<Object[]> l = s.createSQLQuery(qry).list();
            for (Object[] o : l) {
                policyMap.put(Libs.nn(o[0]), Libs.nn(o[1]).trim());
            }
        } catch (Exception ex) {
            log.error("getPolicies", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
        
        return policyMap;
    }

    public static org.zkoss.zk.ui.Session getSession() {
        return Executions.getCurrent().getSession();
    }

    public static Desktop getDesktop() {
        return Executions.getCurrent().getDesktop();
    }

    public static Integer getNewInsuranceId() {
        return (Integer)Executions.getCurrent().getSession().getAttribute("insuranceId");
    }
    
    public static String getInsuranceId() {
        return Libs.nn(Executions.getCurrent().getSession().getAttribute("insuranceId"));
    }

    public static String getUser() {
//        return Libs.nn(Executions.getCurrent().getSession().getAttribute("u"));
    	return (String)Executions.getCurrent().getSession().getAttribute("u");
    }
    
    public static String getUserId() {
        return Libs.nn(Executions.getCurrent().getSession().getAttribute("uid"));
    }

    public static Center getCenter() {
        return (Center) getDesktop().getAttribute("center");
    }

    public static Window getRootWindow() {
        return (Window) getDesktop().getAttribute("rootWindow");
    }

    public static String nn(Object o) {
        if (o!=null) return o.toString(); else return "";
    }

    public static int getDiffDays(Date d1, Date d2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);

        long d1ms = cal1.getTimeInMillis();
        long d2ms = cal2.getTimeInMillis();

        return (int) (Math.ceil((d2ms-d1ms) / (float)(24*3600*1000)));
    }

    public static Listcell createNumericListcell(int d, String format) {
        Listcell lc = new Listcell(new DecimalFormat(format).format(d));
        lc.setStyle("text-align:right;");
        return lc;
    }

    public static Listcell createNumericListcell(double d, String format) {
        Listcell lc = new Listcell(new DecimalFormat(format).format(d));
        lc.setStyle("text-align:right;");
        return lc;
    }

    public static String createAddFieldString(String field) {
        String result = "";
        for (int i=0; i<30; i++) {
            result += field + (i+1) + " + ";
        }
        if (result.endsWith(" + ")) result = result.substring(0, result.length()-3);
        return result;
    }

    public static String createListFieldString(String field) {
        String result = "";
        for (int i=0; i<30; i++) {
            result += field + (i+1) + ", ";
        }
        if (result.endsWith(", ")) result = result.substring(0, result.length()-2);
        return result;
    }

    public static String getBenefitItemDescription(String planItem) {
        String result = "";
        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select hrefdesc21 + hrefdesc22 "
                    + "from idnhltpf.dbo.hltref a "
                    + "where a.hrefcode1 + a.hrefcode2 = '" + planItem + "' ";

            List<String> l = s.createSQLQuery(qry).list();
            if (l.size()==1) {
                result = Libs.nn(l.get(0)).trim();
            }
        } catch (Exception ex) {
            log.error("getBenefitItemDescription", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
        return result;
    }
    
    public static List getProductByUserId(String userId){
    	List product = null;
    	
    	Session edcSession = Libs.sfDB.openSession();
    	
    	try{
    		String query = "SELECT product FROM OCIS.dbo.CIS_User_Product WHERE user_id='"+userId+"'";
    		SQLQuery q = edcSession.createSQLQuery(query);
//    		q.setString("userId", Libs.getUser());
    		
    		product = q.list();
    		
    	}catch(Exception e){
    		log.error("getProductByUserId", e);
    		
    	}finally{
    		if(edcSession != null && edcSession.isOpen()) edcSession.close();
    	}
    	
    	return product;
    }
    
    public static List getPolisByUserId(String userId){
    	
    	Session s = Libs.sfDB.openSession();
    	
    	try{
    		String query = "SELECT polis FROM OCIS.dbo.CIS_User_Polis WHERE user_id='"+userId+"'";
    		SQLQuery q = s.createSQLQuery(query);
    		return q.list();
    		
    	}catch(Exception e){
    		log.error("getPolisByUserId", e);
    		
    	}finally{
    		if(s != null && s.isOpen()) s.close();
    	}

    	return null;
    }

    public static String getProposed() {
        String result = "";
        for (int i=0; i<30; i++) {
            int c = i+1;
            result += "cast(case when a.hclmref" + c + "<>'R' then a.hclmcamt" + c + "*1 else a.hclmcamt" + c + "*0 end as float) + ";
        }
        if (result.endsWith(" + ")) result = result.substring(0, result.length()-3);
        return result;
    }

    public static String getApproved() {
        String result = "";
        for (int i=0; i<30; i++) {
            int c = i+1;
            result += "cast(case when a.hclmref" + c + "<>'R' then a.hclmaamt" + c + "*1 else a.hclmaamt" + c + "*0 end as float) + ";
        }
        if (result.endsWith(" + ")) result = result.substring(0, result.length()-3);
        return result;
    }

    public static void createXLSRow(HSSFSheet sheet, int row, Object[] values) {
        HSSFRow r = sheet.createRow(row);
        int column = 0;
        for (Object o : values) {
            HSSFCell c = r.createCell(column);
            
            if (o instanceof String) {
                c.setCellType(Cell.CELL_TYPE_STRING);
                c.setCellValue(o.toString());
            }

            if (o instanceof Double || o instanceof Integer || o instanceof Float || o instanceof BigDecimal) {
                c.setCellType(Cell.CELL_TYPE_NUMERIC);
                c.setCellValue(Double.valueOf(o.toString()));
            }

            if (o==null) {
                c.setCellType(Cell.CELL_TYPE_BLANK);
                c.setCellValue("");
            }

            column++;
        }
    }
    
    public static boolean isFamilyLimit(PolicyPOJO policy, String planType){
    	boolean isFamily = false;
    	Session s = Libs.sfEDC.openSession();
    	try{
    		String sql = "SELECT count(1) FROM EDC_PRJ.dbo.LimitKeluarga "
     			   + "where ThnPolis="+policy.getYear()+ " and nopolis="+policy.getPolicy_number()+" and Tipe='"+planType+"' ";
    		Integer hasil = (Integer)s.createSQLQuery(sql).uniqueResult();
    		if(hasil > 0) isFamily = true;

    	}catch(Exception e){
    		log.error("isFamilyLimit",e);
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
    	    	return isFamily;
    }
    
    /**
     * 
     * @param policy
     * @param index
     * @param sequence
     * @param planCode
     * @param planType
     * @return an array of object (Object[]) : object[0] = claim counter , object[1] = claim usage
     */
    public static Object[] getPlanUsage(PolicyPOJO policy, String index, String sequence, String planCode, String planType){
    	Session s = Libs.sfDB.openSession();
    	Object[] o = null;
    	try{
    		String q = "select count(*), "
                    + "sum(" + Libs.runningFields("hclmaamt", 1, 30, true) + ") as approved "
                    + "from idnhltpf.dbo.hltclm "
                    + "where "
                    + "hclmyy=" + policy.getYear() + " and hclmpono=" + policy.getPolicy_number() + " "
                    + "and hclmidxno=" + index + " and hclmseqno='" + sequence + "' "
                    + "and hclmtclaim='"+planType+"' "
                    + "and hclmrecid<>'C' ";
    		
    		if(index.equalsIgnoreCase("264"))System.out.println("query dodol banyaknya usage : " +q);

            o = (Object[]) s.createSQLQuery(q).uniqueResult();

    	}catch(Exception e){
    		log.error("getPlanUsage", e);
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
    	return o;
    }
    
    public static Double getFamilyUsage(PolicyPOJO policy, String index, String planType){
    	Double familyUsage = 0.0;
    	Session s = Libs.sfDB.openSession();
    	try{
    		String q = "select "
                    + "sum(" + Libs.runningFields("hclmaamt", 1, 30, true) + ") as approved "
                    + "from idnhltpf.dbo.hltclm "
                    + "where "
                    + "hclmyy=" + policy.getYear() + " and hclmpono=" + policy.getPolicy_number() + " "
                    + "and hclmidxno=" + index + " "
                    + "and hclmtclaim='"+planType+"' "
                    + "and hclmrecid<>'C' ";
    	BigDecimal result = (BigDecimal)s.createSQLQuery(q).uniqueResult();	
    	if(result != null)familyUsage = result.doubleValue(); 
    		
    	}catch(Exception e){
    		log.error("getFamilyUsage",e);
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
    	return familyUsage;
    }
    
    public static String isReimbursementOnly(int NoPolis, String planType, String providerPlan){
    	String result = null;
    	Session s = Libs.sfEDC.openSession();
    	try{
    		String qry = "SELECT b.Plan2 FROM EDC_PRJ.dbo.ChangePlanCat a "
     			   + "INNER JOIN EDC_PRJ.dbo.ChangePlan b ON a.No_Polis=b.No_Polis "
     			   + "WHERE a.No_Polis='"+NoPolis+"' and a.PlanType='"+planType+"' and b.Plan1='"+providerPlan+"' and b.Flg=1";
    		result = (String) s.createSQLQuery(qry).uniqueResult();
    	}catch(Exception e){
    		log.error("isReimbursementOnly", e);
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
    	return result;
    }

    public static BenefitPOJO getBenefit(String policyNumber, String planCode, String index, String seq, String planType) {
        BenefitPOJO benefitPOJO = new BenefitPOJO();
        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + "a.hbftlmtamt, "
                    + Libs.createListFieldString("a.hbftbcd") + " "
                    + "from idnhltpf.dbo.hltbft a "
                    + "where "
                    + "(convert(varchar,a.hbftyy)+'-'+convert(varchar,a.hbftbr)+'-'+convert(varchar,a.hbftdist)+'-'+convert(varchar,a.hbftpono))='" + policyNumber + "' and "
                    + "ltrim(rtrim(a.hbftcode))='" + planCode + "' ";

            List<Object[]> l = s.createSQLQuery(qry).list();
            if (l.size()==1) {
                Object[] o = l.get(0);
                benefitPOJO.setPlan_code(planCode);
                
                //check if limit gaji
                double limitGaji = getLimitGaji(policyNumber, planCode, index, seq, planType);
                if(limitGaji > 0){
                	benefitPOJO.setLimit(limitGaji);
                	benefitPOJO.setLimitGaji(true);
                }else{
                	benefitPOJO.setLimit(Double.valueOf(Libs.nn(o[0])));
                }
               
                for (int i=0; i<30; i++) {
                    benefitPOJO.getPlan_items().add(Libs.nn(o[i+1]).trim());
                }
            }
        } catch (Exception ex) {
            log.error("getBenefit", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
        return benefitPOJO;
    }
    
    /**
     * 
     * @param policyNumber
     * @param planCode
     * @param index
     * @param seq
     * @param planType must be 'I', 'O', 'R', 'D', 'G'
     * @return
     */
    public static Double getLimitGaji(String policyNumber, String planCode, String index, String seq, String planType){
    	Double limit = 0.0;
    	Session s = Libs.sfDB.openSession();
    	String[] polis = policyNumber.split("-");
    	try{
    		String q = "select hgajilmt "
                    + "from idnhltpf.dbo.hltgajisuh "
                    + "where "
                    + "hgajiid='"+planType+"' "
                    + "and hgajiyy=" + polis[0] + " "
                    + "and hgajibr="+ polis[1] + " "
                    + "and hgajidist="+ polis[2] + " "
                    + "and hgajipono=" + polis[3] + " "
                    + "and hgajiidxno=" + index + " "
                    + "and hgajiseqno='" + seq + "' ";

          BigDecimal  result = (BigDecimal) s.createSQLQuery(q).uniqueResult();
          if(result != null) limit = result.doubleValue();

    	}catch(Exception e){
    		log.error("getLimitGaji",e);
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
    	
    	return limit;
    }
    
    public static BenefitPOJO getBenefit(String policyNumber, String planCode) {
        BenefitPOJO benefitPOJO = new BenefitPOJO();
        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + "a.hbftlmtamt, "
                    + Libs.createListFieldString("a.hbftbcd") + " "
                    + "from idnhltpf.dbo.hltbft a "
                    + "where "
                    + "(convert(varchar,a.hbftyy)+'-'+convert(varchar,a.hbftbr)+'-'+convert(varchar,a.hbftdist)+'-'+convert(varchar,a.hbftpono))='" + policyNumber + "' and "
                    + "ltrim(rtrim(a.hbftcode))='" + planCode + "' ";

            List<Object[]> l = s.createSQLQuery(qry).list();
            if (l.size()==1) {
                Object[] o = l.get(0);
                benefitPOJO.setPlan_code(planCode);
                benefitPOJO.setLimit(Double.valueOf(Libs.nn(o[0])));

                for (int i=0; i<30; i++) {
                    benefitPOJO.getPlan_items().add(Libs.nn(o[i+1]).trim());
                }
            }
        } catch (Exception ex) {
            log.error("getBenefit", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
        return benefitPOJO;
    }
    
    public static Object[] getBenefit(String policyNumber, String planCode, int index){
    	Session s = Libs.sfDB.openSession();
    	Object[] result = new Object[3];
    	
    	try{
        	String[] polis = policyNumber.split("-");
        	String qry = "select a.hbftbcd"+index+ ", a.hbftbpln"+index+" from idnhltpf.dbo.hltbft a "
        			   + "where HBFTYY="+polis[0] +" and HBFTBR="+polis[1] + " and HBFTDIST="+polis[2]+" and HBFTPONO="+polis[3]+" and HBFTCODE='"+planCode+"' ";
        	
//        	System.out.println(qry);
        	
        	 List<Object[]> l = s.createSQLQuery(qry).list();
        	 if (l.size()==1) {
                 Object[] o = l.get(0);
                 result[0] = Libs.nn(o[0]);
                 result[1] = o[1];
                 result[2] = getBenefitItemDescription(Libs.nn(o[0]));
             }

    	}catch(Exception e){
    		log.error("getBenefit", e);
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
    	    	
    	
    	return result;
    }

    public static Double[] getRemainingFamilyLimit(String policyNumber, String index, String planCode) {
        String[] policySeg = policyNumber.split("\\-");
        String[] clientData = getClientData(policyNumber, index);
        Double[] result = new Double[2];

        BenefitPOJO benefitPOJO = Libs.getBenefit(policyNumber, planCode);

        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + "sum( "
                    + "hclmaamt1+hclmaamt2+hclmaamt3+hclmaamt4+hclmaamt5+hclmaamt6+hclmaamt7+hclmaamt8+hclmaamt9+hclmaamt10+ "
                    + "hclmaamt11+hclmaamt12+hclmaamt13+hclmaamt14+hclmaamt15+hclmaamt16+hclmaamt17+hclmaamt18+hclmaamt19+hclmaamt20+ "
                    + "hclmaamt12+hclmaamt22+hclmaamt23+hclmaamt24+hclmaamt25+hclmaamt26+hclmaamt27+hclmaamt28+hclmaamt29+hclmaamt30 "
                    + ") "
                    + "from idnhltpf.dbo.hltclm a "
                    + "inner join idnhltpf.dbo.hltemp c on c.hempyy=a.hclmyy and c.hemppono=a.hclmpono and c.hempidxno=a.hclmidxno and c.hempseqno=a.hclmseqno and c.hempctr=0 "
                    + "where "
                    + "convert(varchar,hclmyy)+'-'+convert(varchar,hclmbr)+'-'+convert(varchar,hclmdist)+'-'+convert(varchar,hclmpono)='" + policyNumber + "' ";

            if (Libs.nn(Libs.config.get("family_by_polclient")).contains(policySeg[3])) {
                if (clientData!=null) qry += "and c.hempcnpol='" + clientData[0] + "' ";
            } else {
                qry += "and hclmidxno='" + index.substring(0, index.indexOf("-")) + "' ";
            }

            qry += "and hclmrecid<>'C' ";

            BigDecimal r = (BigDecimal) s.createSQLQuery(qry).uniqueResult();

            result[0] = (r==null) ? benefitPOJO.getLimit() : benefitPOJO.getLimit() - r.doubleValue();
            result[1] = (r==null) ? 0D : r.doubleValue();
        } catch (Exception ex) {
            log.error("getRemainingFamilyLimit", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }

        return result;
    }

    public static Double getEDCUsage(String policyNumber, String index) {
        double result = 0;
        Session s = Libs.sfEDC.openSession();
        try {
            String qry = "select "
                    + "sum(" + createAddFieldString("a.hclmaamt") + ") as approved "
                    + "from edc_prj.dbo.edc_transclm a "
                    + "where "
                    + "(convert(varchar,a.hclmyy)+'-'+convert(varchar,a.hclmbr)+'-'+convert(varchar,a.hclmdist)+'-'+convert(varchar,a.hclmpono))='" + policyNumber + "' and "
                    + "a.hclmidxno='" + index + "' ";

            BigDecimal bd = (BigDecimal) s.createSQLQuery(qry).uniqueResult();
            if (bd!=null) result = bd.doubleValue();
        } catch (Exception ex) {
            log.error("getEDCUsage", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }

        return result;
    }

    public static String getMemberByCardNumber(String cardNumber) {
        String result = "";
        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + "top 1 a.hdt1name, a.hdt1pono "
                    + "from idnhltpf.dbo.hltdt1 a "
                    + "where a.hdt1ncard='" + cardNumber + "' ";

            List<Object[]> l = s.createSQLQuery(qry).list();
            if (l.size()==1) {
                Object[] o = l.get(0);
                result = Libs.nn(o[0]).trim();
            }
        } catch (Exception ex) {
            log.error("getMemberByCardNumber", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
        return result;
    }

    public static String getHospitalById(String id) {
        String result = "";
        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + "a.hproname, a.hpronomor "
                    + "from idnhltpf.dbo.hltpro a "
                    + "where a.hpronomor='" + id + "' ";

            List<Object[]> l = s.createSQLQuery(qry).list();
            if (l.size()==1) {
                Object[] o = l.get(0);
                result = Libs.nn(o[0]).trim();
            }
        } catch (Exception ex) {
            log.error("getHospitalById", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
        return result;
    }

    public static Map<String,String> getClientPlanMap(String policyNumber) {
        Map<String,String> result = new HashMap<String,String>();
        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + "imc_plan_code, client_plan_code "
                    + "from ocisv3.dbo.client_plan_map "
                    + "where policy_number='" + policyNumber + "' ";

            List<Object[]> l = s.createSQLQuery(qry).list();
            for (Object[] o : l) {
                result.put(Libs.nn(o[0]).trim(), Libs.nn(o[1]).trim());
            }
        } catch (Exception ex) {
            log.error("getClientPlanMap", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
        return result;
    }

    public static String getICDByCode(String icds) {
        String result = "";
        Session s = Libs.sfDB.openSession();
        try {
            String[] icdseg = icds.split("\\,");
            icds = "";
            for (String icd : icdseg) {
                icds += "'" + icd.trim() + "', ";
            }
            if (icds.endsWith(", ")) icds = icds.substring(0, icds.length()-2);
            
            String qry = "select ltrim(rtrim((HICDCODE1+HICDCODE2))) as icdCode, (HICDDESC1+HICDDESC2) as deskrips from IDNHLTPF.dbo.hlticd "
            		   + "where ltrim(rtrim((HICDCODE1+HICDCODE2))) in (" + icds + ") ";

            /*String qry = "select "
                    + "icd_code, description "
                    + "from imcs.dbo.icds "
                    + "where icd_code in (" + icds + ") ";*/

            List<Object[]> l = s.createSQLQuery(qry).list();
            for (Object[] o : l) {
                result += Libs.nn(o[1]).trim() + " (" + Libs.nn(o[0]) + "), ";
            }
            if (result.endsWith(", ")) result = result.substring(0, result.length()-2);
        } catch (Exception ex) {
            log.error("getICDByCode", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }

        return result;
    }
    
    public static String getICDAlias(String icds) {
        String result = "";
        Session s = Libs.sfDB.openSession();
        try {
            String[] icdseg = icds.split("\\,");
            icds = "";
            for (String icd : icdseg) {
                icds += "'" + icd.trim() + "', ";
            }
            if (icds.endsWith(", ")) icds = icds.substring(0, icds.length()-2);
            
            String qry = "select (HCDCODE1+HCDCODE2) as icdCode, (HCDALIAS) as deskrips from IDNHLTPF.dbo.HLTICDALIAS "
            		   + "where (HCDCODE1+HCDCODE2) in (" + icds + ") ";

            /*String qry = "select "
                    + "icd_code, description "
                    + "from imcs.dbo.icds "
                    + "where icd_code in (" + icds + ") ";*/

            List<Object[]> l = s.createSQLQuery(qry).list();
            for (Object[] o : l) {
            	if(!Libs.nn(o[1]).trim().equals(""))
                 result += Libs.nn(o[1]).trim() + " (" + Libs.nn(o[0]) + "), ";
            }
            if (result.endsWith(", ")) result = result.substring(0, result.length()-2);
        } catch (Exception ex) {
            log.error("getICDAlias", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }

        return result;
    }

    public static double getMemberClaimUsage(PolicyPOJO policy, MemberPOJO member) {
        double result = 0;

        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + "sum(" + Libs.createAddFieldString("a.hclmaamt") + ") as approved "
                    + "from idnhltpf.dbo.hltclm a "
                    + "inner join idnhltpf.dbo.hltdt1 c on c.hdt1yy=a.hclmyy and c.hdt1pono=a.hclmpono and c.hdt1idxno=a.hclmidxno and c.hdt1seqno=a.hclmseqno and c.hdt1ctr=0 "
                    + "where "
                    + "a.hclmyy=" + policy.getYear() + " and a.hclmpono=" + policy.getPolicy_number() + " "
                    + "and a.hclmidxno=" + member.getIdx() + " "
                    + "and a.hclmseqno='" + member.getSeq() + "' "
                    + "and a.hclmrecid<>'C' ";

            BigDecimal r = (BigDecimal) s.createSQLQuery(qry).uniqueResult();
            if (r!=null) result = r.doubleValue();
        } catch (Exception ex) {
            log.error("getMemberClaimUsage", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }

        return result;
    }

    public static void showDeveloping() {
        Messagebox.show("We are developing this feature. Please wait until futher notice.", "Information", Messagebox.OK, Messagebox.INFORMATION);
    }

    public static MemberPOJO getMember(String policyNumber, String index) {
        MemberPOJO memberPOJO = null;

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
                    + "b.hdt2xdtyy, b.hdt2xdtmm, b.hdt2xdtdd ";

            String qry = "from idnhltpf.dbo.hltdt1 a "
                    + "inner join idnhltpf.dbo.hltdt2 b on b.hdt2yy=a.hdt1yy and b.hdt2pono=a.hdt1pono and b.hdt2idxno=a.hdt1idxno and b.hdt2seqno=a.hdt1seqno and b.hdt2ctr=a.hdt1ctr "
                    + "inner join idnhltpf.dbo.hltemp c on c.hempyy=a.hdt1yy and c.hemppono=a.hdt1pono and c.hempidxno=a.hdt1idxno and c.hempseqno=a.hdt1seqno and c.hempctr=a.hdt1ctr "
                    + "inner join idnhltpf.dbo.hlthdr d on d.hhdryy=a.hdt1yy and d.hhdrpono=a.hdt1pono "
                    + "where "
                    + "a.hdt1ctr=0 "
                    + "and d.hhdrinsid='" + getInsuranceId() + "' "
                    + "and (convert(varchar,a.hdt1yy)+'-'+convert(varchar,a.hdt1br)+'-'+convert(varchar,a.hdt1dist)+'-'+convert(varchar,a.hdt1pono))='" + policyNumber + "' "
                    + "and (convert(varchar,a.hdt1idxno)+'-'+a.hdt1seqno)='" + index + "' ";

            List<Object[]> l = s.createSQLQuery(select + qry).list();
            if (l.size()==1) {
                Object[] o = l.get(0);

                PolicyPOJO policyPOJO = new PolicyPOJO();
                policyPOJO.setYear(Integer.valueOf(Libs.nn(o[35])));
                policyPOJO.setBr(Integer.valueOf(Libs.nn(o[36])));
                policyPOJO.setDist(Integer.valueOf(Libs.nn(o[37])));
                policyPOJO.setPolicy_number(Integer.valueOf(Libs.nn(o[38])));
                policyPOJO.setName(Libs.nn(o[39]).trim());

                Map<String,String> clientPlanMap = Libs.getClientPlanMap(policyPOJO.getPolicy_string());

                memberPOJO = new MemberPOJO();
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
            }
        } catch (Exception ex) {
            log.error("getMember", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }

        return memberPOJO;
    }

    public static void createCell(org.apache.poi.ss.usermodel.Row row, int cnt, Object value) {
        Cell cell = row.createCell(cnt);

        try {
            double d = (Double) value;
//            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
//            cell.setCellType(Cell.CELL_TYPE_BLANK)
            cell.setCellValue(d);
        } catch (Exception ex) {
            cell.setCellValue(Libs.nn(value));
        }
    }

    public static String getClaimType(String s) {
        if (s.equals("I")) return "INPATIENT";
        if (s.equals("O")) return "OUTPATIENT";
        if (s.equals("R")) return "MATERNITY";
        if (s.equals("D")) return "DENTAL";
        if (s.equals("G")) return "GLASSES";
        return "OTHER";
    }

    public static String[] getClientData(String policyNumber, String index) {
        String[] result = null;
        Session s = Libs.sfDB.openSession();
        try {
            String qry = "select "
                    + "hempcnpol, hempcnid "
                    + "from idnhltpf.dbo.hltemp "
                    + "where "
                    + "convert(varchar,hempyy)+'-'+convert(varchar,hempbr)+'-'+convert(varchar,hempdist)+'-'+convert(varchar,hemppono)='" + policyNumber + "' "
                    + "and (convert(varchar,hempidxno)+'-'+hempseqno)='" + index + "'";

            List<Object[]> l = s.createSQLQuery(qry).list();
            if (l.size()==1) {
                Object[] o = l.get(0);
                result = new String[2];
                result[0] = Libs.nn(o[0]).trim();
                result[1] = Libs.nn(o[1]).trim();
            }
        } catch (Exception ex) {
            log.error("getClientData", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }

        return result;
    }

    public static String loadAdvancedMemo(String policyNumber, String index, int claimCount, String claimType, String claimNumber, String benefitCode) {
        String result = "";
        String[] policySeg = policyNumber.split("-");
        String[] indexSeg = index.split("-");
        Session s = sfDB.openSession();
        try {
            String qry = "select memo "
                    + "from imcs.dbo.advanced_memo "
                    + "where "
                    + "policy_year=" + policySeg[0] + " "
                    + "and policy_number=" + policySeg[3] + " "
                    + "and idx=" + indexSeg[0] + " "
                    + "and seq='" + indexSeg[1] + "' "
                    + "and claim_count=" + claimCount + " "
                    + "and claim_type='" + claimType + "' "
                    + "and claim_number='" + claimNumber + "' "
                    + "and benefit_code='" + benefitCode + "' ";

            result = (String) s.createSQLQuery(qry).uniqueResult();
        } catch (Exception ex) {
            log.error("loadAdvancedMemo", ex);
        } finally {
            s.close();
        }
        return result;
    }

    public static Listcell createRemarksListcell(String remarks, Window w) {
        Listcell lc = new Listcell(remarks);
        Popup p = new Popup();
        p.setWidth("300px");
        p.setHeight("200px");
        Textbox t = new Textbox();
        t.setReadonly(true);
        t.setText(remarks);
        t.setWidth("98%");
        t.setHeight("95%");
        t.setRows(3);
        p.appendChild(t);
        lc.setTooltip(p);
        w.appendChild(p);
        return lc;
    }

    public static Integer getUsageDays(int policyYear, int policyNumber, int index, String sequence, int benefitPos) {
        int result = 0;
        Session s = sfDB.openSession();
        try {
            String q = "select sum(hclmaday" + benefitPos + ") "
                    + "from idnhltpf.dbo.hltclm "
                    + "where "
                    + "hclmyy=" + policyYear + " "
                    + "and hclmpono=" + policyNumber + " "
                    + "and hclmidxno=" + index + " "
                    + "and hclmseqno='" + sequence + "' "
                    + "and hclmrecid<>'C' ";

            result = ((BigDecimal) s.createSQLQuery(q).uniqueResult()).intValue();
        } catch (Exception ex) {
            log.error("getUsageDays", ex);
        } finally {
            s.close();
        }
        return result;
    }

    public static String getStatus(String status) {
        if (status.equals("X")) return "PROCESSED";
        if (status.trim().isEmpty()) return "READY TO PAY";
        if (status.equals("P")) return "PAID";
        if (status.equals("D")) return "DELAY";
        if (status.equals("R")) return "REJECT";
        return "";
    }

    public static boolean checkSession() {
        if (getSession().getAttribute("u")==null || getInsuranceId().isEmpty()) {
            Executions.getCurrent().getSession().setMaxInactiveInterval(0);
            Executions.getCurrent().getSession().invalidate();
            Executions.sendRedirect("http://ocis.imcare177.com");
            return true;
        }
        return false;
    }

    public static void log_login(String username, Timestamp dt) {
        Session s = sfDB.openSession();
        try {
            String q = "insert into ocisv3.dbo.login_log "
                    + "values ("
                    + "'" + username + "', "
                    + "'" + dt + "') ";

            s.createSQLQuery(q).executeUpdate();
            s.beginTransaction().commit();
        } catch (Exception ex) {
            log.error("log_login", ex);
        } finally {
            s.close();
        }
    }

    public static String runningFields(String fieldName, int start, int end, boolean isSum) {
        String result = "";
        for (int i=start; i<end+1; i++) {
            result += fieldName + i;
            if (isSum) result += "+"; else result += ",";
        }
        if (result.endsWith("+") || result.endsWith(",")) result = result.substring(0, result.length()-1);
        return result;
    }
    
    public static String formatDate(Date date){
    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    	if(date != null)
    		return sdf.format(date);
    	else return "";
    }

    public static String fixDate(String date) {
        String[] seg = date.split("\\-");

        String segM = "0" + seg[1];
        String segD = "0" + seg[2];

        segM = segM.substring(segM.length()-2);
        segD = segD.substring(segD.length()-2);

        return seg[0] + "-" + segM + "-" + segD;
    }
    
    public static Map<String, CellStyle> createStyles(Workbook wb) {
		 Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
	        CellStyle style;
	        Font titleFont = wb.createFont();
	        titleFont.setFontHeightInPoints((short)11);
	        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
	        style = wb.createCellStyle();
	        style.setAlignment(CellStyle.ALIGN_CENTER);
	        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	        style.setFont(titleFont);
	        styles.put("title", style);

	        Font monthFont = wb.createFont();
	        monthFont.setFontHeightInPoints((short)8);
	        monthFont.setColor(IndexedColors.WHITE.getIndex());
	        style = wb.createCellStyle();
	        style.setAlignment(CellStyle.ALIGN_LEFT);
	        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
	        style.setFont(monthFont);
	        style.setWrapText(true);
	        styles.put("header", style);
	        
	        Font hf = wb.createFont();
	        hf.setFontHeightInPoints((short)8);
	        hf.setBoldweight(Font.BOLDWEIGHT_BOLD);
	        style = wb.createCellStyle();
	        style.setAlignment(CellStyle.ALIGN_LEFT);
	        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	        style.setFont(hf);
	        styles.put("header2", style);
	        
	        
	        style = wb.createCellStyle();
	        style.setAlignment(CellStyle.ALIGN_CENTER);
	        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	        style.setFont(hf);
	        styles.put("header3", style);

	        Font cellFont = wb.createFont();
	        cellFont.setFontHeightInPoints((short)8);
	        style = wb.createCellStyle();
	        style.setAlignment(CellStyle.ALIGN_LEFT);
//	        style.setWrapText(true);
	        style.setBorderRight(CellStyle.BORDER_THIN);
	        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
	        style.setBorderLeft(CellStyle.BORDER_THIN);
	        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
	        style.setBorderTop(CellStyle.BORDER_THIN);
	        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
	        style.setBorderBottom(CellStyle.BORDER_THIN);
	        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	        style.setFont(cellFont);
	        styles.put("cell", style);
	        
	        style = wb.createCellStyle();
	        style.setAlignment(CellStyle.ALIGN_LEFT);
	        style.setFont(cellFont);
	        styles.put("cell2", style);
	        
	        style = wb.createCellStyle();
	        style.setAlignment(CellStyle.ALIGN_RIGHT);
//	        style.setWrapText(true);
	        style.setBorderRight(CellStyle.BORDER_THIN);
	        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
	        style.setBorderLeft(CellStyle.BORDER_THIN);
	        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
	        style.setBorderTop(CellStyle.BORDER_THIN);
	        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
	        style.setBorderBottom(CellStyle.BORDER_THIN);
	        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	        style.setFont(cellFont);
	        styles.put("cell_angka", style);
	        
	        Font cellFontBold = wb.createFont();
	        cellFontBold.setFontHeightInPoints((short)8);
	        cellFontBold.setBoldweight(Font.BOLDWEIGHT_BOLD);
	        style = wb.createCellStyle();
	        style.setAlignment(CellStyle.ALIGN_RIGHT);
//	        style.setWrapText(true);
//	        style.setBorderRight(CellStyle.BORDER_THIN);
//	        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
//	        style.setBorderLeft(CellStyle.BORDER_THIN);
//	        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
//	        style.setBorderTop(CellStyle.BORDER_THIN);
//	        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
//	        style.setBorderBottom(CellStyle.BORDER_THIN);
//	        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	        style.setFont(cellFontBold);
	        styles.put("cell_angka_bold", style);

	        style = wb.createCellStyle();
	        style.setAlignment(CellStyle.ALIGN_CENTER);
	        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
	        style.setDataFormat(wb.createDataFormat().getFormat("0.00"));
	        styles.put("formula", style);

	        style = wb.createCellStyle();
	        style.setAlignment(CellStyle.ALIGN_CENTER);
	        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
	        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
	        style.setDataFormat(wb.createDataFormat().getFormat("0.00"));
	        styles.put("formula_2", style);

	        return styles;
	}

	public static String getRefDescription(String ref) {
		Session s = sfDB.openSession();
		String result = "";
        try {
            String q = "select name from idnhltpf.dbo.ms_rejdelaycode where id='"+ref+"' ";
            result = (String) s.createSQLQuery(q).uniqueResult();
            
            //System.out.println(q + " hasilnya : "+result);
            
        } catch (Exception ex) {
            log.error("getRefDescription", ex);
        } finally {
            s.close();
        }

		return result;
	}

}
