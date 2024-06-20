# Load Test

This Section is dedicated to run and evaluate the load test, if the docker build is used.

## Install Python Packages
```bash
pip install -r requirements.txt
```

## Load Test Execution

The file requests.py will execute the load-test and safe the response (time taken) in responses.csv.
```bash
python send_requests.py
```

## Load Test Evaluation

The file requests_eval.py will use the data from responses.csv and visualize it.
```bash
python requests_eval.py
```