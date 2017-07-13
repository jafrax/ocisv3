package com.imc.ocisv3.controllers;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.A;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Space;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;

import com.imc.ocisv3.tools.Libs;


@SuppressWarnings("serial")
public class ReportListController extends SelectorComposer<Window>{
	
	@Wire private Vbox vbox;
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		createReportActiveMember();
	}

	
	
	private void createReportActiveMember() {
		Hbox hbox = new Hbox();
		hbox.setParent(vbox);
		
		A activeMember = new A("Active Member Report");
		activeMember.setStyle("font-size: 12pt;color:#00bbee;text-decoration :none");
		
		
		A matured = new A("Policies Matured Report");
		matured.setStyle("font-size: 12pt;color:#00bbee;text-decoration :none");
		
		A tbc2 = new A();
		
		hbox.appendChild(activeMember);
		hbox.appendChild(new Space());
		hbox.appendChild(matured);
		hbox.appendChild(tbc2);
		
		activeMember.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				goTo("ActiveMemberReport");			
			}
		});
		
		matured.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				goTo("PoliciesMaturedReport");
				
			}
		});
		
		Integer userlevel = (Integer)Executions.getCurrent().getSession().getAttribute("userLevel");
		 if (userlevel.intValue()==1){ 
			 activeMember.setVisible(true);
			 matured.setVisible(true);
		 }
		 else {
			 activeMember.setVisible(false);
			 matured.setVisible(false);
		 }
		
	}
	
	private void goTo(String page){
//		 if (Libs.getCenter().getChildren().size()>0) Libs.getCenter().removeChild(Libs.getCenter().getFirstChild());
	     Window w = (Window) Executions.createComponents("views/"+page+".zul", null, null);
	     w.doModal();
		
//		 Window w = (Window) Executions.createComponents("/views/ReportList.zul", this, null);
	}
}
