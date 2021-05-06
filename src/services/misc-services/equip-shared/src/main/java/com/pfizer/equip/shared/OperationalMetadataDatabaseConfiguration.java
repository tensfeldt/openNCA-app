package com.pfizer.equip.shared;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
  basePackages = { "com.pfizer.equip.shared.opmeta" },
  entityManagerFactoryRef = "opmetaEntityManagerFactory",
  transactionManagerRef = "opmetaTransactionManager"
)
@ConfigurationProperties(prefix = "opmeta.datasource")
public class OperationalMetadataDatabaseConfiguration {
   
   String jndiName;
  
  public String getJndiName() {
      return jndiName;
   }

   public void setJndiName(String jndiName) {
      this.jndiName = jndiName;
   }

  //@Primary
  @Bean(name = "opmetaDataSource")
  public DataSource dataSource() {
     JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
     return dataSourceLookup.getDataSource(this.getJndiName());
  }
  
  //@Primary
  @Bean(name = "opmetaEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
    EntityManagerFactoryBuilder builder,
    @Qualifier("opmetaDataSource") DataSource dataSource
  ) {
    return builder
      .dataSource(dataSource)
      .packages("com.pfizer.equip.shared.opmeta.entity")
      .persistenceUnit("opmeta")
      .build();
  }
    
  //@Primary
  @Bean(name = "opmetaTransactionManager")
  public PlatformTransactionManager transactionManager(
    @Qualifier("opmetaEntityManagerFactory") EntityManagerFactory 
    entityManagerFactory
  ) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}