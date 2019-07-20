from keras.applications import InceptionResNetV2
from keras.preprocessing.image import img_to_array
from keras.applications.inception_resnet_v2 import preprocess_input
from keras.models import load_model
from PIL import Image
import numpy as np
import flask
import io
import tensorflow as tf
import functools
from keras import backend

app = flask.Flask(__name__)
model_path = 'inceptionResnet-model-retrain100-precision.h5'

def tf_metric_wrap(method):
  @functools.wraps(method)
  def wrapper(self,args, **kwargs):
    value, update_op = method(self,args, **kwargs)
    backend.get_session().run(tf.local_variables_initializer())
    with tf.control_dependencies([update_op]):
      value = tf.identity(value)
    return value
  return wrapper

def prepare_image(image, target):
	if image.mode != "RGB":
		image = image.convert("RGB")
	if image.size != target:
		image = image.resize(target)
	image = img_to_array(image)
	image = np.expand_dims(image, axis=0)
	image = preprocess_input(image)
	return image

@app.route("/predict", methods=["POST"])
def predict():
	data = {"success": False}
	label_list=['calcium_deficiency','healthy','kalium_deficiency','nitrogen_deficiency','phosporus_deficiency']
	label_list= sorted(label_list)
	if flask.request.method == "POST":
		if flask.request.files.get("image"):
			image = flask.request.files["image"].read()
			image = Image.open(io.BytesIO(image))
			image = prepare_image(image, target=(299, 299))
			with graph.as_default():
				preds = model.predict(image)
				data["predictions"] = []
				result = list(preds[0]).index(max(preds[0]))
				# for idx, result in enumerate(preds[0]):
				#  	r = {"label": label_list[idx], "probability": float(result)}
				#  	data["predictions"].append(r)
				data["predictions"].append(label_list[result])
				data["success"] = True
	return flask.jsonify(data)

if __name__ == "__main__":
    global model
    precision = tf_metric_wrap(tf.metrics.precision)
    recall = tf_metric_wrap(tf.metrics.recall)
    model = load_model(model_path,custom_objects={'precision':precision,'recall':recall})
    global graph
    graph = tf.get_default_graph()
    app.run(host='0.0.0.0')