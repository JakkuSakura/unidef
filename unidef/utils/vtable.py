from typedmodel.compat import *
from typedmodel.utils import my_beartype, reannotate, abstract, check_pep_type


class TypeAcceptor:
    def __init__(self, annotation):
        self.annotation = annotation

    def __call__(self, val):
        return check_pep_type(val, self.annotation)

    def __str__(self):
        return 'accept: ' + str(self.annotation)

    def __repr__(self):
        return self.__str__()

class VTableMeta(type):

    def __new__(mcs, name, bases, attr):
        mapping = {}
        additional = []
        for key, value in attr.items():
            if not key.startswith("_") and key != 'default':
                if type(value) is FunctionType:
                    args = value.__code__.co_varnames
                    if len(args) >= 2:
                        ty = value.__annotations__.get(args[1])
                        # For performance
                        if type(ty) == type:
                            mapping[ty] = my_beartype(value)
                        # Support sub classes & annotations
                        additional.append((TypeAcceptor(ty), my_beartype(value)))
                attr[key] = reannotate(value)

        attr['__mapping__'] = mapping
        attr['__additional__'] = additional
        return super(VTableMeta, mcs).__new__(mcs, name, bases, attr)


@abstract
class VTable(metaclass=VTableMeta):
    __mapping__: Dict[type, FunctionType]
    __additional__: List[Tuple[TypeAcceptor, FunctionType]]

    @classmethod
    def __get_func(cls, ty: type):
        func = cls.__mapping__.get(ty)
        if func:
            return func

        if issubclass(cls.__base__, VTable):
            return cls.__base__.__get_func(ty)

    @classmethod
    def __get_func_by_val(cls, val: Any):
        for (accept, func) in cls.__additional__:
            if accept(val):
                return func
        if issubclass(cls.__base__, VTable):
            return cls.__base__.__get_func_by_val(val)

    def __call__(self, value, *args, **kwargs):
        cls = type(self)
        func = cls.__get_func(type(value)) or cls.__get_func_by_val(value)
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

        def bar(self, node: List[str]):
            return 'list of str'

        def baz(self, node: List[int]):
            return 'list of int'

    foo = Foo()
    assert foo(1) == 'node is int'
    assert foo([123]) == 'list of int'
    assert foo(['123']) == 'list of str'


def test_vtable_inheritence():
    class Foo(VTable):
        def default(self, value, *args, **kwargs):
            return 'foo'

    class Bar(Foo):
        def default(self, value, *args, **kwargs):
            return 'bar'

    assert Foo()(1) == 'foo'
    assert Bar()(2) == 'bar'
