package dev.cbyrne.audioswitcher.launch.transformer;

import org.objectweb.asm.tree.ClassNode;

public interface ITransformer {

    /**
     * The class name that's being transformed
     *
     * @return the class name
     */
    String[] getClassName();

    /**
     * Perform any asm in order to transform code
     *
     * @param classNode the transformed class node
     * @param name      the transformed class name
     */
    void transform(ClassNode classNode, String name);
}
