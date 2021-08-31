class Formatter:
    def to_string(self):
        pass


class IndentedWriter(Formatter):
    def __init__(self, tab='    '):
        self.indent = 0
        self.tab = tab
        self.content = []

    def try_indent(self):
        try:
            if self.content[-1] == '\n':
                self.content.append(self.tab * self.indent)
        except:
            pass

    def append_line(self, s: str):
        if not isinstance(s, str):
            raise Exception('Value must be str')
        self.try_indent()
        self.content.extend([s, '\n'])

    def append(self, s: str):
        if not isinstance(s, str):
            raise Exception('Value must be str')
        self.try_indent()
        self.content.append(s)

    def incr_indent(self):
        self.indent += 1

    def decr_indent(self):
        self.indent -= 1

    def to_string(self):
        return ''.join(self.content)

    def clone(self):
        writer = IndentedWriter()
        writer.indent = self.indent
        writer.content = self.content[:]
        writer.tab = self.tab
        return writer


class Formatee:
    def format_with(self, formatter: Formatter):
        pass


class Braces(Formatee):
    def __init__(self, val: Formatee, open='{', close='}', new_line=True):
        self.value = val
        self.open = open
        self.close = close
        self.new_line = new_line

    def format_with(self, writer: IndentedWriter):
        if self.new_line:
            writer.append_line(self.open)
            writer.incr_indent()
        else:
            writer.append(self.open)

        self.value.format_with(writer)

        if self.new_line:
            writer.decr_indent()
            writer.append_line(self.close)
        else:
            writer.append(self.close)


class IndentBlock(Braces):
    def __init__(self, val: Formatee):
        super().__init__(val, open=':', close='')


class Function(Formatee):
    def __init__(self, func):
        self.func = func

    def format_with(self, writer: IndentedWriter):
        self.func(writer)


class Text(Formatee):
    def __init__(self, text):
        self.text = text

    def format_with(self, writer: IndentedWriter):
        writer.append(self.text)
