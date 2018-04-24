package javaSeedTestScripts;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import javaSeed.constants.Const;
import javaSeed.objectRepository.OR;
import javaSeed.utils.Screenshot;
import org.openqa.selenium.JavascriptExecutor;

public class GenericMethods {
	private static WebDriver driver = Const.driver;
	private ExtentTest TestLogger = Const.etTestCases;	
	private static HashMap<String, String[]> ORMap = Const.ORMap;
	public static WebDriverWait wait = new WebDriverWait(driver, 20);
	
	public String highlightElement() throws InterruptedException {
		
		driver.findElement(By.id("_btn_MENU1")).click();
		
		WebElement ele = null;
		ele = OR.FindElement(driver, ORMap.get("MP_Land_Configuration"));
		ele.click();
		
/*        for (int i = 0; i <2; i++) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].setAttribute('style', arguments[1]);", ele, "color: yellow; border: 6px solid red;");
            Thread.sleep(1000);
            js.executeScript("arguments[0].setAttribute('style', arguments[1]);", ele, "");
            }*/
		return "Pass";
        }
	//taking screenshot...
	public String TakeScreenshot(String Message) throws IOException{
		try{
			driver.switchTo().defaultContent();
			TestLogger.log(LogStatus.INFO, "Screenshot for: "+Message+"."+Screenshot.ObjectSnapFullPage(driver));
			
		}catch(Exception e){
			e.printStackTrace();
			TestLogger.log(LogStatus.FATAL,"Login to GPPMP had Errors: Error Description - "+e.toString()+ " Screenshot - "+Screenshot.ObjectSnapFullPage(driver));
			return "Fatal";
		}		
		return "Pass";
	}
	//sleep...
	public void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {


			TestLogger.log(null, "Sleep interrupted", e);
		}
	}			
//click element by any locators...
	public void click(By Locator) {
		findVisible(Locator).click();
	}

	public WebElement findVisible(By locator) {
		for (int i = 0, attempts = 10; i < attempts; i++) {
			try {
				List<WebElement> elements = driver.findElements(locator);
				for (WebElement element : elements) {
					if (element.isDisplayed()) {
						return element;
					}
				}
				sleep(1000);
			} catch (Exception e) {
				TestLogger.log(null, "Could not find element on page: " + locator + e);
				sleep(1000);
			}
		}
		throw new IllegalStateException("Could not find element: " + locator);

	}
	//click element by id...
	public WebElement findVisible(String id) {
		for (int i = 0, attempts = 20; i < attempts; i++) {
			try {
				By locator = By.id(id);
				List<WebElement> elements = driver.findElements(locator);
				for (WebElement element : elements) {
					if (element.isDisplayed()) {
						element.click();
					}
				}
				
			} catch (Exception e) {
				
				
			}
		}
		throw new IllegalStateException("Could not find element: " + id);

	}
//selecting dropdown by xpath...
	public void selectValue(String xpat, String value)  throws Exception
	{
		try{

			wait = new WebDriverWait(driver, 20);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpat)));
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpat)));
			Select select = new Select(driver.findElement(By.xpath(xpat)));
			int index = 0;
			for (WebElement option : select.getOptions()) {

				if (option.getText().equalsIgnoreCase(value))
					break;
				index++;
			}
			select.selectByIndex(index);      

		}catch (NoSuchSessionException e) {

		}catch(Exception e){System.out.println(e.getMessage());}
	}
//sendkeys by xpath/name/id...	
	public static String setText(String xpat, String value) throws Exception
	{
		try{

			wait = new WebDriverWait(driver, 20);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpat)));
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpat)));
			driver.findElement(By.xpath(xpat)).clear();
			driver.findElement(By.xpath(xpat)).sendKeys(value);

		}catch (UnreachableBrowserException e) {

		}catch (NoSuchSessionException e) {
			 
			return "FATAL";
		}catch(Exception e){System.out.println(e.getMessage());}
		return "Pass";
	}
//logging out from app..
	public void tearDown() throws Exception{

		try
		{
			if(!(driver==null))
			{
				System.out.println("In tearDown() !!!");

				click(By.xpath("//tr[@id='_MI463']/td[contains(.,'Exit')]"));

			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
//get text/string by name/ID/xpath...
	public static String getTextByName(String Name) throws Exception
	{
		try{

			wait = new WebDriverWait(driver, 20);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.name(Name)));
			wait.until(ExpectedConditions.elementToBeClickable(By.name(Name)));
			return driver.findElement(By.name(Name)).getText();
		}catch (UnreachableBrowserException e) {
			System.out.println("Exception occured "+ Name); return "";
		}
	}
// click element by ID...
	public static void clickElementByID(String ID) throws Exception
	{

		wait = new WebDriverWait(driver, 20);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id(ID)));
		wait.until(ExpectedConditions.elementToBeClickable(By.id(ID)));
		driver.manage().timeouts().pageLoadTimeout(600, TimeUnit.SECONDS);
		try{
			driver.findElement(By.id(ID)).click();

		}catch(Exception e)
		{System.out.println("Exception occured at clickElementByID  "+ ID);}
		
	}
	//click element by xpath...
	public static void clickElementByXpath(String xpat) throws Exception
	{

		wait = new WebDriverWait(driver, 20);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpat)));
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpat)));
		driver.manage().timeouts().pageLoadTimeout(600, TimeUnit.SECONDS);
		try{
			driver.findElement(By.xpath(xpat)).click();

		}catch(Exception e)
		{System.out.println("Exception occured at clickElementByID  "+ xpat);}
		
	}
	//java script executor...
	public static boolean JavaScriptClick(WebElement element) throws Exception {

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
	//get current time..
	public static String getTimeStamp()
	{
		Format formatter = new SimpleDateFormat("HH:mm:ss");
		Date today = Calendar.getInstance().getTime();        
		String formattedDate = formatter.format(today);

		return formattedDate;
	}
	//get past time..
	public static String getPasTime()
	{
		
		Format formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
	    calendar.getTime();
	    calendar.add(Calendar.MINUTE, -5);
	    
	    String formattedTime = formatter.format(calendar.getTime());

		return formattedTime;
	}
	//select options..
	public static void selectOptionByText(WebElement objProperty, String optionToSelect) {

		String status = "false";

		Select select = new Select(objProperty);

		List<WebElement> options = select.getOptions();

		int optionsCount = options.size();

		if (optionsCount > 0) {

			for (WebElement option : options) {

				if (option.getText().equals(optionToSelect)) {

					select.selectByVisibleText(optionToSelect);
					
					status = "true";
					break;
				}
			}
			if (!(status == "true")) {
				
			}
		} else {
			
		}
	}
	
}
