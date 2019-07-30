# Minecraft Matrix Bridge

This is a small bukkit plugin I wrote to connect a Minecraft server to a Matrix chat.

## Build

Just:
```
mvn package
```

## Deployment

After building just copy the file `target/matrixbridge-1.0-SNAPSHOT-jar-with-dependencies.jar` (make sure to get file that says "with-dependencies") and copy it into the `plugins` directory of your Minecraft server.

Rename the configuration file `matrixbridge.properties.template` to `matrixbridge.properties` and change the settings accordingly. Then copy it into the directory of your Minecraft server (not in the plugins folder). 
If the file is missing the server will create it for you on the next restart with some default values.

That should be it. Have fun. : )
