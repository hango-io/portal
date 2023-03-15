package org.hango.cloud.dashboard.webservice

import com.predic8.schema.creator.SchemaCreatorContext;

class SoapCreatorContext extends SchemaCreatorContext implements Cloneable {
    def path = ''
    def element
    def elements = []
    def isArrayItem = false
    int maxRecursionDepth = 2
    // 数组结构时，是否自动省略parent element
    def ignoreArrayParent = true
    AnalyzerContext analyzerContext = new AnalyzerContext()

    public Object clone() {
        return new SoapCreatorContext(error: error, declNS: copyDeclNS(), createLinks: createLinks, getSchemaId: getSchemaId, path: path, element: element, elements: elements.clone(), isArrayItem: isArrayItem, maxRecursionDepth: maxRecursionDepth, ignoreArrayParent: ignoreArrayParent, analyzerContext: analyzerContext)
    }
}
