package org.hango.cloud.gdashboard.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

public class WsdlUtil {

    private static final Logger log = LoggerFactory.getLogger(WsdlUtil.class);
    private static WSDLFactory wsdlFactory;

    /**
     * 得到wsdl中所有的方法
     *
     * @param wsdlUrl
     * @return
     * @throws Exception
     */
    public static List<String> getOperationList(String wsdlUrl) throws Exception {
        Document document = getDefinitionDocument(wsdlUrl);
        XPath xpath = getXpath(document);

        NodeList operations = DOMUtil.findNodeList(document, "wsdl:definitions/wsdl:portType/wsdl:operation");

        // 返回的结果集list
        List<String> operationList = new ArrayList<String>();
        for (int i = 0; i < operations.getLength(); i++) {
            Node operation = operations.item(i);
            String operationName = DOMUtil.getNodeName(operation);
            if (operationName != null && !"".equals(operationName)) {
                log.debug("解析" + wsdlUrl + "中的方法：" + operationName);
                operationList.add(operationName);
            }
        }
        return operationList;
    }

    /**
     * 根据operationName从wsdl的document中查找出输入element名称， 即<wsdl:part name="parameters" element="ns:helloWorld" />中的element
     *
     * @param document
     * @param operationName
     * @return
     * @throws Exception
     */
    public static String getInputElementName(Document document, String operationName) throws Exception {
        // 得到wsdl文件的xpath
        XPath xpath = getXpath(document);

        // 查找到input结点message属性的值
        Node inputMessageNode = DOMUtil.findNode(document, "wsdl:definitions/wsdl:portType/wsdl:operation[@name='" + operationName + "']/wsdl:input");
        // 完整名称为ns:helloWorldRequest，需要根据":"截取
        String simpleInputMessageName = DOMUtil.getAttributeValue(inputMessageNode, "message").split(":")[1];
        log.debug("查找到wsdl:input message为" + simpleInputMessageName);

        // 根据simpleInputMessageName名称查找message结点下的part结点的element属性值
        Node partNode = DOMUtil.findNode(document, "wsdl:definitions/wsdl:message[@name='" + simpleInputMessageName + "']/wsdl:part");
        String simpleElementName = DOMUtil.getAttributeValue(partNode, "element").split(":")[1];
        log.debug("查找到wsdl:input message为" + simpleElementName);
        return simpleElementName;
    }

    /**
     * 得到WSDLFactory
     *
     * @return
     * @throws Exception
     */
    public static WSDLFactory getWsdlFactory() throws Exception {
        if (wsdlFactory == null)
            wsdlFactory = WSDLFactory.newInstance();
        return wsdlFactory;
    }

    public static Definition getDefinition(String wsdlUrl) throws Exception {
        WSDLFactory factory = getWsdlFactory();

        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", true);
        reader.setFeature("javax.wsdl.importDocuments", true);

        Definition def = reader.readWSDL(wsdlUrl);
        return def;
    }

    /**
     * 得到wsdl文件的根结点的document
     *
     * @param wsdlUrl
     * @return
     * @throws Exception
     */
    public static Document getDefinitionDocument(String wsdlUrl) throws Exception {
        Definition def = getDefinition(wsdlUrl);

        WSDLWriter writer = getWsdlFactory().newWSDLWriter();
        Document document = writer.getDocument(def);

        return document;
    }

    /**
     * 得到document的查找工具xpath
     *
     * @param document
     * @return
     * @throws Exception
     */
    public static XPath getXpath(Document document) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new UniversalNamespaceCache(document, false));
        return xpath;
    }

    /**
     * 查找所有的wsdl:import结点
     *
     * @param document
     * @param xpath
     * @return
     * @throws Exception
     */
    public static List<Document> getImportDocumentList(Document document, XPath xpath) throws Exception {
        List<Document> importDocumentList = new ArrayList<Document>();
        // 查找所有的wsdl:import
        NodeList importNodeList = (NodeList) xpath.evaluate("wsdl:definitions/wsdl:import", document, XPathConstants.NODESET);
        if (importNodeList != null) {
            for (int i = 0; i < importNodeList.getLength(); i++) {
                Node importNode = importNodeList.item(i);
                String location = DOMUtil.getAttributeValue(importNode, "location");

                Document importDocument = getDefinitionDocument(location);
                importDocumentList.add(importDocument);
            }
        }
        return importDocumentList;
    }

    /**
     * 根据complexTypeName在wsdl文件中查找complextType结点
     *
     * @param document
     * @param complexTypeName
     * @return
     * @throws Exception
     */
    public static Node getComplexType(Document document, String complexTypeName) throws Exception {
        Node complextNode = DOMUtil.findNode(document, "wsdl:definitions/wsdl:types/xs:schema/xs:complexType[@name='" + complexTypeName + "']");
        return complextNode;
    }

    /**
     * 根据complexTypeName查找xs:complexType结点下xs:sequence下的xs:element
     *
     * @param document
     * @param complexTypeName
     * @return
     * @throws Exception
     */
    public static List<Node> getSequenceElementOfComplexType(Document document, String complexTypeName) throws Exception {
        NodeList elementsOfSequence = DOMUtil.findNodeList(document, "wsdl:definitions/wsdl:types/xs:schema/xs:complexType[@name='" + complexTypeName
                + "']/xs:sequence/xs:element");
        return DOMUtil.covertNodeListToList(elementsOfSequence);
    }

    /**
     * 如果xs:element结点的type类型不是引用的，是在自己的子结点中定义的话，则查找该element的type
     *
     * @param document
     * @param node
     * @return
     * @throws Exception
     */
    public static String getSequenceElementType(Document document, Node node) throws Exception {
        String type = "";
        if (node != null) {
            type = DOMUtil.getAttributeValue(node, "name");
        }
        return type;
    }

    /**
     * wsdl文件中type包含的基本类型
     */
    public enum SchemaDefaulyType {
        type_string("string"), type_decimal("decimal"), type_integer("integer"), type_int("int"), type_float("float"), type_long("long"), type_boolean(
                "boolean"), type_time("time"), type_date("date"), type_datetime("datetime"), type_array("array"), type_anyType("anyType");

        private String type;

        SchemaDefaulyType(final String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
