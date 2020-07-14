# DummyMapper 🗎

Intellij Idea plugin for mapping Java Classes to formats like JSON/AVRO/GraphQL and others.

## Content
- [Installation](#installation)
- [Format Support](#format-support)
  - [Json](#json)
  - [Json Schema](#json-schema)
  - [Avro Schema](#avro-schema)
  - [GraphQL](#graphql)
- [Limitations](#limitations)
- [Version History](#version-history)

## Installation

- Install using IDEA Plugin site:
  - Navigate to [site](https://plugins.jetbrains.com/plugin/6317-lombok) and click *install* button there.
- Using IDE built-in plugin system on Windows:
  - <kbd>File</kbd> > <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Browse repositories...</kbd> > <kbd>Search for "DummyMapper"</kbd> > <kbd>Install Plugin</kbd>
- Using IDE built-in plugin system on MacOs:
  - <kbd>Preferences</kbd> > <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Browse repositories...</kbd> > <kbd>Search for "DummyMapper"</kbd> > <kbd>Install Plugin</kbd>
- Manually:
  - Download the [latest release](https://github.com/goodforgod/DummyMapper/releases/latest) and install it manually using <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Install plugin from disk...</kbd>

Restart IDE.

## Format Support

Plugin allow mapping Java POJO classes to different formats, 
this section describes information about supported formats and their options.

All format examples will be showed according to this class as exaple.

```java
public class User {

    private class Credential {
        private String id;
        private long issued;
    }

    private UUID id;
    private String name;
    private List<String> roles;
    private Credential credential;
}
```

### Json

Allow mapping Java class as JSON example with fields filled with
random values as if class example was serialized to JSON format.
- <kbd>Mapping options..</kbd> > <kbd>Map as JSON</kbd>

```json
{
  "id" : "76fbb591-8e95-4df7-94ad-5d0606709141",
  "name" : "Alan",
  "roles" : [ "herbalist", "recycling-officer", "exploration-geologist" ],
  "credential" : {
    "id" : "a7ec6315-4292-454c-ac69-e61a7a36e07d",
    "issued" : 1565743683
  }
}
```

Also allow mapping class as array of JSONs.
- <kbd>Mapping options..</kbd> > <kbd>Map as JSON Array</kbd>

You can specify number of entries to generate in array.

```json
[
  {
    "id": "526686d0-8d84-4b85-a751-77fa7a157a69",
    "name": "Rachel",
    "roles": [ "fitness-centre-manager", "plant-breeder", "television-production-assistant" ],
    "credential": {
      "id": "94f349c9-405a-4921-8dbf-8abfacc28cf1",
      "issued": 1548018277
    }
  }
]
```

### Json Schema

Allow mapping Java class as JSON Schema, 3 different drafts are available:
- [Draft 2019](https://json-schema.org/draft/2019-09/schema)
- [Draft 7](https://json-schema.org/draft-07/json-schema-release-notes.html)
- [Draft 6](https://json-schema.org/draft-06/json-schema-release-notes.html)

Mapping is under:
- <kbd>Mapping options..</kbd> > <kbd>Map as JSON Schema</kbd>

```json
{
  "$schema": "https://json-schema.org/draft/2019-09/schema",
  "type": "object",
  "properties": {
    "credential": {
      "type": "object",
      "properties": {
        "id": {
          "type": "string"
        },
        "issued": {
          "type": "integer"
        }
      }
    },
    "id": {
      "type": "string"
    },
    "name": {
      "type": "string"
    },
    "roles": {
      "type": "array",
      "items": {
        "type": "string"
      }
    }
  }
}
```

### Avro Schema

Allow mapping Java class as AVRO Schema for [version 1.9.2](https://avro.apache.org/docs/1.9.2/)

There is option for two different AVRO Schema builders like:
- [Jackson]()
- [Apache]()

Mapping is under:
- <kbd>Mapping options..</kbd> > <kbd>Map as AVRO Schema</kbd>

```json
{
  "type" : "record",
  "name" : "User",
  "namespace" : "io.goodforgod.dummymapper",
  "fields" : [ {
    "name" : "id",
    "type" : {
      "type" : "record",
      "name" : "UUID",
      "namespace" : "java.util",
      "fields" : [ ]
    }
  }, {
    "name" : "name",
    "type" : "string"
  }, {
    "name" : "roles",
    "type" : {
      "type" : "array",
      "items" : "string",
      "java-class" : "java.util.List"
    }
  }, {
    "name" : "credential",
    "type" : {
      "type" : "record",
      "name" : "Credential",
      "fields" : [ {
        "name" : "id",
        "type" : "string"
      }, {
        "name" : "issued",
        "type" : "long"
      } ]
    }
  } ]
}
```

### GraphQL

Allow mapping Java class as GraphQL query with [version v14](https://www.graphql-java.com/documentation/v14)

Mapping is under:
- <kbd>Mapping options..</kbd> > <kbd>Map as GraphQL</kbd>

```text
schema {
  query: Query
}

type Credential {
  id: String
  issued: Long!
}

#Query root
type Query {
  credential: Credential
  id: UUID
  name: String
  roles: [String]
}

#Long type
scalar Long

#Unrepresentable type
scalar UNREPRESENTABLE

#UUID String
scalar UUID
```

## Limitations

- Enums
- Getters, Setters

## Version History

**1.0.0** - Initial project with support for Json, Json array, Json Schema, Avro Schema, GraphQL formats.

## Licence

This project licensed under the MIT - see the [LICENSE](LICENSE) file for details.
