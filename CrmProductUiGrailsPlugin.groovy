class CrmProductUiGrailsPlugin {
    def groupId = "grails.crm"
    def version = "1.2.0"
    def grailsVersion = "2.2 > *"
    def dependsOn = [:]
    def loadAfter = ['crmProduct']
    def pluginExcludes = [
            "grails-app/conf/ApplicationResources.groovy",
            "src/groovy/grails/plugins/crm/product/TestSecurityDelegate.groovy",
            "grails-app/views/error.gsp"
    ]
    def title = "Grails CRM Product Plugin"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
Provides (admin) user interface for product/item management in Grails CRM
'''
    def documentation = "http://grails.org/plugin/crm-product-ui"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/technipelago/grails-crm-product-ui/issues"]
    def scm = [url: "https://github.com/technipelago/grails-crm-product-ui"]

    def features = {
        crmProduct {
            description "Product Catalogue"
            link controller: "crmProduct", action: "index"
            permissions {
                guest "crmProduct:index,list,show"
                partner "crmProduct:index,list,show"
                user "crmProduct:*"
                admin "crmProduct,crmProductGroup,crmPriceList:*"
            }
            hidden true
        }
    }
}
