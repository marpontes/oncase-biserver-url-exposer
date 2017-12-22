package oncase.biserver.security.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.spring.PublishedBeanRegistry;
import org.pentaho.platform.web.http.context.PentahoSolutionSpringApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


public class URLExposer implements BeanFactoryPostProcessor {

	private FilterInvocationSecurityMetadataSource securityMetadataSource;
	

	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("\n\n\n**URLExposer.postProcessBeanFactory(...)**");
		

		PentahoSolutionSpringApplicationContext appContext = getSpringContext();
		GenericBeanDefinition fcp = (GenericBeanDefinition) appContext.getBeanFactory().getBeanDefinition("filterChainProxy");
			
		ConstructorArgumentValues argValues = fcp.getConstructorArgumentValues();
		List<ValueHolder> indexedArgValues = argValues.getGenericArgumentValues();
		Iterator it = indexedArgValues.iterator();
		ValueHolder constructorValueHolder = new ValueHolder(new Object());
		
		//TODO: find a better way yo assign the value holder
		while(it.hasNext()){
			constructorValueHolder = (ValueHolder) it.next();
		}
		BeanDefinitionHolder constructorBeanDefHolder = (BeanDefinitionHolder) constructorValueHolder.getValue();
		GenericBeanDefinition listBeanDefinition = (GenericBeanDefinition) constructorBeanDefHolder.getBeanDefinition();

		
		
		/* Quest for the list */
		List<SecurityFilterChain> lsf = new ArrayList<SecurityFilterChain>();
		
		System.out.println("..................");
				
		FilterChainProxy fcpObject = (FilterChainProxy) appContext.getBean("filterChainProxy");
		lsf = new ArrayList<SecurityFilterChain>( fcpObject.getFilterChains() );
		lsf.add(0, getCustomFilterChain());
		argValues.clear();
		argValues.addGenericArgumentValue(getNewProxyList(), "java.util.List");
		fcp.setConstructorArgumentValues(argValues);
		FilterChainProxy proxy2 = new FilterChainProxy(lsf);
		/*/Quest for the list */
		
		
		appContext.getBeanFactory().registerSingleton(proxy2.getClass().getCanonicalName(), proxy2);
		
		appContext.getBeanFactory().getSingleton(proxy2.getClass().getCanonicalName()).getClass().getCanonicalName();
		
		
		//problema -> pegar a lista
//		argValues.clear();
//		argValues.addGenericArgumentValue(getNewProxyList(), "java.util.List");
//		fcp.setConstructorArgumentValues(argValues);

		

	}
	
	private PentahoSolutionSpringApplicationContext getSpringContext(){
		Set<ListableBeanFactory> factories = PublishedBeanRegistry.getRegisteredFactories();
		ListableBeanFactory[] factoriesArr = factories.toArray(new ListableBeanFactory[factories.size()]);
		
		for(int x = 0 ; x < factoriesArr.length ; x++ ){
			
			if(factoriesArr[x] instanceof PentahoSolutionSpringApplicationContext){
				PentahoSolutionSpringApplicationContext appContext = (PentahoSolutionSpringApplicationContext) factoriesArr[x];
				return appContext;
			}
		}
		return null;
	}

	private FilterChainProxy getNewProxy(){
		FilterChainProxy proxy = getProxy();
		List<SecurityFilterChain> list = proxy.getFilterChains();
		
		ArrayList<SecurityFilterChain> l2 = new ArrayList<SecurityFilterChain>();
		l2.addAll(list);
		DefaultSecurityFilterChain fc1 = 
					new DefaultSecurityFilterChain(
							new AntPathRequestMatcher("/content/integrator/**"), getCustomInterceptor());
		
		l2.add(0, fc1);
		
		FilterChainProxy proxy2 = new FilterChainProxy(l2);
		return proxy2;
		
	}
	
	private List<SecurityFilterChain> getNewProxyList(){
		List<SecurityFilterChain> lsf = new ArrayList<SecurityFilterChain>();
		
		Set<ListableBeanFactory> factories = PublishedBeanRegistry.getRegisteredFactories();
		ListableBeanFactory[] factoriesArr = factories.toArray(new ListableBeanFactory[factories.size()]);
		
		System.out.println("..................");
		for(int x = 0 ; x < factoriesArr.length ; x++ ){
			
			if(factoriesArr[x] instanceof PentahoSolutionSpringApplicationContext){
				PentahoSolutionSpringApplicationContext appContext = (PentahoSolutionSpringApplicationContext) factoriesArr[x];
				FilterChainProxy fcp = (FilterChainProxy) appContext.getBean("filterChainProxy");
				lsf = new ArrayList<SecurityFilterChain>( fcp.getFilterChains() );
				lsf.add(getCustomFilterChain());
			}
		}
		return lsf;
	}

	
	/**
	 * This method gets the authenticationManager bean from PentahoSystem
	 */
	private FilterChainProxy getProxy() {
		FilterChainProxy proxy = PentahoSystem.get(FilterChainProxy.class, "filterChainProxy",
				PentahoSessionHolder.getSession());
		return proxy;
		
	}
	
	
	private FilterSecurityInterceptor getCustomInterceptor() {
		ProviderManager authenticationManager = PentahoSystem.get(ProviderManager.class,
				"authenticationManager", PentahoSessionHolder.getSession());
		FilterSecurityInterceptor interceptor = new FilterSecurityInterceptor();
		interceptor.setSecurityMetadataSource(securityMetadataSource);
		interceptor.setAuthenticationManager((AuthenticationManager) authenticationManager);
		return interceptor;
	}
	
	private DefaultSecurityFilterChain getCustomFilterChain(){
		DefaultSecurityFilterChain fc1 = 
				new DefaultSecurityFilterChain(
						new AntPathRequestMatcher("/content/integrator/**"), getCustomInterceptor());
		return fc1;
	}

	public FilterInvocationSecurityMetadataSource getSecurityMetadataSource() {
		return securityMetadataSource;
	}

	public void setSecurityMetadataSource(FilterInvocationSecurityMetadataSource securityMetadataSource) {
		this.securityMetadataSource = securityMetadataSource;
	}
}
