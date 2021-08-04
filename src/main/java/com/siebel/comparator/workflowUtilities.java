package com.siebel.comparator;

import org.h2.tools.DeleteDbFiles;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

public class workflowUtilities {
  public static void normalizeXML() {
    if (CentralizedDataStore.baseTableNameList.size() != CentralizedDataStore.targetTableNameList.size()) {
      ArrayList<String> removableBaseTables = new ArrayList<String>();
      ArrayList<String> removabletargetTables = new ArrayList<String>();
      System.out.println("\nSignature analysis of the XMLs proved that,The input XMLs are not Structurally Compatible\n");
      System.out.println(" List of Entities in:" + (String)CentralizedDataStore.configuration.get("baseEnvironmentName") + ":" + CentralizedDataStore.baseTableNameList);
      System.out.println(" List of Entities in:" + (String)CentralizedDataStore.configuration.get("targetEnvironmentName") + ":" + CentralizedDataStore.targetTableNameList);
      for (String curr : CentralizedDataStore.baseTableNameList) {
        if (!CentralizedDataStore.targetTableNameList.contains(curr))
          removableBaseTables.add(curr); 
      } 
      if (!removableBaseTables.isEmpty()) {
        System.out.println("\nHence we are removing the following entities from :" + (String)CentralizedDataStore.configuration.get("baseEnvironmentName") + " table List");
        for (String curr : removableBaseTables) {
          CentralizedDataStore.baseTableNameList.remove(curr);
          System.out.println("\t -->" + curr);
        } 
      } 
      for (String curr : CentralizedDataStore.targetTableNameList) {
        if (!CentralizedDataStore.baseTableNameList.contains(curr))
          removabletargetTables.add(curr); 
      } 
      if (!removabletargetTables.isEmpty()) {
        System.out.println("Hence we are removing the following entities from :" + (String)CentralizedDataStore.configuration.get("targetEnvironmentName") + " table List");
        for (String curr : removabletargetTables) {
          System.out.println("\t -->" + curr);
          CentralizedDataStore.targetTableNameList.remove(curr);
        } 
      } 
      System.out.println("\n-------------------------------------------------------------------------------------------");
      System.out.println("|ATTENTION:  IF ANY OF THE ABOVE REMOVED ENTITIES ARE REQUIRED , PLEASE TAKE A FRESH XML  |");
      System.out.println("|            EXPORT WITH CORRECT ENTITIES ,STRUCTURE TYPE AND THEN PROCEED WITH EXECUTION.|");
      System.out.println("-------------------------------------------------------------------------------------------");
      System.out.println("\nPost Normalization:");
      System.out.println(" List of Entities in:" + (String)CentralizedDataStore.configuration.get("baseEnvironmentName") + ":" + CentralizedDataStore.baseTableNameList);
      System.out.println(" List of Entities in:" + (String)CentralizedDataStore.configuration.get("targetEnvironmentName") + ":" + CentralizedDataStore.targetTableNameList);
      System.out.println("\nProceeding for comparison with Normalized entity List!");
    } 
  }
  
  public static void deleteAllTempFiles() {
    File index = null;
    index = new File("DoNotTouch_CreateTableScript.ER");
    index.delete();
    index = new File("DoNotTouch_DYNA_FLAT_XML.ER");
    index.delete();
    index = new File("DoNotTouch_InsertTableScript.ER");
    index.delete();
    index = new File("DoNotTouch_TableDataStructure.ER");
    index.delete();
    index = new File("DoNotTouch_TableStructure.ER");
    index.delete();
    index = new File("DoNotTouch_UpdatePropertySetScript.ER");
    index.delete();
    index = new File("DoNotTouch_UpdateBCindexScript.ER");
    index.delete();
    index = new File("DoNotTouch_PromotionsWorkAroundQueries.ER");
    index.delete();
    DeleteDbFiles.execute(CentralizedDataStore.systemDir, CentralizedDataStore.configuration.get("dbName"), true);
    index = new File("derby.log");
    index.delete();
  }
  
  public static void runCleanUp() {
    CentralizedDataStore.tableNameList.clear();
    CentralizedDataStore.createQueryList.clear();
    CentralizedDataStore.insertQueryList.clear();
    System.out.println("\n<!-CDS flushed for further execution!->");
  }
  
  public static int getLastSymbolIndex(String node, char symbol) {
    int location = 0;
    char[] nodec = node.toCharArray();
    for (int i = 0; i < node.length(); i++) {
      if (nodec[i] == symbol)
        location = i; 
    } 
    return location;
  }
  
  public static int getFirstSymbolIndex(String node, char symbol) {
    return node.indexOf('[');
  }
  
