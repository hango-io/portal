package com.netease.cloud.nsf.processor;

import com.netease.cloud.nsf.parser.ParserConst;
import groovy.lang.GroovyShell;
import org.apache.camel.Exchange;
import org.apache.camel.language.groovy.GroovyShellFactory;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.util.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/18
 **/
public class CustomGroovyShellFactory implements GroovyShellFactory {
    private List<String> defaultImports;

    public CustomGroovyShellFactory(List<String> defaultImports) {
        this.defaultImports = defaultImports;
    }

    @Override
    public GroovyShell createGroovyShell(Exchange exchange) {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        ImportCustomizer importCustomizer = new ImportCustomizer();
        // 默认import的包
        Set<String> importSet = new LinkedHashSet<>(defaultImports);
        // 从header获取需要import的包
        Object imports = exchange.getMessage().getHeader(ParserConst.GROOVY_IMPORTS);
        if (Objects.nonNull(imports) && imports instanceof String) {
            String[] importItems = ((String) imports).split(",");
            importSet.addAll(Arrays.asList(importItems));
        }
        // 从property获取需要import的包
        imports = exchange.getProperty(ParserConst.GROOVY_IMPORTS);
        if (Objects.nonNull(imports) && imports instanceof String) {
            String[] importItems = ((String) imports).split(",");
            importSet.addAll(Arrays.asList(importItems));
        }

        for (String importItem : importSet) {
            // import like groovy.json.*
            if (importItem.endsWith(".*")) {
                String packageName = StringUtils.substringBeforeLast(importItem, ".*");
                importCustomizer.addStarImports(packageName);
                // import like groovy.json.JsonOutput
            } else {
                importCustomizer.addImports(importItem);
            }
        }
        compilerConfiguration.addCompilationCustomizers(importCustomizer);
        return new GroovyShell(compilerConfiguration);
    }
}