<%@ page import="grails.plugins.crm.product.CrmProductComposition" defaultCodec="html" %>
<tr>
    <td>
        <g:select name="compositions[${row}].typeSymbol"
                  from="${CrmProductComposition.TYPE_SYMBOLS.keySet()}"
                  value="${bean.typeSymbol}" valueMessagePrefix="crmProductComposition.type"
                  class="input-medium"/>
    </td>
    <td class="right"><g:textField name="compositions[${row}].quantity"
                                   value="${fieldValue(bean: bean, field: 'quantity')}"
                                   class="input-small"/></td>
    <td>
        <g:select name="compositions[${row}].product.id" from="${productList}"
                  value="${bean.product?.id}" optionKey="id" class="input-xlarge"/>

    </td>
    <td>
        <button type="button" class="btn btn-danger btn-small btn-delete" tabindex="-1" onclick="deleteComposition(this, ${bean.id ?: 'undefined'})"><i class="icon-trash icon-white"></i></button>
    </td>
</tr>