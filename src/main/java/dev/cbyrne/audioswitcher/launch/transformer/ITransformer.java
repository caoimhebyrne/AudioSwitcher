package dev.cbyrne.audioswitcher.launch.transformer;

import org.objectweb.asm.tree.ClassNode;

public interface ITransformer {
    /**
     * The class name that's being transformed
     *
     * @return the class name
     */
    String getClassName();

    /**
     * The name of the transformer
     *
     * @return the transformer's name (e.g. LibraryLWJGLOpenALTransformer -> LibraryLWJGLOpenAL)
     */
    String getTransformerName();

    /**
     * Returns the package for the implementation class dedicated to this transformer
     */
    default String getTransformerImplName() {
        return "dev/cbyrne/audioswitcher/transformer/impl/" + getTransformerName() + "Impl";
    }

    /**
     * Perform any asm in order to transform code
     *
     * @param classNode the transformed class node
     * @param name      the transformed class name
     */
    void transform(ClassNode classNode, String name);
}
