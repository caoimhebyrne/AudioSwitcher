package dev.cbyrne.audioswitcher.asm;

import dev.cbyrne.audioswitcher.launch.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

public class LibraryLWJGLOpenALTransformer implements ITransformer {
    @Override
    public String[] getClassName() {
        return new String[]{"paulscode.sound.libraries.LibraryLWJGLOpenAL"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("init")) {
                for (AbstractInsnNode insnNode : method.instructions.toArray()) {
                    if (insnNode instanceof MethodInsnNode && insnNode.getOpcode() == Opcodes.INVOKESTATIC) {
                        if (((MethodInsnNode) insnNode).name.equals("create")) {
                            method.instructions.set(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/cbyrne/audioswitcher/asm/impl/LibraryLWJGLOpenALImpl", "createAL", "()V", false));
                            break;
                        }
                    }
                }
            }
        }
    }
}
