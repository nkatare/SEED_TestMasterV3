package javaSeedTestScripts.GPPMP;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import javaSeed.constants.Const;
import javaSeed.objectRepository.OR;
import javaSeed.utils.Screenshot;
import javaSeedTestScripts.TestConsts;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class A_LoginPage {

	private WebDriver driver = Const.driver;
	public WebDriver driver1 ;
	private HashMap<String, String[]> ORMap = Const.ORMap;
	private ExtentTest TestLogger = Const.etTestCases;

	//------------------------ Imported Variables Collection
	String TestAppURL 		= TestConsts.ENV_GPPMP_URL;
	String strNewdate       = TestConsts.ENV_GPPMP_BussDate;
	String TestAppUser 		= TestConsts.ENV_GPPMP_UserName1;
	String TestAppUserPwd	= TestConsts.ENV_GPPMP_UserName1_Pwd;
	String TestAppUser2 	= TestConsts.ENV_GPPMP_UserName2;
	String TestAppUserPwd2	= TestConsts.ENV_GPPMP_UserName2_Pwd;


	/*       ###########################################################################################
    Name                      : NavigateLoginToTestApp()
    Description        : Navigate to the Test Application and Login to the Application
    Developed                 : Nikhil Katare 01/12/2017
    
    Input Parameters          : 1. String UserKey: Key that reflects which user to login with parameterized from Env Sheet
    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test
    
    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    Subrat                          10/01/2018                 Changed the input parameter to UserKey. Enable login with different users.
    ############################################################################################*/

	public String NavigateLoginToTestApp(String user1) throws IOException{

		// Driver Wait object to wait until 10 Seconds for the Web Element to becomes visible.
		WebDriverWait wait = new WebDriverWait(driver, 10);

		try{
			String mainHandle=null;
			new WebDriverWait(driver, 10);
			//driver= Const.driver;
			try{
				mainHandle=driver.getWindowHandle();
			}catch(Exception e){
				if(e.getMessage().contains("Window is closed")){
					driver= new InternetExplorerDriver();
					mainHandle=driver.getWindowHandle();
					Const.driver=driver;
				}
			}
			// Get the GPPMP-URL
			driver.get(TestAppURL);
			Thread.sleep(2000);

			//handle the multiple windows
			Set<String> allHandles = driver.getWindowHandles();
			for(String currentHandle : allHandles) {
				if (!currentHandle.equals(mainHandle)) {
					driver.switchTo().window(currentHandle);
					break;
				}
			}
			if(OR.FindElement(driver, ORMap.get("MP_Log_UserNameTxtBx")).isDisplayed()){
				TestLogger.log(LogStatus.INFO,"Navigated to GPPMP. URL: "+TestAppURL);
			}else{
				TestLogger.log(LogStatus.FATAL,"Navigation to GPPMP Failed. URL: "+TestAppURL);
				return "Fatal";
			}

			wait.until(ExpectedConditions.visibilityOf(OR.FindElement(driver, ORMap.get("MP_Log_UserNameTxtBx"))));

			if(user1.equalsIgnoreCase("user1")){

				OR.FindElement(driver, ORMap.get("MP_Log_UserNameTxtBx")).sendKeys(TestAppUser);
				OR.FindElement(driver, ORMap.get("MP_Log_PwdTxtBx")).sendKeys(TestAppUserPwd);
				OR.FindElement(driver, ORMap.get("MP_Log_SubmitBn")).click();
			}
			else if(user1.equalsIgnoreCase("user2")){

				OR.FindElement(driver, ORMap.get("MP_Log_UserNameTxtBx")).sendKeys(TestAppUser2);
				OR.FindElement(driver, ORMap.get("MP_Log_PwdTxtBx")).sendKeys(TestAppUserPwd2);
				OR.FindElement(driver, ORMap.get("MP_Log_SubmitBn")).click();

			}

			Thread.sleep(5000);
			if(OR.FindElements(driver, ORMap.get("MP_Signin_Error")).size()>0){
				TestLogger.log(LogStatus.FATAL,"Login Failed for user: "+TestAppUser+". Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
				return "Fatal";
			} else if(OR.FindElements(driver, ORMap.get("MP_Land_SystemMenu")).size()>0){
				TestLogger.log(LogStatus.INFO,"Login was successful for user: "+TestAppUser);
			}

		} catch(Exception e){
			e.printStackTrace();
			// Logger 
			TestLogger.log(LogStatus.FATAL,"Automation Error on Navigate and Login to GPPSP: Error Description - "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	
	/*       ###########################################################################################
    Name                      : LoginToTestApp()
    Description        		  : Navigate to the Test Application and LoginToTestApp to the Application
    Developed                 : Nikhil Katare 01/12/2017
    
    Input Parameters          : None
    Returns                   : 1. String Flag: Make sure if the method was executed successfully and otherwise return Fatal to terminate the test
    
    U P D A T E S
    --------------------------------------------------------------------------------------------
    Author                          Date                       Description
    --------------------------------------------------------------------------------------------
    None
    ############################################################################################*/
	public String LoginToTestApp() throws IOException{

		try{

			// Driver Wait object to wait until 10 Seconds for the Web Element to becomes visible.
			WebDriverWait wait = new WebDriverWait(driver, 10);

			wait.until(ExpectedConditions.visibilityOf(OR.FindElement(driver, ORMap.get("MP_Log_UserNameTxtBx"))));


			OR.FindElement(driver, ORMap.get("MP_Log_UserNameTxtBx")).sendKeys(TestAppUser);
			OR.FindElement(driver, ORMap.get("MP_Log_PwdTxtBx")).sendKeys(TestAppUserPwd);
			OR.FindElement(driver, ORMap.get("MP_Log_SubmitBn")).click();

			Thread.sleep(2000);
			if(OR.FindElements(driver, ORMap.get("MP_Signin_Error")).size()>0){
				TestLogger.log(LogStatus.FATAL,"Login Failed for user: "+TestAppUser+". Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
				return "Fatal";
			} else if(OR.FindElements(driver, ORMap.get("MP_Land_SystemMenu")).size()>0){
				TestLogger.log(LogStatus.INFO,"Login was successful for user: "+TestAppUser);
			}

		} catch(Exception e){
			e.printStackTrace();
			// Logger 
			TestLogger.log(LogStatus.FATAL,"Login to GPPMP had Errors: Error Description - "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}
		return "Pass";
	}
	

}
