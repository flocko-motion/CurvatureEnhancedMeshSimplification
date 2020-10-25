package de.ftk.threemf.mesh;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.javatuples.Pair;

import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;


@Slf4j
public class Lib3MF {

    public static void processStream(
            Simplifier simplifier,
            InputStream inputStream,
            OutputStream outputStream
    ) throws XMLStreamException {
        long start = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        log.info("process stream.. "
                + " " + SystemWatch.heapStatus());

        XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(inputStream);
        XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(outputStream);
        int meshCount = 0;
        long trianglesBefore = 0;
        long trianglesAfter = 0;
        while (reader.hasNext()) {
            XMLEvent xmlEvent = reader.peek();
            if (isStartNode(xmlEvent, "mesh")) {
                meshCount++;
                log.info("Mesh #" + meshCount + " @" + formatTime(System.currentTimeMillis() - start));
                //if(meshCount == 29) {
                Pair<Long, Long> result = processMesh(simplifier, reader, writer);
                trianglesBefore += result.getValue0();
                trianglesAfter += result.getValue1();
                //}
                //else ignoreMesh(reader);
            } else writer.add(reader.nextEvent());
        }
        reader.close();
        writer.close();
        log.info("triangles before: " + String.format("%,8d", trianglesBefore) + ", triangles after: " + String.format("%,8d", trianglesAfter));
        log.info("processing finished @" + formatTime(System.currentTimeMillis() - start));
    }

    public static String formatTime(long millis) {
        return DurationFormatUtils.formatDuration(millis, "HH:mm:ss.S");
    }

    private static boolean isStartNode(XMLEvent xmlEvent, String name) {
        return xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals(name);
    }

    private static boolean isEndNode(XMLEvent xmlEvent, String name) {
        return xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals(name);
    }

