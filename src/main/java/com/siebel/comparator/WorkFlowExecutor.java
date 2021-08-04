package com.siebel.comparator;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


public class WorkFlowExecutor {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
            System.out.println("\n-->Starting Process Engine\n-->Initiation Sync Module \n-->Running One Time Setup for CentralizedDataStore(CDS)\n<!-CDS is offline in Beta Mode,supplying configuration(only)-!>");
            CentralizedDataStore.loadConfiguration();
            workflowUtilities.deleteAllTempFiles();
            CentralizedDataStore.setColumnListValues();
            CentralizedDataStore.setNotes();
            if (CentralizedDataStore.workAround) {
                CentralizedDataStore.connectionProp.put("DBName", CentralizedDataStore.configuration.get("baseDBName"));
                CentralizedDataStore.connectionProp.put("ServerName", CentralizedDataStore.configuration.get("baseServerName"));
                CentralizedDataStore.connectionProp.put("UserName", CentralizedDataStore.configuration.get("baseUserName"));
                CentralizedDataStore.connectionProp.put("PassWord", CentralizedDataStore.configuration.get("basePassWord"));
                CentralizedDataStore.connectionProp.put("PortNumber", CentralizedDataStore.configuration.get("basePortNumber"));
            }
            runWorkFlow(CentralizedDataStore.configuration.get("baseEnvironmentName"), CentralizedDataStore.configuration.get("baseXMLFileName"));
            CentralizedDataStore.baseTableNameList = new ArrayList<String>(CentralizedDataStore.tableNameList);


            workflowUtilities.runCleanUp();
            if (CentralizedDataStore.workAround) {
                CentralizedDataStore.connectionProp.put("DBName", CentralizedDataStore.configuration.get("targetDBName"));
                CentralizedDataStore.connectionProp.put("ServerName", CentralizedDataStore.configuration.get("targetServerName"));
                CentralizedDataStore.connectionProp.put("UserName", CentralizedDataStore.configuration.get("targetUserName"));
                CentralizedDataStore.connectionProp.put("PassWord", CentralizedDataStore.configuration.get("targetPassWord"));
                CentralizedDataStore.connectionProp.put("PortNumber", CentralizedDataStore.configuration.get("targetPortNumber"));
            }
            runWorkFlow(CentralizedDataStore.configuration.get("targetEnvironmentName"), CentralizedDataStore.configuration.get("targetXMLFileName"));
            CentralizedDataStore.targetTableNameList = new ArrayList<String>(CentralizedDataStore.tableNameList);
            Collections.replaceAll(CentralizedDataStore.baseTableNameList, "TableOfContent", "Mapping");
            Collections.replaceAll(CentralizedDataStore.targetTableNameList, "TableOfContent", "Mapping");


            workflowUtilities.normalizeXML();
            System.out.print("\n-->Reading Data From DB and creating Views\t");
            WorkFlow.readTableAndCreateView(CentralizedDataStore.configuration.get("baseEnvironmentName"), CentralizedDataStore.configuration.get("targetEnvironmentName"), CentralizedDataStore.baseTableNameList);
            WorkFlow.readTableAndCreateView(CentralizedDataStore.configuration.get("targetEnvironmentName"), CentralizedDataStore.configuration.get("baseEnvironmentName"), CentralizedDataStore.targetTableNameList);
            System.out.println("\t[DONE]");
            System.out.println("Reading Data From DB and writing to Fully Compared Excel(this may take few minutes of time)");
            WorkFlow.readDBAndWriteToExcel_Enhanced(CentralizedDataStore.configuration.get("baseEnvironmentName"), CentralizedDataStore.configuration.get("targetEnvironmentName"), CentralizedDataStore.baseTableNameList);
            WorkFlow.readDBAndWriteToExcel_Enhanced(CentralizedDataStore.configuration.get("targetEnvironmentName"), CentralizedDataStore.configuration.get("baseEnvironmentName"), CentralizedDataStore.baseTableNameList);
            WorkFlow.comparisonWriteToExcel(CentralizedDataStore.configuration.get("baseEnvironmentName"), CentralizedDataStore.configuration.get("targetEnvironmentName"));

