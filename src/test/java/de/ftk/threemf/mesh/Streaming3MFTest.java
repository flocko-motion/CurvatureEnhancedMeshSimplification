package de.ftk.threemf.mesh;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.w3c.dom.NamedNodeMap;


import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;


@Slf4j
public class Streaming3MFTest {

    /**
     * streaming with xml event reader
     */
    @Test
    public void stream3MFModelTest() throws XMLStreamException, FileNotFoundException {
        String model;

        model = "bigmodel_mesh2";
        // model = "ballbox";
        // model = "cube";
        // model = "tetra";

        File fileInput = new File("testdata/" + model + ".model");
        InputStream inputStream = new FileInputStream(fileInput);
        File fileOutput = new File("testoutput/3D/3dmodel.model");
        OutputStream outputStream = new FileOutputStream(fileOutput);

        Lib3MF.processStream(new Simplifier(), inputStream, outputStream);

        /* self closing tags
        Reader xml = new StringReader("<?xml version=\"1.0\"?><foo></foo>");
TransformerFactory transFactory = TransformerFactory.newInstance();
Transformer transformer = transFactory.newTransformer();
transformer.transform(new StreamSource(xml),
    new StreamResult(System.out));
         */

    }




}
