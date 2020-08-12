# DummyMapper 🗎

[Intellij IDEA plugin](https://plugins.jetbrains.com/plugin/dummymapper) 
for mapping Java Classes to formats like [JSON](#json)/[AVRO](#avro-schema)/[GraphQL](#graphql)/etc.

![](https://media.giphy.com/media/L1W2twkGRQWPfR8myz/giphy.gif)

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

- Install using [Intellij Plugin marketplace](https://plugins.jetbrains.com/plugin/dummymapper):
  - Navigate to [site](https://plugins.jetbrains.com/plugin/dummymapper) and click *install* button there.
- Using IDE built-in plugin system on:
  - **[Windows]** <kbd>File</kbd> > <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "DummyMapper"</kbd> > <kbd>Install</kbd>
  - **[MacOS]** <kbd>Preferences</kbd> > <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "DummyMapper"</kbd> > <kbd>Install</kbd>
- Manually:
  - Download the [latest release](https://github.com/goodforgod/DummyMapper/releases/latest), compile with *./gradlew buildPlugin* and install it manually using <kbd>File</kbd> > <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install Plugin from Disk...</kbd>

## Format Support

Plugin allow mapping Java Classes to different formats, 
this section describes information about supported formats and their options.

All format examples will be showed according to this class as example.
Keep in mind that mapping is always based on class **fields**, not its *getters\setters*.

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

Plugin uses [DummyMaker library](https://github.com/GoodforGod/dummymaker) for producing Java Classes and then converting them in JSON format.

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

![](https://media.giphy.com/media/L1W2twkGRQWPfR8myz/giphy.gif)

#### Annotations Support

Annotations from [Jackson](https://www.baeldung.com/jackson-annotations) are supported when serializing to JSON, 
but keep in mind that only *fields* used for serialization, not *getters*. 

However, [Jackson annotations](https://www.baeldung.com/jackson-annotations) from respected fields *getters*
will be applied during serialization if found, but such behavior is not guarantied to be always correct.

Example for *User* class:
```java
public class User {
    
    private UUID id;
    @JsonProperty(value = "firstName")
    private String name;
    private String surname;
    @JsonIgnore
    private List<String> roles;

    @JsonIgnore
    public String getSurname() {
        return surname;
    }
}
```

Mapping *User* class as JSON will result in (most of [Jackson annotations](https://www.baeldung.com/jackson-annotations)
and their parameters are supported):
```json
{
  "id": "975e80ed-b95d-46b2-9338-519ff7083dd3",
  "firstName": "Alfred"
}
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

![](https://media.giphy.com/media/YMY6Rd9fxUkIU4BoKF/giphy.gif)

### Avro Schema

Allow mapping Java class as AVRO Schema for [version 1.9.2](https://avro.apache.org/docs/1.9.2/)

There is option for two different AVRO Schema builders like:
- [Jackson](https://github.com/FasterXML/jackson-dataformats-binary/tree/master/avro)
- [Apache](https://avro.apache.org/docs/1.9.2/gettingstartedjava.html)

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

#### Jackson Annotation Support

[Jackson annotations](https://www.baeldung.com/jackson-annotations) are supported when serializing to AVRO Schema, 
but keep in mind that only *fields* used for serialization, not *getters*. 

Most of [Jackson annotations](https://www.baeldung.com/jackson-annotations)
and their parameters are supported.

Example on how to mark field as required (Option *Required By Default* apply such transformation for all fields if selected):
```java
public class User {
    
    @JsonProperty(required = true)
    private UUID id;
    private String name;
    private String surname;
}
```

#### Apache Annotation Support

Annotations from [Apache Avro](https://github.com/apache/avro/tree/master/lang/java/avro/src/main/java/org/apache/avro/reflect) library supported when serializing to AVRO Schema.

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

#### Annotation Support

Annotations from [GraphQL SPQR](https://github.com/leangen/graphql-spqr/tree/master/src/main/java/io/leangen/graphql/annotations) library supported when serializing to GraphQL.

## Limitations

There some limitations for plugin when mapping classes:
- *Enums* are not supported and thus will be displayed as *String* fields, except for *Map as JSON* where enum values will be generated correctly.
This issue is due to library used in plugin unable to create Java Enums.
- Getters, Setters not used for any mapping, *only annotations from getters* are used for mapping (depends on operation)
if found. Standard Java naming convention expected from *getters* to be found.
- Annotations Class Params are not supported. Thus, most of annotation parameters like String, Boolean, etc. are supported, but Class or other complex parameters not supported.

## Version History

**1.0.0** - Initial project with support for Json, Json array, Json Schema, Avro Schema, GraphQL formats.

## Licence

This project licensed under the MIT - see the [LICENSE](LICENSE) file for details.
