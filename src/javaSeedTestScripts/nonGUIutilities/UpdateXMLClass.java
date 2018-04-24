package javaSeedTestScripts.nonGUIutilities;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.regex.Pattern;

import javaSeed.constants.Const;
import javaSeedTestScripts.TestConsts;
import javaSeedTestScripts.nonGUIutilities.com.conXMLupdate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;


public class UpdateXMLClass {
	
	private ExtentTest TestLogger = Const.etTestCases;	
		
	public String UpdateXMLFile(String FileName,String MappedFileName, String TagsToUpdate,String TagsRefData) throws ParseException, ParserConfigurationException, TransformerException, SAXException, IOException {
		
	try{
		conXMLupdate ToUpdateXml = new conXMLupdate();
		HashMap<String, String> XML_file_Datamap = null;
		
		ToUpdateXml.setBusinessDate(TestConsts.ENV_GPPMP_BussDate);
		ToUpdateXml.setPacsFilePath(TestConsts.ENV_FWPATH+FileName);
		ToUpdateXml.setTagsToUpdate(TagsToUpdate);
		ToUpdateXml.setTagsRefData(TagsRefData);
		if(!(MappedFileName.contains(".xml"))){
			XML_file_Datamap = ToUpdateXml.BuildUpdateXMLmap(null,ToUpdateXml.getTagsToUpdate(),ToUpdateXml.getTagsRefData());
			//TestConsts.SUPER_XML_FILE_DATAMAP.put(FileName, XML_file_Datamap);			
		}else {
			XML_file_Datamap = ToUpdateXml.BuildUpdateXMLmap(TestConsts.SUPER_XML_FILE_DATAMAP.get(MappedFileName),ToUpdateXml.getTagsToUpdate(),ToUpdateXml.getTagsRefData());
			//TestConsts.SUPER_XML_FILE_DATAMAP.put(FileName, XML_file_Datamap);	
		}
		ToUpdateXml.setUpdateDataMap(XML_file_Datamap);
		ToUpdateXml.UpdateXMLwithXMLMap(ToUpdateXml, ToUpdateXml.getPacsFilePath());
				
		TestLogger.log(LogStatus.INFO,"XML File: "+FileName+" updated successfully");

	}catch(Exception e){
		e.printStackTrace();
		// Logger 
			TestLogger.log(LogStatus.FATAL,"Update XML File: "+FileName+" has errors");
		return "Fatal";
	}	
		return "Pass";
	}

		
	
	public String ReadXMLFile (String FileName,String TagsToRead) throws ParseException, ParserConfigurationException, TransformerException, IOException{
        
			String[] arTagsToRead = TagsToRead.split("~");      
			HashMap<String, String> XML_file_Datamap = new HashMap<String, String> ();
		try {
		 	// Load the XML File
	        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	        Document doc;

			doc = docBuilder.parse(TestConsts.ENV_FWPATH+FileName);
			
			for (int a=0;a<arTagsToRead.length;a++){
				if(arTagsToRead[a].contains("[")){
	        		String[] TagSplit = arTagsToRead[a].split(Pattern.quote("["));
	        		TagSplit[1]=TagSplit[1].replace("]", "");
	        		NodeList UniqTag = (NodeList) doc.getElementsByTagName(TagSplit[0]);
	        		XML_file_Datamap.put(arTagsToRead[a], UniqTag.item(Integer.parseInt(TagSplit[1])).getTextContent());
	        	}else{
            		NodeList UniqTag = (NodeList) doc.getElementsByTagName(arTagsToRead[a]);
            		XML_file_Datamap.put(arTagsToRead[a],UniqTag.item(0).getTextContent());
            	}
			}
			
			TestConsts.SUPER_XML_FILE_DATAMAP.put(FileName, XML_file_Datamap);	
			
		} catch (SAXException e) {
			e.printStackTrace();
			return "Fatal";
		}
		return "Pass";
	}
	
}
