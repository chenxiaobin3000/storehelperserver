package com.cxb.mybatis.plugins;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class MybatisPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    /**
     * 为 mapper 加注解
     */
    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        // 添加Mapper的import
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));

        // 添加Mapper的注解
        interfaze.addAnnotation("@Mapper");
        return true;
    }

    /**
     * 为每个 Example 类添加: limit/offset/groupBy/orderBy 属性和 get/set 方法
     */
    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        PrimitiveTypeWrapper integerWrapper = FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper();

        // limit
        Field limit = new Field("limit", integerWrapper);
        limit.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(limit);

        Method setLimit = new Method("setLimit");
        setLimit.setVisibility(JavaVisibility.PUBLIC);
        setLimit.addParameter(new Parameter(integerWrapper, "limit"));
        setLimit.addBodyLine("this.limit = limit;");
        topLevelClass.addMethod(setLimit);

        Method getLimit = new Method("getLimit");
        getLimit.setVisibility(JavaVisibility.PUBLIC);
        getLimit.setReturnType(integerWrapper);
        getLimit.addBodyLine("return limit;");
        topLevelClass.addMethod(getLimit);

        // offset
        Field offset = new Field("offset", integerWrapper);
        offset.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(offset);

        Method setOffset = new Method("setOffset");
        setOffset.setVisibility(JavaVisibility.PUBLIC);
        setOffset.addParameter(new Parameter(integerWrapper, "offset"));
        setOffset.addBodyLine("this.offset = offset;");
        topLevelClass.addMethod(setOffset);

        Method getOffset = new Method("getOffset");
        getOffset.setVisibility(JavaVisibility.PUBLIC);
        getOffset.setReturnType(integerWrapper);
        getOffset.addBodyLine("return offset;");
        topLevelClass.addMethod(getOffset);

        // group by
        FullyQualifiedJavaType type = FullyQualifiedJavaType.getStringInstance();
        Field group = new Field("group", type);
        group.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(group);

        Method setGroup = new Method("setGroupBy");
        setGroup.setVisibility(JavaVisibility.PUBLIC);
        setGroup.addParameter(new Parameter(type, "group"));
        setGroup.addBodyLine("this.group = group;");
        topLevelClass.addMethod(setGroup);

        Method getGroup = new Method("getGroupBy");
        getGroup.setVisibility(JavaVisibility.PUBLIC);
        getGroup.setReturnType(type);
        getGroup.addBodyLine("return group;");
        topLevelClass.addMethod(getGroup);

        // order by asc
        Field orderAsc = new Field("orderAsc", type);
        orderAsc.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(orderAsc);

        Method setOrderAsc = new Method("setOrderByAsc");
        setOrderAsc.setVisibility(JavaVisibility.PUBLIC);
        setOrderAsc.addParameter(new Parameter(type, "orderAsc"));
        setOrderAsc.addBodyLine("this.orderAsc = orderAsc;");
        topLevelClass.addMethod(setOrderAsc);

        Method getOrderAsc = new Method("getOrderByAsc");
        getOrderAsc.setVisibility(JavaVisibility.PUBLIC);
        getOrderAsc.setReturnType(type);
        getOrderAsc.addBodyLine("return orderAsc;");
        topLevelClass.addMethod(getOrderAsc);

        // order by desc
        Field orderDesc = new Field("orderDesc", type);
        orderDesc.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(orderDesc);

        Method setOrderDesc = new Method("setOrderByDesc");
        setOrderDesc.setVisibility(JavaVisibility.PUBLIC);
        setOrderDesc.addParameter(new Parameter(type, "orderDesc"));
        setOrderDesc.addBodyLine("this.orderDesc = orderDesc;");
        topLevelClass.addMethod(setOrderDesc);

        Method getOrderDesc = new Method("getOrderByDesc");
        getOrderDesc.setVisibility(JavaVisibility.PUBLIC);
        getOrderDesc.setReturnType(type);
        getOrderDesc.addBodyLine("return orderDesc;");
        topLevelClass.addMethod(getOrderDesc);
        return true;
    }

    /**
     * 为 Mapper.xml 的 selectByExample 添加 limit/offset/groupBy/orderBy
     */
    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
        // limit offset
        XmlElement ifLimitNotNullElement = new XmlElement("if");
        ifLimitNotNullElement.addAttribute(new Attribute("test", "limit != null"));

        XmlElement ifOffsetNotNullElement = new XmlElement("if");
        ifOffsetNotNullElement.addAttribute(new Attribute("test", "offset != null"));
        ifOffsetNotNullElement.addElement(new TextElement("limit ${offset}, ${limit}"));
        ifLimitNotNullElement.addElement(ifOffsetNotNullElement);

        XmlElement ifOffsetNullElement = new XmlElement("if");
        ifOffsetNullElement.addAttribute(new Attribute("test", "offset == null"));
        ifOffsetNullElement.addElement(new TextElement("limit ${limit}"));
        ifLimitNotNullElement.addElement(ifOffsetNullElement);

        element.addElement(ifLimitNotNullElement);

        // group
        XmlElement ifGroupNotNullElement = new XmlElement("if");
        ifGroupNotNullElement.addAttribute(new Attribute("test", "group != null"));
        ifGroupNotNullElement.addElement(new TextElement("group by ${group}"));
        element.addElement(ifGroupNotNullElement);

        // order asc
        XmlElement ifOrderAscNotNullElement = new XmlElement("if");
        ifOrderAscNotNullElement.addAttribute(new Attribute("test", "orderAsc != null"));
        ifOrderAscNotNullElement.addElement(new TextElement("order by ${orderAsc} asc"));
        element.addElement(ifOrderAscNotNullElement);

        // order desc
        XmlElement ifOrderDescNotNullElement = new XmlElement("if");
        ifOrderDescNotNullElement.addAttribute(new Attribute("test", "orderDesc != null"));
        ifOrderDescNotNullElement.addElement(new TextElement("order by ${orderDesc} desc"));
        element.addElement(ifOrderDescNotNullElement);
        return true;
    }
}
