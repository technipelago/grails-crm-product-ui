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

import org.springframework.dao.DataIntegrityViolationException
import javax.servlet.http.HttpServletResponse

class CrmPriceListController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    static navigation = [
            [group: 'admin',
                    order: 620,
                    title: 'crmPriceList.label',
                    action: 'index'
            ]
    ]

    def selectionService
    def crmProductService

    def domainClass = CrmPriceList

    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        def baseURI = new URI('gorm://crmPriceList/list')
        def query = params.getSelectionQuery()
        def uri

        switch (request.method) {
            case 'GET':
                uri = params.getSelectionURI() ?: selectionService.addQuery(baseURI, query)
                break
            case 'POST':
                uri = selectionService.addQuery(baseURI, query)
                grails.plugins.crm.core.WebUtils.setTenantData(request, 'crmPriceListQuery', query)
                break
        }

        params.max = Math.min(params.max ? params.int('max') : 20, 100)

        try {
            def result = selectionService.select(uri, params)
            [crmPriceListList: result, crmPriceListTotal: result.totalCount, selection: uri]
        } catch (Exception e) {
            flash.error = e.message
            [crmPriceListList: [], crmPriceListTotal: 0, selection: uri]
        }
    }

    def create() {
        def crmPriceList = crmProductService.createPriceList(params)
        switch (request.method) {
            case 'GET':
                return [crmPriceList: crmPriceList]
            case 'POST':
                if (!crmPriceList.save(flush: true)) {
                    render view: 'create', model: [crmPriceList: crmPriceList]
                    return
                }

                flash.success = message(code: 'crmPriceList.created.message', args: [message(code: 'crmPriceList.label', default: 'Price List'), crmPriceList.toString()])
                redirect action: 'list'
                break
        }
    }

    def edit() {
        switch (request.method) {
            case 'GET':
                def crmPriceList = domainClass.get(params.id)
                if (!crmPriceList) {
                    flash.error = message(code: 'crmPriceList.not.found.message', args: [message(code: 'crmPriceList.label', default: 'Price List'), params.id])
                    redirect action: 'list'
                    return
                }

                return [crmPriceList: crmPriceList]
            case 'POST':
                def crmPriceList = domainClass.get(params.id)
                if (!crmPriceList) {
                    flash.error = message(code: 'crmPriceList.not.found.message', args: [message(code: 'crmPriceList.label', default: 'Price List'), params.id])
                    redirect action: 'list'
                    return
                }

                if (params.version) {
                    def version = params.version.toLong()
                    if (crmPriceList.version > version) {
                        crmPriceList.errors.rejectValue('version', 'crmPriceList.optimistic.locking.failure',
                                [message(code: 'crmPriceList.label', default: 'Price List')] as Object[],
                                "Another user has updated this Type while you were editing")
                        render view: 'edit', model: [crmPriceList: crmPriceList]
                        return
                    }
                }

                crmPriceList.properties = params

                if (!crmPriceList.save(flush: true)) {
                    render view: 'edit', model: [crmPriceList: crmPriceList]
                    return
                }

                flash.success = message(code: 'crmPriceList.updated.message', args: [message(code: 'crmPriceList.label', default: 'Price List'), crmPriceList.toString()])
                redirect action: 'list'
                break
        }
    }

    def delete() {
        def crmPriceList = domainClass.get(params.id)
        if (!crmPriceList) {
            flash.error = message(code: 'crmPriceList.not.found.message', args: [message(code: 'crmPriceList.label', default: 'Price List'), params.id])
            redirect action: 'list'
            return
        }

        if (isInUse(crmPriceList)) {
            render view: 'edit', model: [crmPriceList: crmPriceList]
            return
        }

        try {
            def tombstone = crmPriceList.toString()
            crmPriceList.delete(flush: true)
            flash.warning = message(code: 'crmPriceList.deleted.message', args: [message(code: 'crmPriceList.label', default: 'Price List'), tombstone])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'crmPriceList.not.deleted.message', args: [message(code: 'crmPriceList.label', default: 'Price List'), params.id])
            redirect action: 'edit', id: params.id
        }
    }

    private boolean isInUse(CrmPriceList list) {
        def count = CrmProductPrice.countByPriceList(list)
        def rval = false
        if (count) {
            flash.error = message(code: "crmPriceList.delete.error.reference", args:
                    [message(code: 'crmPriceList.label', default: 'Price List'),
                            message(code: 'crmProduct.label', default: 'Product'), count],
                    default: "This {0} is used by {1} {2}")
            rval = true
        }

        return rval
    }

    def moveUp(Long id) {
        def target = domainClass.get(id)
        if (target) {
            def sort = target.orderIndex
            def prev = domainClass.createCriteria().list([sort: 'orderIndex', order: 'desc']) {
                lt('orderIndex', sort)
                maxResults 1
            }?.find {it}
            if (prev) {
                domainClass.withTransaction {tx ->
                    target.orderIndex = prev.orderIndex
                    prev.orderIndex = sort
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
        redirect action: 'list'
    }

    def moveDown(Long id) {
        def target = domainClass.get(id)
        if (target) {
            def sort = target.orderIndex
            def next = domainClass.createCriteria().list([sort: 'orderIndex', order: 'asc']) {
                gt('orderIndex', sort)
                maxResults 1
            }?.find {it}
            if (next) {
                domainClass.withTransaction {tx ->
                    target.orderIndex = next.orderIndex
                    next.orderIndex = sort
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
        redirect action: 'list'
    }
}
