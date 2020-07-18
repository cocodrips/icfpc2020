import os

from flask import Flask, request, render_template, Response
from api import demodulator, visualizer
from jinja2 import Template, Environment, FileSystemLoader

app = Flask(__name__)


# web interface
@app.route('/demodulator')
def demodulator_web():
    value = request.args.get("value")
    result = demodulator.demodulate(value)
    return render_template("demodulator.html", base=value, value=result)


@app.route('/visualizer')
def visualizer_web():
    vector = request.args.get("vector")
    width, height, scale, data = visualizer.visualize()
    return render_template("visualizer.html",
                           max_width=width, max_height=height,
                           scale=scale, data=data,
                           vector=vector)


# api
@app.route('/demodulate')
def demodulator_api():
    value = request.args.get("value")
    return demodulator.demodulate(value)


@app.route('/')
def hello_world():
    env = Environment(loader=FileSystemLoader('./templates'), trim_blocks=False)
    template = env.get_template('index.html')
    return template.render()


@app.route('/', defaults={'path': ''})
@app.route('/<path:path>')
def get_resource(path):  # pragma: no cover

    def root_dir():  # pragma: no cover
        return os.path.abspath(os.path.dirname(__file__))

    def get_file(filename):  # pragma: no cover
        try:
            src = os.path.join(root_dir(), filename)
            return open(src).read()
        except IOError as exc:
            return str(exc)

    mimetypes = {
        ".css": "text/css",
        ".html": "text/html",
        ".js": "application/javascript",
        ".md": "text/plain"
    }
    complete_path = os.path.join(root_dir(), path)
    ext = os.path.splitext(path)[1]
    mimetype = mimetypes.get(ext, "text/html")
    content = get_file(complete_path)
    return Response(content, mimetype=mimetype)


if __name__ == "__main__":
    import os

    os.environ["PATH"] = f"{os.environ.get('PATH')}:_build/demodulator/bin"
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 8080)))
