package com.siebel.comparator;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

public class XMLTransformer {
  public void commenceTrasform(String fileName) {
    String inFile = fileName;
    String outFile = "DoNotTouch_DYNA_FLAT_XML.ER";
    URL xslResource = getClass().getClassLoader().getResource("DoNotTouch_DesignDocument.xsl");
    String xslFile;
    if (xslResource == null) {
      throw new IllegalArgumentException("XSL - file not found!");
    } else {
      xslFile= xslResource.getPath();
    }
    xsl(inFile, outFile, xslFile);
  }
  
  public void xsl(String inFilename, String outFilename, String xslFilename) {
    try {
      TransformerFactory factory = TransformerFactory.newInstance();
      Templates template = factory.newTemplates(new StreamSource(
            new FileInputStream(xslFilename)));
      Transformer xformer = template.newTransformer();
      Source source = new StreamSource(new FileInputStream(inFilename));
      Result result = new StreamResult(new FileOutputStream(outFilename));
      xformer.transform(source, result);
    } catch (FileNotFoundException e) {
      System.out.println(e);
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (TransformerConfigurationException e) {
      System.out.println(e);
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      StringSelection stringSelection = new StringSelection(writer.toString());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
      e.printStackTrace();
      System.exit(0);
    } catch (TransformerException e) {
      System.out.println(e);
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
