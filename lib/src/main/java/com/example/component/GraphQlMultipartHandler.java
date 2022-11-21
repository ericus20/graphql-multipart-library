package com.example.component;

import com.example.MultipartGraphQlRequest;
import com.example.mapper.MultipartVariableMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.Assert;
import org.springframework.util.IdGenerator;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import reactor.core.publisher.Mono;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * WebFlux.fn Handler for GraphQL over HTTP requests.
 */
@Component
public class GraphQlMultipartHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GraphQlMultipartHandler.class);

    /**
     * for generating universally unique identifiers ({@link UUID UUIDs}).
     */
    private final IdGenerator idGenerator = new AlternativeJdkIdGenerator();

    private final WebGraphQlHandler graphQlHandler;
    private final PartReader partReader;

    public static final List<MediaType> SUPPORTED_RESPONSE_MEDIA_TYPES =
            Arrays.asList(MediaType.APPLICATION_GRAPHQL, MediaType.APPLICATION_JSON);

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_PARAMETERIZED_TYPE_REF =
            new ParameterizedTypeReference<>() {
            };

    private static final ParameterizedTypeReference<Map<String, List<String>>> LIST_PARAMETERIZED_TYPE_REF =
            new ParameterizedTypeReference<>() {
            };

    public GraphQlMultipartHandler(@NonNull WebGraphQlHandler graphQlHandler, @NonNull PartReader partReader) {
        this.graphQlHandler = graphQlHandler;
        this.partReader = partReader;
    }

    /**
     * The operation type is either query, mutation, or subscription and describes what type of operation you're intending to do.
     *
     * @param serverRequest ServerRequest
     * @return ServerResponse
     */
    public ServerResponse handleRequest(ServerRequest serverRequest) {
        final var httpServletRequest = serverRequest.servletRequest();

        final var inputQuery = Optional.ofNullable(this.<Map<String, Object>>deserializePart(
                httpServletRequest,
                "operations",
                MAP_PARAMETERIZED_TYPE_REF.getType()
        )).orElse(new HashMap<>());

        final var queryVariables = getFromMapOrEmpty(inputQuery, "variables");
        final var fileParams = readMultipartFiles(httpServletRequest);
        final var fileMappings = Optional.ofNullable(this.<Map<String, List<String>>>deserializePart(
                httpServletRequest,
                "map",
                LIST_PARAMETERIZED_TYPE_REF.getType()
        )).orElse(new HashMap<>());

        fileMappings.forEach((String fileKey, List<String> objectPaths) -> {
            MultipartFile file = fileParams.get(fileKey);
            if (file != null) {
                objectPaths.forEach((String objectPath) -> MultipartVariableMapper.mapVariable(
                        objectPath,
                        queryVariables,
                        file
                ));
            }
        });

        return ServerResponse.async(processRequest(serverRequest, inputQuery, queryVariables));
    }

    private Mono<ServerResponse> processRequest(final ServerRequest serverRequest,
                                                final Map<String, Object> inputQuery,
                                                final Map<String, Object> queryVariables) {

        final Map<String, Object> extensions = getFromMapOrEmpty(inputQuery, "extensions");
        var query = (String) inputQuery.get("query");
        var operationName = (String) inputQuery.get("operationName");
        var graphQlRequest = new MultipartGraphQlRequest(
                query,
                operationName,
                queryVariables,
                extensions,
                serverRequest.uri(), serverRequest.headers().asHttpHeaders(),
                this.idGenerator.generateId().toString(), LocaleContextHolder.getLocale());

        LOG.debug("Executing: " + graphQlRequest);

        return this.graphQlHandler.handleRequest(graphQlRequest)
                .map(response -> {
                    LOG.debug("Execution complete");

                    var builder = ServerResponse.ok();
                    builder.headers(headers -> headers.putAll(response.getResponseHeaders()));
                    builder.contentType(selectResponseMediaType(serverRequest));

                    return builder.body(response.toMap());
                });
    }

    private <T> T deserializePart(HttpServletRequest httpServletRequest, String name, Type type) {
        try {
            Part part = httpServletRequest.getPart(name);
            if (part == null) {
                return null;
            }
            return partReader.readPart(part, type);
        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, MultipartFile> readMultipartFiles(HttpServletRequest httpServletRequest) {
        Assert.isInstanceOf(MultipartHttpServletRequest.class, httpServletRequest,
                "Request should be of type MultipartHttpServletRequest");
        MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) httpServletRequest;
        return multipartHttpServletRequest.getFileMap();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getFromMapOrEmpty(Map<String, Object> input, String key) {
        if (input.containsKey(key)) {
            return (Map<String, Object>)input.get(key);
        } else {
            return new HashMap<>();
        }
    }

    private static MediaType selectResponseMediaType(final ServerRequest serverRequest) {
        for (var accepted : serverRequest.headers().accept()) {
            if (SUPPORTED_RESPONSE_MEDIA_TYPES.contains(accepted)) {
                return accepted;
            }
        }
        return MediaType.APPLICATION_JSON;
    }
}
