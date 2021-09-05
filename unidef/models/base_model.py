from pydantic import BaseModel
from pydantic.fields import FieldInfo


class MyField(BaseModel):
    key: str
    value: Any = None

    def default(self, default: Any) -> __qualname__:
        return self(default)

    def __call__(self, value: Any) -> __qualname__:
        field = self.copy(deep=True)
        field.value = value
        return field

    def __str__(self):
        return self.__repr__()

    def __repr__(self):
        return f'{self.key}: {self.value}'


class MyBaseModel(BaseModel):
    """
    Type is the type model used in this program.
    It allows inheritance and multiple traits, similar to those in Rust and Java, as used in many other languages.
    """
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
            assert isinstance(value, list), f'{field.key} is not list, cannot be appended multiple times'
        else:
            self.fields[field.key] = field.value
        return self

    @beartype
    def extend_fields(self, field: MyField, values: Iterable[Any]) -> __qualname__:
        assert not self.is_frozen()
        assert isinstance(field.value, list), f'default value of {field.key} is not list'
        for value in values:
            self.fields[field.key] = value
        return self

    @beartype
    def replace_field(self, trait: MyField) -> __qualname__:
        assert not self.is_frozen()
        self.fields[trait.key] = trait.value
        return self

    @beartype
    def remove_trait(self, trait: MyField) -> __qualname__:
        assert not self.is_frozen()
        self.fields.pop(trait)
        return self

    def get_field(self, field: MyField) -> Any:
        return self.fields.get(field.key)

    def get_field_by_name(self, name: str) -> Any:
        return self.fields.get(name)

    def exist_field(self, field: MyField) -> bool:
        return field.key in self.fields

    def exist_field_by_name(self, name: str) -> bool:
        return name in self.fields

    def keys(self) -> List[str]:
        return list(self.fields.keys())

    def __iter__(self):
        return self.traits

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
