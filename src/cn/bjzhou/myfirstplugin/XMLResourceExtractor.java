package cn.bjzhou.myfirstplugin;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XMLResourceExtractor {

    public static XMLResourceExtractor createResourceExtractor() {
        return new XMLResourceExtractor();
    }

    protected XMLResourceExtractor() {
    }

    public List<Resource> extractResourceObjectsFromStream(InputStream inputStream) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        ArrayList<Resource> resources = new ArrayList<>();
        NodeList nodeList = this.extractWidgetNodesWithId(inputStream);

        for(int i = 0; i < nodeList.getLength(); ++i) {
            resources.add(this.getResourceObject(nodeList.item(i)));
        }

        return resources;
    }

    private Resource getResourceObject(Node node) {
        String[] split = getIdAttributeValue(node).split("\\/");
        String resourceId = split[1];
        int lastDot = node.getNodeName().lastIndexOf(".");
        String resourceType = lastDot == -1 ? node.getNodeName() : node.getNodeName().substring(lastDot + 1);
        return new Resource(resourceId, resourceType);
    }

    private String getIdAttributeValue(Node node) {
        return node.getAttributes().getNamedItem("android:id").getNodeValue();
    }

    private NodeList extractWidgetNodesWithId(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        XPathFactory pathFactory = XPathFactory.newInstance();
        XPath xPath = pathFactory.newXPath();
        XPathExpression expression = xPath.compile("//*[@id]");
        return (NodeList)expression.evaluate(doc, XPathConstants.NODESET);
    }
}