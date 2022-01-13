from typedmodel.compat import *
from beartype import beartype


def abstract(cls):
    old_init = cls.__init__

    def new_init(self, *args, **kwargs):
        if type(self) == cls:
            raise TypeError(f"{cls} is abstract and cannot be initialized here")
        old_init(self, *args, **kwargs)

    cls.__init__ = new_init
    return cls
