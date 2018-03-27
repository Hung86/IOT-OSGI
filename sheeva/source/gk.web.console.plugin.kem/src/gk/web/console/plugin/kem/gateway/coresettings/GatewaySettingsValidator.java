package gk.web.console.plugin.kem.gateway.coresettings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GatewaySettingsValidator {

	private Properties props;
	private HashMap<String, String> _errorTable;
	private File _errorMessages;
	private File _verifier;
	
	public GatewaySettingsValidator(Properties props) {
		this.props = props;
		_verifier = null;
		_errorMessages = null;
		_errorTable = null;
	}
	
	public void setCheckingValidate(File errorMessages, File verifier) {
		_errorMessages = errorMessages;
		_verifier = verifier;
	}

	public  ArrayList<String> validate() {
		ArrayList<String> settingErrors = new ArrayList<String>();
		try {
			loadErrorTable();
			checkPropertyValidate(settingErrors);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return settingErrors;
	}
	
	private void loadErrorTable(){
		if (_errorTable != null) {
			return;
		}
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Document doc = null;
		if (dBuilder != null) {
			try {
				doc = dBuilder.parse(_errorMessages);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (doc != null) {
			doc.getDocumentElement().normalize();
			_errorTable = new HashMap<String, String>();
			Element firstNode = doc.getDocumentElement();
			NodeList childList = firstNode.getChildNodes();
			for (int i = 0; i < childList.getLength(); i++) {
				Node child = childList.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					Element item = (Element) child;
					_errorTable.put(item.getNodeName(), item.getTextContent());
				}
			}
		}
	}
	
	private  void checkPropertyValidate(ArrayList<String> errorsList) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Document doc = null;
		if (dBuilder != null) {
			try {
				doc = dBuilder.parse(_verifier);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (doc != null) {
			doc.getDocumentElement().normalize();
			Set<String> item_adapter = props.stringPropertyNames();
			Iterator<String> it = item_adapter.iterator();
			while (it.hasNext()) {
				String temp = it.next();
				NodeList childList = null;
				ArrayList<String> optList = new ArrayList<String>();
				ArrayList<String> argList = new ArrayList<String>();

				childList = doc.getElementsByTagName(temp);

				if ((childList == null) || (childList.getLength() == 0)) {
					// errorsList.add(getErrorMeaning("unknown") + temp);
					continue;
				}

				parseOptArg(/* in */childList,/* out */optList, /* out */argList);

				processingOptArg(/* in */optList,/* in */argList, /* out */
						errorsList, temp);
			}
		}
	}
	
	private void processingOptArg(ArrayList<String> optList, ArrayList<String> argList, ArrayList<String> errorsList, String propName){
		////Check operators and arguments
		String val_temp = props.getProperty(propName);
		for (int i = 0; i < optList.size(); i++){
			if(optList.get(i).equalsIgnoreCase("match")) {
				
				Pattern matchString = Pattern.compile(argList.get(i));
				Matcher m = matchString.matcher(val_temp);
				//System.out.println("---fix---val_temp="+val_temp + " - matchString = " + matchString.toString());
				if ( !m.matches() && !m.find()) {
					errorsList.add(getErrorMeaning(propName, propName));
				}
			} else if(optList.get(i).equalsIgnoreCase("greater_equal")) {
				try {
					Integer num_left = Integer.parseInt(val_temp);
					Integer num_right = Integer.parseInt(argList.get(i));
					//System.out.println("--------------greater : num_left 2= " +num_left + " - num_righ="+num_right);
					if (num_left < num_right){
						errorsList.add(getErrorMeaning(propName, propName));
					}
				} catch (NumberFormatException nfe) {
					errorsList.add(getErrorMeaning("exception", propName));
				}
			}  else if(optList.get(i).equalsIgnoreCase("less_equal")) {
				try {
					Integer num_left = Integer.parseInt(val_temp);
					Integer num_right = Integer.parseInt(argList.get(i));
					//System.out.println("--------------less : num_left = " +num_left + " - num_righ="+num_right);
					if (num_left > num_right){
						errorsList.add(getErrorMeaning(propName, propName));
					}
				} catch (NumberFormatException nfe) {
					errorsList.add(getErrorMeaning("exception", propName));
				}
			} 
		}
		
	}
	
	private void parseOptArg(NodeList nodeList , ArrayList<String> optList, ArrayList<String> argList) {
		// hungkt will improve for many devices in future
		NodeList cList = nodeList.item(0).getChildNodes();
		for (int i = 0; i <cList.getLength(); i++ ) {
			Node item = (Node) cList.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				//System.out.println("---------------parseOptArg = " +item.getTextContent() );
				if (item.getNodeName().equals("op")) {
					optList.add(item.getTextContent());
				} else if (item.getNodeName().equals("arg")){
					argList.add(item.getTextContent());
				}
			}
		}
	}
	
	private String getErrorMeaning(String key, String propName){
		String meaning = _errorTable.get(key);
		if (meaning == null){
			return propName;
		} 
		return meaning;
	}

}
