package io.smallrye.mutiny.vertx.lang;

import java.io.PrintWriter;

import io.smallrye.mutiny.vertx.AbstractMutinyGenerator;
import io.vertx.codegen.ClassModel;

public class PackageDeclarationCodeWriter implements CodeWriter {

    @Override
    public void generate(ClassModel model, PrintWriter writer) {
        writer.print("package ");
        writer.print(model.getType().translatePackageName(AbstractMutinyGenerator.ID));
        writer.println(";");
        writer.println();
    }
}
