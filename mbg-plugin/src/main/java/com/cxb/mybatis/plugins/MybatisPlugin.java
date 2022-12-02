package com.cxb.mybatis.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

import java.util.List;

/**
 * desc: selectOne 插件，顺序: 4
 * auth: cxb
 * date: 2022/12/1
 */
public class MybatisPlugin extends LimitPlugin {
    public static final String SELECTONE = "selectOne";

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        init(document, introspectedTable, new CustomAbstractXmlElementGenerator());
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        AbstractJavaMapperMethodGenerator methodGenerator = new CustomJavaMapperMethodGenerator();
        methodGenerator.setContext(context);
        methodGenerator.setIntrospectedTable(introspectedTable);
        methodGenerator.addInterfaceElements(interfaze);
        return super.clientGenerated(interfaze, introspectedTable);
    }

    private void init(Document document, IntrospectedTable introspectedTable, AbstractXmlElementGenerator elementGenerator) {
        elementGenerator.setContext(context);
        elementGenerator.setIntrospectedTable(introspectedTable);
        elementGenerator.addElements(document.getRootElement());
    }
}
