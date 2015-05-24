/*
 * Copyright (c) 2015 Goran Ehrsson.
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

class CrmProductUiGrailsPlugin {
    def groupId = ""
    def version = "2.4.0"
    def grailsVersion = "2.2 > *"
    def dependsOn = [:]
    def loadAfter = ['crmProduct']
    def pluginExcludes = [
            "grails-app/conf/ApplicationResources.groovy",
            "src/groovy/grails/plugins/crm/product/TestSecurityDelegate.groovy",
            "grails-app/views/error.gsp"
    ]
    def title = "GR8 CRM Product Management UI"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
Provides (admin) user interface for product/item management in GR8 CRM applications.
'''
    def documentation = "http://gr8crm.github.io/plugins/crm-product-ui/"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/technipelago/grails-crm-product-ui/issues"]
    def scm = [url: "https://github.com/technipelago/grails-crm-product-ui"]

    def features = {
        crmProductUi {
            //dependsOn "crmProduct"
            description "Product Management UI"
            link controller: "crmProduct", action: "index"
            permissions {
                guest "crmProduct:index,list,show,autocompleteGroup,clearQuery"
                partner "crmProduct:index,list,show,autocompleteGroup,clearQuery"
                user "crmProduct:*"
                admin "crmProduct,crmProductGroup,crmPriceList:*"
            }
            hidden true
        }
    }
}
