package oncase.biserver.security.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;

import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.spring.PublishedBeanRegistry;
import org.pentaho.platform.web.http.context.PentahoSolutionSpringApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
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
import org.springframework.security.web.util.matcher.AnyRequestMatcher;


public class URLExposer implements BeanFactoryPostProcessor {

	private FilterInvocationSecurityMetadataSource securityMetadataSource;
	private FilterChainProxy proxy;
	

	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("**URLExposer.postProcessBeanFactory(...)**");
		
		FilterSecurityInterceptor customInterceptor = getCustomInterceptor();
		
		DefaultListableBeanFactory bf = (DefaultListableBeanFactory) beanFactory;
		
		
		
		Set<ListableBeanFactory> factories = PublishedBeanRegistry.getRegisteredFactories();
		ListableBeanFactory[] factoriesArr = factories.toArray(new ListableBeanFactory[factories.size()]);
		
		System.out.println("..................");
		for(int x = 0 ; x < factoriesArr.length ; x++ ){
			
			if(factoriesArr[x] instanceof PentahoSolutionSpringApplicationContext){
				PentahoSolutionSpringApplicationContext appContext = (PentahoSolutionSpringApplicationContext) factoriesArr[x];
				FilterChainProxy fcp = (FilterChainProxy) appContext.getBean("filterChainProxy");
				List<SecurityFilterChain> lsf = new ArrayList<SecurityFilterChain>( fcp.getFilterChains() );
				//lsf.remove(3);
				testBeanFactory(appContext, lsf);
				
				appContext.getServletContext().addFilter("/content/integrator/**", customInterceptor);
			}
		}
		System.out.println("..................");


		initProxy();

		
		
		
		List<SecurityFilterChain> list = proxy.getFilterChains();
		Iterator<SecurityFilterChain> it = list.iterator();
		
		ArrayList<SecurityFilterChain> l2 = new ArrayList<SecurityFilterChain>();
		l2.addAll(list);
		DefaultSecurityFilterChain fc1 = 
					new DefaultSecurityFilterChain(
							new AntPathRequestMatcher("/content/integrator/**"), customInterceptor);
		
		l2.add(0, fc1);
		
		FilterChainProxy proxy2 = new FilterChainProxy(l2);
		
		
		while(it.hasNext()){
			DefaultSecurityFilterChain fc = (DefaultSecurityFilterChain) it.next();
			if(fc.getRequestMatcher() instanceof AnyRequestMatcher){
				ArrayList<Filter> filters = (ArrayList<Filter>) fc.getFilters();
				int index = filters.size() - 2;
				System.out.println(filters.size());
				//filters.add(index, customInterceptor);
				System.out.println(filters.size());
			}
		}

	}

	private void testBeanFactory(PentahoSolutionSpringApplicationContext appContext, List<SecurityFilterChain> li){
		//appContext.getBeanFactory().getbea
		GenericBeanDefinition bd = (GenericBeanDefinition) appContext.getBeanFactory().getBeanDefinition("filterChainProxy");
		ConstructorArgumentValues av = bd.getConstructorArgumentValues();
		ConstructorArgumentValues av2 = new ConstructorArgumentValues();
		av2.addGenericArgumentValue(li);
		bd.setConstructorArgumentValues(av2);
		
		System.out.println("-->");
		System.out.println(bd.getParentName());
		
		System.out.println(av.getArgumentCount());
		
		List<ValueHolder> args = av.getGenericArgumentValues();
		Iterator ia = args.iterator();

		System.out.println("..");
		while( ia.hasNext() ){
			ValueHolder bh = (ValueHolder) ia.next();
			BeanDefinitionHolder dh = (BeanDefinitionHolder) bh.getValue();
			GenericBeanDefinition list = (GenericBeanDefinition) dh.getBeanDefinition();


			System.out.println(list.getPropertyValues().get("sourceList"));
			List agorasim = (List) list.getPropertyValues().get("sourceList");
			agorasim.remove(4);
			Iterator a1 = agorasim.iterator();
			while(a1.hasNext()){
				Object aa = a1.next();
				System.out.println(aa.getClass().getCanonicalName());
			}
			System.out.println(list.getBeanClass());
			
			
			
			
		}
		System.out.println("..");
		bd.validate();
		appContext.afterPropertiesSet();
	}
	
	/**
	 * This method gets the authenticationManager bean from PentahoSystem
	 */
	private void initProxy() {
		proxy = PentahoSystem.get(FilterChainProxy.class, "filterChainProxy",
				PentahoSessionHolder.getSession());
		IPentahoObjectReference<FilterChainProxy> p2 = PentahoSystem.getObjectReference(FilterChainProxy.class,PentahoSessionHolder.getSession());
		
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
