package com.imc.ocisv3.controllers;

import com.imc.ocisv3.tools.Libs;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.*;
import org.zkoss.zul.event.PagingEvent;

import java.util.List;

/**
 * Created by faizal on 10/28/13.
 */
public class ClientSelectionController extends Window {

    private Logger log = LoggerFactory.getLogger(ClientSelectionController.class);
    private Listbox lbInactive;
    private Listbox lbActive;
    private Paging pgInactive;
    private Paging pgActive;
    private Tabbox tbx;
    private String where;
    private String activeClients = "";

    public void onCreate() {
        if (Libs.getSession().getAttribute("u")==null) {
            Executions.getCurrent().getSession().setMaxInactiveInterval(0);
            Executions.getCurrent().getSession().invalidate();
            Executions.sendRedirect("http://ocis.imcare177.com");
        } else {
            initComponents();
            populateInactive(0, pgInactive.getPageSize());
            populateActive(0, pgActive.getPageSize());
        }
    }

    private void initComponents() {
        lbInactive = (Listbox) getFellow("lbInactive");
        lbActive = (Listbox) getFellow("lbActive");
        pgInactive = (Paging) getFellow("pgInactive");
        pgActive = (Paging) getFellow("pgActive");
        tbx = (Tabbox) getFellow("tbx");

        String[] activeClientsSeg = Libs.nn(Libs.config.get("active_clients")).split("\\,");
        for (String activeClient : activeClientsSeg) {
            activeClients += "'" + activeClient + "',";
        }
        if (activeClients.endsWith(",")) activeClients = activeClients.substring(0, activeClients.length()-1);

        pgInactive.addEventListener("onPaging", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                PagingEvent evt = (PagingEvent) event;
                populateInactive(evt.getActivePage()*pgInactive.getPageSize(), pgInactive.getPageSize());
            }
        });
        pgActive.addEventListener("onPaging", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                PagingEvent evt = (PagingEvent) event;
                populateActive(evt.getActivePage()*pgActive.getPageSize(), pgActive.getPageSize());
            }
        });
    }

    private void populateInactive(int offset, int limit) {
        lbInactive.getItems().clear();
        Session s = Libs.sfDB.openSession();
        try {
//            String countQry = "select count(*) from ocis.dbo.cis_inslf where hinsid not in (" + activeClients + ") ";
//            String qry = "select hinsid, hinsname from ocis.dbo.cis_inslf where hinsid not in (" + activeClients + ") ";
            
            String countQry = "select count(1) from IDNHLTPF.dbo.HLTINS where HINSID not in ( "
        		    +"select HINSID from (select distinct hinsid, hinsname from IDNHLTPF.dbo.hltins "
        		    +"inner join IDNHLTPF.dbo.hlthdr on HINSID=HHDRINSID where "
        		    +"convert(datetime, convert(varchar,HHDRMDTMM)+'-'+convert(varchar,HHDRMDTDD)+'-'+convert(varchar,HHDRMDTYY),110) >= GETDATE() "
        		    +"AND HINSID <> 99999) q ) and HINSID not in ('99998','99999') ";
            
            String qry = "select hinsid, hinsname from IDNHLTPF.dbo.HLTINS where HINSID not in ( "
            		    +"select HINSID from (select distinct hinsid, hinsname from IDNHLTPF.dbo.hltins "
            		    +"inner join IDNHLTPF.dbo.hlthdr on HINSID=HHDRINSID where "
            		    +"convert(datetime, convert(varchar,HHDRMDTMM)+'-'+convert(varchar,HHDRMDTDD)+'-'+convert(varchar,HHDRMDTYY),110) >= GETDATE() "
            		    +"AND HINSID <> 99999) q ) and HINSID not in ('99998','99999') ";

            if (where!=null) {
                countQry += "and (" + where + ") ";
                qry += "and (" + where + ") ";
            }

            qry += "order by hinsname asc";

            Integer count = (Integer) s.createSQLQuery(countQry).uniqueResult();
            pgInactive.setTotalSize(count);

            List<Object[]> l = s.createSQLQuery(qry).setFirstResult(offset).setMaxResults(limit).list();
            for (Object[] o : l) {
                String clientName = Libs.nn(o[1]).trim();

                Listitem li = new Listitem();
                li.setValue(o[0]);

                if (Libs.config.get("demo_mode").equals("true") && clientName.contains("REDPATH")) clientName = Libs.nn(Libs.config.get("demo_name"));
                li.appendChild(new Listcell(clientName));

                lbInactive.appendChild(li);
            }

        } catch (Exception ex) {
            log.error("populateInactive", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    private void populateActive(int offset, int limit) {
        lbActive.getItems().clear();
        Session s = Libs.sfDB.openSession();
        try {
//            String countQry = "select count(*) from idnhltpf.dbo.hltins where hinsid in (" + activeClients + ") ";
//            String qry = "select hinsid, hinsname from idnhltpf.dbo.hltins where hinsid in (" + activeClients + ") ";
        	
        	String countQry = "select COUNT(1) from ( select distinct hinsid, hinsname from IDNHLTPF.dbo.hltins "
        			         +"inner join IDNHLTPF.dbo.hlthdr on HINSID=HHDRINSID "
        			         +"where "
        			         +"convert(datetime, convert(varchar,HHDRMDTMM)+'-'+convert(varchar,HHDRMDTDD)+'-'+convert(varchar,HHDRMDTYY),110) >= GETDATE() "
        			         +"AND HINSID <> 99999) q ";
        	String qry = "select hinsid, hinsname from (select distinct hinsid, hinsname from IDNHLTPF.dbo.hltins "
        			    +"inner join IDNHLTPF.dbo.hlthdr on HINSID=HHDRINSID "
        			    +"where "
        			    +"convert(datetime, convert(varchar,HHDRMDTMM)+'-'+convert(varchar,HHDRMDTDD)+'-'+convert(varchar,HHDRMDTYY),110) >= GETDATE() "
        			    +"AND HINSID <> 99999) q ";

            if (where!=null) {
                countQry += "where (" + where + ") ";
                qry += "where (" + where + ") ";
            }

            qry += "order by hinsname asc";

            Integer count = (Integer) s.createSQLQuery(countQry).uniqueResult();
            pgActive.setTotalSize(count);

            List<Object[]> l = s.createSQLQuery(qry).setFirstResult(offset).setMaxResults(limit).list();
            for (Object[] o : l) {
                String clientName = Libs.nn(o[1]).trim();

                Listitem li = new Listitem();
                li.setValue(o[0]);

                if (Libs.config.get("demo_mode").equals("true") && clientName.contains("REDPATH")) clientName = Libs.nn(Libs.config.get("demo_name"));
                li.appendChild(new Listcell(clientName));

                lbActive.appendChild(li);
            }

        } catch (Exception ex) {
            log.error("populateActive", ex);
        } finally {
            if (s!=null && s.isOpen()) s.close();
        }
    }

    public void refresh() {
        where = null;
        populateInactive(0, pgInactive.getPageSize());
        populateActive(0, pgActive.getPageSize());
    }

    public void inactiveClientSelected() {
    	if(Executions.getCurrent().getSession().getAttribute("insuranceId") != null)
        	Executions.getCurrent().getSession().removeAttribute("insuranceId");
    	
        Libs.getSession().setAttribute("insuranceId", lbInactive.getSelectedItem().getValue().toString());
        Executions.sendRedirect("../main.zul");
    }

    public void activeClientSelected() {
    	if(Executions.getCurrent().getSession().getAttribute("insuranceId") != null)
        	Executions.getCurrent().getSession().removeAttribute("insuranceId");
    	
    	Integer clientId = Libs.getNewClientId(lbActive.getSelectedItem().getValue().toString());
//        Libs.getSession().setAttribute("insuranceId", lbActive.getSelectedItem().getValue().toString());
    	Libs.getSession().setAttribute("insuranceId", clientId);
        Executions.sendRedirect("../main.zul");
    }
    
    

    public void quickSearch() {
        String val = ((Textbox) getFellow("tQuickSearch")).getText();
        if (!val.isEmpty()) {
            where = "convert(varchar,hinsname) like '%" + val + "%' or "
                    + "hinsid like '%" + val + "%' ";

            if (tbx.getSelectedIndex()==1) {
                populateInactive(0, pgInactive.getPageSize());
            } else {
                populateActive(0, pgActive.getPageSize());
            }
        } else refresh();
    }

    public void logout() {
        if (Messagebox.show("Are you sure you want to logout?", "Confirmation", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION)==Messagebox.OK) {
            Executions.getCurrent().getSession().setMaxInactiveInterval(0);
            Executions.getCurrent().getSession().invalidate();
            Executions.sendRedirect("/index.zul");
        }
    }

}
