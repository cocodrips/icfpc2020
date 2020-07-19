import os

from flask import Flask, request, render_template, Response
from api import modem, visualizer, interactor
from api.protocol import galaxy
from jinja2 import Template, Environment, FileSystemLoader
import json

app = Flask(__name__)


# web interface
@app.route('/modem', methods=["GET", "POST"])
def demodulator_web():
    demodulator_binary = request.form.get("demodulator-binary")
    demodulator_output = modem.demodulate(demodulator_binary)

    modulator_list = request.form.get("modulator-list")
    modulator_output = modem.modulate(modulator_list)
    modulator_func = modem.demodulate(modulator_output)
    print(modulator_output)
    return render_template("modem.html",
                           demodulator_binary=demodulator_binary,
                           demodulator_output=demodulator_output,
                           modulator_list=modulator_list,
                           modulator_output=modulator_output,
                           modulator_func=modulator_func)

@app.route('/galaxy')
def galaxy_web():
    gal_state = request.args.get("gal_state")
    gal_var = request.args.get("gal_var")
    galaxy_output = interactor.interact("galaxy", gal_state, gal_var, 0, False)

    return render_template("galaxy.html",
            gal_state=gal_state,
            gal_var=gal_var,
            galaxy_output=galaxy_output)

@app.route('/visualizer', methods=["GET", "POST"])
def visualizer_web():
    raw_data = request.form.get("raw_data")
    pictures = visualizer.visualize(raw_data)

    return render_template("visualizer.html",
                           raw_data=raw_data,
                           pictures=pictures)

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
    return interactor.interact(protocol, state, value, max_index, True)

@app.route('/interact-dummy')
def interact_dummy_api():
    protocol = request.args.get("protocol")
    state = request.args.get("state")
    value = request.args.get("value")
    max_index = request.args.get("max_index")
    return interactor.interact(protocol, state, value, max_index, False)

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

@app.route('/protocol/galaxy')
def protocol_galaxy_api():
    value = request.args.get("value")
    return galaxy.convert(value.split("_"))


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
