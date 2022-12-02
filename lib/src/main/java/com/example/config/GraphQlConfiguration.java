package com.example.config;

import com.example.component.GraphQlMultipartHandler;
import com.example.component.PartReader;
import graphql.schema.GraphQLScalarType;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import static com.example.component.GraphQlMultipartHandler.SUPPORTED_REQUEST_MEDIA_TYPES;
import static com.example.component.GraphQlMultipartHandler.SUPPORTED_RESPONSE_MEDIA_TYPES;

@Configuration
class GraphQlConfiguration {

    /**
     * Register a new scalar type 'Upload'.
     * This can then be defined in the schema as 'scalar Upload'
     *
     * @return RuntimeWiringConfigurer
     */
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurerUpload() {

        var uploadScalar = GraphQLScalarType.newScalar()
                .name("Upload")
                .coercing(new UploadCoercing())
                .build();

        return wiringBuilder -> wiringBuilder.scalar(uploadScalar);
    }

    /**
     * RouterFunctions are Central entry point to Spring's functional web framework.
     * Order(1) means this Should be before the [RoutesConfiguration#graphQLRoutes].
     * <p>
     * Configuring the graphql route specified as a POST request and support for multipart from GraphqlMultipartHandler.
     *
     * @param properties              GraphQlProperties
     * @return RouterFunction<ServerResponse>
     */
    @Bean
    @Order(1)
    public RouterFunction<ServerResponse> graphQlMultipartRouterFunction(
            GraphQlProperties properties,
            WebGraphQlHandler webGraphQlHandler,
            PartReader partReader
    ) {
        var path = properties.getPath();
        var builder = RouterFunctions.route();
        var graphqlMultipartHandler = new GraphQlMultipartHandler(webGraphQlHandler, partReader);
        builder = builder.POST(path, RequestPredicates.contentType(SUPPORTED_REQUEST_MEDIA_TYPES.toArray(MediaType[]::new))
                .and(RequestPredicates.accept(SUPPORTED_RESPONSE_MEDIA_TYPES.toArray(MediaType[]::new))), graphqlMultipartHandler::handleRequest);

        return builder.build();
    }
}
