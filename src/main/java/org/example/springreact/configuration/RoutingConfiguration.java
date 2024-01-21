package org.example.springreact.configuration;


import org.example.springreact.handler.TaskHandler;
import org.example.springreact.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;


@Configuration
public class RoutingConfiguration {

    @Bean
    public RouterFunction<ServerResponse> userRouters(UserHandler userHandler) {
        return RouterFunctions.route()
                .GET("/api/functions/users", userHandler::getAll)
                .GET("/api/functions/users/{id}", userHandler::getById)
                .POST("/api/functions/users", userHandler::create)
                .PUT("/api/functions/users/{id}", userHandler::update)
                .DELETE("/api/functions/users/{id}", userHandler::delete)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> taskRouters(TaskHandler taskHandler) {
        return RouterFunctions.route()
                .GET("/api/functions/tasks", taskHandler::getAll)
                .GET("/api/functions/tasks/{id}", taskHandler::getById)
                .POST("/api/functions/tasks", taskHandler::create)
                .PUT("/api/functions/tasks/{id}", taskHandler::update)
                .DELETE("/api/functions/tasks/{id}", taskHandler::delete)
                .POST("/api/functions/tasks/{id}/addObserver/{idObserver}", taskHandler::addObserver)
                .build();
    }
}
