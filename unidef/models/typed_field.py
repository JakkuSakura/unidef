import copy

from unidef.utils.typing import *


def check_pep_type(obj, annotation) -> bool:
    try:
        return check_raise_exception(obj, annotation)
    except:
        return False


def check_raise_exception(obj, annotation):
    @beartype
    def check(o) -> annotation:
        return o

    check(obj)
    return True


class FieldValue:
    def __init__(
            self,
            key,
            value,
            prototype=None,
    ):
        self.key = key
        self.value = value
        self.prototype = prototype

    def __str__(self):
        s = [type(self).__name__, " ", self.key]
        if self._value is not None:
            s.append(" ")
            s.append(self._value)
        return "".join(s)


class TypedField:
    @beartype
    def __init__(
            self,
            key: str,
            ty,
            default=None
    ):
        self.key = key
        self.ty = ty
        self.default = default

    def validate(self, value):
        if self.default is not None and value is None:
            return True
        return check_raise_exception(value, self.ty)

    def _get_value(self, value):
        if self.default is not None and value is None:
            return copy.deepcopy(self.default)
        return value

    def __call__(self, value: Any) -> FieldValue:
        self.validate(value)
        field = FieldValue(key=self.key, value=value, prototype=self)
        return field


def test_check_pep_type():
    assert check_pep_type(1, int)
    assert not check_pep_type(1, float)
    assert check_pep_type([], list)
    assert check_pep_type([1], List[int])


def test_check_field_definition():
    field = TypedField(key='test', ty=str)
    assert field.validate('hello')
    field = TypedField(key='test', ty=Optional[str])
    assert field.validate(None)
    field = TypedField(key='test', ty=str, default='def')
    assert field.validate(None)
    field = TypedField(key='test', ty=Optional[str], default='def')
    assert field.validate(None)
