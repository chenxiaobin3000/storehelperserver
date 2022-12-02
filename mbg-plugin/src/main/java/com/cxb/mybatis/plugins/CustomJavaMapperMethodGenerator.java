package com.cxb.mybatis.plugins;

import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;

import java.util.Set;
import java.util.TreeSet;

/**
 * desc: 为 mapper 添加 selectOne
 * auth: cxb
 * date: 2022/12/2
 */
public class CustomJavaMapperMethodGenerator extends AbstractJavaMapperMethodGenerator {

    @Override
    public void addInterfaceElements(Interface interfaze) {
        addInterfaceSelectOne(interfaze);
        addInterfaceSelectList(interfaze);
        addInterfaceInsertBatch(interfaze);
        addInterfaceDeleteBatchIds(interfaze);
        addInterfaceSelectBatchIds(interfaze);
    }

    private void addInterfaceSelectOne(Interface interfaze) {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
        Method method = new Method(CommonName.SELECTONE);
        method.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        method.setReturnType(returnType);
        FullyQualifiedJavaType parameterType;
        parameterType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "record"));
        initMethod(interfaze, importedTypes, method);
    }

    private void addInterfaceSelectList(Interface interfaze) {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
        Method method = new Method(CommonName.SELECTLIST);
        method.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType listType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        importedTypes.add(listType);
        returnType.addTypeArgument(listType);
        method.setReturnType(returnType);
        FullyQualifiedJavaType parameterType;
        parameterType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "record"));
        initMethod(interfaze, importedTypes, method);
    }

    private void addInterfaceInsertBatch(Interface interfaze) {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
        Method method = new Method(CommonName.INSERTBATCH);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());

        FullyQualifiedJavaType parameterType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType listType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        importedTypes.add(listType);
        parameterType.addTypeArgument(listType);
        method.addParameter(new Parameter(parameterType, "list"));

        initMethod(interfaze, importedTypes, method);
    }

    private void addInterfaceDeleteBatchIds(Interface interfaze) {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        importedTypes.add(new FullyQualifiedJavaType("java.util.Collection"));

        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType("Collection");
        FullyQualifiedJavaType objectType = FullyQualifiedJavaType.getObjectInstance();
        parameterType.addTypeArgument(objectType);

        Method method = new Method(CommonName.DELETEBATCHIDS);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.addParameter(new Parameter(parameterType, "record"));

        initMethod(interfaze, importedTypes, method);
    }

    private void addInterfaceSelectBatchIds(Interface interfaze) {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
        importedTypes.add(new FullyQualifiedJavaType("java.util.Collection"));

        Method method = new Method(CommonName.SELECTBATCHIDS);
        method.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType listType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        importedTypes.add(listType);
        returnType.addTypeArgument(listType);
        method.setReturnType(returnType);
        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType("Collection");
        parameterType.addTypeArgument(FullyQualifiedJavaType.getObjectInstance());

        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "record"));
        initMethod(interfaze, importedTypes, method);
    }

    private void initMethod(Interface interfaze, Set<FullyQualifiedJavaType> importedTypes, Method method) {
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        interfaze.addImportedTypes(importedTypes);
        interfaze.addMethod(method);
        method.setAbstract(true);
    }
}
