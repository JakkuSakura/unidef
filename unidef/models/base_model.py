import copy

from beartype import beartype
from pydantic import BaseConfig, BaseModel
from pydantic.class_validators import Validator
from pydantic.error_wrappers import ErrorWrapper
from pydantic.fields import ModelField

from unidef.utils.typing_compat import *


def get_validator(default, allow_none):
    def inner(val):
        if not allow_none and val is None:
            return Validator(f"Type should be None")
        if default is not None and type(default) != type(val):
            raise ValueError(f"Type does not match {type(default)} != {type(val)}")
        return val

    return Validator(inner)


class MyField:
    def __init__(
        self,
        key,
        default_present=None,
        default_absent=None,
        allow_none=False,
        field=None,
    ):

        if field is None:
            field = ModelField(
                name=key,
                type_=Any,
                class_validators={"value": get_validator(default_present, allow_none)},
                required=True,
                model_config=BaseConfig,
                default_factory=lambda: copy.deepcopy(default_absent),
            )
        self.key: str = key
        self.field: ModelField = field
        self.default_present = default_present
        self.value_: Any = None

    @property
    def value(self):
        if self.value_ is not None:
            return self.value_
        else:
            return self.default_present

    @property
    def default_absent(self) -> Any:
        return self.field.get_default()

    def __call__(self, value: Any) -> __qualname__:
        field = copy.copy(self)
        field_name, validate_result = field.field.validate(value, {}, loc="")
        if isinstance(validate_result, ErrorWrapper):
            raise validate_result.exc

        field.value_ = value
        return field


class MyBaseModel(BaseModel):
    __root__: Dict[str, Any] = {}

    @property
    def fields(self):
        return self.__root__

    def __init__(self, **kwargs):
        super().__init__(**kwargs)

    @beartype
    def append_field(self, field: MyField) -> __qualname__:
        assert not self.is_frozen()
        value = self.fields.get(field.key)
        if value is not None:
            assert isinstance(value, list) and isinstance(
                field.default_present, list
            ), f"{field.key} is not list, cannot be appended multiple times"
            value.append(field.value)
        else:
            self.replace_field(field)

        return self

    @beartype
    def extend_field(self, field: MyField, values: Iterable[Any]) -> __qualname__:
        assert not self.is_frozen()
        assert isinstance(
            field.default_present, list
        ), f"default value of {field.key} is not list"
        fields = []
        for value in values:
            fields.append(value)
        if field.key not in self.fields:
            self.fields[field.key] = field.default_present[:]
        self.fields[field.key].extend(fields)
        return self

    @beartype
    def replace_field(self, field: MyField) -> __qualname__:
        assert not self.is_frozen()
        if isinstance(field.default_present, list) and not isinstance(
            field.value, list
        ):
            self.fields[field.key] = [field.value]
        else:
            self.fields[field.key] = field.value
        return self

    @beartype
    def remove_field(self, field: MyField) -> __qualname__:
        assert not self.is_frozen()
        if field.key in self.fields:
            self.fields.pop(field.key)
        return self

    def get_field(self, field: MyField) -> Any:
        if field.key in self.fields:
            return self.fields.get(field.key)
        else:
            return field.default_absent

    def get_field_by_name(self, name: str) -> Any:
        return self.fields.get(name)

    def exist_field(self, field: MyField) -> bool:
        return field.key in self.fields

    def exist_field_by_name(self, name: str) -> bool:
        return name in self.fields

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
