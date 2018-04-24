package javaSeedTestScripts.GPPMP;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javaSeed.constants.Const;
import javaSeed.objectRepository.OR;
import javaSeed.utils.Screenshot;
import javaSeedTestScripts.TestConsts;
import javaSeedTestScripts.nonGUIutilities.DatabaseUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class E_TransactionPage {

	private WebDriver driver = Const.driver;
	private HashMap<String, String[]> ORMap = Const.ORMap;
	private ExtentTest TestLogger = Const.etTestCases;

	//------------------------ Imported Variables Collection
	String TestAppURL 		= TestConsts.ENV_GPPMP_URL;
	String strNewdate       = TestConsts.ENV_GPPMP_BussDate;
	String TestAppUser 		= TestConsts.ENV_GPPMP_UserName1;
	String TestAppUserPwd	= TestConsts.ENV_GPPMP_UserName1_Pwd;

	/*       ###########################################################################################
    Name                      : Select_TransactionToolBar()
    Description        		  : Navigate to transaction toolbar
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          :	String ToolBarName:toolbar name has to be passed in the test data sheet

    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test


    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None					
    ############################################################################################*/
	public String Select_TransactionToolBar (String ToolBarName) throws IOException{

		try{

			//Driver wait object to wait until 10 seconds for the web element to becomes visible
			//WebDriverWait wait=new WebDriverWait(driver,10);
			driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("messageIframe");

			//wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//table[@id='toolBarItem']"))));

			// Switching frames to reach Search Summary table
			driver.findElement(By.xpath("//table[@id='toolBarItem']/descendant::td[contains(.,'"+ToolBarName+"')]")).click();
			driver.switchTo().defaultContent();

			TestLogger.log(LogStatus.INFO,ToolBarName+" tool bar was selected");
			Thread.sleep(2000);

		} catch(Exception e){
			e.printStackTrace();
			driver.switchTo().defaultContent();
			// Logger 
			TestLogger.log(LogStatus.FATAL,"Automation Exception while selecting "+ToolBarName+" tool bar"+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : ValidateLinksStatus()
    Description        		  : validate different link status
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          :	1. String FileName: name of the input xml
    							2. String MsgStatuses: expected status has to be mentioned in the test data sheet
    							3. String E2EKey: E2E has to be retrive from the input xml
     							4. String DBQuery: pass the DB query for the transaction that looking for in the test data sheet
     							5. String StrMsgSubTypes:expected sub message status has to be mentioned in the test data sheet

    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test


    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None					
    ############################################################################################*/
	public String ValidateLinksStatus(String FileName, String E2EKey, String DBQuery, String MsgStatuses, String StrMsgSubTypes) throws IOException{

		//String E2EID = "E2E112103258";
		String E2EID = TestConsts.SUPER_XML_FILE_DATAMAP.get(FileName).get(E2EKey);
		//String E2EID = E2EKey;


		String parentWindowHandler = driver.getWindowHandle();
		try{
			String arrMsgStatuses[]=null;
			String arrStrMsgSubTypes[]=null;
			boolean MultiChecks;

			if(MsgStatuses.contains(",") && StrMsgSubTypes.contains(",")){
				arrMsgStatuses=MsgStatuses.split(",");
				arrStrMsgSubTypes=StrMsgSubTypes.split(",");
				MultiChecks=true;
			}else{
				MultiChecks=false;
			}

			//Building the DB Query
			String[] arrDBQ=DBQuery.split("<>");
			String ActualDBQ=arrDBQ[0]+E2EID+arrDBQ[1];

			// DB Extract				
			List<String> gppMID = DatabaseUtils.DBExtract(ActualDBQ);
			if(gppMID.isEmpty()){
				TestLogger.log(LogStatus.FATAL,"DB Extraction of Transaction Search Failed for: "+E2EID);
				return "Fatal";
			}

			String subWindowHandler = null;

			Set<String> handles = driver.getWindowHandles();
			Iterator<String> iterator = handles.iterator();
			while (iterator.hasNext()) {
				subWindowHandler = iterator.next();
			}
			driver.switchTo().window(subWindowHandler);
			//List<WebElement> RowsCount = driver.findElements(By.xpath("//div[@id='grdTable']/table/tbody/tr"));
			List<WebElement> RowsCount =OR.FindElements(driver, ORMap.get("MP_Txn_Rows"));


			if(MultiChecks){
				for(int j=2;j<=RowsCount.size();j++){
					String RowText = driver.findElement(By.xpath("//div[@id='grdTable']/table/tbody/tr["+j+"]")).getText();
					if(RowText.contains(gppMID.get(j-2).toUpperCase()) && RowText.contains(arrMsgStatuses[j-2].toUpperCase()) && RowText.contains(arrStrMsgSubTypes[j-2].toUpperCase())){
						TestLogger.log(LogStatus.PASS,"Links of different Messages was verified. MID: "+gppMID.get(j-2)+". Status: "+arrMsgStatuses[j-2]+". Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
					} else{
						TestLogger.log(LogStatus.FAIL,"Validation Failed for Links of different Messages. Expected MID: "+gppMID.get(j-2)+". Status: "+arrMsgStatuses[j-2]+". Message Subtype: "+arrStrMsgSubTypes[j-2]+"" +
								". Actual: "+RowText+ ". Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
					}
				}
			} else {
				String RowText = driver.findElement(By.xpath("//div[@id='grdTable']/table/tbody/tr[2]")).getText();
				//String RowText = OR.FindElements(driver, ORMap.get("MP_Txn_RowText")).getText();

				if(RowText.contains(gppMID.get(0).toUpperCase()) && RowText.contains(MsgStatuses.toUpperCase()) && RowText.contains(StrMsgSubTypes.toUpperCase())){
					TestLogger.log(LogStatus.PASS,"Links of different Messages was verified. MID: "+gppMID.get(0)+". Status: "+MsgStatuses+". Message Subtype: "+StrMsgSubTypes);
				} else{
					TestLogger.log(LogStatus.FAIL,"Validation Failed for Links of different Messages. Expected MID: "+gppMID.get(0)+". Status: "+MsgStatuses+". Message Subtype: "+StrMsgSubTypes+"" +
							". Actual: Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
				}
			}
			/*				driver.switchTo().window(parentWindowHandler);
				Thread.sleep(1000);*/
			driver.switchTo().window(subWindowHandler).close();
			//Thread.sleep(1000);
			driver.switchTo().window(parentWindowHandler);
		}
		catch(Exception e){
			e.printStackTrace();
			// Logger 
			TestLogger.log(LogStatus.FAIL,"Automation Exception on validating Links of different Messages"+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : NavigateAndValidatePostings()
    Description        		  : validate posting details
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          :	1. String FileName: name of the input xml
    							2. String PostingText: expected posting has to be mentioned in the test data sheet
    							3. String E2EKey: E2E has to be retrive from the input xml
     							4. String DBQuery: pass the DB query for the transaction that looking for in the test data sheet

    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test


    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    Subrat							15/02/2018				added one 3 more parameter to get the MID of the posting				
    ############################################################################################*/
	public String NavigateAndValidatePostings(String PostingText,String FileName,String E2E,String DBQuery) throws IOException{

		try{
			String E2EID = TestConsts.SUPER_XML_FILE_DATAMAP.get(FileName).get(E2E);
			//String E2EID = "E2E112103258";

			//		Building the DB Query
			String[] arrDBQ=DBQuery.split("<>");
			String ActualDBQ=arrDBQ[0]+E2EID+arrDBQ[1];


			// DB Extract				
			List<String> gppMID = DatabaseUtils.DBExtract(ActualDBQ);
			if(gppMID.isEmpty()){
				TestLogger.log(LogStatus.FATAL,"DB Extraction of Transaction Search Failed for: "+ActualDBQ);
				return "Fatal";
			}

			String[] arrPostingsValues = null;
			boolean Blank = false;
			boolean valStatus = false;

			if(PostingText.contentEquals("No Postings")){
				PostingText="";
				Blank=true;
			} else if(PostingText.contains(",")){
				arrPostingsValues=PostingText.split(",");
				Blank=false;
			}

			// Switching frames to reach Search Summary table
			driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("messageIframe");

			//driver.findElement(By.id("btn_18")).click();
			OR.FindElement(driver, ORMap.get("MP_Click_Posting_btn")).click();


			//.xpath("//div[@id='tabsContainer']/table/tbody/tr/td[@id='btn_18']")).click();
			String textInsideInputBox = driver.findElement(By.id("grdMemo")).getText();

			if(Blank){
				if (textInsideInputBox.contentEquals(PostingText)) {
					driver.switchTo().defaultContent();
					TestLogger.log(LogStatus.PASS,"No Postings are Expected and varified successfully"+" MID :"+gppMID.get(0));
					return "Pass";
				} else{
					driver.switchTo().defaultContent();
					TestLogger.log(LogStatus.FAIL,"No Postings are Expected and varification Failed");
					return "Fail";
				}
			}else{
				for(String PstValues:arrPostingsValues){
					if (textInsideInputBox.contains(PstValues)) {
						valStatus=true;
					} else{
						driver.switchTo().defaultContent();
						TestLogger.log(LogStatus.FAIL,"Validation Failed for Expected Postings: "+PostingText+". Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
						return "Fail";
					}
				}
			}

			if(valStatus){
				driver.switchTo().defaultContent();
				TestLogger.log(LogStatus.PASS,"Expected Postings verfied as: "+PostingText+" MID : "+gppMID.get(0));
				return "Pass";
			}

		} catch(Exception e){
			driver.switchTo().defaultContent();
			e.printStackTrace();
			// Logger 
			TestLogger.log(LogStatus.FAIL,"Automation Exception: Expected Postings validation - "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : NavigateToTransactionTabs()
    Description        		  : Navigate to transaction tabs
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          :	1.String TabName:Tab name has to be passed in the test data sheet

    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test


    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None					
    ############################################################################################*/

	public String NavigateToTransactionTabs(String TabName) throws IOException{

		try{			
			// Switching frames to reach Search Summary table
			driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("messageIframe");

			driver.findElement(By.xpath("//table[@id='ButtonPanel']/descendant::td[contains(.,'"+TabName+"')]")).click();

			driver.switchTo().defaultContent();

		} catch(Exception e){
			driver.switchTo().defaultContent();
			e.printStackTrace();
			// Logger 
			TestLogger.log(LogStatus.FAIL,"Automation Exception: on NavigateToTransactionTabs Method - "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : GetOutInternalFileID()
    Description        		  : Get the out internal file id from the MP info
    Developed                 : Subrat 01/02/2018

    Input Parameters          :	1.String FileName:input xml file name in the test data sheet
    							2.String key_OutInternalId:put the internal id to a MAP

    Returns                   : 1. String Flag: out internal file id to a MAP & Make sure if the method was executed successfully and otherwise return Fatal to terminate the test


    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None					
    ############################################################################################*/
	public String GetOutInternalFileID(String FileName,String key_OutInternalId) throws IOException{

		try{			
			// Switching frames to reach Search Summary table
			driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("messageIframe");
			String OutID = OR.FindElement(driver, ORMap.get("MP_GetOutInternalFileID")).getAttribute("value");
			//String OutID = driver.findElement(By.id("VIRTUAL.OUT_INTERNAL_FILEID")).getAttribute("value");
			TestConsts.SUPER_XML_FILE_DATAMAP.get(FileName).put(key_OutInternalId, OutID);

			driver.switchTo().defaultContent();

		} catch(Exception e){
			driver.switchTo().defaultContent();
			e.printStackTrace();
			// Logger 
			TestLogger.log(LogStatus.FAIL,"Automation Exception: on GetOutInternalFileID Method - "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : GetInFileReference()
    Description        		  : Get the in file ref. from the MP info
    Developed                 : Subrat 01/02/2018

    Input Parameters          :	1.String FileName:input xml file name in the test data sheet
    							2.String InFileReference:put the infile reference to a MAP

    Returns                   : 1. String Flag: Infile reference to a MAP & Make sure if the method was executed successfully and otherwise return Fatal to terminate the test


    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None					
    ############################################################################################*/
	public String GetInFileReference(String FileName,String InFileReference) throws IOException{

		try{			
			// Switching frames to reach Search Summary table
			driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("messageIframe");
			String InID = OR.FindElement(driver, ORMap.get("MP_InFileReference")).getAttribute("value");
			//String InID = driver.findElement(By.id("MIF.IN_FILE_REFERENCE")).getAttribute("value");
			TestConsts.SUPER_XML_FILE_DATAMAP.get(FileName).put(InFileReference, InID);

			driver.switchTo().defaultContent();

		} catch(Exception e){
			driver.switchTo().defaultContent();
			e.printStackTrace();
			// Logger 
			TestLogger.log(LogStatus.FAIL,"Automation Exception: on GetOutInternalFileID Method - "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : selectLinks()
    Description        		  : select the link on the link screen
    Developed                 : Subrat 1/02/2018

    Input Parameters          :	1.String FileName:input xml file name in the test data sheet
    							2.String DBQuery:pass the DB query to get the MID
    							3.String E2EKey: get the E2E key from the xml and pass it to the test data sheet

    Returns                   : 1. String Flag: Infile reference to a MAP & Make sure if the method was executed successfully and otherwise return Fatal to terminate the test


    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None					
    ############################################################################################*/
	public String selectLinks(String FileName, String E2EKey, String DBQuery)  throws Exception
	{
		Actions action = new Actions (driver);
		String E2EID = TestConsts.SUPER_XML_FILE_DATAMAP.get(FileName).get(E2EKey);
		String[] arrDBQ=DBQuery.split("<>");
		String ActualDBQ=arrDBQ[0]+E2EID+arrDBQ[1];

		// DB Extract				
		List<String> gppMID = DatabaseUtils.DBExtract(ActualDBQ);
		if(gppMID.isEmpty()){
			TestLogger.log(LogStatus.FATAL,"DB Extraction of Transaction Search Failed for: "+E2EID);
			return "Fatal";
		}
		List<WebElement> RowsCount =OR.FindElements(driver, ORMap.get("MP_Txn_Rows"));
		List<WebElement> allRows =OR.FindElements(driver, ORMap.get("MP_AuditTrial_Rows"));
		//List<WebElement> RowsCount = driver.findElements(By.xpath("//div[@id='grdTable']/table/tbody/tr"));
		//List<WebElement> allRows = driver.findElements(By.xpath("//div[@id='grdTable']/table/tbody"));
		try {
			for (WebElement row : allRows) {
				for (int i =1; i <=RowsCount.size(); i++) {
					String webvalue = RowsCount.get(i).getText();
					if(webvalue.equals(gppMID.get(0))){
						WebElement desCol = row.findElement(By.xpath("tr["+ i +"]"));
						action.moveToElement(desCol).build().perform();
						action.doubleClick(desCol);
						break;
					}
				}
			}
			TestLogger.log(LogStatus.INFO,"Successfully Navigate to the Links through the MID : "+gppMID.get(0));
			//driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("messageIframe");
		}catch(Exception e){
			return "FATAL";
		}
		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : Auditrail()
    Description        		  : go to the audit trail screen and validate the status
    Developed                 : Subrat 01/01/2018

    Input Parameters          :	1. String FileName: name of the input xml
    							2. String des1: expected data has to be mentioned in the test data sheet
    							3. String E2EKey: E2E has to be retrive from the input xml
     							4. String DBQuery: pass the DB query for the transaction that looking for in the test data sheet
     							5. String des2: expected data has to be mentioned in the test data sheet

    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test


    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    Subrat							15/02/2018				added one 3 more parameter to get the MID of the posting				
    ############################################################################################*/	
	public String Auditrail(String des1,String des2,String FileName,String E2E,String DBQuery) throws IOException{
		try{
			String E2EID = TestConsts.SUPER_XML_FILE_DATAMAP.get(FileName).get(E2E);
			//String E2EID = "E2E112103258";

			//		Building the DB Query
			String[] arrDBQ=DBQuery.split("<>");
			String ActualDBQ=arrDBQ[0]+E2EID+arrDBQ[1];


			// DB Extract				
			List<String> gppMID = DatabaseUtils.DBExtract(ActualDBQ);
			if(gppMID.isEmpty()){
				TestLogger.log(LogStatus.FATAL,"DB Extraction of Transaction Search Failed for: "+ActualDBQ);
				return "Fatal";
			}

			driver.switchTo().frame("main");
			Thread.sleep(500);
			driver.switchTo().frame("fraQList");
			Thread.sleep(500);
			driver.switchTo().frame("messageIframe");
			Thread.sleep(500);
			//driver.findElement(By.xpath("//td[@id='button_53']")).click();
			OR.FindElement(driver, ORMap.get("MP_Click_Audittrial")).click();

			Thread.sleep(4000);
			System.out.println();

			String parentWindowHandler = driver.getWindowHandle();
			String subWindowHandler = null;

			Set<String> handles = driver.getWindowHandles();
			Iterator<String> iterator = handles.iterator();
			while (iterator.hasNext()) {
				subWindowHandler = iterator.next();
			}
			driver.switchTo().window(subWindowHandler);
			TestLogger.log(LogStatus.INFO,"Audit Trail Validation Screen - "+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			System.out.println(driver.getCurrentUrl());

			List<WebElement> RowsCount =OR.FindElements(driver, ORMap.get("MP_Txn_Rows"));
			//List<WebElement> RowsCount = driver.findElements(By.xpath("//div[@id='grdTable']/table/tbody/tr"));
			//List<WebElement> allRows = driver.findElements(By.xpath("//div[@id='grdTable']/table/tbody"));
			List<WebElement> allRows =OR.FindElements(driver, ORMap.get("MP_AuditTrial_Rows"));

			int count = RowsCount.size();
			System.out.println(count);
			try {
				for (WebElement row : allRows) {
					for (int i =2; i <=count; i++) {
						WebElement desCol = row.findElement(By.xpath("tr["+ i +"]"));
						String description = desCol.getText();
						System.out.println(description);
						if ((description.contains(des1) && description.contains(des2))) {
							System.out.println("pass");
							TestLogger.log(LogStatus.PASS,"Varify Audit Trail status with the Status :"+des2);
							break;
						} else {
							System.out.println("Fail");
							TestLogger.log(LogStatus.FAIL,"Exception occured while varifying Audit Trail status Expected Status : "+des2+" Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
						}

					}

				}
			} catch (Exception e) {

			}    
			Thread.sleep(3000);
			TestLogger.log(LogStatus.PASS, "Task: Audit Trail executed successfully with Status : "+des1+" To "+des2+" MID : "+gppMID.get(0));

			driver.switchTo().window(subWindowHandler).close();
			driver.switchTo().window(parentWindowHandler);

		}catch(Exception e){System.out.println(e.getMessage());
		TestLogger.log(LogStatus.FATAL,"Automation Error while performing Audit Trail: Error Description - "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
		return "FATAL";
		}
		return "Pass";

	}

}
