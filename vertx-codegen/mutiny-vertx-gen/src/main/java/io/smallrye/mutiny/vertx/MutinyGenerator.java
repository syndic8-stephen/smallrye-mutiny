package io.smallrye.mutiny.vertx;

import io.vertx.codegen.ClassModel;
import io.vertx.codegen.Generator;

import java.util.Collections;

public class MutinyGenerator extends Generator<ClassModel> {

    private String id;

    public MutinyGenerator() {
        this.id = "mutiny";
        this.kinds = Collections.singleton("class");
        this.name = this.id;
    }

}
