package dev.asbyth.asmworkspace.tweaker.asm;

import dev.asbyth.asmworkspace.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class MinecraftTransformer implements ITransformer {

    @Override
    public String[] getClassName() {
        // assign the full class name
        return new String[]{"net.minecraft.client.Minecraft"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        // loop through all methods
        for (MethodNode method : classNode.methods) {
            // map the method to searge
            String methodName = mapMethodName(classNode, method);

            // check qualifying names (mcp, srg)
            if (methodName.equals("startGame") || methodName.equals("func_71384_a")) {
                // insert an instruction list before final return
                method.instructions.insertBefore(method.instructions.getLast().getPrevious(), sayBruh());
                // stop looping through methods as we've found the one we need
                break;
            }
        }
    }

    // creates "System.out.println("bruh");"
    private InsnList sayBruh() {
        // create a new instruction list
        InsnList list = new InsnList();
        // get the field "out" from the class "System"
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
        // add "bruh" to the println parameters
        list.add(new LdcInsnNode("bruh"));
        // invoke System.out.println(String)
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
        // return the new set of instructions
        return list;
    }
}
