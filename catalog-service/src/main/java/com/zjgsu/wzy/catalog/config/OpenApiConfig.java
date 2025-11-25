package com.zjgsu.wzy.catalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogServiceOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8081");
        server.setDescription("Development server");

        Contact contact = new Contact();
        contact.setName("课程目录服务");
        contact.setEmail("wzy@zjgsu.edu.cn");

        Info info = new Info()
                .title("课程目录服务 API")
                .version("1.0.0")
                .description("校园选课系统 - 课程目录微服务 API 文档")
                .contact(contact);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
