import os

from flask import Flask, request, render_template, Response
from api import modem, visualizer, interactor
from jinja2 import Template, Environment, FileSystemLoader
import json

app = Flask(__name__)


# web interface
@app.route('/modem')
def demodulator_web():
    demodulator_binary = request.args.get("demodulator-binary")
    demodulator_output = modem.demodulate(demodulator_binary)

    modulator_list = request.args.get("modulator-list")
    modulator_output = modem.modulate(modulator_list)
    modulator_func = modem.demodulate(modulator_output)
    print(modulator_output)
    return render_template("modem.html",
                           demodulator_binary=demodulator_binary,
                           demodulator_output=demodulator_output,
                           modulator_list=modulator_list,
                           modulator_output=modulator_output,
                           modulator_func=modulator_func)


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
    return modem.demodulate(value)


@app.route('/modulate')
def modulator_api():
    value = request.args.get("value")
    return modem.modulate(value)


@app.route('/interact')
def interact_api():
    protocol = request.args.get("protocol")
    state = request.args.get("state")
    value = request.args.get("value")
    max_index = request.args.get("max_index")
    return interactor.interact(protocol, state, value, max_index)


@app.route('/protocol/dummy')
def protocol_dummy_api():
    state = request.args.get("state")
    value = request.args.get("value")
    return json.dumps({
        "flag": 1,
        "state": state,
        "value": value,
    })


@app.route('/protocol/statelessdraw')
def protocol_statelessdraw_api():
    state = request.args.get("state")
    value = request.args.get("value")
    return json.dumps({
        "flag": 0,
        "state": 0,
        "value": value,
    })


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

    os.environ["PATH"] = f"{os.environ.get('PATH')}:bin"
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 8080)))
