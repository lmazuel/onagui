/**
 * 
 */
package fr.onagui.config;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * @author mazman
 *
 */
public class OnaguiConfigImpl implements OnaguiConfiguration {

	/* dom part */
	private Document dom = null;

	/* score color configuration */
	private ScoreColorConfiguration scc = null;

	/* model */
	private File ontologyLastOpenDirectory = null;
	private Collection<File> lastOpenOntology = null;
	private Collection<File> lastOpenAlignment = null;

	public OnaguiConfigImpl(File configfile){
		if(!configfile.exists()) {
			System.out.println("Cannot find configuration file, loading default configuration.");
		}
		else {
			System.out.println("Found the configuration file: "+configfile.getAbsolutePath());
			//get the factory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			try {

				//Using factory get an instance of document builder
				DocumentBuilder db = dbf.newDocumentBuilder();

				//parse using builder to get DOM representation of the XML file
				dom = db.parse(configfile);	
				Element root = dom.getDocumentElement();

				XPathFactory factory = XPathFactory.newInstance();

				// Loading last open directory
				XPath xpath = factory.newXPath();
				XPathExpression expr = xpath.compile("//OntologyLastOpenDirectory");
				Object result = expr.evaluate(root, XPathConstants.STRING);
				ontologyLastOpenDirectory = new File(((String)result).trim());

			}catch(ParserConfigurationException pce) {
				pce.printStackTrace();
			}catch(SAXException se) {
				se.printStackTrace();
			}catch(IOException ioe) {
				ioe.printStackTrace();
			}catch(XPathExpressionException e) {
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see fr.onagui.config.OnaguiConfiguration#getOntologyLastOpenDirectory()
	 */
	@Override
	public File getOntologyLastOpenDirectory() {
		return ontologyLastOpenDirectory;
	}
	
	public void setOntologyLastOpenDirectory(File path) {
		ontologyLastOpenDirectory = path;
	}
	
	public void addLastOpenOntology(File file) {
		lastOpenOntology.add(file);
	}

	public void addLastOpenAlignment(File file) {
		lastOpenAlignment.add(file);
	}
	
	/* (non-Javadoc)
	 * @see fr.onagui.config.OnaguiConfiguration#getScoreColorConfiguration()
	 */
	@Override
	public ScoreColorConfiguration getScoreColorConfiguration() {
		if(scc == null) {
			if(dom == null) {
				scc = new FixedColorManager();
			}
			else {
				Element root = dom.getDocumentElement();

				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				try {
					XPathExpression expr = xpath.compile("//ScoreColorStrategy");
					Object result = expr.evaluate(root, XPathConstants.NODESET);
					NodeList resultList = (NodeList)result;
					for(int j=0; j<resultList.getLength(); j++) {
						Node currentNode = resultList.item(j);
						NamedNodeMap attrs = currentNode.getAttributes();
						Attr activated = (Attr)attrs.getNamedItem("activated");
						Attr name = (Attr)attrs.getNamedItem("name");
						if(activated != null && activated.getValue().equals("true")) {
							System.out.println("Found ScoreColorStrategy in config file, loading: "+name.getValue());
							scc = new FixedColorManager(currentNode);
							break;
						}
					}

				} catch (Exception e) {
					System.err.println("Loading config file launch an error while parsing ScoreColorStrategy.");
					System.err.println("I will use FixedColorManager default value");
				}
				// Fall back to something I know
				if(scc == null) {
					scc = new FixedColorManager();
				}
			}
		}
		return scc;
	}

}
