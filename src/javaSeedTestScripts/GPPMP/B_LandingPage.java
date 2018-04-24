package javaSeedTestScripts.GPPMP;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javaSeed.constants.Const;
import javaSeed.objectRepository.OR;
import javaSeed.utils.Screenshot;
import javaSeedTestScripts.GenericMethods;
import javaSeedTestScripts.TestConsts;
import javaSeedTestScripts.nonGUIutilities.DatabaseUtils;
import javaSeedTestScripts.nonGUIutilities.GetXMLNode;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class B_LandingPage {


	private WebDriver driver = Const.driver;
	private HashMap<String, String[]> ORMap = Const.ORMap;
	private ExtentTest TestLogger = Const.etTestCases;
	static String active_time = getTimeStamp();
	static String past_time = getPasTime();

	//------------------------ Imported Variables Collection
	String TestAppURL 		= TestConsts.ENV_GPPMP_URL;
	String strNewdate       = TestConsts.ENV_GPPMP_BussDate;
	String TestAppUser 		= TestConsts.ENV_GPPMP_UserName1;
	String TestAppUserPwd	= TestConsts.ENV_GPPMP_UserName1_Pwd;



	/*       ###########################################################################################
    Name                      : File_Search_LocalRef_Using_FileRef()
    Description        		  : Displays the search results for the file ref as the input
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          : 1. String FileName: name of the file which is going to process
    						  :	2. String FileRefTag: it will take the fileref from the processed file
    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    Subrat                          15/02/2018                 Changed the name of the method from FileSearch to File_Search_LocalRef_Using_FileRef
    ############################################################################################*/

	public String File_Search_LocalRef_Using_FileRef(String FileName, String FileRefTag) throws IOException {
		try{
			//Driver wait object to wait until 10 seconds for the web element to becomes visible
			WebDriverWait wait=new WebDriverWait(driver,10);

			wait.until(ExpectedConditions.visibilityOf(OR.FindElement(driver, ORMap.get("MP_Click_Messages"))));

			OR.FindElement(driver, ORMap.get("MP_Click_Messages")).click();
			OR.FindElement(driver, ORMap.get("MP_Click_Messages_Search")).click();
			Thread.sleep(1000);
			//select the mass payments radio button		
			String MainWindowHandle = driver.getWindowHandle();
			// Switch to new window opened
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle);
					break;
				}
			}			   

			// == Get File Reference from File ==
			String FilePath = TestConsts.ENV_FWPATH+FileName;

			Map<String, String> FileRef = GetXMLNode.GetXMLNodeValue(FileRefTag, FilePath);

			String strfilerefernce = null;
			if(FileRef.get("strFlag").contentEquals("Fatal")){
				TestLogger.log(LogStatus.FATAL,"File Reference cannot be picked up from the Input File: "+FilePath);
				return "Fatal";
			}else if(FileRef.get("strFlag").contentEquals("Pass")){
				strfilerefernce=FileRef.get(FileRefTag);
			}
			FileRef.clear();
			// ======

			// Perform the actions on new window
			OR.FindElement(driver, ORMap.get("MP_Select_MassPayments_Radiobtn")).click();
			driver.findElement(By.id("File")).click();
			if(!driver.findElement(By.id("MIF.MID")).getAttribute("value").contentEquals("")){
				driver.findElement(By.id("MIF.MID")).clear();					   
			} else if(!OR.FindElement(driver, ORMap.get("MP_Enter_Local_Refernce")).getAttribute("value").contentEquals("")){
				OR.FindElement(driver, ORMap.get("MP_Enter_Local_Refernce")).clear();
			}		   


			//Enter the file reference 
			OR.FindElement(driver, ORMap.get("MP_Enter_Local_Refernce")).sendKeys(strfilerefernce);
			Thread.sleep(1000);
			OR.FindElement(driver, ORMap.get("MP_Enter_blank_From_date")).clear();
			OR.FindElement(driver, ORMap.get("MP_Enter_blank_to_date")).clear();
			Thread.sleep(1000);
			Select Office = new Select (driver.findElement(By.id("MIF.OFFICE")));
			Office.deselectAll();
			Office.selectByValue("IE1");				

			//Select the IE1 from office
			OR.FindElement(driver, ORMap.get("MP_Click_Search_Messages_btn")).click();


			driver.switchTo().window(MainWindowHandle);// Again
			Thread.sleep(1500);

			if(isAlertPresent()){
				System.out.println();
				for (String winHandle : driver.getWindowHandles()) {
					if (!winHandle.equals(MainWindowHandle)) {
						driver.switchTo().window(winHandle);
						Thread.sleep(500);
						break;
					}
				}
				OR.FindElement(driver, ORMap.get("MP_SearchDialog_CloseButton")).click();
				driver.switchTo().window(MainWindowHandle);
				TestLogger.log(LogStatus.FATAL,"No File present with searched File reference: "+strfilerefernce+"Screenshot - "
						+Screenshot.ObjectSnapFullPage(driver));
				Thread.sleep(1000);
				driver.switchTo().window(MainWindowHandle);
				LogoutTestApp();
				return "Fatal";
			} else{
				driver.switchTo().window(MainWindowHandle);
				TestLogger.log(LogStatus.INFO,"File was located searched with file reference: "+strfilerefernce);
			}

		}
		catch(Exception e){
			e.printStackTrace();				
			TestLogger.log(LogStatus.FATAL,"Failed to Search file based on refernce details "+e.toString()+"Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}

		return "Pass";
	}


	/*       ###########################################################################################
    Name                      : File_Search_LocalRef_Using_InterId()
    Description        		  : Displays the search results for the internal id as the input
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          : 1. String FileName: name of the file which is going to process
    						  :	2. String key: it will take the internal id from the MP Info and stored in MAP
    Returns                   : 1. String Flag: Return the search results for the internal id & Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    Subrat                          15/02/2018                 Changed the name of the method from FileSearchForIDFs to File_Search_LocalRef_Using_InterId
    ############################################################################################*/
	public String File_Search_LocalRef_Using_InterId(String FileName,String key) throws IOException {
		try{
			//Driver wait object to wait until 10 seconds for the web element to becomes visible
			WebDriverWait wait=new WebDriverWait(driver,10);

			wait.until(ExpectedConditions.visibilityOf(OR.FindElement(driver, ORMap.get("MP_Click_Messages"))));

			OR.FindElement(driver, ORMap.get("MP_Click_Messages")).click();
			OR.FindElement(driver, ORMap.get("MP_Click_Messages_Search")).click();
			Thread.sleep(1000);
			//select the mass payments radio button		
			String MainWindowHandle = driver.getWindowHandle();
			// Switch to new window opened
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle);
					break;
				}
			}			   

			// == Get File Reference from File ==
			String FileID= TestConsts.SUPER_XML_FILE_DATAMAP.get(FileName).get(key);
			// ======

			// Perform the actions on new window
			OR.FindElement(driver, ORMap.get("MP_Select_MassPayments_Radiobtn")).click();
			driver.findElement(By.id("File")).click();
			OR.FindElement(driver, ORMap.get("MP_Select_File")).click();

			if(!OR.FindElement(driver, ORMap.get("MP_Select_MID")).getAttribute("value").contentEquals("")){
				OR.FindElement(driver, ORMap.get("MP_Select_MID")).clear();
			}
			//			if(!driver.findElement(By.id("MIF.MID")).getAttribute("value").contentEquals("")){
			//				driver.findElement(By.id("MIF.MID")).clear();					   
			//			} else if(!OR.FindElement(driver, ORMap.get("MP_Enter_Local_Refernce")).getAttribute("value").contentEquals("")){
			//				OR.FindElement(driver, ORMap.get("MP_Enter_Local_Refernce")).clear();
			//			}		   


			//Enter the file reference 
			OR.FindElement(driver, ORMap.get("MP_Enter_Local_Refernce")).sendKeys(FileID);
			Thread.sleep(1000);
			OR.FindElement(driver, ORMap.get("MP_Enter_blank_From_date")).clear();
			OR.FindElement(driver, ORMap.get("MP_Enter_blank_to_date")).clear();
			Thread.sleep(1000);

			Select Office = new Select (driver.findElement(By.id("MIF.OFFICE")));
			//Select Office = new Select (driver, ORMap.get("MP_MIF_OFFICE")));
			Office.deselectAll();
			Office.selectByValue("IE1");				

			//Select the IE1 from office
			OR.FindElement(driver, ORMap.get("MP_Click_Search_Messages_btn")).click();


			driver.switchTo().window(MainWindowHandle);// Again
			Thread.sleep(1500);

			if(isAlertPresent()){
				System.out.println();
				for (String winHandle : driver.getWindowHandles()) {
					if (!winHandle.equals(MainWindowHandle)) {
						driver.switchTo().window(winHandle);
						Thread.sleep(500);
						break;
					}
				}
				OR.FindElement(driver, ORMap.get("MP_SearchDialog_CloseButton")).click();
				driver.switchTo().window(MainWindowHandle);
				TestLogger.log(LogStatus.FATAL,"No File present with searched File reference: "+FileID+"Screenshot - "
						+Screenshot.ObjectSnapFullPage(driver));
				Thread.sleep(1000);
				driver.switchTo().window(MainWindowHandle);
				LogoutTestApp();
				return "Fatal";
			} else{
				driver.switchTo().window(MainWindowHandle);
				TestLogger.log(LogStatus.INFO,"File was located searched with file reference: "+FileID);
			}

		}
		catch(Exception e){
			e.printStackTrace();				
			TestLogger.log(LogStatus.FATAL,"Failed to Search file based on refernce details "+e.toString()+"Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}

		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : isAlertPresent()
    Description        		  : accept any windows alert
    Developed                 : Subrat 10/01/2018

    Input Parameters          : 1. 
    						  :	2. 
    Returns                   : 1. Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/
	protected boolean isAlertPresent() throws InterruptedException {
		try {
			driver.switchTo().alert().accept();
			Thread.sleep(1000);
			return true;
		} catch (NoAlertPresentException e) {
			return false;
		}
	}
	/*       ###########################################################################################
    Name                      : Change_Business_date()
    Description        		  : function to get the business date
    Developed                 : Nikhil 01/12/2017

    Input Parameters          : 1. String DString :get the business date from the env sheet
    						  :	2. 
    Returns                   : 1. Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/

	public String Change_Business_date(String DString) throws IOException {
		try{
			System.out.println("*****Logged in********");
			String BusinessDate = null;
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Calendar c = Calendar.getInstance();

			Date DT = sdf.parse(TestConsts.ENV_GPPMP_BussDate);

			c.setTime(DT);
			c.add(Calendar.DATE, Integer.parseInt(DString.substring(1)));
			BusinessDate = sdf.format(c.getTime());

			// Driver Wait object to wait until 10 Seconds for the Web Element to becomes visible.
			WebDriverWait wait = new WebDriverWait(driver, 10);
			Actions action = new Actions (driver);
			Select TPOffice = null;

			//code to click on system
			OR.FindElement(driver, ORMap.get("MP_Land_SystemMenu")).click();
			Thread.sleep(500);
			//click on execute task
			OR.FindElement(driver, ORMap.get("MP_Click_Execute_Task")).click();
			//click on new day button
			WebElement element= OR.FindElement(driver, ORMap.get("MP_Click_New_Day"));				
			action.moveToElement(element).build().perform();

			//click on advance local business office date
			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_Advance_Local_office_Business_date"))).build().perform();      
			Thread.sleep(200);
			OR.FindElement(driver, ORMap.get("MP_Click_Advance_Local_office_Business_date")).click();

			String MainWindowHandle = driver.getWindowHandle();
			// Switch to new window opened
			System.out.println();
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle);
					break;
				}
			}

			TPOffice = new Select(OR.FindElement(driver, ORMap.get("MP_BusinessDate_SelectOffice")));
			TPOffice.selectByVisibleText("IE1");

			if(driver.findElement(By.id("TP_Next Valid Business Date")).getAttribute("value").contentEquals(BusinessDate)){
				TestLogger.log(LogStatus.INFO,"Advance Business Date already set and was not changed: "+BusinessDate);
				driver.findElement(By.id("B_bt_2")).click();
				driver.switchTo().window(MainWindowHandle);
				return "Pass";
			}

			//click on run task button
			OR.FindElement(driver, ORMap.get("TP_Next_Valid_Business_Date")).clear();
			OR.FindElement(driver, ORMap.get("TP_Next_Valid_Business_Date")).sendKeys(BusinessDate);

			//			driver.findElement(By.id("TP_Next Valid Business Date")).clear();
			//			driver.findElement(By.id("TP_Next Valid Business Date")).sendKeys(BusinessDate);
			Thread.sleep(500);

			//OR.FindElement(driver, ORMap.get("MP_Click_Run_Task_button")).click();
			driver.findElement(By.id("B_bt_1")).click();
			Thread.sleep(500);
			driver.switchTo().window(MainWindowHandle);// Again

			if(TaskWindowCheck("Advance Local Office Business Date").contentEquals("Fatal")){
				return "Fatal";
			}			    

			//Navigate to Advance MOP Business date
			//code to click on system
			OR.FindElement(driver, ORMap.get("MP_Land_SystemMenu")).click();
			Thread.sleep(500);
			//click on execute task
			OR.FindElement(driver, ORMap.get("MP_Click_Execute_Task")).click();
			//click on new day button
			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_New_Day"))).build().perform();
			Thread.sleep(500);

			//click on advance MOP Business date
			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_Advance_MOP_business_date"))).build().perform();
			Thread.sleep(500);

			OR.FindElement(driver, ORMap.get("MP_Click_Advance_MOP_business_date")).click();
			Thread.sleep(500);

			// Switch to new window opened
			System.out.println();
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle);
					break;
				}
			}

			//Perform the actions on new window
			//Select the IE1 from office
			TPOffice = new Select(OR.FindElement(driver, ORMap.get("MP_BusinessDate_SelectOffice")));
			TPOffice.selectByVisibleText("IE1");
			Thread.sleep(500);
			//click on run task button
			//OR.FindElement(driver, ORMap.get("MP_Click_Run_Task_button")).click();
			driver.findElement(By.id("B_bt_1")).click();
			Thread.sleep(500);

			driver.switchTo().window(MainWindowHandle);// Again 

			if(TaskWindowCheck("Advance MOP Business Dates").contentEquals("Fatal")){
				return "Fatal";
			}

			//Navigate to apply changes refresh internal cache
			//code to click on system
			OR.FindElement(driver, ORMap.get("MP_Land_SystemMenu")).click();
			Thread.sleep(500);
			//click on execute task
			OR.FindElement(driver, ORMap.get("MP_Click_Execute_Task")).click();
			Thread.sleep(500);
			//click on House Keeping button
			//div[@id='_MI82_SUBMENU']/descendant::td[contains(.,'Advance MOP Business Dates')]
			//div[@id='_MI81_SUBMENU']/descendant::td[contains(.,'New Day')]

			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_HouseKeeping_btn"))).build().perform();
			Thread.sleep(500); 
			//click on Group1
			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_Group1_btn"))).build().perform();
			Thread.sleep(500);
			//Click on Apply changes (refresh internal changes)
			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_Apply_Changes_Refresh_Internal_Changes"))).build().perform();
			Thread.sleep(500);
			OR.FindElement(driver, ORMap.get("MP_Click_Apply_Changes_Refresh_Internal_Changes")).click();

			// Switch to new window opened
			System.out.println();
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle);
					break;
				}
			}
			// Perform the actions on new window
			Thread.sleep(500);
			//click on run task button
			//OR.FindElement(driver, ORMap.get("MP_Click_Run_Task_button")).click();
			driver.findElement(By.id("B_bt_1")).click();
			Thread.sleep(4000);
			//wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("B_bt_1")));
			driver.switchTo().window(MainWindowHandle);// Again 

			if(TaskWindowCheck("Apply Changes (Refresh Internal Cache)").contentEquals("Fatal")){
				return "Fatal";
			}

			TestLogger.log(LogStatus.INFO,"Advance Change of Business Date was changed successfully to : "+BusinessDate);

		} 
		catch(Exception e){
			e.printStackTrace();
			// Logger 
			TestLogger.log(LogStatus.FATAL,"Advance Change of Business Date Automation Failure "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : TaskWindowCheck()
    Description        		  : function to handle the task after changing the date
    Developed                 : Nikhil 01/12/2017

    Input Parameters          : 1. String TaskName :give the task which you are going to handle in the script
    						  :	2. 
    Returns                   : 1. Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/
	private String TaskWindowCheck(String TaskName) throws IOException{

		try{

			WebDriverWait wait = new WebDriverWait(driver, 10);

			driver.switchTo().frame("main").switchTo().frame("fraTasks");
			Thread.sleep(500);
			wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.id("TableTask"))));

			int i = 0;
			do{
				if(driver.findElements(By.xpath("//table[@id='TableTask']/descendant::td[contains(.,'"+TaskName+"')]")).size()>0 || 
						driver.findElements(By.xpath("//table[@id='TableTask']/descendant::td[contains(.,'"+TaskName+"')]/following-sibling::td[1][contains(.,'pending')]")).size()>0){
					if(driver.findElements(By.xpath("//table[@id='TableTask']/descendant::td[contains(.,'"+TaskName+"')]/following-sibling::td[1][contains(.,'error')]")).size()>0){
						driver.switchTo().defaultContent();
						TestLogger.log(LogStatus.FATAL, "Task execution of '"+TaskName+"' got error. Screenshot:"+Screenshot.ObjectSnapFullPage(driver));
						driver.switchTo().frame("main").switchTo().frame("fraTasks");
						driver.findElement(By.xpath("//table[@id='TableTask']/descendant::td[contains(.,'"+TaskName+"')]/following-sibling::td[2]/button")).click();
						return "Fatal";
					} else if(driver.findElements(By.xpath("//table[@id='TableTask']/descendant::td[contains(.,'"+TaskName+"')]/following-sibling::td[1][contains(.,'completed')]")).size()>0){
						TestLogger.log(LogStatus.INFO, "Task execution of '"+TaskName+"' is completed");
						driver.findElement(By.xpath("//table[@id='TableTask']/descendant::td[contains(.,'"+TaskName+"')]/following-sibling::td[2]/button")).click();
						driver.switchTo().defaultContent();
						break;
					}
				} else{
					Thread.sleep(2000);
					i=i+1;
				}
			} while(i<10);
			//driver.switchTo().defaultContent();
		}catch(Exception e){ 
			TestLogger.log(LogStatus.FATAL,"Task execution of '"+TaskName+"' got errors. "+e.toString()+". Screenshot:"+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}

	/*       ###########################################################################################
    Name                      : Release_From_Forward_Processing()
    Description        		  : function to release payment for forward processing and apply internal changes
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          : 1. String Office: name of the office which is going to use(IE1)
    						  :	2. String RelMethod: name of the payment area 
    Returns                   : 1. Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    Subrat                          15/02/2018                 Changed the name of the method from Release_payments_on_SOD to Release_From_Forward_Processing
    ############################################################################################*/

	public String Release_From_Forward_Processing(String Office, String RelMethod) throws IOException{

		try{
			WebDriverWait wait = new WebDriverWait(driver, 10);

			Actions action = new Actions (driver);
			String MainWindowHandle = driver.getWindowHandle();
			Select GenericSelect = null;

			//Click on System button
			OR.FindElement(driver, ORMap.get("MP_Land_SystemMenu")).click();
			Thread.sleep(500);
			//click on execute task
			OR.FindElement(driver, ORMap.get("MP_Click_Execute_Task")).click();
			Thread.sleep(500);

			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_New_Day"))).build().perform();
			Thread.sleep(500);

			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_Advance_Local_office_Business_date"))).build().perform();
			Thread.sleep(500);
			//click on release from forward processing
			OR.FindElement(driver, ORMap.get("MP_Click_forward_Processing")).click();
			Thread.sleep(1000);				
			// Switch to new window opened
			System.out.println();
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle);
					break;
				}
			}

			//Select the IE1 from office
			GenericSelect = new Select(OR.FindElement(driver, ORMap.get("MP_BusinessDate_SelectOffice")));
			GenericSelect.selectByVisibleText(Office);

			//GenericSelect = new Select(OR.FindElement(driver, ORMap.get("MP_TP_MOP")));
			GenericSelect = new Select(driver.findElement(By.id("TP_MOP")));
			GenericSelect.selectByVisibleText("");
			//GenericSelect = new Select(OR.FindElement(driver, ORMap.get("MP_TP_RELEASE_METHOD")));
			GenericSelect = new Select(driver.findElement(By.id("TP_RELEASE_METHOD")));
			GenericSelect.selectByVisibleText(RelMethod);

			OR.FindElement(driver, ORMap.get("MP_Click_forward_Processing_Run_Task_btn")).click();

			driver.switchTo().window(MainWindowHandle);// Again

			if(TaskWindowCheck("Release from Forward Processing").contentEquals("Fatal")){
				return "Fatal";
			}


			//Navigate to apply changes refresh internal cache
			//code to click on system
			OR.FindElement(driver, ORMap.get("MP_Land_SystemMenu")).click();
			Thread.sleep(500);
			//click on execute task
			OR.FindElement(driver, ORMap.get("MP_Click_Execute_Task")).click();
			Thread.sleep(500);
			//click on House Keeping button
			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_HouseKeeping_btn"))).build().perform();
			Thread.sleep(500); 
			//click on Group1
			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_Group1_btn"))).build().perform();
			Thread.sleep(500);
			//Click on Apply changes (refresh internal changes)
			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_Apply_Changes_Refresh_Internal_Changes"))).build().perform();
			Thread.sleep(500);
			OR.FindElement(driver, ORMap.get("MP_Click_Apply_Changes_Refresh_Internal_Changes")).click();
			//switch to window
			Thread.sleep(1000);
			// Switch to new window opened
			System.out.println();
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle);
					break;
				}
			}
			//GenericSelect = new Select(OR.FindElement(driver, ORMap.get("MP_TP_Cache_Type")));
			GenericSelect = new Select(driver.findElement(By.id("TP_Cache Type")));
			GenericSelect.selectByVisibleText("ALL");
			//click on run task button
			//OR.FindElement(driver, ORMap.get("MP_Click_Run_Task_button")).click();
			driver.findElement(By.id("B_bt_1")).click();
			//wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("B_bt_1")));
			driver.switchTo().window(MainWindowHandle);// Again 

			if(TaskWindowCheck("Apply Changes (Refresh Internal Cache)").contentEquals("Fatal")){
				return "Fatal";
			}

			TestLogger.log(LogStatus.INFO,"Task: Release Payments from forward processing executed successfully");

		} 
		catch(Exception e){
			e.printStackTrace();
			// Logger 
			TestLogger.log(LogStatus.FATAL,"Task: Release Payments from forward processing Caused Automation Error. "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}

	/*       ###########################################################################################
    Name                      : LogoutTestApp()
    Description        		  : method to logout from the application
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          : None
    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/	
	public String LogoutTestApp() throws IOException{

		try{
			//Click on system button
			Thread.sleep(5000);
			OR.FindElement(driver, ORMap.get("MP_Land_SystemMenu")).click();
			Thread.sleep(1000);
			OR.FindElement(driver, ORMap.get("MP_Land_System_Exit")).click();
			Thread.sleep(1000);
			TestLogger.log(LogStatus.INFO,"Logout was successful");			


		} catch(Exception e){
			e.printStackTrace();
			// Logger 
			TestLogger.log(LogStatus.FATAL,"Logout to GPPSP had Errors: Error Description - "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : TransactionSearch()
    Description        		  : method to navigate to the transaction page
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          : 1.String FileName:input file name
    							2.String E2EKey:E2E key from the input xml
    							3.String DBQuery:Database query to get the MID
    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/	
	public String TransactionSearch(String FileName, String E2EKey, String DBQuery) throws IOException {

		//Driver wait object to wait until 10 seconds for the web element to becomes visible
		WebDriverWait wait=new WebDriverWait(driver,10);

		try{
			// Getting E2EID from File
			String E2EID = TestConsts.SUPER_XML_FILE_DATAMAP.get(FileName).get(E2EKey);
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
			// Getting MID from DB for MsgID			

			wait.until(ExpectedConditions.visibilityOf(OR.FindElement(driver, ORMap.get("MP_Click_Messages"))));

			OR.FindElement(driver, ORMap.get("MP_Click_Messages")).click();
			OR.FindElement(driver, ORMap.get("MP_Click_Messages_Search")).click();
			Thread.sleep(1000);
			//select the mass payments radio button		
			String MainWindowHandle = driver.getWindowHandle();
			// Switch to new window opened
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle);
					break;
				}
			}
			// Perform the actions on new window
			OR.FindElement(driver, ORMap.get("MP_Select_MassPayments_Radiobtn")).click();
			//OR.FindElement(driver, ORMap.get("MP_Search_Individual")).click();
			//			if(!OR.FindElement(driver, ORMap.get("MP_Select_MID")).getAttribute("value").contentEquals("")){
			//			OR.FindElement(driver, ORMap.get("MP_Select_MID")).clear();
			driver.findElement(By.id("Individual")).click();
			if(!driver.findElement(By.id("MIF.MID")).getAttribute("value").contentEquals("")){
				driver.findElement(By.id("MIF.MID")).clear();

			} else if(!OR.FindElement(driver, ORMap.get("MP_Enter_Local_Refernce")).getAttribute("value").contentEquals("")){
				OR.FindElement(driver, ORMap.get("MP_Enter_Local_Refernce")).clear();
			}

			//Select the IE1 from office
			//Select Office = new Select (driver, ORMap.get("MP_MIF_OFFICE")));
			Select Office = new Select (driver.findElement(By.id("MIF.OFFICE")));
			Office.deselectAll();
			Office.selectByValue("IE1");
			//OR.FindElement(driver, ORMap.get("MP_Select_MID")).sendKeys(gppMID.get(0));
			driver.findElement(By.id("MIF.MID")).sendKeys(gppMID.get(0));
			//(gppMID.get(0));
			//click on search button
			OR.FindElement(driver, ORMap.get("MP_Click_Search_Messages_btn")).click();
			Thread.sleep(500);
			driver.switchTo().window(MainWindowHandle);// Again
			Thread.sleep(500);

			if(isAlertPresent()){
				System.out.println();
				for (String winHandle : driver.getWindowHandles()) {
					if (!winHandle.equals(MainWindowHandle)) {
						driver.switchTo().window(winHandle);
						Thread.sleep(500);
						break;
					}
				}
				OR.FindElement(driver, ORMap.get("MP_SearchDialog_CloseButton")).click();
				driver.switchTo().window(MainWindowHandle);
				TestLogger.log(LogStatus.FATAL,"No Transaction present with searched E2E ID: "+E2EID+"Screenshot - "
						+Screenshot.ObjectSnapFullPage(driver));
				Thread.sleep(1000);
				driver.switchTo().window(MainWindowHandle);
				LogoutTestApp();
				return "Fatal";
			} else{
				driver.switchTo().window(MainWindowHandle);
				TestLogger.log(LogStatus.INFO,"Transaction was located searched with E2E ID: "+E2EID);
			}

		}
		catch(Exception e){
			e.printStackTrace();				
			TestLogger.log(LogStatus.FATAL,"Automation Error while Searching Transaction"+e.toString()+"Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}

		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : CloseTab()
    Description        		  : method to close the tab
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          : 1.String TabName:provide the TAB name which you are going to close
    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/	
	public String CloseTab(String TabName) throws IOException{
		try{
			driver.switchTo().defaultContent();
			if(driver.findElements(By.xpath("//div[@id='TdButtons']/div[contains(.,'"+TabName+"')]")).size()>0){
				driver.findElement(By.xpath("//div[@id='TdButtons']/div[contains(.,'"+TabName+"')]")).click();

				WebElement goButton = driver.findElement(By.xpath("//div[@id='CloseBut']"));
				boolean status = JavaScriptClick(goButton);

				if(status){
					TestLogger.log(LogStatus.INFO,"Tab: "+TabName+" is Closed");
					return "Pass";
				}else{
					TestLogger.log(LogStatus.FAIL,"Tab : "+TabName+" could not be Closed");
					return "Pass";
				}
			}else{
				TestLogger.log(LogStatus.FAIL,"Tab : "+TabName+" could not be found open");
				return "Fail";
			}				
		} 
		catch(Exception e){
			e.printStackTrace();
			TestLogger.log(LogStatus.FATAL,"Automation Failure while Switching Tabs - "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
	}
	/*       ###########################################################################################
    Name                      : JavaScriptClick()
    Description        		  : method to close the tab
    Developed                 : Subrat 10/01/2018

    Input Parameters          : 1.WebElement element:provide the WebElement which you are going Click
    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/	
	private boolean JavaScriptClick(WebElement element) throws Exception {

		try {
			if (element.isEnabled() && element.isDisplayed()) {
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
				return true;
			} else {
				return false;
			}
		} catch (StaleElementReferenceException e) {
			return true;
		}
	}

	/*       ###########################################################################################
    Name                      : File_Search_Internal_Criteria_Using_FileId()
    Description        		  : function to navigate to the file search page and add criteria as Internal file ID and value equals to internal id from MP Info
    Developed                 : Nikhil Katare 01/12/2017

    Input Parameters          : 1. String FileName: name of the xml file which is processed
    						  :	2. String key: stored the internal id from MP info to a MAP
    Returns                   : 1. Make sure if the method was executed successfully and otherwise return Fatal to terminate the test

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    Subrat                          15/02/2018                 Changed the name of the method from SearchByInternalFileID to File_Search_Internal_Criteria_Using_FileId
    ############################################################################################*/
	public String File_Search_Internal_Criteria_Using_FileId(String FileName,String key) throws IOException {

		String InternalFileID;



		try{
			//Driver wait object to wait until 10 seconds for the web element to becomes visible
			WebDriverWait wait=new WebDriverWait(driver,10);

			wait.until(ExpectedConditions.visibilityOf(OR.FindElement(driver, ORMap.get("MP_Click_Messages"))));

			OR.FindElement(driver, ORMap.get("MP_Click_Messages")).click();
			OR.FindElement(driver, ORMap.get("MP_Click_Messages_Search")).click();
			Thread.sleep(1000);
			//select the mass payments radio button		
			String MainWindowHandle = driver.getWindowHandle();
			// Switch to new window opened
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle);
					break;
				}
			}			   

			// == Get Internal File ID from XML Data Map ==
			InternalFileID= TestConsts.SUPER_XML_FILE_DATAMAP.get(FileName).get(key);
			// ======

			// Perform the actions on new window
			OR.FindElement(driver, ORMap.get("MP_Select_MassPayments_Radiobtn")).click();
			//			OR.FindElement(driver, ORMap.get("MP_Select_File")).click();
			//			
			//			if(!OR.FindElement(driver, ORMap.get("MP_Select_MID")).getAttribute("value").contentEquals("")){
			//				OR.FindElement(driver, ORMap.get("MP_Select_MID")).clear();
			driver.findElement(By.id("File")).click();
			if(!driver.findElement(By.id("MIF.MID")).getAttribute("value").contentEquals("")){
				driver.findElement(By.id("MIF.MID")).clear();					   
			} else if(!OR.FindElement(driver, ORMap.get("MP_Enter_Local_Refernce")).getAttribute("value").contentEquals("")){
				OR.FindElement(driver, ORMap.get("MP_Enter_Local_Refernce")).clear();
			}		   
			//Select Office = new Select (driver, ORMap.get("MP_MIF_OFFICE")));
			Select Office = new Select (driver.findElement(By.id("MIF.OFFICE")));
			Office.deselectAll();
			Office.selectByValue("IE1");
			OR.FindElement(driver, ORMap.get("MP_Enter_blank_From_date")).clear();
			OR.FindElement(driver, ORMap.get("MP_Enter_blank_to_date")).clear();
			//OR.FindElement(driver, ORMap.get("MP_cmdClearConditions")).click();
			driver.findElement(By.id("cmdClearConditions")).click();    
			//OR.FindElement(driver, ORMap.get("MP_cmdAddRow")).click();
			driver.findElement(By.id("cmdAddRow")).click();
			Thread.sleep(500);
			//Select Office = new Select (driver, ORMap.get("MP_FIELD_SELECT1")));
			Select Criteria = new Select (driver.findElement(By.id("FIELD_SELECT1")));
			Criteria.selectByVisibleText("Internal file ID");
			//Select Office = new Select (driver, ORMap.get("MP_OPERATION_SELECT1")));
			Select Operation = new Select (driver.findElement(By.id("OPERATION_SELECT1")));
			Operation.selectByVisibleText("Equal To");
			//OR.FindElement(driver, ORMap.get("MP_txtFromVal1")).sendKeys(InternalFileID);
			driver.findElement(By.id("txtFromVal1")).sendKeys(InternalFileID);

			//Select the IE1 from office
			OR.FindElement(driver, ORMap.get("MP_Click_Search_Messages_btn")).click();


			driver.switchTo().window(MainWindowHandle);// Again
			Thread.sleep(1500);

			if(isAlertPresent()){
				System.out.println();
				for (String winHandle : driver.getWindowHandles()) {
					if (!winHandle.equals(MainWindowHandle)) {
						driver.switchTo().window(winHandle);
						Thread.sleep(500);
						break;
					}
				}
				OR.FindElement(driver, ORMap.get("MP_SearchDialog_CloseButton")).click();
				driver.switchTo().window(MainWindowHandle);
				TestLogger.log(LogStatus.FATAL,"No File present with searched Internal File ID: "+InternalFileID+". Screenshot - "
						+Screenshot.ObjectSnapFullPage(driver));
				Thread.sleep(1000);
				driver.switchTo().window(MainWindowHandle);
				LogoutTestApp();
				return "Fatal";
			} else{
				driver.switchTo().window(MainWindowHandle);
				TestLogger.log(LogStatus.INFO,"File was located searched with Internal File ID: "+InternalFileID);
			}

		}
		catch(Exception e){
			e.printStackTrace();				
			TestLogger.log(LogStatus.FATAL,"Failed to Search file based on refernce details "+e.toString()+"Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}

		return "Pass";
	}
	/*       ###########################################################################################
    Name                      : clickLink()
    Description        		  : function to click on a link(robot)
    Developed                 : Subrat 10/01/2018

    Input Parameters          : None

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/

	public void clickLink() throws AWTException{

		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_DOWN);
		robot.keyRelease(KeyEvent.VK_DOWN);
		robot.keyPress(KeyEvent.VK_ENTER);
		robot.keyRelease(KeyEvent.VK_ENTER);

	}
	/*       ###########################################################################################
    Name                      : getTimeStamp()
    Description        		  : function to return the current time
    Developed                 : Subrat 10/01/2018

    Input Parameters          : None

    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/	
	public static String getTimeStamp()
	{
		Format formatter = new SimpleDateFormat("HH:mm:ss");
		Date today = Calendar.getInstance().getTime();        
		String formattedDate = formatter.format(today);

		return formattedDate;
	}
	/*       ###########################################################################################
Name                      : getPasTime()
Description        		  : function to return the past time
Developed                 : Subrat 10/01/2018

Input Parameters          : None

U P D A T E S
--------------------------------------------------------------------------------------------
Author                          Date                       Description
--------------------------------------------------------------------------------------------
None
############################################################################################*/	
	public static String getPasTime()
	{

		Format formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.getTime();
		calendar.add(Calendar.MINUTE, -5);

		String formattedTime = formatter.format(calendar.getTime());

		return formattedTime;
	}
	/*       ###########################################################################################
	Name                      : Approvals_NSF()
	Description        		  : function to approve NSF cutoffs
	Developed                 : Sadashiv 10/01/2018

	Input Parameters          : None

	U P D A T E S
	--------------------------------------------------------------------------------------------
	Author                          Date                       Description
	--------------------------------------------------------------------------------------------
	None
	############################################################################################*/	
	public String Approvals_NSF() throws Exception{

		try{
			Thread.sleep(2500);
			driver.findElement(By.xpath("//button[@id='_btn_MENU486']")).click();
			//OR.FindElement(driver, ORMap.get("MP_Click_Profiles")).click();
			driver.findElement(By.xpath("//div[@id='_MENU486']/descendant::td[contains(.,'Approvals...')]")).click();
			//OR.FindElement(driver, ORMap.get("MP_Click_Approvals")).click();
			Thread.sleep(2500);
			String winHandleBefore = driver.getWindowHandle();

			for (String winHandle : driver.getWindowHandles()) {
				driver.switchTo().window(winHandle);
			}

			Thread.sleep(500);
			clickLink();
			Thread.sleep(3000);
			System.out.println();
			String parentWindow1 = driver.getWindowHandle();
			Set<String> handles1 =  driver.getWindowHandles();
			for(String windowHandle  : handles1)
			{
				if(!windowHandle.equals(parentWindow1))
				{
					driver.switchTo().window(windowHandle);
					System.out.println("PASS");
				}

			}
			Thread.sleep(2500);
			//driver1.findElement(By.xpath("//button[@id='B_STATIC_DATA_REFRESH.BTN_OPEN']")).click();
			driver.findElement(By.id("B_STATIC_DATA_REFRESH.BTN_OPEN")).click();  
			//OR.FindElement(driver, ORMap.get("MP_Click_Open_btn")).click();
			Thread.sleep(4500);
			String parentWindow11 = driver.getWindowHandle();
			Set<String> handles11 =  driver.getWindowHandles();
			for(String windowHandle  : handles11)
			{
				if(!windowHandle.equals(parentWindow11))
				{
					driver.switchTo().window(windowHandle);
					System.out.println("PASS");
				}

			}
			//System.out.println();
			Thread.sleep(4500);
			//System.out.println();
			driver.findElement(By.xpath("//table[@id='T_FOOTER']/tbody/tr/td[3]")).click();
			//OR.FindElement(driver, ORMap.get("MP_Click_Approve")).click();
			Thread.sleep(6000);
			System.out.println();
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(winHandleBefore)) {
					driver.switchTo().window(winHandle).close();
					Thread.sleep(1000);
					break;
				}

			}

			driver.switchTo().window(winHandleBefore);
			Thread.sleep(4000);
			System.out.println("Approval1 completed");
			TestLogger.log(LogStatus.INFO,"Cut off process has been approved successfully for NSFREJ cutoff");

		} catch (Exception e) {
			e.printStackTrace();
			// Logger
			TestLogger.log(LogStatus.FATAL,"Unable to apply the changes for approval process - " + e.toString()+ " Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
	Name                      : Approvals_RMS()
	Description        		  : function to approve RMSG cutoffs
	Developed                 : Sadashiv 10/01/2018

	Input Parameters          : None

	U P D A T E S
	--------------------------------------------------------------------------------------------
	Author                          Date                       Description
	--------------------------------------------------------------------------------------------
	None
	############################################################################################*/	
	public String Approvals_RMS() throws Exception{

		try{

			Thread.sleep(2500);
			driver.findElement(By.xpath("//button[@id='_btn_MENU486']")).click();
			//OR.FindElement(driver, ORMap.get("MP_Click_Profiles")).click();
			//driver.findElement(By.id("_btn_MENU486")).click();
			driver.findElement(By.xpath("//div[@id='_MENU486']/descendant::td[contains(.,'Approvals...')]")).click();
			//OR.FindElement(driver, ORMap.get("MP_Click_Approvals")).click();
			Thread.sleep(2500);
			String winHandleBefore = driver.getWindowHandle();

			for (String winHandle : driver.getWindowHandles()) {
				driver.switchTo().window(winHandle);
			}

			Thread.sleep(500);
			clickLink();
			Thread.sleep(3000);
			System.out.println();
			String parentWindow1 = driver.getWindowHandle();
			Set<String> handles1 =  driver.getWindowHandles();
			for(String windowHandle  : handles1)
			{
				if(!windowHandle.equals(parentWindow1))
				{
					driver.switchTo().window(windowHandle);
					System.out.println("PASS");
				}

			}
			Thread.sleep(2500);
			//driver1.findElement(By.xpath("//button[@id='B_STATIC_DATA_REFRESH.BTN_OPEN']")).click();
			driver.findElement(By.id("B_STATIC_DATA_REFRESH.BTN_OPEN")).click();
			//OR.FindElement(driver, ORMap.get("MP_Click_Open_btn")).click();
			Thread.sleep(4500);
			String parentWindow11 = driver.getWindowHandle();
			Set<String> handles11 =  driver.getWindowHandles();
			for(String windowHandle  : handles11)
			{
				if(!windowHandle.equals(parentWindow11))
				{
					driver.switchTo().window(windowHandle);
					System.out.println("PASS");
				}

			}
			//System.out.println();
			Thread.sleep(4500);
			//System.out.println();
			driver.findElement(By.xpath("//table[@id='T_FOOTER']/tbody/tr/td[3]")).click();
			//OR.FindElement(driver, ORMap.get("MP_Click_Approve")).click();
			Thread.sleep(6000);
			System.out.println();
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(winHandleBefore)) {
					driver.switchTo().window(winHandle).close();
					Thread.sleep(1000);
					break;
				}

			}

			driver.switchTo().window(winHandleBefore);
			System.out.println("Approval2 completed");
			TestLogger.log(LogStatus.INFO,"Cut off process has been approved successfully for RMSGCUTOFF cutoff");

		} catch (Exception e) {
			e.printStackTrace();
			// Logger
			TestLogger.log(LogStatus.FATAL,"Unable to apply the changes for approval process - " + e.toString()+ " Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
	Name                      : ApplySelectiveChanges()
	Description        		  : function to navigate and perform Apply Selective changes
	Developed                 : Sadashiv 10/01/2018

	Input Parameters          : None

	U P D A T E S
	--------------------------------------------------------------------------------------------
	Author                          Date                       Description
	--------------------------------------------------------------------------------------------
	None
	############################################################################################*/	

	public String ApplySelectiveChanges() throws Exception{

		try {
			Thread.sleep(2000);
			//Click on System button
			//OR.FindElement(driver, ORMap.get("MP_Land_SystemMenu")).click();
			driver.findElement(By.id("_btn_MENU1")).click();
			//OR.FindElement(driver, ORMap.get("MP_Select_Apply_Changes")).click();
			driver.findElement(By.xpath("//tr[@id='_MI462']/td[contains(.,'Selective Apply Changes')]")).click();

			Thread.sleep(2500);
			// focus on child browser instance
			String winHandleBefore = driver.getWindowHandle();
			// Switch to new window opened

			for (String winHandle : driver.getWindowHandles()) {
				driver.switchTo().window(winHandle);
			}
			// Perform the actions on new window
			Thread.sleep(500);
			// select the profile cut off time from the window			
			//Select listbox=OR.FindElement(driver, ORMap.get("MP_Select_Value_window"));
			Select listbox = new Select(driver.findElement(By.xpath("//select[@id='STATIC_DATA_REFRESH.AVAILABLE_VALUES']")));
			listbox.selectByIndex(0);
			//Click on next button
			//OR.FindElement(driver, ORMap.get("Click_Next_btn")).click();
			driver.findElement(By.xpath("//button[@id='STATIC_DATA_REFRESH.B_ADD_ITEM']")).click();
			Thread.sleep(500);
			//Click on OK button
			//OR.FindElement(driver, ORMap.get("Click_Ok_btn")).click();
			driver.findElement(By.xpath("//button[@title='OK - Enabled']")).click();
			TestLogger.log(LogStatus.INFO,"Apply the selective changes on approval process is Success");
			driver.switchTo().window(winHandleBefore);
			Thread.sleep(4000);

		} catch (Exception e) {
			e.printStackTrace();
			// Logger
			TestLogger.log(LogStatus.FATAL,"Unable to apply the changes for approval process - " + e.toString()+ " Screenshot - "+ Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
	Name                      : PostWarehoused_SOD()
	Description        		  : function to release payment for PostWarehoused and apply internal changes
	Developed                 : Sadashiv 10/01/2018

	Input Parameters          : None

	U P D A T E S
	--------------------------------------------------------------------------------------------
	Author                          Date                       Description
	--------------------------------------------------------------------------------------------
	None
	############################################################################################*/	
	public String PostWarehoused_SOD(String Office, String RelMethod) throws IOException{

		try{
			WebDriverWait wait = new WebDriverWait(driver, 10);

			Actions action = new Actions (driver);
			String MainWindowHandle = driver.getWindowHandle();
			Select GenericSelect = null;

			//Click on System button
			OR.FindElement(driver, ORMap.get("MP_Land_SystemMenu")).click();
			Thread.sleep(500);
			//click on execute task
			OR.FindElement(driver, ORMap.get("MP_Click_Execute_Task")).click();
			Thread.sleep(500);

			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_New_Day"))).build().perform();
			Thread.sleep(500);

			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_Advance_Local_office_Business_date"))).build().perform();
			Thread.sleep(500);
			//click on release from forward processing
			OR.FindElement(driver, ORMap.get("MP_Click_forward_Processing")).click();
			Thread.sleep(1000);                       
			// Switch to new window opened
			System.out.println();
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle);
					break;
				}
			}

			//Select the IE1 from office
			GenericSelect = new Select(OR.FindElement(driver, ORMap.get("MP_BusinessDate_SelectOffice")));
			GenericSelect.selectByVisibleText(Office);

			GenericSelect = new Select(driver.findElement(By.id("TP_MOP")));
			GenericSelect.selectByVisibleText("");

			GenericSelect = new Select(driver.findElement(By.id("TP_RELEASE_METHOD")));
			GenericSelect.selectByVisibleText(RelMethod);

			OR.FindElement(driver, ORMap.get("MP_Click_forward_Processing_Run_Task_btn")).click();

			driver.switchTo().window(MainWindowHandle);// Again

			if(TaskWindowCheck("Release from Forward Processing").contentEquals("Fatal")){
				return "Fatal";
			}

			// code to click on system
			driver.findElement(By.id("_btn_MENU1")).click();
			Thread.sleep(1500);
			///OR.FindElement(driver, ORMap.get("MP_Land_SystemMenu")).click();
			Thread.sleep(500);
			//click on execute task
			// OR.FindElement(driver, ORMap.get("MP_Click_Execute_Task")).click();
			Thread.sleep(500);
			driver.findElement(By.xpath("//div[@id='_MENU1']/descendant::td[contains(.,'Execute Task')]")).click();
			Thread.sleep(1500);

			// click on new day button
			//action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_New_Day"))).build().perform();
			//Thread.sleep(500);
			WebElement element = driver.findElement(By.xpath("//div[@id='_MI81_SUBMENU']/descendant::td[contains(.,'New Day')]"));
			Actions action10 = new Actions(driver);
			action10.moveToElement(element).build().perform();
			Thread.sleep(1500);
			// click on local advance office business date
			// action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_Advance_Local_office_Business_date"))).build().perform();
			WebElement element1 = driver.findElement(By.xpath("//div[@id='_MI82_SUBMENU']/descendant::td[contains(.,'Advance Local Office Business Date')]"));
			Actions action11 = new Actions(driver);
			action11.moveToElement(element1).build().perform();
			// click on release from post warehoused posting entries
			driver.findElement(By.xpath("//div[@id='_MI82_SUBMENU']/descendant::td[contains(.,'Post warehoused posting entries - MP')]")).click();
			// OR.FindElement(driver, ORMap.get("MP_Click_PostWarehoused")).click();
			Thread.sleep(1000);
			//here
			// Select mass payments
			String winHandleBefore8 = driver.getWindowHandle();
			// Switch to new window opened
			for (String winHandle8 : driver.getWindowHandles()) {
				driver.switchTo().window(winHandle8);
			}
			Thread.sleep(8500);
			driver.findElement(By.xpath("//button[@id='B_bt_1']")).click();
			Thread.sleep(4500);
			driver.switchTo().window(winHandleBefore8);// Again
			if(TaskWindowCheck("Post warehoused posting entries - MP").contentEquals("Fatal")){
				return "Fatal";
			}
			//Navigate to apply changes refresh internal cache
			//code to click on system
			OR.FindElement(driver, ORMap.get("MP_Land_SystemMenu")).click();
			Thread.sleep(500);
			//click on execute task
			OR.FindElement(driver, ORMap.get("MP_Click_Execute_Task")).click();
			Thread.sleep(500);
			//click on House Keeping button
			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_HouseKeeping_btn"))).build().perform();
			Thread.sleep(500); 
			//click on Group1
			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_Group1_btn"))).build().perform();
			Thread.sleep(500);
			//Click on Apply changes (refresh internal changes)
			action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_Apply_Changes_Refresh_Internal_Changes"))).build().perform();
			Thread.sleep(500);
			OR.FindElement(driver, ORMap.get("MP_Click_Apply_Changes_Refresh_Internal_Changes")).click();
			//switch to window
			Thread.sleep(1000);
			// Switch to new window opened
			//here
			System.out.println();
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle);
					break;
				}
			}

			GenericSelect = new Select(driver.findElement(By.id("TP_Cache Type")));
			GenericSelect.selectByVisibleText("ALL");
			//click on run task button
			driver.findElement(By.id("B_bt_1")).click();
			//wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("B_bt_1")));
			driver.switchTo().window(MainWindowHandle);// Again 

			if(TaskWindowCheck("Apply Changes (Refresh Internal Cache)").contentEquals("Fatal")){
				return "Fatal";
			}

			TestLogger.log(LogStatus.INFO,"Task: Release Payments from forward processing executed successfully for mass payments");

		} 
		catch(Exception e){
			e.printStackTrace();
			// Logger 
			TestLogger.log(LogStatus.FATAL,"Task: Release Payments from forward processing Caused Automation Error. "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	/*       ###########################################################################################
	Name                      : CUTOFF_RMS()
	Description        		  : function to apply RMSG cutoffs
	Developed                 : Subart 10/01/2018

	Input Parameters          : None

	U P D A T E S
	--------------------------------------------------------------------------------------------
	Author                          Date                       Description
	--------------------------------------------------------------------------------------------
	None
	############################################################################################*/	
	public String CUTOFF_RMS() throws Exception{

		Actions action = new Actions (driver);
		try
		{
			System.out.println(driver.getCurrentUrl());
			driver.findElement(By.xpath("//button[@id='_btn_MENU486']")).click();
			//OR.FindElement(driver, ORMap.get("MP_Click_Profiles")).click();
			WebElement ele= driver.findElement(By.xpath("//div[@id='_MENU486']/descendant::td[contains(.,'Cutoffs')]"));
			action.moveToElement(ele).build().perform();
			action.moveToElement(driver.findElement(By.xpath("//div[@id='_MI507_SUBMENU']/descendant::td[contains(.,'Cutoff Usages')]"))).build().perform();      
			//action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_Cutoff_Usage"))).build().perform();
			action.release();
			Thread.sleep(200);
			driver.findElement(By.xpath("//div[@id='_MI507_SUBMENU']/descendant::td[contains(.,'Cutoff Times')]")).click();
			//OR.FindElement(driver, ORMap.get("MP_CutoffTimes")).click();
			String MainWindowHandle = driver.getWindowHandle();
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle);
					Thread.sleep(200);
					break;
				}

			}
			TestLogger.log(LogStatus.INFO,"Successfully Navigate to Cutoff Times window : ");
			//OR.FindElement(driver, ORMap.get("MP_Click_show_btn")).click();

			driver.findElement(By.id("B_STATIC_DATA_REFRESH.FILTER")).click();

			Thread.sleep(2000);           
			//RMSGCUTOFF....
			clickLink();                        
			Thread.sleep(2000);
			System.out.println();
			String parentWindow1 = driver.getWindowHandle();
			Set<String> handles1 =  driver.getWindowHandles();
			for(String windowHandle  : handles1)
			{
				if(!windowHandle.equals(parentWindow1))
				{
					driver.switchTo().window(windowHandle);
					System.out.println("PASS");
				}

			}
			TestLogger.log(LogStatus.INFO,"Successfully Navigate to Cutoff Times(RMSGCUTOFF) Clearing window : ");
			System.out.println(driver.getCurrentUrl());

			driver.findElement(By.xpath("//input[@id='CUTOFF_PROFILE.FINAL_CUTOFF_EXT']")).clear();                  
			Thread.sleep(1000);
			//OR.FindElement(driver, ORMap.get("MP_Enter_CutoffFinal")).clear();
			driver.findElement(By.xpath("//input[@id='CUTOFF_PROFILE.FINAL_CUTOFF']")).clear();
			//OR.FindElement(driver, ORMap.get("MP_Enter_CutoffTime")).clear();
			Alert alert = driver.switchTo().alert();
			alert.accept();
			Thread.sleep(1000); 
			driver.findElement(By.xpath("//input[@id='CUTOFF_PROFILE.FINAL_CUTOFF']")).sendKeys(active_time);
			//OR.FindElement(driver, ORMap.get("MP_Enter_CutoffTime")).sendKeys(active_time);
			Thread.sleep(1000); 
			driver.findElement(By.xpath("//input[@id='CUTOFF_PROFILE.FINAL_CUTOFF_EXT']")).sendKeys(active_time);
			//OR.FindElement(driver, ORMap.get("MP_Enter_CutoffFinal")).sendKeys(active_time);
			TestLogger.log(LogStatus.INFO,"Successfully initiated Cutoffs for RMSGCUTOFF ");
			//GenericMethods.setText("//input[@id='CUTOFF_PROFILE.FINAL_CUTOFF_EXT']", active_time);
			Thread.sleep(1000); 
			driver.findElement(By.xpath("//table[@id='T_FOOTER']/tbody/tr/td[10]")).click();
			//OR.FindElement(driver, ORMap.get("MP_Click_Save")).click();
			Thread.sleep(1000);
			System.out.println();
			String parentWindow = driver.getWindowHandle();
			Set<String> handles =  driver.getWindowHandles();
			for(String windowHandle  : handles)
			{
				if(!windowHandle.equals(parentWindow))
				{
					driver.switchTo().window(windowHandle);
				}

			}
			TestLogger.log(LogStatus.INFO,"Successfully Created Cutoffs for RMS - "+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			System.out.println(driver.getCurrentUrl());
			Thread.sleep(1000); 
			driver.findElement(By.id("PROFILE_NOTE")).sendKeys("Automation");
			//OR.FindElement(driver, ORMap.get("MP_Click_Save")).sendKeys("Automation");
			driver.findElement(By.xpath("(//button[@id='cancel'])[2]")).click();
			//OR.FindElement(driver, ORMap.get("MP_Click_Cancel_btn")).click();
			Thread.sleep(1000);
			System.out.println();
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle).close();
					Thread.sleep(1000);
					break;
				}

			}

			driver.switchTo().window(MainWindowHandle);
			System.out.println("RMS Cutoff completed");
			TestLogger.log(LogStatus.INFO,"Successfully Navigate to Cutoff Times (RMSGCUTOFF)OK Window");
			Thread.sleep(1000);
		}catch(Exception e){
			System.out.println(e.getMessage());
			TestLogger.log(LogStatus.FATAL,"Automation Error while performing CutOffs(RMS): Error Description - "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "FATAL";
		}

		return "Pass";
	}
	/*       ###########################################################################################
	Name                      : CUTOFF_NSF()
	Description        		  : function to apply NSF cutoffs
	Developed                 : Subart 10/01/2018

	Input Parameters          : None

	U P D A T E S
	--------------------------------------------------------------------------------------------
	Author                          Date                       Description
	--------------------------------------------------------------------------------------------
	None
	############################################################################################*/	
	public String CUTOFF_NSF() throws Exception{
		System.out.println("Log on success to Cutoff NSF");

		Actions action = new Actions (driver);
		try
		{
			//OR.FindElement(driver, ORMap.get("MP_Click_Profiles")).click();
			driver.findElement(By.xpath("//button[@id='_btn_MENU486']")).click();

			//WebElement ele=OR.FindElement(driver, ORMap.get("MP_Click_Cutoffs"));
			//action.moveToElement(ele).build().perform();
			WebElement ele= driver.findElement(By.xpath("//div[@id='_MENU486']/descendant::td[contains(.,'Cutoffs')]"));
			action.moveToElement(ele).build().perform();
			action.moveToElement(driver.findElement(By.xpath("//div[@id='_MI507_SUBMENU']/descendant::td[contains(.,'Cutoff Usages')]"))).build().perform();      
			//action.moveToElement(OR.FindElement(driver, ORMap.get("MP_Click_Cutoff_Usage"))).build().perform();
			action.release();
			Thread.sleep(200);
			driver.findElement(By.xpath("//div[@id='_MI507_SUBMENU']/descendant::td[contains(.,'Cutoff Times')]")).click();
			//OR.FindElement(driver, ORMap.get("MP_CutoffTimes")).click();
			String MainWindowHandle = driver.getWindowHandle();
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle);
					Thread.sleep(200);
					break;
				}

			}
			TestLogger.log(LogStatus.INFO,"Successfully Navigate to Cutoff Times window : ");
			driver.findElement(By.id("B_STATIC_DATA_REFRESH.FILTER")).click();
			//OR.FindElement(driver, ORMap.get("MP_Click_show_btn")).click();
			Thread.sleep(2000); 
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_DOWN);
			robot.keyRelease(KeyEvent.VK_DOWN);
			robot.keyPress(KeyEvent.VK_DOWN);
			robot.keyRelease(KeyEvent.VK_DOWN);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER); 
			Thread.sleep(2000);
			System.out.println();
			String parentWindow1 = driver.getWindowHandle();
			Set<String> handles1 =  driver.getWindowHandles();
			for(String windowHandle  : handles1)
			{
				if(!windowHandle.equals(parentWindow1))
				{
					driver.switchTo().window(windowHandle);
					System.out.println("PASS");
				}

			}
			TestLogger.log(LogStatus.INFO,"Successfully Navigate to Cutoff Times(NSFREJ ) Clearing window : ");
			System.out.println(driver.getCurrentUrl());	
			driver.findElement(By.xpath("//input[@id='CUTOFF_PROFILE.FINAL_CUTOFF_EXT']")).clear();                  
			Thread.sleep(1000); 
			//OR.FindElement(driver, ORMap.get("MP_Enter_CutoffFinal")).clear();
			driver.findElement(By.xpath("//input[@id='CUTOFF_PROFILE.FINAL_CUTOFF']")).clear();
			//OR.FindElement(driver, ORMap.get("MP_Enter_CutoffTime")).clear();
			Alert alert = driver.switchTo().alert();
			alert.accept();
			Thread.sleep(1000); 
			driver.findElement(By.xpath("//input[@id='CUTOFF_PROFILE.FINAL_CUTOFF']")).sendKeys(past_time);
			//OR.FindElement(driver, ORMap.get("MP_Enter_CutoffTime")).sendKeys(past_time);
			Thread.sleep(1000); 
			driver.findElement(By.xpath("//input[@id='CUTOFF_PROFILE.FINAL_CUTOFF_EXT']")).sendKeys(past_time);
			//OR.FindElement(driver, ORMap.get("MP_Enter_CutoffFinal")).sendKeys(active_time);
			TestLogger.log(LogStatus.INFO,"Successfully initiated Cutoffs for NSFREJ  ");
			//GenericMethods.setText("//input[@id='CUTOFF_PROFILE.FINAL_CUTOFF_EXT']", active_time);
			Thread.sleep(1000); 
			driver.findElement(By.xpath("//table[@id='T_FOOTER']/tbody/tr/td[10]")).click();
			//OR.FindElement(driver, ORMap.get("MP_Click_Save")).click();
			Thread.sleep(1000);
			System.out.println();
			String parentWindow = driver.getWindowHandle();
			Set<String> handles =  driver.getWindowHandles();
			for(String windowHandle  : handles)
			{
				if(!windowHandle.equals(parentWindow))
				{
					driver.switchTo().window(windowHandle);
				}

			}
			TestLogger.log(LogStatus.INFO,"Successfully Created Cutoffs for NSF - "+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			System.out.println(driver.getCurrentUrl());
			Thread.sleep(1000); 
			driver.findElement(By.id("PROFILE_NOTE")).sendKeys("Automation");
			//OR.FindElement(driver, ORMap.get("MP_Click_Save")).sendKeys("Automation");
			driver.findElement(By.xpath("(//button[@id='cancel'])[2]")).click();
			//OR.FindElement(driver, ORMap.get("MP_Click_Cancel_btn")).click();
			Thread.sleep(1000);                
			System.out.println();
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle).close();
					Thread.sleep(1000);
					break;
				}

			}

			driver.switchTo().window(MainWindowHandle);
			System.out.println("NFS Cutoff completed");
			TestLogger.log(LogStatus.INFO,"Successfully Navigate to Cutoff Times (NSFREJ)OK Window");

		}catch(Exception e){
			System.out.println(e.getMessage());
			TestLogger.log(LogStatus.FATAL,"Automation Error while performing CutOffs(NSF): Error Description - "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "FATAL";
		}

		return "Pass";
	}
	/*       ###########################################################################################
	Name                      : Lump_Sum()
	Description        		  : function to apply NSF cutoffs
	Developed                 : Subart 10/01/2018

	Input Parameters          : 1.String task: Task name has to passed into for validation

	U P D A T E S
	--------------------------------------------------------------------------------------------
	Author                          Date                       Description
	--------------------------------------------------------------------------------------------
	None
	############################################################################################*/
	public String Lump_Sum(String task) throws InterruptedException, IOException{

		try{
			Actions action = new Actions (driver);
			driver.findElement(By.id("_btn_MENU1")).click();
			//code to click on system
			//OR.FindElement(driver, ORMap.get("MP_Land_SystemMenu")).click();
			Thread.sleep(500);

			//click on execute task
			//OR.FindElement(driver, ORMap.get("MP_Click_Execute_Task")).click();
			driver.findElement(By.xpath("//div[@id='_MENU1']/descendant::td[contains(.,'Execute Task')]")).click();
			Thread.sleep(500);
			//Click on Housekeeping
			action.moveToElement(driver.findElement(By.xpath("//div[@id='_MI81_SUBMENU']/table/tbody/tr[3]/td[2]"))).build().perform();
			//action.moveToElement (OR.FindElement(driver, ORMap.get("MP_Click_HouseKeeping_btn"))).build().perform();
			Thread.sleep(500);
			action.moveToElement(driver.findElement(By.xpath("//div[@id='_MI152_SUBMENU']//table//tbody//tr[@title='Group1']//td[4]"))).build().perform();
			//action.moveToElement (OR.FindElement(driver, ORMap.get("MP_Click_Group1_btn"))).build().perform();
			Thread.sleep(500);      
			action.moveToElement(driver.findElement(By.xpath("//div[@id='_MI153_SUBMENU']/table/tbody/tr[1]/td[2]"))).build().perform();
			//action.moveToElement (OR.FindElement(driver, ORMap.get("MP_Click_lump_sum"))).build().perform();
			action.release();
			Thread.sleep(200);
			driver.findElement(By.xpath("//div[@id='_MI153_SUBMENU']/table/tbody/tr[23]/td[2]")).click();
			//OR.FindElement(driver, ORMap.get("MP_Click_lump_sum")).click();
			Thread.sleep(1000);
			System.out.println();
			String MainWindowHandle = driver.getWindowHandle();
			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equals(MainWindowHandle)) {
					driver.switchTo().window(winHandle);
					break;
				}
			}
			//System.out.println(driver.getCurrentUrl());
			//OR.FindElement(driver, ORMap.get("MP_Click_Run_Task_button")).click();
			driver.findElement(By.xpath("(//button[@id='B_bt_1'])[1]")).click();
			driver.switchTo().window(MainWindowHandle);
			Thread.sleep(17000);
			driver.switchTo().frame("main");
			driver.switchTo().frame("fraTasks");
			//WebElement element= OR.FindElement(driver, ORMap.get("MP_Lump_element"));
			//String status = OR.FindElement(driver, ORMap.get("MP_Lump_status")).getText();
			WebElement element = driver.findElement(By.xpath("//table[@id='TableTask']//tr[3]/td[4]"));
			String status = driver.findElement(By.xpath("//table[@id='TableTask']//tr[3]/td[3]")).getText();
			try {
				if (element.isEnabled() && element.isDisplayed() && status.equals(task)) {
					((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
					//System.out.println(element.getText());
					//TestLogger.log(LogStatus.PASS,"Varify Lump sum MP Returns executed :"+status);
				} else {
					//System.out.println("fail");
					//TestLogger.log(LogStatus.FAIL,"Varified status of Lump sum task failed with the status:"+status);
				}
			} catch (StaleElementReferenceException e) {

			}
			driver.findElement(By.xpath("//table[@id='TableTask']//tr[3]/td[4]")).click();

			driver.switchTo().defaultContent();
			TestLogger.log(LogStatus.INFO,"Task: Lump sum MP Returns executed successfully with the status:"+status);

		}catch(Exception e){

			System.out.println(e.getMessage());
			TestLogger.log(LogStatus.FATAL,"Automation Error while performing Lump Sum MP Return task: Error Description - "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "FATAL";
		}
		return "Pass";
	}



}
