package com.tripbutler.security.cache

import org.springframework.beans.factory.InitializingBean
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Element;
import net.sf.ehcache.Ehcache;
import org.springframework.util.Assert
import org.springframework.dao.DataRetrievalFailureException
import com.tripbutler.user.ApiAuthentication;


class EhCacheBasedAuthenticationCache implements InitializingBean {
    //~ Static fields/initializers =====================================================================================

    private static final Log logger = LogFactory.getLog(EhCacheBasedAuthenticationCache.class)

    //~ Instance fields ================================================================================================

    Ehcache cache

    //~ Methods ========================================================================================================

    void afterPropertiesSet() {
        Assert.notNull(cache, "cache mandatory")
    }

	ApiAuthentication getApiAuthenticationFromCache(String accessKey) {
        Element element
        try {
            element = cache.get(accessKey)
        } catch (CacheException cacheException) {
            throw new DataRetrievalFailureException("Cache failure: " + cacheException.getMessage())
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Cache hit: " + (element != null) + "; accessKey: " + accessKey)
        }

		return element?.getValue()
	}

	void putApiAuthenticationInCache(ApiAuthentication apiAuthentication) {
        Element element = new Element(apiAuthentication.accessKey, apiAuthentication)

        if (logger.isDebugEnabled()) {
            logger.debug("Cache put: " + element.getKey())
        }

        cache.put(element)
	}
}
