<%@ page import="grails.plugins.crm.product.CrmProduct" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmProduct.label', default: 'Product')}"/>
    <title><g:message code="crmProduct.create.title" args="[entityName]"/></title>
    <r:require modules="autocomplete"/>
    <r:script>
        $(document).ready(function () {
            // Supplier.
            $("input[name='supplierName']").autocomplete("${createLink(action: 'autocompleteSupplier')}", {
                remoteDataType: 'json',
                useCache: false,
                filter: false,
                preventDefaultReturn: true,
                minChars: 1,
                selectFirst: true,
                onItemSelect: function(item) {
                    var id = item.data[0];
                    $("#supplierId").val(id);
                    $("header h1 small").text(item.value);
                },
                onNoMatch: function() {
                    $("#supplierId").val('');
                    $("header h1 small").text($("input[name='supplierName']").val());
                }
            });
        });
    </r:script>
</head>

<body>

<crm:header title="crmProduct.create.title" args="[entityName]"/>

<g:hasErrors bean="${crmProduct}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${crmProduct}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:form action="create">

    <div class="row-fluid">

        <div class="span4">
            <div class="row-fluid">
                <div class="control-group">
                    <label class="control-label">
                        <g:message code="crmProduct.number.label"/>
                    </label>

                    <div class="controls">
                        <g:textField name="number" value="${crmProduct.number}" class="span12" autofocus=""/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">
                        <g:message code="crmProduct.name.label"/>
                    </label>

                    <div class="controls">
                        <g:textField name="name" value="${crmProduct.name}" class="span12"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">
                        <g:message code="crmProduct.displayNumber.label"/>
                    </label>

                    <div class="controls">
                        <g:textField name="displayNumber" value="${crmProduct.displayNumber}" class="span12"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">
                        <g:message code="crmProduct.displayName.label"/>
                    </label>

                    <div class="controls">
                        <g:textField name="displayName" value="${crmProduct.displayName}" class="span12"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">
                        <g:message code="crmProduct.description.label"/>
                    </label>

                    <div class="controls">
                        <g:textArea name="description" value="${crmProduct.description}" rows="4" cols="50"
                                    class="span12"/>
                    </div>
                </div>
            </div>
        </div>

        <div class="span4">
            <div class="row-fluid">
                <div class="control-group">
                    <label class="control-label">
                        <g:message code="crmProduct.supplier.label"/>
                    </label>

                    <div class="controls">
                        <g:textField name="supplierName" value="${crmProduct.supplierName}" class="span12"
                                     autocomplete="off"/>
                        <g:hiddenField name="supplierId" value="${crmProduct.supplierId}"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">
                        <g:message code="crmProduct.suppliersNumber.label"/>
                    </label>

                    <div class="controls">
                        <g:textField name="suppliersNumber" value="${crmProduct.suppliersNumber}" class="span9"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">
                        <g:message code="crmProduct.group.label"/>
                    </label>

                    <div class="controls">
                        <g:select name="group.id" from="${metadata.groups}" optionKey="id"
                                  value="${crmProduct.group?.id}"/>
                    </div>
                </div>
            </div>
        </div>

        <div class="span4">
            <div class="row-fluid">
                <div class="control-group">
                    <label class="control-label">
                        <g:message code="crmProduct.barcode.label"/>
                    </label>

                    <div class="controls">
                        <g:textField name="barcode" value="${crmProduct.barcode}" class="span6"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">
                        <g:message code="crmProduct.customsCode.label"/>
                    </label>

                    <div class="controls">
                        <g:textField name="customsCode" value="${crmProduct.customsCode}" class="span6"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">
                        <g:message code="crmProduct.weight.label"/>
                    </label>

                    <div class="controls">
                        <g:textField name="weight" value="${crmProduct.weight}" class="span6"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">
                        <g:message code="crmProduct.enabled.label"/>
                    </label>

                    <div class="controls">
                        <g:checkBox name="enabled" value="true" checked="${crmProduct.enabled}"/>
                    </div>
                </div>
            </div>
        </div>

    </div>

    <div class="form-actions">
        <crm:button visual="success" icon="icon-ok icon-white" label="crmProduct.button.save.label"/>
    </div>

</g:form>

</body>
</html>
