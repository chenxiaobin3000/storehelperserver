package com.cxb.mybatis.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

/**
 * desc: limit/offset/groupBy/orderBy 插件，顺序: 3
 * auth: cxb
 * date: 2022/12/1
 */
public class LimitPlugin extends LombokPlugin {

    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    /**
     * 为每个 Example 类添加: limit/offset/groupBy 属性和 get/set 方法
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
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 为 Mapper.xml 的 selectByExample 添加 limit/offset/groupBy
     */
    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
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
        return super.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }
}
