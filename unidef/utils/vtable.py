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

    def __call__(self, value, *args, **kwargs):
        func = self.__mapping__.get(type(value))
        if func:
            return func(self, value, *args, **kwargs)
        else:
            return self.default(value, *args, **kwargs)

    def default(self, *args, **kwargs):
        raise NotImplementedError()


def test_vtable():
    class Foo(VTable):
        def foo(self, node: int):
            return 'node is int'

        def bar(self, node: str):
            return 'node is str'

        def default(self, node):
            return 'why is node default'

    foo = Foo()
    assert foo(1) == 'node is int'
