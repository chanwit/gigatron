package org.codehaus.gigatron.asm.transformer;

import java.util.Map;

import org.codehaus.gigatron.Transformer;
import org.codehaus.gigatron.asm.PartialDefUseAnalyser;
import org.codehaus.gigatron.asm.ReverseStackDistance;
import org.codehaus.gigatron.asm.Utils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class UnwrapCompareTransformer implements Transformer, Opcodes {

//    ILOAD 0
//    INVOKESTATIC java/lang/Integer.valueOf(I)Ljava/lang/Integer;
//    LDC 0
//    INVOKESTATIC java/lang/Integer.valueOf(I)Ljava/lang/Integer;
//    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.compareLessThan(Ljava/lang/Object;Ljava/lang/Object;)Z
//    IFEQ L2

//    ILOAD 0
//    INVOKESTATIC java/lang/Integer.valueOf(I)Ljava/lang/Integer;
//    CHECKCAST java/lang/Integer
//    INVOKESTATIC unbox
//    LDC 0
//    INVOKESTATIC java/lang/Integer.valueOf(I)Ljava/lang/Integer;
//    CHECKCAST java/lang/Integer
//    INVOKESTATIC unbox
//    INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.compareLessThan(Ljava/lang/Object;Ljava/lang/Object;)Z
//    IFEQ L2


    private enum ComparingMethod {
        compareLessThan,
        compareGreaterThan,
        compareLessThanEqual,
        compareGreaterThanEqual
    };

    private static final String SCRIPT_BYTECODE_ADAPTER = "org/codehaus/groovy/runtime/ScriptBytecodeAdapter";
    private static final String COMPARE_METHOD_DESC = "(Ljava/lang/Object;Ljava/lang/Object;)Z";

    @Override
    public void internalTransform(MethodNode body, Map<String, Object> options) {
        // Steps
        // 1. find ScriptBytecodeAdapter.compareXXX
        // 2. get starting point
        // 3. doing partial def-use
        // 4. check types in used map
        // if type of op1 and op2 are same, do conversion
        InsnList units = body.instructions;
        AbstractInsnNode s = units.getFirst();
        while(s != null) {
            if(s.getOpcode() != INVOKESTATIC) { s = s.getNext(); continue; }

            MethodInsnNode m = (MethodInsnNode)s;
            if(m.owner.equals(SCRIPT_BYTECODE_ADAPTER) &&
               m.name.startsWith("compare")            &&
               m.desc.equals(COMPARE_METHOD_DESC))
            {
                ComparingMethod compare;
                try {
                    compare = ComparingMethod.valueOf(m.name);
                } catch (IllegalArgumentException e) {
                    s = s.getNext();
                    continue;
                }

                ReverseStackDistance rvd = new ReverseStackDistance(m);
                AbstractInsnNode start = rvd.findStartingNode();
                PartialDefUseAnalyser pdua = new PartialDefUseAnalyser(body, start, m);
                Map<AbstractInsnNode, AbstractInsnNode[]> usedMap = pdua.analyse();
                AbstractInsnNode[] array = usedMap.get(m);
                assert array.length == 2;
                Type t0 = Utils.getType(array[0]);
                Type t1 = Utils.getType(array[1]);
//                System.out.println(t0);
//                System.out.println(t1);
                if(t0.equals(t1)){
                    units.insert(array[0], Utils.getUnboxNodes(t0.getDescriptor()));
                    units.insert(array[1], Utils.getUnboxNodes(t1.getDescriptor()));

                    if(t0.getDescriptor().equals("Ljava/lang/Integer;")) {
                        AbstractInsnNode newS = convertCompareForInt(compare, s);
                        units.set(s, newS);
                        AbstractInsnNode oldIf = newS.getNext();
                        s = oldIf.getNext();
                        units.remove(oldIf);
                        continue;
                    } else
                        throw new RuntimeException("NYI");

//                    switch(t0.getSort()) {
//                        case Type.INT: {
//                            }
//                            continue;
//                        default:
//                            throw new RuntimeException("NYI");
                        // TODO: case Type.LONG:   convertCompare(LCMP, compare, s); break;
                        // TODO: case Type.FLOAT:  convertCompare(FCMPL, compare, s); break;
                        // TODO: case Type.DOUBLE: convertCompare(DCMPL, compare, s); break;
                }
            }
            s = s.getNext();
        }
    }

    private JumpInsnNode convertCompareForInt(ComparingMethod compare, AbstractInsnNode s) {
        JumpInsnNode s1 = (JumpInsnNode) s.getNext();
        switch (compare) {
            case compareGreaterThan:
                return new JumpInsnNode(IF_ICMPLE, s1.label);
            case compareGreaterThanEqual:
                return new JumpInsnNode(IF_ICMPLT, s1.label);
            case compareLessThan:
                return new JumpInsnNode(IF_ICMPGE, s1.label);
            case compareLessThanEqual:
                return new JumpInsnNode(IF_ICMPGT, s1.label);
            default:
                throw new RuntimeException("NYI");
        }
    }


    private AbstractInsnNode[] convertCompare(int opcode, ComparingMethod compare, AbstractInsnNode s) {
        AbstractInsnNode[] result = new AbstractInsnNode[2];
        JumpInsnNode s1 = (JumpInsnNode) s.getNext();
        result[0] = new InsnNode(opcode);
        //units.set(s, );
        switch (compare) {
            case compareGreaterThan:
                result[1] = new JumpInsnNode(IFLE, s1.label);
                break;
            case compareGreaterThanEqual:
                result[1] = new JumpInsnNode(IFLT, s1.label);
                break;
            case compareLessThan:
                result[1] = new JumpInsnNode(IFGE, s1.label);
                break;
            case compareLessThanEqual:
                result[1] = new JumpInsnNode(IFGT, s1.label);
                break;
        }
        return result;
    }

}
