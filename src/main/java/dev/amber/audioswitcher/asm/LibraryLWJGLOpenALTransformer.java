package dev.amber.audioswitcher.asm;

import dev.amber.audioswitcher.launch.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class LibraryLWJGLOpenALTransformer implements ITransformer {

    @Override
    public String[] getClassName() {
        return new String[]{"paulscode.sound.libraries.LibraryLWJGLOpenAL"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode method : classNode.methods) {
            String methodName = mapMethodName(classNode, method);

            if (methodName.equals("init")) {
                for (AbstractInsnNode node : method.instructions.toArray()) {
                    if (node.getOpcode() == Opcodes.INVOKESTATIC) {
                        MethodInsnNode castedNode = (MethodInsnNode) node;
                        if (castedNode.owner.equals("org/lwjgl/openal/AL") && castedNode.name.equals("create") && castedNode.desc.equals("()V")) {
                            // Overwrite the original Al.create() call
                            method.instructions.set(castedNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/amber/audioswitcher/asm/impl/LibraryLWJGLOpenALImpl", "createAL", "()V", false));
                            break;
                        }
                    }
                }
            }
        }
    }
}
