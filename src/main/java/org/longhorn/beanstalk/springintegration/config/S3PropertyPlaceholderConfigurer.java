package org.longhorn.beanstalk.springintegration.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;

public class S3PropertyPlaceholderConfigurer extends
        PropertyPlaceholderConfigurer implements FactoryBean<Properties> {

    private S3ResourceLoader resourceLoader;
    private String[] s3Locations = new String[0];
    private Resource[] conventionalResources = new Resource[0];
    private Properties processedProps;

    public S3PropertyPlaceholderConfigurer() {
        resourceLoader = new S3ResourceLoader();
    }

    public void setLocations(Resource[] locations) {
        this.conventionalResources = locations;
    }

    @SuppressWarnings("deprecation")
    public void setS3Locations(String[] s3Locations) {
        this.s3Locations = new String[s3Locations.length];
        for (int i = 0; i < s3Locations.length; i++) {
            this.s3Locations[i] = parseStringValue(s3Locations[i].trim(),
                    new Properties(), new HashSet<String>());
        }

    }

    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) throws BeansException {
        injectS3Resources();
        super.postProcessBeanFactory(beanFactory);
    }

    private void injectS3Resources() {

        int total = conventionalResources.length + s3Locations.length;

        if (total > 0) {
            List<Resource> allResources = new ArrayList<Resource>();
            for (Resource conventionalResource : conventionalResources) {
                allResources.add(conventionalResource);
            }
            for (String s3Location : s3Locations) {
                allResources.add(resourceLoader.getResource(s3Location));
            }
            super.setLocations(allResources.toArray(new Resource[0]));
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
     * #processProperties
     * (org.springframework.beans.factory.config.ConfigurableListableBeanFactory
     * , java.util.Properties)
     */
    @Override
    protected void processProperties(
            ConfigurableListableBeanFactory beanFactoryToProcess,
            Properties props) throws BeansException {
        super.processProperties(beanFactoryToProcess, props);
        this.processedProps = props;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    @Override
    public Properties getObject() throws Exception {
        return processedProps;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    @Override
    public Class<?> getObjectType() {
        return Properties.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    @Override
    public boolean isSingleton() {
        return false;
    }
}
