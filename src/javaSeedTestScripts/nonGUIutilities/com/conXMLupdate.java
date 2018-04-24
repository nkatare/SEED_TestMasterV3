package javaSeedTestScripts.nonGUIutilities.com;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class conXMLupdate {
	
	private String PacsFilePath;
	private String TagsToUpdate;
	private String TagsRefData;
	private Map <String, String> mapXMLUpdateData;
	private Date BusinessDate;
	
	
	public Date getBusinessDate() {
		return BusinessDate;
	}

	public void setBusinessDate(String BusinessDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		try {
			this.BusinessDate = sdf.parse(BusinessDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public String getPacsFilePath() {
		return PacsFilePath;
	}

	public void setPacsFilePath(String PacsFilePath) {
		this.PacsFilePath = PacsFilePath;
	}

	public String getTagsToUpdate() {
		return TagsToUpdate;
	}

	public void setTagsToUpdate(String tagsToUpdate) {
		TagsToUpdate = tagsToUpdate;
	}

	public String getTagsRefData() {
		return TagsRefData;
	}

	public void setTagsRefData(String tagsRefData) {
		TagsRefData = tagsRefData;
	}

	public void setUpdateDataMap(Map <String, String> mapPacsUpdateData) {
			this.mapXMLUpdateData = mapPacsUpdateData;		
	}

	public Map<String, String> getUpdateDataMap() {	
			return mapXMLUpdateData;
	}
	
	/*Function to Build a Map with Key as Tag Name[index] and Value with either Unique ID or Date
	Prerequisite:1. These Attributes should be set: PacsFilePath, TagsToUpdate, TagsRefData and BusinessDate
		 		2. Tags to be updated String should be in line with Tags Reference Data String
	Logic: Split Tags String to Individual Tags and Split respective Tag reference String. Both the Split array should be equal in size to proceed
	Unique Tags Values will be picked and updated using <[UNIQUE]> keyword. Date Tag values will be updated using <[DATE]> keyword and D-1/D+2 will
	get the corresponding date from D (Current Business Date). MORE Keywords will be added as and when required.
	Developed by : Nikhil Katare for GPPMP
	Date: 08/11/2017*/
	public HashMap <String, String> BuildUpdateXMLmap (Map<String, String> DataMap,String TagsToUpdate,String TagsRefData) throws ParseException, ParserConfigurationException, TransformerException, IOException{
        
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar c = Calendar.getInstance();
        
        Date DT = conXMLupdate.this.getBusinessDate();


      String[] arTagsToUpdate = TagsToUpdate.split("~");
      String[] arTagsRefData = TagsRefData.split("~");
      
      HashMap <String, String> XMLDataMap = new HashMap <String, String> (arTagsToUpdate.length+1);

	      if(arTagsToUpdate.length == arTagsRefData.length){
			for (int a=0;a<arTagsToUpdate.length;a++){
				if(arTagsRefData[a].contains("[UNIQUE]")){
					String[] SplitArr = arTagsRefData[a].split(Pattern.quote("["));
			        DateFormat df = new SimpleDateFormat("ddHHmmss");
			        Date RandonNumber = Calendar.getInstance().getTime();
					String DynamicString = SplitArr[0]+df.format(RandonNumber);
					XMLDataMap.put(arTagsToUpdate[a], DynamicString);        			
				} else if(arTagsRefData[a].contains("[DATE]")){
					String[] SplitArr = arTagsRefData[a].split(Pattern.quote("["));
			        c.setTime(DT);
			        c.add(Calendar.DATE, Integer.parseInt(SplitArr[0].substring(1)));
			        sdf.format(c.getTime());
			        XMLDataMap.put(arTagsToUpdate[a], sdf.format(c.getTime()));
				} else if(arTagsRefData[a].contains("[DATE-TIME]")){
					String[] SplitArr = arTagsRefData[a].split(Pattern.quote("["));
			        c.setTime(DT);
			        c.add(Calendar.DATE, Integer.parseInt(SplitArr[0].substring(1)));
			        sdtf.format(c.getTime());
			        XMLDataMap.put(arTagsToUpdate[a], sdtf.format(c.getTime()));   
				} else if(arTagsRefData[a].contains("[KEY]")){
					String actualKey = arTagsRefData[a].replace("[KEY]", "");
					XMLDataMap.put(arTagsToUpdate[a], DataMap.get(actualKey));
				}
			}
	  	} else {
	  		System.out.println("BuildUpdateXMLmap Method Prerequisite was not Met!!");
	  	}
		return XMLDataMap;
	}
	
	/*Function to Build a Map with Key as Tag Name[index] and Value with either Unique ID or Date
	Prerequisite:1. These Attributes should be set: PacsFilePath, TagsToUpdate, TagsRefData and BusinessDate
				2. Tags to be updated String should be in line with Tags Reference Data String
	Logic: Split Tags String to Individual Tags and Split respective Tag reference String. Both the Split array should be equal in size to proceed
		Unique Tags Values will be picked and updated using <[UNIQUE]> keyword. Date Tag values will be updated using <[DATE]> keyword and D-1/D+2 will
		get the corresponding date from D (Current Business Date). MORE Keywords will be added as and when required.
	Developed by : Nikhil Katare for GPPMP
	Date: 08/11/2017*/
	
	public void UpdateXMLwithXMLMap(conXMLupdate XMLObj, String filepath){
		 try {
			 	// Load the XML File
	            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	            Document doc = docBuilder.parse(filepath);
	            
	            Map<String, String> LocalMap = XMLObj.getUpdateDataMap();
	            
	            // Logic to Update each node
	            for (Map.Entry<String, String> entry : LocalMap.entrySet()){
	            	if(entry.getKey().contains("[")){
	            		String[] TagSplit = entry.getKey().split(Pattern.quote("["));
	            		TagSplit[1]=TagSplit[1].replace("]", "");
	            		NodeList UniqTag = (NodeList) doc.getElementsByTagName(TagSplit[0]);
	            		UniqTag.item(Integer.parseInt(TagSplit[1])).setTextContent(entry.getValue());
	            	}else{
	            		NodeList UniqTag = (NodeList) doc.getElementsByTagName(entry.getKey());
	            		UniqTag.item(0).setTextContent(entry.getValue());
	            	}
	            }
	            
	            // write the content into xml file
	            TransformerFactory transformerFactory = TransformerFactory
	                    .newInstance();
	            Transformer transformer = transformerFactory.newTransformer();
	            DOMSource source = new DOMSource(doc);
	            StreamResult result = new StreamResult(new File(filepath));
	            transformer.transform(source, result);
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (TransformerException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
	}
	
	
}

