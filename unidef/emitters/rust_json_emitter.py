from unidef.emitters.registry import Emitter
from unidef.models.config_model import ModelDefinition
from unidef.models.type_model import Type, Traits
from unidef.utils.formatter import IndentedWriter
from unidef.utils.typing_compat import *
from pydantic import BaseModel


class JsonCrate(BaseModel):
    object_type: str
    array_type: str
    none_type: str
    value_type: str
    no_macro: bool = False

    def process_raw_value(self, s: Any) -> str:
        return f'"{s}"'

    def map_value(self, ty: Type, indent=0) -> str:
        formatter = IndentedWriter(indent=indent)
        if ty.get_trait(Traits.Struct):
            fields = ty.get_traits(Traits.StructField)
            if fields:
                formatter.append_line('{')
                formatter.incr_indent()
                formatter.append_line(f'let mut node = <{self.object_type}>::new();')
                for field in fields:
                    for line in field.get_traits(Traits.LineComment):
                        formatter.append_line('//{}'.format(line))
                    formatter.append_line('node.insert("{field}".into(), {value}.into());'
                                          .format(field=field.get_trait(Traits.FieldName),
                                                  value=self.map_value(field, formatter.indent).strip()))

                formatter.append_line('node')
                formatter.decr_indent()
                formatter.append('}')
            else:
                formatter.append(f'<{self.object_type}>::new()')
        elif ty.get_trait(Traits.Vector):
            traits = ty.get_traits(Traits.ValueType)
            if self.no_macro:
                if traits:
                    formatter.append_line('{')
                    formatter.incr_indent()
                    formatter.append_line('let mut node = Vec::new();')
                    for field in traits:
                        for line in field.get_traits(Traits.LineComment):
                            formatter.append_line('//{}'.format(line))
                        formatter.append_line(
                            'node.push({});'.format(self.map_value(field, formatter.indent)))
                    formatter.append_line('node')
                    formatter.decr_indent()
                    formatter.append_line('}')
                else:
                    formatter.append('Vec::new()')
            else:
                formatter.append('vec![')
                for field in ty.get_traits(Traits.ValueType):
                    formatter.append(self.map_value(field, formatter.indent))
                    formatter.append(',')
                formatter.append(']')
        elif ty.get_trait(Traits.RawValue) == 'undefined':
            formatter.append(f'{self.none_type}')
        elif ty.get_trait(Traits.Bool):
            formatter.append(str(ty.get_trait(Traits.RawValue)).lower())
        elif ty.get_trait(Traits.RawValue):
            formatter.append(self.process_raw_value(ty.get_trait(Traits.RawValue)))
        else:
            formatter.append('Could not process {}'.format(ty))
        return formatter.to_string(strip_left=True)


class IjsonCrate(JsonCrate):
    object_type = 'ijson::IObject'
    array_type = 'ijson::IArray'
    none_type = 'Option::<ijson::IValue>::None'
    value_type = 'ijson::IValue'


class SerdeJsonCrate(JsonCrate):
    object_type = 'serde_json::Map<String, serde_json::Value>'
    array_type = 'Vec<serde_json::Value>'
    none_type = 'serde_json::json!(null)'
    value_type = 'serde_json::Value'
    only_outlier = False

    def map_value(self, ty: Type, indent=0) -> str:
        if self.no_macro:
            return super().map_value(ty, indent)
        formatter = IndentedWriter(indent=indent)
        if self.only_outlier and indent == 0:
            formatter.append('serde_json::json!(')

        if ty.get_trait(Traits.Struct):
            if not self.only_outlier:
                formatter.append('serde_json::json!(')
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
            if not self.only_outlier:
                formatter.append(')')
        elif ty.get_trait(Traits.Vector):
            formatter.append('vec![')
            for field in ty.get_traits(Traits.ValueType):
                formatter.append(self.map_value(field, formatter.indent))
                formatter.append(',')
            formatter.append(']')
        elif ty.get_trait(Traits.RawValue) == 'undefined':
            formatter.append(f'{self.none_type}')
        elif ty.get_trait(Traits.Bool):
            formatter.append(str(ty.get_trait(Traits.RawValue)).lower())
        elif ty.get_trait(Traits.RawValue):
            formatter.append(self.process_raw_value(ty.get_trait(Traits.RawValue)))
        else:
            formatter.append('Could not process {}'.format(ty))
        if self.only_outlier and indent == 0:
            formatter.append(')')
        return formatter.to_string(strip_left=True)


def get_json_crate(target: str) -> JsonCrate:
    if 'ijson' in target:
        result = IjsonCrate()
    elif 'serde_json' in target:
        result = SerdeJsonCrate()
    else:
        raise Exception(f'Could not find json crate for {target}')
    if 'no_macro' in target:
        result.no_macro = True
    return result


class RustJsonEmitter(Emitter):
    def accept(self, target: str) -> bool:
        return 'rust' in target and 'json' in target

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        return self.emit_type(target, model.get_parsed())

    def emit_type(self, target: str, ty: Type) -> str:
        json_crate = get_json_crate(target)
        return json_crate.map_value(ty)