    /**
     * Read stream until end of mesh node into buffer then write complete mesh buffer back into output
     *
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    public static Pair<Long, Long> processMesh(Simplifier simplifier, XMLEventReader reader, XMLEventWriter writer) throws XMLStreamException {
        // write <mesh> tag to output
        writer.add(reader.nextEvent());
        //
        Mesh mesh = new Mesh();
        UniqueVertices uniqueVertices = null;
        while (reader.hasNext()) {
            XMLEvent xmlEvent = reader.nextEvent();
            if (isStartNode(xmlEvent, "vertices")) {
                uniqueVertices = readVertices(reader);
            } else if (isStartNode(xmlEvent, "triangles")) {
                if (uniqueVertices == null)
                    throw new XMLStreamException("Vertices still empty! Program expects <triangles> to come after <vertices> within <mesh> node.");
                mesh = readTriangles(reader, mesh, uniqueVertices);
                break;
            }
        }
        long trianglesBefore = mesh.triangles.size();
        log.info("loaded " + String.format("%,8d", uniqueVertices.size()) + " vertices and " + String.format("%,8d", trianglesBefore)  + " triangles.");

        // optimize mesh
        if(simplifier != null) mesh = simplifier.simplify(mesh);
        long trianglesAfter = mesh.triangles.size();

        // write mesh
        log.info("write..");
        meshToXmlStream(writer, mesh);
        writer.flush();

        return new Pair(trianglesBefore, trianglesAfter);
    }

    /**
     * Read mesh from reader and write to writer without any changes
     *
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    public static void passThroughMesh(XMLEventReader reader, XMLEventWriter writer) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent xmlEvent = reader.nextEvent();
            writer.add(xmlEvent);
            if (isEndNode(xmlEvent, "mesh")) {
                break;
            }
        }
        log.info("passed through.");
    }

    /**
     * Read stream until end of mesh node
     *
     * @param reader
     * @throws XMLStreamException
     */
    public static void ignoreMesh(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent xmlEvent = reader.nextEvent();
            if (isEndNode(xmlEvent, "mesh")) {
                break;
            }
        }
        log.info("ignored.");
    }

    public static void meshToXmlStream(XMLEventWriter writer, Mesh mesh) throws XMLStreamException {
        UniqueVertices uniqueVertices = mesh.getUniqueVertices();
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();

        writer.add(eventFactory.createStartElement("", "", "vertices"));
        AtomicInteger count = new AtomicInteger();
        uniqueVertices.stream()
//                .map( x -> {
//                    if(count.intValue() < 10)
//                        log.info("vertex #" + count.getAndIncrement() + " " + x.toString()
//                        + " known as #" + String.valueOf(uniqueVertices.getVertexId(x)));
//                    return x;
//                })
                .map(vertexToXMLEvents(eventFactory))
                .flatMap(Arrays::stream)
                .forEachOrdered(writeXMLEvent(writer));
        writer.add(eventFactory.createEndElement("", "", "vertices"));

        writer.add(eventFactory.createStartElement("", "", "triangles"));
        mesh.triangles.stream()
                .map(triangleToXMLEvents(eventFactory, uniqueVertices))
                .flatMap(Arrays::stream)
                .forEachOrdered(writeXMLEvent(writer));
        writer.add(eventFactory.createEndElement("", "", "triangles"));
    }

    public static Consumer<XMLEvent> writeXMLEvent(XMLEventWriter writer) {
        return (event) -> {
            try {
                writer.add(event);
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        };
    }

    public static Function<Vertex, XMLEvent[]> vertexToXMLEvents(XMLEventFactory xmlEventFactory) {
        return (vertex) ->
        {
            XMLEvent[] events = new XMLEvent[5];
            events[0] = xmlEventFactory.createStartElement("", "", "vertex");
            events[1] = xmlEventFactory.createAttribute("x", String.valueOf(vertex.getX()));
            events[2] = xmlEventFactory.createAttribute("y", String.valueOf(vertex.getY()));
            events[3] = xmlEventFactory.createAttribute("z", String.valueOf(vertex.getZ()));
            events[4] = xmlEventFactory.createEndElement("", "", "vertex");
            // events[3] = xmlEventFactory.createCharacters("\n");
            return events;
        };
    }

    public static Function<Triangle, XMLEvent[]> triangleToXMLEvents(XMLEventFactory xmlEventFactory, UniqueVertices uniqueVertices) {
        return (triangle) ->
        {
            XMLEvent[] events = new XMLEvent[5];
            Vertex[] vertices = triangle.getVertices();
            // if(triangle.id < 10) log.info("write " + triangle.toString());
            events[0] = xmlEventFactory.createStartElement("", "", "triangle");
            for(int i = 0; i < 3; i++)
            {
                events[i + 1] = xmlEventFactory.createAttribute("v"+(i+1),
                        String.valueOf(uniqueVertices.getVertexId(vertices[i])));
                // if(triangle.id < 10) log.info(events[i + 1].toString());
            }
            events[4] = xmlEventFactory.createEndElement("", "", "triangle");
            return events;
        };
    }

    /**
     * Read vertices from stream until (but not including) </vertices> tag and return list of unique vertices
     *
     * @param reader
     * @return List of unique vertices
     * @throws XMLStreamException
     */
    public static UniqueVertices readVertices(XMLEventReader reader) throws XMLStreamException {
        UniqueVertices uniqueVertices = new UniqueVertices();
        while (reader.hasNext()) {
            XMLEvent xmlEvent = reader.nextEvent();
            if (isStartNode(xmlEvent, "vertex")) {
                Vector v = nodeToVector(xmlEvent);
                Vertex vertex = uniqueVertices.putVertex(v);
                // log.info(vertex.toString());
            } else if (isEndNode(xmlEvent, "vertices")) break;
        }
        return uniqueVertices;
    }

    /**
     * Read triangles from stream until (but not including) </triangles> tag and return new mesh
     *
     * @param reader
     * @param vertices
     * @return mesh
     * @throws XMLStreamException
     */
    public static Mesh readTriangles(XMLEventReader reader, Mesh mesh, UniqueVertices vertices) throws XMLStreamException {
        // int count = 0;
        while (reader.hasNext()) {
            XMLEvent xmlEvent = reader.nextEvent();
            if (isStartNode(xmlEvent, "triangle")) {
                Triangle t = mesh.addTriangle(nodeToTriangle(xmlEvent, vertices));
                // t.id = count++;
                // if(t.id < 10) log.info("read " + t.toString());
            }
            if (isEndNode(xmlEvent, "triangles")) break;
        }
        return mesh;
    }


    private static HashMap getEventAttributes(XMLEvent xmlEvent) {
        HashMap attributes = new HashMap();
        Iterator<Attribute> iterator = xmlEvent.asStartElement().getAttributes();
        while (iterator.hasNext()) {
            Attribute attribute = iterator.next();
            attributes.put(attribute.getName().toString(), attribute.getValue());
        }
        return attributes;
    }


    public static final Vector nodeToVector(XMLEvent xmlEvent) {
        HashMap attributes = getEventAttributes(xmlEvent);
        String[] ids = {"x", "y", "z"};
        Double[] xyz = Arrays.stream(ids, 0, 3)
                .map(id -> attributes.get(id).toString())
                .mapToDouble(Double::parseDouble)
                .boxed()
                .toArray(Double[]::new);
        return new Vector(xyz);
    }

    public static final Triangle nodeToTriangle(XMLEvent xmlEvent, UniqueVertices uniqueVertices) {
        HashMap attributes = getEventAttributes(xmlEvent);
        Vertex[] vertices = IntStream.range(1, 4)
                .mapToObj(i -> "v" + i)
                .map(id -> attributes.get(id).toString())
                .mapToInt(Integer::parseInt)
                .boxed()
                .map(uniqueVertices::getVertex)
                .toArray(Vertex[]::new);
        return new Triangle(vertices);
    }

}
