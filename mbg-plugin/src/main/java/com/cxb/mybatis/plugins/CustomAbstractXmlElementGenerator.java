package com.cxb.mybatis.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;

/**
 * desc: 为 xml 添加 selectOne
 * auth: cxb
 * date: 2022/12/2
 */
public class CustomAbstractXmlElementGenerator extends AbstractXmlElementGenerator {

    @Override
    public void addElements(XmlElement parentElement) {
        // 增加base_query
        XmlElement sql = new XmlElement("sql");
        sql.addAttribute(new Attribute("id", "base_query"));
        // 在这里添加where条件
        XmlElement selectTrimElement = new XmlElement("trim"); //设置trim标签
        selectTrimElement.addAttribute(new Attribute("prefix", "WHERE"));
        selectTrimElement.addAttribute(new Attribute("prefixOverrides", "AND | OR")); //添加where和and
        StringBuilder sb = new StringBuilder();

        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            XmlElement selectNotNullElement = new XmlElement("if"); //$NON-NLS-1$
            sb.setLength(0);
            sb.append("null != ");
            sb.append(introspectedColumn.getJavaProperty());
            selectNotNullElement.addAttribute(new Attribute("test", sb.toString()));
            sb.setLength(0);
            // 添加and
            sb.append(" and ");
            // 添加别名t
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            // 添加等号
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            selectNotNullElement.addElement(new TextElement(sb.toString()));
            selectTrimElement.addElement(selectNotNullElement);
        }

        sql.addElement(selectTrimElement);
        parentElement.addElement(sql);

        // 公用select
        sb.setLength(0);
        sb.append("select ");
        sb.append("* ");//<include refid="Base_Column_List" />
        sb.append("from ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        TextElement selectText = new TextElement(sb.toString());

        // 公用include
        XmlElement include = new XmlElement("include");
        include.addAttribute(new Attribute("refid", "base_query"));

        // 增加selectOne
        XmlElement selectOne = new XmlElement("select");
        selectOne.addAttribute(new Attribute("id", CommonName.SELECTONE));
        selectOne.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        selectOne.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
        selectOne.addElement(selectText);
        selectOne.addElement(include);
        parentElement.addElement(selectOne);

        // 增加selectList
        XmlElement selectList = new XmlElement("select");
        selectList.addAttribute(new Attribute("id", CommonName.SELECTLIST));
        selectList.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        selectList.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
        selectList.addElement(selectText);
        selectList.addElement(include);
        parentElement.addElement(selectList);

        //增加DeleteBatchIds
        XmlElement deleteBatchIdsElement = new XmlElement("delete");
        deleteBatchIdsElement.addAttribute(new Attribute("id", CommonName.DELETEBATCHIDS));

        sb.setLength(0);
        sb.append("delete from ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        deleteBatchIdsElement.addElement(new TextElement(sb.toString()));

        sb.setLength(0);
        sb.append("where ");
        sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedTable.getPrimaryKeyColumns().get(0)));
        sb.append(" in");
        deleteBatchIdsElement.addElement(new TextElement(sb.toString()));

        sb.setLength(0);
        XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("collection", "list"));
        foreach.addAttribute(new Attribute("item", "item"));
        foreach.addAttribute(new Attribute("open", "("));
        foreach.addAttribute(new Attribute("separator", ","));
        foreach.addAttribute(new Attribute("close", ")"));
        foreach.addElement(new TextElement("#{item}"));
        deleteBatchIdsElement.addElement(foreach);
        parentElement.addElement(deleteBatchIdsElement);

        //增加selectBatchIds
        XmlElement selectBatchIdsElement = new XmlElement("select");
        selectBatchIdsElement.addAttribute(new Attribute("id", CommonName.SELECTBATCHIDS));
        selectBatchIdsElement.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        selectBatchIdsElement.addAttribute(new Attribute("parameterType", "java.util.List"));
        selectBatchIdsElement.addElement(selectText);

        sb.setLength(0);
        sb.append("where ");
        sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedTable.getPrimaryKeyColumns().get(0)));
        sb.append(" in");
        selectBatchIdsElement.addElement(new TextElement(sb.toString()));
        selectBatchIdsElement.addElement(foreach);
        parentElement.addElement(selectBatchIdsElement);
    }
}
