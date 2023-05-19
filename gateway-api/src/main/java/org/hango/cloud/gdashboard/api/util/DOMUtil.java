package org.hango.cloud.gdashboard.api.util;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author user
 */
public class DOMUtil {
    /**
     * Serialise the supplied W3C DOM subtree.
     * <p/>
     * The output is unformatted.
     *
     * @param nodeList The DOM subtree as a NodeList.
     * @return The subtree in serailised form.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static String serialize(NodeList nodeList) throws DOMException {
        return serialize(nodeList, false);
    }

    /**
     * Serialise the supplied W3C DOM subtree.
     *
     * @param node   The DOM node to be serialized.
     * @param format Format the output.
     * @return The subtree in serailised form.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static String serialize(final Node node, boolean format) throws DOMException {
        StringWriter writer = new StringWriter();
        serialize(node, format, writer);
        return writer.toString();
    }

    /**
     * the supplied W3C DOM subtree.
     *
     * @param node   The DOM node to be serialized.
     * @param format Format the output.
     * @param writer The target writer for serialization.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static void serialize(final Node node, boolean format, Writer writer) throws DOMException {
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            serialize(node.getChildNodes(), format, writer);
        } else {
            serialize(new NodeList() {
                public Node item(int index) {
                    return node;
                }

                public int getLength() {
                    return 1;
                }
            }, format, writer);
        }
    }

    /**
     * Serialise the supplied W3C DOM subtree.
     *
     * @param nodeList The DOM subtree as a NodeList.
     * @param format   Format the output.
     * @return The subtree in serailised form.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static String serialize(NodeList nodeList, boolean format) throws DOMException {
        StringWriter writer = new StringWriter();
        serialize(nodeList, format, writer);
        return writer.toString();
    }

    /**
     * Serialise the supplied W3C DOM subtree.
     *
     * @param nodeList The DOM subtree as a NodeList.
     * @param format   Format the output.
     * @param writer   The target writer for serialization.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static void serialize(NodeList nodeList, boolean format, Writer writer) throws DOMException {
        try {
            // nodeList为空
            if (nodeList == null) {
                throw new IllegalArgumentException("XmlUtil.serialize(NodeList nodeList, boolean format, Writer writer)中参数nodeList为");
            }
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            // 设置格式化
            if (format) {
                try {
                    // (用于jdk1.5)设置缩进，如果运行在1.4上会抛出异常
                    factory.setAttribute("indent-number", new Integer(4));
                } catch (Exception e) {
                    throw new RuntimeException("设置TransformerFactory的缩进量为4失败:" + e.getMessage());
                }
            }
            // 取得transformer
            Transformer transformer = factory.newTransformer();
            // 设置编码格式
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            // 设置是否忽略xml声明片段
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            if (format) {
                // 设置xml是否进行缩进处理
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                // (用于jdk1.4)也可以这样写http://xml.apache.org/xslt}indent-amount，区别只是命名空间不一样
                transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");
            }
            // 处理所有的结点
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (isTextNode(node)) {
                    // 如果是文本结点，则直接输出
                    writer.write(node.getNodeValue());
                } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                    writer.write(((Attr) node).getValue());
                } else if (node.getNodeType() == Node.ELEMENT_NODE) {
                    transformer.transform(new DOMSource(node), new StreamResult(writer));
                }
            }
        } catch (Exception e) {
            DOMException domExcep = new DOMException(DOMException.INVALID_ACCESS_ERR, "Unable to serailise DOM subtree.");
            domExcep.initCause(e);
            throw domExcep;
        }
    }

    /**
     * 判断node是否为文本结点
     *
     * @param node
     * @return
     */
    public static boolean isTextNode(Node node) {
        short nodeType;

        if (node == null) {
            return false;
        }
        nodeType = node.getNodeType();

        return nodeType == Node.CDATA_SECTION_NODE || nodeType == Node.TEXT_NODE;
    }

