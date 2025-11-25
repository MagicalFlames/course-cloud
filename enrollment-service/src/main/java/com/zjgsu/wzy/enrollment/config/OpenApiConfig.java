package com.zjgsu.wzy.enrollment.config;

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
    public OpenAPI enrollmentServiceOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8082");
        server.setDescription("Development server");

        Contact contact = new Contact();
        contact.setName("选课服务");
        contact.setEmail("wzy@zjgsu.edu.cn");

        Info info = new Info()
                .title("选课服务 API")
                .version("1.0.0")
                .description("校园选课系统 - 选课微服务 API 文档，包含学生管理和选课管理")
                .contact(contact);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
