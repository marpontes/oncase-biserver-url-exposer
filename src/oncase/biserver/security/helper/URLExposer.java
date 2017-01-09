package oncase.biserver.security.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;


public class URLExposer implements BeanFactoryPostProcessor {

	private FilterInvocationSecurityMetadataSource securityMetadataSource;
	private FilterChainProxy proxy;


	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("**URLExposer.postProcessBeanFactory(...)**");

		FilterSecurityInterceptor customInterceptor = getCustomInterceptor();
		initProxy();

		List<SecurityFilterChain> list = proxy.getFilterChains();
		Iterator<SecurityFilterChain> it = list.iterator();
		
		proxy = new FilterChainProxy(list);
		while(it.hasNext()){
			DefaultSecurityFilterChain fc = (DefaultSecurityFilterChain) it.next();
			if(fc.getRequestMatcher() instanceof AnyRequestMatcher){
				ArrayList<Filter> filters = (ArrayList<Filter>) fc.getFilters();
				int index = filters.size() - 1;
				filters.add(index, customInterceptor);
			}
		}

	}
	
	/**
	 * This method gets the authenticationManager bean from PentahoSystem
	 */
	private void initProxy() {
		proxy = PentahoSystem.get(FilterChainProxy.class, "filterChainProxy",
				PentahoSessionHolder.getSession());
	}
	
	private FilterSecurityInterceptor getCustomInterceptor() {
		ProviderManager authenticationManager = PentahoSystem.get(ProviderManager.class,
				"authenticationManager", PentahoSessionHolder.getSession());
		FilterSecurityInterceptor interceptor = new FilterSecurityInterceptor();
		interceptor.setSecurityMetadataSource(securityMetadataSource);
		interceptor.setAuthenticationManager((AuthenticationManager) authenticationManager);
		return interceptor;
	}

	public FilterInvocationSecurityMetadataSource getSecurityMetadataSource() {
		return securityMetadataSource;
	}

	public void setSecurityMetadataSource(FilterInvocationSecurityMetadataSource securityMetadataSource) {
		this.securityMetadataSource = securityMetadataSource;
	}
}