    /**
     * 统计在node元素前，有多少个兄弟元素
     *
     * @param node
     * @param tagName
     * @return
     */
    public static int countElementsBefore(Node node, String tagName) {
        // 得到该元素的父元素
        Node parent = node.getParentNode();
        // 得到父元素下的所有子元素
        NodeList siblings = parent.getChildNodes();
        // 统计在该元素前有几个兄弟元素
        int count = 0;
        // 父元素下的所有子元素数量
        int siblingCount = siblings.getLength();
        // 循环遍历
        for (int i = 0; i < siblingCount; i++) {
            Node sibling = siblings.item(i);
            // 判断当前元素是否与node相等，若相等，则返回
            if (sibling == node) {
                break;
            }
            // 统计count，若sibling为element且tagName与参数相等，count加1
            if (sibling.getNodeType() == Node.ELEMENT_NODE && ((Element) sibling).getTagName().equals(tagName)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 根据type从element中查找第一个子元素
     *
     * @param element
     * @param nodeType
     * @return
     */
    public static Node getFirstChildByType(Element element, int nodeType) {
        NodeList children = element.getChildNodes();
        int childCount = children.getLength();

        for (int i = 0; i < childCount; i++) {
            Node child = children.item(i);
            if (child.getNodeType() == nodeType) {
                return child;
            }
        }

        return null;
    }

    /**
     * 根据type从element中查找第一个子元素
     *
     * @param node
     * @param nodeType
     * @return
     */
    public static Node getFirstChildByType(Node node, int nodeType) {
        NodeList children = node.getChildNodes();
        int childCount = children.getLength();

        for (int i = 0; i < childCount; i++) {
            Node child = children.item(i);
            if (child.getNodeType() == nodeType) {
                return child;
            }
        }

        return null;
    }

    /**
     * 判断这个element是否为集合，依据为：注释中是否包含"Zero or more repetitions"字符串
     * <p>
     * //	 * @param node
     *
     * @return
     */
    public static boolean assertIsCollectionByParentNode(Node parentNode) {
        Comment firstComment = (Comment) DOMUtil.getFirstChildByType(parentNode, Node.COMMENT_NODE);

        if (firstComment != null && firstComment.getNodeValue().indexOf("Zero or more repetitions") != -1) {
            return true;
        }
        /*
         * else if (element.getNodeType() == Node.ELEMENT_NODE && getReturnType(element).equals(SOAPReturnType.type_array.toString())) { //
         * 如果element元素的子元素是否有多个return结点，如果有，则返回true return true; }
         */

        return false;
    }

    /**
     * 判断该结点是否为集合
     *
     * @param node
     * @return
     */
    public static boolean assertIsCollection(Node node) {
        boolean result = false;
        if (node != null) {
            Node previousNode = node.getPreviousSibling().getPreviousSibling();
            if (previousNode.getNodeType() == Node.COMMENT_NODE) {
                Comment comment = (Comment) previousNode;

                if (comment != null && comment.getNodeValue().indexOf("Zero or more repetitions") != -1) {
                    return true;
                }
            }
        }
        return result;
    }

    /**
     * 判断结点的属性是否存在
     *
     * @param node
     * @param attributeName
     * @return
     */
    public static boolean assertNodeAttributeExist(Node node, String attributeName) {
        boolean result = false;
        if (node != null) {
            NamedNodeMap attributeMap = node.getAttributes();
            Node attributeNode = attributeMap.getNamedItem(attributeName);
            if (attributeNode != null) {
                if (StringUtils.isNotEmpty(attributeNode.getNodeValue())) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * 查看parentNode下的所有element类型的子结点的个数
     *
     * @param parentNode
     * @return
     * @throws Exception
     */
    public static int getChildElementNodeLength(Node parentNode) throws Exception {
        int elementChildLength = 0;
        if (parentNode != null) {
            NodeList childList = parentNode.getChildNodes();
            for (int i = 0; i < childList.getLength(); i++) {
                Node node = childList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    elementChildLength++;
                }
            }
        }
        return elementChildLength;
    }

    /**
     * 根据parentNode得到第一个element结点
     *
     * @param parentNode
     * @return
     * @throws Exception
     */
    public static Node getFirstChildElementNode(Node parentNode) throws Exception {
        Node elementNode = null;
        if (parentNode != null) {
            NodeList childList = parentNode.getChildNodes();
            for (int i = 0; i < childList.getLength(); i++) {
                Node node = childList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    elementNode = node;
                    break;
                }
            }
        }
        return elementNode;
    }

    /**
     * 得到该结点下的element子结点
     *
     * @param parentNode
     * @return
     * @throws Exception
     */
    public static List<Node> getChildElementNodes(Node parentNode) throws Exception {
        List<Node> list = new ArrayList<Node>();
        if (parentNode != null) {
            NodeList childList = parentNode.getChildNodes();
            for (int i = 0; i < childList.getLength(); i++) {
                Node node = childList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    list.add(node);
                }
            }
        }
        return list;
    }

    /**
     * 根据结点名称查找结点
     *
     * @param parentNode
     * @return
     * @throws Exception
     */
    public static Node getChildElementNodeByName(Node parentNode, String childName) throws Exception {
        Node elementNode = null;
        if (parentNode != null) {
            NodeList childList = parentNode.getChildNodes();
            for (int i = 0; i < childList.getLength(); i++) {
                Node node = childList.item(i);
                if (childName.equals(node.getNodeName())) {
                    elementNode = node;
                    break;
                }
            }
        }
        return elementNode;
    }

    /**
     * 根据结点名称查找结点
     *
     * @param parentNode
     * @return
     * @throws Exception
     */
    public static Node getChildElementNodeByIndex(Node parentNode, int index) throws Exception {
        Node elementNode = null;
        if (parentNode != null) {
            NodeList childList = parentNode.getChildNodes();
            int count = -1;
            for (int i = 0; i < childList.getLength(); i++) {
                Node node = childList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    count++;
                    if (count == index) {
                        elementNode = node;
                        break;
                    }
                }
            }
        }
        return elementNode;
    }

    /**
     * 将NodeList转换为List<Node>
     *
     * @param nodeList
     * @return
     * @throws Exception
     */
    public static List<Node> covertNodeListToList(NodeList nodeList) throws Exception {
        List<Node> list = new ArrayList<Node>();
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                list.add(nodeList.item(i));
            }
        }
        return list;
    }

