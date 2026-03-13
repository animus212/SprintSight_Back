//package com.example.SprintSight.Configurations;
//
//import org.apache.catalina.connector.Connector;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
//import org.springframework.boot.web.server.servlet.ServletWebServerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class HttpConfiguration {
//    @Bean
//    public ServletWebServerFactory servletContainer(@Value("${server.http.port}") int httpPort) {
//        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
//        connector.setPort(httpPort);
//
//        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
//        tomcat.addAdditionalConnectors(connector);
//
//        return tomcat;
//    }
//}
