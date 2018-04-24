package javaSeedTestScripts.nonGUIutilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javaSeed.constants.Const;
import javaSeedTestScripts.TestConsts;

public class DatabaseUtils {
	private static WebDriver driver = Const.driver;
	public static WebDriver  wait_driver ;
	public static WebDriverWait wait = new WebDriverWait(driver, 20);
	
	public static List<String> DBExtract(String queryString) {

		String GPPMPconnString = TestConsts.ENV_DBConnString;
		String GPPMPdbUsername = TestConsts.ENV_DBUserName;
		String GPPMPdbPassword = TestConsts.ENV_DBPassword;

		List<String> sqlOut = new ArrayList<String>();
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection conn = DriverManager.getConnection(GPPMPconnString, GPPMPdbUsername, GPPMPdbPassword);
			//System.out.println("connected");
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(queryString);
			while (rs.next()) {
				sqlOut.add(rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sqlOut;
	}

}
