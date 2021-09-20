from unidef.languages.common.type_inference.blackboard import Blackboard, EdgeType
from unidef.models.type_model import DyType, Traits


class InferenceEngine:
    def __init__(self, blackboard: Blackboard):
        self.blackboard = blackboard

    def unify(self, n1: str, n2: str, edge_type: EdgeType) -> bool:
        if edge_type == EdgeType.EQUAL_TO:
            n1_in = n1 in self.blackboard.inferred_cache
            n2_in = n2 in self.blackboard.inferred_cache
            if n1_in and not n2_in:
                self.blackboard.inferred_cache[n2] = self.blackboard.inferred_cache[n1]
                return True
            elif not n1_in and n2_in:
                self.blackboard.inferred_cache[n1] = self.blackboard.inferred_cache[n2]
                return True
            elif n1_in and n2_in:
                if (
                    self.blackboard.inferred_cache[n1]
                    != self.blackboard.inferred_cache[n2]
                ):
                    raise Exception(f"Type conflicting: {n1} {n2}")
                else:
                    return True
            else:
                return False
        raise Exception(f"Could not unify: {n1} {n2}")

    def inference(self):
        progress = True
        while progress:
            progress = False
            nodes = []
            for n in self.blackboard.graph.nodes:
                if n not in self.blackboard.inferred_cache:
                    nodes.append(n)
            for n in nodes:
                for n2 in self.blackboard.graph.adj[n]:
                    progress |= self.unify(
                        n, n2, self.blackboard.graph[n][n2]["edge_type"]
                    )
        print(self.blackboard.inferred_cache)
        for k, v in self.blackboard.inferred_cache.items():
            for ass in self.blackboard.post_assign_type.get(k) or []:
                ass.post_assign_type(v)
