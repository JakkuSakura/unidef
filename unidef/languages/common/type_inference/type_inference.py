import copy
import logging
import networkx

from unidef.models.ir_model import *
from unidef.utils.transformer import *
from unidef.languages.common.walk_nodes import walk_nodes
from unidef.models.type_model import infer_type_from_example
from unidef.utils.typing import *
from unidef.models.type_model import *

from unidef.languages.common.type_inference.scope import *
from unidef.languages.common.type_inference.blackboard import *


class TypeInference(NodeTransformer[IrNode, IrNode]):
    environment: Environment = Environment()

    class Config:
        arbitrary_types_allowed = True

    def accept(self, node: IrNode) -> bool:
        return node.get_field(Attributes.Program)

    def build_graph(
            self, node: IrNode, scope_preparer: ScopePreparer, blackboard: Blackboard
    ) -> bool:
        scope_preparer.with_scope(node)
        scope = scope_preparer.scope.copy()
        ns = self.environment.get_advanced_ir_node(node, scope)

        if node.get_field(Attributes.VariableDeclaration):
            val = node.get_field(Attributes.DefaultValue)
            if val:
                blackboard.add_edge(
                    node=self.environment.get_advanced_ir_node(node, scope),
                    rely_on=self.environment.get_advanced_ir_node(val, scope),
                )
        elif node.get_field(Attributes.AssignExpression):
            left = node.get_field(Attributes.AssignExpressionLeft)
            right = node.get_field(Attributes.AssignExpressionRight)
            decl = self.environment.find_declaration(scope_preparer.scope, left)
            blackboard.add_edge(
                node=decl, rely_on=self.environment.get_advanced_ir_node(right, scope)
            )
        elif node.get_field(Attributes.Return):
            ret = node.get_field(Attributes.Return)
            blackboard.add_edge(
                node=ns,
                rely_on=self.environment.get_advanced_ir_node(ret, scope),
            )
        elif node.get_field(Attributes.MemberExpression):
            obj = node.get_field(Attributes.MemberExpressionObject)
            prop = node.get_field(Attributes.MemberExpressionProperty)
            blackboard.add_edge(
                node=ns,
                rely_on=self.environment.get_advanced_ir_node(obj, scope),
            )
            blackboard.add_edge(
                node=ns,
                rely_on=self.environment.get_advanced_ir_node(prop, scope),
            )
        elif node.get_field(Attributes.Operator):
            op1 = node.get_field(Attributes.OperatorLeft)
            op2 = node.get_field(Attributes.OperatorRight)
            op3 = node.get_field(Attributes.OperatorMiddle)
            for n in [op1, op2, op3]:
                if n:
                    blackboard.add_edge(
                        node=ns,
                        rely_on=self.environment.get_advanced_ir_node(n, scope),
                    )
        elif node.get_field(Attributes.Identifier):
            decl = self.environment.find_declaration(scope, node)
            if not decl:
                logging.warning("Could not get declaration of %s", str(node))
        elif node.get_field(Attributes.FunctionDecl):
            body = self.environment.get_advanced_ir_node(node.get_field(Attributes.FunctionBody), scope)

            blackboard.add_node(ns.node_path, node)
            blackboard.add_edge(ns, body)
            blackboard.add_assign_type(
                ns.node_path,
                AssignTypeFunc(
                    lambda ty: node.replace_field(Attributes.FunctionReturn(ty))
                ),
            )
            args = node.get_field(Attributes.Arguments)
            for arg in args:
                arg_path = str(self.environment.find_global_path(scope, arg))
                blackboard.add_node(arg_path, arg)
                blackboard.add_assign_type(
                    arg_path,
                    AssignTypeFunc(
                        lambda ty: arg.append_field(Attributes.ArgumentType(ty))
                    ),
                )
            body = self.environment.get_advanced_ir_node(node.get_field(Attributes.FunctionBody), scope)

            def collect_return(node1: IrNode) -> bool:
                if node1.get_field(Attributes.Return):
                    blackboard.add_edge(
                        node=body,
                        rely_on=self.environment.get_advanced_ir_node(node1, scope),
                    )
                return False

            walk_nodes(body.node, collect_return)
        return False
        # else:
        #     print("did not process", node.get_field(Attributes.Kind), id(node))

    def infer_type(self, ns: AdvancedIrNode, blackboard: Blackboard):
        node = ns.node

        if node.get_field(Attributes.AssignExpression):
            assignee = node.get_field(Attributes.AssignExpressionLeft)
            expr = node.get_field(Attributes.AssignExpressionRight)
            assignee_path = assignee.get_field(Attributes.GlobalPath)
            expr_path = expr.get_field(Attributes.GlobalPath)
            blackboard.add_inferred(assignee_path, blackboard.get_inferred(expr_path))

        elif node.get_field(Attributes.Operator):
            left = node.get_field(Attributes.OperatorLeft)
            right = node.get_field(Attributes.OperatorRight)
            op = node.get_field(Attributes.Operator)
            comp = ["===", "==", ">=", "<=", "!=", "!==", ">", "<"]
            operations = ["+", "-", "*", "/", "%"]
            if op in comp:
                node.append_field(Attributes.InferredType(Types.Bool))
                blackboard.add_inferred(ns.node_path, Types.Bool)
                return
            if op in operations:
                left_path = left.get_field(Attributes.GlobalPath)
                right_path = right.get_field(Attributes.GlobalPath)
                left_ty = blackboard.get_inferred(left_path)
                right_ty = blackboard.get_inferred(right_path)
                assert left_ty is not None and left_ty == right_ty
                node.append_field(Attributes.InferredType(left_ty))
                blackboard.add_inferred(ns.node_path, left_ty)
                return
        elif node.get_field(Attributes.Literal):
            ty = node.get_field(Attributes.InferredType)
            blackboard.add_inferred(ns.node_path, ty)
            return
        elif len(blackboard.graph.out_edges([ns.node_path])) >= 1:
            rely_on = list(blackboard.graph.out_edges([ns.node_path]))[0][1]
            inferred = blackboard.get_inferred(rely_on)
            node.append_field(Attributes.InferredType(inferred))
            blackboard.add_inferred(ns.node_path, inferred)
            return
        raise Exception("Could not infer properly: " + str(node))

    def drive_infer_types(self, root: IrNode, blackboard: Blackboard):
        sorted_list = list(
            networkx.topological_sort(blackboard.graph.reverse(copy=True))
        )


        for n in sorted_list:
            n = blackboard.get_node(n)
            # print("Inferring", n.node_path, n.node)
            self.infer_type(n, blackboard)

    def transform(self, root: IrNode) -> IrNode:
        scope = ScopePreparer()
        walk_nodes(root, scope.build_scope)
        self.environment = scope.environment

        scope = ScopePreparer(environment=self.environment)
        blackboard = Blackboard()

        # noinspection PyTypeChecker
        walk_nodes(root, self.build_graph, scope, blackboard)

        self.drive_infer_types(root, blackboard)
        return root
