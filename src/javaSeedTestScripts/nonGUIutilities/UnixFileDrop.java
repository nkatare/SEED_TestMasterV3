package javaSeedTestScripts.nonGUIutilities;

import java.io.File;
import java.io.FileInputStream;

import javaSeed.constants.Const;
import javaSeedTestScripts.TestConsts;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class UnixFileDrop {
	
	private ExtentTest TestLogger = Const.etTestCases;	
	
	public String FileDropViaSFTP(String fileName) throws InterruptedException {
		
		String[] UnixData = TestConsts.ENV_UNIX_HostPortUsernamePwd.split(":");
				
		String SFTPHOST = UnixData[0];
	    int SFTPPORT = Integer.parseInt(UnixData[1]);
	    String SFTPUSER = UnixData[2];
	    String SFTPPASS = UnixData[3];
	    String SFTPWORKINGDIR = TestConsts.ENV_Unix_FilePath_Pacsin;
		
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
            session.setPassword(SFTPPASS);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            
            File f = new File(TestConsts.ENV_FWPATH+fileName);
            channelSftp.cd(SFTPWORKINGDIR);
            channelSftp.put(new FileInputStream(f),f.getName(),channelSftp.OVERWRITE);
            channelSftp.chmod(0777,SFTPWORKINGDIR+"/"+fileName);
            
            TestLogger.log(LogStatus.INFO,"Input file: "+fileName+" was dropped in the Unix box successfully at: "+SFTPHOST);
            
            //log.info("File transfered successfully to host.");
        } catch (Exception ex) {
        	TestLogger.log(LogStatus.INFO,"Exception found while tranfering the File to the Unix Box."+SFTPHOST+
        			". Exception: "+ex.toString());
        	return "Fatal";
        }
        finally{
            channelSftp.exit();
            channel.disconnect();
            session.disconnect();
        }
		return "Pass";
	}
}
