class ReportFormat(object):
    PDF = 0
    TEXT = 1


class Report(object):
    """Strategy context."""

    def __init__(self, format_):
        self.title = 'Monthly report'
        self.text = ['Things are going', 'really, really well.']
        self.format_ = format_


class Handler(object):

    def __init__(self):
        self.nextHandler = None

    def handle(self, request):
        self.nextHandler.handle(request)


class PDFHandler(Handler):

    def handle(self, request):
        if request.format_ == ReportFormat.PDF:
            self.output_report(request.title, request.text)
        else:
            super(PDFHandler, self).handle(request)

    def output_report(self, title, text):
        print( '<html>')
        print( '  <head>')
        print( '    <title>%s</title>' % title)
        print( '  </head>')
        print( '  <body>')
        for line in text:
            print( '    <p>%s</p>' % line)
        print( '  </body>')
        print( '</html>')


class TextHandler(Handler):

    def handle(self, request):
        if request.format_ == ReportFormat.TEXT:
            self.output_report(request.title, request.text)
        else:
            super(TextHandler, self).handle(request)

    def output_report(self, title, text):
        print( 5*'*' + title + 5*'*')
        for line in text:
            print( line)


class ErrorHandler(Handler):

    def handle(self, request):
        print( "Invalid request")


if __name__ == '__main__':
    report = Report(ReportFormat.PDF)
    pdf_handler = PDFHandler()
    text_handler = TextHandler()

    pdf_handler.nextHandler = text_handler
    text_handler.nextHandler = ErrorHandler()

    pdf_handler.handle(report)
