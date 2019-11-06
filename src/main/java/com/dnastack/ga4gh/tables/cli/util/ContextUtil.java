package com.dnastack.ga4gh.tables.cli.util;

import com.dnastack.ddap.cli.login.Context;
import com.dnastack.ddap.cli.login.ContextDAO;

public class ContextUtil {

    public static Context loadContextOrExit(ContextDAO contextDAO) {
        try {
            return contextDAO.load();
        } catch (ContextDAO.PersistenceException e) {
            System.err.println(e.getMessage());
            System.err.println("Try running the 'login' command.");
            //throw new CommandLineClient.SystemExit(1, e);
            throw new RuntimeException(e);
        }
    }
}