package com.imc.ocisv3.controllers;

import com.imc.ocisv3.tools.Libs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.*;
import java.util.Date;
import java.util.Calendar;

/**
 * Created by faizal on 11/12/13.
 */
public class ReportDialogController extends Window {

    private Logger log = LoggerFactory.getLogger(ReportDialogController.class);
    private Combobox cbProduct;
    private Combobox cbScope;
    private Combobox cbPeriod;
    private Combobox cbMonthStart;
    private Combobox cbMonthEnd;
    private Spinner spnMonthYearStart;
    private Spinner spnMonthYearEnd;
    private Spinner spnYearStart;
    private Spinner spnYearEnd;
    private Datebox dDate;
    private Datebox dDateStart;
    private Datebox dDateEnd;
    private Row rowToDate;
    private Row rowPeriod;
    private Row rowMonthRange;
    private Row rowDateRange;
    private Row rowYearRange;
    private String userProductViewrestriction;

    public void onCreate() {
        if (!Libs.checkSession()) {
            userProductViewrestriction = Libs.restrictUserProductView.get(Libs.getUser());
            initComponents();
        }
    }

    private void initComponents() {
        cbProduct = (Combobox) getFellow("cbProduct");
        cbScope = (Combobox) getFellow("cbScope");
        cbPeriod = (Combobox) getFellow("cbPeriod");
        cbMonthStart = (Combobox) getFellow("cbMonthStart");
        cbMonthEnd = (Combobox) getFellow("cbMonthEnd");
        spnMonthYearStart = (Spinner) getFellow("spnMonthYearStart");
        spnMonthYearEnd = (Spinner) getFellow("spnMonthYearEnd");
        spnYearStart = (Spinner) getFellow("spnYearStart");
        spnYearEnd = (Spinner) getFellow("spnYearEnd");
        dDate = (Datebox) getFellow("dDate");
        dDateStart = (Datebox) getFellow("dDateStart");
        dDateEnd = (Datebox) getFellow("dDateEnd");
        rowToDate = (Row) getFellow("rowToDate");
        rowPeriod = (Row) getFellow("rowPeriod");
        rowMonthRange = (Row) getFellow("rowMonthRange");
        rowDateRange = (Row) getFellow("rowDateRange");
        rowYearRange = (Row) getFellow("rowYearRange");

        getCaption().setLabel("Export " + String.valueOf(getAttribute("title")));

        for (String s : Libs.months) {
            cbMonthStart.appendItem(s);
            cbMonthEnd.appendItem(s);
        }

        cbScope.setSelectedIndex(1);
        cbPeriod.setSelectedIndex(0);
        dDate.setValue(new Date());
        
        rowPeriod.setVisible(true);
        rowDateRange.setVisible(true);
        dDateStart.setValue(new Date());
        dDateEnd.setValue(new Date());

        cbScope.addEventListener("onSelect", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                hideAllRows();
                switch (cbScope.getSelectedIndex()) {
                    case 0:
                        cbProduct.setDisabled(true);
                        break;
                    case 1:
                        cbProduct.setDisabled(false);
                        rowPeriod.setVisible(true);
//                        rowToDate.setVisible(true);
                        cbScope.setSelectedIndex(1);
                        cbPeriod.setSelectedIndex(0);
                        dDate.setValue(new Date());
                }
            }
        });
        
        rowToDate.setVisible(false);

        cbPeriod.addEventListener("onSelect", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                hideAllRows();
                int year = Calendar.getInstance().get(Calendar.YEAR);
                int month = Calendar.getInstance().get(Calendar.MONTH);
                switch (cbPeriod.getSelectedIndex()) {
                case 0:
                    rowPeriod.setVisible(true);
                    rowDateRange.setVisible(true);
                    dDateStart.setValue(new Date());
                    dDateEnd.setValue(new Date());
                    break;
                    
                	/**
                    case 0:
                        rowPeriod.setVisible(true);
                        rowToDate.setVisible(true);
                        cbScope.setSelectedIndex(1);
                        cbPeriod.setSelectedIndex(0);
                        dDate.setValue(new Date());
                        break;
                    case 1:
                        rowPeriod.setVisible(true);
                        rowToDate.setVisible(true);
                        cbScope.setSelectedIndex(1);
                        cbPeriod.setSelectedIndex(1);
                        dDate.setValue(new Date());
                        break;
                    case 2:
                        rowPeriod.setVisible(true);
                        rowDateRange.setVisible(true);
                        dDateStart.setValue(new Date());
                        dDateEnd.setValue(new Date());
                        break;
                    case 3:
                        rowPeriod.setVisible(true);
                        rowMonthRange.setVisible(true);
                        cbMonthStart.setSelectedIndex(month);
                        cbMonthEnd.setSelectedIndex(month);
                        spnMonthYearStart.setValue(year);
                        spnMonthYearEnd.setValue(year);
                        break;
                    case 4:
                        rowPeriod.setVisible(true);
                        rowYearRange.setVisible(true);
                        spnYearStart.setValue(year);
                        spnYearEnd.setValue(year);
                        break; */
                }
            }
        });

        int i = 0;
        cbProduct.appendItem("All Products");
        cbProduct.setSelectedIndex(0);
        boolean show = true;
        for (String s : Libs.policyMap.keySet()) {
            String productName = Libs.policyMap.get(s);
            String productString = productName + " (" + s + ")";
            if (Libs.config.get("demo_mode").equals("true") && Libs.getInsuranceId().equals("00051")) productName = Libs.config.get("demo_name").toString();

            if (!Libs.nn(userProductViewrestriction).isEmpty()) {
                if (!userProductViewrestriction.contains(s.split("\\-")[3])) show=false;
                else show=true;
            }

            if (show) cbProduct.appendItem(productString);

            if (productString.equals(String.valueOf(getAttribute("product")))) cbProduct.setSelectedIndex(i+1);
            i++;
        }
    }

    private void hideAllRows() {
        rowToDate.setVisible(false);
        rowPeriod.setVisible(false);
        rowMonthRange.setVisible(false);
        rowDateRange.setVisible(false);
        rowYearRange.setVisible(false);
    }

    public void export() {
        setAttribute("export", true);
        setAttribute("product", cbProduct.getSelectedItem().getLabel());
        setAttribute("scope", cbScope.getSelectedIndex());
        setAttribute("period", cbPeriod.getSelectedIndex());
        setAttribute("date", dDate.getValue());
        setAttribute("dateStart", dDateStart.getValue());
        setAttribute("dateEnd", dDateEnd.getValue());
        setAttribute("monthStart", cbMonthStart.getSelectedIndex());
        setAttribute("monthEnd", cbMonthEnd.getSelectedIndex());
        setAttribute("monthYearStart", spnMonthYearStart.getRawValue());
        setAttribute("monthYearEnd", spnMonthYearEnd.getRawValue());
        setAttribute("yearStart", spnYearStart.getRawValue());
        setAttribute("yearEnd", spnYearEnd.getRawValue());
        detach();
    }

}
