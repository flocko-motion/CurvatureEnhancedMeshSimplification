package de.ftk.threemf.mesh;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class IoFile3MF {

    public static Mesh readMesh(File file) throws IOException {
        try
        {
            //an instance of factory that gives a document builder
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //an instance of builder to parse the specified xml file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

            // idea: make flux from nodelist and go through those..
            NodeList vertices = doc.getElementsByTagName("vertices");
            NodeList triangles = doc.getElementsByTagName("triangles");
            // nodeList is not iterable, so we are using for loop
            for (int i = 0; i < vertices.getLength(); i++)
            {
                Node node = vertices.item(i);

                System.out.println("\nNode Name : " + node.getNodeName());
                /*
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element eElement = (Element) node;
                    System.out.println("Student id: "+ eElement.getElementsByTagName("id").item(0).getTextContent());
                    System.out.println("First Name: "+ eElement.getElementsByTagName("firstname").item(0).getTextContent());
                    System.out.println("Last Name: "+ eElement.getElementsByTagName("lastname").item(0).getTextContent());
                    System.out.println("Subject: "+ eElement.getElementsByTagName("subject").item(0).getTextContent());
                    System.out.println("Marks: "+ eElement.getElementsByTagName("marks").item(0).getTextContent());
                }

                 */
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // TODO: create mesh
        return null;
    }
}
