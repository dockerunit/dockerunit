package com.github.dockerunit.internal.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerunit.annotation.ContainerBuilder;
import com.github.dockerunit.annotation.ExtensionMarker;
import com.github.dockerunit.annotation.Image;
import com.github.dockerunit.annotation.Named;
import com.github.dockerunit.annotation.Usages;
import com.github.dockerunit.annotation.Use;
import com.github.dockerunit.internal.ServiceDescriptor;
import com.github.dockerunit.internal.UsageDescriptor;
import com.github.dockerunit.internal.reflect.DefaultTestDescriptor.DefaultTestDescriptorBuilder;
import org.junit.runners.model.*;

public class DefaultDependencyDescriptorBuilder implements UsageDescriptorBuilder {

    @Override
    public UsageDescriptor buildDescriptor(FrameworkMethod method) {
        return buildDescriptor(method.getMethod());
    }

    @Override
    public UsageDescriptor buildDescriptor(Class<?> klass) {
        return buildDescriptor((AnnotatedElement) klass);
    }

    private UsageDescriptor buildDescriptor(AnnotatedElement element) {
        List<Use> requirements = getDependencies(element);
        List<ServiceDescriptor> descriptors = asDescriptors(requirements);
        return new DefaultDependencyDescriptor(descriptors);
    }

    private List<ServiceDescriptor> asDescriptors(List<Use> requirements) {
        List<ServiceDescriptor> descriptors = requirements.stream()
                .map(this::buildDescriptor)
                .collect(Collectors.toList());
        return descriptors;
    }

    private ServiceDescriptor buildDescriptor(Use use) {
        DefaultTestDescriptorBuilder builder = DefaultTestDescriptor.builder();

        checkServiceClass(use.service());
        try {
            builder.instance(use.service().newInstance());
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate service class " + use.service().getName());
        }
        builder.replicas(extractReplicas(use))
                .order(use.order())
                .containerName(use.containerPrefix())
                .image(findImage(use.service()))
                .named(findNamed(use.service()))
                .customisationHook(findCustomisationHook(use))
                .options(extractOptions(use.service()));
        return builder.build();
    }

    private List<? extends Annotation> extractOptions(Class<?> service) {
        return Arrays.asList(service.getDeclaredAnnotations()).stream()
                .filter(a -> a.annotationType().isAnnotationPresent(ExtensionMarker.class))
                .collect(Collectors.toList());
    }

    private void checkServiceClass(Class<?> service) {
        String className = service.getSimpleName();
        StringBuffer buffer = new StringBuffer();
        if (service.isInterface()) {
            buffer.append(className + " cannot be an interface. ");
        }
        if (service.isEnum()) {
            buffer.append(className + " cannot be an enum. ");
        }
        if (service.isSynthetic()) {
            buffer.append(className + " cannot be synthetic. ");
        }
        if (service.isAnnotation()) {
            buffer.append(className + " cannot be an annotation. ");
        }
        if (service.isArray()) {
            buffer.append(className + " cannot be an array. ");
        }
        if (service.isAnonymousClass()) {
            buffer.append(className + " cannot be anonymous. ");
        }
        if (service.isLocalClass()) {
            buffer.append(className + " cannot be a local class. ");
        }
        if (service.isMemberClass()) {
            buffer.append(className + " cannot be a member class. ");
        }
        int modifiers = service.getModifiers();
        if (Modifier.isAbstract(modifiers)) {
            buffer.append("Service class cannot be abstract. ");
        }
        if (!Modifier.isPublic(modifiers)) {
            buffer.append("Service class must be public. ");
        }
        if (Modifier.isStatic(modifiers)) {
            buffer.append("Service class cannot be static. ");
        }
        Optional<Constructor<?>> c = findSuitableContructor(service.getDeclaredConstructors());
        if (!c.isPresent()) {
            buffer.append("Service class must provide a public zero args constructor. ");
        }

        if (buffer.length() > 0) {
            throw new RuntimeException(buffer.toString());
        }

    }

    private Optional<Constructor<?>> findSuitableContructor(Constructor<?>[] declaredConstructors) {
        return Arrays.asList(declaredConstructors)
                .stream()
                .filter(c -> Modifier.isPublic(c.getModifiers()) && c.getParameterCount() == 0)
                .findFirst();
    }

    private Method findCustomisationHook(Use use) {
        Optional<Method> opt = Arrays.asList(use.service().getDeclaredMethods()).stream()
                .filter(m -> m.isAnnotationPresent(ContainerBuilder.class)
                        && Modifier.isPublic(m.getModifiers())
                        && !Modifier.isStatic(m.getModifiers())
                        && m.getParameterCount() == 1
                        && m.getParameterTypes()[0].equals(CreateContainerCmd.class)
                        && m.getReturnType().equals(CreateContainerCmd.class))
                .findFirst();
        return opt.orElse(null);
    }

    private int extractReplicas(Use use) {
        if (use.replicas() < 1) {
            throw new RuntimeException("Cannot require less than one replica");
        }
        return use.replicas();
    }

    private <T extends Annotation> T findRequiredAnnotation(Class<?> service, Class<T> annotationType) {
        if (!service.isAnnotationPresent(annotationType)) {
            throw new RuntimeException(
                    "No @" + annotationType.getSimpleName() + " has been specified on class " + service.getName());
        }
        return service.getAnnotation(annotationType);
    }

    private Image findImage(Class<?> service) {
        return findRequiredAnnotation(service, Image.class);
    }

    private Named findNamed(Class<?> service) {
        return findRequiredAnnotation(service, Named.class);
    }

    private List<Use> getDependencies(AnnotatedElement element) {
        Use[] requirements = element.isAnnotationPresent(Usages.class)
                ? element.getAnnotation(Usages.class)
                .value()
                : new Use[] {};
        if (requirements.length == 0 && element.isAnnotationPresent(Use.class)) {
            requirements = new Use[] { element.getAnnotation(Use.class) };
        }
        return Arrays.asList(requirements);
    }

}
