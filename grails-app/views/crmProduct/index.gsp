<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmProduct.label', default: 'Product')}"/>
    <title><g:message code="crmProduct.find.title" args="[entityName]"/></title>
    <r:require module="select2"/>
    <r:script>
        $(document).ready(function () {
        });
    </r:script>
</head>

<body>

<div class="row-fluid">
    <div class="span9">

        <crm:header title="crmProduct.find.title" args="[entityName]"/>

        <g:form action="list">

            <div class="row-fluid">

                <div class="span4">
                    <div class="control-group">
                        <label class="control-label">
                            <g:message code="crmProduct.number.label"/>
                        </label>

                        <div class="controls">
                            <g:textField name="number" value="${cmd.number}" class="span12" autofocus=""/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">
                            <g:message code="crmProduct.name.label"/>
                        </label>

                        <div class="controls">
                            <g:textField name="name" value="${cmd.name}" class="span12"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">
                            <g:message code="crmProduct.group.label"/>
                        </label>

                        <div class="controls">
                            <g:textField name="group" value="${cmd.group}" class="span12"/>
                        </div>
                    </div>
                </div>

                <div class="span4">
                    <div class="control-group">
                        <label class="control-label">
                            <g:message code="crmProduct.suppliersNumber.label"/>
                        </label>

                        <div class="controls">
                            <g:textField name="suppliersNumber" value="${cmd.suppliersNumber}"
                                         class="span12"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">
                            <g:message code="crmProduct.supplier.label"/>
                        </label>

                        <div class="controls">
                            <g:textField name="supplier" value="${cmd.supplier}" class="span12"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">
                            <g:message code="crmProduct.barcode.label"/>
                        </label>

                        <div class="controls">
                            <g:textField name="barcode" value="${cmd.barcode}" class="span12"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">
                            <g:message code="crmProduct.customsCode.label"/>
                        </label>

                        <div class="controls">
                            <g:textField name="customsCode" value="${cmd.customsCode}" class="span12"/>
                        </div>
                    </div>
                </div>

                <div class="span4">
                    <div class="control-group">
                        <label class="control-label">
                            <g:message code="crmProduct.price.label"/>
                        </label>

                        <div class="controls">
                            <g:textField name="price" value="${cmd.price}" class="span9"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">
                            <g:message code="crmProduct.weight.label"/>
                        </label>

                        <div class="controls">
                            <g:textField name="weight" value="${cmd.weight}" class="span9"/>
                        </div>
                    </div>
                </div>

            </div>

            <div class="form-actions btn-toolbar">
                <crm:selectionMenu visual="primary">
                    <crm:button action="list" icon="icon-search icon-white" visual="primary"
                                label="crmProduct.button.search.label"/>
                </crm:selectionMenu>
                <crm:button type="link" group="true" action="create" visual="success" icon="icon-file icon-white"
                            label="crmProduct.button.create.label" permission="crmProduct:create"/>

                <g:link action="clearQuery" class="btn btn-link"><g:message code="crmProduct.button.query.clear.label"
                                                                            default="Reset fields"/></g:link>
            </div>

        </g:form>
    </div>

    <div class="span3">
    </div>
</div>

</body>
</html>
