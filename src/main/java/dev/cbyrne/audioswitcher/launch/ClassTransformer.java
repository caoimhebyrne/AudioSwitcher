package dev.cbyrne.audioswitcher.launch;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.cbyrne.audioswitcher.asm.LibraryLWJGLOpenALTransformer;
import dev.cbyrne.audioswitcher.launch.transformer.ITransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;

public class ClassTransformer implements IClassTransformer {
    private static final Logger LOGGER = LogManager.getLogger("AudioSwitcher: ClassTransformer");
    private final Multimap<String, ITransformer> transformerMap = ArrayListMultimap.create();

    public ClassTransformer() {
        registerTransformer(new LibraryLWJGLOpenALTransformer());
    }

    private void registerTransformer(ITransformer transformer) {
        for (String cls : transformer.getClassName()) {
            transformerMap.put(cls, transformer);
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        try {
            if (bytes == null) return null;

            Collection<ITransformer> transformers = transformerMap.get(transformedName);
            if (transformers.isEmpty()) return bytes;

            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.EXPAND_FRAMES);

            for (ITransformer transformer : transformers) {
                transformer.transform(node, transformedName);
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            node.accept(writer);

            return writer.toByteArray();
        } catch (Throwable t) {
            LOGGER.error("Exception occurred whilst transforming {}. Not transforming this class.", transformedName);
            t.printStackTrace();

            return bytes;
        }
    }
}
