package oncase.biserver.security.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.spring.PublishedBeanRegistry;
import org.pentaho.platform.web.http.context.PentahoSolutionSpringApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


public class URLExposer implements BeanFactoryPostProcessor {

	private String filterChainUrlPattern;
	private FilterSecurityInterceptor filterInterceptor;
	
	private PentahoSolutionSpringApplicationContext appContext;
	private FilterChainProxy fcpObject;
	

	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		
		System.out.println("\n\n\n**URLExposer.postProcessBeanFactory(...)**");

		initSpringContext();
		initFcpObject();

		
		/* Quest for the list */
		List<SecurityFilterChain> lsf = new ArrayList<SecurityFilterChain>();
		
		System.out.println("..................");
				
		
		lsf = new ArrayList<SecurityFilterChain>( fcpObject.getFilterChains() );

		System.out.println(".................. SIZE OF THE LIST");
		System.out.println(lsf.size());
		
		lsf.add(lsf.size()-1, getCustomFilterChain());
		
		System.out.println(".................. NEW SIZE OF THE LIST");
		System.out.println(lsf.size());

		FilterChainProxy proxy2 = new FilterChainProxy(lsf);
		/*/Quest for the list */
		
		appContext.getBeanFactory().registerSingleton(proxy2.getClass().getCanonicalName(), proxy2);
		
	}
	
	private void initFcpObject() {
		fcpObject = (FilterChainProxy) appContext.getBean("filterChainProxy");
	}

	private void initSpringContext(){
		Set<ListableBeanFactory> factories = PublishedBeanRegistry.getRegisteredFactories();
		ListableBeanFactory[] factoriesArr = factories.toArray(new ListableBeanFactory[factories.size()]);
		
		for(int x = 0 ; x < factoriesArr.length ; x++ ){
			
			if(factoriesArr[x] instanceof PentahoSolutionSpringApplicationContext){
				PentahoSolutionSpringApplicationContext appCon = (PentahoSolutionSpringApplicationContext) factoriesArr[x];
				this.appContext = appCon;
				break;
			}
		}
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
		AffirmativeBased accessDecisionManager = PentahoSystem.get(AffirmativeBased.class,
				"accessDecisionManager", PentahoSessionHolder.getSession());

		filterInterceptor.setAuthenticationManager( authenticationManager );
		filterInterceptor.setAccessDecisionManager( accessDecisionManager ); 
		return filterInterceptor;
	}
	
	private DefaultSecurityFilterChain getCustomFilterChain(){
		// Get the last sec:filter-chain /**
		SecurityFilterChain sfc = fcpObject.getFilterChains().get(fcpObject.getFilterChains().size()-1);
		
		List<Filter> filters = sfc.getFilters();
		filters.set(filters.size()-1, getCustomInterceptor());
		DefaultSecurityFilterChain fc1 = 
				new DefaultSecurityFilterChain(
						new AntPathRequestMatcher(filterChainUrlPattern), 
						filters );
		return fc1;
	}

	public String getFilterChainUrlPattern() {
		return filterChainUrlPattern;
	}

	public FilterSecurityInterceptor getFilterInterceptor() {
		return filterInterceptor;
	}

	public void setFilterChainUrlPattern(String filterChainUrlPattern) {
		this.filterChainUrlPattern = filterChainUrlPattern;
	}

	public void setFilterInterceptor(FilterSecurityInterceptor filterInterceptor) {
		this.filterInterceptor = filterInterceptor;
	}


}
