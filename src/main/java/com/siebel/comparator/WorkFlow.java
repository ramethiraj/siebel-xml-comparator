package com.siebel.comparator;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkFlow {
  static String word1;
  
  static String word2;
  
  static BufferedReader br = null;
  
  public  void getAllTableStructure(String fileName) {
    String line = null;
    String inLine = null;
    String line_str = null;
    String prevTableName = "NoTableName";
    String prevPrintLn = "NoPrintLn";
    String tableName = null;
    String columnName = null;
    try {
      File fout = new File("DoNotTouch_TableStructure.ER");
      FileOutputStream fos = new FileOutputStream(fout);
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
      br = new BufferedReader(new FileReader(new File(fileName)));
      while ((inLine = br.readLine()) != null) {
        line = inLine;
        line_str = line.substring(0, line.indexOf("*"));
        line = line_str.substring(workflowUtilities.getLastSymbolIndex(line_str, '/'), line_str.length());
        tableName = line.substring(1, workflowUtilities.getFirstSymbolIndex(line, '['));
        if (!tableName.equals(prevTableName) && 
          !prevPrintLn.equals("EOT")) {
          bw.write("EOT");
          prevPrintLn = "EOT";
          bw.newLine();
        } 
        if (line.contains("[CREATE]") && !inLine.contains("[IGNORE]")) {
          if (prevPrintLn == "EOT" && inLine.startsWith("/PropertySet[CREATE]/PropertySet[")) {
            columnName = "PropertySet";
            bw.write(String.valueOf(tableName) + "::" + columnName);
            prevPrintLn = String.valueOf(tableName) + "::" + columnName;
            bw.newLine();
          } else if ((inLine.startsWith("/PropertySet[CREATE]/TableOfContent[CREATE]/ISS_undPROD_undDEF") || 
            inLine.startsWith("/PropertySet[CREATE]/TableOfContent[CREATE]/ISS_undATTR_undDEF") || 
            inLine.startsWith("/PropertySet[CREATE]/TableOfContent[CREATE]/ISS_undCLASS_undDEF")) && 
            prevPrintLn == "EOT") {
            columnName = "PropertySet";
            bw.write(String.valueOf(tableName) + "::" + columnName);
            prevPrintLn = String.valueOf(tableName) + "::" + columnName;
            bw.newLine();
          } 
          columnName = inLine.substring(inLine.indexOf("[@"), 
              inLine.length());
          columnName = columnName.substring(2, columnName.indexOf("="));
          bw.write(String.valueOf(tableName) + "::" + columnName);
          prevPrintLn = String.valueOf(tableName) + "::" + columnName;
          bw.newLine();
        } 
        prevTableName = tableName;
      } 
      bw.write("EOT");
      bw.newLine();
      bw.write("EOF");
      bw.flush();
      bw.close();
    } catch (FileNotFoundException e) {
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (Exception e) {
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } 
  }
  
  public  void getAllCreateStatements(String environment, String fileName) {
    String inLine = null;
    ArrayList<String> tableContentList = new ArrayList<String>();
    ArrayList<String> columnNameList = new ArrayList<String>();
    ArrayList<String> tableNameList = new ArrayList<String>();
    String currTableName = null;
    try {
      br = new BufferedReader(new FileReader(new File(fileName)));
      File fout = new File("DoNotTouch_CreateTableScript.ER");
      FileOutputStream fos = new FileOutputStream(fout);
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
      while ((inLine = br.readLine()) != null) {
        if (inLine.equals("EOF")) {
          bw.flush();
          bw.close();
          continue;
        } 
        if (inLine.equals("EOT") && !tableContentList.isEmpty()) {
          currTableName = ((String)tableContentList.get(0)).substring(0, (
              (String)tableContentList.get(0)).indexOf("::")).replaceAll("_spc", "").replaceAll("_und", "");
          for (String columnName : tableContentList)
            columnNameList.add(columnName.substring(columnName.indexOf("::") + 2, columnName.length())); 
          if (!tableNameList.contains(currTableName)) {
            tableNameList.add(currTableName);
            CentralizedDataStore.tableNameList.add(currTableName);
            bw.write(workflowUtilities.formCreateQuery(environment, currTableName, columnNameList));
            bw.newLine();
          } 
          columnNameList.clear();
          tableContentList.clear();
          columnNameList = new ArrayList<String>();
          tableContentList = new ArrayList<String>();
          continue;
        } 
        if (!inLine.equals("EOT") && !inLine.equals("EOF"))
          tableContentList.add(inLine); 
      } 
    } catch (FileNotFoundException e) {
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (IOException e) {
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } 
  }
  
  public  void getAllTableDataStructure(String fileName) {
    String line = null;
    String inLine = null;
    String line_str = null;
    String prevTableName = "NoTableName";
    String prevPrintLn = "NoPrintLn";
    String tableName = null;
    String columnValue = null;
    String columnName = null;
    String prevBracketedContent = "some";
    String currBracketedContent = null;
    int idioticMapping = 1;
    try {
      File fout = new File("DoNotTouch_TableDataStructure.ER");
      FileOutputStream fos = new FileOutputStream(fout);
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
      br = new BufferedReader(
          new FileReader(new File(fileName)));
      while ((inLine = br.readLine()) != null) {
        line = inLine;
        line_str = line.substring(0, line.indexOf("*"));
        line = line_str.substring(workflowUtilities.getLastSymbolIndex(line_str, '/'), line_str.length());
        tableName = line.substring(1, workflowUtilities.getFirstSymbolIndex(line, '['));
        currBracketedContent = line.substring(line.indexOf('[') + 1, workflowUtilities.getLastSymbolIndex(line, ']'));
        if (!tableName.equals(prevTableName) || !prevBracketedContent.equals(currBracketedContent))
          if (!prevPrintLn.equals("EOT")) {
            bw.write("EOT");
            prevPrintLn = "EOT";
            bw.newLine();
          }  
        if (!inLine.contains("[IGNORE]")) {
          if (prevPrintLn == "EOT" && inLine.startsWith("/PropertySet[CREATE]/PropertySet[")) {
            String temp = inLine.substring(33, 45);
            columnValue = temp.substring(0, temp.indexOf("]"));
            if (columnValue.equals("CREATE"))
              columnValue = "1"; 
            columnName = "PropertySet";
            bw.write(String.valueOf(tableName) + ":" + columnName + ":" + columnValue);
            prevPrintLn = String.valueOf(tableName) + ":" + columnName + ":" + columnValue;
            bw.newLine();
          } 
          if (inLine.startsWith("/PropertySet[CREATE]/TableOfContent[CREATE]/ISS_undPROD_undDEF") && prevPrintLn == "EOT") {
            String temp = inLine.substring(63, inLine.length());
            columnValue = temp.substring(0, temp.indexOf("]"));
            if (columnValue.equals("CREATE"))
              columnValue = "1"; 
            columnName = "PropertySet";
            bw.write(String.valueOf(tableName) + ":" + columnName + ":" + idioticMapping++);
            prevPrintLn = String.valueOf(tableName) + ":" + columnName + ":" + (idioticMapping - 1);
            bw.newLine();
          } else if (inLine.startsWith("/PropertySet[CREATE]/TableOfContent[CREATE]/ISS_undATTR_undDEF") && prevPrintLn == "EOT") {
            String temp = inLine.substring(63, inLine.length());
            columnValue = temp.substring(0, temp.indexOf("]"));
            if (columnValue.equals("CREATE"))
              columnValue = "1"; 
            columnName = "PropertySet";
            bw.write(String.valueOf(tableName) + ":" + columnName + ":" + idioticMapping++);
            prevPrintLn = String.valueOf(tableName) + ":" + columnName + ":" + (idioticMapping - 1);
            bw.newLine();
          } else if (inLine.startsWith("/PropertySet[CREATE]/TableOfContent[CREATE]/ISS_undCLASS_undDEF") && prevPrintLn == "EOT") {
            String temp = inLine.substring(64, inLine.length());
            columnValue = temp.substring(0, temp.indexOf("]"));
            if (columnValue.equals("CREATE"))
              columnValue = "1"; 
            columnName = "PropertySet";
            bw.write(String.valueOf(tableName) + ":" + columnName + ":" + idioticMapping++);
            prevPrintLn = String.valueOf(tableName) + ":" + columnName + ":" + (idioticMapping - 1);
            bw.newLine();
          } 
          columnValue = inLine.substring(inLine.indexOf("[@"), inLine.length());
          columnName = columnValue.substring(2, columnValue.indexOf("="));
          columnValue = columnValue.substring(columnValue.indexOf("=") + 2, workflowUtilities.getLastSymbolIndex(columnValue, ']') - 1);
          bw.write(String.valueOf(tableName) + ":" + columnName + ":" + columnValue);
          prevPrintLn = String.valueOf(tableName) + ":" + columnName + ":" + columnValue;
          bw.newLine();
        } 
        prevBracketedContent = currBracketedContent;
        prevTableName = tableName;
      } 
      bw.write("EOT");
      bw.newLine();
      bw.write("EOF");
      bw.flush();
      bw.close();
    } catch (FileNotFoundException e) {
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (Exception e) {
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } 
  }
  
  public  void getAllInsertStatements(String environment, String fileName) {
    String inLine = null;
    String t1 = null, t2 = null;
    ArrayList<String> tableContentList = new ArrayList<String>();
    ArrayList<String> columnDataList = new ArrayList<String>();
    String currTableName = null;
    try {
      br = new BufferedReader(
          new FileReader(new File(fileName)));
      File fout = new File("DoNotTouch_InsertTableScript.ER");
      FileOutputStream fos = new FileOutputStream(fout);
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
      while ((inLine = br.readLine()) != null) {
        if (inLine.equals("EOF")) {
          bw.flush();
          bw.close();
          continue;
        } 
        if (inLine.equals("EOT") && !tableContentList.isEmpty()) {
          currTableName = ((String)tableContentList.get(0)).substring(0, ((String)tableContentList.get(0)).indexOf(":")).replaceAll("_spc", "").replaceAll("_und", "");
          for (String columnName : tableContentList) {
            t1 = columnName.substring(columnName.indexOf(':') + 1, columnName.length());
            t2 = t1.substring(t1.indexOf(':') + 1, t1.length());
            columnDataList.add(t2);
          } 
          bw.write(workflowUtilities.formInsertQuery(environment, currTableName, columnDataList));
          bw.newLine();
          columnDataList.clear();
          tableContentList.clear();
          columnDataList = new ArrayList<String>();
          tableContentList = new ArrayList<String>();
          continue;
        } 
        if (!inLine.equals("EOT") && !inLine.equals("EOF"))
          tableContentList.add(inLine); 
      } 
    } catch (FileNotFoundException e) {
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (IOException e) {
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } 
  }
  
  public  void runDBScript(String environment, ArrayList<String> queryList) {
    String currQuery = null;
    String driver = CentralizedDataStore.configuration.get("derbyDriverString");
    String dbName = String.valueOf(CentralizedDataStore.systemDir) + "/" + (String)CentralizedDataStore.configuration.get("dbName");
    String additionalProperties = CentralizedDataStore.configuration.get("addProps");
    String connectionURL = "jdbc:h2:file:" + dbName + ";" + 
      additionalProperties + ";";
    try {
      Class.forName(driver);
      Connection conn = DriverManager.getConnection(connectionURL);
      Statement stmt = conn.createStatement();
      for (String query : queryList) {
        currQuery = query;
        stmt.executeUpdate(query);
      } 
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      System.out.println("Error while accessing DB with query:\n" + currQuery);
      e.printStackTrace();
      System.exit(0);
    } catch (ClassNotFoundException e) {
      System.out.println("Error in Client DB Driver String");
      e.printStackTrace();
      System.exit(0);
    } 
  }
  
  public static void readTableAndCreateView(String base, String target, ArrayList<String> tableNameList) throws IOException {
    String query = null;
    String viewName = null;
    String driver = CentralizedDataStore.configuration.get("derbyDriverString");
    String dbName = String.valueOf(CentralizedDataStore.systemDir) + "/" + (String)CentralizedDataStore.configuration.get("dbName");
    String connectionURL = "jdbc:h2:file:" + dbName + ";";
    try {
      Class.forName(driver);
      Connection conn = DriverManager.getConnection(connectionURL);
      Statement stmt = conn.createStatement();
      for (String tableName : tableNameList) {
        System.out.println(workflowUtilities.VerifyColumnList(base.toUpperCase(), target.toUpperCase(), tableName.toUpperCase()));
        query = workflowUtilities.formSelectQuery(base, target, tableName, CentralizedDataStore.columnList.get(tableName));
        viewName = String.valueOf(base) + "vs" + target + "_" + tableName;
        try {
          query = "CREATE VIEW " + viewName + " AS " + query;
          stmt.executeUpdate(query);
          CentralizedDataStore.viewNameList.add(viewName);
        } catch (Exception e) {
          CentralizedDataStore.issueTableNameList.add(tableName);
        } 
      } 
      stmt.close();
      conn.close();
    } catch (Exception e) {
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } 
  }
  
  public  void readDBAndWriteToExcel(String base, String target, ArrayList<String> tableNameList) {
    String query = null;
    String driver = CentralizedDataStore.configuration.get("derbyDriverString");
    String dbName = String.valueOf(CentralizedDataStore.systemDir) + "/" + (String)CentralizedDataStore.configuration.get("dbName");
    String connectionURL = "jdbc:h2:file:" + dbName + ";";
    ResultSetMetaData tableMetaData = null;
    ResultSet rs = null;
    HSSFWorkbook hSSFWorkbook = new HSSFWorkbook();
    HSSFCellStyle borderedCell = (HSSFCellStyle)hSSFWorkbook.createCellStyle();
    borderedCell.setBorderTop(BorderStyle.valueOf((short)1));
    borderedCell.setBorderBottom(BorderStyle.valueOf((short)1));
    borderedCell.setBorderLeft(BorderStyle.valueOf((short)1));
    borderedCell.setBorderRight(BorderStyle.valueOf((short)1));
    HSSFCellStyle errorStyle = (HSSFCellStyle)hSSFWorkbook.createCellStyle();
    errorStyle.setFillForegroundColor((short)1);
    errorStyle.setFillPattern(FillPatternType.forInt(1));
    errorStyle.setBorderTop(BorderStyle.valueOf((short)1));
    errorStyle.setBorderBottom(BorderStyle.valueOf((short)1));
    errorStyle.setBorderLeft(BorderStyle.valueOf((short)1));
    errorStyle.setBorderRight(BorderStyle.valueOf((short)1));
    HSSFCellStyle verifiedStyle = (HSSFCellStyle)hSSFWorkbook
      .createCellStyle();
    verifiedStyle.setFillForegroundColor((short)42);
    verifiedStyle.setFillPattern(FillPatternType.forInt(1));
    verifiedStyle.setBorderTop(BorderStyle.valueOf((short)1));
    verifiedStyle.setBorderBottom(BorderStyle.valueOf((short)1));
    verifiedStyle.setBorderLeft(BorderStyle.valueOf((short)1));
    verifiedStyle.setBorderRight(BorderStyle.valueOf((short)1));
    Sheet sheet = null;
    Row row = null;
    Cell cell = null;
    int centralCount = 1;
    int columnCount = 0;
    int testableColumn = 0;
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(new File(String.valueOf(base) + "vs" + target + ".xls"));
      Class.forName(driver);
      Connection conn = DriverManager.getConnection(connectionURL);
      Statement stmt = conn.createStatement();
      for (String tableName : tableNameList) {
        sheet = hSSFWorkbook.createSheet(tableName);
        row = sheet.createRow(0);
        query = "SELECT ";
        query = String.valueOf(query) + ((CentralizedDataStore.columnList.get(tableName) == null) ? "*" : CentralizedDataStore.columnList.get(tableName));
        query = String.valueOf(query) + " FROM " + target + "vs" + base + "_" + tableName;
        rs = stmt.executeQuery(query);
        while (rs.next())
          CentralizedDataStore.checkableContentList.add(rs.getString(1)); 
        query = "SELECT * FROM " + base + "vs" + target + "_" + tableName;
        rs = stmt.executeQuery(query);
        tableMetaData = rs.getMetaData();
        columnCount = tableMetaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
          cell = row.createCell(i - 1);
          cell.setCellStyle((CellStyle)borderedCell);
          cell.setCellValue(tableMetaData.getColumnName(i));
          if (tableMetaData.getColumnName(i).equalsIgnoreCase(CentralizedDataStore.columnList.get(tableName)))
            testableColumn = i; 
        } 
        while (rs.next()) {
          row = sheet.createRow(centralCount);
          for (int j = 1; j <= columnCount; j++) {
            cell = row.createCell(j - 1);
            cell.setCellStyle((CellStyle)borderedCell);
            if (j == testableColumn)
              if (CentralizedDataStore.checkableContentList.contains(rs.getString(j))) {
                CentralizedDataStore.checkableContentList.remove(rs.getString(j));
                cell.setCellStyle((CellStyle)verifiedStyle);
              } else {
                cell.setCellStyle((CellStyle)errorStyle);
              }  
            cell.setCellValue(rs.getString(j));
          } 
          centralCount++;
        } 
        testableColumn = 0;
        centralCount = 1;
        CentralizedDataStore.checkableContentList.clear();
      } 
      hSSFWorkbook.write(fos);
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      System.out.println("Error while accessing DB ");
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (ClassNotFoundException e) {
      System.out.println("Error in Client DB Driver String");
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (FileNotFoundException e) {
      System.out.println("Error while accessing file");
      System.out.println("The File is in access by some other application");
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (Exception e) {
      System.out.println("Unknown Exception!!!!");
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } 
  }
  
  public  void UpdateDataDetails(String environment, ArrayList<String> tableNameList) throws IOException {
    File fout = new File("DoNotTouch_UpdateBCindexScript.ER");
    FileOutputStream fos = new FileOutputStream(fout);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
    List<String> currentList = new ArrayList<String>();
    String currQuery = null;
    String driver = CentralizedDataStore.configuration.get("derbyDriverString");
    String dbName = String.valueOf(CentralizedDataStore.systemDir) + "/" + (String)CentralizedDataStore.configuration.get("dbName");
    String additionalProperties = CentralizedDataStore.configuration.get("addProps");
    String connectionURL = "jdbc:h2:file:" + dbName + ";" + 
      additionalProperties + ";";
    ResultSet rs = null;
    int AttributeMax = 0, ClassMax = 0, ProdMax = 0;
    try {
      Class.forName(driver);
      Connection conn = DriverManager.getConnection(connectionURL);
      Statement stmt = conn.createStatement();
      Map<Integer, String> contentMap = new HashMap<Integer, String>();
      if (tableNameList.contains("ISSATTRDEF")) {
        rs = stmt.executeQuery("SELECT max(CAST(XML_PROPERTYSET as int)) from " + environment + "_" + "ISSATTRDEF");
        rs.next();
        AttributeMax = rs.getInt(1);
      } else {
        AttributeMax = 0;
      } 
      if (tableNameList.contains("ISSCLASSDEF")) {
        rs = stmt.executeQuery("SELECT max(CAST(XML_PROPERTYSET as int)) from " + environment + "_" + "ISSCLASSDEF");
        rs.next();
        ClassMax = rs.getInt(1);
      } else {
        ClassMax = 0;
      } 
      if (tableNameList.contains("ISSPRODDEF")) {
        rs = stmt.executeQuery("SELECT max(CAST(XML_PROPERTYSET as int)) from " + environment + "_" + "ISSPRODDEF");
        rs.next();
        ProdMax = rs.getInt(1);
      } else {
        ProdMax = 0;
      } 
      CentralizedDataStore.maxIndexCount = workflowUtilities.maximum(AttributeMax, ClassMax, ProdMax);
      currentList.addAll(tableNameList);
      if (currentList.contains("ISSATTRDEF"))
        currentList.remove("ISSATTRDEF"); 
      if (currentList.contains("ISSPRODDEF"))
        currentList.remove("ISSPRODDEF"); 
      if (currentList.contains("ISSCLASSDEF"))
        currentList.remove("ISSCLASSDEF"); 
      if (currentList.contains("TableOfContent"))
        currentList.remove("TableOfContent"); 
      if (currentList.contains("PropertySet"))
        currentList.remove("PropertySet"); 
      for (String table : currentList) {
        rs = stmt.executeQuery("SELECT XML_PROPERTYSET," + (String)CentralizedDataStore.columnList.get(table) + " FROM " + environment + "_" + table + " ORDER BY 1");
        while (rs.next())
          contentMap.put(Integer.valueOf(Integer.parseInt(rs.getString(1))), rs.getString(2)); 
        Set<Integer> keySet = contentMap.keySet();
        for (Iterator<Integer> localIterator2 = keySet.iterator(); localIterator2.hasNext(); ) {
          int key = ((Integer)localIterator2.next()).intValue();
          if (key <= CentralizedDataStore.maxIndexCount)
            continue; 
          stmt.executeUpdate("Update " + environment + "_" + table + " SET XML_PROPERTYSET='" + (key - CentralizedDataStore.maxIndexCount) + "' WHERE XML_PROPERTYSET='" + key + "'");
          bw.write("Update " + environment + "_" + table + " SET XML_PROPERTYSET='" + (key - CentralizedDataStore.maxIndexCount) + "' WHERE XML_PROPERTYSET='" + key + "'");
          bw.newLine();
        } 
        contentMap.clear();
      } 
      bw.flush();
      bw.close();
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      System.out.println("Error while accessing DB with query:\n" + currQuery);
      e.printStackTrace();
      System.exit(0);
    } catch (ClassNotFoundException e) {
      System.out.println("Error in Client DB Driver String");
      e.printStackTrace();
      System.exit(0);
    } 
  }
  
  public  void UpdateDatabaseData(String environment, ArrayList<String> tableNameList) throws IOException {
    File fout = new File("DoNotTouch_UpdatePropertySetScript.ER");
    FileOutputStream fos = new FileOutputStream(fout);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
    List<String> currentList = new ArrayList<String>();
    Map<Integer, String> contentMapper = new HashMap<Integer, String>();
    String currQuery = null;
    String value = null;
    String type = null;
    String driver = CentralizedDataStore.configuration.get("derbyDriverString");
    String dbName = String.valueOf(CentralizedDataStore.systemDir) + "/" + (String)CentralizedDataStore.configuration.get("dbName");
    String additionalProperties = CentralizedDataStore.configuration.get("addProps");
    String connectionURL = "jdbc:h2:file:" + dbName + ";" + 
      additionalProperties + ";";
    ResultSet rs = null;
    try {
      Class.forName(driver);
      Connection conn = DriverManager.getConnection(connectionURL);
      Statement stmt = conn.createStatement();
      Statement mapQuery = conn.createStatement();
      if (tableNameList.contains("ISSATTRDEF")) {
        rs = stmt.executeQuery("select XML_PropertySet,XML_VODNAME from " + environment + "_" + "ISSATTRDEF");
        while (rs.next())
          contentMapper.put(Integer.valueOf(Integer.parseInt(rs.getString(1))), String.valueOf(rs.getString(2)) + " || Attribute"); 
      } 
      if (tableNameList.contains("ISSCLASSDEF")) {
        rs = stmt.executeQuery("select XML_PropertySet,XML_VODNAME from " + environment + "_" + "ISSCLASSDEF");
        while (rs.next())
          contentMapper.put(Integer.valueOf(Integer.parseInt(rs.getString(1))), String.valueOf(rs.getString(2)) + " || Class"); 
      } 
      if (tableNameList.contains("ISSPRODDEF")) {
        rs = stmt.executeQuery("select XML_PropertySet,XML_VODNAME from " + environment + "_" + "ISSPRODDEF");
        while (rs.next())
          contentMapper.put(Integer.valueOf(Integer.parseInt(rs.getString(1))), String.valueOf(rs.getString(2)) + " || Product"); 
      } 
      currentList.addAll(tableNameList);
      if (currentList.contains("ISSATTRDEF"))
        currentList.remove("ISSATTRDEF"); 
      if (currentList.contains("ISSPRODDEF"))
        currentList.remove("ISSPRODDEF"); 
      if (currentList.contains("ISSCLASSDEF"))
        currentList.remove("ISSCLASSDEF"); 
      if (currentList.contains("TableOfContent"))
        currentList.remove("TableOfContent"); 
      Set<Integer> keySet = contentMapper.keySet();
      for (String tableName : currentList) {
        for (Iterator<Integer> iterator = keySet.iterator(); iterator.hasNext(); ) {
          int i = ((Integer)iterator.next()).intValue();
          stmt.executeUpdate("Update " + environment + "_" + tableName + " SET XML_PropertySet='" + i + " || " + ((String)contentMapper.get(Integer.valueOf(i))).replaceAll("'", "''") + "' WHERE XML_PropertySet='" + i + "'");
          bw.write("Update " + environment + "_" + tableName + " SET XML_PropertySet='" + i + " || " + ((String)contentMapper.get(Integer.valueOf(i))).replaceAll("'", "''") + "' WHERE XML_PropertySet='" + i + "'");
          bw.newLine();
        } 
      } 
      mapQuery.executeUpdate("CREATE TABLE " + environment + "_" + "Mapping(ID INT,Name VARCHAR(1000),Type VARCHAR(500))");
      for (Iterator<Integer> localIterator2 = keySet.iterator(); localIterator2.hasNext(); ) {
        int i = ((Integer)localIterator2.next()).intValue();
        value = ((String)contentMapper.get(Integer.valueOf(i))).substring(0, ((String)contentMapper.get(Integer.valueOf(i))).indexOf("||"));
        type = ((String)contentMapper.get(Integer.valueOf(i))).substring(((String)contentMapper.get(Integer.valueOf(i))).lastIndexOf('|') + 1, ((String)contentMapper.get(Integer.valueOf(i))).length());
        mapQuery.executeUpdate("INSERT INTO " + environment + "_" + "Mapping values(" + i + ",'" + value.replaceAll("'", "''") + "','" + type + "')");
      } 
      bw.flush();
      bw.close();
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      System.out.println("Error while accessing DB with query:\n" + currQuery);
      e.printStackTrace();
      System.exit(0);
    } catch (ClassNotFoundException e) {
      System.out.println("Error in Client DB Driver String");
      e.printStackTrace();
      System.exit(0);
    } 
  }
  
  public static void readDBAndWriteToExcel_Enhanced(String base, String target, ArrayList<String> tableNameList) {
    String query = null;
    String driver = CentralizedDataStore.configuration.get("derbyDriverString");
    String dbName = String.valueOf(CentralizedDataStore.systemDir) + "/" + (String)CentralizedDataStore.configuration.get("dbName");
    String connectionURL = "jdbc:h2:file:" + dbName + ";";
    ResultSetMetaData tableMetaData = null;
    ResultSet rs = null;
    ResultSet rs_run = null;
    ResultSet rs_b = null;
    ResultSet rs_t = null;
    HSSFWorkbook hSSFWorkbook = new HSSFWorkbook();
    HSSFCellStyle borderedCell = (HSSFCellStyle)hSSFWorkbook.createCellStyle();
    borderedCell.setBorderTop(BorderStyle.valueOf((short)1));
    borderedCell.setBorderBottom(BorderStyle.valueOf((short)1));
    borderedCell.setBorderLeft(BorderStyle.valueOf((short)1));
    borderedCell.setBorderRight(BorderStyle.valueOf((short)1));
    HSSFCellStyle errorStyle = (HSSFCellStyle)hSSFWorkbook.createCellStyle();
    errorStyle.setFillForegroundColor((short)0);
    errorStyle.setFillPattern(FillPatternType.forInt(1));
    errorStyle.setBorderTop(BorderStyle.valueOf((short)1));
    errorStyle.setBorderBottom(BorderStyle.valueOf((short)1));
    errorStyle.setBorderLeft(BorderStyle.valueOf((short)1));
    errorStyle.setBorderRight(BorderStyle.valueOf((short)1));
    HSSFCellStyle verifiedStyle = (HSSFCellStyle)hSSFWorkbook
      .createCellStyle();
    verifiedStyle.setFillForegroundColor((short)42);
    verifiedStyle.setFillPattern(FillPatternType.forInt(1));
    verifiedStyle.setBorderTop(BorderStyle.valueOf((short)1));
    verifiedStyle.setBorderBottom(BorderStyle.valueOf((short)1));
    verifiedStyle.setBorderLeft(BorderStyle.valueOf((short)1));
    verifiedStyle.setBorderRight(BorderStyle.valueOf((short)1));
    Sheet sheet = null;
    Row row = null;
    Cell cell = null;
    int centralCount = 2;
    int columnCount = 0;
    FileOutputStream fos = null;
    int currentContentIndex = 0;
    int totalCount = 0;
    try {
      fos = new FileOutputStream(new File(String.valueOf(base) + "vs" + target + "_ResultSet.xls"));
      Class.forName(driver);
      Connection conn = DriverManager.getConnection(connectionURL);
      Statement stmt = conn.createStatement();
      Statement stmt_run = conn.createStatement();
      Statement stmt_b = conn.createStatement();
      Statement stmt_t = conn.createStatement();
      Statement stmt_s = conn.createStatement();
      boolean src = false;
      boolean tar = false;
      if (src && tar)
        totalCount = 0; 
      for (String tableName : tableNameList) {
        if (CentralizedDataStore.DND.contains(tableName))
          continue; 
        sheet = hSSFWorkbook.createSheet((CentralizedDataStore.aliasTableNameList.get(tableName) == null) ? tableName : CentralizedDataStore.aliasTableNameList.get(tableName));
        row = sheet.createRow(0);
        if (CentralizedDataStore.contentMapTables.contains(tableName)) {
          cell = row.createCell(0);
          cell.setCellValue("UI Guide:");
          cell = row.createCell(1);
          cell.setCellValue(CentralizedDataStore.contentMap.get(tableName));
        } 
        row = sheet.createRow(1);
        query = "SELECT ";
        query = String.valueOf(query) + ((CentralizedDataStore.columnList.get(tableName) == null) ? "*" : CentralizedDataStore.columnList.get(tableName));
        if (CentralizedDataStore.indexTableNameList.contains(tableName)) {
          query = String.valueOf(query) + " FROM " + base + "_" + tableName;
          rs = stmt.executeQuery(query);
          while (rs.next())
            CentralizedDataStore.checkableContentList.add(rs.getString(1)); 
        } else {
          query = String.valueOf(query) + ",XML_PROPERTYSET FROM " + base + "vs" + target + "_" + tableName;
          rs = stmt.executeQuery(query);
          while (rs.next()) {
            CentralizedDataStore.checkableContentList.add(rs.getString(1));
            CentralizedDataStore.PS_checkableContentList.add(rs.getString(2).substring(rs.getString(2).indexOf("||") + 1, rs.getString(2).length()));
          } 
        } 
        if (CentralizedDataStore.indexTableNameList.contains(tableName)) {
          query = "SELECT TOP 1 * FROM " + base + "_" + tableName;
        } else {
          query = "SELECT TOP 1 * FROM " + base + "vs" + target + "_" + tableName;
        } 
        rs = stmt.executeQuery(query);
        tableMetaData = rs.getMetaData();
        columnCount = tableMetaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
          cell = row.createCell(i - 1);
          cell.setCellStyle((CellStyle)borderedCell);
          cell.setCellValue(tableMetaData.getColumnName(i));
        } 
        if (tableName.equalsIgnoreCase("ObjectProductImpExpBC")) {
          String currQuery = "DROP TABLE IF EXISTS PRODCOMP";
          stmt_s.executeUpdate(currQuery);
          currQuery = "CREATE TABLE PRODCOMP (PART_NUM VARCHAR(100), ACTION VARCHAR(10), FIELD_NM VARCHAR(100), SOURCE_VAL VARCHAR(100), TARGET_VAL VARCHAR(100))";
          stmt_s.executeUpdate(currQuery);
          query = "SELECT XML_PARTNUMBER FROM " + base + "_" + tableName + " EXCEPT SELECT XML_PARTNUMBER FROM " + target + "_" + tableName + " order by 1";
          rs_b = stmt_s.executeQuery(query);
          while (rs_b.next()) {
            currQuery = "INSERT INTO PRODCOMP (PART_NUM, ACTION, FIELD_NM, SOURCE_VAL, TARGET_VAL) values ('" + rs_b.getString("XML_PARTNUMBER") + "', 'DELETE',' ',' ',' ')";
            stmt_b.executeUpdate(currQuery);
          } 
          query = "SELECT XML_PARTNUMBER FROM " + target + "_" + tableName + " EXCEPT SELECT XML_PARTNUMBER FROM " + base + "_" + tableName + " order by 1";
          rs_t = stmt.executeQuery(query);
          while (rs_t.next()) {
            currQuery = "INSERT INTO PRODCOMP (PART_NUM, ACTION, FIELD_NM, SOURCE_VAL, TARGET_VAL) values ('" + rs_t.getString("XML_PARTNUMBER") + "', 'ADD',' ',' ',' ')";
            stmt_t.executeUpdate(currQuery);
          } 
        } 
        String sourceContent = null;
        String targetContent = null;
        String currentWordB = null;
        currentContentIndex = 0;
        int safePlayIndex = 0;
        totalCount = CentralizedDataStore.checkableContentList.size();
        for (String currentWordA : CentralizedDataStore.checkableContentList) {
          word1 = currentWordA;
          if (!CentralizedDataStore.indexTableNameList.contains(tableName)) {
            currentWordB = CentralizedDataStore.PS_checkableContentList.get(safePlayIndex++);
            word2 = currentWordB;
            currentContentIndex++;
            if (CentralizedDataStore.keyList.contains(String.valueOf(currentWordA) + "-" + currentWordB))
              continue; 
            CentralizedDataStore.keyList.add(String.valueOf(currentWordA) + "-" + currentWordB);
            query = "SELECT * FROM " + base + "vs" + target + "_" + tableName + " where " + (String)CentralizedDataStore.columnList.get(tableName) + "='" + currentWordA.replaceAll("'", "''") + "' AND XML_PROPERTYSET like '%" + currentWordB.replaceAll("'", "''") + "%'";
            rs_run = stmt_run.executeQuery(query);
            tableMetaData = rs_run.getMetaData();
            query = "SELECT * FROM " + target + "vs" + base + "_" + tableName + " where " + (String)CentralizedDataStore.columnList.get(tableName) + "='" + currentWordA.replaceAll("'", "''") + "' AND XML_PROPERTYSET like '%" + currentWordB.replaceAll("'", "''") + "%'";
            rs = stmt.executeQuery(query);
            while ((src = rs.next()) && (tar = rs_run.next())) {
              row = sheet.createRow(centralCount);
              for (int j = 1; j <= columnCount; j++) {
                cell = row.createCell(j - 1);
                cell.setCellStyle((CellStyle)borderedCell);
                sourceContent = rs_run.getString(j);
                targetContent = rs.getString(j);
                if (sourceContent == null)
                  sourceContent = String.valueOf(sourceContent) + " "; 
                if (targetContent == null)
                  targetContent = String.valueOf(targetContent) + " "; 
                if (CentralizedDataStore.ignoreColumnNameList.contains(tableMetaData.getColumnName(j))) {
                  cell.setCellStyle((CellStyle)borderedCell);
                } else if (ContentCheck(sourceContent)) {
                  cell.setCellStyle((CellStyle)borderedCell);
                } else if (targetContent.equals(sourceContent)) {
                  cell.setCellStyle((CellStyle)verifiedStyle);
                } else {
                  cell.setCellStyle((CellStyle)errorStyle);
                  if (tableName.contains("ObjectProductImpExpBC")) {
                    String currQuery = "INSERT INTO PRODCOMP (PART_NUM, ACTION, FIELD_NM, SOURCE_VAL, TARGET_VAL) values ('" + rs.getString("XML_PARTNUMBER") + "', 'UPDATE','" + tableMetaData.getColumnName(j) + "','" + rs.getString(j) + "','" + rs_run.getString(j) + "')";
                    stmt_b.executeUpdate(currQuery);
                  } 
                } 
                cell.setCellType(CellType.forInt(1));
                cell.setCellValue(rs_run.getString(j));
              } 
              centralCount++;
            } 
            if (rs_run.next()) {
              row = sheet.createRow(centralCount);
              for (int j = 1; j <= columnCount; j++) {
                cell = row.createCell(j - 1);
                cell.setCellStyle((CellStyle)borderedCell);
                cell.setCellStyle((CellStyle)errorStyle);
                cell.setCellType(CellType.forInt(1));
                cell.setCellValue(rs_run.getString(j));
              } 
              centralCount++;
            } 
            workflowUtilities.printProgBar(100 * currentContentIndex / totalCount, tableName);
            continue;
          } 
          currentContentIndex++;
          if (CentralizedDataStore.keyList.contains(currentWordA))
            continue; 
          CentralizedDataStore.keyList.add(currentWordA);
          query = "SELECT * FROM " + base + "_" + tableName + " where " + (String)CentralizedDataStore.columnList.get(tableName) + "='" + currentWordA.replaceAll("'", "''") + "' ORDER BY " + (String)CentralizedDataStore.columnList.get(tableName);
          rs_run = stmt_run.executeQuery(query);
          tableMetaData = rs_run.getMetaData();
          query = "SELECT * FROM " + target + "_" + tableName + " where " + (String)CentralizedDataStore.columnList.get(tableName) + "='" + currentWordA.replaceAll("'", "''") + "' ORDER BY " + (String)CentralizedDataStore.columnList.get(tableName);
          rs = stmt.executeQuery(query);
          while ((src = rs.next()) && (tar = rs_run.next())) {
            row = sheet.createRow(centralCount);
            for (int j = 1; j <= columnCount; j++) {
              cell = row.createCell(j - 1);
              cell.setCellStyle((CellStyle)borderedCell);
              sourceContent = rs_run.getString(j);
              if (tableMetaData.getColumnName(j).equalsIgnoreCase("ID") || tableMetaData.getColumnName(j).equalsIgnoreCase("XML_PROPERTYSET") || tableMetaData.getColumnName(j).contains("COMMENT")) {
                cell.setCellStyle((CellStyle)borderedCell);
              } else if (ContentCheck(sourceContent)) {
                cell.setCellStyle((CellStyle)borderedCell);
              } else if (rs.getString(j).equalsIgnoreCase(rs_run.getString(j))) {
                cell.setCellStyle((CellStyle)verifiedStyle);
              } else {
                cell.setCellStyle((CellStyle)errorStyle);
              }
              cell.setCellType(CellType.forInt(1));
              cell.setCellValue(rs_run.getString(j));
            } 
            centralCount++;
          } 
          if (rs_run.next()) {
            row = sheet.createRow(centralCount);
            for (int j = 1; j <= columnCount; j++) {
              cell = row.createCell(j - 1);
              cell.setCellStyle((CellStyle)borderedCell);
              cell.setCellStyle((CellStyle)errorStyle);
              cell.setCellType(CellType.forInt(1));
              cell.setCellValue(rs_run.getString(j));
            } 
            centralCount++;
          } 
          workflowUtilities.printProgBar(100 * currentContentIndex / totalCount, tableName);
        } 
        centralCount = 2;
        safePlayIndex = 0;
        CentralizedDataStore.checkableContentList.clear();
        CentralizedDataStore.PS_checkableContentList.clear();
        CentralizedDataStore.keyList.clear();
      } 
      int testableColumn = 0;
      if (tableNameList.contains("ObjectRuleNodesImpExpBC")) {
        CentralizedDataStore.checkableContentList.clear();
        sheet = hSSFWorkbook.createSheet("ObjectRuleNodesImpExpBC");
        row = sheet.createRow(0);
        cell = row.createCell(0);
        cell.setCellValue("Appended to result Set as requested by design Team");
        row = sheet.createRow(1);
        query = "SELECT ";
        query = String.valueOf(query) + ((CentralizedDataStore.columnList.get("ObjectRuleNodesImpExpBC") == null) ? "*" : CentralizedDataStore.columnList.get("ObjectRuleNodesImpExpBC"));
        query = String.valueOf(query) + " FROM " + target + "vs" + base + "_" + "ObjectRuleNodesImpExpBC";
        rs = stmt.executeQuery(query);
        while (rs.next())
          CentralizedDataStore.checkableContentList.add(rs.getString(1)); 
        query = "SELECT * FROM " + base + "vs" + target + "_" + "ObjectRuleNodesImpExpBC";
        rs = stmt.executeQuery(query);
        tableMetaData = rs.getMetaData();
        columnCount = tableMetaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
          cell = row.createCell(i - 1);
          cell.setCellStyle((CellStyle)borderedCell);
          cell.setCellValue(tableMetaData.getColumnName(i));
          if (tableMetaData.getColumnName(i).equalsIgnoreCase(CentralizedDataStore.columnList.get("ObjectRuleNodesImpExpBC")))
            testableColumn = i; 
        } 
        while (rs.next()) {
          row = sheet.createRow(centralCount);
          for (int j = 1; j <= columnCount; j++) {
            cell = row.createCell(j - 1);
            cell.setCellStyle((CellStyle)borderedCell);
            if (j == testableColumn)
              if (CentralizedDataStore.checkableContentList.contains(rs.getString(j))) {
                CentralizedDataStore.checkableContentList.remove(rs.getString(j));
                cell.setCellStyle((CellStyle)verifiedStyle);
              } else {
                cell.setCellStyle((CellStyle)errorStyle);
              }  
            cell.setCellValue(rs.getString(j));
          } 
          centralCount++;
        } 
      } 
      testableColumn = 0;
      centralCount = 2;
      CentralizedDataStore.checkableContentList.clear();
      hSSFWorkbook.write(fos);
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      System.out.println("Error while accessing DB " + query);
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (ClassNotFoundException e) {
      System.out.println("Error in Client DB Driver String");
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (FileNotFoundException e) {
      System.out.println("Error while accessing file");
      System.out.println("The File is in access by some other application");
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (Exception e) {
      System.out.println("Unknown Exception!!!!" + query + " " + word1 + word2);
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } 
  }
  
  public static boolean ContentCheck(String source) {
    boolean returnValue = false;
    if (source.length() < 2)
      return returnValue; 
    if (source.matches("[9]+"))
      returnValue = true; 
    if (String.valueOf(source.charAt(0)).matches("[0-9]+") && 
      source.charAt(1) == '-' && 
      source.length() < 12 && 
      String.valueOf(source.substring(2, source.length())).matches("[A-Z0-9]+"))
      returnValue = true; 
    if (String.valueOf(source.charAt(0)).matches("[0-9]+") && 
      source.charAt(1) == '-' && 
      String.valueOf(source.substring(2, source.length())).matches("[0-9]+"))
      returnValue = true; 
    return returnValue;
  }
  
  public static void comparisonWriteToExcel(String base, String target) {
    String query = null;
    String driver = CentralizedDataStore.configuration.get("derbyDriverString");
    String dbName = String.valueOf(CentralizedDataStore.systemDir) + "/" + (String)CentralizedDataStore.configuration.get("dbName");
    String connectionURL = "jdbc:h2:file:" + dbName + ";";
    ResultSet rs = null;
    HSSFWorkbook hSSFWorkbook = new HSSFWorkbook();
    HSSFCellStyle borderedCell = (HSSFCellStyle)hSSFWorkbook.createCellStyle();
    borderedCell.setBorderTop(BorderStyle.valueOf((short)1));
    borderedCell.setBorderBottom(BorderStyle.valueOf((short)1));
    borderedCell.setBorderLeft(BorderStyle.valueOf((short)1));
    borderedCell.setBorderRight(BorderStyle.valueOf((short)1));
    Sheet sheet = null;
    Row row = null;
    Cell cell = null;
    int centralCount = 2;
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(new File(String.valueOf(base) + "vs" + target + "_prod_comp.xls"));
      Class.forName(driver);
      Connection conn = DriverManager.getConnection(connectionURL);
      Statement stmt = conn.createStatement();
      sheet = hSSFWorkbook.createSheet("ProductCompare");
      row = sheet.createRow(0);
      cell = row.createCell(0);
      cell.setCellValue("Product Comparison Guide:");
      cell = row.createCell(1);
      cell.setCellValue("Source: " + base);
      cell = row.createCell(2);
      cell.setCellValue("Target: " + target);
      row = sheet.createRow(1);
      cell = row.createCell(0);
      cell.setCellStyle((CellStyle)borderedCell);
      cell.setCellValue("PART_NUM");
      cell = row.createCell(1);
      cell.setCellStyle((CellStyle)borderedCell);
      cell.setCellValue("ACTION");
      cell = row.createCell(2);
      cell.setCellStyle((CellStyle)borderedCell);
      cell.setCellValue("FIELD_NAME");
      cell = row.createCell(3);
      cell.setCellStyle((CellStyle)borderedCell);
      cell.setCellValue("SOURCE_VAL");
      cell = row.createCell(4);
      cell.setCellStyle((CellStyle)borderedCell);
      cell.setCellValue("TARGET_VAL");
      query = "SELECT * FROM PRODCOMP";
      rs = stmt.executeQuery(query);
      while (rs.next()) {
        row = sheet.createRow(centralCount);
        cell = row.createCell(0);
        cell.setCellStyle((CellStyle)borderedCell);
        cell.setCellValue(rs.getString("PART_NUM"));
        cell = row.createCell(1);
        cell.setCellStyle((CellStyle)borderedCell);
        cell.setCellValue(rs.getString("ACTION"));
        cell = row.createCell(2);
        cell.setCellStyle((CellStyle)borderedCell);
        cell.setCellValue(rs.getString("FIELD_NM"));
        cell = row.createCell(3);
        cell.setCellStyle((CellStyle)borderedCell);
        cell.setCellValue(rs.getString("SOURCE_VAL"));
        cell = row.createCell(4);
        cell.setCellStyle((CellStyle)borderedCell);
        cell.setCellValue(rs.getString("TARGET_VAL"));
        centralCount++;
      } 
      hSSFWorkbook.write(fos);
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      System.out.println("Error while accessing DB ");
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (ClassNotFoundException e) {
      System.out.println("Error in Client DB Driver String");
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (FileNotFoundException e) {
      System.out.println("Error while accessing file");
      System.out.println("The File is in access by some other application");
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (Exception e) {
      System.out.println("Unknown Exception!!!!");
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } 
  }
}
