package cms.model;

import java.io.BufferedReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import cms.model.communication.format.Request;
import cms.model.communication.format.Response;
import cms.model.communication.format.Transmission;

public class XMLParser {

	private static Document doc;
	private static DocumentBuilderFactory dbFactory;
	private static DocumentBuilder dBuilder;

	private static NodeList particles;
	private static Element element;

	private static Transmission transmission;

	public XMLParser() {
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Transmission makedoc(BufferedReader input) {

		try {
			doc = dBuilder.parse(new InputSource(input));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			particles = doc.getElementsByTagName("particle");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			
			element = (Element) doc.getElementsByTagName("request").item(0);
			if(element != null){
				transmission = new Request();
				return construct(element, transmission);
			} 
			
			element = (Element) doc.getElementsByTagName("response").item(0);
			if(element != null){
				transmission = new Response();
				return construct(element, transmission);
			} 


		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;

	}
	
	private static Transmission construct(Element element, Transmission transmission){
		String[] tags = new String[5];
		
		tags[0] = element.getTextContent();
		tags[1] = element.getAttribute("id");
		tags[2] = element.getAttribute("type");
		tags[3] = element.getAttribute("to");
		tags[4] = element.getAttribute("from");
		
		transmission.addAttributes(tags[1], tags[2], tags[3], tags[4]);
		transmission.addOpcode(tags[0]);
		transmission.addParticles(particles);
		
		return transmission;
	}
}