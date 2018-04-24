package javaSeedTestScripts.nonGUIutilities;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Vector;

import javaSeed.constants.Const;
import javaSeedTestScripts.TestConsts;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class FileHandling {
	
	private ExtentTest TestLogger = Const.etTestCases;	
	
	public String FileDropViaSFTP(String fileName, String FolderName) throws InterruptedException {
		String[] UnixData = TestConsts.ENV_UNIX_HostPortUsernamePwd.split(":");
				
		String SFTPHOST = UnixData[0];
	    int SFTPPORT = Integer.parseInt(UnixData[1]);
	    String SFTPUSER = UnixData[2];
	    String SFTPPASS = UnixData[3];
	    String SFTPWORKINGDIR = null;
	    if(FolderName.toUpperCase().contains("PACS_IN")){
	    	SFTPWORKINGDIR = TestConsts.ENV_Unix_FilePath_Pacsin;
	    }else if(FolderName.toUpperCase().contains("PAIN_IN")){
	    	SFTPWORKINGDIR = TestConsts.ENV_Unix_FilePath_Painin;
	    }else{
	    	System.out.println("Unix Input Folder name was not defined correctly");
	    }
	    
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
            
            TestLogger.log(LogStatus.INFO,"Input file: "+fileName+" was dropped in the Unix box- "+SFTPHOST+" successfully at: "+SFTPWORKINGDIR);
            
            //log.info("File transfered successfully to host.");
        } catch (Exception ex) {
        	TestLogger.log(LogStatus.INFO,"Exception found while tranfering the File "+fileName+" to the Unix Box."+SFTPHOST+
        			" at location: "+SFTPWORKINGDIR+". Exception: "+ex.toString());
        	return "Fatal";
        }
        finally{
            channelSftp.exit();
            channel.disconnect();
            session.disconnect();
        }
		return "Pass";
	}


	public String FileCopyFromUnix(String FileName, String CopiedToFileName, String DBQuery) throws InterruptedException {
		String[] UnixData = TestConsts.ENV_UNIX_HostPortUsernamePwd.split(":");
				
		String SFTPHOST = UnixData[0];
	    int SFTPPORT = Integer.parseInt(UnixData[1]);
	    String SFTPUSER = UnixData[2];
	    String SFTPPASS = UnixData[3];
	    String SFTPWORKINGDIR = TestConsts.ENV_Unix_PacsOutFilePath;
		
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;
        
        try {
// Get the PacsOut file name from DB using Out internal file id
        	
        	String OutInternalFileID = TestConsts.SUPER_XML_FILE_DATAMAP.get(FileName).get("Out Internal File ID");
        	//String OutInternalFileID = "1811611264000102";
//    		Building the DB Query
    				String[] arrDBQ=DBQuery.split("<>");
    				String ActualDBQ=arrDBQ[0]+OutInternalFileID+arrDBQ[1];    				

    // DB Extract				
    				List<String> PacsOutFileList = DatabaseUtils.DBExtract(ActualDBQ);
    				if(PacsOutFileList.isEmpty()){
    					System.out.println("DB Extraction of Pacs Out File Path Failed for Out Internal File ID: "+OutInternalFileID);
    					   TestLogger.log(LogStatus.FATAL,"DB Extraction of Pacs Out File Path Failed for Out Internal File ID: "+OutInternalFileID);
    					   return "Fatal";
    				}
        	
        	String[] arrFullFilePathPacsOut = PacsOutFileList.get(0).split("/");
        	String PacsOutFileName= arrFullFilePathPacsOut[arrFullFilePathPacsOut.length-1];
        	
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
            channelSftp.cd(SFTPWORKINGDIR);

            Vector<ChannelSftp.LsEntry>	FileList = channelSftp.ls(PacsOutFileName);
            for(ChannelSftp.LsEntry listEntry : FileList){
            	channelSftp.get(listEntry.getFilename(), TestConsts.ENV_FWPATH+CopiedToFileName); 
            }

            TestLogger.log(LogStatus.INFO,"Output file: "+PacsOutFileName+" is copied to Test files location with name: "+TestConsts.ENV_FWPATH+FileName+"_PACS_OUT.xml");
            
            //log.info("File transfered successfully to host.");
        } catch (Exception ex) {
        	TestLogger.log(LogStatus.INFO,"Exception found while Copying the File from the Unix Box."+SFTPHOST+
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
