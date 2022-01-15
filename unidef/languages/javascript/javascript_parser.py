import traceback

import esprima
from esprima.nodes import *
from esprima.nodes import Node as EsprimaNode

from unidef.languages.common.ir_model import *
from unidef.languages.common.ir_model import ClassDeclNode
from unidef.languages.common.type_model import (Traits, infer_type_from_example)
from unidef.models.input_model import SourceInput
from unidef.parsers import InputDefinition, Parser
from unidef.utils.loader import load_module
from unidef.utils.vtable import VTable
from .jsonify import *


class JavasciprtVisitorBase(VTable):

    def get_recursive(self, obj: Dict, path: str) -> Any:
        for to_visit in path.split("."):
            obj = obj.get(to_visit)
            if obj is None:
                return None
        return obj

    def get_name(self, node: Dict[str, Any], member_expression=False, warn=True) -> Any:
        if node is None:
            return
        ty = node.get("type")
        if member_expression and ty == "MemberExpression":
            first = self.get_name(
                node.get("object"), warn=warn, member_expression=member_expression
            )
            property = node.get('property')
            second = property.get('value') or self.get_name(
                property, warn=warn, member_expression=member_expression
            )
            return ".".join([x for x in [first, second] if x])
        elif ty == "ThisExpression":
            return "this"
        elif ty == "Super":
            return "super"
        elif ty == "Identifier":
            return node.get("name")
        elif ty == "ObjectPattern":
            properties = node["properties"]
            names = []
            for prop in properties:
                key = prop["key"]
                value = prop["value"]
                names.append(
                    (
                        self.get_name(
                            key, warn=warn, member_expression=member_expression
                        ),
                        self.get_name(
                            value, warn=warn, member_expression=member_expression
                        ),
                    )
                )
            return names
        elif node.get("name"):
            return node.get("name")
        elif node.get("elements"):
            return DecomposePatternNode(names=[x['name'] for x in node.get("elements")])
        else:
            if warn:
                logging.warning("could not get name from %s", node)
            return

    def match_func_call(self, node: CallExpression, name: str) -> bool:
        spt = tuple(name.split("."))
        try:
            if len(spt) == 2:
                obj, method = spt
                callee = node.callee
                obj0 = self.get_name(callee.toDict(), member_expression=True)
                if (
                        obj0 == obj
                        and self.get_name(callee.property, member_expression=True) == method
                ):
                    return True
            elif len(spt) == 1:
                (obj,) = spt
                callee = node.callee
                obj0 = self.get_name(callee.toDict(), member_expression=True)
                return obj0 == obj
        except KeyError as e:
            traceback.print_exc()

        return False

    def transform_script(self, node: Script) -> ProgramNode:
        body = [self.transform(n) for n in node.body]
        return ProgramNode(body=Children(body))

    def transform_static_member_expression(
            self, node: StaticMemberExpression
    ) -> MemberExpressionNode:
        return MemberExpressionNode(obj=self.transform(node.object), property=self.transform(node.property),
                                    static=True)

    def transform_directive(self, node: Directive) -> DirectiveNode:
        return DirectiveNode(node.directive)

    def transform_literal(self, node: Literal) -> LiteralNode:
        return LiteralNode(raw_value=node.value, raw_code=node.raw)

    def transform_identifier(self, node: Identifier) -> IdentifierNode:
        return IdentifierNode(identifier=node.name)


