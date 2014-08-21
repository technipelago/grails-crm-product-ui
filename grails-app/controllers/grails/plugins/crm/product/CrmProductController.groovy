/*
 * Copyright (c) 2014 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.product

import grails.plugins.crm.core.CrmValidationException
import grails.plugins.crm.core.SearchUtils
import org.springframework.dao.DataIntegrityViolationException
import grails.converters.JSON
import grails.plugins.crm.core.WebUtils
import grails.plugins.crm.core.TenantUtils
import org.springframework.web.servlet.support.RequestContextUtils

import javax.servlet.http.HttpServletResponse
import java.util.concurrent.TimeoutException

class CrmProductController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def crmSecurityService
    def selectionService
    def crmProductService
    def crmContactService
    def userTagService

    def index() {
        // If any query parameters are specified in the URL, let them override the last query stored in session.
        def cmd = new CrmProductQueryCommand()
        def query = params.getSelectionQuery()
        bindData(cmd, query ?: WebUtils.getTenantData(request, 'crmProductQuery'))
        [cmd: cmd]
    }

    def list() {
        def baseURI = new URI('bean://crmProductService/list')
        def query = params.getSelectionQuery()
        def uri

        switch (request.method) {
            case 'GET':
                uri = params.getSelectionURI() ?: selectionService.addQuery(baseURI, query)
                break
            case 'POST':
                uri = selectionService.addQuery(baseURI, query)
                WebUtils.setTenantData(request, 'crmProductQuery', query)
                break
        }

        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def currency = grailsApplication.config.crm.currency.default ?: 'EUR'

        def result
        try {
            result = selectionService.select(uri, params)
            if (result.totalCount == 1 && params.view != 'list') {
                // If we only got one record, show the record immediately.
                redirect action: "show", params: selectionService.createSelectionParameters(uri) + [id: result.head().ident()]
            } else {
                [crmProductList: result, crmProductTotal: result.totalCount, selection: uri, currency: currency]
            }
        } catch (Exception e) {
            flash.error = e.message
            [crmProductList: [], crmProductTotal: 0, selection: uri, currency: currency]
        }
    }

    def clearQuery() {
        WebUtils.setTenantData(request, 'crmProductQuery', null)
        redirect(action: 'index')
    }

    def export() {
        def user = crmSecurityService.getUserInfo()
        def namespace = params.namespace ?: 'crmProduct'
        if (request.post) {
            def filename = message(code: 'crmProduct.label', default: 'Product')
            try {
                def topic = params.topic ?: 'export'
                def result = event(for: namespace, topic: topic,
                        data: params + [user: user, tenant: TenantUtils.tenant, locale: request.locale, filename: filename]).waitFor(60000)?.value
                if (result?.file) {
                    try {
                        WebUtils.inlineHeaders(response, result.contentType, result.filename ?: namespace)
                        WebUtils.renderFile(response, result.file)
                    } finally {
                        result.file.delete()
                    }
                    return null // Success
                } else {
                    flash.warning = message(code: 'crmProduct.export.nothing.message', default: 'Nothing was exported')
                }
            } catch (TimeoutException te) {
                flash.error = message(code: 'crmProduct.export.timeout.message', default: 'Export did not complete')
            } catch (Exception e) {
                log.error("Export event throwed an exception", e)
                flash.error = message(code: 'crmProduct.export.error.message', default: 'Export failed due to an error', args: [e.message])
            }
            redirect(action: "index")
        } else {
            def uri = params.getSelectionURI()
            def layouts = event(for: namespace, topic: (params.topic ?: 'exportLayout'),
                    data: [tenant: TenantUtils.tenant, username: user.username, uri: uri, locale: request.locale]).waitFor(10000)?.values?.flatten()
            [layouts: layouts, selection: uri]
        }
    }

    def create() {
        def metadata = [:]
        metadata.groups = crmProductService.listProductGroups()
        CrmProduct crmProduct = new CrmProduct()

        switch (request.method) {
            case "GET":
                crmProduct = crmProductService.initProduct(crmProduct, params, RequestContextUtils.getLocale(request))
                return [crmProduct: crmProduct, metadata: metadata]
            case "POST":
                def ok = false
                try {
                    crmProduct = crmProductService.saveProduct(crmProduct, params)
                    ok = !crmProduct.hasErrors()
                } catch (CrmValidationException e) {
                    crmProduct = e[0]
                } catch (Exception e) {
                    log.warn("Failed to save crmProduct@$id", e)
                    flash.error = e.message
                }

                if (ok) {
                    def currentUser = crmSecurityService.currentUser
                    event(for: "crmProduct", topic: "created", fork: false, data: [id: crmProduct.id, tenant: crmProduct.tenantId, user: currentUser?.username])
                    flash.success = message(code: 'crmProduct.created.message', args: [message(code: 'crmProduct.label', default: 'Product'), crmProduct.toString()])
                    redirect(action: "show", id: crmProduct.id)
                } else {
                    render(view: "create", model: [crmProduct: crmProduct, metadata: metadata])
                }
                break
        }
    }

    def show(Long id) {
        def crmProduct = crmProductService.getProduct(id)
        if (!crmProduct) {
            flash.error = message(code: 'crmProduct.not.found.message', args: [message(code: 'crmProduct.label', default: 'Product'), id])
            redirect(action: "index")
            return
        }
        // Get all prices sorted by price list and unit/amount.
        def prices = crmProductService.findProductPrices(crmProduct)
        def currency = grailsApplication.config.crm.currency.default ?: 'EUR'
        [crmProduct: crmProduct, prices: prices, currency: currency, selection: params.getSelectionURI()]
    }

    private List getVatOptions() {
        getVatList().collect {
            [label: "${it}%", value: (it / 100).doubleValue()]
        }
    }

    private List<Number> getVatList() {
        grailsApplication.config.crm.currency.vat.list ?: [0]
    }

    def edit(Long id) {
        CrmProduct crmProduct = crmProductService.getProduct(id)
        if (!crmProduct) {
            flash.error = message(code: 'crmProduct.not.found.message', args: [message(code: 'crmProduct.label', default: 'Product'), id])
            redirect(action: "index")
            return
        }
        def metadata = [:]
        metadata.groups = crmProductService.listProductGroups()
        metadata.vatList = getVatOptions()
        // Remove self because it's not a valid relation
        metadata.allProducts = crmProductService.list().findAll { it.id != id }

        switch (request.method) {
            case "GET":
                return [crmProduct: crmProduct, metadata: metadata]
            case "POST":
                if (params.int('version') != null) {
                    if (crmProduct.version > params.int('version')) {
                        crmProduct.errors.rejectValue("version", "crmProduct.optimistic.locking.failure",
                                [message(code: 'crmProduct.label', default: 'Product')] as Object[],
                                "Another user has updated this Product while you were editing")
                        render(view: "edit", model: [crmProduct: crmProduct, metadata: metadata])
                        return
                    }
                }
                def ok = false
                try {
                    crmProduct = crmProductService.saveProduct(crmProduct, params)
                    ok = !crmProduct.hasErrors()
                } catch (CrmValidationException e) {
                    crmProduct = (CrmProduct) e[0]
                } catch (Exception e) {
                    // Re-attach object to this Hibernate session to avoid problems with uninitialized associations.
                    if (!crmProduct.isAttached()) {
                        crmProduct.discard()
                        crmProduct.attach()
                    }
                    log.warn("Failed to save crmProduct@$id", e)
                    flash.error = e.message
                }

                if (ok) {
                    def currentUser = crmSecurityService.currentUser
                    event(for: "crmProduct", topic: "updated", fork: false, data: [id: crmProduct.id, tenant: crmProduct.tenantId, user: currentUser?.username])
                    flash.success = message(code: 'crmProduct.updated.message', args: [message(code: 'crmProduct.label', default: 'Product'), crmProduct.toString()])
                    redirect(action: "show", id: crmProduct.id)
                } else {
                    render(view: "edit", model: [crmProduct: crmProduct, metadata: metadata])
                }
                break
        }
    }

    def delete(Long id) {
        def crmProduct = crmProductService.getProduct(id)
        if (!crmProduct) {
            flash.error = message(code: 'crmProduct.not.found.message', args: [message(code: 'crmProduct.label', default: 'Product'), id])
            redirect(action: "index")
            return
        }

        try {
            def tombstone
            CrmProduct.withTransaction {
                CrmProductComposition.findAllByProduct(crmProduct)*.delete()
                // This can be removed when we depend on crm-product >= 2.0.2
                tombstone = crmProductService.deleteProduct(crmProduct)
            }
            flash.warning = message(code: 'crmProduct.deleted.message', args: [message(code: 'crmProduct.label', default: 'Product'), tombstone])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'crmProduct.not.deleted.message', args: [message(code: 'crmProduct.label', default: 'Product'), id])
            redirect(action: "show", id: id)
        }
    }

    def addPrice(Long id) {
        def crmProduct = id ? crmProductService.getProduct(id) : null
        def vat = grailsApplication.config.crm.currency.vat.default ?: 0
        render template: 'price', model: [row: 0, bean: new CrmProductPrice(product: crmProduct, fromAmount: 1, inPrice: 0, outPrice: 0, vat: vat), vatList: getVatOptions()]
    }

    def deletePrice(Long id) {
        def price = CrmProductPrice.get(id)
        if (price) {
            def product = price.product
            if (product.tenantId == TenantUtils.tenant) {
                try {
                    price.delete(flush: true)
                    render 'true'
                } catch (Exception e) {
                    log.error("Failed to delete CrmProductPrice($id)", e)
                    render 'false'
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
    }

    def addRelated(Long id) {
        def crmProduct = id ? crmProductService.getProduct(id) : null
        def productList = crmProductService.list().findAll { it.id != id }
        // Remove self because it's not a valid relation
        render template: 'related', model: [row: 0, bean: new CrmProductComposition(product: crmProduct, quantity: 1, type: CrmProductComposition.INCLUDES), productList: productList]
    }

    def deleteRelated(Long id) {
        def comp = CrmProductComposition.get(id)
        if (comp) {
            def product = comp.product
            if (product.tenantId == TenantUtils.tenant) {
                try {
                    comp.delete(flush: true)
                    render 'true'
                } catch (Exception e) {
                    log.error("Failed to delete CrmProductComposition($id)", e)
                    render 'false'
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
    }

    def createFavorite(Long id) {
        def crmProduct = crmProductService.getProduct(id)
        if (!crmProduct) {
            flash.error = message(code: 'crmProduct.not.found.message', args: [message(code: 'crmProduct.label', default: 'Product'), id])
            redirect action: 'index'
            return
        }
        userTagService.tag(crmProduct, grailsApplication.config.crm.tag.favorite, crmSecurityService.currentUser?.username, TenantUtils.tenant)

        redirect(action: 'show', id: id)
    }

    def deleteFavorite(Long id) {
        def crmProduct = crmProductService.getProduct(id)
        if (!crmProduct) {
            flash.error = message(code: 'crmProduct.not.found.message', args: [message(code: 'crmProduct.label', default: 'Product'), id])
            redirect action: 'index'
            return
        }
        userTagService.untag(crmProduct, grailsApplication.config.crm.tag.favorite, crmSecurityService.currentUser?.username, TenantUtils.tenant)
        redirect(action: 'show', id: id)
    }

    def autocomplete(String q, int limit, int max, String key) {
        def result = CrmProduct.createCriteria().list() {
            projections {
                property('name')
                if (key) {
                    property(key)
                }
            }
            if (q) {
                def filter = SearchUtils.wildcard(q)
                or {
                    ilike('number', filter)
                    ilike('name', filter)
                }
            }
            eq('tenantId', TenantUtils.tenant)
            maxResults(limit ?: (max ?: 10))
        }
        WebUtils.shortCache(response)
        render result as JSON
    }

    def autocompleteGroup() {
        def result = CrmProductGroup.withCriteria(params) {
            projections {
                property('name')
            }
            eq('tenantId', TenantUtils.tenant)
            if (params.q) {
                or {
                    ilike('name', SearchUtils.wildcard(params.q))
                    ilike('param', SearchUtils.wildcard(params.q))
                }
            }
        }
        WebUtils.shortCache(response)
        render result as JSON
    }

    def autocompleteSupplier() {
        def result
        if (crmContactService != null) {
            result = crmContactService.list([name: params.q], [max: 100]).collect { [it.name, it.id] }
        } else {
            result = CrmProduct.createCriteria().list() {
                projections {
                    distinct('supplierName')
                }
                eq('tenantId', TenantUtils.tenant)
                if (params.q) {
                    ilike('supplierName', SearchUtils.wildcard(params.q))
                }
                maxResults 100
            }.sort()
        }
        WebUtils.shortCache(response)
        render result as JSON
    }

}
