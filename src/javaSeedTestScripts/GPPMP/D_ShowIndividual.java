package javaSeedTestScripts.GPPMP;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javaSeed.constants.Const;
import javaSeed.objectRepository.OR;
import javaSeed.utils.Screenshot;
import javaSeedTestScripts.GenericMethods;
import javaSeedTestScripts.TestConsts;
import javaSeedTestScripts.nonGUIutilities.DatabaseUtils;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class D_ShowIndividual {

	private WebDriver driver = Const.driver;
	private HashMap<String, String[]> ORMap = Const.ORMap;
	private ExtentTest TestLogger = Const.etTestCases;

	//------------------------ Imported Variables Collection
	String TestAppURL 		= TestConsts.ENV_GPPMP_URL;
	String strNewdate       = TestConsts.ENV_GPPMP_BussDate;
	String TestAppUser 		= TestConsts.ENV_GPPMP_UserName1;
	String TestAppUserPwd	= TestConsts.ENV_GPPMP_UserName1_Pwd;
	/*       ###########################################################################################
    Name                      : VerifyFileStatusForIDFs()
    Description        		  : verify individual transaction status
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          :
    							1. String FileName: name of the input xml
    							2. String Status: expected status has to be mentioned in the test data sheet
    							3. String E2EKey: E2E has to be retrive from the input xml
     							4. String DBQuery: pass the DB query for the transaction that looking for in the test data sheet
    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test


    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/
	public String VerifyIndividualTxnsStatus(String FileName, String E2EKey, String DBQuery, String Status) throws IOException {

		try {
			int StatusColNumber = -1;
			int MIDColNumber = -1;
			int MIDRow = -1;
			String AcutalStatus;

			// Getting E2EID from File
			String E2EID = TestConsts.SUPER_XML_FILE_DATAMAP.get(FileName).get(E2EKey);
			//String E2EID = "E2E112132145";

			//	Building the DB Query
			String[] arrDBQ=DBQuery.split("<>");
			String ActualDBQ=arrDBQ[0]+E2EID+arrDBQ[1];

			// DB Extract				
			List<String> gppMID = DatabaseUtils.DBExtract(ActualDBQ);
			if(gppMID.isEmpty()){
				System.out.println("DB Extraction of Transaction Search Failed for: "+E2EID);
				TestLogger.log(LogStatus.FATAL,"DB Extraction of Transaction Search Failed for: "+E2EID);
				return "Fatal";
			}



			// Switching frames to reach Search Summary table
			driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("listIframe~~INDIVIDUAL_SEARCH").switchTo().frame("queueFrame");

			// Get the Column number that contains 'Status'

			List<WebElement> Colms =OR.FindElements(driver, ORMap.get("MP_Individual_Search_ForwardProcessingTable_Header"));
			for (int i = 0; i <= Colms.size() - 1; i++) {
				if (Colms.get(i).getAttribute("alias").contentEquals("Status") && Colms.get(i).isDisplayed()) {
					StatusColNumber = i;
					break;
				}else if(StatusColNumber != -1) {
					TestLogger.log(LogStatus.FAIL, "Status Column was not visible on the page. Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
					return "Fail";
				}
			}
			// Get the Column number that contains 'Message ID'	

			for (int i = 0; i <= Colms.size() - 1; i++) {
				if (Colms.get(i).getAttribute("alias").contentEquals("Message ID") && Colms.get(i).isDisplayed()) {
					MIDColNumber = i;
					break;
				}else if (MIDColNumber != -1){
					TestLogger.log(LogStatus.FAIL, "MID Column was not visible on the page. Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
					return "Fail";
				}
			}
			// Get MID Row 
			List<WebElement> Rowscount =OR.FindElements(driver, ORMap.get("MP_ShowBatch_Rows"));
			//List<WebElement> Rowscount = driver.findElements(By.xpath("//table[@id='MainTableBody']/tbody/tr"));
			List<WebElement> GetMIDRow =null;			
			for(int i=1;i<=Rowscount.size();i++){
				GetMIDRow=driver.findElements(By.xpath("//table[@id='MainTableBody']/tbody/tr["+i+"]/td"));
				if(GetMIDRow.get(MIDColNumber).getAttribute("origValue").contentEquals(gppMID.get(0))){
					MIDRow=i;
					GetMIDRow.get(StatusColNumber).click();
					break;
				}
			}

			if(MIDRow==-1){
				driver.switchTo().defaultContent();
				TestLogger.log(LogStatus.FAIL, "Individual Transaction was not found."+	
						". Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
				return "Fail";
			}

			// Refresh			
			int Counter = 0;
			do{
				List<WebElement> Columns =null;	
				Columns=driver.findElements(By.xpath("//table[@id='MainTableBody']/tbody/tr["+MIDRow+"]/td"));
				AcutalStatus=Columns.get(StatusColNumber).getAttribute("origValue");
				if(Columns.get(StatusColNumber).getAttribute("origValue").contentEquals(Status)){
					Columns.get(StatusColNumber).click();
					driver.switchTo().defaultContent();
					TestLogger.log(LogStatus.PASS, "Status of Individual Transaction Validated for MID: "+gppMID.get(0)+" having Status: "+Status);
					return "Pass";
				} else {
					driver.findElement(By.id("b_refresh")).click();
				}
				Counter=Counter+1;
				Thread.sleep(10000);
			} while(Counter<5);	

			if (Counter > 4) {
				driver.switchTo().defaultContent();
				TestLogger.log(LogStatus.FAIL, "Status of Individual Transaction validation Failed. Expected: "+Status+". Actual: "+AcutalStatus+"" +
						". Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
				return "Fail";
			}	

		} catch (Exception e) {
			e.printStackTrace();
			// Logger
			TestLogger.log(LogStatus.FATAL,"Automation Exception while Validating Individual Transaction Status - "+ e.toString() + " Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : SelectFile()
    Description        		  : Select the transaction based on the MID through the DB query
    Developed                 : Subrat 20/01/2018

    Input Parameters          :
    							1. String FileName: name of the input xml
    							2. String E2EKey: E2E has to be retrive from the input xml
     							3. String DBQuery: pass the DB query for the transaction that looking for in the test data sheet
    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test


    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/
	public String SelectFile(String FileName, String E2EKey, String DBQuery) throws IOException {

		try {
			List<String> MIDs=new ArrayList<String>();
			int StatusColNumber = -1;
			int MIDColNumber = -1;
			int MIDRow = -1;
			String AcutalStatus;

			// Getting E2EID from File
			String E2EID = TestConsts.SUPER_XML_FILE_DATAMAP.get(FileName).get(E2EKey);
			//String E2EID = "E2E112132145";

			//	Building the DB Query
			String[] arrDBQ=DBQuery.split("<>");
			String ActualDBQ=arrDBQ[0]+E2EID+arrDBQ[1];

			// Database Query
			String GPPMPconnString = TestConsts.ENV_DBConnString;
			String GPPMPdbUsername = TestConsts.ENV_DBUserName;
			String GPPMPdbPassword = TestConsts.ENV_DBPassword;					
			// Getting MID from DB for MsgID				
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con = DriverManager.getConnection(GPPMPconnString,GPPMPdbUsername,GPPMPdbPassword);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(ActualDBQ);
			while (rs.next()){
				MIDs.add(rs.getString(1));
			}
			if(MIDs.isEmpty()){
				TestLogger.log(LogStatus.FATAL,"Transaction could not be retrived from DB : "+E2EID);
				return "Fatal";
			}
			con.close();



			// Switching frames to reach Search Summary table
			driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("listIframe~~INDIVIDUAL_SEARCH").switchTo().frame("queueFrame");

			// Get the Column number that contains 'Status'

			List<WebElement> Colms =OR.FindElements(driver, ORMap.get("MP_Individual_Search_ForwardProcessingTable_Header"));
			for (int i = 0; i <= Colms.size() - 1; i++) {
				if (Colms.get(i).getAttribute("alias").contentEquals("Status") && Colms.get(i).isDisplayed()) {
					StatusColNumber = i;
					break;
				}else if(StatusColNumber != -1) {
					TestLogger.log(LogStatus.FAIL, "Status Column was not visible on the page. Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
					return "Fail";
				}
			}
			// Get the Column number that contains 'Message ID'	

			for (int i = 0; i <= Colms.size() - 1; i++) {
				if (Colms.get(i).getAttribute("alias").contentEquals("Message ID") && Colms.get(i).isDisplayed()) {
					MIDColNumber = i;
					break;
				}else if (MIDColNumber != -1){
					TestLogger.log(LogStatus.FAIL, "MID Column was not visible on the page. Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
					return "Fail";
				}
			}
			// Get MID Row 			
			List<WebElement> Rowscount =OR.FindElements(driver, ORMap.get("MP_ShowBatch_Rows"));
			//List<WebElement> Rowscount = driver.findElements(By.xpath("//table[@id='MainTableBody']/tbody/tr"));
			List<WebElement> Columns =null;			
			for(int i=1;i<=Rowscount.size();i++){
				Columns=driver.findElements(By.xpath("//table[@id='MainTableBody']/tbody/tr["+i+"]/td"));
				if(Columns.get(MIDColNumber).getAttribute("origValue").contentEquals(MIDs.get(0))){
					MIDRow=i;
					break;
				}
			}
			// Refresh			
			int Counter = 0;
			do{			
				Columns=driver.findElements(By.xpath("//table[@id='MainTableBody']/tbody/tr["+MIDRow+"]/td"));
				AcutalStatus=Columns.get(StatusColNumber).getAttribute("origValue");
				if(Columns.get(StatusColNumber).getAttribute("origValue").contentEquals(MIDs.get(0))){
					Columns.get(StatusColNumber).click();			
					driver.switchTo().defaultContent();
					TestLogger.log(LogStatus.PASS, "Status of Individual Transaction Validated for MID: "+MIDs.get(0));
					return "Pass";
				} else {
					driver.findElement(By.id("b_refresh")).click();
				}
				Counter=Counter+1;
				Thread.sleep(10000);
			} while(Counter<3);	

			if (Counter > 2) {
				driver.switchTo().defaultContent();
				TestLogger.log(LogStatus.FAIL, "Status of Individual Transaction validation Failed. Expected: "+MIDs+". Actual: "+AcutalStatus+"" +
						". Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
				return "Fail";
			}	

		} catch (Exception e) {
			e.printStackTrace();
			// Logger
			TestLogger.log(LogStatus.FATAL,"Automation Exception while Selecting Individual Transaction Status - "+ e.toString() + " Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : ShowSubBatch()
    Description        		  : verify the sub batch has created or not
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          :
    							1. String FileName: name of the input xml
    							2. String BatchGeneratedFlag: expected flag has to be mentioned in the test data sheet
    							3. String E2EKey: E2E has to be retrive from the input xml
     							4. String DBQuery: pass the DB query for the transaction that looking for in the test data sheet
    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test


    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None					
    ############################################################################################*/
	public String ShowSubBatch(String FileName, String E2EKey, String DBQuery,String BatchGeneratedFlag) throws IOException {

		//Driver wait object to wait until 10 seconds for the web element to becomes visible
		WebDriverWait wait=new WebDriverWait(driver,10);

		try {
			//String E2EID = "E2E112132145";
			String E2EID = TestConsts.SUPER_XML_FILE_DATAMAP.get(FileName).get(E2EKey);
			//String E2EID = E2EKey;

			//Building the DB Query
			String[] arrDBQ=DBQuery.split("<>");
			String ActualDBQ=arrDBQ[0]+E2EID+arrDBQ[1];

			// DB Extract				
			List<String> gppMID = DatabaseUtils.DBExtract(ActualDBQ);
			if(gppMID.isEmpty()){
				TestLogger.log(LogStatus.FATAL,"DB Extraction of Transaction Search Failed for: "+E2EID);
				return "Fatal";
			}

			// Switching frames to reach Search Summary table
			driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("listIframe~~INDIVIDUAL_SEARCH").switchTo().frame("queueFrame");

			// Get the Column number that contains 'Message ID'
			int MIDColNumber = -1;
			List<WebElement> Colms =OR.FindElements(driver, ORMap.get("MP_ShowBatch_Colms"));
			//List<WebElement> Colms =driver.findElements(By.xpath("//table[@id='MainTable']/tbody/tr/td"));
			for (int i = 0; i <= Colms.size() - 1; i++) {
				if (Colms.get(i).getAttribute("alias").contentEquals("Message ID") && Colms.get(i).isDisplayed()) {
					MIDColNumber = i;
					break;
				}
			}
			//List<WebElement> RowsCount =OR.FindElements(driver, ORMap.get("MP_ShowBatch_Rows"));
			List<WebElement> RowsCount =driver.findElements(By.xpath("//table[@id='MainTableBody']/tbody/tr"));
			List<WebElement> Columns =null;

			for(int i=1;i<=RowsCount.size();i++){
				Columns=driver.findElements(By.xpath("//table[@id='MainTableBody']/tbody/tr["+i+"]/td"));
				if(Columns.get(MIDColNumber).getAttribute("origValue").contentEquals(gppMID.get(0))){
					Columns.get(MIDColNumber).click();
					break;
				}
			}
			OR.FindElement(driver, ORMap.get("MP_D_ShowSubBatches")).click();
			Thread.sleep(1000);
			if(BatchGeneratedFlag.equalsIgnoreCase("Y")){

				//driver.findElement(By.xpath("//table[@id='toolBarItem']/descendant::td/img[@alt='Show Sub-Batches']")).click();
				driver.switchTo().defaultContent();
				driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("listIframe~~INDIVIDUAL_SEARCH").switchTo().frame("virtualFrame");

				wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.id("qTable"))));

				driver.switchTo().defaultContent();

				TestLogger.log(LogStatus.INFO,"Show Sub batch was performed successfully");
			}
			else if(BatchGeneratedFlag.equalsIgnoreCase("N")){

				Alert alert = driver.switchTo().alert();
				alert.accept();
				driver.switchTo().defaultContent();	

				TestLogger.log(LogStatus.INFO,"Sub Batch has not created...");
				TestLogger.log(LogStatus.INFO,"Sub Batch Status For the MID : "+gppMID.get(0)+" , Screenshot - "+Screenshot.ObjectSnapFullPage(driver));

			}

		} catch (Exception e) {
			e.printStackTrace();
			// Logger
			TestLogger.log(LogStatus.FATAL,"Automation Exception while Validating Individual File Status - "+ e.toString() + " Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : handleAlert()
    Description        		  : accept any windows alert if present
    Developed                 : Subrat 10/01/2018

    Input Parameters          : None

    Returns                   : 1. Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/
	public void handleAlert(){
		try{
			if(iAlertPresent()){
				Alert alert = driver.switchTo().alert();
				alert.accept();
			}
		}catch (NoAlertPresentException e) {

		}
	}
	/*       ###########################################################################################
    Name                      : iAlertPresent()
    Description        		  : accept any windows alert
    Developed                 : Subrat 10/01/2018

    Input Parameters          : None

    Returns                   : 1. Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/
	public boolean iAlertPresent(){
		try{
			driver.switchTo().alert();
			return true;
		}catch(NoAlertPresentException ex){
			return false;
		}
	}
	/*       ###########################################################################################
    Name                      : VerifySubBatchStatus()
    Description        		  : verify the sub batch has created or not
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          :	1.String sbStatus : expected status has to be mentioned in the test data sheet

    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test


    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None					
    ############################################################################################*/
	public String VerifySubBatchStatus(String sbStatus) throws IOException {

		try{
			// Switching frames to reach Search Summary table
			driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("listIframe~~INDIVIDUAL_SEARCH").switchTo().frame("virtualFrame");

			// Get the Column number that contains 'Message ID'
			int SBSColNumber = -1;
			List<WebElement> Colms =OR.FindElements(driver, ORMap.get("MP_ShowBatch_Colms"));
			//List<WebElement> Colms = driver.findElements(By.xpath("//table[@id='MainTable']/tbody/tr/td"));
			for (int i = 0; i <= Colms.size()-1; i++) {
				if (Colms.get(i).getAttribute("alias").contentEquals("Sub-Batch Status") && Colms.get(i).isDisplayed()) {
					SBSColNumber = i;
					break;
				}
			}

			String ActualStatus =driver.findElement(By.xpath("//table[@id='MainTableBody']/tbody/tr/td["+(SBSColNumber+1)+"]")).getAttribute("origValue");
			if(ActualStatus.contentEquals(sbStatus)){
				driver.switchTo().defaultContent();
				TestLogger.log(LogStatus.PASS,"Sub batch Status verified as : "+sbStatus);
			} else{
				driver.switchTo().defaultContent();
				TestLogger.log(LogStatus.FAIL,"Sub batch Status varification Failed. Expected: "+sbStatus+". Actual: "+ActualStatus+". Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
			}

		} catch (Exception e) {
			e.printStackTrace();
			// Logger
			TestLogger.log(LogStatus.FATAL,"Automation Exception while Sub batch Status varification - "+ e.toString() + " Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : ShowSubBatchInvidual()
    Description        		  : Navigate to sub batch individual screen
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          :	None

    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test


    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None					
    ############################################################################################*/
	public String ShowSubBatchInvidual() throws IOException {

		//Driver wait object to wait until 10 seconds for the web element to becomes visible
		WebDriverWait wait=new WebDriverWait(driver,10);
		try{

			driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("listIframe~~INDIVIDUAL_SEARCH").switchTo().frame("virtualFrame");

			wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.id("qTable"))));

			OR.FindElement(driver, ORMap.get("MP_Show_SubBatch_Individuals")).click();
			//driver.findElement(By.xpath("//table[@id='toolBarItem']/descendant::td/img[@alt='Show Sub-Batch Individuals']")).click();

			driver.switchTo().defaultContent();

			driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("listIframe~~INDIVIDUAL_SEARCH").switchTo().frame("queueFrame");

			wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.id("qTable"))));

			driver.switchTo().defaultContent();

			TestLogger.log(LogStatus.INFO,"Show Sub batch Individual was performed successfully");

		} catch (Exception e) {
			e.printStackTrace();
			// Logger
			TestLogger.log(LogStatus.FATAL,"Automation Exception while Show Sub batch Individual - "+ e.toString() + " Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}

}