class JavascriptVisitor(JavasciprtVisitorBase):
    def transform(self, node):
        return self(node)

    def transform_variable_declaration(self, node: VariableDeclaration) -> VariableDeclarationsNode:
        if len(node.declarations) == 1:
            decl: VariableDeclarator = node.declarations[0]
            if isinstance(decl.init, CallExpression) and self.match_func_call(
                    decl.init, "require"
            ):
                names = self.get_name(decl.id.toDict())
                paths = [arg.value for arg in decl.init.arguments]
                assert len(paths) == 1
                paths = paths[0]

                req = Nodes.requires(paths, names)
                return req
        decls = []
        for decl in node.declarations:
            decl: VariableDeclarator = decl
            id = self.get_name(decl.id.toDict())
            nd = VariableDeclarationNode(id=id, init=decl.init and self.transform(decl.init), ty=None)

            decls.append(nd)
        return VariableDeclarationsNode(decls=decls)

    def transform_assignment_expression(self, node: AssignmentExpression) -> StatementNode:
        name = self.get_name(node.left.toDict(), member_expression=True, warn=False)
        if name == "module.exports":
            return self.transform(node.right)
        value = AssignmentExpressionNode(assignee=self.transform(node.left), value=self.transform(node.right))

        return StatementNode(value=value)

    def transform_class_expression(self, node: ClassExpression) -> ClassDeclNode:
        class_name = self.get_name(node.id.toDict())
        super_class = self.get_name(node.superClass.toDict())

        body = [self.transform(n) for n in node.body.body]

        return ClassDeclNode(
            name=class_name,
            super_class=[super_class],
            functions=body
        )

    def transform_this_expression(self, node: ThisExpression) -> ThisExpressionNode:
        return ThisExpressionNode(this='this')

    def transform_super(self, node: Super) -> SuperExpressionNode:
        return SuperExpressionNode(super='super')

    def transform_method_definition(self, node: MethodDefinition) -> FunctionDecl:
        name = self.get_name(node.key.toDict())
        is_async = node.value.toDict()["async"]
        params = [self.transform_assignment_pattern_or_value(a) for a in node.value.params]
        children = [self.transform(n) for n in node.value.body.body]
        n = FunctionDecl(name=name,
                         arguments=params,
                         is_async=is_async,
                         function_body=Children(children=children)
                         )

        return n

    def transform_assignment_pattern_or_value(
            self, node: Union[AssignmentPattern, Any]
    ) -> ParameterNode:
        if isinstance(node, AssignmentPattern):
            name = self.get_name(node.left.toDict())
            default = self.transform(node.right)
        else:
            name = self.get_name(node.toDict())
            default = None
        n = ParameterNode(argument_name=name, argument_type=None, default=default)

        return n

    def transform_call_expression(self, node: CallExpression) -> FunctionCallNode:
        if self.match_func_call(node, "console.log"):
            return Nodes.print(self.transform(node["arguments"]))
        arguments = []
        for arg in node.arguments:
            arguments.append(self.transform(arg))
        return FunctionCallNode(callee=self.transform(node.callee), arguments=arguments)

    def transform_expression_statement(self, node: ExpressionStatement) -> Union[ClassDeclNode, StatementNode]:
        t = self.transform(node.expression)
        if isinstance(t, ClassDeclNode):
            return t
        return StatementNode(value=t)

    def transform_return_statement(self, node: ReturnStatement) -> ReturnNode:
        return ReturnNode(returnee=node.argument and self.transform(node.argument))

    def transform_property(self, node: Property) -> JsonProperty:

        return JsonProperty(
            self.transform(node.key),
            self.transform(node.value)
            # .append_field(Attributes.InferredType(Types.Object))
        )

    def transform_object_expression(self, node: ObjectExpression) -> JsonObject:
        properties = [self.transform_property(p) for p in node.properties]
        return JsonObject(properties=properties)

    def transform_array_expression(self, node: ArrayExpression) -> ArrayExpressionNode:
        return ArrayExpressionNode(
            elements=[self.transform(n) for n in node.elements]
        )

    def transform_binary_expression(self, node: BinaryExpression) -> OperatorNode:
        return OperatorNode(operator=node.operator, kind='middle',
                            left=self.transform(node.left),
                            right=self.transform(node.right),
                            )

    def transform_unary_expression(self, node: UnaryExpression) -> OperatorNode:
        kind = 'postfix'
        if node.prefix:
            kind = 'prefix'
        return OperatorNode(operator=node.operator, kind=kind, value=self.transform(node.argument))

    def transform_update_expression(self, node: UpdateExpression) -> IrNode:
        kind = 'postfix'
        if node.prefix:
            kind = 'prefix'
        return OperatorNode(operator=node.operator, kind=kind, value=self.transform(node.argument))

    def transform_if_statement(self, node: IfStatement) -> IfExpressionNode:
        return IfExpressionNode(test=self.transform(node.test),
                                consequent=node.consequent and self.transform(node.consequent),
                                alternative=node.alternate and self.transform(node.alternate))

    def transform_block_statement(self, node: BlockStatement) -> BlockStatementNode:
        return BlockStatementNode(children=[self.transform(n) for n in node.body])

    def transform_for_statement(self, node: ForStatement) -> ClassicalLoopNode:
        body = Children(children=[self.transform(n) for n in node.body.body])
        n.append_field(Attributes.Children(body))
        return ClassicalLoopNode(
            init=self.transform(node.init),
            test=self.transform(node.test),
            update=self.transform(node.update),
            body=body
        )

    def transform_computed_member_expression(
            self, node: ComputedMemberExpression
    ) -> MemberExpressionNode:
        return MemberExpressionNode(static=False, obj=self.transform(node.object),
                                    property=self.transform(node.property))

    def transform_await_expression(self, node: AwaitExpression) -> AwaitExpressionNode:
        return AwaitExpressionNode(value=self.transform(node.argument))

    def transform_throw_statement(self, node: ThrowStatement) -> IrNode:
        return ThrowStatementNode(content=self.transform(node.argument))

    def transform_new_expression(self, node: NewExpression) -> IrNode:
        return NewExpressionNode(
            type=self.transform(node.callee),
            arguments=[self.transform(a) for a in node.arguments]

        )

    def transform_conditional_expression(self, node: ConditionalExpression) -> IfExpressionNode:
        return IfExpressionNode(
            test=self.transform(node.test),
            consequent=self.transform(node.consequent),
            alternative=self.transform(node.alternate),
            conditional_op=True
        )

    def transform_break_statement(self, node: BreakStatement) -> BreakStatementNode:
        return BreakStatementNode()

    def transform_continue_statement(self, node: ContinueStatement) -> ContinueStatementNode:
        return ContinueStatementNode()

    def transform_try_statement(self, node: TryStatement) -> TryStatementNode:
        return TryStatementNode(
            try_body=[self.transform(n) for n in node.block.body],
            catch_clauses=node.handler and [self.transform_catch_clause(node.handler)],
            finally_clause=node.finalizer and self.transform(node.finalizer.body.body)
        )

    def transform_catch_clause(self, node: CatchClause) -> CatchClauseNode:
        return CatchClauseNode(
            argument_name=self.transform(node.param),
            body=Children([self.transform(n) for n in node.body.body])
        )


class JavascriptParserImpl(Parser):
    def accept(self, fmt: InputDefinition) -> bool:
        return (
                isinstance(fmt, SourceInput)
                and fmt.lang == "javascript"
                and load_module("esprima")
        )

    def parse(self, name: str, fmt: InputDefinition) -> IrNode:
        assert isinstance(fmt, SourceInput)
        parsed = esprima.parseScript(fmt.code, {"comment": True})
        node = JavascriptVisitor().transform(parsed)
        return node
