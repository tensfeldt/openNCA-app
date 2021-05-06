package com.pfizer.equip.shared;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
  basePackages = { "com.pfizer.equip.shared.relational" },
  entityManagerFactoryRef = "entityManagerFactory",
  transactionManagerRef = "transactionManager"
)
@ConfigurationProperties(prefix = "spring.datasource")
public class ServicesDatabaseConfiguration {
   
   String jndiName;
  
  public String getJndiName() {
      return jndiName;
   }

   public void setJndiName(String jndiName) {
      this.jndiName = jndiName;
   }

  @Primary
  @Bean(name = "dataSource")
  public DataSource dataSource() {
     JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
     return dataSourceLookup.getDataSource(this.getJndiName());
  }
  
  @Primary
  @Bean(name = "entityManagerFactory")
  public LocalContainerEntityManagerFactoryBean 
  entityManagerFactory(
    EntityManagerFactoryBuilder builder,
    @Qualifier("dataSource") DataSource dataSource
  ) {
    return builder
      .dataSource(dataSource)
      .packages("com.pfizer.equip.shared.relational.entity")
      .persistenceUnit("relational")
      .build();
  }
    
  @Primary
  @Bean(name = "transactionManager")
  public PlatformTransactionManager transactionManager(
    @Qualifier("entityManagerFactory") EntityManagerFactory 
    entityManagerFactory
  ) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}