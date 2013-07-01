ThinSchema
==========

Copyright (C) 2013, Albert Tedja.

Database migration has always been a pain-staking process. Keeping track and managing migration is even more so, especially when you have released so many different database version on your Android app. 
This library is provided to make it easier to migrate and manage your database schemas. The schema can be written in JSON format. There will be support for other formats (YAML and XML) in the future.

## Why?

When releasing client-side applications, you are responsible in managing all the database versions that you have released. It becomes increasingly complex to handle database upgrades as the number of versions increases.

## What This Project Is

This project is an Android Library project, and also contains the test project. To run the test, run `ant debug install && ant test` from the test project. Make sure you have an emulator running.

See Installation Guide below for how to use ThinSchema in your project.

## Installation Guide

Update project so the ant tools can point to your local Android SDK installation.

`android update project --path <thinschema_project_folder>`

This project is a library project. You can include it in your main application project. Please refer to the official Android documentation on how to do so.

Additionally, you can also create a JAR file by executing:

```
ant debug  // or ant release
ant jar
```

It will create `org.thinschema.jar` in the project folder. Copy it to your main project's `lib/` folder, and you can start using it!

## How do I use it?

1. Create a JSON Schema file, and include that somewhere in your project (e.g. `res/raw`)

2. Create an instance of DatabaseManager, and pass your JSON to it.

```java
// Assuming we have a method loadFromRaw that takes a resource id and returns the JSONObject
JSONObject json = loadFromRaw(R.raw.my_schema);
JSONDBSchema jsonSchema = new JSONDBSchema(json);
DatabaseManager dm = new DatabaseManager(this, jsonSchema);
```

DatabaseManager will handle all creation and migration automatically!

## Compatibility

This project is designed for the Android operating system.

## Database Schema Format

Database:

```
{
  "name": "YourDatabaseName",
  "version": DatabaseVersion,
  "tables" : [ .. ]
}
```

Tables:

```
{
  "name": "TableName",
  "autoPrimaryKey": true | false, # Auto generate a primary key column named '_id'
  "columns": [ .. ]
}
```

Columns:

```
{
  "name": "ColumnName",
  "type": "integer" | "int" | "string" | "text" | "float" | "double" | "real",
  "isPrimary": true | false,
  "autoIncrement": true | false,
  "notNull": true | false,
  "defaultValue": DefaultValue 
}
```

##### Example:

```
{
  "name": "Students.db",
  "version": 1,
  "tables": [
    {
      "name": "Students",
      "columns": [
        {
          "name": "_id",
          "type": "integer",
          "isPrimary": true,
          "autoIncrement": true,
          "notNull": true
        },
        {
          "name": "FirstName",
          "type": "text",
          "notNull": true
        },
        {
          "name": "LastName",
          "type": "text",
          "notNull": true,
        },
        {
          "name": "IDNumber",
          "type": "integer",
          "notNull": true
        }
      ]
    },
    {
      "name": "StudentsClassRelations",
      "columns": [
        {
          "name": "_id",
          "type": "integer",
          "isPrimary": true,
          "autoIncrement": true,
          "notNull": true
        },
        {
          "name": "StudentID",
          "type": "integer",
          "notNull": true
        },
        {
          "name": "ClassID",
          "type": "integer",
          "notNull": true
        }
      ]
    }
  ]
}
```

### Enjoy!


