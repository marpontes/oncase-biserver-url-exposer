package oncas.biserver.security.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.security.intercept.web.FilterInvocationDefinitionSource;
import org.springframework.security.intercept.web.FilterInvocationDefinitionSourceEditor;
import org.springframework.security.intercept.web.FilterSecurityInterceptor;
import org.springframework.security.providers.ProviderManager;
import org.springframework.security.util.FilterChainProxy;
import org.springframework.security.vote.AffirmativeBased;

@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class URLExposer implements BeanFactoryPostProcessor {

	private String filterChainUrlPattern;
	private String interceptorDefifnintion;
	private AffirmativeBased httpRequestAccessDecisionManager;
	private ProviderManager authenticationManager;
	private FilterSecurityInterceptor interceptor;
	private FilterChainProxy proxy;

	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		
		FilterSecurityInterceptor customInterceptor = getCustomInterceptor();
		initProxy();
		updateProxy(customInterceptor);
		
		String token = PentahoSystem.get(String.class, "jcrPreAuthenticationToken", PentahoSessionHolder.getSession());
		System.out.println("########## PRE-AUTH-TOKEN ##########\n\n");
		System.out.println(token);
		System.out.println("\n\n########## PRE-AUTH-TOKEN ##########");
		

	}
	
	/**
	 * This method updates the system's FilterChainProxy injecting our
	 * URL chain to its collection
	 * @param customInterceptor our interceptor that is going to replace the 
	 * 							system's interceptor in our custom entry
	 *              			
	 */
	private void updateProxy(FilterSecurityInterceptor customInterceptor){
		
		LinkedHashMap systemMap = getSystemChainMap();
		LinkedHashMap hma = new LinkedHashMap();
		ArrayList defaultRule = getDefaultURLChain(proxy);
		int index = defaultRule.size() - 1;
		defaultRule.add(index, customInterceptor);
		hma.put(filterChainUrlPattern, defaultRule);
		hma.putAll(systemMap);
		proxy.setFilterChainMap(hma);
		
	}

	/**
	 * This method is responsible for retrieving a LinkedHashMap from
	 * the initialized FilterChainProxy
	 * 
	 * @return A linkedHashMap representing the FilterChain map
	 * from the system's FilterChainProxy
	 */
	private LinkedHashMap getSystemChainMap() {
		return (LinkedHashMap) proxy.getFilterChainMap();
	}

	/**
	 * This method is responsible for retrieving an ArrayList containing
	 * the sorted objects on the chain for the url /**
	 * @return This method 
	 */
	private ArrayList getDefaultURLChain(FilterChainProxy proxy) {

		LinkedHashMap chainMap = getSystemChainMap();

		Iterator pit = chainMap.keySet().iterator();

		ArrayList defaultUrlChain = null;

		while (pit.hasNext()) {
			Object item = pit.next();
			if ("/**".equals(item)) {
				defaultUrlChain = new ArrayList((ArrayList) chainMap.get(item));
			}
		}

		return defaultUrlChain;
	}

	/**
	 * This method creates a FilterInvocationDefinitionSource based on our
	 * textual rules.
	 * 
	 * @return A FilterInvocationDefinitionSource based on our textual rules
	 * 
	 * @todo We're using a deprecated Class and this should be replaced in the
	 *       future versions
	 */
	private FilterInvocationDefinitionSource getObjectDefinitionSource(
			String txt) {
		FilterInvocationDefinitionSourceEditor editor = new FilterInvocationDefinitionSourceEditor();

		editor.setAsText(txt);
		return (FilterInvocationDefinitionSource) editor.getValue();
	}

	/**
	 * This method creates a new FilterSecurityInterceptor using all biserver
	 * default configuration but the ObjectDefinitionSource
	 * 
	 * @return A FilterSecurityInterceptor using all biserver default
	 *         configuration but the ObjectDefinitionSource
	 */
	private FilterSecurityInterceptor getCustomInterceptor() {

		if (interceptor == null) {

			initHttpRequestAccessDecisionManager();
			initAuthenticationManager();

			FilterInvocationDefinitionSource ods = getObjectDefinitionSource(interceptorDefifnintion);
			interceptor = new FilterSecurityInterceptor();
			interceptor.setAuthenticationManager(authenticationManager);
			interceptor
					.setAccessDecisionManager(httpRequestAccessDecisionManager);
			interceptor.setObjectDefinitionSource(ods);
		}

		return interceptor;

	}

	/**
	 * This method gets the httpRequestAccessDecisionManager bean from
	 * PentahoSystem
	 */
	private void initHttpRequestAccessDecisionManager() {
		httpRequestAccessDecisionManager = PentahoSystem.get(
				AffirmativeBased.class, "httpRequestAccessDecisionManager",
				PentahoSessionHolder.getSession());
	}

	/**
	 * This method gets the authenticationManager bean from PentahoSystem
	 */
	private void initAuthenticationManager() {
		authenticationManager = PentahoSystem.get(ProviderManager.class,
				"authenticationManager", PentahoSessionHolder.getSession());
	}

	/**
	 * This method gets the authenticationManager bean from PentahoSystem
	 */
	private void initProxy() {
		proxy = PentahoSystem.get(FilterChainProxy.class, "filterChainProxy",
				PentahoSessionHolder.getSession());
	}

	/*
	 * getters&setters -----------------------------------------------------
	 */

	public String getFilterChainUrlPattern() {
		return filterChainUrlPattern;
	}

	public void setFilterChainUrlPattern(String filterChainUrlPattern) {
		this.filterChainUrlPattern = filterChainUrlPattern;
	}

	public String getInterceptorDefifnintion() {
		return interceptorDefifnintion;
	}

	public void setInterceptorDefifnintion(String interceptorDefifnintion) {
		this.interceptorDefifnintion = interceptorDefifnintion;
	}

	public AffirmativeBased getHttpRequestAccessDecisionManager() {
		return httpRequestAccessDecisionManager;
	}

	public void setHttpRequestAccessDecisionManager(
			AffirmativeBased httpRequestAccessDecisionManager) {
		this.httpRequestAccessDecisionManager = httpRequestAccessDecisionManager;
	}

	public ProviderManager getAuthenticationManager() {
		return authenticationManager;
	}

	public void setAuthenticationManager(ProviderManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public FilterSecurityInterceptor getInterceptor() {
		return interceptor;
	}

	public void setInterceptor(FilterSecurityInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	public FilterChainProxy getProxy() {
		return proxy;
	}

	public void setProxy(FilterChainProxy proxy) {
		this.proxy = proxy;
	}

}
