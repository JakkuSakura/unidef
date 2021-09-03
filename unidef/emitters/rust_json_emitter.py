from unidef.emitters.emitter_registry import Emitter
from unidef.models.config_model import ModelDefinition
from unidef.models.type_model import Type, Traits
from unidef.utils.formatter import IndentedWriter
from pydantic import BaseModel


def is_numeric(s: str) -> bool:
    try:
        int(s)
        return True
    except:
        return False


class JsonCrate(BaseModel):
    object_type: str
    array_type: str
    none_type: str
    value_type: str

    def map_value(self, ty: Type, indent=0) -> str:
        raise NotImplementedError()


class IjsonCrate(JsonCrate):
    object_type = 'ijson::IObject'
    array_type = 'ijson::IArray'
    none_type = 'Option::<ijson::IValue>::None'
    value_type = 'ijson::IValue'

    def map_value(self, ty: Type, indent=0) -> str:
        formatter = IndentedWriter(indent=indent)
        if ty.get_trait(Traits.Struct):
            fields = ty.get_traits(Traits.StructField)
            if fields:
                formatter.append_line('{')
                formatter.incr_indent()
                formatter.append_line(f'let mut node = <{json_crate.object_type}>::new();')
                for field in ty.get_traits(Traits.StructField):
                    for line in field.get_traits(Traits.LineComment):
                        formatter.append_line('//{}'.format(line))
                    formatter.append_line('node.insert("{field}".into(), {value}.into());'
                                          .format(field=field.get_trait(Traits.FieldName),
                                                  value=self.emit_type(field, formatter.indent)))

                formatter.append_line('node')
                formatter.decr_indent()
                formatter.append('}')
            else:
                formatter.append(f'<{json_crate.object_type}>::new()')
        elif ty.get_trait(Traits.RawValue) == 'undefined':
            formatter.append(f'{json_crate.none_type}')
        elif ty.get_trait(Traits.Bool):
            formatter.append(str(ty.get_trait(Traits.RawValue)).lower())
        elif ty.get_trait(Traits.RawValue):
            formatter.append('"{}"'.format(ty.get_trait(Traits.RawValue)))
        else:
            formatter.append('Could not process {}'.format(ty))
        return formatter.to_string(strip_left=True)


class SerdeJsonCrate(JsonCrate):
    object_type = 'serde_json::Map<String, serde_json::Value>'
    array_type = 'Vec<serde_json::Value>'
    none_type = 'None'
    value_type = 'serde_json::Value'

    def map_value(self, ty: Type, indent=0) -> str:
        formatter = IndentedWriter(indent=indent)
        if indent == 0:
            formatter.append('serde_json::json!(')

        if ty.get_trait(Traits.Struct):
            fields = ty.get_traits(Traits.StructField)
            if fields:
                formatter.append_line('{')
                formatter.incr_indent()
                for field in ty.get_traits(Traits.StructField):
                    for line in field.get_traits(Traits.LineComment):
                        formatter.append_line('//{}'.format(line))
                    formatter.append_line('"{field}": {value},'
                                          .format(field=field.get_trait(Traits.FieldName),
                                                  value=self.map_value(field, formatter.indent)))

                formatter.decr_indent()
                formatter.append('}')
            else:
                formatter.append('{}')
        elif ty.get_trait(Traits.RawValue) == 'undefined':
            formatter.append(f'{self.none_type}')
        elif ty.get_trait(Traits.Bool):
            formatter.append(str(ty.get_trait(Traits.RawValue)).lower())
        elif ty.get_trait(Traits.RawValue):
            formatter.append('"{}"'.format(ty.get_trait(Traits.RawValue)))
        else:
            formatter.append('Could not process {}'.format(ty))
        if indent == 0:
            formatter.append_line(')')
            formatter.decr_indent()
        return formatter.to_string(strip_left=True)


def get_json_crate(target: str) -> JsonCrate:
    if target == 'rust_ijson':
        return IjsonCrate()
    elif target == 'rust_serde_json':
        return SerdeJsonCrate()
    else:
        raise Exception(f'Could not find json crate for {target}')


class RustJsonEmitter(Emitter):
    def accept(self, target: str) -> bool:
        return 'rust' in target and 'json' in target

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        return self.emit_type(target, model.get_parsed())

    def emit_type(self, target: str, ty: Type) -> str:
        json_crate = get_json_crate(target)
        return json_crate.map_value(ty)
