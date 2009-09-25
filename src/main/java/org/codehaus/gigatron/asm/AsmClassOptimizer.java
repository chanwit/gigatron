package org.codehaus.gigatron.asm;

import java.util.List;

import org.codehaus.gigatron.AbstractClassOptimizer;
import org.codehaus.gigatron.Transformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public class AsmClassOptimizer extends AbstractClassOptimizer {

    @SuppressWarnings("unchecked")
    protected void applyTransformers() {
        //
        // prepare constant and call site name array
        //
        Utils.prepareClassInfo(this.classNode);

        List<MethodNode> methods = this.classNode.methods;
        for(MethodNode method: methods) {
            // skip a synthetic method
            if((method.access & Opcodes.ACC_SYNTHETIC) != 0) continue;
            // skip class init method (static { ... })
            if(method.name.equals("<clinit>")) continue;

            for(Transformer t: transformers) {
                t.internalTransform(method, null);
            }
        }
    }

}
