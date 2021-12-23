import copy
import logging

import networkx

from unidef.languages.common.ir_model import *
from unidef.languages.common.type_inference.blackboard import *
from unidef.languages.common.type_inference.inference_engine import *
from unidef.languages.common.type_inference.scope import *
from unidef.languages.common.type_model import *
from unidef.languages.common.type_model import infer_type_from_example
from unidef.languages.common.walk_nodes import walk_nodes
from unidef.utils.transformer import *
from unidef.utils.typing import *


class VariableDeclarationProcessor(NodeTypeProcessor):
    def accept(self, node: IrNode) -> bool:
        return node.get_field(Attributes.VariableDeclaration)

    def prepare_inference(
        self,
        node: IrNode,
        environment: Environment,
        scope: Scope,
        builder: TypeRelationBuilder,
    ) -> None:
        val = node.get_field(Attributes.DefaultValue)
        ns = environment.get_advanced_ir_node(node, scope)
        builder.add_post_inferred_type(ns.node_path, PostAssignTypeInferred(node))
        if val:
            builder.add_edge(ns, environment.get_advanced_ir_node(val, scope))


class AssignExpressionProcessor(NodeTypeProcessor):
    def accept(self, node: IrNode) -> bool:
        return node.get_field(Attributes.AssignExpression)

    def prepare_inference(
        self,
        node: IrNode,
        environment: Environment,
        scope: Scope,
        builder: TypeRelationBuilder,
    ) -> None:
        left = node.get_field(Attributes.AssignExpressionLeft)
        right = node.get_field(Attributes.AssignExpressionRight)
        decl = environment.find_declaration(left, scope)
        builder.add_edge(
            decl,
            environment.get_advanced_ir_node(right, scope),
        )


class ReturnProcessor(NodeTypeProcessor):
    def accept(self, node: IrNode) -> bool:
        return node.get_field(Attributes.Return)

    def prepare_inference(
        self,
        node: IrNode,
        environment: Environment,
        scope: Scope,
        builder: TypeRelationBuilder,
    ) -> None:
        ret = node.get_field(Attributes.Return)
        ns = environment.get_advanced_ir_node(node, scope)

        builder.add_edge(ns, environment.get_advanced_ir_node(ret, scope))


class MemberExpressionProcessor(NodeTypeProcessor):
    def accept(self, node: IrNode) -> bool:
        return node.get_field(Attributes.StaticMemberExpression)

    def prepare_inference(
        self,
        node: IrNode,
        environment: Environment,
        scope: Scope,
        builder: TypeRelationBuilder,
    ) -> None:
        ns = environment.get_advanced_ir_node(node, scope)
        obj = node.get_field(Attributes.MemberExpressionObject)
        prop: IrNode = node.get_field(Attributes.MemberExpressionProperty)
        if prop.get_field(Attributes.Identifier):
            obj_ns = environment.get_advanced_ir_node(obj, scope)
            if not obj_ns:
                logging.warning(f"Could not find declaration of {obj}")
                return
            prop_id = prop.get_field(Attributes.Identifier)
            if prop_id == "length":
                builder.add_known(obj_ns.node_path, VectorType(Types.AllValue))
            else:

                builder.add_group(
                    NodeGroup(
                        "member_expression",
                        {
                            "obj": obj_ns.node_path,
                            "prop": f"{scope}.{prop_id}",
                            "member_expr": ns,
                        },
                        self.infer_node,
                    )
                )
            # TODO builder.add_edge(ns, )
        elif prop.get_field(Attributes.Literal):
            obj_ns = environment.get_advanced_ir_node(obj, scope)
            prop_ns = environment.get_advanced_ir_node(prop, scope)
            builder.add_group(
                NodeGroup(
                    "member_expression",
                    {
                        "obj": obj_ns.node_path,
                        "prop": prop_ns.node_path,
                        "member_expr": ns,
                        "prop_literal": prop.get_field(Attributes.RawValue),
                    },
                    self.infer_node,
                )
            )

        else:
            raise Exception(f"Could not process prop: {prop}")

    def infer_node(
        self,
        obj: str,
        prop: str,
        member_expr: AdvancedIrNode,
        blackboard: Blackboard,
        prop_literal: str = "",
    ) -> bool:
        obj_in = obj in blackboard.inferred_cache
        prop_in = prop in blackboard.inferred_cache
        if obj_in and prop_in:
            pass
        elif not obj_in and prop_in:
            prop_ty = blackboard.inferred_cache[prop]
            if prop_ty.get_field(Traits.Integer):
                blackboard.inferred_cache[obj] = VectorType(Types.AllValue)
                return True
            elif prop_ty.get_field(Traits.String):
                blackboard.inferred_cache[obj] = Types.Object.copy()
                return True
            else:
                raise Exception(f"Could nor process prop: {prop} {prop_ty}")
        elif obj_in and not prop_in:
            obj_ty = blackboard.inferred_cache[obj]
            if obj_ty.get_field(Traits.Vector):
                blackboard.inferred_cache[prop] = Types.I64.copy()
                return True
            elif obj_ty.get_field(Traits.Object):
                blackboard.inferred_cache[prop] = Types.String.copy()
                return True
            else:
                raise Exception(f"Could nor process obj: {obj} {obj_ty}")
        return False


