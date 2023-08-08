package model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

public class FileManager {

    String format;
    String regularExpresion;

    public FileManager(){
        format = "";
        regularExpresion= "";
    }


    public String readExtension(String fileRoute){
        Pattern check;
        check = Pattern.compile(".*\\.(xml|txt)");
        Matcher reconocerMatch;
        reconocerMatch = check.matcher(fileRoute);
        while (reconocerMatch.find()){
            format = reconocerMatch.group(1);
        }
        return format;
    }

    public String readXML(String fileRoute) throws Exception {
        try {
            File archivo = new File(fileRoute);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.parse(archivo);
            document.getDocumentElement().normalize();
            NodeList list = document.getElementsByTagName("expresiones");
            for (int temp = 0; temp < list.getLength(); temp++) {
                Node nodo = list.item(temp);
                if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) nodo;
                    regularExpresion = element.getElementsByTagName("forma").item(0).getTextContent();
                }
            }
        } catch (IOException | ParserConfigurationException | DOMException | SAXException e) {
            throw e;
        }
        return regularExpresion;
    }
    
    public void saveXML(String expression, String route) {
        org.jdom2.Element resolucion = new org.jdom2.Element ("resolucion");
        org.jdom2.Document doc = new org.jdom2.Document(resolucion);
        org.jdom2.Element expresiones = new org.jdom2.Element("expresiones");
        resolucion.addContent(expresiones);
        org.jdom2.Element expresion = new org.jdom2.Element("expresion");
        org.jdom2.Element forma = new org.jdom2.Element("forma");
        forma.setText(expression);


        expresion.addContent(forma);

        expresiones.addContent(expresion);

        XMLOutputter xml = new XMLOutputter();
        xml.setFormat(Format.getPrettyFormat());
        try {
            xml.output(doc,new FileWriter(route));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readTxt(String fileRoute) throws Exception {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(fileRoute));
            String temp="";
            String bfRead;
            while((bfRead = bf.readLine()) != null){
                temp = temp + bfRead;
            }
            regularExpresion = temp;
        } catch (IOException e) {
            throw e;
        }
        return regularExpresion;
    }
    
    public void saveTxt(String expression, String route) throws Exception {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(route)));
            writer.write(expression + "\n");
            writer.close();
        } catch(IOException e) {
            throw e;
        }
    }


}
