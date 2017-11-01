package com.github.qzagarese.dockerunit.internal.lifecycle;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.github.qzagarese.dockerunit.MicrounitRunner;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MicrounitAfter extends Statement {

	private final FrameworkMethod method;
	private final MicrounitRunner runner;
	private final Statement statement;
	private final DiscoveryProvider discoveryProvider;
	private final ServiceContextBuilder contextBuilder;
	
	@Override
	public void evaluate() throws Throwable {
		try {
			statement.evaluate();
		} catch(Throwable t) {
			t.printStackTrace();
			throw t;
		} finally {
			ServiceContext context = runner.getContext(method);
			if(context != null) {
				context = context.subtract(runner.getClassContext());
				ServiceContext cleared = contextBuilder.clearContext(context);
				runner.setContext(method, cleared);
				discoveryProvider.clearRegistry(cleared, runner.getClassContext());
			}
		}
	}

}
