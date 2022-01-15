from typedmodel.compat import *
from typedmodel.utils import my_beartype, reannotate, abstract


class VTableMeta(type):

    def __new__(mcs, name, bases, attr):
        mapping = {}
        for key, value in attr.items():
            if not key.startswith("_"):
                if type(value) is FunctionType:
                    args = value.__code__.co_varnames
                    if len(args) >= 2:
                        ty = value.__annotations__.get(args[1])
                        if ty is not None:
                            mapping[ty] = my_beartype(value)
                attr[key] = reannotate(value)

        attr['__mapping__'] = mapping
        return super(VTableMeta, mcs).__new__(mcs, name, bases, attr)


@abstract
class VTable(metaclass=VTableMeta):
    __mapping__: Dict[type, FunctionType]
    @classmethod
    def __get_func(cls, ty: type):
        func = cls.__mapping__.get(ty)
        if func:
            return func
        elif issubclass(cls.__base__, VTable):
            return cls.__base__.__get_func(ty)


    def __call__(self, value, *args, **kwargs):
        cls = type(self)
        func = cls.__get_func(type(value))
        if func:
            return func(self, value, *args, **kwargs)
        else:
            return self.default(value, *args, **kwargs)

    def default(self, value, *args, **kwargs):
        raise NotImplementedError("Not implemented for type {}".format(type(value)))


def test_vtable():
    class Foo(VTable):
        def default(self, value, *args, **kwargs):
            return 'why is node default'

        def foo(self, node: int):
            return 'node is int'

        def bar(self, node: str):
            return 'node is str'

    foo = Foo()
    assert foo(1) == 'node is int'


def test_vtable_inheritence():
    class Foo(VTable):
        def default(self, value, *args, **kwargs):
            return 'foo'

    class Bar(Foo):
        def default(self, value, *args, **kwargs):
            return 'bar'

    assert Foo()(1) == 'foo'
    assert Bar()(2) == 'bar'
