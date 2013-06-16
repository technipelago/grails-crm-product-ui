<%@ page import="grails.plugins.crm.product.CrmProductComposition" %>
<table class="table table-striped">
    <thead>
    <tr>
        <th><g:message code="crmProductComposition.type.label"/></th>
        <th><g:message code="crmProductComposition.quantity.label"/></th>
        <th><g:message code="crmProductComposition.product.label"/></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${result}" var="c">
        <tr>
            <td>
                ${message(code: 'crmProductComposition.type.' + c.typeSymbol, default: c.typeSymbol)}
            </td>
            <td class="right"><g:formatNumber number="${c.quantity}" maxFractionDigits="2"/></td>
            <td>
                <g:link controller="crmProduct" action="show" id="${c.productId}">
                    ${fieldValue(bean: c, field: "product")}
                </g:link>
            </td>
        </tr>
    </g:each>
    </tbody>
</table>