  public static String formCreateQuery(String environment, String tableName, ArrayList<String> columnList) {
    String queryString = "CREATE TABLE " + environment + "_" + tableName + " (";
    String columnName = null;
    for (int i = 0; i < columnList.size(); i++) {
      columnName = "XML_" + ((String)columnList.get(i)).replaceAll("_spc", "").replaceAll("_und", "").replaceAll("-", "");
      queryString = String.valueOf(queryString) + columnName + " " + "VARCHAR(5000)";
      if (i < columnList.size() - 1)
        queryString = String.valueOf(queryString) + ","; 
    } 
    queryString = String.valueOf(queryString) + ")";
    CentralizedDataStore.createQueryList.add(queryString);
    return queryString;
  }
  
  public static String formInsertQuery(String environment, String tableName, ArrayList<String> columnList) {
    String queryString = "INSERT INTO " + environment + "_" + tableName + " values('";
    for (int i = 0; i < columnList.size(); i++) {
      queryString = String.valueOf(queryString) + ((String)columnList.get(i)).replaceAll("'", "''");
      if (i < columnList.size() - 1)
        queryString = String.valueOf(queryString) + "','"; 
    } 
    queryString = String.valueOf(queryString) + "')";
    CentralizedDataStore.insertQueryList.add(queryString);
    return queryString;
  }
  
  public static String formSelectQuery(String base, String target, String tableName, String columnName) {
    String queryString = "";
    if (tableName == "Mapping") {
      queryString = "SELECT * FROM " + base + "_" + tableName + " EXCEPT SELECT * FROM " + target + "_" + tableName + " order by 1";
    } else {
      queryString = "SELECT " + GenerateColumnList(base.toUpperCase(), tableName.toUpperCase()) + " FROM " + base + "_" + tableName.toUpperCase() + " EXCEPT SELECT " + GenerateColumnList(target.toUpperCase(), tableName.toUpperCase()) + " FROM " + target + "_" + tableName.toUpperCase();
    } 
    CentralizedDataStore.selectQueryList.add(queryString);
    return queryString;
  }
  
  public static String GenerateColumnList(String sourc, String tableName) {
    String driver = CentralizedDataStore.configuration.get("derbyDriverString");
    String dbName = String.valueOf(CentralizedDataStore.systemDir) + "/" + (String)CentralizedDataStore.configuration.get("dbName");
    String additionalProperties = CentralizedDataStore.configuration.get("addProps");
    String connectionURL = "jdbc:h2:file:" + dbName + ";" + additionalProperties + ";";
    String querystr = "select column_name from information_schema.columns where table_name='" + sourc + "_" + tableName + "' order by column_name";
    try {
      Class.forName(driver);
      Connection conn = DriverManager.getConnection(connectionURL);
      Statement stmt = conn.createStatement();
      String columnList = "XML_PROPERTYSET, ";
      ResultSet rs = stmt.executeQuery(querystr);
      while (rs.next()) {
        String s = rs.getString("column_name");
        if (s.contains("XML_PROPERTYSET") || s.contains("XML_FIRSTVERSION") || s.contains("XML_LASTVERSION") || 
          s.contains("XML_VODOBJECTNUM") || s.contains("XML_MESSAGEID") || s.contains("XML_OBJECTID") || 
          s.contains("XML_ANCESTOROBJNUM") || s.contains("XML_ORIGID") || s.contains("XML_RULESPEC") || 
          s.contains("XML_COMMENTS") || s.contains("XML_ORGID") || s.contains("XML_INTEGRATIONID") || 
          s.contains("XML_PRODID") || s.contains("XML_PARENTOPTIONORIGID") || s.contains("XML_PARENTGROUPORIGID") || 
          s.contains("XML_PRODUCTOBJNUM") || s.contains("XML_ITEMPATH1") || s.contains("XML_SUBOBJECTOBJNUM") || 
          s.contains("XML_PARENTPORTORIGID")) {
          columnList = (new StringBuilder(String.valueOf(columnList))).toString();
          continue;
        } 
        columnList = String.valueOf(columnList) + rs.getString("column_name") + ", ";
      } 
      if (columnList.length() > 2) {
        columnList = columnList.substring(0, columnList.length() - 2);
      } else {
        columnList = " * ";
      } 
      stmt.close();
      conn.close();
      return columnList;
    } catch (SQLException e) {
      System.out.println("Error while accessing DB with query:\n" + querystr);
      e.printStackTrace();
      System.exit(0);
      return null;
    } catch (ClassNotFoundException e) {
      System.out.println("Error in Client DB Driver String");
      e.printStackTrace();
      System.exit(0);
      return null;
    } 
  }
  
