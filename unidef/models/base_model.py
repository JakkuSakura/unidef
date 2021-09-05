from pydantic import BaseModel
from pydantic.fields import FieldInfo
from unidef.utils.typing_compat import *
from beartype import beartype


class MyField(BaseModel):
    key: str
    default_absent: Any = None
    default_present: Any = None
    value: Any = None

    def __init__(self, **kwargs):
        if 'value' not in kwargs:
            kwargs['value'] = kwargs.get('default_present')
        super().__init__(**kwargs)

    def __call__(self, value: Any) -> __qualname__:
        field = self.copy(deep=True)
        field.value = value
        return field

    def __str__(self):
        return self.__repr__()

    def __repr__(self):
        return f'{self.key}: {self.value}'


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
        assert field.value is not None, f'value of {field.key} should not be None'
        value = self.fields.get(field.key)
        if value is not None:
            assert isinstance(value, list) and isinstance(field.default_present, list), \
                f'{field.key} is not list, cannot be appended multiple times'
            value.append(field.value)
        else:
            self.replace_field(field)

        return self

    @beartype
    def extend_field(self, field: MyField, values: Iterable[Any]) -> __qualname__:
        assert not self.is_frozen()
        assert isinstance(field.default_present, list), f'default value of {field.key} is not list'
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
        if isinstance(field.default_present, list) and not isinstance(field.value, list):
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
        return self.fields.get('frozen')

    def freeze(self) -> __qualname__:
        self.fields['frozen'] = True
        return self

    def unfreeze(self) -> __qualname__:
        self.fields['frozen'] = False
        return self

    def copy(self, *args, **kwargs) -> __qualname__:
        kwargs['deep'] = True
        this = super().copy(*args, **kwargs)
        this.unfreeze()
        return this

    def __str__(self):
        return f'{type(self).__name__}{self.fields}'

    def __repr__(self):
        return self.__str__()
