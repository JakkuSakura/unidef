from pydantic import BaseModel

from unidef.utils.name_convert import *
from unidef.utils.typing import *

Input = TypeVar("Input")
Output = TypeVar("Output")


@abstract
class NodeTransformer(BaseModel, Generic[Input, Output]):
    @beartype
    def accept(self, node: Input) -> bool:
        raise NotImplementedError()

    @beartype
    def transform(self, node: Input) -> Output:
        raise NotImplementedError()


class FuncNodeTransformer(NodeTransformer[Input, Output]):
    target_name: str
    func: Callable
    acceptor: Callable

    @beartype
    def accept(self, node: Input, **kwargs) -> bool:
        return self.acceptor(self, node)

    @beartype
    def transform(self, node: Input, **kwargs) -> Output:
        return self.func(node, **kwargs)


@abstract
class NodeTransformable(BaseModel):
    pass
