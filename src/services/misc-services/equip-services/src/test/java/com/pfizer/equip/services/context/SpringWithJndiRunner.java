package com.pfizer.equip.services.context;

import java.util.Properties;

import javax.naming.NamingException;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class SpringWithJndiRunner extends SpringJUnit4ClassRunner {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   public static boolean isJNDIactive;

   /**
    * JNDI is activated with this constructor.
    * 
    * @param klass
    * @throws InitializationError
    * @throws NamingException
    * @throws IllegalStateException
    */
   public SpringWithJndiRunner(Class<?> klass) throws InitializationError, IllegalStateException, NamingException {
      super(klass);
      log.info("Unit Testing - Spring with JNDI Runner to setup Data Source ");
      synchronized (SpringWithJndiRunner.class) {
         if (!isJNDIactive) {
            log.info("Unit Testing - No JNDI is active, hence settingup Data Source");

            try {
               // Get the jasypt password from testapplication.properties and decrypt the encrypted password using the StandardPBEStringEncryptor with jasypt password.
               // We follow this approach than the approach followed in the base code (i.e., from the java class with @@ConfigurationProperties), because spring
               // configurations will not be available at this point.
               // Spring configuration will be loaded once the Spring Runner is ready

               // Get Jasypt Password from testapplicaton.properties and set it to the encryptor
               Properties appProperties = new Properties();
               appProperties.load(new ClassPathResource("testapplication.properties").getInputStream());
               String jasyptPassword = appProperties.getProperty("jasypt.encryptor.password");

               StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
               standardPBEStringEncryptor.setPassword(jasyptPassword);

               // Load the testdatasource.properties using EncryptableProperties as this properties file has encrypted password
               Properties properties = new EncryptableProperties(standardPBEStringEncryptor);
               properties.load(new ClassPathResource("/config/testdatasource.properties").getInputStream());

               SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
               DriverManagerDataSource equipDS = new DriverManagerDataSource(properties.getProperty("spring.dataSource.url"),
                     properties.getProperty("spring.dataSource.username"), properties.getProperty("spring.dataSource.password"));
               builder.bind("java:/datasources/equipDS", equipDS);
               DriverManagerDataSource podsDS = new DriverManagerDataSource(properties.getProperty("opmeta.dataSource.url"),
                     properties.getProperty("opmeta.dataSource.username"), properties.getProperty("opmeta.dataSource.password"));
               builder.bind("java:/datasources/podsDS", podsDS);

               isJNDIactive = true;
            } catch (Exception e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
      }
   }
}
