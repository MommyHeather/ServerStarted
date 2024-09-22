package computer.heather.serverstarted.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import computer.heather.serverstarted.ServerStarted;
import computer.heather.serverstarted.config.ConfigTypes.BooleanValue;
import computer.heather.serverstarted.config.ConfigTypes.ConfigValidationEnum;
import computer.heather.serverstarted.config.ConfigTypes.FloatValue;
import computer.heather.serverstarted.config.ConfigTypes.FreeStringValue;
import computer.heather.serverstarted.config.ConfigTypes.LongValue;
import computer.heather.serverstarted.config.ConfigTypes.StringArrayValue;
import computer.heather.serverstarted.config.ConfigTypes.ValidatedStringValue;

public class ConfigManager {

    private static HashMap<String, ConfigTypes> entries = new HashMap<>();

    
    public static void register(String key, ConfigTypes configType) {
        entries.put(key, configType);
    }

    
    public static final BooleanValue enabled = new BooleanValue("config.serverstarted.enabled", true, ConfigManager::register);
    public static final LongValue delay = new LongValue("config.serverstarted.delay", 50, 1, Integer.MAX_VALUE, ConfigManager::register);
    public static final FreeStringValue message = new FreeStringValue("config.serverstarted.message", "Server Started!", ConfigManager::register);
    




    public static void loadOrCreateConfig() {
        // Called when the config needs to be loaded, but one may not exist.
        // Creates a new config it one doesn't exist, then loads it.
        File dir = new File("./config");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "ServerStarted.properties");
        if (!file.exists()) {
            writeConfig();
        }
        loadConfig();
  
    }

    private static void writeConfig() {
        // Called to write to a config file.
        // Create a complete properties file in the cwd, including any existing changes
        ServerStarted.LOGGER.info("Preparing to write to properties file...");
        File file = new File("./config/ServerStarted.properties");
        try {
            file.createNewFile();
            file.setWritable(true); 
            InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream("serverstarted-properties.txt");

            String text = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))
                  .lines()
                  .collect(Collectors.joining("\n"));

            for (String key : entries.keySet()) {
                Matcher matcher = Pattern.compile(Pattern.quote(key) + "$", Pattern.MULTILINE).matcher(text);
                text = matcher.replaceAll(key + "=" + entries.get(key).save());
            }

            FileWriter writer = new FileWriter(file);
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            // TODO : Scream to user
            e.printStackTrace();
        }
    }


    private static void loadConfig() {
        //Load the config file.
        
        Properties props = new Properties();
        File file = new File("./config/ServerStarted.properties");
        FileReader reader;
        try {
            reader = new FileReader(file);   
            props.load(reader);
            reader.close();
        } catch (IOException e) {
            // TODO : Scream to user
            e.printStackTrace();
            return;
        }

        ArrayList<String> missingProps = new ArrayList<>();

        for (String key : entries.keySet()) {
            if (!props.containsKey(key)) {
                missingProps.add(key);
                ServerStarted.LOGGER.warn("Missing key : " + key);
                continue;
            }
            ConfigValidationEnum valid = entries.get(key).validate(props.getProperty(key));
            if (valid != ConfigValidationEnum.VALID) {
                missingProps.add(key);
                ServerStarted.LOGGER.warn(valid.getError() + " : " + key);
                continue;

            }
            entries.get(key).load(props.getProperty(key));
        }


        if (!missingProps.isEmpty()) {
            ServerStarted.LOGGER.warn("The following properties were missing from the loaded file :");
            for (String string : missingProps) {
                ServerStarted.LOGGER.warn(string);
            }
            ServerStarted.LOGGER.warn("Properties file will be regenerated! Existing config values will be preserved.");

            writeConfig();
        }
        

    }
}
