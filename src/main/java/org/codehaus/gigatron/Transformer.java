package org.codehaus.gigatron;

import java.util.Map;

import org.objectweb.asm.tree.MethodNode;

public interface Transformer {

    void internalTransform(MethodNode body, Map<String, Object> options);

}
