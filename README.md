ThinSchema
==========

Copyright (C) 2013, Albert Tedja.

Database migration has always been a pain-staking process. Keeping track and managing migration is even more so, especially when you have released so many different database version on your Android app. 
This library is provided to make it easier to migrate and manage your database schemas. The schema is written in JSON format. 

## Why?

When releasing client-side applications, you are responsible in managing all the database versions that you have released. It becomes increasingly complex to handle database upgrades as the number of versions increases.

## What This Project Is

This project actually contains the tests for ThinSchema. To run the test, run `ant debug`.  You will get a debug apk in the `bin/` folder. Load it up on emulator or your device, click Run Test, and see the tests pass.

See Installation Guide below for how to use ThinSchema in your project.

## Installation Guide

Create a JAR file by executing:

```
ant jar
```

It will create `org.thinschema.jar` in the project folder. Copy it to your project's `lib/` folder, and you can start using it!

## How do I use it?

1. Create a JSON Schema file, and include that somewhere in your project (e.g. `res/raw`)

2. Create an instance of DatabaseManager, and pass your JSON to it.

```java
// Assuming we have a method loadFromRaw that takes a resource id and returns the JSONObject
JSONObject json = loadFromRaw(R.raw.my_schema);
JSONDBSchema jsonSchema = new JSONDBSchema(json);
DatabaseManager dm = new DatabaseManager(this, jsonSchema);
```

3. DatabaseManager will handle all creation and migration automatically!

## Compatibility

This project is designed for use on Android operating system.

## Database Schema Format

WIP. Please see the test files located in `res/raw` to learn more about the format of the JSON schema for now.

### Enjoy!