  public static String VerifyColumnList(String base, String target, String tableName) {
    String driver = CentralizedDataStore.configuration.get("derbyDriverString");
    String dbName = String.valueOf(CentralizedDataStore.systemDir) + "/" + (String)CentralizedDataStore.configuration.get("dbName");
    String additionalProperties = CentralizedDataStore.configuration.get("addProps");
    String connectionURL = "jdbc:h2:file:" + dbName + ";" + additionalProperties + ";";
    String querystr1 = "select column_name from information_schema.columns where table_name='" + base.toUpperCase() + "_" + tableName.toUpperCase() + "' EXCEPT select column_name from information_schema.columns where table_name='" + target.toUpperCase() + "_" + tableName.toUpperCase() + "'";
    Scanner in = new Scanner(System.in);
    try {
      Class.forName(driver);
      Connection conn = DriverManager.getConnection(connectionURL);
      Statement stmt = conn.createStatement();
      Statement stmt1 = conn.createStatement();
      Integer countt = Integer.valueOf(0);
      ResultSet rs = null;
      rs = stmt.executeQuery(querystr1);
      while (rs.next()) {
        System.out.println(String.valueOf(base) + "_" + tableName + " has the extra tag: " + rs.getString("column_name"));
        System.out.println("Do you wish to remove it and Proceed? (YES/NO)");
        String s = in.nextLine();
        s = s.toUpperCase();
        if (s.contains("YES")) {
          String querystr = "alter table " + base + "_" + tableName + " DROP COLUMN " + rs.getString("column_name");
          stmt1.execute(querystr);
          countt = Integer.valueOf(1);
        } 
      } 
      rs = null;
      stmt.close();
      stmt1.close();
      conn.close();
      if (countt.intValue() == 0)
        return String.valueOf(tableName) + " has Matching Columns"; 
      return String.valueOf(tableName) + "has MisMatch Columns";
    } catch (SQLException e) {
      System.out.println("Error while accessing DB with query:\n");
      e.printStackTrace();
      System.exit(0);
      return null;
    } catch (ClassNotFoundException e) {
      System.out.println("Error in Client DB Driver String");
      e.printStackTrace();
      System.exit(0);
      return null;
    } 
  }
  
  public static void printProgBar(int percent, String tableName) {
    StringBuilder bar = new StringBuilder("[");
    for (int i = 0; i < 50; i++) {
      if (i < percent / 2) {
        bar.append("=");
      } else if (i == percent / 2) {
        bar.append(">");
      } else {
        bar.append(" ");
      } 
    } 
    bar.append("] " + percent + "% " + "Writing:" + tableName + 
        "                       ");
    System.out.print("\r" + bar.toString());
  }
  
  public static void checkAccess() {
    String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    String dbName = "<ANY FOLDER PATH WOULD DO>";
    String connectionURL = "jdbc:derby:" + dbName + ";";
    ResultSet rs = null;
    String UserName = System.getProperty("user.name");
    try {
      Class.forName(driver);
      Connection conn = DriverManager.getConnection(connectionURL);
      Statement stmt = conn.createStatement();
      rs = stmt.executeQuery("select username,status from access where username='" + UserName + "'");
      if (rs.next()) {
        if (rs.getString(1).equalsIgnoreCase(UserName) && rs.getInt(2) == 1) {
          System.out.println("Welcome " + UserName + "! You are logged in now");
        } else if (rs.getString(1).equalsIgnoreCase(UserName) && rs.getInt(2) == 0) {
          System.out.println("Welcome " + UserName + "!");
          driver = "org.apache.derby.jdbc.EmbeddedDriver";
          dbName = "<ANY FOLDER PATH WOULD DO>";
          connectionURL = "jdbc:derby:" + dbName + ";";
          Class.forName(driver);
          conn = DriverManager.getConnection(connectionURL);
          stmt = conn.createStatement();
          System.exit(0);
        } 
      } else {
        driver = "org.apache.derby.jdbc.EmbeddedDriver";
        dbName = " ";
        connectionURL = "jdbc:derby:" + dbName + ";";
        Class.forName(driver);
        conn = DriverManager.getConnection(connectionURL);
        stmt = conn.createStatement();
        System.exit(0);
      } 
    } catch (SQLException sQLException) {
    
    } catch (ClassNotFoundException classNotFoundException) {}
  }
  
  public static int maximum(int x, int y, int z) {
    if (x > y && x > z)
      return x; 
    if (y > x && y > z)
      return y; 
    if (z > x && z > y)
      return z; 
    return 0;
  }
}
