package com.imc.ocisv3.controllers;

import com.imc.ocisv3.tools.Libs;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.*;
import org.zkoss.zul.Calendar;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by faizal on 10/29/13.
 */
public class DashboardController extends Window {

    private Logger log = LoggerFactory.getLogger(DashboardController.class);
    private Flashchart chartFrequency;
    private Flashchart distributionChart;
    private Flashchart icdDistribution;
    private Listbox frequencyList;
    private Listbox claimList;
    private Listbox providerFrequencyList;
    private Listbox providerClaimList;
    private Listbox diagnosisFrequencyList;
    private Listbox diagnosisValueList;
    
    private String userProductViewrestriction;
    
    private String polis ="";
    private List polisList;
    private List products;
    private String insid="";

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
            
            
        	products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);*/
            
            populateClaimByFrequencyNew();
            populateClaimByValueNew();
            populateProviderFrequencyNew();
            populateProviderClaimValueNew();
            populateDiagnosisByFrequenceyNew();
            populateDiagnosisByClaimValueNew();
            populateDistributionNew();
            
        	/*
            populateChartFrequency();
            poplulateClaimByFrequency();
            populateClaimByValue();
            populateProviderFrequency();
            populateProviderClaimValue();
            populateDiagnosisByFrequency();
            populateDiagnosisByClaimValue();
            populateDistribution(); */
            
        }
    }
    
    private void populateDistributionNew() {
		PieModel pieModel = new SimplePieModel();
		Session s = Libs.sfOCIS.openSession();
		try{
			String sql = "select * from "+Libs.getDbName()+".dbo.F_OCISTopTenClaimDist(:idClient)";
			SQLQuery query = s.createSQLQuery(sql);
			query.setInteger("idClient", Libs.getNewInsuranceId());
			List<Object[]> l = query.list();
			for(Object[] o : l){
				pieModel.setValue(Libs.nn(o[0]), ((BigDecimal)o[2]).doubleValue());
			}
			
		}catch(Exception e){
			log.error("populateDistributionNew", e);
		}finally{
			if(setVisible(s != null && s.isOpen()))s.close();
		}
		/*String employee = "Employee";
		String spouse = "Spouse";
		String children = "Children";
		
		
		
		pieModel.setValue(employee, getDistribution(1));
		pieModel.setValue(spouse, getDistribution(2));
		pieModel.setValue(children, getDistribution(3));*/
		
		distributionChart.setModel(pieModel);
		
	}

    private void populateDistribution() {
		PieModel pieModel = new SimplePieModel();
		String employee = "Employee";
		String spouse = "Spouse";
		String children = "Children";
		
		pieModel.setValue(employee, getDistribution(1));
		pieModel.setValue(spouse, getDistribution(2));
		pieModel.setValue(children, getDistribution(3));
		
		distributionChart.setModel(pieModel);
		
	}
    
    private double getIcdDistribution(String icd, int type){
    	double result = 0.0;
    	Session s = Libs.sfDB.openSession();
    	try{
    	
    	String qry = "select SUM(b.HCLMAAMT1+b.HCLMAAMT2+b.HCLMAAMT3+b.HCLMAAMT4+b.HCLMAAMT5+ b.HCLMAAMT6+b.HCLMAAMT7+b.HCLMAAMT8+b.HCLMAAMT9+ "
    			   + "b.HCLMAAMT10+b.HCLMAAMT11+b.HCLMAAMT12+b.HCLMAAMT13+b.HCLMAAMT14+b.HCLMAAMT15+b.HCLMAAMT16+b.HCLMAAMT17+b.HCLMAAMT18+b.HCLMAAMT19+ "
    			   + "b.HCLMAAMT20+b.HCLMAAMT21+b.HCLMAAMT22+b.HCLMAAMT23+b.HCLMAAMT24+ b.HCLMAAMT25+b.HCLMAAMT26+ b.HCLMAAMT27+b.HCLMAAMT28+b.HCLMAAMT29+b.HCLMAAMT30) as approved "
    			   + "from IDNHLTPF.dbo.HLTHDR a inner join IDNHLTPF.dbo.hltclm b on a.HHDRYY=b.HCLMYY and a.HHDRBR=b.HCLMBR and a.HHDRDIST=b.HCLMDIST "
    			   + "and a.HHDRPONO=b.HCLMPONO INNER JOIN idnhltpf.dbo.hltovc v on b.hclmnomor=v.hovccno inner join idnhltpf.dbo.hltdt1 c on b.hclmyy=c.hdt1yy and b.hclmbr=c.HDT1BR "
    			   + "and b.hclmdist=c.HDT1DIST and b.hclmpono=c.hdt1pono and b.hclmidxno=c.hdt1idxno and b.hclmseqno=c.hdt1seqno and c.hdt1ctr=0 inner join idnhltpf.dbo.hltdt2 d  "
    			   + "on c.HDT1YY=d.hdt2yy and c.HDT1BR=d.HDT2BR and c.HDT1DIST=d.HDT2DIST and c.HDT1PONO=d.hdt2pono and c.HDT1IDXNO=d.hdt2idxno and c.HDT1SEQNO=d.hdt2seqno "
    			   + "and c.HDT1CTR=d.hdt2ctr where a.HHDRINSID";
    	
    	if(products.size() > 0) qry = qry + " in  ("+insid+") ";
		else qry = qry + "='" + Libs.getInsuranceId() + "' ";
    	
    	qry = qry + "and b.HCLMRECID<>'C' AND HCLMPONO<>99999 AND HCLMIDXNO < 99989 and hclmseqno";
    	if(type == 1) qry = qry + "='A' ";
    	else if(type == 2) qry = qry + "='B' ";
    	else if(type == 3) qry = qry + " not in ('A','B') ";
    	
    	qry = qry + "and b.hclmdiscd1='"+icd+"' ";
    	
    	qry = qry + "and d.hdt2moe not in('M','U','C','D') "
    	    + "and c.HDT1NAME not like '%DUMMY%' and convert(datetime, convert(varchar,HDT2MDTMM)+'-'+convert(varchar,HDT2MDTDD)+'-'+convert(varchar,HDT2MDTYY), 110) > GETDATE()";
    
    	BigDecimal hasil = (BigDecimal)s.createSQLQuery(qry).uniqueResult(); 
    	if(hasil != null)result = hasil.doubleValue();
    	
    	}catch(Exception e){
    		log.error("getIcdDistribution", e);
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
    	
    	return result;
    }
    
    private double getDistribution(int type){
    	
    	double result = 0.0;
    	Session s = Libs.sfDB.openSession();
    	try{
    	
    	String qry = "select SUM(b.HCLMAAMT1+b.HCLMAAMT2+b.HCLMAAMT3+b.HCLMAAMT4+b.HCLMAAMT5+ b.HCLMAAMT6+b.HCLMAAMT7+b.HCLMAAMT8+b.HCLMAAMT9+ "
    			   + "b.HCLMAAMT10+b.HCLMAAMT11+b.HCLMAAMT12+b.HCLMAAMT13+b.HCLMAAMT14+b.HCLMAAMT15+b.HCLMAAMT16+b.HCLMAAMT17+b.HCLMAAMT18+b.HCLMAAMT19+ "
    			   + "b.HCLMAAMT20+b.HCLMAAMT21+b.HCLMAAMT22+b.HCLMAAMT23+b.HCLMAAMT24+ b.HCLMAAMT25+b.HCLMAAMT26+ b.HCLMAAMT27+b.HCLMAAMT28+b.HCLMAAMT29+b.HCLMAAMT30) as approved "
    			   + "from IDNHLTPF.dbo.HLTHDR a inner join IDNHLTPF.dbo.hltclm b on a.HHDRYY=b.HCLMYY and a.HHDRBR=b.HCLMBR and a.HHDRDIST=b.HCLMDIST "
    			   + "and a.HHDRPONO=b.HCLMPONO INNER JOIN idnhltpf.dbo.hltovc v on b.hclmnomor=v.hovccno inner join idnhltpf.dbo.hltdt1 c on b.hclmyy=c.hdt1yy and b.hclmbr=c.HDT1BR "
    			   + "and b.hclmdist=c.HDT1DIST and b.hclmpono=c.hdt1pono and b.hclmidxno=c.hdt1idxno and b.hclmseqno=c.hdt1seqno and c.hdt1ctr=0 inner join idnhltpf.dbo.hltdt2 d  "
    			   + "on c.HDT1YY=d.hdt2yy and c.HDT1BR=d.HDT2BR and c.HDT1DIST=d.HDT2DIST and c.HDT1PONO=d.hdt2pono and c.HDT1IDXNO=d.hdt2idxno and c.HDT1SEQNO=d.hdt2seqno "
    			   + "and c.HDT1CTR=d.hdt2ctr where a.HHDRINSID";
    	
    	if(products.size() > 0) qry = qry + " in  ("+insid+") ";
		else qry = qry + "='" + Libs.getInsuranceId() + "' ";
    	
    	qry = qry + "and b.HCLMRECID<>'C' AND HCLMPONO<>99999 AND HCLMIDXNO < 99989 and hclmseqno";
    	if(type == 1) qry = qry + "='A' ";
    	else if(type == 2) qry = qry + "='B' ";
    	else if(type == 3) qry = qry + " not in ('A','B') ";
    	
    	qry = qry + "and d.hdt2moe not in('M','U','C','D') "
    	    + "and c.HDT1NAME not like '%DUMMY%' and convert(datetime, convert(varchar,HDT2MDTMM)+'-'+convert(varchar,HDT2MDTDD)+'-'+convert(varchar,HDT2MDTYY), 110) > GETDATE()";
    
    	BigDecimal hasil = (BigDecimal)s.createSQLQuery(qry).uniqueResult(); 
    	result = hasil.doubleValue();
    	
    	}catch(Exception e){
    		log.error("getDistribution", e);
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
    	
    	return result;
    }
    
    private void populateDiagnosisByClaimValueNew(){
    	diagnosisValueList.getItems().clear();
    	Session s = Libs.sfOCIS.openSession();
    	try{
    		String sql = "Exec "+Libs.getDbName()+".dbo.S_OCISTopTenICDByValue :idClient";
    		SQLQuery query = s.createSQLQuery(sql);
    		query.setInteger("idClient", Libs.getNewInsuranceId());
    		
    		CategoryModel model = new SimpleCategoryModel();
    		
    		List<Object[]> l = query.list();
    		for(Object[] o :  l){
    			 Listitem item = new Listitem();
				 item.appendChild(new Listcell(Libs.nn(o[0])));
				 item.appendChild(new Listcell(Libs.getICDByCode(Libs.nn(o[1]))));
				 item.appendChild(Libs.createNumericListcell(((BigDecimal)o[3]).doubleValue(), "#,###.##"));
				 item.appendChild(Libs.createNumericListcell(((BigDecimal)o[4]).doubleValue(), "#,###.##"));
				 
				 diagnosisValueList.appendChild(item);
				 
				 double empAm = 0.0;
				 double spAm = 0.0;
				 double chdAm = 0.0;
				 
				 if(o[5] != null) empAm = ((BigDecimal)o[5]).doubleValue();
				 if(o[6] != null) spAm = ((BigDecimal)o[5]).doubleValue();
				 if(o[7] != null) chdAm = ((BigDecimal)o[5]).doubleValue();
				 
				 model.setValue("Employee", Libs.nn(o[2]), empAm);
				 model.setValue("Spouse", Libs.nn(o[2]), spAm);
				 model.setValue("Children", Libs.nn(o[2]), chdAm);
    		}
    		
    		icdDistribution.setModel(model);
    	}catch(Exception e){
    		
    	}finally{
    		if(s != null && s.isOpen())s.close();
    	}
    }

	private void populateDiagnosisByClaimValue() {
    	diagnosisValueList.getItems().clear();
    	Session s = Libs.sfDB.openSession();
    	try{
    		
    		
        	
        	String qry = "select top 10 SUM(b.HCLMCAMT1+b.HCLMCAMT2+b.HCLMCAMT3+b.HCLMCAMT4+b.HCLMCAMT5+ "
        			   + "b.HCLMCAMT6+b.HCLMCAMT7+b.HCLMCAMT8+b.HCLMCAMT9+b.HCLMCAMT10+b.HCLMCAMT11+b.HCLMCAMT12+ "
        			   + "b.HCLMCAMT13+b.HCLMCAMT14+b.HCLMCAMT15+b.HCLMCAMT16+b.HCLMCAMT17+b.HCLMCAMT18+b.HCLMCAMT19+ "
        			   + "b.HCLMCAMT20+b.HCLMCAMT21+b.HCLMCAMT22+b.HCLMCAMT23+b.HCLMCAMT24+b.HCLMCAMT25+b.HCLMCAMT26+ "
        			   + "b.HCLMCAMT27+b.HCLMCAMT28+b.HCLMCAMT29+b.HCLMCAMT30) as total, SUM(b.HCLMAAMT1+b.HCLMAAMT2+b.HCLMAAMT3+b.HCLMAAMT4+b.HCLMAAMT5+ "
        			   + "b.HCLMAAMT6+b.HCLMAAMT7+b.HCLMAAMT8+b.HCLMAAMT9+b.HCLMAAMT10+b.HCLMAAMT11+b.HCLMAAMT12+ "
        			   + "b.HCLMAAMT13+b.HCLMAAMT14+b.HCLMAAMT15+b.HCLMAAMT16+b.HCLMAAMT17+b.HCLMAAMT18+b.HCLMAAMT19+ "
        			   + "b.HCLMAAMT20+b.HCLMAAMT21+b.HCLMAAMT22+b.HCLMAAMT23+b.HCLMAAMT24+b.HCLMAAMT25+b.HCLMAAMT26+ "
        			   + "b.HCLMAAMT27+b.HCLMAAMT28+b.HCLMAAMT29+b.HCLMAAMT30) as approved, b.hclmdiscd1 from IDNHLTPF.dbo.HLTHDR a "
        			   + "inner join IDNHLTPF.dbo.hltclm b on a.HHDRYY=b.HCLMYY and a.HHDRBR=b.HCLMBR and a.HHDRDIST=b.HCLMDIST and "
        			   + "a.HHDRPONO=b.HCLMPONO inner join IDNHLTPF.dbo.HLTPRO e on b.HCLMNHOSCD=e.HPRONOMOR "
        			   + "INNER JOIN idnhltpf.dbo.hltovc v on b.hclmnomor=v.hovccno "
        			   + "inner join idnhltpf.dbo.hltdt1 c on b.hclmyy=c.hdt1yy and b.hclmbr=c.HDT1BR and b.hclmdist=c.HDT1DIST "
        			   + "and b.hclmpono=c.hdt1pono and b.hclmidxno=c.hdt1idxno and b.hclmseqno=c.hdt1seqno and c.hdt1ctr=0 "
        			   + "inner join idnhltpf.dbo.hltdt2 d  on c.HDT1YY=d.hdt2yy and c.HDT1BR=d.HDT2BR and c.HDT1DIST=d.HDT2DIST "
        			   + "and c.HDT1PONO=d.hdt2pono and c.HDT1IDXNO=d.hdt2idxno and c.HDT1SEQNO=d.hdt2seqno and c.HDT1CTR=d.hdt2ctr "
        			   + "where a.HHDRINSID";
        	
        	if(products.size() > 0) qry = qry + " in  ("+insid+") ";
 		   	else qry = qry + "='" + Libs.getInsuranceId() + "' ";
        	
        	if(polisList.size() > 0){
    			qry = qry + "and convert(varchar,a.hhdryy)+'-'+convert(varchar,a.hhdrbr)+'-'+convert(varchar,a.hhdrdist)+'-'+convert(varchar,a.hhdrpono) "
    					  + "in ("+polis+") ";
    		} 
        	
        	qry = qry + "and b.HCLMRECID<>'C' and d.hdt2moe not in('M','U','C') and "
        			  + "convert(datetime, convert(varchar,HDT2MDTMM)+'-'+convert(varchar,HDT2MDTDD)+'-'+convert(varchar,HDT2MDTYY), 110) > GETDATE() "
        			  + "Group by b.hclmdiscd1 order by approved desc ";
        	
        	List<Object[]> l = s.createSQLQuery(qry).list();
        	
        	CategoryModel model = new SimpleCategoryModel();
        	
        	int nomor = 1;
			 for(Object[] o : l){
				 Listitem item = new Listitem();
				 item.appendChild(new Listcell(nomor+""));
				 item.appendChild(new Listcell(Libs.getICDByCode(Libs.nn(o[2]))));
				 item.appendChild(Libs.createNumericListcell(((BigDecimal)o[0]).doubleValue(), "#,###.##"));
				 item.appendChild(Libs.createNumericListcell(((BigDecimal)o[1]).doubleValue(), "#,###.##"));
				 
				 diagnosisValueList.appendChild(item);
				 
				 model.setValue("Employee", Libs.nn(o[2]), getIcdDistribution(Libs.nn(o[2]), 1));
				 model.setValue("Spouse", Libs.nn(o[2]), getIcdDistribution(Libs.nn(o[2]), 2));
				 model.setValue("Children", Libs.nn(o[2]), getIcdDistribution(Libs.nn(o[2]), 3));
				 
				 nomor = nomor + 1;
			 }
			 
			 icdDistribution.setModel(model);
			
		}catch(Exception e){
			log.error("populateDiagnosisByClaimValue()", e);
		}finally{
			if (s!=null && s.isOpen()) s.close();
		}
		
	}
	
	private void populateDiagnosisByFrequenceyNew(){
		diagnosisFrequencyList.getItems().clear();
		Session s = Libs.sfOCIS.openSession();
		try{
			String sql = "select * from "+Libs.getDbName()+".dbo.F_OCISTopTenICDByFreq(:idClient)";
			SQLQuery query = s.createSQLQuery(sql);
			query.setInteger("idClient", Libs.getNewInsuranceId());
			
			List<Object[]> l = query.list();
			for(Object[] o : l){
				Listitem item = new Listitem();
				item.appendChild(new Listcell(Libs.nn(o[0])));
				item.appendChild(new Listcell(Libs.nn(o[2])));
				item.appendChild(Libs.createNumericListcell(((Integer)o[3]).intValue(), "#,###.##"));
				 
				diagnosisFrequencyList.appendChild(item);
				 
			}
			
		}catch(Exception e){
			
		}finally{
			if(s != null && s.isOpen())s.close();
		}
	}

	private void populateDiagnosisByFrequency() {
		diagnosisFrequencyList.getItems().clear();
		Session s = Libs.sfDB.openSession();
		try{
			
			String insid="";
        	List products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
        	
        	String qry = "select top 10 COUNT(1) as total, b.hclmdiscd1 from IDNHLTPF.dbo.HLTHDR a "
        			   + "inner join IDNHLTPF.dbo.hltclm b on a.HHDRYY=b.HCLMYY "
        			   + "and a.HHDRBR=b.HCLMBR and a.HHDRDIST=b.HCLMDIST and a.HHDRPONO=b.HCLMPONO "
        			   + "INNER JOIN idnhltpf.dbo.hltovc v on b.hclmnomor=v.hovccno "
        			   + "inner join IDNHLTPF.dbo.HLTPRO e on b.HCLMNHOSCD=e.HPRONOMOR "
        			   + "inner join idnhltpf.dbo.hltdt1 c "
        			   + "on b.hclmyy=c.hdt1yy and b.hclmbr=c.HDT1BR and b.hclmdist=c.HDT1DIST and b.hclmpono=c.hdt1pono "
        			   + "and b.hclmidxno=c.hdt1idxno and b.hclmseqno=c.hdt1seqno and c.hdt1ctr=0 "
        			   + "inner join idnhltpf.dbo.hltdt2 d  on c.HDT1YY=d.hdt2yy and c.HDT1BR=d.HDT2BR and c.HDT1DIST=d.HDT2DIST "
        			   + "and c.HDT1PONO=d.hdt2pono and c.HDT1IDXNO=d.hdt2idxno and c.HDT1SEQNO=d.hdt2seqno and c.HDT1CTR=d.hdt2ctr "
        			   + "where a.HHDRINSID";
        	
        	if(products.size() > 0) qry = qry + " in  ("+insid+") ";
 		   	else qry = qry + "='" + Libs.getInsuranceId() + "' ";
        	
        	if(polisList.size() > 0){
    			qry = qry + "and convert(varchar,a.hhdryy)+'-'+convert(varchar,a.hhdrbr)+'-'+convert(varchar,a.hhdrdist)+'-'+convert(varchar,a.hhdrpono) "
    					  + "in ("+polis+") ";
    		} 
    		
        	qry = qry + "and b.HCLMRECID<>'C' and d.hdt2moe not in('M','U','C') and "
        			  + "convert(datetime, convert(varchar,HDT2MDTMM)+'-'+convert(varchar,HDT2MDTDD)+'-'+convert(varchar,HDT2MDTYY), 110) > GETDATE() "
        			  + "Group by b.hclmdiscd1 order by total desc ";
        	
        	 List<Object[]> l = s.createSQLQuery(qry).list();
    		 int nomor = 1;
			 for(Object[] o : l){
				 Listitem item = new Listitem();
				 item.appendChild(new Listcell(nomor+""));
				 item.appendChild(new Listcell(Libs.getICDByCode(Libs.nn(o[1]))));
				 item.appendChild(Libs.createNumericListcell(((Integer)o[0]).intValue(), "#,###.##"));
				 
				 diagnosisFrequencyList.appendChild(item);
				 
				 nomor = nomor + 1;
			 }
			
		}catch(Exception e){
			log.error("populateDiagnosisByFrequency()", e);
		}finally{
			if (s!=null && s.isOpen()) s.close();
		}
		
		
	}
	
	private void populateProviderClaimValueNew(){
		providerClaimList.getItems().clear();
		Session s = Libs.sfOCIS.openSession();
		try{
			String sql = "select * from "+Libs.getDbName()+".dbo.F_OCISTopTenProvByValue(:idClient)";
			SQLQuery query = s.createSQLQuery(sql);
			query.setInteger("idClient", Libs.getNewInsuranceId());
			
			List<Object[]> l = query.list();
			for(Object[] o : l){
				Listitem item = new Listitem();
				 item.appendChild(new Listcell(Libs.nn(o[0])));
				 item.appendChild(new Listcell(Libs.nn(o[1])));
				 item.appendChild(Libs.createNumericListcell(((BigDecimal)o[2]).doubleValue(), "#,###.##"));
				 item.appendChild(Libs.createNumericListcell(((BigDecimal)o[3]).doubleValue(), "#,###.##"));
				 
				 providerClaimList.appendChild(item);
			}
		}catch(Exception e){
			log.error("populateProviderClaimValueNew",e);
		}finally{
			if(s != null && s.isOpen())s.close();
		}
	}

	private void populateProviderClaimValue() {
    	providerClaimList.getItems().clear();
    	Session s = Libs.sfDB.openSession();
    	try{
    		String insid="";
        	List products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
        	
    		String qry = "select top 10 SUM(b.HCLMCAMT1+b.HCLMCAMT2+b.HCLMCAMT3+b.HCLMCAMT4+b.HCLMCAMT5+ "
    				   + "b.HCLMCAMT6+b.HCLMCAMT7+b.HCLMCAMT8+b.HCLMCAMT9+b.HCLMCAMT10+b.HCLMCAMT11+b.HCLMCAMT12+ "
    				   + "b.HCLMCAMT13+b.HCLMCAMT14+b.HCLMCAMT15+b.HCLMCAMT16+b.HCLMCAMT17+b.HCLMCAMT18+b.HCLMCAMT19+ "
    				   + "b.HCLMCAMT20+b.HCLMCAMT21+b.HCLMCAMT22+b.HCLMCAMT23+b.HCLMCAMT24+b.HCLMCAMT25+b.HCLMCAMT26+ "
    				   + "b.HCLMCAMT27+b.HCLMCAMT28+b.HCLMCAMT29+b.HCLMCAMT30) as total, SUM(b.HCLMAAMT1+b.HCLMAAMT2+b.HCLMAAMT3+b.HCLMAAMT4+b.HCLMAAMT5+ "
        			   + "b.HCLMAAMT6+b.HCLMAAMT7+b.HCLMAAMT8+b.HCLMAAMT9+b.HCLMAAMT10+b.HCLMAAMT11+b.HCLMAAMT12+ "
        			   + "b.HCLMAAMT13+b.HCLMAAMT14+b.HCLMAAMT15+b.HCLMAAMT16+b.HCLMAAMT17+b.HCLMAAMT18+b.HCLMAAMT19+ "
        			   + "b.HCLMAAMT20+b.HCLMAAMT21+b.HCLMAAMT22+b.HCLMAAMT23+b.HCLMAAMT24+b.HCLMAAMT25+b.HCLMAAMT26+ "
        			   + "b.HCLMAAMT27+b.HCLMAAMT28+b.HCLMAAMT29+b.HCLMAAMT30) as approved, e.HPRONAME from IDNHLTPF.dbo.HLTHDR a "
    				   + "inner join IDNHLTPF.dbo.hltclm b on a.HHDRYY=b.HCLMYY and a.HHDRBR=b.HCLMBR and a.HHDRDIST=b.HCLMDIST "
    				   + "and a.HHDRPONO=b.HCLMPONO INNER JOIN idnhltpf.dbo.hltovc v on b.hclmnomor=v.hovccno "
    				   + "inner join IDNHLTPF.dbo.HLTPRO e on b.HCLMNHOSCD=e.HPRONOMOR "
    				   + "inner join idnhltpf.dbo.hltdt1 c on b.hclmyy=c.hdt1yy and b.hclmbr=c.HDT1BR and b.hclmdist=c.HDT1DIST "
    				   + "and b.hclmpono=c.hdt1pono and b.hclmidxno=c.hdt1idxno and b.hclmseqno=c.hdt1seqno and c.hdt1ctr=0 "
    				   + "inner join idnhltpf.dbo.hltdt2 d  on c.HDT1YY=d.hdt2yy and c.HDT1BR=d.HDT2BR and c.HDT1DIST=d.HDT2DIST "
    				   + "and c.HDT1PONO=d.hdt2pono and c.HDT1IDXNO=d.hdt2idxno and c.HDT1SEQNO=d.hdt2seqno and c.HDT1CTR=d.hdt2ctr "
    				   + "where a.HHDRINSID";
    		if(products.size() > 0) qry = qry + " in  ("+insid+") ";
 		   	else qry = qry + "='" + Libs.getInsuranceId() + "' ";
    		
    		if(polisList.size() > 0){
    			qry = qry + "and convert(varchar,a.hhdryy)+'-'+convert(varchar,a.hhdrbr)+'-'+convert(varchar,a.hhdrdist)+'-'+convert(varchar,a.hhdrpono) "
    					  + "in ("+polis+") ";
    		 } 
    		
    		qry = qry + "and b.HCLMRECID<>'C' AND HCLMPONO<>99999 AND HCLMIDXNO < 99989 and b.HCLMNHOSCD <> 0 and d.hdt2moe not in('M','U','C') and "
    				  + "convert(datetime, convert(varchar,HDT2MDTMM)+'-'+convert(varchar,HDT2MDTDD)+'-'+convert(varchar,HDT2MDTYY), 110) > GETDATE() "
    				  + "Group by e.HPRONAME order by approved desc";
    		
//    		System.out.println(qry);
    		
    		 List<Object[]> l = s.createSQLQuery(qry).list();
    		 int nomor = 1;
			 for(Object[] o : l){
				 Listitem item = new Listitem();
				 item.appendChild(new Listcell(nomor+""));
				 item.appendChild(new Listcell(Libs.nn(o[2])));
				 item.appendChild(Libs.createNumericListcell(((BigDecimal)o[0]).doubleValue(), "#,###.##"));
				 item.appendChild(Libs.createNumericListcell(((BigDecimal)o[1]).doubleValue(), "#,###.##"));
				 
				 providerClaimList.appendChild(item);
				 
				 nomor = nomor + 1;
			 }
    		
    	}catch(Exception e){
    		log.error("populateProviderClaimValue",e);
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
		
	}
	
	private void populateProviderFrequencyNew(){
		providerFrequencyList.getItems().clear();
		Session s = Libs.sfOCIS.openSession();
		try{
			String sql = "select * from "+Libs.getDbName()+".dbo.F_OCISTopTenProvByFreq(:idClient)";
			SQLQuery query = s.createSQLQuery(sql);
			query.setInteger("idClient", Libs.getNewInsuranceId());
			
			List<Object[]> l = query.list();
			for(Object[] o : l){
				Listitem item = new Listitem();
				item.appendChild(new Listcell(Libs.nn(o[0])));
				item.appendChild(new Listcell(Libs.nn(o[1])));
				item.appendChild(Libs.createNumericListcell(((Integer)o[2]).intValue(), "#,###.##"));
				 
				providerFrequencyList.appendChild(item);
			}
			
		}catch(Exception e){
			log.error("populateProviderFrequencyNew", e);
		}finally{
			if(s != null && s.isOpen())s.close();
		}
	}

	private void populateProviderFrequency() {
		providerFrequencyList.getItems().clear();
		Session s = Libs.sfDB.openSession();
		try{
			String insid="";
        	List products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
        	
			String qry = "select top 10 COUNT(1) as total, e.HPRONAME from IDNHLTPF.dbo.HLTHDR a "
					   + "inner join IDNHLTPF.dbo.hltclm b on a.HHDRYY=b.HCLMYY and a.HHDRBR=b.HCLMBR and "
					   + "a.HHDRDIST=b.HCLMDIST and a.HHDRPONO=b.HCLMPONO inner join IDNHLTPF.dbo.HLTPRO e "
					   + "on b.HCLMNHOSCD=e.HPRONOMOR INNER JOIN idnhltpf.dbo.hltovc v on b.hclmnomor=v.hovccno "
					   + "inner join idnhltpf.dbo.hltdt1 c on b.hclmyy=c.hdt1yy "
					   + "and b.hclmbr=c.HDT1BR and b.hclmdist=c.HDT1DIST and b.hclmpono=c.hdt1pono and b.hclmidxno=c.hdt1idxno "
					   + "and b.hclmseqno=c.hdt1seqno and c.hdt1ctr=0 inner join idnhltpf.dbo.hltdt2 d  on c.HDT1YY=d.hdt2yy and "
					   + "c.HDT1BR=d.HDT2BR and c.HDT1DIST=d.HDT2DIST and c.HDT1PONO=d.hdt2pono and c.HDT1IDXNO=d.hdt2idxno "
					   + "and c.HDT1SEQNO=d.hdt2seqno and c.HDT1CTR=d.hdt2ctr where a.HHDRINSID";
			 if(products.size() > 0) qry = qry + " in  ("+insid+") ";
  		   	 else qry = qry + "='" + Libs.getInsuranceId() + "' "; 
			 
			 if(polisList.size() > 0){
     			qry = qry + "and convert(varchar,a.hhdryy)+'-'+convert(varchar,a.hhdrbr)+'-'+convert(varchar,a.hhdrdist)+'-'+convert(varchar,a.hhdrpono) "
     					  + "in ("+polis+") ";
     		 } 
			 
			qry = qry + "and b.HCLMRECID<>'C' AND HCLMPONO<>99999 AND HCLMIDXNO < 99989 and b.HCLMNHOSCD <> 0 and d.hdt2moe not in('M','U','C') "
					  + "and convert(datetime, convert(varchar,HDT2MDTMM)+'-'+convert(varchar,HDT2MDTDD)+'-'+convert(varchar,HDT2MDTYY), 110) > GETDATE() "
					  + "Group by e.HPRONAME order by total desc ";
			
			 List<Object[]> l = s.createSQLQuery(qry).list();
			 int nomor = 1;
			 for(Object[] o : l){
				 Listitem item = new Listitem();
				 item.appendChild(new Listcell(nomor+""));
				 item.appendChild(new Listcell(Libs.nn(o[1])));
				 item.appendChild(Libs.createNumericListcell(((Integer)o[0]).intValue(), "#,###.##"));
				 
				 providerFrequencyList.appendChild(item);
				 nomor = nomor + 1;
			 }
			 
		}catch(Exception e){
			log.error("populateProviderFrequency", e);
		}finally{
			if (s!=null && s.isOpen()) s.close();
		}
		
	}
	
	private void populateClaimByValueNew(){
		claimList.getItems().clear();
		Session s = Libs.sfOCIS.openSession();
		try{
			String sql = "select * from "+Libs.getDbName()+".dbo.F_OCISTopTenByValue(:idClient)";
			SQLQuery query = s.createSQLQuery(sql);
			query.setInteger("idClient", Libs.getNewInsuranceId());
			
			List<Object[]> l = query.list();
			for(Object[] o : l){
				Listitem item = new Listitem();
				 item.appendChild(new Listcell(Libs.nn(o[0])));
				 item.appendChild(new Listcell(Libs.nn(o[2])));
				 item.appendChild(Libs.createNumericListcell(((BigDecimal)o[3]).doubleValue(), "#,###.##"));
				 item.appendChild(Libs.createNumericListcell(((BigDecimal)o[4]).doubleValue(), "#,###.##"));
				 
				 claimList.appendChild(item);
			}
		}catch(Exception e){
			log.error("populateClaimByValueNew",e);
		}finally{
			if(s != null && s.isOpen())s.close();
		}
	}

	private void populateClaimByValue() {
    	Session s = Libs.sfDB.openSession();
    	claimList.getItems().clear();
    	try{
    		String insid="";
        	List products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
    		
    		String qry = "select top 10 SUM(b.HCLMCAMT1+b.HCLMCAMT2+b.HCLMCAMT3+b.HCLMCAMT4+b.HCLMCAMT5+ "
    				   + "b.HCLMCAMT6+b.HCLMCAMT7+b.HCLMCAMT8+b.HCLMCAMT9+b.HCLMCAMT10+b.HCLMCAMT11+b.HCLMCAMT12+ "
    				   + "b.HCLMCAMT13+b.HCLMCAMT14+b.HCLMCAMT15+b.HCLMCAMT16+b.HCLMCAMT17+b.HCLMCAMT18+b.HCLMCAMT19+ "
    				   + "b.HCLMCAMT20+b.HCLMCAMT21+b.HCLMCAMT22+b.HCLMCAMT23+b.HCLMCAMT24+b.HCLMCAMT25+b.HCLMCAMT26+ "
    				   + "b.HCLMCAMT27+b.HCLMCAMT28+b.HCLMCAMT29+b.HCLMCAMT30) as total, SUM(b.HCLMAAMT1+b.HCLMAAMT2+b.HCLMAAMT3+b.HCLMAAMT4+b.HCLMAAMT5+ "
        			   + "b.HCLMAAMT6+b.HCLMAAMT7+b.HCLMAAMT8+b.HCLMAAMT9+b.HCLMAAMT10+b.HCLMAAMT11+b.HCLMAAMT12+ "
        			   + "b.HCLMAAMT13+b.HCLMAAMT14+b.HCLMAAMT15+b.HCLMAAMT16+b.HCLMAAMT17+b.HCLMAAMT18+b.HCLMAAMT19+ "
        			   + "b.HCLMAAMT20+b.HCLMAAMT21+b.HCLMAAMT22+b.HCLMAAMT23+b.HCLMAAMT24+b.HCLMAAMT25+b.HCLMAAMT26+ "
        			   + "b.HCLMAAMT27+b.HCLMAAMT28+b.HCLMAAMT29+b.HCLMAAMT30) as approved, c.HDT1NCARD, c.HDT1NAME from IDNHLTPF.dbo.HLTHDR a "
    				   + "inner join IDNHLTPF.dbo.hltclm b on a.HHDRYY=b.HCLMYY and a.HHDRBR=b.HCLMBR and a.HHDRDIST=b.HCLMDIST and a.HHDRPONO=b.HCLMPONO "
    				   + "INNER JOIN idnhltpf.dbo.hltovc v on b.hclmnomor=v.hovccno "
    				   + "inner join idnhltpf.dbo.hltdt1 c on b.hclmyy=c.hdt1yy and b.hclmbr=c.HDT1BR and b.hclmdist=c.HDT1DIST and b.hclmpono=c.hdt1pono "
    				   + "and b.hclmidxno=c.hdt1idxno and b.hclmseqno=c.hdt1seqno and c.hdt1ctr=0 inner join idnhltpf.dbo.hltdt2 d  on c.HDT1YY=d.hdt2yy and "
    				   + "c.HDT1BR=d.HDT2BR and c.HDT1DIST=d.HDT2DIST and c.HDT1PONO=d.hdt2pono and c.HDT1IDXNO=d.hdt2idxno and c.HDT1SEQNO=d.hdt2seqno and "
    				   + "c.HDT1CTR=d.hdt2ctr where a.HHDRINSID";
    		
    		 if(products.size() > 0) qry = qry + " in  ("+insid+") ";
  		   	 else qry = qry + "='" + Libs.getInsuranceId() + "' "; 
    		 
    		 if(polisList.size() > 0){
     			qry = qry + "and convert(varchar,a.hhdryy)+'-'+convert(varchar,a.hhdrbr)+'-'+convert(varchar,a.hhdrdist)+'-'+convert(varchar,a.hhdrpono) "
     					  + "in ("+polis+") ";
     		 } 
    		
    		qry = qry + "and b.HCLMRECID<>'C' AND HCLMPONO<>99999 AND HCLMIDXNO < 99989 and d.hdt2moe not in('M','U','C') and c.HDT1NAME not like '%DUMMY%' and "
    			+ "convert(datetime, convert(varchar,HDT2MDTMM)+'-'+convert(varchar,HDT2MDTDD)+'-'+convert(varchar,HDT2MDTYY), 110) > GETDATE() "
    			+ "Group by c.HDT1NCARD, c.HDT1NAME order by approved desc ";
    		
    		System.out.println("query top ten claim : "+qry);
    		
    		 List<Object[]> l = s.createSQLQuery(qry).list();
    		 int nomor = 1;
			 for(Object[] o : l){
				 Listitem item = new Listitem();
				 item.appendChild(new Listcell(nomor+""));
				 item.appendChild(new Listcell(Libs.nn(o[3])));
				 item.appendChild(Libs.createNumericListcell(((BigDecimal)o[0]).doubleValue(), "#,###.##"));
				 item.appendChild(Libs.createNumericListcell(((BigDecimal)o[1]).doubleValue(), "#,###.##"));
				 
				 claimList.appendChild(item);
				 
				 nomor = nomor + 1;
			 }
    		
    	}catch(Exception e){
    		log.error("poplulateClaimByValue",e);
    		
    	}finally{
    		if (s!=null && s.isOpen()) s.close();
    	}
		
	}
	private void populateClaimByFrequencyNew(){
		frequencyList.getItems().clear();
		Session s = Libs.sfOCIS.openSession();
		try{
			String sql = "select * from "+Libs.getDbName()+".dbo.F_OCISTopTenByFreq(:idClient)";
			SQLQuery q = s.createSQLQuery(sql);
			q.setInteger("idClient", Libs.getNewInsuranceId());
			
			List<Object[]> l = q.list();
			for(Object[] o : l){
					Listitem item = new Listitem();
				 item.appendChild(new Listcell(Libs.nn(o[0])));
				 item.appendChild(new Listcell(Libs.nn(o[1])));
				 item.appendChild(Libs.createNumericListcell(((Integer)o[2]).intValue(), "#,###.##"));
				 
				 frequencyList.appendChild(item);
			}
		}catch(Exception e){
			log.error("populateClaimByFrequencyNew",e);
		}finally{
			if(s != null && s.isOpen()) s.close();
		}
	}

	private void poplulateClaimByFrequency() {
		frequencyList.getItems().clear();
		Session s = Libs.sfDB.openSession();
		try{
			
			String insid="";
        	List products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
			
			String qry = "select top 10 COUNT(1) as total, c.HDT1NCARD,c.HDT1NAME from IDNHLTPF.dbo.HLTHDR a "
					   + "inner join IDNHLTPF.dbo.hltclm b on a.HHDRYY=b.HCLMYY "
					   + "and a.HHDRBR=b.HCLMBR and a.HHDRDIST=b.HCLMDIST and a.HHDRPONO=b.HCLMPONO "
					   + "INNER JOIN idnhltpf.dbo.hltovc v on b.hclmnomor=v.hovccno "
					   + "inner join idnhltpf.dbo.hltdt1 c "
					   + "on b.hclmyy=c.hdt1yy and b.hclmbr=c.HDT1BR and b.hclmdist=c.HDT1DIST and b.hclmpono=c.hdt1pono "
					   + "and b.hclmidxno=c.hdt1idxno and b.hclmseqno=c.hdt1seqno and c.hdt1ctr=0 "
					   + "inner join idnhltpf.dbo.hltdt2 d  on c.HDT1YY=d.hdt2yy and c.HDT1BR=d.HDT2BR and c.HDT1DIST=d.HDT2DIST " 
					   + "and c.HDT1PONO=d.hdt2pono and c.HDT1IDXNO=d.hdt2idxno and c.HDT1SEQNO=d.hdt2seqno and c.HDT1CTR=d.hdt2ctr "
					   + "where a.HHDRINSID";
		   if(products.size() > 0) qry = qry + " in  ("+insid+") ";
		   else qry = qry + "='" + Libs.getInsuranceId() + "' "; 
		   
		   if(polisList.size() > 0){
   				qry = qry + "and convert(varchar,a.hhdryy)+'-'+convert(varchar,a.hhdrbr)+'-'+convert(varchar,a.hhdrdist)+'-'+convert(varchar,a.hhdrpono) "
   					  + "in ("+polis+") ";
   		   } 
		   
			qry = qry + " and b.HCLMRECID<>'C' AND HCLMPONO<>99999 AND HCLMIDXNO < 99989 and d.hdt2moe not in('M','U','C') and c.HDT1NAME not like '%DUMMY%' and "
				+"convert(datetime, convert(varchar,HDT2MDTMM)+'-'+convert(varchar,HDT2MDTDD)+'-'+convert(varchar,HDT2MDTYY), 110) >= GETDATE() " 
				+"Group by c.HDT1NCARD,c.HDT1NAME order by COUNT(1) desc ";
			
//			System.out.println(qry);
			
			 List<Object[]> l = s.createSQLQuery(qry).list();
			 int nomor = 1;
			 for(Object[] o : l){
				 Listitem item = new Listitem();
				 item.appendChild(new Listcell(nomor+""));
				 item.appendChild(new Listcell(Libs.nn(o[2])));
				 item.appendChild(Libs.createNumericListcell(((Integer)o[0]).intValue(), "#,###.##"));
				 
				 frequencyList.appendChild(item);
				 
				 nomor = nomor + 1;
			 }
			
		}catch(Exception e){
			log.error("poplulateClaimByFrequency",e);
		}finally{
			if (s!=null && s.isOpen()) s.close();
		}
		
	}

	private void initComponents() {
		
        chartFrequency = (Flashchart) getFellow("chartFrequency");
        frequencyList = (Listbox)getFellow("frequencyList");
        claimList = (Listbox)getFellow("claimList");
        providerFrequencyList = (Listbox)getFellow("providerFrequencyList");
        providerClaimList = (Listbox)getFellow("providerClaimList");
        diagnosisFrequencyList = (Listbox)getFellow("diagnosisFrequencyList");
        diagnosisValueList = (Listbox)getFellow("diagnosisValueList");
        distributionChart = (Flashchart)getFellow("distributionChart");
        icdDistribution = (Flashchart)getFellow("icdDistribution");
    }

    private void populateChartFrequency() {
        Map<String,Integer> claimMap = new HashMap<String,Integer>();
        CategoryModel cm = new SimpleCategoryModel();

        Session s = Libs.sfDB.openSession();
        try {
        	
        	String insid="";
        	List products = Libs.getProductByUserId(Libs.getUser());
        	for(int i=0; i < products.size(); i++){
        		insid=insid+"'"+(String)products.get(i)+"'"+",";
        	}
        	if(insid.length() > 1)insid = insid.substring(0, insid.length()-1);
        	
            String qry = "select "
                    + "a.hclmcdatey, a.hclmcdatem, a.hclmtclaim, count(*) "
                    + "from idnhltpf.dbo.hltclm a "
                    + "inner join idnhltpf.dbo.hlthdr b on b.hhdryy=a.hclmyy and b.hhdrpono=a.hclmpono "
                    + "where "
                    + "b.hhdrinsid";
            		if(products.size() > 0) qry = qry + " in  ("+insid+") ";
            		else qry = qry + "='" + Libs.getInsuranceId() + "' ";  
            		
            		if(polisList.size() > 0){
            			qry = qry + "and convert(varchar,b.hhdryy)+'-'+convert(varchar,b.hhdrbr)+'-'+convert(varchar,b.hhdrdist)+'-'+convert(varchar,b.hhdrpono) "
            					  + "in ("+polis+") ";
            		} 
            		
            		qry = qry  + " and a.hclmrecid<>'C' AND hclmPONO<>99999 AND hclmIDXNO < 99989";

            if (!Libs.nn(userProductViewrestriction).isEmpty()) qry += "and b.hhdrpono in (" + userProductViewrestriction + ") ";

            qry += "group by a.hclmcdatey, a.hclmcdatem, a.hclmtclaim ";
            
//            Messagebox.show(qry);

            List<Object[]> l = s.createSQLQuery(qry).list();

            for (Object[] o : l) {
                String key = o[0] + "-" + (Integer.valueOf(Libs.nn(o[1]))-1) + "-" + o[2];
                claimMap.put(key, Integer.valueOf(Libs.nn(o[3])));
            }

            for (int i=-11; i<1; i++) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.MONTH, i);
                String key = cal.get(java.util.Calendar.YEAR) + "-" + cal.get(java.util.Calendar.MONTH) + "-I";
                cm.setValue("Inpatient", Libs.shortMonths[cal.get(java.util.Calendar.MONTH)] + " " + String.valueOf(cal.get(java.util.Calendar.YEAR)).substring(2), claimMap.get(key));
                
            }

            for (int i=-11; i<1; i++) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.MONTH, i);
                String key = cal.get(java.util.Calendar.YEAR) + "-" + cal.get(java.util.Calendar.MONTH) + "-O";
                cm.setValue("Outpatient", Libs.shortMonths[cal.get(java.util.Calendar.MONTH)] + " " + String.valueOf(cal.get(java.util.Calendar.YEAR)).substring(2), claimMap.get(key));
            }

            for (int i=-11; i<1; i++) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.MONTH, i);
                String key = cal.get(java.util.Calendar.YEAR) + "-" + cal.get(java.util.Calendar.MONTH) + "-R";
                cm.setValue("Maternity", Libs.shortMonths[cal.get(java.util.Calendar.MONTH)] + " " + String.valueOf(cal.get(java.util.Calendar.YEAR)).substring(2), claimMap.get(key));
            }

            for (int i=-11; i<1; i++) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.MONTH, i);
                String key = cal.get(java.util.Calendar.YEAR) + "-" + cal.get(java.util.Calendar.MONTH) + "-D";
                cm.setValue("Dental", Libs.shortMonths[cal.get(java.util.Calendar.MONTH)] + " " + String.valueOf(cal.get(java.util.Calendar.YEAR)).substring(2), claimMap.get(key));
            }

            for (int i=-11; i<1; i++) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.MONTH, i);
                String key = cal.get(java.util.Calendar.YEAR) + "-" + cal.get(java.util.Calendar.MONTH) + "-G";
                cm.setValue("Glasses", Libs.shortMonths[cal.get(java.util.Calendar.MONTH)] + " " + String.valueOf(cal.get(java.util.Calendar.YEAR)).substring(2), claimMap.get(key));
            }

            chartFrequency.setModel(cm);
        } catch (Exception ex) {
            log.error("populateChartFrequency", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

}
