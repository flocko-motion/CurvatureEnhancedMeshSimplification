package de.ftk.threemf.mesh;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


public final class XmlUtil {
    private XmlUtil(){}

    public static List<Node> asList(@NotNull NodeList n) {
        return n.getLength()==0?
                Collections.<Node>emptyList(): new NodeListWrapper(n);
    }

    static final List<Node> nodesByTagName(Document document, String tagName) {
        final NodeList nodeList = document.getElementsByTagName(tagName);
        return asList(nodeList);
    }

    static final Stream<Node> streamNodesByTagName(Document document, String tagName) {
        final NodeList nodeList = document.getElementsByTagName(tagName);
        return IntStream.range(0, nodeList.getLength())
                .mapToObj(nodeList::item);
    }

    static final List<Node> childNodes(Node node) {
        return asList(node.getChildNodes());
    }

    static final List<Node> childNodes(Node node, String tagName) {
        final List<Node> list = asList(node.getChildNodes());
        final List<Node> filtered = list.stream()
                .filter(n -> n.getNodeName().toLowerCase().equals(tagName))
                .collect(Collectors.toList());
        return filtered;
    }

    static final class NodeListWrapper extends AbstractList<Node>
            implements RandomAccess {
        private final NodeList list;
        NodeListWrapper(NodeList l) {
            list=l;
        }
        public Node get(int index) {
            return list.item(index);
        }
        public int size() {
            return list.getLength();
        }
    }




}