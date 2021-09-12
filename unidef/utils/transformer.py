from unidef.utils.typing import *
from unidef.utils.name_convert import *
from pydantic import BaseModel

Input = TypeVar("Input")
Output = TypeVar("Output")


@abstract
class NodeTransformer(BaseModel, Generic[Input, Output]):
    @beartype
    def accept(self, node: Input) -> bool:
        return NotImplemented

    @beartype
    def transform(self, node: Input) -> Output:
        return NotImplemented


class FuncNodeTransformer(NodeTransformer[Input, Output]):
    target_name: str
    func: Callable
    acceptor: Callable

    @beartype
    def accept(self, node: Input) -> bool:
        return self.acceptor(self, node)

    @beartype
    def transform(self, node: Input) -> Output:
        return self.func(node)


@abstract
class NodeTransformable(BaseModel, Generic[Output]):
    def transform(self) -> Output:
        return NotImplemented
