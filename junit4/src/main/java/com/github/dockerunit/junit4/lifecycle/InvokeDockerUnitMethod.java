package com.github.dockerunit.internal.lifecycle;

import com.github.dockerunit.core.ServiceContext;
import com.github.dockerunit.junit4.DockerUnitRunner;
import lombok.AllArgsConstructor;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;

@AllArgsConstructor
public class InvokeDockerUnitMethod extends Statement {

    private final FrameworkMethod testMethod;
    private final Object target;
    private final DockerUnitRunner runner;

    @Override
    public void evaluate() throws Throwable {
        Method method = testMethod.getMethod();
        if (method.getParameterCount() == 0) {
            testMethod.invokeExplosively(target);
        } else if (method.getParameterCount() == 1
                && method.getParameterTypes()[0].isAssignableFrom(ServiceContext.class)) {
            ServiceContext context = runner.getContext(testMethod);
            testMethod.invokeExplosively(target, context);
        } else {
            throw new IllegalArgumentException("Test methods must have either zero arguments or one argument of type "
                    + ServiceContext.class.getName());
        }
    }

}
