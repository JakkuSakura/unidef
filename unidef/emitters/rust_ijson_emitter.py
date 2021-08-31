from unidef.emitters.emitter_registry import Emitter
from unidef.models.config_model import ModelDefinition
from unidef.models.type_model import Type, Traits
from unidef.utils.formatter import IndentedWriter


def is_numeric(s: str) -> bool:
    try:
        int(s)
        return True
    except:
        return False


def emit_type(ty: Type, indent=0) -> str:
    formatter = IndentedWriter(indent=indent)
    if ty.get_trait(Traits.Struct):
        fields = ty.get_traits(Traits.StructField)
        if fields:
            formatter.append_line('{')
            formatter.incr_indent()
            formatter.append_line('let mut node = ijson::IObject::new();')
            for field in ty.get_traits(Traits.StructField):
                for line in field.get_traits(Traits.LineComment):
                    formatter.append_line('//{}'.format(line))
                formatter.append_line('node.insert("{field}", {value});'
                                      .format(field=field.get_trait(Traits.FieldName),
                                              value=emit_type(field, formatter.indent)))

            formatter.append_line('node')
            formatter.decr_indent()
            formatter.append('}')
        else:
            formatter.append_line('ijson::IObject::new()')
    elif ty.get_trait(Traits.RawValue) == 'undefined':
        formatter.append('Option::<ijson::IValue>::None')
    elif ty.get_trait(Traits.Bool):
        formatter.append(str(ty.get_trait(Traits.RawValue)).lower())
    elif ty.get_trait(Traits.RawValue):
        formatter.append('"{}"'.format(ty.get_trait(Traits.RawValue)))
    else:
        formatter.append('Could not process {}'.format(ty))
    return formatter.to_string()


class RustIjsonEmitter(Emitter):
    def accept(self, target: str) -> bool:
        return target == 'rust_ijson'

    def emit_model(self, target: str, model: ModelDefinition) -> str:
        return self.emit_type(target, model.get_parsed())

    def emit_type(self, target: str, ty: Type) -> str:
        return emit_type(ty)
