import json
import logging
import traceback

from beartype import beartype

from unidef.models.input_model import SourceInput
from unidef.models.ir_model import Attribute, Attributes, IrNode, Nodes
from unidef.models.type_model import Traits, DyType, Types
from unidef.parsers import InputDefinition, Parser
from unidef.utils.loader import load_module
from unidef.utils.name_convert import *
from unidef.utils.typing import *
from unidef.utils.visitor import VisitorPattern
from unidef.utils.transformer import NodeTransformer

import esprima
from esprima.nodes import *
from esprima.nodes import Node as EsprimaNode


class JavasciprtVisitorBase(NodeTransformer[Any, DyType], VisitorPattern):
    functions: Any = None

    def get_recursive(self, obj: Dict, path: str) -> Any:
        for to_visit in path.split("."):
            obj = obj.get(to_visit)
            if obj is None:
                return None
        return obj

    @beartype
    def get_name(self, node: Dict[str, Any], member_expression=False, warn=True) -> Any:
        if node is None:
            return
        ty = node.get("type")
        if member_expression and ty == "MemberExpression":
            first = self.get_name(
                node.get("object"), warn=warn, member_expression=member_expression
            )
            second = self.get_name(
                node.get("property"), warn=warn, member_expression=member_expression
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
        else:
            if warn:
                logging.warning("could not get name from %s", node)
            return

    @beartype
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

    @beartype
    def transform_script(self, node: Script) -> IrNode:
        program = IrNode.from_attribute(Attributes.Program)
        body = [self.transform(n) for n in node.body]
        program.append_field(Attributes.Children(body))
        return program

    @beartype
    def transform_static_member_expression(
        self, node: StaticMemberExpression
    ) -> IrNode:
        n = IrNode.from_attribute(Attributes.MemberExpression)
        n.append_field(
            Attributes.MemberExpressionObject(self.transform(node.object))
        )
        n.append_field(
            Attributes.MemberExpressionProperty(self.transform(node.property))
        )
        return n

    @beartype
    def transform_directive(
        self, node: Directive
    ) -> Union[IrNode, type(NotImplemented)]:
        return IrNode.from_attribute(Attributes.Directive(node.directive))

    @beartype
    def transform_literal(self, node: Literal) -> IrNode:
        return (
            IrNode.from_attribute(Attributes.Literal)
            .append_field(Attributes.RawCode(node.raw))
            .append_field(Attributes.RawValue(node.value))
        )

    @beartype
    def transform_identifier(self, node: Identifier) -> IrNode:
        return IrNode.from_attribute(Attributes.Identifier(node.name))

    @beartype
    def transform(self, node: EsprimaNode) -> Union[IrNode, List[IrNode]]:
        if self.functions is None:
            self.functions = self.get_functions("transform_")
        for func in self.functions:
            if func.accept(node):
                result = func.transform(node)
                if result is not NotImplemented:
                    break
        else:
            result = NotImplemented

        if result is NotImplemented:
            raise Exception(f"Still got NotImplemented for {type(node)}")
        else:
            comments = getattr(node, "comments")
            if result and comments:
                comments = []
                for comment in comments:
                    # TODO: check comment['type']
                    comments.append(comment.value)
                result.append_field(Traits.BeforeLineComment(comments))
            return result


class JavascriptVisitor(JavasciprtVisitorBase):
    @beartype
    def transform_variable_declaration(self, node: VariableDeclaration) -> IrNode:
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
            init = self.transform(decl.init)
            decls.append(
                IrNode.from_attribute(Attributes.VariableDeclaration)
                .append_field(Attributes.Id(id))
                .append_field(Attributes.Value(init))
            )
        return IrNode.from_attribute(Attributes.VariableDeclarations(decls))

    @beartype
    def transform_assignment_expression(self, node: AssignmentExpression) -> IrNode:
        name = self.get_name(node.left.toDict(), member_expression=True, warn=False)
        if name == "module.exports":
            return self.transform(node.right)
        assign = (
            IrNode.from_attribute(Attributes.AssignExpression)
            .append_field(
                Attributes.AssignExpressionLeft(self.transform(node.left))
            )
            .append_field(
                Attributes.AssignExpressionRight(self.transform(node.right))
            )
        )
        return IrNode.from_attribute(Attributes.Statement(value=assign))

    @beartype
    def transform_class_expression(self, node: ClassExpression) -> IrNode:
        class_name = self.get_name(node.id.toDict())
        super_class = self.get_name(node.superClass.toDict())

        body = [self.transform(n) for n in node.body.body]

        n = (
            IrNode.from_attribute(Attributes.ClassDeclaration)
            .append_field(Attributes.Name(class_name))
            .append_field(Attributes.SuperClasses([super_class]))
            .append_field(Attributes.Children(body))
        )

        return n

    @beartype
    def transform_this_expression(self, node: ThisExpression):
        return IrNode.from_attribute(Attributes.ThisExpression)

    @beartype
    def transform_super(self, node: Super):
        return IrNode.from_attribute(Attributes.SuperExpression)

    @beartype
    def transform_method_definition(self, node: MethodDefinition):
        name = self.get_name(node.key.toDict())
        is_async = node.value.toDict()["async"]
        params = [self.transform_assignment_pattern(a) for a in node.value.params]
        children = [self.transform(n) for n in node.value.body.body]

        n = IrNode.from_attribute(Attributes.FunctionDecl)
        n.append_field(Attributes.Name(name))
        n.append_field(Attributes.Async(is_async))
        n.append_field(Attributes.Children(children))
        n.append_field(Attributes.Arguments(params))

        return n

    @beartype
    def transform_assignment_pattern(
        self, node: Union[AssignmentPattern, Any]
    ) -> IrNode:
        if isinstance(node, AssignmentPattern):
            name = self.get_name(node.left.toDict())
            default = self.transform(node.right)
        else:
            name = self.get_name(node.toDict())
            default = None
        n = IrNode.from_attribute(Attributes.ArgumentName(name)).append_field(
            Attributes.ArgumentType(
                Types.AllValue.copy().append_field(Traits.NotInferredType).freeze()
            )
        )
        if default:
            n.append_field(Attributes.DefaultValue(default))
        return n

    @beartype
    def transform_call_expression(self, node: CallExpression) -> IrNode:
        if self.match_func_call(node, "console.log"):
            return Nodes.print(self.transform(node["arguments"]))
        n = IrNode.from_attribute(Attributes.FunctionCall)
        n.append_field(Attributes.Callee(self.transform(node.callee)))
        arguments = [self.transform(n) for n in node.arguments]
        n.append_field(Attributes.Arguments(arguments))
        return n

    @beartype
    def transform_expression_statement(self, node: ExpressionStatement) -> IrNode:
        t = self.transform(node.expression)
        if t.get_field(Attributes.ClassDeclaration):
            return t
        return IrNode.from_attribute(Attributes.Statement(t))

    @beartype
    def transform_return_statement(self, node: ReturnStatement) -> IrNode:
        arg = node.argument
        if arg:
            returnee = self.transform(arg)
        else:
            returnee = None
        return IrNode.from_attribute(Attributes.Return(returnee))

    @beartype
    def transform_property(self, node: Property) -> IrNode:
        return (
            IrNode.from_attribute(Attributes.ObjectProperty)
            .append_field(Attributes.KeyName(self.transform(node.key)))
            .append_field(Attributes.Value(self.transform(node.value)))
        )

    @beartype
    def transform_object_expression(self, node: ObjectExpression) -> IrNode:
        properties = [self.transform(p) for p in node.properties]
        return IrNode.from_attribute(Attributes.ObjectProperties(properties))

    @beartype
    def transform_array_expression(self, node: ArrayExpression) -> IrNode:
        return IrNode.from_attribute(
            Attributes.ArrayElements([self.transform(n) for n in node.elements])
        )

    def transform_logical_expression(self, node) -> IrNode:
        return self.transform_operator(node)

    def transform_binary_expression(self, node) -> IrNode:
        return self.transform_operator(node)

    def transform_unary_expression(self, node) -> IrNode:
        return self.transform_operator(node)

    def transform_update_expression(self, node) -> IrNode:
        return IrNode.from_attribute(
            Attributes.Statement(self.transform_operator(node))
        )

    def transform_operator(self, node) -> IrNode:

        operator = IrNode.from_attribute(Attributes.Operator(node.operator))
        positions = [
            ("left", "left", Attributes.OperatorLeft),
            ("right", "right", Attributes.OperatorRight),
            ("prefix", "argument", Attributes.OperatorSinglePrefix),
            ("postfix", "argument", Attributes.OperatorSinglePostfix),
        ]
        for key, value_key, attr in positions:
            if getattr(node, key):
                operator.append_field(
                    attr(self.transform(getattr(node, value_key)))
                )
        if getattr(node, "prefix") is False:
            operator.append_field(
                Attributes.OperatorSinglePostfix(self.transform(node.argument))
            )
        return operator

    def transform_if_statement(self, node) -> IrNode:
        if_clauses = []
        while node:
            if isinstance(node, BlockStatement):
                attr = Attributes.ElseClause
                test = None
                body = self.transform(node)
            elif isinstance(node, IfStatement):
                if if_clauses:
                    attr = Attributes.ElseIfClause
                else:
                    attr = Attributes.IfClause
                body = self.transform(node.consequent)
                test = self.transform(node.test)
            else:
                raise Exception("Could not process in if statement" + str(node))

            n = IrNode.from_attribute(attr).append_field(Attributes.Consequence(body))
            if test is not None:
                n.append_field(Attributes.TestExpression(test))

            if_clauses.append(n)
            node = getattr(node, "alternate")

        return IrNode.from_attribute(Attributes.IfClauses).append_field(
            Attributes.Children(if_clauses)
        )

    @beartype
    def transform_block_statement(self, node: BlockStatement) -> IrNode:
        return IrNode.from_attribute(Attributes.BlockStatement).append_field(
            Attributes.Children([self.transform(n) for n in node.body])
        )

    @beartype
    def transform_for_statement(self, node: ForStatement) -> IrNode:
        n = IrNode.from_attribute(Attributes.CForLoop)
        init = self.transform(node.init)
        n.append_field(Attributes.CForLoopInit(init))
        test = self.transform(node.test)
        n.append_field(Attributes.CForLoopTest(test))
        update = self.transform(node.update)
        n.append_field(Attributes.CForLoopUpdate(update))
        body = [self.transform(n) for n in node.body.body]
        n.append_field(Attributes.Children(body))
        return n

    @beartype
    def transform_computed_member_expression(
        self, node: ComputedMemberExpression
    ) -> IrNode:
        n = IrNode.from_attribute(Attributes.MemberExpression)
        n.append_field(
            Attributes.MemberExpressionObject(self.transform(node.object))
        )
        n.append_field(
            Attributes.MemberExpressionProperty(self.transform(node.property))
        )
        return n

    @beartype
    def transform_await_expression(self, node: AwaitExpression) -> IrNode:
        return IrNode.from_attribute(
            Attributes.AwaitExpression(self.transform(node.argument))
        )

    @beartype
    def transform_throw_statement(self, node: ThrowStatement) -> IrNode:
        return IrNode.from_attribute(
            Attributes.ThrowStatement(self.transform(node.argument))
        )

    @beartype
    def transform_new_expression(self, node: NewExpression) -> IrNode:
        return (
            IrNode.from_attribute(Attributes.NewExpression)
            .append_field(Attributes.Callee(self.transform(node.callee)))
            .append_field(
                Attributes.Arguments([self.transform(a) for a in node.arguments])
            )
        )

    @beartype
    def transform_conditional_expression(self, node: ConditionalExpression) -> IrNode:
        return (
            IrNode.from_attribute(Attributes.ConditionalExpression)
            .append_field(Attributes.TestExpression(self.transform(node.test)))
            .append_field(Attributes.Consequence(self.transform(node.consequent)))
            .append_field(Attributes.Alternative(self.transform(node.alternate)))
        )

    @beartype
    def transform_try_statement(self, node: TryStatement) -> IrNode:
        return IrNode.from_attribute(Attributes.TryStatement)

    @beartype
    def transform_break_statement(self, node: BreakStatement) -> IrNode:
        return IrNode.from_attribute(Attributes.BreakStatement)

    @beartype
    def transform_continue_statement(self, node: ContinueStatement) -> IrNode:
        return IrNode.from_attribute(Attributes.ContinueStatement)

    @beartype
    def transform_try_statement(self, node: TryStatement) -> IrNode:
        n = IrNode.from_attribute(
            Attributes.TryStatement([self.transform(n) for n in node.block.body])
        )
        if node.handler:
            n.append_field(Attributes.CatchClauses([self.transform(node.handler)]))
        if node.finalizer:
            n.append_field(
                Attributes.FinallyClause(
                    self.transform(n) for n in node.finalizer.body.body
                )
            )

        return n

    @beartype
    def transform_catch_clause(self, node: CatchClause) -> IrNode:
        return (
            IrNode.from_attribute(Attributes.CatchClause)
            .append_field(Attributes.ArgumentName(self.transform(node.param)))
            .append_field(
                Attributes.Children([self.transform(n) for n in node.body.body])
            )
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
