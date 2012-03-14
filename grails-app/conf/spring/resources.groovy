import org.springframework.cache.ehcache.EhCacheFactoryBean
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean

beans = {
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