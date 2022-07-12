package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.parser.BaseParser;
import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.parser.StepNode;
import com.netease.cloud.nsf.service.TranslateService;
import com.netease.cloud.nsf.step.Step;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/21
 **/
public class TranslateServiceImpl extends BaseParser implements TranslateService {
    private static final Logger logger = LoggerFactory.getLogger(TranslateServiceImpl.class);

    private ParserContext parserContext;

    public TranslateServiceImpl(ParserContext parserContext) {
        this.parserContext = parserContext;
    }

    @Override
    public String translate(Step step) {
        try {
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("routes", "http://camel.apache.org/schema/spring");
            StepNode stepNode = new StepNode(step);
            parserContext.getParser(stepNode).parse(root, stepNode, parserContext);
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setTrimText(false);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            XMLWriter writer = new XMLWriter(outputStream, format);
            writer.write(document.getRootElement());
            writer.close();

            String result = outputStream.toString("utf-8");
            logger.debug("integration xml:\n{}", result);

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
