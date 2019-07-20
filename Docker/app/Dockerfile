FROM python:3.5.4
WORKDIR /app
COPY requirements.txt /app
RUN pip install -r ./requirements.txt
COPY detect.py /app
COPY inceptionResnet-model-retrain100-precision.h5 /app
CMD ["python", "detect.py"]~