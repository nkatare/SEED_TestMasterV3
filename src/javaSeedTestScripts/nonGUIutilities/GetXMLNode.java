package javaSeedTestScripts.nonGUIutilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GetXMLNode {
	
	public static  Map<String, String> LocalMap = new HashMap<String, String>(2);
	
	public static Map<String,String> GetXMLNodeValue (String Tag, String filepath){
		 try {
			 	// Load the XML File
	            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	            Document doc = docBuilder.parse(filepath);
	            
	            LocalMap.put(Tag, "");
	            LocalMap.put("strFlag", "");
	            	            
	            // Logic to Update each node
	            if(Tag.contains("[")){
            		String[] TagSplit = Tag.split(Pattern.quote("["));
            		TagSplit[1]=TagSplit[1].replace("]", "");
            		NodeList UniqTag = (NodeList) doc.getElementsByTagName(TagSplit[0]);
            		LocalMap.replace(Tag, UniqTag.item(Integer.parseInt(TagSplit[1])).getTextContent());
            	}else{
            		NodeList UniqTag = (NodeList) doc.getElementsByTagName(Tag);
            		LocalMap.replace(Tag, UniqTag.item(0).getTextContent());
            	}
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	            LocalMap.replace("strFlag", "Fatal");
	            return LocalMap; 
	        } catch (SAXException e) {
				e.printStackTrace();
	            LocalMap.replace("strFlag", "Fatal");
	            return LocalMap; 
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
	            LocalMap.replace("strFlag", "Fatal");
	            return LocalMap; 
			}
		 LocalMap.replace("strFlag", "Pass");
		 return LocalMap;
	}

}
