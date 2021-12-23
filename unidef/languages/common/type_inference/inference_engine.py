from unidef.languages.common.type_inference.blackboard import (Blackboard,
                                                               NodeGroup)
from unidef.languages.common.type_model import DyType, Traits
from unidef.utils.typing import *


class InferenceEngine:
    def __init__(self, blackboard: Blackboard):
        self.blackboard = blackboard

    def unify_equals_to(self, n1: str, n2: str, blackboard: Blackboard):
        n1_in = n1 in blackboard.inferred_cache
        n2_in = n2 in blackboard.inferred_cache
        if n1_in and not n2_in:
            blackboard.inferred_cache[n2] = blackboard.inferred_cache[n1]
            return True
        elif not n1_in and n2_in:
            blackboard.inferred_cache[n1] = blackboard.inferred_cache[n2]
            return True
        elif n1_in and n2_in:
            if blackboard.inferred_cache[n1] != blackboard.inferred_cache[n2]:
                raise Exception(f"Type conflicting: {n1} {n2}")
            else:
                return True
        else:
            return False

    def unify(self, g: NodeGroup) -> bool:
        kwargs = dict((k, v) for k, v in g.members.items())
        kwargs["blackboard"] = self.blackboard
        if g.name == "equals_to":
            return self.unify_equals_to(**kwargs)
        elif g.callback is not None:
            return g.callback(**kwargs)
        raise Exception(f"Could not unify: {n1} {n2}")

    def inference(self):
        progress = True
        while progress:
            progress = False
            for g in self.blackboard.groups:
                if g.unified:
                    continue
                result = self.unify(g)
                progress |= result
                if result:
                    g.unified = result

        for k, v in self.blackboard.inferred_cache.items():
            for ass in self.blackboard.post_assign_type.get(k) or []:
                ass.post_assign_type(v)
