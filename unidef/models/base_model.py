import copy
import typing

from pydantic import BaseConfig, BaseModel
from pydantic.class_validators import Validator
from pydantic.error_wrappers import ErrorWrapper
from pydantic.fields import ModelField

from unidef.utils.typing import *
from .typed_field import FieldValue, TypedField


class DynamicBaseModel(BaseModel):
    __root__: Dict[str, Any] = {}

    @property
    def fields(self):
        return self.__root__

    def __init__(self, **kwargs):
        super().__init__(**kwargs)

    @beartype
    def append_field(self, field: FieldValue) -> __qualname__:
        assert not self.is_frozen()

        value = self.fields.get(field.key)
        if value is not None:
            value.extend(field.value)
        else:
            self.replace_field(field)

        return self

    @beartype
    def replace_field(self, field: FieldValue) -> __qualname__:
        assert not self.is_frozen()
        self.fields[field.key] = field.value
        return self

    @beartype
    def remove_field(self, field: TypedField) -> __qualname__:
        assert not self.is_frozen()
        if field.key in self.fields:
            self.fields.pop(field.key)
        return self

    def get_field(self, field: TypedField) -> Any:
        if field.key in self.fields:
            return self.fields.get(field.key)
        else:
            return field.default

    def get_field_opt(self, field: TypedField) -> Optional[Any]:
        if field.key in self.fields:
            return self.fields.get(field.key)
        else:
            return None

    def exist_field(self, field: TypedField) -> bool:
        return field.key in self.fields

    def keys(self) -> List[str]:
        return list(self.fields.keys())

    def __iter__(self):
        yield from self.fields.items()

    def is_frozen(self) -> bool:
        return self.fields.get("frozen")

    def freeze(self) -> __qualname__:
        self.fields["frozen"] = True
        return self

    def unfreeze(self) -> __qualname__:
        self.fields["frozen"] = False
        return self

    def copy(self, *args, **kwargs) -> __qualname__:
        kwargs["deep"] = True
        this = super().copy(*args, **kwargs)
        this.unfreeze()
        return this

    def __str__(self):
        return f"{type(self).__name__}{self.fields}"

    def __repr__(self):
        return self.__str__()
