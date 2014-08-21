<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmProduct.label', default: 'Product')}"/>
    <title><g:message code="crmProduct.list.title" args="[entityName]"/></title>
</head>

<body>

<crm:header title="crmProduct.list.title" subtitle="crmProduct.totalCount.label"
            args="[entityName, crmProductTotal]">
</crm:header>

<table class="table table-striped">
    <thead>
    <tr>
        <crm:sortableColumn property="number"
                            title="${message(code: 'crmProduct.number.label', default: 'Number')}"/>

        <crm:sortableColumn property="name"
                            title="${message(code: 'crmProduct.name.label', default: 'Name')}"/>

        <crm:sortableColumn property="group.orderIndex"
                            title="${message(code: 'crmProduct.group.label', default: 'Group')}"/>

        <crm:sortableColumn property="supplier.name"
                            title="${message(code: 'crmProduct.supplier.label', default: 'Supplier')}"/>
        <th class="money"><g:message code="crmProductPrice.priceVAT.label" default="Price inc. VAT"/></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${crmProductList}" var="crmProduct">
        <g:set var="price" value="${crmProduct.getProductPrice()}"/>
        <tr>

            <td title="${crmProduct.displayNumber?.encodeAsHTML()}">
                <g:link controller="crmProduct" action="show" id="${crmProduct.id}">
                    ${fieldValue(bean: crmProduct, field: "number")}
                </g:link>
            </td>

            <td title="${crmProduct.displayName?.encodeAsHTML()}">
                <g:link controller="crmProduct" action="show" id="${crmProduct.id}">
                    ${fieldValue(bean: crmProduct, field: "name")}
                </g:link>
            </td>

            <td>
                ${fieldValue(bean: crmProduct, field: "group")}
            </td>

            <td>
                <g:if test="${crmProduct.supplierId}">
                    <g:link mapping="crm-contact-show" id="${crmProduct.supplierId}">
                        <g:fieldValue bean="${crmProduct}" field="supplierName"/>
                    </g:link>
                </g:if>
                <g:else>
                    <g:fieldValue bean="${crmProduct}" field="supplierName"/>
                </g:else>
            </td>
            <td class="money">
                <g:if test="${price}">
                    <g:formatNumber number="${price.priceVAT}"
                                    type="currency" currencyCode="${currency}"
                                    maxFractionDigits="2"/> / ${price.unit}
                </g:if>
            </td>

        </tr>
    </g:each>
    </tbody>
</table>

<crm:paginate total="${crmProductTotal}"/>

<div class="form-actions btn-toolbar">
    <crm:selectionMenu visual="primary"/>

    <g:if test="${crmProductTotal}">
        <select:link action="export" accesskey="p" selection="${selection}" class="btn btn-info">
            <i class="icon-print icon-white"></i>
            <g:message code="crmProduct.button.export.label" default="Print/Export"/>
        </select:link>
    </g:if>

    <div class="btn-group">
        <crm:button type="link" action="create" visual="success" icon="icon-file icon-white"
                    label="crmProduct.button.create.label" permission="crmProduct:create"/>
    </div>
</div>

</body>
</html>
