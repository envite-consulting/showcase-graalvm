import argparse
import requests
import csv
import re

base_url = "http://localhost:8085/load-test"
url_mapping = {
    "jvm": "http://graalvm-demo-book-jvm:8080",
    "jlink": "http://graalvm-demo-book-jvm-jlink:8080",
    "cds": "http://graalvm-demo-book-jvm-jlink-cds:8080",
    "aot": "http://graalvm-demo-book-jvm-jlink-cds-aot:8080",
    "native": "http://graalvm-demo-book-native:8080"
}

body_template = {
    "webClientUrl": "",
    "webClientEndpoint": "/books/bulk",
    "numberOfBooks": 30,
    "numberOfRequests": 30,
    "concurrentUsers": 20
}


def send_request(book_web_client_url):
    body = body_template.copy()
    body["webClientUrl"] = book_web_client_url

    try:
        response = requests.post(base_url, json=body)
        response.raise_for_status()

        web_client_response = response.text
        print(f"Response for {book_web_client_url} content: {web_client_response}")

        return web_client_response
    except requests.exceptions.RequestException as e:
        print(f"Failed to get response for {book_web_client_url}: {e}")
        return None


def is_valid_file(file_path):
    if not file_path.endswith('.csv'):
        return False
    return True


def main():
    parser = argparse.ArgumentParser(description="Run load-test multiple times.")
    parser.add_argument("-u", "--urls", metavar='', type=str, required=True,
                        help="Comma-separated list of URL short names (e.g., 'jvm,cds,native').")
    parser.add_argument("-r", "--runs", metavar='', type=int, default=30,
                        help="Number of runs for each URL (default: 20).")
    parser.add_argument("-f", "--file", metavar='', type=str, default="./load-test/responses.csv",
                        help="Path and name for results storage (default: ./load-test/responses.csv)")

    args = parser.parse_args()
    selected_urls = args.urls.split(',')
    runs = args.runs
    responses_file = args.file

    default_file_path = './load-test/responses.csv'
    if not is_valid_file(responses_file):
        print(f"Invalid file path provided. Using default file: {default_file_path}")
        responses_file = default_file_path

    book_web_client_urls = [url_mapping[url] for url in selected_urls if url in url_mapping]
    responses_dict = {url: [] for url in book_web_client_urls}

    with open(responses_file, 'w', newline='') as csvfile:
        csvwriter = csv.writer(csvfile)
        url_short_names = [re.sub(r'^http://graalvm-demo-book-(.*?):8080$', r'\1', url) for url in book_web_client_urls]
        csvwriter.writerow(url_short_names)
        print(f"Initialized CSV file: {responses_file}")

    for web_client_url in book_web_client_urls:
        for _ in range(runs):
            response_text = send_request(web_client_url)
            if response_text:
                time_value = re.search(r"Time taken:\s*(\d+)ms", response_text)
                responses_dict[web_client_url].append(time_value.group(1))

    with open(responses_file, 'a', newline='') as csvfile:
        csvwriter = csv.writer(csvfile)
        for i in range(runs):
            row = [responses_dict[url][i] if i < len(responses_dict[url]) else "" for url in book_web_client_urls]
            csvwriter.writerow(row)

    print(f"Responses saved to {responses_file}.")


if __name__ == "__main__":
    main()
