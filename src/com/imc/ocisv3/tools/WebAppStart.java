package com.imc.ocisv3.tools;

import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.WebAppInit;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by faizal on 10/24/13.
 */
public class WebAppStart implements WebAppInit {

    private Logger log = LoggerFactory.getLogger(WebAppStart.class);

    @Override
    public void init(WebApp wa) throws Exception {
        loadConfig(wa);
        connectDatabaseDB();
        connectDatabaseEDC();
        connectDatabaseOCis();
    }

    private void connectDatabaseOCis() {
    	Configuration configuration = new Configuration();

        Properties prop = new Properties();
        prop.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        prop.put("hibernate.connection.driver_class", "net.sourceforge.jtds.jdbc.Driver");
        prop.put("hibernate.connection.url", String.valueOf(Libs.config.get("ocis_url")));
        prop.put("hibernate.connection.username", String.valueOf(Libs.config.get("ocis_username")));
        prop.put("hibernate.connection.password", String.valueOf(Libs.config.get("ocis_password")));
        prop.put("transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
        prop.put("current_session_context_class", "thread");
        prop.put("hibernate.show_sql", "false");

        if (Libs.config.get("c3p0").equals("true")) {
            prop.put("hibernate.c3p0.acquire_increment", "2");
            prop.put("hibernate.c3p0.idle_test_period", "3000");
            prop.put("hibernate.c3p0.timeout", "18000");
            prop.put("hibernate.c3p0.max_size", "25");
            prop.put("hibernate.c3p0.min_size", "3");
            prop.put("hibernate.c3p0.max_statements", "0");
            prop.put("hibernate.c3p0.preferredTestQuery", "select 1;");
            prop.put("hibernate.c3p0.validate", "true");
        }

        configuration.setProperties(prop);

        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
        Libs.sfOCIS = configuration.buildSessionFactory(serviceRegistry);
		
	}

	private void loadConfig(WebApp wa) {
        Libs.config = new Properties();
        try {
            File f = new File(wa.getRealPath("/conf/config.properties"));
            Libs.config.load(new FileInputStream(f));

            String restrict_user_product_view = Libs.nn(Libs.config.get("restrict_user_product_view"));
            if (!restrict_user_product_view.isEmpty()) {
                String[] seg = restrict_user_product_view.split("\\|");
                for (String s : seg) {
                    String u = s.split("\\:")[0];
                    String v = s.split("\\:")[1];
                    Libs.restrictUserProductView.put(u, v);
                }
            }
        } catch (Exception ex) {
            log.error("loadConfig", ex);
        }
    }

    private void connectDatabaseDB() {
        Configuration configuration = new Configuration();

        Properties prop = new Properties();
        prop.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        prop.put("hibernate.connection.driver_class", "net.sourceforge.jtds.jdbc.Driver");
        prop.put("hibernate.connection.url", String.valueOf(Libs.config.get("db_url")));
        prop.put("hibernate.connection.username", String.valueOf(Libs.config.get("db_username")));
        prop.put("hibernate.connection.password", String.valueOf(Libs.config.get("db_password")));
        prop.put("transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
        prop.put("current_session_context_class", "thread");
        prop.put("hibernate.show_sql", "false");

        if (Libs.config.get("c3p0").equals("true")) {
            prop.put("hibernate.c3p0.acquire_increment", "2");
            prop.put("hibernate.c3p0.idle_test_period", "3000");
            prop.put("hibernate.c3p0.timeout", "18000");
            prop.put("hibernate.c3p0.max_size", "25");
            prop.put("hibernate.c3p0.min_size", "3");
            prop.put("hibernate.c3p0.max_statements", "0");
            prop.put("hibernate.c3p0.preferredTestQuery", "select 1;");
            prop.put("hibernate.c3p0.validate", "true");
        }

        configuration.setProperties(prop);

        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
        Libs.sfDB = configuration.buildSessionFactory(serviceRegistry);
    }

    private void connectDatabaseEDC() {
        Configuration configuration = new Configuration();

        Properties prop = new Properties();
        prop.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        prop.put("hibernate.connection.driver_class", "net.sourceforge.jtds.jdbc.Driver");
        prop.put("hibernate.connection.url", String.valueOf(Libs.config.get("edc_url")));
        prop.put("hibernate.connection.username", String.valueOf(Libs.config.get("edc_username")));
        prop.put("hibernate.connection.password", String.valueOf(Libs.config.get("edc_password")));
        prop.put("transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
        prop.put("current_session_context_class", "thread");
        prop.put("hibernate.show_sql", "false");

        if (Libs.config.get("c3p0").equals("true")) {
            prop.put("hibernate.c3p0.acquire_increment", "2");
            prop.put("hibernate.c3p0.idle_test_period", "3000");
            prop.put("hibernate.c3p0.timeout", "18000");
            prop.put("hibernate.c3p0.max_size", "25");
            prop.put("hibernate.c3p0.min_size", "3");
            prop.put("hibernate.c3p0.max_statements", "0");
            prop.put("hibernate.c3p0.preferredTestQuery", "select 1;");
            prop.put("hibernate.c3p0.validate", "true");
        }

        configuration.setProperties(prop);

        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
        Libs.sfEDC = configuration.buildSessionFactory(serviceRegistry);
    }

}
