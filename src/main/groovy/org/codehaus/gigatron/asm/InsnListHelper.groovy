package org.codehaus.gigatron.asm

import groovy.lang.Closure

import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.VarInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.Type
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList

import junit.framework.Assert

import static org.objectweb.asm.util.AbstractVisitor.OPCODES

public class InsnListHelper {

    static installed = false

    static install() {
        if(installed) return
        InsnList.metaClass.append = { Closure c ->
            c.delegate = new AsmNodeBuilder()
            c.resolveStrategy = Closure.DELEGATE_FIRST
            c.call()
            delegate.add( c.delegate.list )
        }
        InsnList.metaClass.get << { IntRange r ->
            new InsnListRange(range:r, list: delegate)
        }
        InsnList.metaClass.getAt = { i ->
            delegate.get(i)
        }
        InsnList.metaClass.equals = { obj ->
            if(delegate.size() != obj.size()) {
                Assert.failNotEquals("Instruction size not same", delegate.size(), obj.size())
                return false
            }
            for(i in 0..<delegate.size()) {
                def r = delegate.get(i).test(obj.get(i))
                if(r == false) {
                    Assert.failNotEquals("Instruction $i not same",
                            delegate.get(i).text(),
                            obj.get(i).text())
                    return false
                }
            }
            return true
        }

        Class.metaClass.getDesc = { ->
            Type.getDescriptor(delegate)
        }
        Class.metaClass.getInternalName = { ->
            Type.getInternalName(delegate)
        }
        Object.metaClass.asm = { Closure c ->
            c.delegate = new AsmNodeBuilder()
            c.resolveStrategy = Closure.DELEGATE_FIRST
            c.call()
            if(c.delegate.list.size() == 1)
                c.delegate.list.get(0)
            else
                c.delegate.list
        }
        VarInsnNode.metaClass.test = { obj ->
            return delegate.opcode == obj.opcode &&
                   delegate.var    == obj.var
        }
        VarInsnNode.metaClass.equals = { obj ->
            if(delegate.opcode != obj.opcode)
                Assert.failNotEquals("Instruction not same",
                        delegate.text(), obj.text())
            return delegate.opcode == obj.opcode &&
                   delegate.var    == obj.var
        }
        VarInsnNode.metaClass.text = { ->
            return "${OPCODES[delegate.opcode]}(var: ${delegate.var})"
        }

        MethodInsnNode.metaClass.test = { obj ->
            return delegate.opcode == obj.opcode &&
                   delegate.owner  == obj.owner  &&
                   delegate.name   == obj.name   &&
                   delegate.desc   == obj.desc
        }
        MethodInsnNode.metaClass.equals = { obj ->
            if(delegate.opcode != obj.opcode)
                Assert.failNotEquals("Instruction not same",
                        delegate.text(), obj.text())
            if(delegate.owner != obj.owner)
                Assert.failNotEquals("${OPCODES[delegate.opcode]}'s [owner] not same",
                    delegate.owner, obj.owner)
            if(delegate.name != obj.name)
                Assert.failNotEquals("${OPCODES[delegate.opcode]}'s [name] not same",
                    delegate.name, obj.name)
            if(delegate.desc != obj.desc)
                Assert.failNotEquals("${OPCODES[delegate.opcode]}'s [desc] not same",
                    delegate.desc, obj.desc)
            return true
        }
        MethodInsnNode.metaClass.text = { ->
            "${OPCODES[delegate.opcode]}(" +
                "owner: ${delegate.owner}," +
                "name: ${delegate.name},"   +
                "desc: ${delegate.desc})"
        }

        TypeInsnNode.metaClass.test = { obj ->
            return delegate.opcode == obj.opcode &&
                   delegate.desc   == obj.desc
        }
        TypeInsnNode.metaClass.equals = { obj ->
            if(delegate.opcode != obj.opcode)
                Assert.failNotEquals("Instruction not same", delegate.text(), obj.text())
            return delegate.opcode == obj.opcode &&
                   delegate.desc   == obj.desc
        }
        TypeInsnNode.metaClass.text = { ->
            "${OPCODES[delegate.opcode]}(desc: ${delegate.desc})"
        }

        LdcInsnNode.metaClass.test = { obj ->
            return delegate.opcode == obj.opcode &&
                   delegate.cst    == obj.cst
        }
        LdcInsnNode.metaClass.equals = { obj ->
            if(delegate.opcode != obj.opcode)
                Assert.failNotEquals("Instruction not same", delegate.text(), obj.text())
            return delegate.opcode == obj.opcode &&
                   delegate.cst    == obj.cst
        }
        LdcInsnNode.metaClass.text = { ->
            "${OPCODES[delegate.opcode]}(cst: ${delegate.cst})"
        }

        InsnNode.metaClass.test = { obj ->
            return delegate.opcode == obj.opcode
        }
        InsnNode.metaClass.equals = { obj ->
            if(delegate.opcode != obj.opcode)
                Assert.failNotEquals("Instruction not same", delegate.text(), obj.text())
            return delegate.opcode == obj.opcode
        }
        InsnNode.metaClass.text = { ->
            "${OPCODES[delegate.opcode]}"
        }

        FieldInsnNode.metaClass.test = { obj ->
            return delegate.opcode == obj.opcode &&
                   delegate.owner  == obj.owner  &&
                   delegate.name   == obj.name   &&
                   delegate.desc   == obj.desc
        }
        FieldInsnNode.metaClass.equals = { obj ->
            if(delegate.opcode != obj.opcode)
                Assert.failNotEquals("Instruction not same",
                    delegate.text(), obj.text())
            if(delegate.owner != obj.owner)
                Assert.failNotEquals("${OPCODES[delegate.opcode]}'s [owner] not same",
                    delegate.owner, obj.owner)
            if(delegate.name != obj.name)
                Assert.failNotEquals("${OPCODES[delegate.opcode]}'s [name] not same",
                    delegate.name, obj.name)
            if(delegate.desc != obj.desc)
                Assert.failNotEquals("${OPCODES[delegate.opcode]}'s [desc] not same",
                    delegate.desc, obj.desc)
            return true
        }
        FieldInsnNode.metaClass.text = { ->
            "${OPCODES[delegate.opcode]}("  +
                "owner: ${delegate.owner}," +
                "name: ${delegate.name},"   +
                "desc: ${delegate.desc})"
        }

        IntInsnNode.metaClass.test = { obj ->
            return delegate.opcode  == obj.opcode  &&
                   delegate.operand == obj.operand
        }
        IntInsnNode.metaClass.equals = { obj ->
            if(delegate.opcode != obj.opcode)
                Assert.failNotEquals("Instruction not same",
                    delegate.text(), obj.text())
            if(delegate.operand != obj.operand)
                Assert.failNotEquals("${OPCODES[delegate.opcode]}'s [operand] not same",
                    delegate.operand, obj.operand)
            return true
        }
        IntInsnNode.metaClass.text = {
            "${OPCODES[delegate.opcode]}(operand: ${delegate.operand})"
        }

        installed = true
    }

}