class OperatorProcessor(NodeTypeProcessor):
    def accept(self, node: IrNode) -> bool:
        return node.get_field(Attributes.Operator)

    def prepare_inference(
        self,
        node: IrNode,
        environment: Environment,
        scope: Scope,
        builder: TypeRelationBuilder,
    ) -> None:
        ns = environment.get_advanced_ir_node(node, scope)
        left = node.get_field(Attributes.OperatorLeft)
        right = node.get_field(Attributes.OperatorRight)
        if left and right:
            left_ns = environment.get_advanced_ir_node(left, scope)
            right_ns = environment.get_advanced_ir_node(right, scope)
            op = node.get_field(Attributes.Operator)
            comp = ["===", "==", ">=", "<=", "!=", "!==", ">", "<"]
            operations = ["+", "-", "*", "/", "%"]
            if op in comp:
                builder.add_known(ns.node_path, Types.Bool)
                builder.add_edge(left_ns, right_ns)
            elif op in operations:
                builder.add_edge(ns, left_ns)
                builder.add_edge(ns, right_ns)


class FunctionDeclProcessor(NodeTypeProcessor):
    def accept(self, node: IrNode) -> bool:
        return node.get_field(Attributes.FunctionDecl)

    def prepare_inference(
        self,
        node: IrNode,
        environment: Environment,
        scope: Scope,
        builder: TypeRelationBuilder,
    ) -> None:
        ns = environment.get_advanced_ir_node(node, scope)
        body = environment.get_advanced_ir_node(
            node.get_field(Attributes.FunctionBody), scope
        )

        builder.add_edge(ns, body)
        builder.add_post_inferred_type(
            ns.node_path,
            PostAssignTypeFunc(
                lambda ty: node.replace_field(Attributes.FunctionReturn(ty))
            ),
        )
        args = node.get_field(Attributes.Arguments)
        for arg in args:
            arg_path = str(environment.find_global_path(arg, scope))
            builder.add_node(arg_path, arg)
            builder.add_post_inferred_type(
                arg_path,
                PostAssignTypeFunc(
                    lambda ty: arg.replace_field(Attributes.ArgumentType(ty))
                ),
            )

        def collect_return(node1: IrNode) -> bool:
            if node1.get_field(Attributes.Return):
                builder.add_edge(
                    body,
                    environment.get_advanced_ir_node(node1, scope),
                )
            return False

        walk_nodes(body.node, collect_return)


class LiteralProcessor(NodeTypeProcessor):
    def accept(self, node: IrNode) -> bool:
        return node.get_field(Attributes.Literal)

    def prepare_inference(
        self,
        node: IrNode,
        environment: Environment,
        scope: Scope,
        builder: TypeRelationBuilder,
    ) -> None:
        if node.get_field(Attributes.InferredType):
            ns = environment.get_advanced_ir_node(node, scope)
            builder.add_known(ns.node_path, node.get_field(Attributes.InferredType))


class TypeInference(NodeTransformer[IrNode, IrNode]):
    environment: Environment = Environment()
    node_processors: List[NodeTypeProcessor] = [
        VariableDeclarationProcessor(),
        AssignExpressionProcessor(),
        ReturnProcessor(),
        MemberExpressionProcessor(),
        MemberExpressionProcessor(),
        OperatorProcessor(),
        FunctionDeclProcessor(),
        LiteralProcessor(),
    ]

    class Config:
        arbitrary_types_allowed = True

    def accept(self, node: IrNode) -> bool:
        return node.get_field(Attributes.Program)

    def build_graph(self, node: IrNode, *args, **kwargs) -> bool:
        scope_preparer: ScopePreparer = kwargs["scope"]
        blackboard: Blackboard = kwargs["blackboard"]
        scope_preparer.with_scope(node)
        for np in self.node_processors:
            if np.accept(node):
                scope = scope_preparer.scope.copy()
                builder = TypeRelationBuilder()
                np.prepare_inference(node, self.environment, scope, builder)
                blackboard.merge_builder(builder)
        return False

    def transform(self, root: IrNode) -> IrNode:
        scope = ScopePreparer()
        walk_nodes(root, scope.build_scope)
        self.environment = scope.environment

        scope = ScopePreparer(environment=self.environment)
        blackboard = Blackboard()

        walk_nodes(root, self.build_graph, scope=scope, blackboard=blackboard)
        InferenceEngine(blackboard).inference()
        return root
