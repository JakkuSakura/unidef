from unidef.languages.common.ir_model import *
from unidef.languages.common.type_model import *


@abstract
class Constrain:
    pass


class FreeTypeVariable(Constrain):
    def __init__(self, name: str):
        self.name = name

    def __str__(self):
        return self.name


class ConcreteType(Constrain):
    def __init__(self, ty: DyType):
        self.ty = ty

    def __str__(self):
        return str(self.ty)


class IsObject(Constrain):
    pass


class IsVector(Constrain):
    pass


class Function:
    def __init__(self, name: str, args: List[Constrain], ret: Constrain):
        self.name = name
        self.args = args
        self.ret = ret

    def __str__(self):
        args = ", ".join([str(a) for a in self.args])
        return f"{self.name}({args}) -> {self.ret}"


class Functions:
    PLUS = Function(
        "plus", [FreeTypeVariable("a"), FreeTypeVariable("a")], FreeTypeVariable("a")
    )
    EQUAL = Function(
        "equals",
        [FreeTypeVariable("a"), FreeTypeVariable("a")],
        ConcreteType(Types.Bool),
    )


def test_print_functions():
    print()
    for func in Functions.__dict__.values():
        if isinstance(func, Function):
            print(func)
