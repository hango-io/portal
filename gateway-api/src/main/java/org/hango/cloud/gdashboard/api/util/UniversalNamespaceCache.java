package org.hango.cloud.gdashboard.api.util;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class UniversalNamespaceCache implements NamespaceContext {
    private static final String DEFAULT_NS = "DEFAULT";
    private Map<String, String> prefix2Uri = new HashMap<String, String>();
    private Map<String, String> uri2Prefix = new HashMap<String, String>();

    /**
     * 查找所有能够找到的namespace，如果toplevelOnly为true，则只查找根元素。如果toplevelOnly为false，则递归查询
     *
     * @param document
     * @param toplevelOnly
     */
    public UniversalNamespaceCache(Document document, boolean toplevelOnly) {
        try {
            // 检查docment的根元素wsdl:definitions
            examineNode(document.getFirstChild(), toplevelOnly);
            // 检查import中的命名空间
            NodeList childrenOfDefinition = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < childrenOfDefinition.getLength(); i++) {
                Node childOfDefinition = childrenOfDefinition.item(i);
                if ("import".equals(childOfDefinition.getLocalName())) {
                    String location = DOMUtil.getAttributeValue(childOfDefinition, "location");
                    Document importDoc = WsdlUtil.getDefinitionDocument(location);
                    examineNode(importDoc.getDocumentElement(), toplevelOnly);
                }
            }
            // System.out.println("The list of the cached namespaces:");
            // for (String key : prefix2Uri.keySet()) {
            // System.out.println("prefix " + key + ": uri " + prefix2Uri.get(key));
            // }
        } catch (Exception e) {
            throw new RuntimeException("解析wsdl命名空间失败", e);
        }
    }

    /**
     * 根据attributesOnly递归检查结点的命名空间属性
     *
     * @param node
     * @param attributesOnly
     */
    private void examineNode(Node node, boolean attributesOnly) {
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            storeAttribute((Attr) attribute);
        }

        if (!attributesOnly) {
            NodeList chields = node.getChildNodes();
            for (int i = 0; i < chields.getLength(); i++) {
                Node chield = chields.item(i);
                if (chield.getNodeType() == Node.ELEMENT_NODE)
                    examineNode(chield, false);
            }
        }
    }

    /**
     * 存储命名空间
     *
     * @param attribute
     */
    private void storeAttribute(Attr attribute) {
        // 检查属性的命名空间是否为xmlns
        if (attribute.getNamespaceURI() != null && attribute.getNamespaceURI().equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            if (attribute.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                // 查找默认的命名空间xmlns="http://www.w3.org/2001/XMLSchema"
                putInCache(DEFAULT_NS, attribute.getNodeValue());
            } else {
                // 以命名空间xmlns:ns1="http://wstool.service.openbilling.ailk.com/"的ns1为key，url为value，存储map中
                putInCache(attribute.getLocalName(), attribute.getNodeValue());
            }
        }

    }

    /**
     * 将前缀存储到map中
     *
     * @param prefix
     * @param uri
     */
    private void putInCache(String prefix, String uri) {
        prefix2Uri.put(prefix, uri);
        uri2Prefix.put(uri, prefix);
    }

    /**
     * This method is called by XPath. It returns the default namespace, if the prefix is null or "".
     *
     * @param prefix to search for
     * @return uri
     */
    public String getNamespaceURI(String prefix) {
        if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return prefix2Uri.get(DEFAULT_NS);
        } else {
            return prefix2Uri.get(prefix);
        }
    }

    /**
     * This method is not needed in this context, but can be implemented in a similar way.
     */
    public String getPrefix(String namespaceURI) {
        return uri2Prefix.get(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI) {
        // Not implemented
        return null;
    }

}
