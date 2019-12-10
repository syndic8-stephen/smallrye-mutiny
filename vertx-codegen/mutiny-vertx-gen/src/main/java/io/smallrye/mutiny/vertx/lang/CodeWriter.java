package io.smallrye.mutiny.vertx.lang;

import static java.util.stream.Collectors.joining;

import java.io.PrintWriter;
import java.util.function.BiFunction;

import io.smallrye.mutiny.vertx.MutinyGenerator;
import io.vertx.codegen.ClassModel;
import io.vertx.codegen.type.ClassKind;
import io.vertx.codegen.type.ParameterizedTypeInfo;
import io.vertx.codegen.type.TypeInfo;

public interface CodeWriter extends BiFunction<ClassModel, PrintWriter, Void> {

    void generate(ClassModel model, PrintWriter writer);

    default Void apply(ClassModel model, PrintWriter writer) {
        generate(model, writer);
        return null;
    }

    default String genTypeName(TypeInfo type) {
        if (type.isParameterized()) {
            ParameterizedTypeInfo pt = (ParameterizedTypeInfo) type;
            return genTypeName(pt.getRaw()) + pt.getArgs().stream().map(this::genTypeName)
                    .collect(joining(", ", "<", ">"));
        } else if (type.getKind() == ClassKind.API) {
            return type.translateName(MutinyGenerator.ID);
        } else {
            return type.getSimpleName();
        }
    }
}
