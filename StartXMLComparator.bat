@echo off
mode con: cols=130 lines=250
:top
cls
COLOR 0A

echo ------------------------------------------------------------
echo *****************Seibel Automation Utilities****************
echo --------------- End2End Business Solution ------------------
echo ---------------------XML Comparision------------------------
echo ------------------------------------------------------------
echo.
echo.
echo ------------------------------------------------------------------
echo [This version of Utilities  requires a pre-installed java JDK    ]
echo ------------------------------------------------------------------
echo.
echo 1)Configure the Configuration.xlsm/XML_CONFIG Sheet
echo 2)Then Place the listed XML files in the Current Folder
echo 3)The Program requires a minimum of 500MB Free RAM Space and 200MB of DISK SPACE
echo Once you are done.
pause 1
java  -Xms256m -Xmx512m -jar  target/XMLComparator.jar com.siebel.comparator.WorkFlowExecutor "delete"
pause 1