    /**
     * 得到结点的属性值
     *
     * @param node
     * @param attributeName
     * @return
     * @throws Exception
     */
    public static String getAttributeValue(Node node, String attributeName) throws Exception {
        String attributeValue = "";
        if (node != null) {
            NamedNodeMap attributeMap = node.getAttributes();
            Node attributeNode = attributeMap.getNamedItem(attributeName);
            if (attributeNode != null) {
                attributeValue = attributeNode.getNodeValue();
            }
        }
        return attributeValue;
    }

    /**
     * 得到结点的name属性值
     *
     * @param node
     * @return
     * @throws Exception
     */
    public static String getNodeName(Node node) throws Exception {
        return getAttributeValue(node, "name");
    }

    /**
     * 得到结点的type属性值
     *
     * @param node
     * @return
     * @throws Exception
     */
    public static String getNodeType(Node node) throws Exception {
        String type = getAttributeValue(node, "type");
        if (StringUtils.isNotEmpty(type)) {
            if (type.indexOf(":") >= 0) {
                return type.split(":")[1];
            } else {
                return type;
            }
        }
        return "";
    }

    /**
     * 得到结点的maxOccurs属性值
     *
     * @param node
     * @return
     * @throws Exception
     */
    public static String getNodeMaxOccurs(Node node) throws Exception {
        return getAttributeValue(node, "maxOccurs");
    }

    /**
     * 得到结点的minOccurs属性值
     *
     * @param node
     * @return
     * @throws Exception
     */
    public static String getNodeMinOccurs(Node node) throws Exception {
        return getAttributeValue(node, "minOccurs");
    }

    /**
     * 判断type是否为schema默认的类型
     *
     * @param node
     * @return
     * @throws Exception
     */
    public static boolean isDefaultType(Node node) throws Exception {
        boolean result = false;
        if (node != null) {
            String type = DOMUtil.getNodeType(node);
            WsdlUtil.SchemaDefaulyType[] defaultTypes = WsdlUtil.SchemaDefaulyType.values();
            for (int i = 0; i < defaultTypes.length; i++) {
                WsdlUtil.SchemaDefaulyType defaultType = defaultTypes[i];
                if (type.equals(defaultType.getType())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 判断是否为基本数据类型
     *
     * @param type
     * @return
     * @throws Exception
     */
    public static boolean isDefaultType(String type) throws Exception {
        boolean result = false;
        if (StringUtils.isNotEmpty(type)) {
            WsdlUtil.SchemaDefaulyType[] defaultTypes = WsdlUtil.SchemaDefaulyType.values();
            for (int i = 0; i < defaultTypes.length; i++) {
                WsdlUtil.SchemaDefaulyType defaultType = defaultTypes[i];
                if (type.equals(defaultType.getType())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 判断xs:element是否为数组类型
     * <p>
     * //	 * @param maxOccurs
     * //	 * @param minOccurs
     *
     * @return
     * @throws Exception
     */
    public static boolean isArray(Node node) throws Exception {
        boolean result = false;
        if (node != null) {
            String minOccurs = DOMUtil.getNodeMinOccurs(node);
            String maxOccurs = DOMUtil.getNodeMaxOccurs(node);
            if ("0".equals(minOccurs) && maxOccurs != null && !"".equals(maxOccurs)
                    && ("unbounded".equals(maxOccurs) || Integer.valueOf(maxOccurs) > 0)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * 在document中查找结点，如果查找不到，则进入wsd:import结点中递归查找
     *
     * @param document
     * @param xpathStr
     * @return
     * @throws Exception
     */
    public static Node findNode(Document document, String xpathStr) throws Exception {
        XPath xpath = WsdlUtil.getXpath(document);
        Node node = (Node) xpath.evaluate(xpathStr, document, XPathConstants.NODE);
        if (node == null) {
            List<Document> importDocumentList = WsdlUtil.getImportDocumentList(document, xpath);
            for (Document importDoucment : importDocumentList) {
                node = (Node) findNode(importDoucment, xpathStr);
                if (node != null)
                    return node;
            }
        }
        return node;
    }

    /**
     * 在document中查找结点，如果查找不到，则进入wsd:import结点中递归查找
     *
     * @param document
     * @param xpathStr
     * @return
     * @throws Exception
     */
    public static NodeList findNodeList(Document document, String xpathStr) throws Exception {
        XPath xpath = WsdlUtil.getXpath(document);
        NodeList nodeList = (NodeList) xpath.evaluate(xpathStr, document, XPathConstants.NODESET);
        if (nodeList.getLength() == 0) {
            List<Document> importDocumentList = WsdlUtil.getImportDocumentList(document, xpath);
            for (Document importDoucment : importDocumentList) {
                nodeList = findNodeList(importDoucment, xpathStr);
                if (nodeList.getLength() > 0)
                    return nodeList;
            }
        }
        return nodeList;
    }
}
