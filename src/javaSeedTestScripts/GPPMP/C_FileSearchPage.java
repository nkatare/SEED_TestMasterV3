package javaSeedTestScripts.GPPMP;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javaSeed.constants.Const;
import javaSeed.objectRepository.OR;
import javaSeed.utils.Screenshot;
import javaSeedTestScripts.TestConsts;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class C_FileSearchPage {

	private WebDriver driver = Const.driver;
	private HashMap<String, String[]> ORMap = Const.ORMap;
	private ExtentTest TestLogger = Const.etTestCases;

	//------------------------ Imported Variables Collection
	String TestAppURL 		= TestConsts.ENV_GPPMP_URL;
	String strNewdate       = TestConsts.ENV_GPPMP_BussDate;
	String TestAppUser 		= TestConsts.ENV_GPPMP_UserName1;
	String TestAppUserPwd	= TestConsts.ENV_GPPMP_UserName1_Pwd;

	/*       ###########################################################################################
    Name                      : VerifyFileStatus()
    Description        		  : verify the status of a individual transaction
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          : 1. String ExpectedStatus: expected status has to be placed in the test data sheet
    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/

	public String VerifyFileStatus(String ExpectedStatus)throws IOException {

		try {

			String StatusValidationFlag = null; 
			String arString[] = null;
			Boolean MultiValidation =null;

			// Switching frames to reach Search Summary table
			driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("listIframe~~FILE_SEARCH").switchTo().frame("queueFrame");

			// Find the Status Column
			int StatusColNumber = -1;
			List<WebElement> Colms = OR.FindElements(driver, ORMap.get("MP_FileSearch_Scheduled_Covers_Headertable"));
			for (int i = 0; i <= Colms.size() - 1; i++) {
				if (Colms.get(i).getAttribute("alias").contentEquals("Status") && Colms.get(i).isDisplayed()) {
					StatusColNumber = i;
					break;
				}
			}

			List<WebElement> Rowscount =OR.FindElements(driver, ORMap.get("MP_File_Table_Rows"));
			if(Rowscount.size()>1){
				arString=ExpectedStatus.split(",");
				MultiValidation=true;
			}
			else{ MultiValidation=false;
			}

			// Loop to Refresh if Status not match			
			int Counter = 0;
			do{
				if(!MultiValidation) {
					if (StatusColNumber >= 0) {
						if(!OR.FindElements(driver, ORMap.get("MP_FileSearch_Scheduled_Covers_datatable"))
								.get(StatusColNumber).getAttribute("origValue").contentEquals(ExpectedStatus)){
							driver.findElement(By.id("tbimgrefresh")).click();
						}else {
							break;
						}
					} else{
						driver.switchTo().defaultContent();
						TestLogger.log(LogStatus.FAIL, "Status Column was not visible on the page. Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
						break;
					}
				}
				else{
					for (int j=0; j<=Rowscount.size()-1;j++){
						if (StatusColNumber >= 0) {
							if(!OR.FindElements(driver, ORMap.get("MP_FileSearch_Scheduled_Covers_datatable"))
									.get(StatusColNumber).getAttribute("origValue").contentEquals(arString[j])){
								driver.findElement(By.id("tbimgrefresh")).click();
							}else{
								Counter=5;
								break;
							}
						} else{
							driver.switchTo().defaultContent();
							TestLogger.log(LogStatus.FAIL, "Status Column was not visible on the page. Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
							break;
						}
					}
				}
				Counter=Counter+1;
				Thread.sleep(15000);
			} while(Counter<5);


			// Validation Flag			
			if(MultiValidation){
				for (int j=0; j<=Rowscount.size()-1;j++){
					if (StatusColNumber >= 0) {
						if(OR.FindElements(driver, ORMap.get("MP_FileSearch_Scheduled_Covers_datatable"))
								.get(StatusColNumber).getAttribute("origValue").contentEquals(arString[j])){
							StatusValidationFlag = "Pass";
						}else{

							StatusValidationFlag = "Fail";
							break;
						}
					} else{
						driver.switchTo().defaultContent();
						TestLogger.log(LogStatus.FAIL, "Status Column was not visible on the page. Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
						break;
					}
				}
			} else{
				if (StatusColNumber >= 0) {
					if(OR.FindElements(driver, ORMap.get("MP_FileSearch_Scheduled_Covers_datatable"))
							.get(StatusColNumber).getAttribute("origValue").contentEquals(ExpectedStatus)){
						StatusValidationFlag = "Pass";
					}else{
						StatusValidationFlag = "Fail";
					}
				} else{
					driver.switchTo().defaultContent();
					TestLogger.log(LogStatus.FAIL, "Status Column was not visible on the page. Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
				}
			}

			// Validation		
			if(StatusValidationFlag==null){
				driver.switchTo().defaultContent();
				return "Fail";
			}else if(StatusValidationFlag.contentEquals("Pass")){
				driver.switchTo().defaultContent();
				TestLogger.log(LogStatus.PASS, "File status verified as: "+ ExpectedStatus);
				return "Pass";
			}else{
				driver.switchTo().defaultContent();
				TestLogger.log(LogStatus.FAIL, "File status verification failed. Expected Status "+ExpectedStatus+" did not matched with "+
						"Actual Status. Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
				return "Fail";
			}

		} catch (Exception e) {
			e.printStackTrace();
			// Logger
			driver.switchTo().defaultContent();
			TestLogger.log(LogStatus.FATAL,"File status verification Automation Failure "+ e.toString() + " Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
	}
	/*       ###########################################################################################
    Name                      : VerifyFileStatusForIDFs()
    Description        		  : verify the status for file IDFs 
    Developed                 : Subrat 10/01/2018

    Input Parameters          :
    							1. String File:File name of the input xml
    							2. String ExpectedStatus: expected status has to be mentioned in the test data sheet
    							3. String Direction: expected direction of the file status has to be mentioned in the test data sheet
    							4. String Key:internal file id has be to retrive from MP info and puted in the MAP
    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/
	public String VerifyFileStatusForIDFs(String File,String ExpectedStatus,String Direction,String Key)throws IOException {

		try {


			String status[] = null;
			String direction[] = null;


			// Switching frames to reach Search Summary table
			driver.switchTo().frame("main").switchTo().frame("fraQList").switchTo().frame("listIframe~~FILE_SEARCH").switchTo().frame("queueFrame");

			// Find the Status Column
			int StatusColNumber = -1;
			List<WebElement> Colms = OR.FindElements(driver, ORMap.get("MP_FileSearch_Scheduled_Covers_Headertable"));
			for (int i = 0; i <= Colms.size() - 1; i++) {
				if (Colms.get(i).getAttribute("alias").contentEquals("Status") && Colms.get(i).isDisplayed()) {
					StatusColNumber = i;
					break;
				}
			}

			status=ExpectedStatus.split(",");
			direction=Direction.split(",");
			String InternalFileID= TestConsts.SUPER_XML_FILE_DATAMAP.get(File).get(Key);
			//List<WebElement> RowsCount = driver.findElements(By.xpath("//table[@id='MainTableBody']/descendant::tr"));
			List<WebElement> RowsCount = OR.FindElements(driver, ORMap.get("MP_File_Table_Rows"));
			//List<WebElement> allRows = driver.findElements(By.xpath("//table[@id='MainTableBody']/tbody"));
			List<WebElement> allRows = OR.FindElements(driver, ORMap.get("MP_Table_rows"));

			int count = RowsCount.size();
			//System.out.println(count);
			try {
				//for (WebElement row : allRows) {
					for (int i =1; i <=count; i++) {
						WebElement desCol = allRows.get(i).findElement(By.xpath("tr["+ i +"]"));
						String description = desCol.getText();
						//System.out.println(description);
						if ((description.contains(InternalFileID) && description.contains(status[i]) && description.contains(direction[i]))) {
							//System.out.println("******************Two IDFs Status Found**************");
							TestLogger.log(LogStatus.PASS,"File status for internal File ID : "+InternalFileID+" is varified with Status : "+ExpectedStatus);
							break;
						} else {
							//System.out.println("Fail");
							TestLogger.log(LogStatus.FAIL,"Status Validation failed for Internal File ID: "+InternalFileID+ " with the expected status : "+ExpectedStatus+" Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));

						}

					}
				
			} catch (Exception e) {

			}   


		} catch (Exception e) {
			e.printStackTrace();
			// Logger
			driver.switchTo().defaultContent();
			TestLogger.log(LogStatus.FATAL,"File status verification Automation Failure "+ e.toString() + " Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : GoToFileIndividual()
    Description        		  : Navigate to file individual screen
    Developed                 : Nikhil Katare 01/12/2018

    Input Parameters          : None
    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/
	public String GoToFileIndividual() throws IOException{

		try{
			
			//Driver wait object to wait until 10 seconds for the web element to becomes visible
			WebDriverWait wait=new WebDriverWait(driver,10);

			// Switching frames to reach Search Summary table
			driver.switchTo().frame("main");
			driver.switchTo().frame("fraQList");
			driver.switchTo().frame("listIframe~~FILE_SEARCH");
			driver.switchTo().frame("queueFrame");
			//Click on show file individuals
			Thread.sleep(3000);
			OR.FindElement(driver, ORMap.get("MP_click_show_File_Individual")).click();
			driver.switchTo().defaultContent();

			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("main"));
			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("fraQList"));
			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("listIframe~~INDIVIDUAL_SEARCH"));
			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("queueFrame"));

			if(driver.findElements(By.id("qTable")).size()>0){
				driver.switchTo().defaultContent();
				TestLogger.log(LogStatus.INFO,"Navigated to Individual Files page");
				Thread.sleep(500);
			}else{
				driver.switchTo().defaultContent();
				TestLogger.log(LogStatus.FATAL,"Navigation Failed to Individual Files page"+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			}
		} 
		catch(Exception e){
			e.printStackTrace();
			driver.switchTo().defaultContent();
			TestLogger.log(LogStatus.FATAL,"Automation Failure while Navigation to Individual Files page - "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}


}
