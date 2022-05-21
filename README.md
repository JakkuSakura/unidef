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
# scala version
sbt publishLocal
sbt nativeImageRunAgent
sbt nativeImage
# python version
pip install -e .
```
## Supported parsers

- [x] Unidef fields/variants
- [x] JSON message example
- [x] FIX message example
- [x] JSON Schema
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


### Reimplementation

I decided to rewrite it with scala 3. Because

1. Python is too slow
2. Python is not expressive with pattern matching
3. Has GC, and no need to care about machine level details compared with Rust

Some reasoning:

TyNode is an independent concept that cannot exist on its own
AstNode is a standalone concept, the minimal element to represent everything


### Scala tasty version
I try to implement the tasty version to

1. support parsing scala 3
2. implement a interpreter in the same time
3. try to do some optimization work(similar to LMS, but without intruding the original language)
4. leave space for a transpile framework

### TODOs
- [ ] Add more tests for json schema
- [ ] Support anonymous struct with fields in yaml
- [ ] Support anonymous enum in yaml
- [ ] Use automatically generated TyNode and AstNode(self bootstrap)
- [ ] Use circe.json.HCursor to spot the problem
- [ ] Support save actions: formatter, etc
- [ ] Simple SVN for code generated files
- [ ] Support most of what python version can do: Rust structs, FIX, sql ddl, python peewee/pydantic model, etc
- [ ] Workflow: Generic AST + Extendable <=> Specific AST with static type <=> Source code
- [ ] Deprecate Velocity with simple Scala interpolation
- [ ] Replace SQLParser with Druid SQL parser, since the latter is most performant and feature complete
