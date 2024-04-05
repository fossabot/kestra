package io.kestra.core.serializers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import io.kestra.core.contexts.KestraClassLoader;
import io.kestra.core.plugins.PluginScanner;
import io.kestra.core.plugins.RegisteredPlugin;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class AliasesTypeResolver extends ClassNameIdResolver {

    public AliasesTypeResolver() {
        super(null, null, null);
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) throws IOException {
        KestraClassLoader classLoader = (KestraClassLoader) Thread.currentThread().getContextClassLoader();
        Map<String, Class<?>> aliases = classLoader.getPluginRegistry().getPlugins().stream()
            .flatMap(plugin -> plugin.getAliases().entrySet().stream())
            .collect(Collectors.toMap(
                entry -> entry.getKey(),
                entry -> entry.getValue()
            ));
        PluginScanner scanner = new PluginScanner(classLoader);
        RegisteredPlugin corePlugin = scanner.scan();
        aliases.putAll(corePlugin.getAliases());

        if (aliases.containsKey(id)) {
            return context.constructType(aliases.get(id));
        }
        return super.typeFromId(context, id);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return null;
    }
}
