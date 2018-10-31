package yk.core.util;

import org.dom4j.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 杨剑
 * @date 2018/10/25
 */
public class XMLParser {

    private String charset;

    public XMLParser(String charset) {
        this.charset = charset;
    }

    public String getText(String xml, String elementXpath) {
        Document doc = parseXmlToDoc(xml);
        Node node = doc.selectSingleNode("/" + elementXpath);
        return node == null ? null : node.getText();
    }

    public Map<String, String> mappingElements(String xml, String xpath) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (Node node : selectNodes(xml, xpath)) {
            map.put(node.getName(), ((Element) node).getTextTrim());
        }
        return map;
    }

    public List<Map<String, String>> mappingElementsByName(String xml, String xpath, String elementName) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (Node node : selectNodes(xml, xpath)) {
            if (node.getName().equals(elementName)) {
                list.add(mappingSubElements((Element) node));
            }
        }
        return list;
    }

    public List<Node> getNodesByAttr(String xml, String xpath, String attrName) {
        List<Node> list = new ArrayList<Node>();
        for (Node node : selectNodes(xml, xpath)) {
            if (((Element) node).attributeValue(attrName) != null) {
                list.add(node);
            }
        }
        return list;
    }

    private List<Node> selectNodes(String xml, String xpath) {
        List<Node> list = new ArrayList<Node>();
        Document doc = parseXmlToDoc(xml);
        for (Object obj : doc.selectNodes(xpath)) {
            list.add((Node) obj);
        }
        return list;
    }

    private Document parseXmlToDoc(String xml) {
        Document doc;
        try {
            doc = DocumentHelper.parseText(xml);
            doc.setXMLEncoding(charset);
        } catch (DocumentException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return doc;
    }

    private Map<String, String> mappingSubElements(Element element) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        Element ele;
        for (Object obj : element.elements()) {
            ele = (Element) obj;
            map.put(ele.getName(), ele.getTextTrim());
        }
        return map;
    }
}
