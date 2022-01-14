# Unidef

Unidef stands for _define once, use everywhere_. It aims for define data structures in a unified format. It combines **parsers**,
**transformers**, and **emitters**. By supporting multiple parsers, it can use multiple data sources. By supporting multiple emitters,
the data structure can be converted into many endpoint languages.


Unidef is similar to JSON Type Definition. When I was writing this program, I'm not aware of the JSON Type Definition.
Unidef has some advantages over JSON Type Definition:

1. Unidef supports more complex and customized inferring
2. Unidef gives finer control over types
3. Unidef can do transpiling and code generation
4. Unidef supports more input types
## How to use
```shell
# add a soft link to unidef
pip install -e .
```
## Supported parsers

- [x] Unidef fields/variants
- [x] JSON message example
- [x] FIX message example
- [ ] JSON Schema
- [ ] [OpenAPI Schema object](https://spec.openapis.org/oas/v3.1.0#schemaObject)
- [ ] JavaScript
- [ ] protobuf
- [ ] JSON Type Definition

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
- [x] Replace Pydantic with typedmodel
- [ ] Replace RustLineNode, RustBulkNode, etc with jinja2 template engine for advanced indentation control
- [ ] Build a common set of AST nodes
### Improve inner representation model
Right now inner model is now mixed key-value with validations. The next step should be conversion from and to well-defined python object

### Type Inference for transpiling


### Add more tests