# Unidef

Unidef stands for _define once, use everywhere_. It aims for define data structures in a unified format. It combines **parsers**,
**transformers**, and **emitters**. By supporting multiple parsers, it can use multiple data sources. By supporting multiple emitters,
the data structure can be converted into many endpoint languages.

## Supported parsers

- [x] Unidef fields/variants
- [x] JSON message example
- [x] FIX message example
- [ ] JSON Schema
- [ ] [OpenAPI Schema object](https://spec.openapis.org/oas/v3.1.0#schemaObject)
- [ ] JavaScript
- [ ] protobuf

## Supported emitters
- [x] Python peewee
- [ ] Python pydantic
- [x] Rust serde struct
- [x] Rust serde_json/ijson
- [x] SQL definition
- [ ] JSON Schema
- [ ] OpenAPI Schema
- [ ] JavaScript

## Future plan

### Improve inner representation model
Right now inner model is fixed key-value without validations. The next step should be conversion from and to well-defined python object

### Type Inference for transpiling
