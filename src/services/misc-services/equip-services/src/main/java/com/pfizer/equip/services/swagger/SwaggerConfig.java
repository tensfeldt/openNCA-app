package com.pfizer.equip.services.swagger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
   @Bean
   public Docket productApi() {
      //TODO:Get the USERID headers IAMPFIZERUSERCN from the application properties. 
      //Adding userID Header
      ParameterBuilder aParameterBuilder = new ParameterBuilder();
      aParameterBuilder.name("IAMPFIZERUSERCN").modelRef(new ModelRef("string")).parameterType("header").required(true).build();
      List<Parameter> aParameters = new ArrayList<>();
      aParameters.add(aParameterBuilder.build());
      return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.basePackage("com.pfizer.equip.services.controllers"))
            .paths(PathSelectors.any()).build().globalOperationParameters(aParameters).apiInfo(apiInfo());
   }
   private ApiInfo apiInfo() {
      return new ApiInfoBuilder().title("Equip Services").version("1.0.0").build();
  }
 
}
