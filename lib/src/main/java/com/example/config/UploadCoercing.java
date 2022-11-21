package com.example.config;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * To transform the binary part to a MultipartFile.
 */
class UploadCoercing implements Coercing<MultipartFile, MultipartFile> {

    @Override
    public MultipartFile serialize(@NonNull Object dataFetcherResult) throws CoercingSerializeException {
        throw new CoercingSerializeException("Upload is an input-only type");
    }

    @NonNull
    @Override
    public MultipartFile parseValue(@NonNull Object input) throws CoercingParseValueException {
        if (input instanceof MultipartFile) {
            return (MultipartFile)input;
        }
        throw new CoercingParseValueException(
                String.format("Expected a 'MultipartFile' like object but was '%s'.", input.getClass())
        );
    }

    @NonNull
    @Override
    public MultipartFile parseLiteral(@NonNull Object input) throws CoercingParseLiteralException {
        throw new CoercingParseLiteralException("Parsing literal of 'MultipartFile' is not supported");
    }
}
