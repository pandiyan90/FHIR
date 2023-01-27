/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.sovrinhealth.fhir.server.test;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import net.sovrinhealth.fhir.persistence.FHIRPersistence;
import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;
import net.sovrinhealth.fhir.persistence.helper.FHIRPersistenceHelper;
import net.sovrinhealth.fhir.persistence.helper.PersistenceHelper;
import net.sovrinhealth.fhir.server.listener.FHIRServletContextListener;

/**
 *
 */
public class MockServletContext implements ServletContext {

    @Override
    public Object getAttribute(String name) {
        if (FHIRServletContextListener.FHIR_SERVER_INIT_COMPLETE.equals(name)) {
            return true;
        }

        if (FHIRPersistenceHelper.class.getName().equals(name)) {
            return new PersistenceHelper() {
                @Override
                public FHIRPersistence getFHIRPersistenceImplementation() throws FHIRPersistenceException {
                    return new MockPersistenceImpl();
                }
                @Override
                public FHIRPersistence getFHIRPersistenceImplementation(String factoryPropertyName) throws FHIRPersistenceException {
                    return new MockPersistenceImpl();
                }
            };
        }

        return null;
    }

    // All below methods are auto-generated stubs
    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {}

    @Override
    public void setSessionTimeout(int sessionTimeout) {}

    @Override
    public void setResponseCharacterEncoding(String encoding) {}

    @Override
    public void setRequestCharacterEncoding(String encoding) {}

    @Override
    public boolean setInitParameter(String name, String value) { return true; }

    @Override
    public void setAttribute(String name, Object object) {}

    @Override
    public void removeAttribute(String name) {}

    @Override
    public void log(String message, Throwable throwable) {}

    @Override
    public void log(Exception exception, String msg) {}

    @Override
    public void log(String msg) {}

    @Override
    public String getVirtualServerName() { return null; }

    @Override
    public int getSessionTimeout() { return 0; }

    @Override
    public SessionCookieConfig getSessionCookieConfig() { return null; }

    @Override
    public Enumeration<Servlet> getServlets() { return null; }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() { return null; }

    @Override
    public ServletRegistration getServletRegistration(String servletName) { return null; }

    @Override
    public Enumeration<String> getServletNames() { return null; }

    @Override
    public String getServletContextName() { return null; }

    @Override
    public Servlet getServlet(String name) throws ServletException { return null; }

    @Override
    public String getServerInfo() { return null; }

    @Override
    public String getResponseCharacterEncoding() { return null; }

    @Override
    public Set<String> getResourcePaths(String path) { return null; }

    @Override
    public InputStream getResourceAsStream(String path) { return null; }

    @Override
    public URL getResource(String path) throws MalformedURLException { return null; }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) { return null; }

    @Override
    public String getRequestCharacterEncoding() { return null; }

    @Override
    public String getRealPath(String path) { return null; }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) { return null; }

    @Override
    public int getMinorVersion() { return 0; }

    @Override
    public String getMimeType(String file) { return null; }

    @Override
    public int getMajorVersion() { return 0; }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() { return null; }

    @Override
    public Enumeration<String> getInitParameterNames() { return null; }

    @Override
    public String getInitParameter(String name) { return null; }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() { return null; }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) { return null; }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() { return null; }

    @Override
    public int getEffectiveMinorVersion() { return 0; }

    @Override
    public int getEffectiveMajorVersion() { return 0; }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() { return null; }

    @Override
    public String getContextPath() { return null; }

    @Override
    public ServletContext getContext(String uripath) { return null; }

    @Override
    public ClassLoader getClassLoader() { return null; }

    @Override
    public Enumeration<String> getAttributeNames() { return null; }

    @Override
    public void declareRoles(String... roleNames) {}

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException { return null; }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException { return null; }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException { return null; }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) { return null; }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) { return null; }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, String className) { return null; }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {}

    @Override
    public <T extends EventListener> void addListener(T t) {}

    @Override
    public void addListener(String className) {}

    @Override
    public javax.servlet.ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) { return null; }

    @Override
    public Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) { return null; }

    @Override
    public Dynamic addFilter(String filterName, Filter filter) { return null; }

    @Override
    public Dynamic addFilter(String filterName, String className) { return null; }
}