            if (!CentralizedDataStore.issueTableNameList.isEmpty())
                System.out.println("\nList of Ignored Content Catagories");
            for (String tableName : CentralizedDataStore.issueTableNameList)
                System.out.println(". " + tableName);
            CentralizedDataStore.knownTableNameList = new ArrayList<String>(CentralizedDataStore.columnList.keySet());
            for (String tableName : CentralizedDataStore.tableNameList) {
                if (!CentralizedDataStore.knownTableNameList.contains(tableName))
                    CentralizedDataStore.unknownTableNameList.add(tableName);
            }
            if (!CentralizedDataStore.unknownTableNameList.isEmpty()) {
                System.out.println("\nPrinting newly found tables:");
                for (String tableName : CentralizedDataStore.unknownTableNameList)
                    System.out.println("\t" + tableName);
                System.out.println("----------------------------");
            }
            System.out.println("\n\n-->Unloading chache memory");
            System.gc();

            if ("delete".equalsIgnoreCase(args[1])) {
                workflowUtilities.deleteAllTempFiles();
                System.out.println("-->Deleting TEMP Files");
                workflowUtilities.deleteAllTempFiles();
            } else {
                System.out.println("-->TEMP Files are not deleted,since Argument Request say so");
            }
            String driver = "org.apache.derby.jdbc.EmbeddedDriver";
            String dbName = "<ANY FOLDER PATH WOULD DO>";
            String connectionURL = "jdbc:derby:" + dbName + ";";
            try {
                Class.forName(driver);
                Connection conn = DriverManager.getConnection(connectionURL);
                Statement stmt = conn.createStatement();
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                stmt.executeUpdate("insert into usage(username,toolname,date) values('" + System.getProperty("user.name") + "','XML Compare Tool','" + dateFormat.format(date) + "')");
            } catch (SQLException sQLException) {}
            System.out.println("-->Stopping CDS and Application");
        } catch (Exception e) {
            Object writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter((Writer)writer);
            e.printStackTrace(printWriter);
            StringSelection stringSelection = new StringSelection(writer.toString());
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
            e.printStackTrace();
            System.exit(0);
        }
        long endTime = System.currentTimeMillis();
        long millis = endTime - startTime;
        System.out.println("\nTotal time Taken:" + (
                millis / 3600000L % 24L) + " Hours " + (
                millis / 60000L % 60L) + " Minutes " + (
                millis / 1000L % 60L) + " Seconds");
    }

    public static void runWorkFlow(String environment, String XMLFileName) throws IOException {
        System.out.println("\nInitiating WorkFlow for the XML:" + XMLFileName);
        System.out.println("Please wait while the System Performs,");
        System.out.print(" 1)XML Transform using XSLT\t\t\t");
        XMLTransformer transformer = new XMLTransformer();
        transformer.commenceTrasform(XMLFileName);
        System.out.println("\t[DONE]");
        System.out.print(" 2)Table Structure Analysis using Core Engine\t");
        WorkFlow workFlow = new WorkFlow();
        workFlow.getAllTableStructure("DoNotTouch_DYNA_FLAT_XML.ER");
        System.out.println("\t[DONE]");
        System.out.print(" 3)SQL generation(Create Script) using SQL GenX\t");
        workFlow.getAllCreateStatements(environment, "DoNotTouch_TableStructure.ER");
        System.out.println("\t[DONE]");
        System.out.print(" 4)SQL Execution(Create Script) using SQL Runner");
        workFlow.runDBScript(environment, CentralizedDataStore.createQueryList);
        System.out.println("\t[DONE]");
        System.out.print(" 5)Table Data Structure Analysis using Core Engine");
        workFlow.getAllTableDataStructure("DoNotTouch_DYNA_FLAT_XML.ER");
        System.out.println("\t[DONE]");
        System.out.print(" 6)SQL generation(Insert Script) using SQL GenX\t");
        workFlow.getAllInsertStatements(environment, "DoNotTouch_TableDataStructure.ER");
        System.out.println("\t[DONE]");
        System.out.print(" 7)SQL Execution(Insert Script) using SQL Runner");
        workFlow.runDBScript(environment, CentralizedDataStore.insertQueryList);
        System.out.println("\t[DONE]");
        System.out.print(" 8)Index Mapping for Bundles/Coupons (If Exists)");
        workFlow.UpdateDataDetails(environment, CentralizedDataStore.tableNameList);
        System.out.println("\t[DONE]");
        System.out.print(" 9)SQL Execution(Update Script) using SQL Runner");
        workFlow.UpdateDatabaseData(environment, CentralizedDataStore.tableNameList);
        System.out.println("\t[DONE]");
        System.out.println("\nDone Executing workflow for:" + XMLFileName);
    }
}
