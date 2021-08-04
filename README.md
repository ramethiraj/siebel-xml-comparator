# Introduction
XMLComparator is not just *any XML Comparator*, this tool is created with a purpose to enable Siebel CRM Configurations Managers / Siebel Developers / Admins to **Compare two different Siebel Environments** using the Siebel Object XML Export. This provides the capability of identifying the newly added / edited Sibel Product/ Product Promotions / Bundles or any line item. This offering is custom made for the purpose of working with the Siebel products Export.

> This Work have aged for almost 7 Years. I created this tool during my first 6 Months of Work Experience and i moved out of that space. Since i started to collect all the engaging/interesting work i have done. Even-thought i have gained a lot of work experience, i cherish this beginner code and wanted to have it as-is without having any influence from the new qualified me. Thats the reason why the code doesn't have any logger, poor Object handling and a very basic programming paradigm approach.I have just refurbished this code into a Maven Project.


# Product Description
## Oracle Siebel
Oracle's Siebel CRM Technology provides the server framework to support Siebel Applications. It delivers solutions for: Development, Deployment, Diagnostic, Integration, Productivity, and Mobile services. [Oracle Siebel](https://www.oracle.com/cx/siebel/) 

### Siebel Product XML
A Siebel product XML is Export extracted from any Siebel installation which can be used to import new changes from one environment to another (ex: Dev to Test)

## Design
As you can see in the code the product has a very primitive Code-First approach so as to achieve the target solution disregarding Object Oriented Design / Implementation Standards.

## Execution Instructions
1. After cloning the project, build using the IDE Maven plugin or `mvn clean install`
2. Review the code to understand the flow
3. _src/main/java_ - has the code for Executing Siebel XML Comparison 
4. StartXMLComparator.bat - script which can be used to execute the tool
5. Configuration.xlsm - Scripted Configruation Excel File which is used by the User / App to exchange Configuration.
6. _src/main/resources_ - has the default XSL Template to translate the input XML to code understandable format
