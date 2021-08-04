package com.siebel.comparator;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CentralizedDataStore {
  static final String systemDir = System.getProperty("user.dir");
  
  public static int NumOutputObjects = 0;
  
  public static ArrayList<String> tableNameList = new ArrayList<String>();
  
  public static ArrayList<String> viewNameList = new ArrayList<String>();
  
  public static ArrayList<String> checkableContentList = new ArrayList<String>();
  
  public static ArrayList<String> baseTableNameList = new ArrayList<String>();
  
  public static ArrayList<String> targetTableNameList = new ArrayList<String>();
  
  public static ArrayList<String> createQueryList = new ArrayList<String>();
  
  public static ArrayList<String> insertQueryList = new ArrayList<String>();
  
  public static ArrayList<String> selectQueryList = new ArrayList<String>();
  
  public static Map<String, String> configuration = new HashMap<String, String>();
  
  public static Map<String, String> columnList = new HashMap<String, String>();
  
  public static ArrayList<String> ignoreColumnNameList = new ArrayList<String>();
  
  public static ArrayList<String> issueTableNameList = new ArrayList<String>();
  
  public static ArrayList<String> knownTableNameList = new ArrayList<String>();
  
  public static ArrayList<String> indexTableNameList = new ArrayList<String>();
  
  public static ArrayList<String> unknownTableNameList = new ArrayList<String>();
  
  public static ArrayList<String> PS_checkableContentList = new ArrayList<String>();
  
  public static ArrayList<String> keyList = new ArrayList<String>();
  
  public static ArrayList<String> DND = new ArrayList<String>();
  
  public static Map<String, String> contentMap = new HashMap<String, String>();
  
  public static Map<String, String> connectionProp = new HashMap<String, String>();
  
  public static Set<String> contentMapTables;
  
  public static int maxIndexCount;
  
  public static boolean workAround;
  
  public static Map<String, String> aliasTableNameList = new HashMap<String, String>();
  
  public static void loadConfiguration() throws Exception {
    FileInputStream fis = new FileInputStream(String.valueOf(systemDir) + "/configuration.xls");
    HSSFWorkbook hSSFWorkbook = new HSSFWorkbook(fis);
    Sheet sheet = hSSFWorkbook.getSheet("XML_CONFIG");
    Cell cell = null;
    cell = sheet.getRow(7).getCell(3);
    configuration.put("baseEnvironmentName", cell.getStringCellValue());
    cell = sheet.getRow(8).getCell(3);
    configuration.put("baseXMLFileName", cell.getStringCellValue());
    cell = sheet.getRow(7).getCell(12);
    configuration.put("targetEnvironmentName", cell.getStringCellValue());
    cell = sheet.getRow(8).getCell(12);
    configuration.put("targetXMLFileName", cell.getStringCellValue());
    configuration.put("derbyDriverString", "org.h2.Driver");
    configuration.put("dbName", "XML_" + (String)configuration.get("baseEnvironmentName") + 
        "_" + (String)configuration.get("targetEnvironmentName") + "_DB");
    configuration.put("addProps", "AUTO_SERVER=TRUE");
    cell = sheet.getRow(12).getCell(9);
    workAround = cell.getStringCellValue().equalsIgnoreCase("Y");
    if (workAround) {
      cell = sheet.getRow(15).getCell(3);
      configuration.put("AEnvironmentName", cell.getStringCellValue());
      cell = sheet.getRow(16).getCell(3);
      configuration.put("baseServerName", cell.getStringCellValue());
      cell = sheet.getRow(17).getCell(3);
      configuration.put("baseUserName", cell.getStringCellValue());
      cell = sheet.getRow(18).getCell(3);
      configuration.put("basePassWord", cell.getStringCellValue());
      cell = sheet.getRow(19).getCell(3);
      configuration.put("baseDBName", cell.getStringCellValue());
      configuration.put("basePortNumber", "1521");
      cell = sheet.getRow(15).getCell(12);
      configuration.put("BEnvironmentName", cell.getStringCellValue());
      cell = sheet.getRow(16).getCell(12);
      configuration.put("targetServerName", cell.getStringCellValue());
      cell = sheet.getRow(17).getCell(12);
      configuration.put("targetUserName", cell.getStringCellValue());
      cell = sheet.getRow(18).getCell(12);
      configuration.put("targetPassWord", cell.getStringCellValue());
      cell = sheet.getRow(19).getCell(12);
      configuration.put("targetDBName", cell.getStringCellValue());
      configuration.put("targetPortNumber", "1521");
    } 
    hSSFWorkbook.close();
    if (((String)configuration.get("baseEnvironmentName")).equalsIgnoreCase(configuration.get("targetEnvironmentName"))) {
      System.out.println("\n\nError:Both the Base and Target Environent has Same Name");
      System.out.println("Base  :" + (String)configuration.get("baseEnvironmentName"));
      System.out.println("Target:" + (String)configuration.get("targetEnvironmentName"));
      System.out.println("The compare Engine is completely dependent on the uniqueness of the environment Names.");
      System.out.println("Hence, You cannot proceed with this configuration. Sorry :( ");
      System.exit(0);
    } 
    if (((String)configuration.get("baseXMLFileName")).equalsIgnoreCase(configuration.get("targetXMLFileName"))) {
      System.out.println("\n\nError:Both the Base and Target Environent XMLs has Same Name");
      System.out.println("Base   XML File:" + (String)configuration.get("baseXMLFileName"));
      System.out.println("Target XML File:" + (String)configuration.get("targetXMLFileName"));
      System.out.println("The compare Engine is completely dependent on the uniqueness of the environment XML Names.");
      System.out.println("Hence, You cannot proceed with this configuration. Sorry :( ");
      System.exit(0);
    } 
  }
  
  public static void setColumnListValues() throws IOException, FileNotFoundException {
    FileInputStream fis = new FileInputStream(String.valueOf(systemDir) + "/configuration.xls");
    HSSFWorkbook hSSFWorkbook = new HSSFWorkbook(fis);
    Sheet sheet = hSSFWorkbook.getSheet("XML_KeyFields");
    Cell cell = null;
    String table = null;
    String column = null;
    String alias = null;
    for (int i = 1; i <= sheet.getPhysicalNumberOfRows(); i++) {
      cell = sheet.getRow(i).getCell(0);
      table = cell.getStringCellValue();
      cell = sheet.getRow(i).getCell(1);
      alias = cell.getStringCellValue();
      cell = sheet.getRow(i).getCell(2);
      column = cell.getStringCellValue();
      cell = sheet.getRow(i).getCell(3);
      if (1 == (int)cell.getNumericCellValue())
        DND.add(table); 
      columnList.put(table, column);
      aliasTableNameList.put(table, alias);
      if (table.equalsIgnoreCase("END"))
        i = 30000; 
    } 
    indexTableNameList.add("ISSATTRDEF");
    indexTableNameList.add("ISSCLASSDEF");
    indexTableNameList.add("ISSPRODDEF");
    indexTableNameList.add("Mapping");
    ignoreColumnNameList.add("XML_PROPERTYSET");
    ignoreColumnNameList.add("XML_FIRSTVERSION");
    ignoreColumnNameList.add("XML_COMMENTS");
    ignoreColumnNameList.add("XML_ITEMCODE");
    ignoreColumnNameList.add("ID");
    ignoreColumnNameList.add("XML_RULESPEC");
    hSSFWorkbook.close();
  }
  
  public static void setNotes() throws IOException, FileNotFoundException {
    FileInputStream fis = new FileInputStream(String.valueOf(systemDir) + "/configuration.xls");
    HSSFWorkbook hSSFWorkbook = new HSSFWorkbook(fis);
    Sheet sheet = hSSFWorkbook.getSheet("XML_Notes");
    Cell cell = null;
    String table = null;
    String column = null;
    for (int i = 1; i <= sheet.getPhysicalNumberOfRows(); i++) {
      cell = sheet.getRow(i).getCell(0);
      table = cell.getStringCellValue();
      cell = sheet.getRow(i).getCell(1);
      column = cell.getStringCellValue();
      contentMap.put(table, column);
      if (table.equalsIgnoreCase("END"))
        i = 30000; 
    } 
    contentMapTables = contentMap.keySet();
    hSSFWorkbook.close();
  }
}
