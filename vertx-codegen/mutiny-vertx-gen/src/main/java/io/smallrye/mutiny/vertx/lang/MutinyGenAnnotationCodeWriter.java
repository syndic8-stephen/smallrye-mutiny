package io.smallrye.mutiny.vertx.lang;

import java.io.PrintWriter;

import io.vertx.codegen.ClassModel;

public class MutinyGenAnnotationCodeWriter implements CodeWriter {
    @Override
    public void generate(ClassModel model, PrintWriter writer) {
        writer.print("@io.smallrye.mutiny.vertx.helpers.MutinyGen(");
        writer.print(model.getType().getName());
        writer.println(".class)");
    }
}
