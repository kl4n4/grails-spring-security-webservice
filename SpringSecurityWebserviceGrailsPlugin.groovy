import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import com.tripbutler.security.RequestHeaderAuthenticationEntryPoint
import com.tripbutler.security.RequestHeaderAuthenticationUserDetailsService
import com.tripbutler.security.RequestHeaderAuthenticationFilter
import com.tripbutler.security.RequestHeaderAuthenticationProvider
import com.tripbutler.security.cache.EhCacheBasedAuthenticationCache
import org.springframework.cache.ehcache.EhCacheFactoryBean
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition

class SpringSecurityWebserviceGrailsPlugin {
	// the plugin version
	def version = "0.1"
	// the version or versions of Grails the plugin is designed for
	def grailsVersion = "2.0 > *"
	// the other plugins this plugin depends on
	def dependsOn = [:]
	// resources that are excluded from plugin packaging
	def pluginExcludes = [
			"grails-app/views/error.gsp"
	]

	// URL to the plugin's documentation

	String author = 'Thomas Klaner'
	String authorEmail = 'thomas@iwa-tech.com'
	String title = 'Spring Security Webservice Plugin'
	String description = 'Grails Plugin to secure web services using spring security'
	String documentation = 'http://grails.org/plugin/spring-security-webservice'

	String license = 'APACHE'
	//def organization = [name: 'IWAtech', url: 'http://www.iwa-tech.com/']
	def issueManagement = [system: 'GitHub', url: 'https://github.com/grails-plugins/grails-spring-security-webservice/issues']
	def scm = [url: 'https://github.com/grails-plugins/grails-spring-security-webservice']

	// make sure the filter is after the Spring Security filter chain filter
	def getWebXmlFilterOrder() {
		def FilterManager = getClass().getClassLoader().loadClass('grails.plugin.webxml.FilterManager')
		[ responseSigningFilter: FilterManager.GRAILS_WEB_REQUEST_POSITION + 110 ]
	}

	def doWithSpring = {
		def conf = SpringSecurityUtils.securityConfig
		if (!conf || !conf.active) {
			return
		}

		SpringSecurityUtils.loadSecondaryConfig('DefaultWebserviceSecurityConfig')

		// have to reload again after overlaying DefaultWebserviceSecurityConfig
		conf = SpringSecurityUtils.securityConfig

		if (!conf.webserviceSecurity.active) {
			return
		}

		println 'Configuring Spring Security for Web Services ...'
		SpringSecurityUtils.registerProvider('requestHeaderAuthenticationProvider')
		SpringSecurityUtils.registerFilter('requestHeaderAuthenticationFilter', SecurityFilterPosition.BASIC_AUTH_FILTER)

		authenticationEntryPoint(RequestHeaderAuthenticationEntryPoint)

		requestHeaderAuthenticationUserDetailsService(RequestHeaderAuthenticationUserDetailsService) {
			grailsApplication = ref('grailsApplication')
		}

		requestHeaderAuthenticationFilter(RequestHeaderAuthenticationFilter) {
			authenticationManager = ref("authenticationManager")
			rememberMeServices = ref("rememberMeServices")
			springSecurityService = ref("securityService")
			authenticationEntryPoint = ref('authenticationEntryPoint')
			signedRequestHeaderUtil = ref('signedRequestHeaderUtil')
			anonymousAuthenticationFilter = ref('anonymousAuthenticationFilter')
		}

		requestHeaderAuthenticationProvider(RequestHeaderAuthenticationProvider) {
			authenticationCache = ref('authenticationCache')
			signedRequestHeaderUtil = ref('signedRequestHeaderUtil')
			userDetailsService = ref("requestHeaderAuthenticationUserDetailsService")
		}

		/**
		 * Cache (for Authentication)
		 */
		authenticationCache(EhCacheBasedAuthenticationCache) {
			cache = ref('securityAuthenticationCache')
		}
		securityAuthenticationCache(EhCacheFactoryBean) {
			cacheManager = ref('cacheManager')
			cacheName = 'authenticationCache'
		}
		cacheManager(EhCacheManagerFactoryBean)
	}

	def doWithDynamicMethods = { ctx ->
		// TODO Implement registering dynamic methods to classes (optional)
	}

	def doWithApplicationContext = { applicationContext ->
		// TODO Implement post initialization spring config (optional)
	}

	def onChange = { event ->
		// TODO Implement code that is executed when any artefact that this plugin is
		// watching is modified and reloaded. The event contains: event.source,
		// event.application, event.manager, event.ctx, and event.plugin.
	}

	def onConfigChange = { event ->
		// TODO Implement code that is executed when the project configuration changes.
		// The event is the same as for 'onChange'.
	}

	def onShutdown = { event ->
		// TODO Implement code that is executed when the application shuts down (optional)
	}
}
