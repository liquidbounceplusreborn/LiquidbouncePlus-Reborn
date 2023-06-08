package net.ccbluex.liquidbounce.file.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.file.FileConfig;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.value.Value;
import org.lwjgl.input.Keyboard;
import java.io.*;
import java.util.Map;

public class ProfilesConfig extends FileConfig {
    public float yPos = 0F;
    /**
     * Constructor of config
     *
     * @param file of config
     */
    public ProfilesConfig(File file) {
        super(file);
    }

    /**
     * Load config from file
     *
     * @throws IOException IOException
     */
    @Override
    public void loadConfig() throws IOException {
        final JsonElement jsonElement = new JsonParser().parse(new BufferedReader(new FileReader(getFile())));

        if(jsonElement instanceof JsonNull)
            return;
        for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
            final Module module = LiquidBounce.moduleManager.getModule(entry.getKey());
            if (module != null) {
                final JsonObject jsonModule = (JsonObject) entry.getValue();
                if(jsonModule.has("Active"))
                    module.setState(jsonModule.get("Active").getAsBoolean());
                if(jsonModule.has("KeyBind"))
                    module.setKeyBind(Keyboard.getKeyIndex(jsonModule.get("KeyBind").getAsString()));
                if(!module.getValues().isEmpty())
                    for (final Value<?> moduleValue : module.getValues()) {
                        final JsonElement element = jsonModule.get(moduleValue.getName());
                        if (element != null)
                            moduleValue.fromJson(element);
                    }
            }
        }
    }

    /**
     * Save config to file
     *
     * @throws IOException IOException
     */
    @Override
    public void saveConfig() throws IOException {
        final JsonObject jsonObject = new JsonObject();
        LiquidBounce.moduleManager.getModules().forEach(module -> {
            final JsonObject jsonModule = new JsonObject();
            jsonModule.addProperty("Active", module.getState());
            jsonModule.addProperty("KeyBind", Keyboard.getKeyName(module.getKeyBind()));
            if(!module.getValues().isEmpty())
                module.getValues().forEach(value -> jsonModule.add(value.getName(), value.toJson()));
            jsonObject.add(module.getName(), jsonModule);
        });

        final PrintWriter printWriter = new PrintWriter(new FileWriter(getFile()));
        printWriter.println(FileManager.PRETTY_GSON.toJson(jsonObject));
        printWriter.close();
    }
}
