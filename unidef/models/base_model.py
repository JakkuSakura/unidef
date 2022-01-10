import copy

from unidef.utils.typing import *

from .typed_field import FieldValue, TypedField


class MixedModel(BaseModel):
    extended: Dict[str, Any] = {}
    frozen: bool = False

    def __init__(self, **kwargs):
        super().__init__(**kwargs)

    @beartype
    def append_field(self, field: FieldValue) -> __qualname__:
        assert not self.is_frozen()
        if hasattr(self, field.key):
            value = getattr(self, field.key)
        else:
            value = self.extended.get(field.key)
        if value is not None:
            value.extend(field.value)
        else:
            self.replace_field(field)

        return self

    @beartype
    def replace_field(self, field: FieldValue) -> __qualname__:
        assert not self.is_frozen()
        if hasattr(self, field.key):
            setattr(self, field.key, field.value)
        else:
            self.extended[field.key] = field.value
        return self

    @beartype
    def remove_field(self, field: TypedField) -> __qualname__:
        assert not self.is_frozen()
        if hasattr(self, field.key):
            raise Exception(
                "Could not remove required field {} in {}".format(field.key, type(self))
            )
        if field.key in self.extended:
            self.extended.pop(field.key)
        return self

    def _get_field_raw(self, key: str, default):
        if hasattr(self, key):
            return getattr(self, key)
        if hasattr(self, key + "_field"):
            return getattr(self, key + "_field")
        if key in self.extended:
            return self.extended.get(key)
        else:
            return default

    def get_field(self, field: TypedField) -> Any:
        return self._get_field_raw(field.key, field.default)

    def get_field_opt(self, field: TypedField) -> Optional[Any]:
        return self._get_field_raw(field.key, None)

    def exist_field(self, field: TypedField) -> bool:
        return field.key in self.keys()

    def keys(self) -> List[str]:
        keys = set(self._keys())
        keys.update(self.extended.keys())
        for x in ["extended", "frozen"]:
            keys.remove(x)
        return list(keys)

    def __iter__(self):
        collected = self.keys()
        for key in collected:
            yield key, self._get_field_raw(key, None)

    def is_frozen(self) -> bool:
        return self.frozen

    def freeze(self) -> __qualname__:
        self.frozen = True
        return self

    def unfreeze(self) -> __qualname__:
        self.frozen = False
        return self

    def copy(self, *args, **kwargs) -> __qualname__:
        this = copy.deepcopy(self)
        this.unfreeze()
        return this

    def __str__(self):
        return f"{type(self).__qualname__}{dict(list(self))}"

    def __repr__(self):
        return self.__str__()


def test_mixed_model():
    class Model(MixedModel):
        key1: int
        key2: int

    model = Model(key1=1, key2=2)
    assert set(model.keys()) == {"key1", "key2"}
    assert dict(list(model)) == {"key1": 1, "key2": 2}
